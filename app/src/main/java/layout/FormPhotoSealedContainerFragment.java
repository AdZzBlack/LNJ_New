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
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import static com.inspira.lnj.IndexInternal.global;

public class FormPhotoSealedContainerFragment extends FormPhotoEmptyContainer {

    public FormPhotoSealedContainerFragment() {
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
        StrTitle = "Sealed Container";
        StrInfo = "Upload Sealed Container Picture";
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
    protected void checkingPrevious()
    {
        if(!LibInspira.getShared(global.temppreferences, global.temp.photo_pathraw_sealed_container, "").equals(""))
        {
            super.createNewImage();
            btnAdd.setVisibility(View.GONE);

            String tempPath = LibInspira.getShared(global.temppreferences, global.temp.photo_path_sealed_container, "");
            String tempPathRaw = LibInspira.getShared(global.temppreferences, global.temp.photo_pathraw_sealed_container, "");
            String tempPhotoName = LibInspira.getShared(global.temppreferences, global.temp.photo_photoname_sealed_container, "");

            String[] pieces1 = tempPath.trim().split("\\|");
            String[] pieces2 = tempPathRaw.trim().split("\\|");
            String[] pieces3 = tempPhotoName.trim().split("\\|");
            for(int i=0 ; i < pieces1.length ; i++){
                mCurrentPhotoPath = pieces1[i];
                mCurrentPhotoPathRaw = pieces2[i];
                mCurrentPhotoName = pieces3[i];

                setImage();
            }
        }
    }

    @Override
    public void onClick(View v) {
        v.startAnimation(global.buttoneffect);
        if(v.getId() == R.id.btnAdd){
            createNewImage();
            btnAdd.setVisibility(View.GONE);
        }
        else if(v.getId() == R.id.btnNext){
//            LibInspira.showLoading(getContext(), "Processing Photos", "Loading...");

            photoName = "";
            for(int i=0;i<mPathRaw_.size();i++)
            {
                if(!mPathRaw_.get(i).equals(""))
                {
                    photoName = photoName + mPhotoName_.get(i) + "|";
                    path = path + mPath_.get(i) + "|";
                    pathRaw = pathRaw + mPathRaw_.get(i) + "|";
//                    new doFileUpload().execute(decodeFile(mPhotoName_.get(i), mPathRaw_.get(i), 1920, 1080));
                }
            }
            LibInspira.setShared(global.temppreferences, global.temp.photo_pathraw_sealed_container, pathRaw);
            LibInspira.setShared(global.temppreferences, global.temp.photo_path_sealed_container, path);
            LibInspira.setShared(global.temppreferences, global.temp.photo_photoname_sealed_container, photoName);
            LibInspira.BackFragment(getActivity().getSupportFragmentManager());
        }
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
    public void onDestroy() {
        super.onDestroy();
    }
}
