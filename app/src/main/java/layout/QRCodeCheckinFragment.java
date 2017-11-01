/******************************************************************************
    Author           : ADI
    Description      : dashboard untuk internal
    History          :

******************************************************************************/
package layout;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.Result;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

//import android.app.Fragment;

public class QRCodeCheckinFragment extends QRCodeFragment implements ZXingScannerView.ResultHandler {
    private static final int REQUEST_CAMERA = 1;
    private ZXingScannerView scannerView;
    private String tipe, nomorDokumen, kodeDokumen;

    public QRCodeCheckinFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }


    /*****************************************************************************/
    //OnAttach dijalankan pada saat fragment ini terpasang pada Activity penampungnya
    /*****************************************************************************/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    //added by Tonny @15-Jul-2017
    //untuk mapping UI pada fragment, jangan dilakukan pada OnCreate, tapi dilakukan pada onActivityCreated
    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void handleResult(Result result) {
        final String scanResult = result.getText();
        String parts[] = scanResult.split("\\|");
        if (parts.length > 0){
            if(parts[0].toLowerCase().equals("lnj") && parts[1].toLowerCase().equals("deliverynote")){
                LibInspira.setShared(global.userpreferences, global.user.checkin_nomorth, parts[2]);
                LibInspira.setShared(global.userpreferences, global.user.checkin_kodecontainer, parts[3]);
                LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormTrackingFragment());
            }else{
                LibInspira.showLongToast(getContext(), "Invalid QRCode");
                scannerView.resumeCameraPreview(QRCodeCheckinFragment.this);
            }
        }
    }
}
