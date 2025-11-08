    package com.example.scenceassist;

    import android.content.Intent;
    import android.graphics.Bitmap;
    import android.graphics.Matrix;
    import android.os.Bundle;
    import android.os.Handler;
    import android.os.Looper;
    import android.speech.RecognizerIntent;
    import android.speech.tts.TextToSpeech;
    import android.util.Log;
    import android.widget.ImageView;
    import android.widget.TextView;
    import android.widget.Toast;

    import androidx.annotation.Nullable;
    import androidx.appcompat.app.AppCompatActivity;

    import com.google.ai.client.generativeai.GenerativeModel;
    import com.google.ai.client.generativeai.java.GenerativeModelFutures;
    import com.google.ai.client.generativeai.type.Content;
    import com.google.ai.client.generativeai.type.GenerateContentResponse;
    import com.google.android.material.floatingactionbutton.FloatingActionButton;
    import com.google.common.util.concurrent.FutureCallback;
    import com.google.common.util.concurrent.Futures;
    import com.google.common.util.concurrent.ListenableFuture;

    import java.util.ArrayList;
    import java.util.Locale;
    import java.util.concurrent.Executor;
    import java.util.concurrent.Executors;

    public class DescribeSceneWindow extends AppCompatActivity {

        private TextView resultView;
        private ImageView imageView;
        private FloatingActionButton askButton;
        private TextToSpeech tts;
        private Bitmap currentBitmap;
        private static final int STT_REQUEST_CODE = 2001;

        private Bitmap currentBitmapCopy;

        private final Executor executor = Executors.newSingleThreadExecutor();
        private final Handler uiHandler = new Handler(Looper.getMainLooper());

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_describe_scene_window);

            resultView = findViewById(R.id.resultView);
            imageView = findViewById(R.id.imageView);
            askButton = findViewById(R.id.askJyotiBtn);

            currentBitmap = GlobalBitmap.getBitmap();

            // Initialize TTS
            tts = new TextToSpeech(this, status -> {
                if (status == TextToSpeech.SUCCESS)
                    tts.setLanguage(Locale.getDefault());
                    tts.setSpeechRate(1.0f);
            });

            String question = getIntent().getStringExtra("question");
            if (question != null && currentBitmap != null) {
                describe(currentBitmap, question);
            }

            askButton.setOnClickListener(v -> startSpeechToText());
        }

        private void startSpeechToText() {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask a question about this scene...");
            try {
                startActivityForResult(intent, STT_REQUEST_CODE);
            } catch (Exception e) {
                Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == STT_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    describe(currentBitmap, result.get(0));
                }
            }
        }

        private void describe(Bitmap bitmap, String question) {
            try {
                // Step 1: ESRGAN enhance
                Bitmap enhanced = ESRGANEnhancer.enhance(bitmap, getApplicationContext());

                // Step 2: Gemini setup (same as Jyoti)
                GenerativeModel gm = new GenerativeModel("gemini-2.0-flash", "AIzaSyAkTO9g_hWjXisPhQ7b4cEST9vmM3a9csI");
                GenerativeModelFutures model = GenerativeModelFutures.from(gm);

                Content content = new Content.Builder()
                        .addText("You are Scene Assist, a helpful assistant for visually impaired. " +
                                "Please provide a clear, accurate, and context-aware description. " +
                                "While returning output, avoid using asterisks (*). " +
                                question)
                        .addImage(enhanced.copy(enhanced.getConfig(), false))
                        .build();

                ListenableFuture<GenerateContentResponse> response = model.generateContent(content);


                Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                    @Override
                    public void onSuccess(GenerateContentResponse result) {
                        String resultText = result.getText();
                        Log.i("Gemini-Success", resultText);
                        String cleaned = resultText != null ? resultText.replace("*", "") :
                                "Sorry, I couldn’t process the image.";
                        uiHandler.post(() -> {
                            resultView.setText(cleaned);
                            speak(cleaned);
                            if (currentBitmap != null) {
                                    Bitmap rotatedBitmap = rotateBitmap(currentBitmap, 90);
                                    imageView.setImageBitmap(rotatedBitmap);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e("Gemini-Failure", "Error generating response", t);
                        uiHandler.post(() -> {
                            resultView.setText("Scene Assist is unavailable right now. Please try again later.");
                            speak("Sorry, Scene Assist is unavailable right now. Please try again later.");
                        });
                    }
                }, executor);

            } catch (Exception e) {
                Log.e("Gemini-Describe", "Error in describe()", e);
                uiHandler.post(() -> {
                    resultView.setText("Something went wrong while processing the image.");
                    speak("Sorry, something went wrong while analyzing the image.");
                });
            }
        }

        private void speak(String text) {
            if (tts != null && text != null && !text.isEmpty()) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sceneassist_tts");
            }
        }



        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }
        }

        public Bitmap rotateBitmap(Bitmap original, float degrees) {
            Matrix matrix = new Matrix();
            matrix.preRotate(degrees);
            return Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
        }
    }
