package com.android.tipit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Created by Eugen on 23.10.2017.
 */

public class Tipit {

    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;
    private static final String MODEL_FILE = "file:///android_asset/constant_graph_weights.pb";
    private static final String INPUT_NODE = "data_1:0";
    private static final String OUTPUT_NODE = "up_tiny2vga_1/conv2d_transpose:0";
    private int[] intValues = new int[WIDTH * HEIGHT];
    private float[] floatValues = new float[WIDTH * HEIGHT * 3];
    private TensorFlowInferenceInterface tensorflow;

    public static void drawRectangleOnBitmap(Bitmap bitmap, int x, int y) {
        int x1 = x + 10;
        int y1 = y + 10;
        for (int i = x; i <= x1; i++) {
            for (int k = y; k <= y1; k++) {
                bitmap.setPixel(i, k, Color.rgb(255, 0, 0));
            }
        }
    }

    public void Tipit(Context context) {
        init(context);
    }

    private void init(Context context) {
        tensorflow = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);
    }

    public void processImageWithTensorFlow(Context context,final Bitmap bitmap) {
        tensorflow = new TensorFlowInferenceInterface(context.getAssets(), MODEL_FILE);
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
