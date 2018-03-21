package com.inspira.lnj;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.view.View;

/**
 * Created by Tonny on 3/21/2018.
 */

public class BottomSheetDialog extends android.support.design.widget.BottomSheetDialogFragment {
    private int layout;

    public void setLayout(int layout) {
        this.layout = layout;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View contentView = View.inflate(getContext(), layout, null);
        dialog.setContentView(contentView);
    }
}
