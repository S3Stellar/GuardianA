package com.example.guardiana.customviews.resources;

import android.content.Context;

import com.example.guardiana.R;
import com.example.guardiana.customviews.concretecustomviews.bottommenu.CustomDriveView;
import com.example.guardiana.customviews.concretecustomviews.bottommenu.CustomFavoriteView;
import com.example.guardiana.customviews.concretecustomviews.bottommenu.CustomRemoveView;
import com.example.guardiana.customviews.concretecustomviews.bottommenu.CustomSendLocationView;

import java.util.Arrays;

public class BottomSheetAddressMenuResource extends AbstractBottomSheetResource {

    public BottomSheetAddressMenuResource(Context context) {
        super(context);
    }

    @Override
    public void createCustomList() {
        getResources().addAll(Arrays.asList(
                new CustomRemoveView(getContext(), R.drawable.bottom_sheet_bin, "Remove"),
                new CustomSendLocationView(getContext(), R.drawable.bottom_sheet_location, "Send Location"),
                new CustomFavoriteView(getContext(), R.drawable.bottom_sheet_star, "Favorite"),
                new CustomDriveView(getContext(), R.drawable.bottom_sheet_bicycle, "Drive")));
    }
}
