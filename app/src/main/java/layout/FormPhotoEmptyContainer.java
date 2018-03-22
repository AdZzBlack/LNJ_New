package layout;

import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.inspira.lnj.IndexInternal.global;

public class FormPhotoEmptyContainer extends Fragment implements View.OnClickListener {
    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE_REQUEST = 3;

    public String StrTitle = "Empty Container";
    public String StrInfo = "Upload Empty Container Picture";

    private Button btnSend;
    protected Button btnAdd;
    protected TextView tvJob;

    protected ArrayList<ImageView> mImageView_;
    protected ArrayList<ImageButton> mImageButton_;
    protected ArrayList<Button> mButton_;
    protected ArrayList<Button> mButtonSelect_;
    protected ArrayList<String> mPath_;
    protected ArrayList<String> mPathRaw_;
    protected ArrayList<String> mPhotoName_;
    protected String mPath = "";
    protected String mPathRaw = "";
    protected String photoName = "";
    protected String path = "";
    protected String pathRaw = "";
    protected ImageButton mImageButton;
    protected ImageView mImageView;
    protected int ctrImage = 0;

    protected Bitmap mImageBitmap;
    protected String mCurrentPhotoName = "";
    protected String mCurrentPhotoPath = "";
    protected String mCurrentPhotoPathRaw = "";

    TableLayout layout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_form_upload, container, false);
        getActivity().setTitle(StrTitle);

        //-----START DECLARE---------------------------------------------------------------------------------------
        ((TextView) v.findViewById(R.id.tvInfo)).setText(StrInfo);

        btnSend = (Button) v.findViewById(R.id.btnNext);
        btnSend.setOnClickListener(this);

        btnAdd = (Button) v.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

        layout = (TableLayout) v.findViewById(R.id.ll);

        mImageView_ = new ArrayList<ImageView>();
        mImageButton_ = new ArrayList<ImageButton>();
        mButton_ = new ArrayList<Button>();
        mButtonSelect_ = new ArrayList<Button>();
        mPath_ = new ArrayList<String>();
        mPathRaw_ = new ArrayList<String>();
        mPhotoName_ = new ArrayList<String>();

        mImageView = (ImageView) v.findViewById(R.id.imageView);

        mImageButton = (ImageButton) v.findViewById(R.id.ib_camera);
        mImageButton.setOnClickListener(new View.OnClickListener() {
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

        //-----END DECLARE---------------------------------------------------------------------------------------

        checkingPrevious();
        return v;
    }

    protected void checkingPrevious()
    {
        if(!LibInspira.getShared(global.temppreferences, global.temp.photo_pathraw_empty_container, "").equals(""))
        {
            createNewImage();
            btnAdd.setVisibility(View.GONE);

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

                setImage();
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
        mImageView_.add(image);

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
        mImageButton_.add(image1);

        btnSelect.setLayoutParams(layoutParams1);
        btnSelect.setText("Select From File");
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
            }
        });
        // Adds the view to the layout
        layout.addView(btnSelect);
        mButtonSelect_.add(btnSelect);

        btn.setLayoutParams(layoutParams1);
        btn.setText("Remove Image");
        btn.setVisibility(View.GONE);

        // Adds the view to the layout
        layout.addView(btn);
        mButton_.add(btn);
    }

    protected void setImage()
    {
        mImageBitmap = decodeBitmap(mCurrentPhotoPathRaw, 1920, 1080);
        if(mImageView_.size()!=0)
        {
            mImageView_.get(mImageView_.size()-1).setImageBitmap(mImageBitmap);
            mImageButton_.get(mImageButton_.size()-1).setVisibility(View.GONE);
            mButtonSelect_.get(mButtonSelect_.size()-1).setVisibility(View.GONE);
            mButton_.get(mButton_.size()-1).setVisibility(View.VISIBLE);
            mImageView_.get(mImageView_.size()-1).setVisibility(View.VISIBLE);
            mPathRaw_.add(mCurrentPhotoPathRaw);
            mPath_.add(mCurrentPhotoPath);
            mPhotoName_.add(mCurrentPhotoName);
            final int ctr = mPath_.size()-1;
            final int ctrIView = mImageView_.size()-1;
            final int ctrIButton = mImageButton_.size()-1;
            final int ctrButton = mButton_.size()-1;

            mImageView_.get(mImageView_.size()-1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ctr", ctr + ", " + ctrIButton + ", " + ctrIView + ", " + ctrButton);
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(mPath_.get(ctr)), "image/*");
                    startActivity(intent);
                }
            });

            mButton_.get(mButton_.size()-1).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ctr", ctr + ", " + ctrIButton + ", " + ctrIView + ", " + ctrButton);

                    File f = new File(mPath_.get(ctr));
                    f.delete();
                    f = new File(mPathRaw_.get(ctr));
                    f.delete();

                    mPath_.set(ctr, "");
                    mPathRaw_.set(ctr, "");
                    mPhotoName_.set(ctr, "");
                    mImageButton_.get(ctrIButton).setVisibility(View.GONE);
                    mButton_.get(ctrButton).setVisibility(View.GONE);
                    mImageView_.get(ctrIView).setVisibility(View.GONE);
                    layout.removeView(mImageButton_.get(ctrIButton));
                    layout.removeView(mButton_.get(ctrButton));
                    layout.removeView(mImageView_.get(ctrIView));
                    if(mButton_.size()==0)
                    {
                        btnAdd.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
        createNewImage();

    }

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
                }
            }
            LibInspira.setShared(global.temppreferences, global.temp.photo_pathraw_empty_container, pathRaw);
            LibInspira.setShared(global.temppreferences, global.temp.photo_path_empty_container, path);
            LibInspira.setShared(global.temppreferences, global.temp.photo_photoname_empty_container, photoName);
            LibInspira.BackFragment(getActivity().getSupportFragmentManager());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {

            Log.d("url", mCurrentPhotoPath);
            setImage();

        }
        else if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            File f = new File(MediaFilePath.getPath(getActivity().getBaseContext(), data.getData()));
            mCurrentPhotoPath = "file:" + f.getAbsolutePath();
            mCurrentPhotoPathRaw = f.getAbsolutePath();
            mCurrentPhotoName = f.getName();

            setImage();
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath(), ".LNJ");
        if(!storageDir.exists()){
            storageDir.mkdirs();
        }

        File image = File.createTempFile(
                imageFileName,  // prefix
                ".jpg",         // suffix
                storageDir      // directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        mCurrentPhotoPathRaw = image.getAbsolutePath();
        mCurrentPhotoName = image.getName();
        return image;
    }

    private Bitmap decodeBitmap(String path, int DESIREDWIDTH, int DESIREDHEIGHT) {
        String strMyImagePath = null;
        Bitmap scaledBitmap = null;

        try {
            // Part 1: Decode image
            Bitmap unscaledBitmap = ScalingUtilities.decodeFile(path, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);

            if (!(unscaledBitmap.getWidth() <= DESIREDWIDTH && unscaledBitmap.getHeight() <= DESIREDHEIGHT)) {
                // Part 2: Scale image
                scaledBitmap = ScalingUtilities.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingUtilities.ScalingLogic.FIT);
            } else {
                unscaledBitmap.recycle();
            }
        }
        catch (Throwable e) {
        }

        return  scaledBitmap;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //added by Tonny @19-Jan-2018 menampilkan kode JOB pada textView
        tvJob = (TextView) getView().findViewById(R.id.tvJob);
        tvJob.setText("Job No. " + LibInspira.getShared(global.temppreferences, global.temp.selected_job_kode, ""));
    }
}