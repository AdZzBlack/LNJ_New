package layout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.math.MathUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.inspira.lnj.FormPhotoFullscreen;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;
import static com.inspira.lnj.GlobalVar.STRING_PHOTO_EMPTY;
import static com.inspira.lnj.GlobalVar.STRING_PHOTO_OTHER;
import static com.inspira.lnj.GlobalVar.STRING_PHOTO_SEALED_CONDITION;
import static com.inspira.lnj.GlobalVar.STRING_PHOTO_SEALED_CONTAINER;
import static com.inspira.lnj.IndexInternal.global;
import static com.inspira.lnj.LibInspira.ConvertDpToPx;
import static com.inspira.lnj.LibInspira.ReplaceFragment;
import static com.inspira.lnj.LibInspira.setShared;

@SuppressLint("ValidFragment")
public class FormPhotoViewerFragment extends Fragment implements View.OnClickListener {
    private int urltype = 0;
    public FormPhotoViewerFragment(int _urltype){
        urltype = _urltype;
    }
    protected ImageView ivThumbnail;
    protected GridLayout gridPhoto;
    protected FloatingActionButton fab;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE_REQUEST = 3;

    public String StrTitle = "Empty Container";
    public String StrInfo = "Upload Empty Container Picture";

    protected TextView tvJob;

    LoadContainerPhoto loadContainerPhoto;

    BtmSheetUploadMethodFragment btmSheetUploadMethodFragment;
    TableLayout layout;

    Boolean isImageFitToScreen = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_form_upload_new, container, false);
        switch (urltype) {
            case 1:  //container empty
                StrTitle = "Empty Container";
                break;
            case 2:  //container sealed
                StrTitle = "Sealed Container";
                break;
            case 3:  //container sealed condition
                StrTitle = "Sealed Condition";
                break;
            case 4:  //other
                StrTitle = "Other Picture";
                break;
        }
        getActivity().setTitle(StrTitle);

        //-----START DECLARE---------------------------------------------------------------------------------------
        ((TextView) v.findViewById(R.id.tvInfo)).setText(StrInfo);

        Button btnSend = (Button) v.findViewById(R.id.btnNext);
        btnSend.setOnClickListener(this);

//        btnAdd = (Button) v.findViewById(R.id.btnAdd);
//        btnAdd.setOnClickListener(this);

        layout = v.findViewById(R.id.ll);
//        ivThumbnail = v.findViewById(R.id.ivThumbnail);
        gridPhoto =  v.findViewById(R.id.gridPhoto);
        fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(this);
        //-----END DECLARE---------------------------------------------------------------------------------------
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(btmSheetUploadMethodFragment != null) btmSheetUploadMethodFragment.dismiss();
    }

    public void onClick(View v) {
        v.startAnimation(global.buttoneffect);
        if(v.getId() == R.id.fab){
//            btmSheetUploadMethodFragment = new BtmSheetUploadMethodFragment();
//            btmSheetUploadMethodFragment.show(getFragmentManager(), btmSheetUploadMethodFragment.getTag());
            switch (urltype) {
                case 1:  //container empty
                    ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormPhotoEmptyContainer());
                    break;
                case 2:  //container sealed
                    ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormPhotoSealedContainerFragment());
                    break;
                case 3:  //container sealed condition
                    ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormPhotoSealedConditionFragment());
                    break;
                case 4:  //container other
                    ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormPhotoOtherPictureFragment());
                    break;
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //added by Tonny @19-Jan-2018 menampilkan kode JOB pada textView
        tvJob = (TextView) getView().findViewById(R.id.tvJob);
        tvJob.setText("Job No. " + LibInspira.getShared(global.temppreferences, global.temp.selected_job_kode, ""));

        String actionUrl = "ContainerLoading/getPhotos/";
        loadContainerPhoto = new LoadContainerPhoto();
        loadContainerPhoto.execute(actionUrl);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(loadContainerPhoto != null)loadContainerPhoto.cancel(true);
    }

    private class LoadContainerPhoto extends AsyncTask<String, Void, String> {
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
//                jsonObject.put("kodecontainer", kode);
                String kategori = "";
                switch (urltype) {
                    case 1:  //container empty
                        kategori = STRING_PHOTO_EMPTY;
                        break;
                    case 2:  //container sealed
                        kategori = STRING_PHOTO_SEALED_CONTAINER;
                        break;
                    case 3:  //container sealed condition
                        kategori = STRING_PHOTO_SEALED_CONDITION;
                        break;
                    case 4:  //container other
                        kategori = STRING_PHOTO_OTHER;
                        break;
                }
                jsonObject.put("kategori", kategori);
                jsonObject.put("nomorcontainer", LibInspira.getShared(global.temppreferences,global.temp.selected_container_nomor,""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("result", "onPostExecute: " + result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                for (int i = jsonarray.length() - 1; i >= 0; i--) {
                    final JSONObject obj = jsonarray.getJSONObject(i);
                    if(!obj.has("query")){
                        //create ivThumbnail programmatically
                        final ImageView newImageView = new ImageView(getContext());
//                        final RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//                        lp.setMargins(ConvertDpToPx(metrics, 10), ConvertDpToPx(metrics, 10), ConvertDpToPx(metrics, 10), ConvertDpToPx(metrics, 10));
                        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                        lp.setMargins(10, 10, 10, 10);
                        lp.gravity = Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK;
                        final String nama = obj.getString("nama");
                        newImageView.setLayoutParams(lp);
                        newImageView.setVisibility(View.VISIBLE);
                        newImageView.setClickable(true);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            newImageView.setMinimumHeight(ConvertDpToPx(metrics, 100));
                            newImageView.setMinimumWidth(ConvertDpToPx(metrics, 100));
                            newImageView.setMaxHeight(ConvertDpToPx(metrics, 100));
                            newImageView.setMaxWidth(ConvertDpToPx(metrics, 100));
                        }else{
                            newImageView.setMinimumHeight(ConvertDpToPx(metrics, 110));
                            newImageView.setMinimumWidth(ConvertDpToPx(metrics, 110));
                            newImageView.setMaxHeight(ConvertDpToPx(metrics, 110));
                            newImageView.setMaxWidth(ConvertDpToPx(metrics, 110));
                            newImageView.setPadding(10, 10, 10, 10);
                        }

                        final String photo_url = global.getUploadBaseURL(urltype) + obj.getString("nama");
                        newImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LibInspira.showShortToast(getContext(), nama);
//                                ReplaceFragment(getFragmentManager(), R.id.fragment_container, new FormPhotoFullscreen(photo_url));
                                setShared(global.temppreferences, global.temp.photo_url, photo_url);
                                startActivity(new Intent(getActivity(), FormPhotoFullscreen.class));
                            }
                        });
                        Picasso.get()
                                .load(photo_url)
                                .placeholder(R.drawable.failed)
                                .resize(LibInspira.ConvertDpToPx(metrics, 100), LibInspira.ConvertDpToPx(metrics, 100))
                                .centerCrop()
                                .into(newImageView);
                        gridPhoto.addView(newImageView);
                        Log.wtf("picasso url", photo_url);

                    }else{
                        LibInspira.showShortToast(getContext(), obj.getString("message"));
                        return;
                    }
                }
                LibInspira.hideLoading();
            }catch(Exception e)
            {
                e.printStackTrace();
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