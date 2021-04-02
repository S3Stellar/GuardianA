package com.example.guardiana.customViews.resources;

import android.content.Context;

import com.example.guardiana.R;
import com.example.guardiana.customViews.concretecustomviews.bottommenu.CustomDriveView;
import com.example.guardiana.customViews.concretecustomviews.bottommenu.CustomFavoriteView;
import com.example.guardiana.customViews.concretecustomviews.bottommenu.CustomRemoveView;
import com.example.guardiana.customViews.concretecustomviews.bottommenu.CustomSendLocationView;

import java.util.Arrays;

public class BottomSheetAddressMenuResource extends AbstractBottomSheetResource {

    public BottomSheetAddressMenuResource(Context context) {
        super(context);
    }

    @Override
    public void createCustomList() {
        getResources().addAll(Arrays.asList(
                new CustomRemoveView(getContext(), R.drawable.bin, "Remove"),
                new CustomSendLocationView(getContext(), R.drawable.location, "Send Location"),
                new CustomFavoriteView(getContext(), R.drawable.star, "Favorite"),
                new CustomDriveView(getContext(), R.drawable.other, "Drive")));
    }
}
