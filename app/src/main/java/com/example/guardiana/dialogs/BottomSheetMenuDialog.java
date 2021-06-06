package com.example.guardiana.dialogs;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.guardiana.R;
import com.example.guardiana.customViews.AbstractBaseView;
import com.example.guardiana.customViews.resources.AbstractBottomSheetResource;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class BottomSheetMenuDialog extends BottomSheetDialogFragment {

    // Hold the id's which connect each custom view as grid
    private final int[][] idArray;

    // Hold all the resources which will be displayed in the bottom sheet grid view
    private final AbstractBottomSheetResource resources;

    // Map each view id the the correspond location in the resource list
    private final Map<Integer, Integer> mapId;

    // Callback for on click operation
    private OnCustomViewClickEvent onCustomViewClickEvent;

    // Hold the header value which will be displayed on the view
    private final String header;
    private TextView headerTextView;

    // Root layout
    private ConstraintLayout layout;

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public int getTheme() {
        return R.style.AppBottomSheetDialogTheme;
    }

    public BottomSheetMenuDialog(Builder builder) {
        this.header = builder.header;
        this.resources = builder.resources;
        this.idArray = new int[builder.rows][builder.cols];
        this.mapId = new HashMap<>();
        this.onCustomViewClickEvent = builder.onCustomViewClickEvent;
    }

    public static class Builder {
        private String header;
        private int rows;
        private int cols;
        private AbstractBottomSheetResource resources;
        private OnCustomViewClickEvent onCustomViewClickEvent;

        public Builder setHeader(String header) {
            this.header = header;
            return this;
        }

        public Builder setNumberRows(int rows) {
            if(rows < 0) throw new RuntimeException("Rows cannot be less then 0.");
            this.rows = rows;
            return this;
        }

        public Builder setNumberCols(int cols) {
            if(cols < 0) throw new RuntimeException("Cols cannot be less then 0.");
            this.cols = cols;
            return this;
        }

        public Builder setResources(AbstractBottomSheetResource resources) {
            if(resources == null) throw new RuntimeException("Resource list cannot be null.");
            this.resources = resources;
            return this;
        }

        public Builder setOnClickEvent(OnCustomViewClickEvent onCustomViewClickEvent) {
            this.onCustomViewClickEvent = onCustomViewClickEvent;
            return this;
        }

        public BottomSheetMenuDialog build() {
            if(this.rows * this.cols > resources.getResources().size()) throw new RuntimeException("Incompatible number of resources and grid distribution.");
            return new BottomSheetMenuDialog(this);
        }
    }


    @Override
    public int show(@NonNull FragmentTransaction transaction, @Nullable String tag) {
        Log.i(TAG, "show: in show");
        return super.show(transaction, tag);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_layout, container, false);
        initViews(v);
        setHeader();
        addBottomSheetViews();
        connectViews();
        return v;
    }

    public int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    /* initialize the views  */
    private void initViews(View v) {
        layout = v.findViewById(R.id.layout);
        headerTextView = v.findViewById(R.id.headerTextView);
    }

    /* Set the header of the inflated view */
    private void setHeader() {
        headerTextView.setText(header);
    }

    /* Add the views which set in the builder resources list */
    private void addBottomSheetViews() {
        ConstraintLayout.LayoutParams lp;
        for (int i = 0; i < idArray.length; i++) {
            for (int j = 0; j < idArray[0].length; j++) {
                lp = new ConstraintLayout.LayoutParams(ConstraintSet.MATCH_CONSTRAINT, ConstraintSet.MATCH_CONSTRAINT);
                layout.addView(createBottomSheetImageView(i, j), lp);
            }
        }
    }

    /* Create new view to the inflated bottom view */
    private AbstractBaseView createBottomSheetImageView(int i, int j) {
        AbstractBaseView customView = resources.getResources().get(i * idArray[0].length + j);
        idArray[i][j] = customView.getId();
        mapId.put(customView.getId(), i * idArray[0].length + j);
        customView.setOnClickListener(v -> {
            Integer pos = mapId.get(customView.getId());
            if (onCustomViewClickEvent != null && pos != null)
                onCustomViewClickEvent.onItemClick(pos);
        });
        return customView;
    }

    /* Connect the views which will be viewed as grid*/
    private void connectViews() {

        int id;
        ConstraintSet cs = new ConstraintSet();
        cs.clone(layout);
        cs.setDimensionRatio(R.id.gridFrame, idArray[0].length + ":" + idArray.length);
        for (int iRow = 0; iRow < idArray.length; iRow++) {
            for (int iCol = 0; iCol < idArray[0].length; iCol++) {
                id = idArray[iRow][iCol];
                cs.setDimensionRatio(id, "1:1");
                if (iRow == 0) {
                    // Connect the top row to the top of the frame.
                    cs.connect(id, ConstraintSet.TOP, R.id.gridFrame, ConstraintSet.TOP);
                } else {
                    // Connect top to bottom of row above.
                    cs.connect(id, ConstraintSet.TOP, idArray[iRow - 1][0], ConstraintSet.BOTTOM);
                }
            }
            // Create a horizontal chain that will determine the dimensions of our squares.
            // Could also be createHorizontalChainRtl() with START/END.
            cs.createHorizontalChain(R.id.gridFrame, ConstraintSet.LEFT,
                    R.id.gridFrame, ConstraintSet.RIGHT,
                    idArray[iRow], null, ConstraintSet.CHAIN_PACKED);
        }

        cs.applyTo(layout);

    }

    /* Add on click listener to each element which displayed in the grid*/

    public void setOnCustomViewClickEvent(OnCustomViewClickEvent onCustomViewClickEvent) {
        this.onCustomViewClickEvent = onCustomViewClickEvent;
    }

    public interface OnCustomViewClickEvent {
        void onItemClick(int pos);
    }

}

