/******************************************************************************
    Author           : ADI
    Description      : dashboard untuk internal
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
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

//import android.app.Fragment;

public class QRCodeDocumentFragment extends QRCodeFragment implements ZXingScannerView.ResultHandler {
    private static final int REQUEST_CAMERA = 1;
    private SubmitData submitData;
    private String actionUrl;
    //private String kodedoc;

    public QRCodeDocumentFragment() {
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (submitData != null){
            submitData.cancel(true);
        }
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
        Log.wtf("scan result ", scanResult);
        String parts[] = scanResult.split("\\|");
        if (parts.length >= 5){
            if(parts[0].toLowerCase().equals("lnj") && parts[1].toLowerCase().equals("sampuljob")){
                LibInspira.setShared(global.temppreferences, global.temp.nomor_doc, parts[2]);
                LibInspira.setShared(global.temppreferences, global.temp.kode_doc, parts[3]);
//                kodedoc = parts[3];
                LibInspira.alertBoxYesNo("Submit " + LibInspira.getShared(global.temppreferences, global.temp.kode_doc, "").toUpperCase(),
                        "Do you want to assign this document to " + LibInspira.getShared(global.temppreferences, global.temp.selected_nama_user, "").toUpperCase() + "?", getActivity()
                 , new Runnable() {
                    @Override
                    public void run() {
                        submitData = new SubmitData();
                        actionUrl = "Order/updateDoc/";
                        submitData.execute(actionUrl);
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        LibInspira.setShared(global.temppreferences, global.temp.nomor_doc, "");
                        LibInspira.setShared(global.temppreferences, global.temp.kode_doc, "");
                        scannerView.resumeCameraPreview(QRCodeDocumentFragment.this);
                    }
                });
            }else{
                LibInspira.showLongToast(getContext(), "Invalid QRCode");
                scannerView.resumeCameraPreview(QRCodeDocumentFragment.this);
            }
        }
        else
        {
            LibInspira.showLongToast(getContext(), "Invalid QRCode");
            scannerView.resumeCameraPreview(QRCodeDocumentFragment.this);
        }
    }

    private class SubmitData extends AsyncTask<String, Void, String> {
        JSONObject jsonObject;
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("nomormhadmin", LibInspira.getShared(global.temppreferences, global.temp.selected_nomor_user, ""));
                jsonObject.put("nomordoc", LibInspira.getShared(global.temppreferences, global.temp.nomor_doc, ""));
                Log.wtf("kodedoc ", LibInspira.getShared(global.temppreferences, global.temp.kode_doc, ""));
                jsonObject.put("kodedoc", LibInspira.getShared(global.temppreferences, global.temp.kode_doc, "").toString().toUpperCase());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("resultQuery", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            LibInspira.showLongToast(getContext(), "Data has been successfully submitted");
                            LibInspira.clearShared(global.temppreferences);
                            LibInspira.hideLoading();
                            LibInspira.ReplaceFragmentNoBackStack(getFragmentManager(), R.id.fragment_container, new DashboardInternalFragment());
                        }
                        else
                        {
                            LibInspira.showLongToast(getContext(), "Submitting data failed");
                            LibInspira.hideLoading();
                            scannerView.resumeCameraPreview(QRCodeDocumentFragment.this);
                        }
                        Log.wtf("result ", result);
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.showLongToast(getContext(), "Submitting data failed");
                LibInspira.hideLoading();
                scannerView.resumeCameraPreview(QRCodeDocumentFragment.this);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Submitting document data", "Loading...");
        }
    }
}
