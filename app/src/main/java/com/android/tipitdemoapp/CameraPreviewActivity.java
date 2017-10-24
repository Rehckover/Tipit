package com.android.tipitdemoapp;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableBoolean;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.renderscript.RenderScript;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    public volatile ObservableBoolean mode = new ObservableBoolean();
    private boolean modeType;
    ActivityCameraPreviewBinding binding;

    private Fotoapparat backFotoapparat;
    private Tipit tipit;
    private RenderScript mRs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera_preview);
        binding.setActivity(this);
        tipit = Tipit.getInstance(this);
        mRs = RenderScript.create(this);
        backFotoapparat = createFotoapparat(LensPosition.BACK);
        setListeners();
    }

    private void setListeners() {
        binding.mode.setOnCheckedChangeListener((compoundButton, checked) -> {
            setModeType(checked);
        });
    }

    private synchronized boolean getModeType() {
        return modeType;
    }

    private synchronized void setModeType(boolean modeType) {
        this.modeType = modeType;
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
            if (!getModeType()) {
                Tipit.drawRectangleOnBitmap(rgbBitmap, rgbBitmap.getWidth() / 2, rgbBitmap.getHeight() / 2);
            } else {
                tipit.processImageWithTensorFlow(rgbBitmap);
            }
            runOnUiThread(() ->
                    binding.rgbPreview.setImageBitmap(rgbBitmap)
            );
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        backFotoapparat.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        backFotoapparat.stop();
    }
}
