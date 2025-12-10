package com.example.scenceassist;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.media.Image;
import android.util.Log;

import androidx.camera.core.ImageProxy;

import java.nio.ByteBuffer;

public class BitmapUtils {

    public static Bitmap imageProxyToBitmap(ImageProxy image) {
        try {
            ImageProxy.PlaneProxy plane = image.getPlanes()[0];
            ByteBuffer buffer = plane.getBuffer();
            int width = image.getWidth();
            int height = image.getHeight();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(bytes));
            return bitmap;
        } catch (Exception e) {
            Log.e("BitmapUtils", "Conversion failed", e);
            return null;
        }
    }
}
