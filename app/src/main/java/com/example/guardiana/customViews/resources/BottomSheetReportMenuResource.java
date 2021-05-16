package com.example.guardiana.customViews.resources;

import android.content.Context;

import com.example.guardiana.R;
import com.example.guardiana.customViews.concretecustomviews.favoritemenu.CustomBottomSheetView;

import java.util.Arrays;


public class BottomSheetReportMenuResource extends AbstractBottomSheetResource {


    public BottomSheetReportMenuResource(Context context) {
        super(context);

    }

    @Override
    public void createCustomList() {
        getResources().addAll(Arrays.asList(
                new CustomBottomSheetView(getContext(), R.drawable.thumup, ""),
                new CustomBottomSheetView(getContext(), R.drawable.thumbdown, "")));
    }
}
