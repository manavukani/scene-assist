package com.example.scenceassist;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Runs Real-ESRGAN-General-x4v3.tflite to enhance low-light or blurry images before
 * sending them to Gemini.  Works fully offline using TensorFlow Lite.
 */
public class ESRGANEnhancer {

    private static final String MODEL_PATH = "real_esrgan_x4v3.tflite\n";
    private static final String TAG = "ESRGANEnhancer";

    /** Loads TFLite model from assets and performs 4× super-resolution */
    public static Bitmap enhance(Bitmap src, Context context) {
        try {
            MappedByteBuffer modelBuffer = loadModel(context, MODEL_PATH);
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            Interpreter interpreter = new Interpreter(modelBuffer, options);

            int inW = src.getWidth();
            int inH = src.getHeight();
            int outW = inW * 4;
            int outH = inH * 4;

            // Prepare input
            float[][][][] input = new float[1][inH][inW][3];
            for (int y = 0; y < inH; y++) {
                for (int x = 0; x < inW; x++) {
                    int px = src.getPixel(x, y);
                    input[0][y][x][0] = ((px >> 16) & 0xFF) / 255f;
                    input[0][y][x][1] = ((px >> 8) & 0xFF) / 255f;
                    input[0][y][x][2] = (px & 0xFF) / 255f;
                }
            }

            // Allocate output
            float[][][][] output = new float[1][outH][outW][3];

            interpreter.run(input, output);
            interpreter.close();

            // Convert output to Bitmap
            Bitmap result = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888);
            for (int y = 0; y < outH; y++) {
                for (int x = 0; x < outW; x++) {
                    int r = clamp((int) (output[0][y][x][0] * 255));
                    int g = clamp((int) (output[0][y][x][1] * 255));
                    int b = clamp((int) (output[0][y][x][2] * 255));
                    result.setPixel(x, y, Color.rgb(r, g, b));
                }
            }

            Log.i(TAG, "ESRGAN upscale successful: " + inW + "×" + inH + " → " + outW + "×" + outH);
            return result;

        } catch (Exception e) {
            Log.e(TAG, "ESRGAN failed, returning original image", e);
            return src;
        }
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    private static MappedByteBuffer loadModel(Context context, String modelFile) throws Exception {
        AssetFileDescriptor fd = context.getAssets().openFd(modelFile);
        FileInputStream inputStream = new FileInputStream(fd.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,
                fd.getStartOffset(), fd.getDeclaredLength());
    }
}
