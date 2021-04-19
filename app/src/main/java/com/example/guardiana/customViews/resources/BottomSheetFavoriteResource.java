package com.example.guardiana.customViews.resources;

import android.content.Context;

import com.example.guardiana.R;
import com.example.guardiana.customViews.concretecustomviews.favoritemenu.CustomBottomSheetView;

import java.util.Arrays;

public class BottomSheetFavoriteResource extends AbstractBottomSheetResource {


    public BottomSheetFavoriteResource(Context context) {
        super(context);

    }

    @Override
    public void createCustomList() {
        getResources().addAll(Arrays.asList(
                new CustomBottomSheetView(getContext(), R.drawable.bottom_sheet_home, "House"),
                new CustomBottomSheetView(getContext(), R.drawable.bottom_sheet_work, "Work"),
                new CustomBottomSheetView(getContext(), R.drawable.bottom_sheet_friends, "Friends"),
                new CustomBottomSheetView(getContext(), R.drawable.bottom_sheet_kindergarten, "Kindergarten"),
                new CustomBottomSheetView(getContext(), R.drawable.bottom_sheet_shops, "Shop"),
                new CustomBottomSheetView(getContext(), R.drawable.bottom_sheet_clinic, "Clinic"),
                new CustomBottomSheetView(getContext(), R.drawable.bottom_sheet_hospital, "Hospital"),
                new CustomBottomSheetView(getContext(), R.drawable.bottom_sheet_bicycle, "Other")));
    }
}
