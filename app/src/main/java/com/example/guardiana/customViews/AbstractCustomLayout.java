package com.example.guardiana.customViews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.guardiana.R;

/**
 * Parent class for custom layout
 */
public abstract class AbstractCustomLayout extends LinearLayout{

    public AbstractCustomLayout(Context context, int layout) {
        super(context);
        inflate(context, layout, this);
    }
}
