package com.android.tipitdemoapp;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

public class RenderScriptHelper {


    public static Bitmap convertYuvToRgbIntrinsic(RenderScript rs, byte[] data, int imageWidth, int imageHeight) {

        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.RGBA_8888(rs));

        // Create the input allocation  memory for Renderscript to work with
        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs))
                .setX(imageWidth)
                .setY(imageHeight)
                .setYuvFormat(ImageFormat.NV21);

        Allocation aIn = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
        // Set the YUV frame data into the input allocation
        aIn.copyFrom(data);


        // Create the output allocation
        Type.Builder rgbType = new Type.Builder(rs, Element.RGBA_8888(rs))
                .setX(imageWidth)
                .setY(imageHeight);

        Allocation aOut = Allocation.createTyped(rs, rgbType.create(), Allocation.USAGE_SCRIPT);


        yuvToRgbIntrinsic.setInput(aIn);
        // Run the script for every pixel on the input allocation and put the result in aOut
        yuvToRgbIntrinsic.forEach(aOut);

        // Create an output bitmap and copy the result into that bitmap
        Bitmap outBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        aOut.copyTo(outBitmap);

        return rotate(outBitmap,90);

    }

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        Matrix mtx = new Matrix();
        //       mtx.postRotate(degree);
        mtx.setRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }
}
