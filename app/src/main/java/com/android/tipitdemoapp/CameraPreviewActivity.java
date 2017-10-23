package com.android.tipitdemoapp;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.RenderScript;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.tipit.Tipit;
import com.android.tipitdemoapp.databinding.ActivityCameraPreviewBinding;

import io.fotoapparat.Fotoapparat;
import io.fotoapparat.parameter.LensPosition;
import io.fotoapparat.parameter.ScaleType;
import io.fotoapparat.parameter.Size;
import io.fotoapparat.preview.Frame;
import io.fotoapparat.preview.FrameProcessor;

import static io.fotoapparat.parameter.selector.FocusModeSelectors.autoFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.continuousFocus;
import static io.fotoapparat.parameter.selector.FocusModeSelectors.fixed;
import static io.fotoapparat.parameter.selector.LensPositionSelectors.lensPosition;
import static io.fotoapparat.parameter.selector.Selectors.firstAvailable;

public class CameraPreviewActivity extends AppCompatActivity {

    private static final String TAG = CameraPreviewActivity.class.getSimpleName();
    private static final int WIDTH = 480;
    private static final int HEIGHT = 640;
    public ObservableBoolean mode = new ObservableBoolean();
    ActivityCameraPreviewBinding binding;

    private Fotoapparat backFotoapparat;
    private RenderScript mRs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera_preview);
        binding.setActivity(this);
        mRs = RenderScript.create(this);
//        backFotoapparat = createFotoapparat(LensPosition.BACK);
        processImageTensorflowly();
    }

    private Fotoapparat createFotoapparat(LensPosition position) {
        return Fotoapparat
                .with(this)
                .into(binding.cameraView)
                .previewScaleType(ScaleType.CENTER_CROP)
                .previewSize(items -> new Size(WIDTH, HEIGHT))
                .lensPosition(lensPosition(position))
                .focusMode(firstAvailable(
                        continuousFocus(),
                        autoFocus(),
                        fixed()
                ))
                .frameProcessor(new SampleFrameProcessor())
                .cameraErrorCallback(e ->
                        Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show())
                .build();
    }

    private class SampleFrameProcessor implements FrameProcessor {

        @Override
        public void processFrame(Frame frame) {
            Log.d(TAG, "processFrame: Frame");
            Bitmap rgbBitmap = RenderScriptHelper.convertYuvToRgbIntrinsic(mRs, frame.image, WIDTH, HEIGHT);
//            Tipit.drawRectangleOnBitmap(rgbBitmap, rgbBitmap.getWidth() / 2, rgbBitmap.getHeight() / 2);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(rgbBitmap, HEIGHT,
                    WIDTH, false);

            Tipit tipit = new Tipit();
//            tipit.processImageWithTensorFlow(CameraPreviewActivity.this, scaledBitmap);
            runOnUiThread(() ->
                    binding.rgbPreview.setImageBitmap(rgbBitmap)
            );
        }
    }

    public void processImageTensorflowly() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap realImage = BitmapFactory.decodeResource(getResources(), R.drawable.man_picture, options);
        Log.d(TAG, "processImageTensorflowly: real Image size: " + realImage.getWidth() + " : " + realImage.getHeight());
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(realImage, HEIGHT,
                WIDTH, false);
        Log.d(TAG, "processImageTensorflowly: real image after scale" + scaledBitmap.getWidth() + " : " + scaledBitmap.getHeight());
        Tipit tipit = new Tipit();
        tipit.processImageWithTensorFlow(this, scaledBitmap);
        binding.rgbPreview.setImageBitmap(scaledBitmap);
    }


    @Override
    protected void onStart() {
        super.onStart();
//        backFotoapparat.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
//        backFotoapparat.stop();
    }
}
