/******************************************************************************
    Author           : ADI
    Description      : dashboard untuk internal
    History          :

******************************************************************************/
package layout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import static com.inspira.lnj.IndexInternal.global;

//import android.app.Fragment;

public class FormContainerLoadingDetailFragment extends Fragment implements View.OnClickListener{

    private GlobalVar global;
    private JSONObject jsonObject;

    private TextView tvContainer, tvContainerSize, tvContainerType;
    private EditText edtContainerCode;
    private Button btnEmptyContainer, btnSealedContainer, btnSealedCondition, btnOtherPicture;

    private Save save;

    public FormContainerLoadingDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_form_container_loading_detail, container, false);
        getActivity().setTitle("Container Loading Report");
        return v;
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

        tvContainer = (TextView) getView().findViewById(R.id.tvContainer);
        tvContainerSize = (TextView) getView().findViewById(R.id.tvContainerSize);
        tvContainerType = (TextView) getView().findViewById(R.id.tvContainerType);
        edtContainerCode = (EditText) getView().findViewById(R.id.edtContainerCode);
        btnEmptyContainer = (Button) getView().findViewById(R.id.btnEmptyContainer);
        btnSealedContainer = (Button) getView().findViewById(R.id.btnSealedContainer);
        btnSealedCondition = (Button) getView().findViewById(R.id.btnSealedCondition);
        btnOtherPicture = (Button) getView().findViewById(R.id.btnOtherPicture);

        getView().findViewById(R.id.btnDone).setOnClickListener(this);
        getView().findViewById(R.id.btnSave).setOnClickListener(this);

        tvContainer.setOnClickListener(this);
        btnEmptyContainer.setOnClickListener(this);
        btnSealedContainer.setOnClickListener(this);
        btnSealedCondition.setOnClickListener(this);
        btnOtherPicture.setOnClickListener(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume()
    {
        if(!LibInspira.getShared(global.temppreferences, global.temp.selected_container_nomor, "").equals(""))
        {
            edtContainerCode.setText(LibInspira.getShared(global.temppreferences, global.temp.selected_container_kode, ""));
            tvContainer.setText(LibInspira.getShared(global.temppreferences, global.temp.selected_container_nomor, "") + "/" + LibInspira.getShared(global.temppreferences, global.temp.selected_container_type, "") + "/" + LibInspira.getShared(global.temppreferences, global.temp.selected_container_size, ""));
            tvContainerType.setText(LibInspira.getShared(global.temppreferences, global.temp.selected_container_type, ""));
            tvContainerSize.setText(LibInspira.getShared(global.temppreferences, global.temp.selected_container_size, ""));
        }
        super.onResume();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id==R.id.tvContainer)
        {
            LibInspira.setShared(global.temppreferences, global.temp.temp, "");
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseJobContainerFragment());
        }
        else if(id==R.id.btnEmptyContainer)
        {
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormPhotoEmptyContainer());
        }
        else if(id==R.id.btnSealedContainer)
        {
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormPhotoSealedContainerFragment());
        }
        else if(id==R.id.btnDone)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Container Loading Report");
            builder.setMessage("Are you sure want to upload report?");

            // Set up the buttons
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    done(false);
                }
            });
            builder.show();
        }
        else if(id==R.id.btnSave)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Container Loading Report");
            builder.setMessage("Are you sure want to upload report?");

            // Set up the buttons
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    done(true);
                }
            });
            builder.show();
        }
    }

    private void done(Boolean next)
    {
        if(!LibInspira.getShared(global.temppreferences, global.temp.selected_container_nomor, "").equals(""))
        {
            if(!LibInspira.getShared(global.temppreferences, global.temp.photo_pathraw_empty_container, "").equals(""))
            {
                String tempPath = LibInspira.getShared(global.temppreferences, global.temp.photo_path_empty_container, "");
                String tempPathRaw = LibInspira.getShared(global.temppreferences, global.temp.photo_pathraw_empty_container, "");
                String tempPhotoName = LibInspira.getShared(global.temppreferences, global.temp.photo_photoname_empty_container, "");

                uploadPhotos(tempPath, tempPathRaw, tempPhotoName, 1);
            }

            if(!LibInspira.getShared(global.temppreferences, global.temp.photo_pathraw_sealed_container, "").equals(""))
            {
                String tempPath = LibInspira.getShared(global.temppreferences, global.temp.photo_path_sealed_container, "");
                String tempPathRaw = LibInspira.getShared(global.temppreferences, global.temp.photo_pathraw_sealed_container, "");
                String tempPhotoName = LibInspira.getShared(global.temppreferences, global.temp.photo_photoname_sealed_container, "");

                uploadPhotos(tempPath, tempPathRaw, tempPhotoName, 2);
            }

            if(!LibInspira.getShared(global.temppreferences, global.temp.photo_pathraw_sealed_condition, "").equals(""))
            {
                String tempPath = LibInspira.getShared(global.temppreferences, global.temp.photo_path_sealed_condition, "");
                String tempPathRaw = LibInspira.getShared(global.temppreferences, global.temp.photo_pathraw_sealed_condition, "");
                String tempPhotoName = LibInspira.getShared(global.temppreferences, global.temp.photo_photoname_sealed_condition, "");

                uploadPhotos(tempPath, tempPathRaw, tempPhotoName, 3);
            }

            if(!LibInspira.getShared(global.temppreferences, global.temp.photo_pathraw_other_picture, "").equals(""))
            {
                String tempPath = LibInspira.getShared(global.temppreferences, global.temp.photo_path_other_picture, "");
                String tempPathRaw = LibInspira.getShared(global.temppreferences, global.temp.photo_pathraw_other_picture, "");
                String tempPhotoName = LibInspira.getShared(global.temppreferences, global.temp.photo_photoname_other_picture, "");

                uploadPhotos(tempPath, tempPathRaw, tempPhotoName, 4);
            }

            String actionUrl = "ContainerLoading/AddArchieve/";
            save = new Save(next);
            save.execute( actionUrl );
        }
        else
        {
            LibInspira.showShortToast(getContext(), "Container Required");
        }
    }

    private void uploadPhotos(String tempPath, String tempPathRaw, String tempPhotoName, int urltype)
    {
        String[] pieces1 = tempPath.trim().split("\\|");
        String[] pieces2 = tempPathRaw.trim().split("\\|");
        String[] pieces3 = tempPhotoName.trim().split("\\|");
        for(int i=0 ; i < pieces1.length ; i++){
            final String mCurrentPhotoPath = pieces1[i];
            final String mCurrentPhotoPathRaw = pieces2[i];
            final String mCurrentPhotoName = pieces3[i];

            new doFileUpload(new AsyncListener() {
                public void postTaskMethod() {
//                    File f = new File(mCurrentPhotoPath);
//                    f.delete();
//                    f = new File(mCurrentPhotoPathRaw);
//                    f.delete();
                }

            }, urltype).execute(LibInspira.decodeFile(mCurrentPhotoName, mCurrentPhotoPathRaw, 1920, 1080));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(save != null){
            save.cancel(true);
        }
    }

    private class Save extends AsyncTask<String, Void, String> {
        JSONObject jsonObject;

        String kode = edtContainerCode.getText().toString();
        Boolean next;

        public Save(Boolean _next)
        {
            next = _next;
        }

        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("kodecontainer", kode);
                jsonObject.put("nomorcontainer", LibInspira.getShared(global.temppreferences,global.temp.selected_container_nomor,""));
                jsonObject.put("photoempty", LibInspira.getShared(global.temppreferences,global.temp.photo_photoname_empty_container,""));
                jsonObject.put("photosealed", LibInspira.getShared(global.temppreferences,global.temp.photo_photoname_sealed_container,""));
                jsonObject.put("photosealedport", LibInspira.getShared(global.temppreferences,global.temp.photo_photoname_sealed_condition,""));
                jsonObject.put("photoother", LibInspira.getShared(global.temppreferences,global.temp.photo_photoname_other_picture,""));
                jsonObject.put("user_nomor", LibInspira.getShared(global.userpreferences,global.user.nomor,""));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("runnn", "onPostExecute: " + result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                for (int i = jsonarray.length() - 1; i >= 0; i--) {
                    JSONObject obj = jsonarray.getJSONObject(i);

                    String message = obj.getString("message");
                    if(!obj.has("query")){
                        if(next)
                        {
                            LibInspira.setShared(global.temppreferences, global.temp.photo_pathraw_empty_container, "");
                            LibInspira.setShared(global.temppreferences, global.temp.photo_path_empty_container, "");
                            LibInspira.setShared(global.temppreferences, global.temp.photo_photoname_empty_container, "");
                            LibInspira.setShared(global.temppreferences, global.temp.photo_pathraw_sealed_container, "");
                            LibInspira.setShared(global.temppreferences, global.temp.photo_path_sealed_container, "");
                            LibInspira.setShared(global.temppreferences, global.temp.photo_photoname_sealed_container, "");
                            LibInspira.setShared(global.temppreferences, global.temp.photo_pathraw_sealed_condition, "");
                            LibInspira.setShared(global.temppreferences, global.temp.photo_path_sealed_condition, "");
                            LibInspira.setShared(global.temppreferences, global.temp.photo_photoname_sealed_condition, "");
                            LibInspira.setShared(global.temppreferences, global.temp.photo_pathraw_other_picture, "");
                            LibInspira.setShared(global.temppreferences, global.temp.photo_path_other_picture, "");
                            LibInspira.setShared(global.temppreferences, global.temp.photo_photoname_other_picture, "");

                            LibInspira.setShared(global.temppreferences, global.temp.selected_container_nomor, "");
                            LibInspira.setShared(global.temppreferences, global.temp.selected_container_kode, "");
                            LibInspira.setShared(global.temppreferences, global.temp.selected_container_size, "");
                            LibInspira.setShared(global.temppreferences, global.temp.selected_container_type, "");
                            LibInspira.setShared(global.temppreferences, global.temp.selected_container_seal, "");

                            edtContainerCode.setText("");
                            tvContainer.setText("");
                            tvContainerType.setText("");
                            tvContainerSize.setText("");
                        }
                        else
                        {
                            LibInspira.clearShared(global.temppreferences);
                            LibInspira.BackFragment(getActivity().getSupportFragmentManager());
                        }
                    }
                    LibInspira.showShortToast(getContext(), message);
                }
                LibInspira.hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.showShortToast(getContext(), "Update Settings Failed");
                LibInspira.hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Settings", "Loading");
        }
    }
}
