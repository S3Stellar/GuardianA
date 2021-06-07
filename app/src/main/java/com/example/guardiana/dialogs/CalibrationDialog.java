package com.example.guardiana.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

import com.example.guardiana.R;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;

import static android.content.Context.SENSOR_SERVICE;

public class CalibrationDialog extends Dialog implements SensorEventListener {

    private final SensorManager sensorManager;
    private TextView textView;
    double ax, ay, az;   // these are the acceleration in x,y and z axis
    private final AtomicInteger allSet = new AtomicInteger(0);
    private volatile boolean isCalibrated = false;
    private volatile boolean isRunning = true;
    private volatile boolean isCountdownRunning = false;

    public CalibrationDialog(Context context) {
        super(context);
        sensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calib_dialog);
        textView = findViewById(R.id.calibration_text);
        findViewById(R.id.calibration_button).setOnClickListener(v -> dismiss());
    }


    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isRunning) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                ax = event.values[0];
                ay = event.values[1];
                az = event.values[2];

                if (Math.round(az) < -2.3) {
                    textView.setTextColor(Color.RED);
                    textView.setText(R.string.TiltFront);
                    allSet.set(0);
                } else if (Math.round(az) > 4.3) {
                    textView.setTextColor(Color.RED);
                    textView.setText(R.string.TiltBack);
                    allSet.set(0);
                } else {

                    if (!isCountdownRunning) {
                        allSet.incrementAndGet();
                        isCountdownRunning = true;
                        new CountDownTimer(3000, 1000) {
                            int countDown = 3;

                            @Override
                            public void onTick(long millisUntilFinished) {
                                if (allSet.get() != 0) {
                                    textView.setTextColor(Color.GREEN);
                                    textView.setText(MessageFormat.format("All set please wait...{0}", countDown--));
                                    allSet.incrementAndGet();
                                }
                            }

                            @Override
                            public void onFinish() {
                                if (allSet.get() == 4) {
                                    isCalibrated = true;
                                } else {
                                    isCountdownRunning = false;
                                }
                            }
                        }.start();
                    }
                }
            }
            if (isCalibrated) {
                textView.setText(R.string.CalibratedSuccess);
                sensorManager.unregisterListener(this);
                isCalibrated = false;
                isRunning = false;
                isCountdownRunning = false;
            }
        }

    }
}

