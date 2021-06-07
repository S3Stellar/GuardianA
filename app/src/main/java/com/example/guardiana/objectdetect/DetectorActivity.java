package com.example.guardiana.objectdetect;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.widget.Toast;

import com.example.guardiana.R;
import com.example.guardiana.objectdetect.customview.OverlayView;
import com.example.guardiana.objectdetect.tracking.MultiBoxTracker;
import com.example.guardiana.objectdetect.utils.ImageUtils;
import com.example.guardiana.objectdetect.utils.Logger;
import com.example.lib_interpreter.Detector;
import com.example.lib_interpreter.TFLiteObjectDetectionAPIModel;
import com.thecode.aestheticdialogs.AestheticDialog;
import com.thecode.aestheticdialogs.DialogAnimation;
import com.thecode.aestheticdialogs.DialogStyle;
import com.thecode.aestheticdialogs.DialogType;
import com.thecode.aestheticdialogs.OnDialogClickListener;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kotlin.UninitializedPropertyAccessException;

import static android.view.Gravity.CENTER;

public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {
    private static final Logger LOGGER = new Logger();

    // Configuration values for the prepackaged SSD model.
    private static final int TF_OD_API_INPUT_SIZE = 300;
    private static final boolean TF_OD_API_IS_QUANTIZED = true;
    private static final String TF_OD_API_MODEL_FILE = "detect.tflite";
    private static final String TF_OD_API_LABELS_FILE = "labelmap.txt";
    private static final DetectorMode MODE = DetectorMode.TF_OD_API;
    // Minimum detection confidence to track a detection.
    private static final float MINIMUM_CONFIDENCE_TF_OD_API = 0.58f;
    private static final boolean MAINTAIN_ASPECT = false;
    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);
    private static final boolean SAVE_PREVIEW_BITMAP = false;
    OverlayView trackingOverlay;

    private Detector detector;

    private Bitmap rgbFrameBitmap = null;
    private Bitmap croppedBitmap = null;

    private boolean computingDetection = false;

    private long timestamp = 0;

    private Matrix frameToCropTransform;
    private Matrix cropToFrameTransform;

    private MultiBoxTracker tracker;
    private Toast toast;
    private final String[] detectList = {"car", "person", "bicycle", "bus", "motorcycle", "truck", "train"};
    private AestheticDialog.Builder objectDetectAlertDialog;
    private boolean isFirstDialog = true;


    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {
        tracker = new MultiBoxTracker(this);
        toast = new Toast(getApplicationContext());

        int cropSize = TF_OD_API_INPUT_SIZE;

        try {
            detector =
                    TFLiteObjectDetectionAPIModel.create(
                            this,
                            TF_OD_API_MODEL_FILE,
                            TF_OD_API_LABELS_FILE,
                            TF_OD_API_INPUT_SIZE,
                            TF_OD_API_IS_QUANTIZED);
            cropSize = TF_OD_API_INPUT_SIZE;
        } catch (final IOException e) {
            e.printStackTrace();
            showAToast("Detector could not be initialized");
            finish();
        }

        previewWidth = size.getWidth();
        previewHeight = size.getHeight();

        int sensorOrientation = rotation - getScreenOrientation();

        rgbFrameBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Config.ARGB_8888);
        croppedBitmap = Bitmap.createBitmap(cropSize, cropSize, Config.ARGB_8888);

        frameToCropTransform =
                ImageUtils.getTransformationMatrix(
                        previewWidth, previewHeight,
                        cropSize, cropSize,
                        sensorOrientation, MAINTAIN_ASPECT);

        cropToFrameTransform = new Matrix();
        frameToCropTransform.invert(cropToFrameTransform);

        trackingOverlay = findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(canvas -> {
        });
        tracker.setFrameConfiguration(previewWidth, previewHeight, sensorOrientation);

        objectDetectAlertDialog = new AestheticDialog.Builder(this, DialogStyle.TOASTER, DialogType.WARNING)
                .setTitle("")
                .setCancelable(false)
                .setDarkMode(true)
                .setDuration(0)
                .setGravity(Gravity.TOP | CENTER)
                .setAnimation(DialogAnimation.SHRINK);
        objectDetectAlertDialog.show();
        objectDetectAlertDialog.dismiss();
    }

    @Override
    protected void processImage() {
        ++timestamp;
        final long currTimestamp = timestamp;
        trackingOverlay.postInvalidate();

        // No mutex needed as this method is not reentrant.
        if (computingDetection) {
            readyForNextImage();
            return;
        }
        computingDetection = true;
        //LOGGER.i("Preparing image " + currTimestamp + " for detection in bg thread.");

        rgbFrameBitmap.setPixels(getRgbBytes(), 0, previewWidth, 0, 0, previewWidth, previewHeight);

        readyForNextImage();

        final Canvas canvas = new Canvas(croppedBitmap);
        canvas.drawBitmap(rgbFrameBitmap, frameToCropTransform, null);
        // For examining the actual TF input.
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }

        runInBackground(
                () -> {
                    final List<Detector.Recognition> results = detector.recognizeImage(croppedBitmap);
                    //cropCopyBitmap = Bitmap.createBitmap(croppedBitmap);

                    float minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                    switch (MODE) {
                        case TF_OD_API:
                            minimumConfidence = MINIMUM_CONFIDENCE_TF_OD_API;
                            break;
                    }

                    final List<Detector.Recognition> mappedRecognitions =
                            new ArrayList<>();

                    for (final Detector.Recognition result : results) {
                        final RectF location = result.getLocation();
                        if (location != null && result.getConfidence() >= minimumConfidence) {
                            Log.i("TAG", "DETECT GENERAL");
                            if (Arrays.asList(detectList).contains(result.getTitle())) {
                                Log.i("TAG", "DETECT SUCCESS");

                                if (isFirstDialog) {
                                    isFirstDialog = false;
                                    showADialog(result.getTitle());
                                } else {
                                    if (!objectDetectAlertDialog.getAlertDialog().isShowing())
                                        showADialog(result.getTitle());
                                }
                            }

                            cropToFrameTransform.mapRect(location);
                            result.setLocation(location);
                            mappedRecognitions.add(result);
                        }
                    }

                    tracker.trackResults(mappedRecognitions, currTimestamp);
                    trackingOverlay.postInvalidate();
                    computingDetection = false;
                });
    }


    public void showADialog(String st) {
        objectDetectAlertDialog.setMessage("A " + st + " close by watch out!")
                .setDuration(3000)
                .show();
    }

    public void showAToast(String st) { //"Toast toast" is declared in the class
        try {
            toast.getView().isShown();     // true if visible
            toast.setText(st);
        } catch (Exception e) {         // invisible if exception
            toast = Toast.makeText(this, st, Toast.LENGTH_SHORT);
        }
        toast.show();  //finally display it
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_camera_connection;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }


    // Which detection model to use: by default uses Tensorflow Object Detection API frozen
    // checkpoints.
    private enum DetectorMode {
        TF_OD_API;
    }

    @Override
    protected void setUseNNAPI(final boolean isChecked) {
        runInBackground(
                () -> {
                    try {
                        detector.setUseNNAPI(isChecked);
                    } catch (UnsupportedOperationException e) {
                        LOGGER.e(e, "Failed to set \"Use NNAPI\".");
                        runOnUiThread(
                                () -> showAToast(e.getMessage()));
                    }
                });
    }

    @Override
    protected void setNumThreads(final int numThreads) {
        runInBackground(() -> detector.setNumThreads(numThreads));
    }
}
