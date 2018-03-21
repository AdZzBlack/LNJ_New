/******************************************************************************
    Author           : Tonny
    Description      : Bottom Sheet Dialog for Upload Source
    History          :

******************************************************************************/
package layout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.inspira.lnj.IndexInternal.global;

public class BtmSheetUploadMethodFragment extends BottomSheetDialogFragment implements View.OnClickListener{
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
    protected int ctrImage = 0;
    private ListView lvSearch;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PICK_IMAGE_REQUEST = 3;
    private ArrayList<ItemAdapter> list;
    private ItemListAdapter itemadapter;
    private String SOURCE_CAMERA = "Camera";
    private String SOURCE_GALLERY = "Gallery";
    protected Bitmap mImageBitmap;
    protected String mCurrentPhotoName = "";
    protected String mCurrentPhotoPath = "";
    protected String mCurrentPhotoPathRaw = "";
    public BtmSheetUploadMethodFragment() {
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
        View v = inflater.inflate(R.layout.fragment_bottom_dialog, container, false);
        return v;
    }

    //added by Tonny @15-Jul-2017
    //untuk mapping UI pada fragment, jangan dilakukan pada OnCreate, tapi dilakukan pada onActivityCreated
    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);
        list = new ArrayList<ItemAdapter>();
        itemadapter = new ItemListAdapter(getActivity(), R.layout.list_option, new ArrayList<ItemAdapter>());
        lvSearch = (ListView) getView().findViewById(R.id.lvChoose);
        lvSearch.setAdapter(itemadapter);
        refreshList();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
//            Log.d("url", mCurrentPhotoPath);
//        }
//        else if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
//            File f = new File(MediaFilePath.getPath(getActivity().getBaseContext(), data.getData()));
//            mCurrentPhotoPath = "file:" + f.getAbsolutePath();
//            mCurrentPhotoPathRaw = f.getAbsolutePath();
//            mCurrentPhotoName = f.getName();
//        }
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

    private void refreshList() //isi data static
    {
        itemadapter.clear();
        list.clear();

        ItemAdapter dataItem = new ItemAdapter();
        dataItem.setNama(SOURCE_CAMERA);
        list.add(dataItem);
        itemadapter.add(dataItem);
        itemadapter.notifyDataSetChanged();

        dataItem = new ItemAdapter();
        dataItem.setNama(SOURCE_GALLERY);
        list.add(dataItem);
        itemadapter.add(dataItem);
        itemadapter.notifyDataSetChanged();
    }

    private class ItemAdapter {
        private String nama;
        public String getNama() {return nama;}
        public void setNama(String _param) {this.nama = _param;}
    }

    private class ItemListAdapter extends ArrayAdapter<ItemAdapter> {

        private List<ItemAdapter> items;
        private int layoutResourceId;
        private Context context;

        public ItemListAdapter(Context context, int layoutResourceId, List<ItemAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public List<ItemAdapter> getItems() {
            return items;
        }

        public class Holder {
            ItemAdapter adapterItem;
            TextView tvNama;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            Holder holder = null;

            if(row==null)
            {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
            }

            holder = new Holder();
            holder.adapterItem = items.get(position);
            holder.tvNama = (TextView)row.findViewById(R.id.tvName);
            holder.tvNama.setVisibility(View.VISIBLE);

            row.setTag(holder);
            setupItem(holder, row);

            final Holder finalHolder = holder;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("document"))
//                    {
//                        LibInspira.setShared(global.temppreferences, global.temp.selected_nama_driver, finalHolder.adapterItem.getNama());
//                        LibInspira.BackFragment(getFragmentManager());
//                    }
                    if(finalHolder.adapterItem.getNama().equals(SOURCE_CAMERA)){ //jika upload dari kamera
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                            // Create the File where the photo should go
                            File photoFile = null;
                            try {
                                photoFile = LibInspira.createImageFile();
                                mCurrentPhotoPath = "file:" + photoFile.getAbsolutePath();
                                mCurrentPhotoPathRaw = photoFile.getAbsolutePath();
                                mCurrentPhotoName = photoFile.getName();
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
                    }else if(finalHolder.adapterItem.getNama().equals(SOURCE_GALLERY)){  //jika upload dari gallery
                        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        getIntent.setType("image/*");

                        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        pickIntent.setType("image/*");

                        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                        startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
                    }
                    LibInspira.setShared(global.temppreferences, global.temp.photo_pathraw_empty_container, pathRaw);
                    LibInspira.setShared(global.temppreferences, global.temp.photo_path_empty_container, path);
                    LibInspira.setShared(global.temppreferences, global.temp.photo_photoname_empty_container, photoName);
                }
            });
            return row;
        }

        private void setupItem(final Holder holder, final View row) {
            holder.tvNama.setText(holder.adapterItem.getNama().toUpperCase());
            Log.wtf("getnama", holder.adapterItem.getNama().toUpperCase());
            holder.tvNama.setVisibility(View.VISIBLE);
            holder.tvNama.setTextColor(getResources().getColor(R.color.colorPrimary));
        }
    }
}
