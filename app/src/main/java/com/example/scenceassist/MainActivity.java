package com.example.scenceassist;

//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.speech.RecognizerIntent;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.ImageCapture;
//import androidx.camera.core.ImageCaptureException;
//import androidx.camera.core.Preview;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.camera.view.PreviewView;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.google.common.util.concurrent.ListenableFuture;
//
//import java.util.ArrayList;
//import java.util.Locale;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class MainActivity extends AppCompatActivity {
//
//    private PreviewView previewView;
//    private Button describeBtn, askBtn;
//    private ExecutorService cameraExecutor;
//    private ImageCapture imageCapture;
//    private static final int REQUEST_CODE_PERMISSIONS = 10;
//    private static final String[] REQUIRED_PERMISSIONS =
//            new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
//    private static final int STT_REQUEST_CODE = 100;
//
//    private ActivityResultLauncher<Intent> sttLauncher;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        previewView = findViewById(R.id.viewFinder);
//        describeBtn = findViewById(R.id.describeBtn);
//        askBtn = findViewById(R.id.askBtn);
//        cameraExecutor = Executors.newSingleThreadExecutor();
//
//        if (allPermissionsGranted()) {
//            startCamera();
//        } else {
//            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
//        }
//
//        // STT launcher
//        sttLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                        ArrayList<String> results = result.getData()
//                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                        if (results != null && !results.isEmpty()) {
//                            captureCurrentFrameAndOpenDescribe(results.get(0));
//                        }
//                    }
//                });
//
//        describeBtn.setOnClickListener(v ->
//                captureCurrentFrameAndOpenDescribe("Describe this image in detail for a visually impaired person."));
//
//        askBtn.setOnClickListener(v -> startSpeechToText());
//    }
//
//    private void startCamera() {
//        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
//                ProcessCameraProvider.getInstance(this);
//        cameraProviderFuture.addListener(() -> {
//            try {
//                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                bindPreview(cameraProvider);
//            } catch (ExecutionException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }, ContextCompat.getMainExecutor(this));
//    }
//
//    private void bindPreview(ProcessCameraProvider cameraProvider) {
//        Preview preview = new Preview.Builder().build();
//        imageCapture = new ImageCapture.Builder().build();
//        CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;
//
//        preview.setSurfaceProvider(previewView.getSurfaceProvider());
//        cameraProvider.unbindAll();
//        cameraProvider.bindToLifecycle(this, selector, preview, imageCapture);
//    }
//
//    private void captureCurrentFrameAndOpenDescribe(String question) {
//        if (imageCapture == null) {
//            Toast.makeText(this, "Camera not ready!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
//                new ImageCapture.OnImageCapturedCallback() {
//                    @Override
//                    public void onCaptureSuccess(@NonNull androidx.camera.core.ImageProxy image) {
//                        super.onCaptureSuccess(image);
//                        android.graphics.Bitmap bitmap = BitmapUtils.imageProxyToBitmap(image);
//                        image.close();
//
//                        if (bitmap != null) {
//                            JyotiGlobalBitmap.setBitmap(bitmap);
//                            openDescribeActivity(question);
//                        } else {
//                            Toast.makeText(MainActivity.this, "Failed to capture frame!", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onError(@NonNull ImageCaptureException exception) {
//                        Toast.makeText(MainActivity.this, "Capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    private void startSpeechToText() {
//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask Jyoti about this scene...");
//        sttLauncher.launch(intent);
//    }
//
//    private void openDescribeActivity(String question) {
//        Intent intent = new Intent(MainActivity.this, DescribeSceneWindow.class);
//        intent.putExtra("question", question);
//        startActivity(intent);
//    }
//
//    private boolean allPermissionsGranted() {
//        for (String permission : REQUIRED_PERMISSIONS) {
//            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
//                return false;
//        }
//        return true;
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (allPermissionsGranted()) startCamera();
//        else Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        cameraExecutor.shutdown();
//    }
//}

//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.speech.RecognizerIntent;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.ImageCapture;
//import androidx.camera.core.ImageCaptureException;
//import androidx.camera.core.Preview;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.camera.view.PreviewView;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.google.common.util.concurrent.ListenableFuture;
//
//import java.util.ArrayList;
//import java.util.Locale;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class MainActivity extends AppCompatActivity {
//
//    private PreviewView previewView;
//    private Button describeBtn, askBtn;
//    private ExecutorService cameraExecutor;
//    private ImageCapture imageCapture;
//    private static final int REQUEST_CODE_PERMISSIONS = 10;
//    private static final String[] REQUIRED_PERMISSIONS =
//            new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
//    private static final int STT_REQUEST_CODE = 100;
//
//    private ActivityResultLauncher<Intent> sttLauncher;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        previewView = findViewById(R.id.viewFinder);
//        describeBtn = findViewById(R.id.describeBtn);
//        askBtn = findViewById(R.id.askBtn);
//        cameraExecutor = Executors.newSingleThreadExecutor();
//
//        if (allPermissionsGranted()) {
//            startCamera();
//        } else {
//            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
//        }
//
//        // STT launcher
//        sttLauncher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                result -> {
//                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
//                        ArrayList<String> results = result.getData()
//                                .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                        if (results != null && !results.isEmpty()) {
//                            captureCurrentFrameAndOpenDescribe(results.get(0));
//                        }
//                    }
//                });
//
//        describeBtn.setOnClickListener(v ->
//                captureCurrentFrameAndOpenDescribe("Describe this image in detail for a visually impaired person."));
//
//        askBtn.setOnClickListener(v -> startSpeechToText());
//    }
//
//    private void startCamera() {
//        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
//                ProcessCameraProvider.getInstance(this);
//        cameraProviderFuture.addListener(() -> {
//            try {
//                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                bindPreview(cameraProvider);
//            } catch (ExecutionException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }, ContextCompat.getMainExecutor(this));
//    }
//
//    private void bindPreview(ProcessCameraProvider cameraProvider) {
//        Preview preview = new Preview.Builder().build();
//        imageCapture = new ImageCapture.Builder().build();
//        CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;
//
//        preview.setSurfaceProvider(previewView.getSurfaceProvider());
//        cameraProvider.unbindAll();
//        cameraProvider.bindToLifecycle(this, selector, preview, imageCapture);
//    }
//
//    private void captureCurrentFrameAndOpenDescribe(String question) {
//        if (imageCapture == null) {
//            Toast.makeText(this, "Camera not ready!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
//                new ImageCapture.OnImageCapturedCallback() {
//                    @Override
//                    public void onCaptureSuccess(@NonNull androidx.camera.core.ImageProxy image) {
//                        super.onCaptureSuccess(image);
//                        android.graphics.Bitmap bitmap = BitmapUtils.imageProxyToBitmap(image);
//                        image.close();
//
//                        if (bitmap != null) {
//                            JyotiGlobalBitmap.setBitmap(bitmap);
//                            openDescribeActivity(question);
//                        } else {
//                            Toast.makeText(MainActivity.this, "Failed to capture frame!", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                    @Override
//                    public void onError(@NonNull ImageCaptureException exception) {
//                        Toast.makeText(MainActivity.this, "Capture failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }
//
//    private void startSpeechToText() {
//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask your question about this scene...");
//        sttLauncher.launch(intent);
//    }
//
//    private void openDescribeActivity(String question) {
//        Intent intent = new Intent(MainActivity.this, DescribeSceneWindow.class);
//        intent.putExtra("question", question);
//        startActivity(intent);
//    }
//
//    private boolean allPermissionsGranted() {
//        for (String permission : REQUIRED_PERMISSIONS) {
//            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
//                return false;
//        }
//        return true;
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (allPermissionsGranted()) startCamera();
//        else Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        cameraExecutor.shutdown();
//    }
//}


//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.os.Bundle;
//import android.speech.RecognizerIntent;
//import android.widget.Button;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.core.CameraSelector;
//import androidx.camera.core.ImageAnalysis;
//import androidx.camera.core.ImageProxy;
//import androidx.camera.core.Preview;
//import androidx.camera.lifecycle.ProcessCameraProvider;
//import androidx.camera.view.PreviewView;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.google.common.util.concurrent.ListenableFuture;
//
//import java.util.ArrayList;
//import java.util.Locale;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//
//public class MainActivity extends AppCompatActivity {
//
//    private PreviewView previewView;
//    private Button describeBtn, askBtn;
//    private ExecutorService cameraExecutor;
//    private Bitmap currentFrameBitmap;
//    private static final int REQUEST_CODE_PERMISSIONS = 10;
//    private static final String[] REQUIRED_PERMISSIONS = {
//            Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO
//    };
//    private static final int STT_REQUEST_CODE = 100;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        previewView = findViewById(R.id.viewFinder);
//        describeBtn = findViewById(R.id.describeBtn);
//        askBtn = findViewById(R.id.askBtn);
//        cameraExecutor = Executors.newSingleThreadExecutor();
//
//        if (allPermissionsGranted()) {
//            startCamera();
//        } else {
//            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
//        }
//
//        describeBtn.setOnClickListener(v -> {
//            if (currentFrameBitmap != null) {
//                JyotiGlobalBitmap.setBitmap(currentFrameBitmap);
//                openDescribeActivity("Describe this image in detail for a visually impaired person.");
//            } else {
//                Toast.makeText(this, "No image available yet!", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//        askBtn.setOnClickListener(v -> startSpeechToText());
//    }
//
//    private void startCamera() {
//        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
//        cameraProviderFuture.addListener(() -> {
//            try {
//                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
//                bindPreview(cameraProvider);
//            } catch (ExecutionException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }, ContextCompat.getMainExecutor(this));
//    }
//
//    private void bindPreview(ProcessCameraProvider cameraProvider) {
//        Preview preview = new Preview.Builder().build();
//        CameraSelector selector = CameraSelector.DEFAULT_BACK_CAMERA;
//
//        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
//                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
//                .build();
//
//        imageAnalysis.setAnalyzer(cameraExecutor, image -> {
//            // Convert live frame to Bitmap
//            currentFrameBitmap = BitmapUtils.imageProxyToBitmap(image);
//            image.close();
//        });
//
//        preview.setSurfaceProvider(previewView.getSurfaceProvider());
//        cameraProvider.unbindAll();
//        cameraProvider.bindToLifecycle(this, selector, preview, imageAnalysis);
//    }
//
//    private void startSpeechToText() {
//        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
//        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask Jyoti about this scene...");
//        startActivityForResult(intent, STT_REQUEST_CODE);
//    }
//
//    private void openDescribeActivity(String question) {
//        Intent intent = new Intent(MainActivity.this, DescribeSceneWindow.class);
//        intent.putExtra("question", question);
//        startActivity(intent);
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == STT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
//            ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//            if (result != null && !result.isEmpty()) {
//                if (currentFrameBitmap != null) {
//                    JyotiGlobalBitmap.setBitmap(currentFrameBitmap);
//                    openDescribeActivity(result.get(0));
//                } else {
//                    Toast.makeText(this, "No image available yet!", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    }
//
//    private boolean allPermissionsGranted() {
//        for (String permission : REQUIRED_PERMISSIONS) {
//            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED)
//                return false;
//        }
//        return true;
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (allPermissionsGranted()) startCamera();
//        else Toast.makeText(this, "Permissions not granted.", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        cameraExecutor.shutdown();
//    }
//}

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
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

            if (askClick && currentFrame != null) {
                askClick = false;
                GlobalBitmap.setBitmap(currentFrame);
                runOnUiThread(() -> openDescribeActivity(currentQuestion));
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}




