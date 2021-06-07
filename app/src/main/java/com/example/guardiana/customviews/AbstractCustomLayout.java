package com.example.guardiana.customviews;

import android.content.Context;
import android.widget.LinearLayout;

/**
 * Parent class for custom layout
 */
public abstract class AbstractCustomLayout extends LinearLayout {

    public AbstractCustomLayout(Context context, int layout) {
        super(context);
        inflate(context, layout, this);
    }
}
