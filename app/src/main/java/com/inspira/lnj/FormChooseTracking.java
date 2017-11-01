package com.inspira.lnj;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.Window;

public class FormChooseTracking extends Dialog implements View.OnClickListener {
    OnMyDialogResult mDialogResult;

    public FormChooseTracking(@NonNull Context context) {
        super(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.form_choose_tracking);

        findViewById(R.id.btnStartFumigasi).setOnClickListener(this);
        findViewById(R.id.btnStopFumigasi).setOnClickListener(this);
        findViewById(R.id.btnInDepo).setOnClickListener(this);
        findViewById(R.id.btnOutDepo).setOnClickListener(this);
        findViewById(R.id.btnOutPort).setOnClickListener(this);
        findViewById(R.id.btnUnloading).setOnClickListener(this);
        findViewById(R.id.btnPickup).setOnClickListener(this);
        findViewById(R.id.btnReturn).setOnClickListener(this);
        findViewById(R.id.btnPortGateIn).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartFumigasi:
                mDialogResult.finish("startfumigasi");
                break;
            case R.id.btnStopFumigasi:
                mDialogResult.finish("stopfumigasi");
                break;
            case R.id.btnInDepo:
                mDialogResult.finish("indepo");
                break;
            case R.id.btnOutDepo:
                mDialogResult.finish("outdepo");
                break;
            case R.id.btnOutPort:
                mDialogResult.finish("outport");
                break;
            case R.id.btnUnloading:
                mDialogResult.finish("unloading");
                break;
            case R.id.btnPickup:
                mDialogResult.finish("pickup");
                break;
            case R.id.btnReturn:
                mDialogResult.finish("return");
                break;
            case R.id.btnPortGateIn:
                mDialogResult.finish("portgatein");
                break;
            default:
                break;
//            case R.id.btn_cancel:
//                dismiss();
//                break;
        }
        dismiss();
    }

    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }

    public interface OnMyDialogResult{
        void finish(String jenis);
    }
}