package com.example.guardiana.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.guardiana.R;

import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;

import javax.security.auth.login.LoginException;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.Context.SENSOR_SERVICE;

public class CalibrationDialog extends Dialog implements SensorEventListener {

    private final SensorManager sensorManager;
    private TextView textView;
    double ax, ay, az;   // these are the acceleration in x,y and z axis
    private volatile AtomicInteger allSet = new AtomicInteger(0);
    private volatile boolean isCalibrated = false;
    private volatile boolean isRunning = true;
    private volatile boolean isCountdownRunning = false;
    private Button button;

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
        button = findViewById(R.id.calibration_button);
        button.setOnClickListener(v -> dismiss());
    }


    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isRunning) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                ax = event.values[0];
                ay = event.values[1];
                az = event.values[2];
                Log.d("TAG", "onSensorChanged: " + az);
                if (Math.round(az) < -2) {
                    textView.setText("Tilt Front");
                    Log.d("TAG", "Tile Forward");
                    allSet.set(0);
                } else if (Math.round(az) > 4) {
                    textView.setText("Tilt Back");
                    Log.d("TAG", "Tilt Backward");
                    allSet.set(0);
                } else {

                    if (!isCountdownRunning) {
                        textView.setText("All set please wait...");
                        allSet.incrementAndGet();
                        isCountdownRunning = true;
                        new CountDownTimer(3000, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                if (allSet.get() != 0) {
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
                textView.setText("Calibrated Successfully Ride Safely");
                isCalibrated = false;
                isRunning = false;
                isCountdownRunning = false;
            }
        }

    }


}

