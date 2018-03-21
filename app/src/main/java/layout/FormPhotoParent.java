package layout;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.inspira.lnj.IndexInternal.global;
import static com.inspira.lnj.LibInspira.createImageFile;

public class FormPhotoParent extends Fragment implements View.OnClickListener {
    protected ImageButton mImageButton;
    protected ImageView mImageView;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE_REQUEST = 3;

    public String StrTitle = "Empty Container";
    public String StrInfo = "Upload Empty Container Picture";

    private Button btnSend;
    protected TextView tvJob;

    protected Bitmap mImageBitmap;
    protected String mCurrentPhotoName = "";
    protected String mCurrentPhotoPath = "";
    protected String mCurrentPhotoPathRaw = "";

    BtmSheetUploadMethodFragment btmSheetUploadMethodFragment;
    TableLayout layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_form_upload_new, container, false);
        getActivity().setTitle(StrTitle);

        //-----START DECLARE---------------------------------------------------------------------------------------
        ((TextView) v.findViewById(R.id.tvInfo)).setText(StrInfo);

        btnSend = (Button) v.findViewById(R.id.btnNext);
        btnSend.setOnClickListener(this);

//        btnAdd = (Button) v.findViewById(R.id.btnAdd);
//        btnAdd.setOnClickListener(this);

        layout = (TableLayout) v.findViewById(R.id.ll);

        mImageView = (ImageView) v.findViewById(R.id.imageView);

        mImageButton = (ImageButton) v.findViewById(R.id.ib_add_picture);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //show option to upload from camera or gallery
                btmSheetUploadMethodFragment = new BtmSheetUploadMethodFragment();
                btmSheetUploadMethodFragment.show(getFragmentManager(), btmSheetUploadMethodFragment.getTag());
//                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
//                    // Create the File where the photo should go
//                    File photoFile = null;
//                    try {
//                        photoFile = createImageFile();
//                    } catch (IOException ex) {
//                        // Error occurred while creating the File
//                        Log.i("err", ex.toString());
//                    }
//                    // Continue only if the File was successfully created
//                    if (photoFile != null) {
//                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
//                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
//                    }
//                }
            }
        });
        //-----END DECLARE---------------------------------------------------------------------------------------

        checkingPrevious();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(btmSheetUploadMethodFragment != null) btmSheetUploadMethodFragment.dismiss();
    }

    protected void checkingPrevious()
    {
        if(!LibInspira.getShared(global.temppreferences, global.temp.photo_pathraw_empty_container, "").equals(""))
        {
            createNewImage();
//            btnAdd.setVisibility(View.GONE);

            String tempPath = LibInspira.getShared(global.temppreferences, global.temp.photo_path_empty_container, "");
            String tempPathRaw = LibInspira.getShared(global.temppreferences, global.temp.photo_pathraw_empty_container, "");
            String tempPhotoName = LibInspira.getShared(global.temppreferences, global.temp.photo_photoname_empty_container, "");

            String[] pieces1 = tempPath.trim().split("\\|");
            String[] pieces2 = tempPathRaw.trim().split("\\|");
            String[] pieces3 = tempPhotoName.trim().split("\\|");
            for(int i=0 ; i < pieces1.length ; i++){
                mCurrentPhotoPath = pieces1[i];
                mCurrentPhotoPathRaw = pieces2[i];
                mCurrentPhotoName = pieces3[i];

//                setImage();
            }
        }
    }

    protected void createNewImage()
    {
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("blah", R.drawable.camera);

        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (200 * scale + 0.5f);
        int pixelsPad = (int) (50 * scale + 0.5f);

        TableLayout.LayoutParams layoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, pixels);
        TableLayout.LayoutParams layoutParams1 = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
        layoutParams1.setMargins(0,0,0,40);

        final ImageView image = new ImageView(getContext());
        final Button btn = new Button(getContext());
        final Button btnSelect = new Button(getContext());
        final ImageButton image1 = new ImageButton(getContext());

        image.setLayoutParams(layoutParams);
        image.setBackgroundColor(getResources().getColor(R.color.colorBackground));
        image.setImageResource(map.get("blah"));
        image.setVisibility(View.GONE);

        // Adds the view to the layout
        layout.addView(image);
//        mImageView_.add(image);

        image1.setLayoutParams(layoutParams);
        image1.setBackgroundColor(getResources().getColor(R.color.colorBackground));
        image1.setImageResource(map.get("blah"));
        image1.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image1.setPadding(pixelsPad, pixelsPad, pixelsPad, pixelsPad);
        image1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        Log.i("err", ex.toString());
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                    }
                }
            }
        });
        // Adds the view to the layout
        layout.addView(image1);
//        mImageButton_.add(image1);

        btnSelect.setLayoutParams(layoutParams1);
        btnSelect.setText("Select From File");
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
            }
        });
        // Adds the view to the layout
        layout.addView(btnSelect);
//        mButtonSelect_.add(btnSelect);

        btn.setLayoutParams(layoutParams1);
        btn.setText("Remove Image");
        btn.setVisibility(View.GONE);

        // Adds the view to the layout
        layout.addView(btn);
//        mButton_.add(btn);
    }



    public void onClick(View v) {
        v.startAnimation(global.buttoneffect);
//        if(v.getId() == R.id.btnAdd){
//            createNewImage();
//            btnAdd.setVisibility(View.GONE);
//        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //added by Tonny @19-Jan-2018 menampilkan kode JOB pada textView
        tvJob = (TextView) getView().findViewById(R.id.tvJob);
        tvJob.setText("Job No. " + LibInspira.getShared(global.temppreferences, global.temp.selected_job_kode, ""));
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}