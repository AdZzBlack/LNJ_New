package layout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Arta on 18-Jan-18.
 */

public class ChatImageViewer extends Fragment implements View.OnClickListener{
    public ChatImageViewer() {
        // Required empty public constructor
    }

    List<ChatMsgContainer> data = new ArrayList<>();
    String idMsg;
    public void setup(List<ChatMsgContainer> data)
    {
        this.data = data;
    }
    public void setup(List<ChatMsgContainer> data, String idMsg)
    {
        this.data = data;
        this.idMsg = idMsg;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.chat_image_viewer, container, false);
        getActivity().setTitle("View Image");
        return v;
    }


    /*****************************************************************************/
    //OnAttach dijalankan pada saat fragment ini terpasang pada Activity penampungnya
    /*****************************************************************************/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    ImageView imgViewer;
    Button btnDownload;
    String msgImg="";
    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);

        getView().findViewById(R.id.llparent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //biar ga bocor
            }
        });

        btnDownload = (Button) getView().findViewById(R.id.btnDownloadImg);
        imgViewer = (ImageView) getView().findViewById(R.id.ivImageViewer);

        btnDownload.setOnClickListener(this);

        msgImg="";
        for(ChatMsgContainer temp : this.data)
        {
            if(idMsg.equals(temp.getId()))
            {
                msgImg = temp.getMessage();
                break;
            }
        }

        Log.d("imgv",msgImg);
        //added by Tonny @23-Mar-2018
        Picasso.get()
                .load(GlobalVar.URL_SERVER_PICTURE_PATH+msgImg)
                .centerInside()
                .resize(1000,1000)
                .placeholder(R.drawable.cast_album_art_placeholder)
                .into(imgViewer);
        //remarked by Tonny @23-Mar-2018  older version
//        Picasso.with(con)
//                .load(GlobalVar.URL_SERVER_PICTURE_PATH+msgImg)
//                .centerInside()
//                .resize(1000,1000)
//                .placeholder(R.drawable.cast_album_art_placeholder)
//                .into(imgViewer);

    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id==R.id.btnDownloadImg)
        {
//            try{
////                Bitmap bm = Picasso.with(con)
////                        .load(GlobalVar.URL_SERVER_PICTURE_PATH+msgImg).get();
//                String path = ChatFragment.saveImage(con,bm);
//                LibInspira.ShowShortToast(con,"Download in progress");
//
////                ChatMsgContainer temp = new ChatMsgContainer();
////                temp.copy(data.get(position));
////                temp.setMessage(path);
////                IndexInternal.replaceMessage(temp,"");
//            }catch (Exception e)
//            {
//                LibInspira.ShowShortToast(con,"Download err");
//            }

            final Target tg = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    String path = ChatFragment.saveImage(getContext(),bitmap);
                    LibInspira.showShortToast(getContext(),"Saved");

//                            ChatMsgContainer temp = new ChatMsgContainer();
//                            temp.copy(data.get(position));
//                            temp.setMessage(path);
//                            replaceMessage(temp,"");

                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {

                }

                public void onBitmapFailed(Drawable errorDrawable) {
                    //set gone semua - asumsi file gambar di hapus
                    //setAllView(viewFinal,View.GONE);
                    LibInspira.showShortToast(getContext(),"Download err");
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            Picasso.get()
                    .load(GlobalVar.URL_SERVER_PICTURE_PATH+msgImg)
                    .into(tg);

            //remarked by Tonny @23-Mar-2018  older version
//            Picasso.with(con)
//                    .load(GlobalVar.URL_SERVER_PICTURE_PATH+msgImg)
//                    .into(tg);

        }
    }


}
