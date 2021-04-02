package com.example.guardiana.customViews.resources;

import android.content.Context;

import com.example.guardiana.customViews.AbstractCustomBottomSheetView;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBottomSheetResource {

    private final Context context;
    private final List<AbstractCustomBottomSheetView> resources;

    public AbstractBottomSheetResource(Context context) {
        this.context = context;
        resources = new ArrayList<>();
        createCustomList();
    }

    public Context getContext() {
        return context;
    }

    public abstract void createCustomList();

    public List<AbstractCustomBottomSheetView> getResources() {
        return resources;
    }
}
