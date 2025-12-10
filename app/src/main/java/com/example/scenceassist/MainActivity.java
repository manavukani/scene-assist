package com.example.scenceassist;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

public class MainActivity extends AppCompatActivity implements ImageAnalysis.Analyzer {

    private PreviewView previewView;
    private Button describeBtn, askBtn;
    private ExecutorService cameraExecutor;
    private Bitmap currentFrame;
    private boolean describeClick = false, askClick = false;
    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final int STT_REQUEST_CODE = 100;
    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private TextToSpeech tts;
    private final ExecutorService llmExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.viewFinder);
        describeBtn = findViewById(R.id.describeBtn);
        askBtn = findViewById(R.id.askBtn);

        cameraExecutor = Executors.newSingleThreadExecutor();

        if (allPermissionsGranted()) startCamera();
        else ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);

        describeBtn.setOnClickListener(v -> describeClick = true);
        askBtn.setOnClickListener(v -> startSpeechToText());

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.getDefault());
                tts.setSpeechRate(1.0f);
            }
        });

    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindCamera(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCamera(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(cameraExecutor, this);

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.unbindAll();
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    }

    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        try {
            currentFrame = imageProxy.toBitmap();

            if (describeClick && currentFrame != null) {
//                Toast.makeText(this, "Describe Clicked", Toast.LENGTH_SHORT).show();
                describeClick = false;
                GlobalBitmap.setBitmap(currentFrame);
                runOnUiThread(() -> openDescribeActivity("Describe this scene for a visually impaired person."));
            }

//            if (askClick && currentFrame != null) {
//                askClick = false;
//                GlobalBitmap.setBitmap(currentFrame);
//                runOnUiThread(() -> openDescribeActivity(currentQuestion));
//            }

//            if (askClick && currentFrame != null) {
//                askClick = false;
//                Bitmap frameCopy = Bitmap.createBitmap(currentFrame);
//                runVisibilityCheck(frameCopy, currentQuestion);
//            }

            if (askClick && currentFrame != null) {
                askClick = false;
                Bitmap originalFrame = Bitmap.createBitmap(currentFrame);   // keep original
                Bitmap rotatedFrame = rotateBitmap(originalFrame, -90);      // rotated for LLM

                runVisibilityCheck(rotatedFrame, originalFrame, currentQuestion);

            }



        } catch (Exception e) {
            Log.e("Analyzer", "Frame error: " + e.getMessage());
        } finally {
            imageProxy.close();
        }
    }

    private String currentQuestion = "";

    private void startSpeechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask about this scene...");
        startActivityForResult(intent, STT_REQUEST_CODE);
    }

    private void runVisibilityCheck(Bitmap frame, Bitmap originalFrame, String question) {
        try {
            GenerativeModel gm = new GenerativeModel("gemini-2.0-flash", "API_KEY_HERE"); // removed the API key for saftey
            GenerativeModelFutures model = GenerativeModelFutures.from(gm);

            // LLM must respond ONLY with:
            // "OK"  OR  "PARTIAL: move left/right"  OR  "NOT_FOUND"
            Content content = new Content.Builder()
                    .addText(
                            "You are a vision assistant. A user asked: \"" + question + "\".\n" +
                                    "Analyze the image and determine if the information asked about is completely and fully refer visible.\n\n" +
                                    "Respond using ONE of the following formats ONLY:\n" +
                                    "1. If fully visible → reply exactly: OK\n" +
                                    "2. If partially visible and not full information to judge or provide the output → reply like: PARTIAL: move slightly left/right/up/down\n" +
                                    "3. If not visible → reply exactly: NOT_FOUND"
                    )
                    .addImage(frame)
                    .build();

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String reply = result.getText().trim().toUpperCase();
                    Log.i("LLM-VIS", "LLM said: " + reply);

                    runOnUiThread(() -> handleVisibilityResult(reply, question, frame, originalFrame));
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("LLM-VIS", "LLM error", t);
                    runOnUiThread(() -> speak("Scene Assist is unavailable right now."));
                }
            }, llmExecutor);

        } catch (Exception e) {
            Log.e("LLM-VIS", "Exception", e);
            speak("Something went wrong.");
        }
    }

    private void handleVisibilityResult(String reply, String question, Bitmap frame, Bitmap originalFrame) {

        if (reply.equals("OK")) {
            // Fully visible → go to next screen immediately
            GlobalBitmap.setBitmap(originalFrame);
            openDescribeActivity(question);
            return;
        }

        if (reply.startsWith("PARTIAL")) {
            String instruction = reply.replace("PARTIAL:", "").trim();
            speak(instruction);
            return;
        }

        if (reply.equals("NOT_FOUND")) {
            speak("No such item found in the camera view.");
            return;
        }

        // Fallback if LLM misbehaves
        speak("I did not understand the response.");
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                currentQuestion = results.get(0);
                askClick = true;
            }
        }
    }

    private void openDescribeActivity(String question) {
        Intent intent = new Intent(MainActivity.this, DescribeSceneWindow.class);
        intent.putExtra("question", question);
        startActivity(intent);
    }

    private boolean allPermissionsGranted() {
        for (String perm : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    private void speak(String msg) {
        if (tts != null) {
            tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "tts_id_main");
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
        if (tts != null) { tts.stop(); tts.shutdown(); }

    }

    private Bitmap rotateBitmap(Bitmap original, float degrees) {
        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);
        return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
    }

}




