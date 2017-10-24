package com.android.tipit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Created by Eugen on 23.10.2017.
 */

public class Tipit {

    /**
     * Tipit can be used as singleton module class
     * but static method can be called without instance creation.
     */

    private static Tipit instacne;
    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;
    private static final String MODEL_FILE = "file:///android_asset/constant_graph_weights.pb";
    private static final String INPUT_NODE = "data_1:0";
    private static final String OUTPUT_NODE = "up_tiny2vga_1/conv2d_transpose:0";
    private int[] intValues = new int[WIDTH * HEIGHT];
    private float[] floatValues = new float[WIDTH * HEIGHT * 3];
    private TensorFlowInferenceInterface tensorflow;


    /**
     * Draw 10 pixels rectangle depends on input position.
     * Rectangle will be centered to initial coordinates.
     *
     * @param bitmap
     * @param x      initial x position
     * @param y      intial y position
     */
    public static void drawRectangleOnBitmap(Bitmap bitmap, int x, int y) {
        int x1 = x + 10;
        int y1 = y + 10;
        for (int i = x; i <= x1; i++) {
            for (int k = y; k <= y1; k++) {
                bitmap.setPixel(i, k, Color.rgb(255, 0, 0));
            }
        }
    }

    private Tipit(Context context) {
        initTensorflow(context);
    }

    public static Tipit getInstance(@NonNull Context context) {
        if (instacne == null) {
            instacne = new Tipit(context);
        }
        return instacne;
    }

    public void Tipit(Context context) {
        initTensorflow(context);
    }

    private void initTensorflow(Context context) {
        if (tensorflow == null) {
            tensorflow = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);
        }
    }

    /**
     * Process image as bitmap using tesnorflow model file located in library assets folder.
     *
     * @param bitmap  is rgba format with max size 640x480
     */

    public void processImageWithTensorFlow(final Bitmap bitmap) {
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF) / 255.0f;
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;
            floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f;
        }

        tensorflow.feed(
                INPUT_NODE, floatValues, 1, bitmap.getWidth(), bitmap.getHeight(), 3);

        tensorflow.run(new String[]{OUTPUT_NODE}, false);
        tensorflow.fetch(OUTPUT_NODE, floatValues);

        for (int i = 0; i < intValues.length; ++i) {
            intValues[i] =
                    0xFF000000
                            | (((int) (floatValues[i * 3] * 255)) << 16)
                            | (((int) (floatValues[i * 3 + 1] * 255)) << 8)
                            | ((int) (floatValues[i * 3 + 2] * 255));
        }


        bitmap.setPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    }
}
