package com.example.guardiana.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Switch;

import androidx.annotation.NonNull;

import com.example.guardiana.PreferencesManager;
import com.example.guardiana.R;

import java.util.Objects;

public class SettingDialog extends Dialog {

    private final PreferencesManager preferencesManager;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch senSwitch;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private Switch calibSwitch;

    public SettingDialog(@NonNull Context context) {
        super(context);
        preferencesManager = new PreferencesManager(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        senSwitch = findViewById(R.id.sensitivitySwitch);
        calibSwitch = findViewById(R.id.calibSwitch);
        senSwitch.setChecked(Objects.equals(preferencesManager.getSens(), "HIGH"));
        calibSwitch.setChecked(preferencesManager.getCalib());
        setSenSwitch();
        calibSenSwitch();
    }

    private void setSenSwitch() {
        senSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // checked is on
                preferencesManager.setSens("HIGH");
            } else {
                // checked is off
                preferencesManager.setSens("LOW");
            }
        });
    }

    private void calibSenSwitch() {
        calibSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // checked is on
                preferencesManager.setCalib(true);

            } else {
                // checked is off
                preferencesManager.setCalib(false);
            }
        });
    }


}
