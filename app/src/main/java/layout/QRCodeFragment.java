/******************************************************************************
    Author           : ADI
    Description      : dashboard untuk internal
    History          :

******************************************************************************/
package layout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.Result;
import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static android.Manifest.permission.CAMERA;

//import android.app.Fragment;

public class QRCodeFragment extends Fragment implements ZXingScannerView.ResultHandler {
    private static final int REQUEST_CAMERA = 1;
    protected ZXingScannerView scannerView;
    private SaveDoc saveDoc;
    private String tipe, nomorDokumen, kodeDokumen, urlDokumen;

    protected GlobalVar global;
    protected JSONObject jsonObject;

    public QRCodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        scannerView = new ZXingScannerView(getContext());
        // Inflate the layout for this fragment
        //View v = inflater.inflate(R.layout.fragment_qrcode, container, false);
        getActivity().setTitle("QRCode");
        return scannerView;
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

        global = new GlobalVar(getActivity());

//        scannerView = new ZXingScannerView(getContext());
//        getActivity().setContentView(scannerView);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkPermission()){
                //remarked by Tonny @02-Oct-2017
                //LibInspira.showLongToast(getContext(), "Permission is granted!");
            }else{
                requestPermission();
            }
        }
    }

    private Boolean checkPermission(){
        return (ContextCompat.checkSelfPermission(getContext(), CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(getActivity(), new String[]{CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_CAMERA:
                if (grantResults.length > 0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted){
                        LibInspira.showLongToast(getContext(), "Permission granted");
                    }else{
                        LibInspira.showLongToast(getContext(), "Permission Denied");
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            if(shouldShowRequestPermissionRationale(CAMERA)){
                                displayAlertMessage("You need to allow access to both permission",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                requestPermissions(new String[]{CAMERA}, REQUEST_CAMERA);
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(checkPermission()){
                if(scannerView == null){
                    scannerView = new ZXingScannerView(getContext());
                    getActivity().setContentView(scannerView);
                }
                scannerView.setResultHandler(this);
                scannerView.startCamera();
            }else{
                requestPermission();
            }
        }
    }

    public void displayAlertMessage(String message, DialogInterface.OnClickListener listener){
        new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("OK", listener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void handleResult(Result result) {
        final String scanResult = result.getText();
        String parts[] = scanResult.split("\\|");
        if (parts.length == 5){
            if(parts[0].toLowerCase().equals("lnj")){
                tipe = parts[1];
                nomorDokumen = parts[2];
                kodeDokumen = parts[3];
                urlDokumen = parts[4];
                String actionUrl = "Scanning/saveDoc/";
                saveDoc = new SaveDoc();
                saveDoc.execute(actionUrl);
            }else{
                LibInspira.showLongToast(getContext(), "Invalid QRCode");
                scannerView.resumeCameraPreview(QRCodeFragment.this);
            }
        }
        else
        {
            LibInspira.showLongToast(getContext(), "Invalid QRCode");
            scannerView.resumeCameraPreview(QRCodeFragment.this);
        }
    }

    private class SaveDoc extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("nomormhadmin", LibInspira.getShared(global.userpreferences,global.user.nomor,""));
                jsonObject.put("tipe", tipe);
                jsonObject.put("nomordokumen", nomorDokumen);
                jsonObject.put("kodedokumen", kodeDokumen);
                jsonObject.put("urldokumen", urlDokumen);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
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
                        if(!obj.has("query")){  //jika success
                            LibInspira.showLongToast(getContext(), obj.getString("message"));
                        }
                        else
                        {
                            LibInspira.showLongToast(getContext(), obj.getString("query"));
                            //LibInspira.showLongToast(getContext(), obj.getString("message"));  //unremark untuk menampilkan pesan error (untuk end user)
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.showLongToast(getContext(), e.getMessage());
                LibInspira.hideLoading();
            }
            LibInspira.BackFragment(getFragmentManager());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Adding the documents", "Loading");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (saveDoc != null){
            saveDoc.cancel(true);
        }
        scannerView.stopCamera();
    }
}
