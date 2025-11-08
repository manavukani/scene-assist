package com.example.scenceassist;

import android.graphics.Bitmap;

public class GlobalBitmap {
    private static Bitmap bitmap;

    public static void setBitmap(Bitmap b) {
        bitmap = b;
    }

    public static Bitmap getBitmap() {
        return bitmap;
    }
}
