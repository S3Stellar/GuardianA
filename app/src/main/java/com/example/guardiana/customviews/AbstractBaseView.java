package com.example.guardiana.customviews;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.guardiana.R;

/**
 * This class create custom view element
 */
public abstract class AbstractBaseView extends AbstractCustomLayout {
    private final ImageView imageView;
    private final TextView textView;


    public AbstractBaseView(Context context) {
        super(context, R.layout.image_dialog_sheet);
        imageView = findViewById(R.id.imageDialogSheetImage);
        textView = findViewById(R.id.imageDialogSheetDescription);
        setId(View.generateViewId());

    }

    public AbstractBaseView(Context context, int imageResource, String text) {
        this(context);
        setImageResource(imageResource);
        setText(text);

    }

    public void setImageResource(int imageResource) {
        imageView.setImageResource(imageResource);
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public TextView getTextView() {
        return textView;
    }
}
