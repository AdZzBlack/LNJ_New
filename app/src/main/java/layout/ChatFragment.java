package layout;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.inspira.lnj.BackgroundTask;
import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import io.socket.emitter.Emitter;

import static com.inspira.lnj.BackgroundTask.listChatData;
import static com.inspira.lnj.BackgroundTask.mSocket;
import static com.inspira.lnj.IndexInternal.global;

/**
 * Created by Arta on 01-Dec-17.
 */

public class ChatFragment extends Fragment implements View.OnClickListener {
    public ChatFragment()
    {

    }

    ChatItemAdapter mitemListAdapter;
    public void setup(Context con)
    {
        mitemListAdapter = new ChatItemAdapter(con);
    }

    ChatData mChatData;
    public void setAdapter(ChatData data)
    {
        mChatData = data;
        Log.d("msglala","size "+mChatData.getChatMsgData().size()+"");
        mitemListAdapter.reset(mChatData.getChatMsgData());
    }
    String chatRoomName="", mToUserId ="";
    public void setChatName(String name)
    {
        chatRoomName = name;
        mToUserId = LibInspira.getShared(global.chatPreferences, global.chat.chat_to_id, "");
    }

    private String TAG = "chatFrag";
    //private Socket mSocket;
//    private Boolean isConnected = true;
    private String mUsername = "";
    private boolean mTyping = false;
    String data_flag = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        log("on create");
        setHasOptionsMenu(true);

        LibInspira.setShared(GlobalVar.chatPreferences, GlobalVar.chat.chat_menu_position, "chatFrag");


        mUserid = LibInspira.getShared(global.userpreferences, global.user.nomor, "");
        mUsername = LibInspira.getShared(global.userpreferences, global.user.nama, "");


//        ChatApplication app = (ChatApplication) getActivity().getApplication();
//        mSocket = app.getSocket();
//        mSocket.on(Socket.EVENT_CONNECT,onConnect);
//        mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect);
//        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
//        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("new message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);
        mSocket.on("login", onLogin);



        //mSocket.connect();
        //mSocket.emit("add user", mUsername);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //mSocket.disconnect();

//        mSocket.off(Socket.EVENT_CONNECT, onConnect);
//        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
//        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
//        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("new message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);
        mSocket.off("login", onLogin);

        LibInspira.setShared(
                global.chatPreferences,
                global.chat.chat_to_id, "");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.chat_main_layout, container, false);
        getActivity().setTitle(chatRoomName);
        log("create view");
        return v;
    }

    EditText mInputMessageView;
    private Handler mTypingHandler = new Handler();
    private static final int TYPING_TIMER_LENGTH = 600;
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        log("view create");
        mInputMessageView = (EditText) view.findViewById(R.id.message_input);
        mInputMessageView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int id, KeyEvent event) {
                if (id == R.id.send || id == EditorInfo.IME_NULL) {
                    attemptSend(data_flag);
                    return true;
                }
                return false;
            }
        });
        mInputMessageView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (null == mUsername) return;
                if (!mSocket.connected()) return;

                if (!mTyping) {
                    mTyping = true;

                    JSONObject jsonObject;
                    jsonObject = new JSONObject();
                    //---------------------------------------------HEADER-----------------------------------------------------//
                    try {
                        //kirim data diri sendiri
                        jsonObject.put("uname",mUsername);
                        jsonObject.put("id_room_info",mChatData.getMroomInfo().getIdRoom());
                        jsonObject.put("id",mUserid);
                        mSocket.emit("typing",jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                mTypingHandler.removeCallbacks(onTypingTimeout);
                mTypingHandler.postDelayed(onTypingTimeout, TYPING_TIMER_LENGTH);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if( imgPath!=null && imgPath.equals("") ) {
                    data_flag = ChatMsgContainer.message_data_type_string;
                }
            }
        });

        ImageButton sendButton = (ImageButton) view.findViewById(R.id.send_button);
        sendButton.setOnClickListener(this);


    }

    private Context con;
    @Override
    public void onAttach(Context context)
    {
        con = context;
        super.onAttach(context);
        log("attach");
    }


    ListView lvChatMsgList;
    TextView tvUserAction;
    LinearLayout llAttachFile, llSelectedImage;
    ImageButton ibAttachFile,ibGallery,ibCamera;
    ImageView ivPreview;
    //List<ChatMsgContainer> dataMsg = new ArrayList<>();
    String mRoom;
    String mUserid;

    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);
        log("act create");

        mitemListAdapter.setFM(getActivity().getSupportFragmentManager());
        lvChatMsgList = (ListView) getView().findViewById(R.id.lvMsgList);
        lvChatMsgList.setAdapter(mitemListAdapter);

        tvUserAction = (TextView) getView().findViewById(R.id.tvUserAction);
        llAttachFile = (LinearLayout) getView().findViewById(R.id.llAttachFile);
        llSelectedImage = (LinearLayout) getView().findViewById(R.id.llSelectedImage);

        ibAttachFile = (ImageButton) getView().findViewById(R.id.ib_attach_file_button);
        ibGallery = (ImageButton) getView().findViewById(R.id.ib_open_gallery);
        ibCamera = (ImageButton) getView().findViewById(R.id.ib_open_camera);

        ivPreview = (ImageView) getView().findViewById(R.id.ivPreview);
        ivPreview.setOnClickListener(this);

        ibAttachFile.setOnClickListener(this);
        ibGallery.setOnClickListener(this);
        ibCamera.setOnClickListener(this);


        BackgroundTask.updateStatusToRead(mChatData); //  asumsi user buka chat berarti sdh baca

        if(!mitemListAdapter.isUnreadLog())
        {
            scrollToBottom();
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    final int CAMERA_PIC_REQUEST_CODE = 1;
    final int GALLERY_PIC_REQUEST_CODE = 2;
    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id == R.id.ib_attach_file_button)
        {
            if(llAttachFile.getVisibility() == View.VISIBLE)
            {
                llAttachFile.setVisibility(View.INVISIBLE);
            }
            else
            {
                llAttachFile.setVisibility(View.VISIBLE);
            }

        }
        else if(id  == R.id.ib_open_camera)
        {
            if ( Build.VERSION.SDK_INT >= 23 &&
                    ContextCompat.checkSelfPermission( getContext(), Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED) {
                //LibInspira.ShowShortToast(getContext(),"someting wrong");
                requestPermissions(new String[]{Manifest.permission.CAMERA},
                        CAMERA_PIC_REQUEST_CODE);
                return;
            }else {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, CAMERA_PIC_REQUEST_CODE);
            }
        }
        else if(id  == R.id.ib_open_gallery)
        {
            Intent i = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.INTERNAL_CONTENT_URI);

            startActivityForResult(i, GALLERY_PIC_REQUEST_CODE);
        }
        else if(id == R.id.ivPreview)
        {
            LibInspira.alertBoxYesNo("Remove image", "Apakah menghapus image?", getActivity(), new Runnable() {
                    public void run() {
                        //YES
                        resetAttachedFile();
                    }
           }, new Runnable() {
                    public void run() {
                        //NO
                    }
                });
        }
        else if(id == R.id.send_button)
        {
            attemptSend(data_flag);
        }

//        if(id==R.id.ibtnSearch)
//        {
//            search();
//        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public static String saveImage(Context con, Bitmap finalBitmap) {

//        String root = Environment.getExternalStorageDirectory().toString();
//        File myDir = new File(root);
//        final File myDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_DCIM), "BABIES_IMG");
        File myDir = new File(Environment.getExternalStorageDirectory() +
                File.separator + "BABIES" + File.separator + "BABIES-IMG");
        boolean success = true;
        if (!myDir.exists()) {
            success = myDir.mkdirs();
        }

        if(success) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
            String image_name = sdf.format(new Date())
                    + "_" +
                    System.currentTimeMillis();
            String fname = "IMG-" + image_name + ".jpg";
            File imgFile = new File(myDir.getAbsolutePath() + File.separator + fname);
            if (imgFile.exists()) imgFile.delete();
            Log.d("saveimg", myDir.getAbsolutePath() + " " + fname);
            try {
                FileOutputStream out = new FileOutputStream(imgFile);
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                MediaScannerConnection.scanFile(con, new String[] { imgFile.getPath() }, new String[] { "image/jpeg" }, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return imgFile.getPath();
        }else
        {
            LibInspira.showShortToast(con,"Something Wrong cant save file");
            return "";
        }

    }

    private void setupAttachedFile(String type_file)
    {
        if(ChatMsgContainer.message_data_type_picture.equals(type_file))
        {
            data_flag = ChatMsgContainer.message_data_type_picture;
        }
        else if(ChatMsgContainer.message_data_type_video.equals(type_file))
        {
            data_flag = ChatMsgContainer.message_data_type_video;
        }

        mInputMessageView.setFocusableInTouchMode(false);
        mInputMessageView.setFocusable(false);
    }
    private void resetAttachedFile()
    {
        encodedString = "";
        imgPath = "";
        llSelectedImage.setVisibility(View.GONE);

        //enabling et
        mInputMessageView.setFocusableInTouchMode(true);
        mInputMessageView.setFocusable(true);
    }

    String imgPath = "";
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            llAttachFile.setVisibility(View.GONE);
            llSelectedImage.setVisibility(View.VISIBLE);
            if (requestCode == CAMERA_PIC_REQUEST_CODE) {
                if (resultCode == Activity.RESULT_OK) {
//                        String result = data.getStringExtra("result");
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    Picasso.with(con)
                            .load(getImageUri(con,imageBitmap))
                            .resize(200, 200)
                            .centerCrop()
                            .into(ivPreview);
                    saveImage(con,imageBitmap);
                    encodeImagetoString(imageBitmap);
//                    imgPath = getAbsolutePath(picUri);
//                    //fileSize(imgPath);
//                    encodeImagetoString();
                }
                if (resultCode == Activity.RESULT_CANCELED) {
                    //Write your code if there's no result
                    LibInspira.showShortToast(con,"cam result canceled");
                }
            } else if (requestCode == GALLERY_PIC_REQUEST_CODE) {
                if(data != null) {
                    if (resultCode == Activity.RESULT_OK) {
                        //String result = data.getStringExtra("result");
                        imgPath = getAbsolutePath(data.getData());
                        //fileSize(imgPath);
                        encodeImagetoString();
                    }
                    if (resultCode == Activity.RESULT_CANCELED) {
                        //Write your code if there's no result
                        LibInspira.showShortToast(con, "gallery result canceled");
                    }
                }
                else
                {
                    LibInspira.showShortToast(con,"no image selected");
                }
            }

        }
        catch (Exception e)
        {
            Log.d(TAG,e.toString());
            LibInspira.showShortToast(con,"Something Went Wrong");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PIC_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                LibInspira.showShortToast(getContext(), "camera granted");
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST_CODE);

            } else {
                LibInspira.showShortToast(getContext(), "camera denied");

            }
        }
    }

    public Bitmap rotateBitmap(Bitmap temp, String imgPath)
    {
        Bitmap myBitmap = temp;
        try {
            ExifInterface exif = new ExifInterface(imgPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }
            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
        }
        catch (IOException e) {
            LibInspira.showShortToast(getContext(), "rotate Fail");
        }
        return myBitmap;
    }


    public Bitmap resizeFile(String path) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, o);
            // The new size we want to scale to
            int REQUIRED_SIZE = 250;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;

            return BitmapFactory.decodeFile(path, o2);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;

    }

    public String getAbsolutePath(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor != null && cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        if(cursor!=null)
        cursor.close();
        return res;
    }

    Bitmap bitmap;
    String encodedString = "";
    // AsyncTask - To convert Image to String
    public void encodeImagetoString() {
        new AsyncTask<String, Void, String>() {

            protected void onPreExecute() {

            };

            @Override
            protected String doInBackground(String... params) {
                int compressVal = 100;

//                BitmapFactory.Options options = null;
//                options = new BitmapFactory.Options();
//                options.inSampleSize = 3;

                log(imgPath);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imgPath, options);
                options.inJustDecodeBounds = false;
                options.inSampleSize = ScalingUtilities.calculateSampleSize(options.outWidth, options.outHeight, 1920,
                        1080, ScalingUtilities.ScalingLogic.FIT);
                bitmap = BitmapFactory.decodeFile(imgPath,options);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy

                try {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                } catch (Exception e) {

                    e.printStackTrace();
                }

//                int size = 10000000;
//                while ( size > 150 * 1024) {
//                    if(compressVal <= 5)
//                    {
//                        break;
//                    }
//                    stream.reset();
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, compressVal, stream);
//                    byte[] temp_byte_arr = stream.toByteArray();
//                    size = temp_byte_arr.length;
//                    Log.d(TAG,size+"");
//                    compressVal-=5;
//                }
                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                encodedString = Base64.encodeToString(byte_arr, 0);
                log("success");
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                Picasso.with(con)
                        .load(new File(imgPath))
                        .resize(200, 200)
                        .centerCrop()
                        .into(ivPreview);
                //data_flag = ChatMsgContainer.message_data_type_picture;
                setupAttachedFile(ChatMsgContainer.message_data_type_picture);
            }
        }.execute(null, null, null);
    }

    public void encodeImagetoString(Bitmap img) {
        new AsyncTask<Bitmap, Void, String>() {

            protected void onPreExecute() {

            };

            @Override
            protected String doInBackground(Bitmap... params) {
                int compressVal = 100;


//                BitmapFactory.Options options = null;
//                options = new BitmapFactory.Options();
//                options.inSampleSize = 3;
//                //
//                bitmap = params[0];
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                // Must compress the Image to reduce image size to make upload easy
//                int size = 10000000;
//                while ( size > 110 * 1024) {
//                    if(compressVal <= 5)
//                    {
//                        break;
//                    }
//                    stream.reset();
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, compressVal, stream);
//                    byte[] temp_byte_arr = stream.toByteArray();
//                    size = temp_byte_arr.length;
//                    Log.d(TAG,size+"");
//                    compressVal-=5;
//                }


//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                BitmapFactory.decodeFile(imgPath, options);
//                options.inJustDecodeBounds = false;
//                options.inSampleSize = ScalingUtilities.calculateSampleSize(options.outWidth, options.outHeight, 1920,
//                        1080, ScalingUtilities.ScalingLogic.FIT);
//                bitmap = BitmapFactory.decodeFile(imgPath,options);

                bitmap = params[0];
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                // Must compress the Image to reduce image size to make upload easy

                try {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 75, stream);
                } catch (Exception e) {

                    e.printStackTrace();
                }

                byte[] byte_arr = stream.toByteArray();
                // Encode Image to String
                encodedString = Base64.encodeToString(byte_arr, 0);
                return "";
            }

            @Override
            protected void onPostExecute(String msg) {
                setupAttachedFile(ChatMsgContainer.message_data_type_picture);
            }
        }.execute(img, null, null);
    }

    public int fileSize(String filepath)
    {
        File file = new File(filepath);
        double fileSize = file.length();
        Log.d(TAG,"file size : "+fileSize);

        double sizeFilterA = 1024*1024;
        double sizeFilterB = 5*1024;
        if(fileSize < sizeFilterB)
        {
            return 0;
        }
        else if(fileSize > sizeFilterB && fileSize < sizeFilterA)
        {
            return 1;
        }
        else
        {
            return 2;
        }
    }



    private void attemptSend(String data_flag) {
        if (null == mUsername) return;
        if (!mSocket.connected()) return;

        mTyping = false;
        mUserid = LibInspira.getShared(global.userpreferences, global.user.nomor, "");
        mUsername = LibInspira.getShared(global.userpreferences, global.user.nama, "");


        log("attemp send" + encodedString);

        String message = "";
        if(data_flag.equals(ChatMsgContainer.message_data_type_string)) {
            message  = mInputMessageView.getText().toString().trim();
        }
        else if(data_flag.equals(ChatMsgContainer.message_data_type_picture))
        {
            message = encodedString;
        }
        if (TextUtils.isEmpty(message)) {
            mInputMessageView.requestFocus();
            return;
        }
        mInputMessageView.setText("");

//        this.id = id;
//        this.idRoom = idroom;
//        this.from_id = from_id;
//        this.from_nama = nama;
//        this.type = type;
//        this.msgType = msgType;
//        this.status = status;
//        this.message = message;
//        this.sendTime = sendTime;

        String[] newData = new String[9];
        newData[0] = UUID.randomUUID().toString();
        newData[1] = ChatMsgContainer.typeMSG;
        newData[2] = message;
        newData[3] = data_flag;
        newData[4] = mChatData.getMroomInfo().getIdRoom();
        newData[5] = ChatMsgContainer.statusPraSend;
        newData[6] = mUserid;
        newData[7] = mUsername;
        newData[8] = "";

        UUID.randomUUID().toString(); // untuk random uuid msg
        ChatMsgContainer newMsg = new ChatMsgContainer(
            newData[0],newData[1],newData[2],newData[3],
            newData[4], newData[5],newData[6],newData[7],
            newData[8]
        );

        ChatMsgContainer tempNewMsg = new ChatMsgContainer();
        tempNewMsg.copy(newMsg);
        if(data_flag.equals(ChatMsgContainer.message_data_type_picture))
        {
            tempNewMsg.setMessage(imgPath);
        }
        addMessage(tempNewMsg);
        //Log.d("indexInternal","chat frag add msg id "+newMsg.getId());

        // perform the sending message attempt.
        JSONObject jsonObject;
        jsonObject = new JSONObject();
        //---------------------------------------------HEADER-----------------------------------------------------//
        try {
            jsonObject.put("id",newMsg.getId());
            jsonObject.put("id_room_info",newMsg.getIdRoom());
            jsonObject.put("from_id",newMsg.getFrom_id());
            jsonObject.put("from_nama",newMsg.getFrom_nama());
            jsonObject.put("message_type",newMsg.getType());
            jsonObject.put("message_data_type",newMsg.getMsgType());
            jsonObject.put("status",newMsg.getStatus());
            jsonObject.put("message_data", newMsg.getMessage());
            jsonObject.put("sendTime",newMsg.getSendTime());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //log(jsonObject.toString());
        mSocket.emit("new message", jsonObject.toString());
        resetAttachedFile();
    }

    private void hideTyping()
    {
        tvUserAction.setVisibility(View.GONE);
    }
    private void showTyping(String uname)
    {
        log("typing");
        tvUserAction.setVisibility(View.VISIBLE);
        tvUserAction.setText(uname+" is Typing");
    }

    private void addMessage(ChatMsgContainer newMsgData)
    {
        int tempPost=0;
        boolean flag = false;
        for(int i=0;i<listChatData.size();i++)
        {
            if(listChatData.get(i).getMroomInfo().getIdRoom().equals(newMsgData.getIdRoom()))
            {
                tempPost = i;
                flag = true;
                break;
            }
        }

        if(flag) {
            //Log.d("msglala","2 size "+mChatData.getChatMsgData().size()+"");
            listChatData.get(tempPost).getChatMsgData().add(newMsgData);
            mChatData = listChatData.get(tempPost);
            //Log.d("msglala","3 size "+mChatData.getChatMsgData().size()+"");
            mitemListAdapter.reset(mChatData.getChatMsgData());
        }
//        mChatData = listChatData.get(tempPost);
//        dataMsg = mChatData.getChatMsgData();
        mitemListAdapter.notifyDataSetChanged();
        mitemListAdapter.notifyDataSetInvalidated();
        lvChatMsgList.invalidate();
        scrollToBottom();
    }
    private void replaceMessage(ChatMsgContainer newMsgData, String prevId)
    {
        // yg masuk sini sdh data bersih sdh di pisah id nya antara id db dengan id generate android
        // search di list berdasar prev id
        // replace semua dgn data baru
        int flag = 0;
        for(int i=0; i<mChatData.getChatMsgData().size();i++ )
        {
            if(!prevId.equals("")) {
                if (mChatData.getChatMsgData().get(i).getId().equals(prevId)) {
                    mChatData.getChatMsgData().get(i).copy(newMsgData);
                    flag = 1;
                    break;
                }
            }
            else
            {
                if (mChatData.getChatMsgData().get(i).getId().equals(newMsgData.getId())) {
                    mChatData.getChatMsgData().get(i).copy(newMsgData);
                    flag = 1;
                    break;
                }
            }
        }
        if(flag == 0)
        {
            mChatData.getChatMsgData().add(newMsgData);
        }

        //mitemListAdapter.add(mChatData.getChatMsgData());

        mitemListAdapter.notifyDataSetChanged();
        mitemListAdapter.notifyDataSetInvalidated();
        lvChatMsgList.invalidate();
        scrollToBottom();
    }

    private void scrollToBottom() {
        //lvChatMsgList.smoothScrollByOffset(mitemListAdapter.getCount() - 1);
        lvChatMsgList.post(new Runnable() {
            @Override
            public void run() {
                // Select the last row so it will scroll into view...
                lvChatMsgList.setSelection(mitemListAdapter.getCount() - 1);
            }
        });
    }

    private void scrollToPosition(final int pos) {
        //lvChatMsgList.smoothScrollByOffset(mitemListAdapter.getCount() - 1);
        lvChatMsgList.post(new Runnable() {
            @Override
            public void run() {
                lvChatMsgList.setSelection(pos);
            }
        });
    }

    private void addLog(String log)
    {
        //mitemListAdapter.add(new ChatMsgContainer("",log,ChatMsgContainer.typeLOG));
        mChatData.getChatMsgData().add(new ChatMsgContainer("",log,ChatMsgContainer.typeLOG));
        //mitemListAdapter.add(dataMsg);
        mitemListAdapter.notifyDataSetChanged();
        mitemListAdapter.notifyDataSetInvalidated();
        lvChatMsgList.invalidate();
    }

//    private Emitter.Listener onAppStart = new Emitter.Listener() {
//        @Override
//        public void call(Object... args) {
//            JSONObject data = (JSONObject) args[0];
//
//            try {
//                log(data.getString("socketid")+"");
//            } catch (JSONException e) {
//                return;
//            }
//        }
//    };


    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }

            log("on login "+numUsers);
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("onnewmsg","chat fragment msg");
                    // repalce data di jalankan di service/ activity parentnya, supaya chat ttp masuk meskipun ga buka chat fragement nya
//                    JSONObject obj = (JSONObject) args[0];
//                    String[] dataStr = new String[obj.length()];
////                    String username;
////                    String message;
//                    try {
//                        dataStr[0] = obj.getString("id");
//                        dataStr[1] = obj.getInt("message_type")+"";
//                        dataStr[2] = obj.getString("message_data");
//                        dataStr[3] = obj.getInt("message_data_type")+"";
//                        dataStr[4] = obj.getInt("id_room_info")+"";
//                        dataStr[5] = obj.getInt("status")+"";
//                        dataStr[6] = obj.getInt("from_id")+"";
//                        dataStr[7] = obj.getString("from_nama");
//                        dataStr[8] = obj.getString("sendTime");
//
//                    } catch (JSONException e) {
//                        Log.e(TAG, e.getMessage());
//                        return;
//                    }
//                    log("new message");
//                    hideTyping();
////                    removeTyping(username);
//
//                    String prevId = "";
//                    if(dataStr[0].contains("~")) {
//                        String[] id_piece = dataStr[0].trim().split("\\~");
//                        if (id_piece.length > 0) {
//                            dataStr[0] = id_piece[0];
//                            prevId = id_piece[1];
//                        }
//                    }
//
//                    ChatMsgContainer newMsg = new ChatMsgContainer(
//                            dataStr[0],dataStr[1],dataStr[2],dataStr[3],
//                            dataStr[4],dataStr[5],dataStr[6],dataStr[7],
//                            dataStr[8]
//                    );
//
//                    //addMessage(newMsg);
//                    // cek misal from dr diri sendiri ga ush di update
//                    if(newMsg.getStatus().equals(ChatMsgContainer.statusSend) && newMsg.getFrom_id().equals(mUserid)) {
//                        //status send dan from yourself
//                        // tinggal replace message
//                        replaceMessage(newMsg,prevId);
//                    }
//                    else if(newMsg.getStatus().equals(ChatMsgContainer.statusSend) && !newMsg.getFrom_id().equals(mUserid))
//                    {
//                        newMsg.setStatus(ChatMsgContainer.statusDelivered);
//                        replaceMessage(newMsg,prevId);
//                        //send dan from other
//                        JSONObject jsonObject = new JSONObject();
//                        try {
//                            jsonObject.put("id",newMsg.getId());
//                            jsonObject.put("id_room_info",newMsg.getIdRoom());
//                            jsonObject.put("from_id",newMsg.getFrom_id());
//                            jsonObject.put("from_nama",newMsg.getFrom_nama());
//                            jsonObject.put("message_type",newMsg.getType());
//                            jsonObject.put("message_data_type",newMsg.getMsgType());
//                            jsonObject.put("status",newMsg.getStatus());
//                            jsonObject.put("message_data", newMsg.getMessage());
//                            jsonObject.put("sendTime",newMsg.getSendTime());
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        mSocket.emit("new message", jsonObject.toString());
//
//                    }
//                    else
//                    {
//                        replaceMessage(newMsg,prevId);
//                    }

                    mitemListAdapter.notifyDataSetChanged();
                    mitemListAdapter.notifyDataSetInvalidated();
                    lvChatMsgList.invalidate();

                    if(mitemListAdapter.isUnreadLog())
                    {
                        Log.d(TAG,"pos item : "+mitemListAdapter.unreadLogPosition());
                        scrollToPosition(mitemListAdapter.unreadLogPosition());
                    }
                    else {
                        scrollToBottom();
                    }

                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                        mRoom = data.getString("room");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    log("emit on user join");

                    addLog("user join "+username);
//                    addParticipantsLog(numUsers);
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    log("emit user left");
                    addLog("user left "+username);
//                    addLog(getResources().getString(R.string.message_user_left, username));
//                    addParticipantsLog(numUsers);
//                    removeTyping(username);
                    hideTyping();
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username,id;
                    try {
                        username = data.getString("username");
                        id = data.getInt("id")+"";
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    log("emit on typing");
                    // cek apakah sesuai dengan orang yang sedang di chat
                    if(mToUserId.equals(id))
                    {showTyping(username);}
//                    addTyping(username);
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                        return;
                    }
                    log("emit stop typing");
                    hideTyping();
//                    removeTyping(username);
                }
            });
        }
    };

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) return;

            mTyping = false;
            mSocket.emit("stop typing");
        }
    };

    private void log(String log)
    {
        String pos = "pos : ";
        Log.d(TAG,pos+log);
    }
}
