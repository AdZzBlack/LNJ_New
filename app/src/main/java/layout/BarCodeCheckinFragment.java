/******************************************************************************
    Author           : Tonny
    Description      : Fragment untuk scan barcode check in
    History          :

******************************************************************************/
package layout;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.Result;
import com.inspira.lnj.LibInspira;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class BarCodeCheckinFragment extends QRCodeFragment implements ZXingScannerView.ResultHandler {
    private static final int REQUEST_CAMERA = 1;
    private DocumentSave documentSave;
    private String strScanResult;

    public BarCodeCheckinFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        getActivity().setTitle("BarCode Scan");
        StrTitle = "BarCode Scan";
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
        //remarked by Tonny @16-Dec-2017 tidak perlu pengecekan karena ditampilkan jika perlu
//        if(!LibInspira.getShared(global.userpreferences, global.user.checkin_nomorthsuratjalan, "").equals("") ||
//                !LibInspira.getShared(global.userpreferences, global.user.checkin_nomortdsuratjalan, "").equals(""))
//        {
//            LibInspira.AddFragment(getFragmentManager(), R.id.fragment_container, new FormTrackingFragment());
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        scannerView.resumeCameraPreview(BarCodeCheckinFragment.this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(documentSave != null) documentSave.cancel(true);
    }

    @Override
    public void handleResult(Result result) {
        final String scanResult = result.getText();
        LibInspira.setShared(global.userpreferences, global.user.checkin_nomorthsuratjalan, "");
        LibInspira.setShared(global.userpreferences, global.user.checkin_nomortdsuratjalan, "");
        Log.wtf("scan result ", scanResult);
        String prefixDoc = scanResult.substring(0,1);
        if(prefixDoc.toLowerCase().equals("h") || prefixDoc.toLowerCase().equals("d")){
            if(prefixDoc.toLowerCase().equals("h")){
                documentSave = new DocumentSave("thsuratjalan");
                LibInspira.setShared(global.userpreferences, global.user.checkin_nomorthsuratjalan, scanResult.substring(1));
            }else{
                documentSave = new DocumentSave("tdsuratjalan");
                strScanResult = scanResult;
                LibInspira.setShared(global.userpreferences, global.user.checkin_nomortdsuratjalan, scanResult.substring(1));
            }
            if(documentSave != null){
                String actionUrl = "Scanning/saveDeliveryOrder/";
                documentSave.execute(actionUrl);
            }
        }else{
            LibInspira.showLongToast(getContext(), "Invalid BarCode");
            scannerView.resumeCameraPreview(BarCodeCheckinFragment.this);
        }
    }

    //added by Tonny @05-Dec-2017 untuk cek suatu dokumen sudah selesai atau belum
    private class DocumentSave extends AsyncTask<String, Void, String> {
        private String docType;

        private DocumentSave(String _type)
        {
            docType = _type;
        }
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("nomormhadmin", LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
                jsonObject.put("nomorthsuratjalan", LibInspira.getShared(global.userpreferences,global.user.checkin_nomorthsuratjalan,""));
                jsonObject.put("nomortdsuratjalan", LibInspira.getShared(global.userpreferences,global.user.checkin_nomortdsuratjalan,""));
                jsonObject.put("doctype", docType);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("tes", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        LibInspira.hideLoading();
                        if(!obj.has("query")){  //jika success menyimpan data baru
                            //remarked by Tonny @27-Dec-2017 perubahan metode pengecekan
//                            if(obj.getString("status_selesai").equals("0") &&
//                                    obj.getString("nomormhadmin_driver").equals(LibInspira.getShared(global.userpreferences, global.user.nomor, ""))){
//                                //jika dokumen belum sampai DAN belum ada dalam deliveryorderlist, maka simpan ke dalam deliveryorderlist
//                                if(LibInspira.isAlreadyOnList(LibInspira.getShared(global.datapreferences, global.data.deliveryorderlist, ""), strScanResult, "\\|")){
//                                    LibInspira.showShortToast(getContext(), "This document is already scanned");
//                                }else{
////                                    LibInspira.setShared(global.datapreferences, global.data.deliveryorderlist, LibInspira.getShared(global.datapreferences, global.data.deliveryorderlist, "") +
////                                            strScanResult + "|");
//                                    LibInspira.showShortToast(getContext(), "Document has been successfully added to the list");
//                                }
//                            }else if(!obj.getString("nomormhadmin_driver").equals("0") ||
//                                    !obj.getString("nomormhadmin_driver").equals(LibInspira.getShared(global.userpreferences, global.user.nomor, ""))){
//                                //tampilkan pesan jika document sudah diambil driver lain
//                                LibInspira.showLongToast(getContext(), "This document is already given to another driver");
//                            }else{
//                                //jika dokumen sudah selesai, tampilkan pesan bahwa job sudah selesai dan lanjutkan scan untuk dokumen lainnya
//                                LibInspira.showLongToast(getContext(), "This document is already delivered, please scan another document");
//                            }
//                            LibInspira.showShortToast(getContext(), "Document has been successfully added to the list");
                            LibInspira.showLongToast(getContext(), obj.getString("message"));
                            scannerView.resumeCameraPreview(BarCodeCheckinFragment.this);
                        }
                        else
                        {
//                            LibInspira.showLongToast(getContext(), "Document not found. Please try again with a valid document");
                            LibInspira.showLongToast(getContext(), obj.getString("message"));
                            Log.wtf("error ", obj.getString("query"));
                            scannerView.resumeCameraPreview(BarCodeCheckinFragment.this);
                            //remarked by Tonny @16-Dec-2017
//                            LibInspira.BackFragment(getFragmentManager());
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.showLongToast(getContext(), e.getMessage());
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Checking Document", "Loading");
        }
    }
}
