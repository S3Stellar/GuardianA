package com.example.guardiana.customViews.resources;

import android.content.Context;

import com.example.guardiana.R;
import com.example.guardiana.customViews.concretecustomviews.favoritemenu.CustomClinicView;
import com.example.guardiana.customViews.concretecustomviews.favoritemenu.CustomFriendView;
import com.example.guardiana.customViews.concretecustomviews.favoritemenu.CustomHomeView;
import com.example.guardiana.customViews.concretecustomviews.favoritemenu.CustomHospitalView;
import com.example.guardiana.customViews.concretecustomviews.favoritemenu.CustomKindergartenView;
import com.example.guardiana.customViews.concretecustomviews.favoritemenu.CustomOtherView;
import com.example.guardiana.customViews.concretecustomviews.favoritemenu.CustomShopView;
import com.example.guardiana.customViews.concretecustomviews.favoritemenu.CustomWorkView;

import java.util.Arrays;

public class BottomSheetFavoriteResource extends AbstractBottomSheetResource {


    public BottomSheetFavoriteResource(Context context) {
        super(context);

    }

    @Override
    public void createCustomList() {
        getResources().addAll(Arrays.asList(
                new CustomHomeView(getContext(), R.drawable.house, "House"),
                new CustomWorkView(getContext(), R.drawable.work, "Work"),
                new CustomFriendView(getContext(), R.drawable.friends, "Friends"),
                new CustomKindergartenView(getContext(), R.drawable.kindergarten, "Kindergarten"),
                new CustomShopView(getContext(), R.drawable.shops, "Shop"),
                new CustomClinicView(getContext(), R.drawable.clinic, "Clinic"),
                new CustomHospitalView(getContext(), R.drawable.hospital, "Hospital"),
                new CustomOtherView(getContext(), R.drawable.other, "Other")));
    }
}
