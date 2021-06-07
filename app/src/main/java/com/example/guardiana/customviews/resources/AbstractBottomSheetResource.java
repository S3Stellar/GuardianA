package com.example.guardiana.customviews.resources;

import android.content.Context;

import com.example.guardiana.customviews.AbstractBaseView;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBottomSheetResource {

    private final Context context;
    private final List<AbstractBaseView> resources;

    public AbstractBottomSheetResource(Context context) {
        this.context = context;
        resources = new ArrayList<>();
        createCustomList();
    }

    public Context getContext() {
        return context;
    }

    public abstract void createCustomList();

    public List<AbstractBaseView> getResources() {
        return resources;
    }
}
