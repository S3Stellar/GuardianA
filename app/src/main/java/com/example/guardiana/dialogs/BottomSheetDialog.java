package com.example.guardiana.dialogs;

import android.media.Image;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import com.example.guardiana.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

public class BottomSheetDialog extends BottomSheetDialogFragment {

    private ConstraintLayout layout;
    private ConstraintLayout.LayoutParams params;
    private int mRows = 7;
    private int mCols = 7;
    private final List<Integer> imagesId;
    public BottomSheetDialog(List<Integer> imagesId, int mRows, int mCols) {
        this.imagesId = imagesId;
        this.mRows = mRows;
        this.mCols = mCols;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_layout, container, false);
        layout = v.findViewById(R.id.layout);
        ImageView imageView;

        int id;
        int[][] idArray = new int[mRows][mCols];
        ConstraintSet cs = new ConstraintSet();

        // Add our views to the ConstraintLayout.
        for (int iRow = 0; iRow < mRows; iRow++) {
            for (int iCol = 0; iCol < mCols; iCol++) {
                imageView = new ImageView(getActivity());
                params = new ConstraintLayout.LayoutParams(ConstraintSet.MATCH_CONSTRAINT, ConstraintSet.MATCH_CONSTRAINT);
                params.setMargins(10, 10, 10, 10);
                imageView.setId(idArray[iRow][iCol] = View.generateViewId());
                imageView.setImageResource(this.imagesId.get(iRow * mCols + iCol));
                layout.addView(imageView, params);
            }
        }

        // Create horizontal chain for each row and set the 1:1 dimensions.
        // but first make sure the layout frame has the right ratio set.
        cs.clone(layout);
        cs.setDimensionRatio(R.id.gridFrame, mCols + ":" + mRows);
        for (int iRow = 0; iRow < mRows; iRow++) {
            for (int iCol = 0; iCol < mCols; iCol++) {
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
            // Create a horiontal chain that will determine the dimensions of our squares.
            // Could also be createHorizontalChainRtl() with START/END.
            cs.createHorizontalChain(R.id.gridFrame, ConstraintSet.LEFT,
                    R.id.gridFrame, ConstraintSet.RIGHT,
                    idArray[iRow], null, ConstraintSet.CHAIN_PACKED);
        }

        cs.applyTo(layout);


        return v;
    }


    public interface OnItemClickListener {
        void onItemClick(String text);
    }

}
