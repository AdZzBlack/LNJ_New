package layout;

import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.inspira.lnj.IndexInternal.global;

/**
 * Created by Arta on 01-Dec-17.
 */

public class ChatItemAdapter extends BaseAdapter {
    List<ChatMsgContainer> data = new ArrayList<>();
    Context con;
    String userId;
    FragmentManager fragmentManager;

    private final String TAG = "chatitemadapter";
    public ChatItemAdapter(Context con)
    {
        this.con = con;
    }

    public void setFM(FragmentManager fm)
    {
        this.fragmentManager = fm;
    }
    public ChatItemAdapter(Context con, List<ChatMsgContainer> data)
    {
        this.con = con;
        userId = LibInspira.getShared(global.userpreferences, global.user.nomor, "");

        if(data.size()>0) {
            // reset log date dan unread message
            //remove semua log
            List<Integer> listDel = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                if (!data.get(i).getType().equals(ChatMsgContainer.typeMSG)) {
                    listDel.add(i);
                }
            }

            Collections.sort(listDel, Collections.reverseOrder());
            for (int i : listDel) {
                data.remove(i);
            }

            //Log.d(TAG,data.get(0).getType()+"|"+data.get(0).getId());
            //create date log
            String prevDate = "";
            prevDate = data.get(0).getSendTime().substring(0, 10);
            data.add(0, new ChatMsgContainer(dateToString(prevDate), 1));
            for (int i = 0; i < data.size(); i++) {
                if (!data.get(i).getType().equals(ChatMsgContainer.typeLOG)) {
                    String loopDate = data.get(i).getSendTime().substring(0, 10);
                    if (!prevDate.equals(loopDate)) {
                        data.add(i, new ChatMsgContainer(dateToString(loopDate), 1));
                        prevDate = loopDate;
                    }
                }
            }

            //create unread message log
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).getType().equals(ChatMsgContainer.typeMSG)
                        && !data.get(i).getFrom_id().equals(userId)) {
                    if (!data.get(i).getStatus().equals(ChatMsgContainer.statusRead)
                            && !data.get(i).getStatus().equals(ChatMsgContainer.statusReadDelivered)) {
                        data.add(i, new ChatMsgContainer(countUnreadMsg(data) + " Unread Message", 2));
                        break;
                    }
                }
            }
        }

        this.data = data;
    }

    public void reset(List<ChatMsgContainer> data)
    {
        // hapus semua log
        // create ualng semua log. mulai dari log tanggal sampai log unreadLOH
        userId = LibInspira.getShared(global.userpreferences, global.user.nomor, "");

        if(data.size()>0) {
            // reset log date dan unread message
            //remove semua log
            List<Integer> listDel = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                if (!data.get(i).getType().equals(ChatMsgContainer.typeMSG)) {
                    listDel.add(i);
                }
            }

            Collections.sort(listDel, Collections.reverseOrder());
            for (int i : listDel) {
                data.remove(i);
            }

            //Log.d(TAG,data.get(0).getType()+"|"+data.get(0).getId());
            //create date log
            if(data.get(0).getSendTime().length() > 10) {
                String prevDate = "";
                prevDate = data.get(0).getSendTime().substring(0, 10);
                data.add(0, new ChatMsgContainer(dateToString(prevDate), 1));
                for (int i = 0; i < data.size(); i++) {
                    if (!data.get(i).getType().equals(ChatMsgContainer.typeLOG)) {
                        if (data.get(i).getSendTime().length() > 10) {
                            String loopDate = data.get(i).getSendTime().substring(0, 10);
                            if (!prevDate.equals(loopDate)) {
                                data.add(i, new ChatMsgContainer(dateToString(loopDate), 1));
                                prevDate = loopDate;
                            }
                        }
                    }
                }

                //create unread message log
                for (int i = 0; i < data.size(); i++) {
                    if (data.get(i).getType().equals(ChatMsgContainer.typeMSG)
                            && !data.get(i).getFrom_id().equals(userId)) {
                        if (!data.get(i).getStatus().equals(ChatMsgContainer.statusRead)
                                && !data.get(i).getStatus().equals(ChatMsgContainer.statusReadDelivered)) {
                            data.add(i, new ChatMsgContainer(countUnreadMsg(data) + " Unread Message", 2));
                            break;
                        }
                    }
                }
            }
        }

        this.data = data;
    }


    private String dateToString(String yyyy_mm_hh)
    {
        // ubah format date dati angka menjadi kalimat
        String dateString = "";

        String year = yyyy_mm_hh.substring(0,4);
        String month = yyyy_mm_hh.substring(5,7);
        String day = yyyy_mm_hh.substring(8);

        if(month.equals("01") || month.equals("1"))
        {month = "Januari";}
        else if(month.equals("02") || month.equals("2"))
        {month = "Februari";}
        else if(month.equals("03") || month.equals("3"))
        {month = "Maret";}
        else if(month.equals("04") || month.equals("4"))
        {month = "April";}
        else if(month.equals("05") || month.equals("5"))
        {month = "Mei";}
        else if(month.equals("06") || month.equals("6"))
        {month = "Juni";}
        else if(month.equals("07") || month.equals("7"))
        {month = "Juli";}
        else if(month.equals("08") || month.equals("8"))
        {month = "Agustus";}
        else if(month.equals("09") || month.equals("9"))
        {month = "September";}
        else if(month.equals("10"))
        {month = "Oktober";}
        else if(month.equals("11"))
        {month = "Novomber";}
        else if(month.equals("12"))
        {month = "Desember";}

        dateString = month+", "+day+" "+year;
        return dateString;
    }

    private String countUnreadMsg(List<ChatMsgContainer> chatdata)
    {
        // hitung jumlah message yang belum ter baca (status < read)
        int counter = 0;
        for(int i=0;i<chatdata.size();i++)
        {
            if(chatdata.get(i).getType().equals(ChatMsgContainer.typeMSG)
                    && !chatdata.get(i).getStatus().equals(ChatMsgContainer.statusRead)
                    && !chatdata.get(i).getStatus().equals(ChatMsgContainer.statusReadDelivered)
                    && !chatdata.get(i).getFrom_id().equals(userId))
            {
                counter++;
            }
        }
        return String.valueOf(counter);
    }
    public boolean isUnreadLog()
    {
        // untuk cari apakah ada log unread message di adapter
        for(ChatMsgContainer temp : this.data)
        {
            if(temp.getId().equals(ChatMsgContainer.id_UnreadMsg))
            {
                return true;
            }
        }
        return false;
    }
    public int unreadLogPosition()
    {
        // cek apakah ada unreadLOG dan return posisinya (untuk remove biasanya)
        if(isUnreadLog()) {
            for (int i = 0;i< this.data.size() ;i++) {
                if (this.data.get(i).getId().equals(ChatMsgContainer.id_UnreadMsg)) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void add (ChatMsgContainer newmsg)
    {
        this.data.add(newmsg);
        notifyDataSetChanged();
    }

    public void clear()
    {
        data.clear();
    }

//    public void add (ChatMsgContainer newMsg)
//    {
//        Log.d("chatFragAdapter","asd"+newMsg.getMessage());
//        ChatMsgContainer temp = new ChatMsgContainer();
//        temp.copy(newMsg);
//        data.add(newMsg);
//        //this.notifyDataSetInvalidated();
//        this.notifyDataSetChanged();
//    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }
    // untuk update status, cek nomor, get posisi i, get item i, set status

    @Override
    public long getItemId(int i) {
        return i;
    }

//    @Override
//    public int getViewTypeCount() {
//        if(getCount() > 1) {
//            return data.size();
//        }
//        return 1;
//    }
//
//    @Override
//    public int getItemViewType(int position) {
//        return position-1;
//    }


    List<Target> targets = new ArrayList<>();

    static class Holder{

        LinearLayout llMsgYou;
            LinearLayout llMsgYou_text;
                TextView tvMsgYou,tvDateTimeYou;;
            LinearLayout llMsgYou_img;
                ImageView ivPicContainerYou;
                TextView tvDateTimeImageYou;
            TextView tvStatusYou;

        LinearLayout llMsgOther;
            LinearLayout llMsgOther_text;
                TextView tvMsgOther,tvDateTimeOther;
            LinearLayout llMsgOther_img;
                ImageView ivPicContainerOther;
                TextView tvDateTimeImageOther;
            TextView tvNameOther;

        LinearLayout llLog;
            TextView tvLog;

        LinearLayout llLogFull;
            TextView tvLogFull;


    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        View view = convertView;
        Holder holder;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //view = inflater.inflate(R.layout.chat_list_adapter , null);
            view = inflater.inflate(R.layout.chat_list_adapter , parent, false);
            holder = new Holder();

            holder.tvMsgYou = (TextView) view.findViewById(R.id.tvMsgContainer_you);
            holder.tvStatusYou = (TextView) view.findViewById(R.id.tvMsgStatus_you);
            holder.tvDateTimeYou = (TextView) view.findViewById(R.id.tvMsgDateTime_you);

            holder.ivPicContainerYou = (ImageView) view.findViewById(R.id.ivPicContainer_you);
            holder.tvDateTimeImageYou = (TextView) view.findViewById(R.id.tvMsgDateTime_image_you);

            holder.tvMsgOther = (TextView) view.findViewById(R.id.tvMsgContainer_other);
            holder.tvDateTimeOther = (TextView) view.findViewById(R.id.tvMsgDateTime_other);
            holder.tvNameOther = (TextView) view.findViewById(R.id.tvName_other);

            holder.ivPicContainerOther = (ImageView) view.findViewById(R.id.ivPicContainer_other);
            holder.tvDateTimeImageOther = (TextView) view.findViewById(R.id.tvMsgDateTime_image_other);

            holder.tvLog  = (TextView) view.findViewById(R.id.tvLog);
            holder.tvLogFull  = (TextView) view.findViewById(R.id.tvLogFull);

            holder.llMsgYou = (LinearLayout) view.findViewById(R.id.llMsgYou);
            holder.llMsgOther = (LinearLayout)view.findViewById(R.id.llMsgOther);

            holder.llLog = (LinearLayout)view.findViewById(R.id.llLog);
            holder.llLogFull = (LinearLayout)view.findViewById(R.id.llLogFull);

            holder.llMsgYou_text = (LinearLayout)view.findViewById(R.id.LLmsg_you_text);
            holder.llMsgYou_img = (LinearLayout)view.findViewById(R.id.LLmsg_you_img);

            holder.llMsgOther_text = (LinearLayout)view.findViewById(R.id.LLmsg_other_text);
            holder.llMsgOther_img = (LinearLayout)view.findViewById(R.id.LLmsg_other_img);

            view.setTag(holder);
        }
        else
        {
            holder = (Holder) view.getTag();
        }

        setAllViewVisibility(holder,View.VISIBLE);


        //setAllView(view,View.VISIBLE);

//        view.findViewById(R.id.llMsgYou).setVisibility(View.VISIBLE);
//        view.findViewById(R.id.llMsgOther).setVisibility(View.VISIBLE);
//        view.findViewById(R.id.llLog).setVisibility(View.VISIBLE);
//        view.findViewById(R.id.llLogFull).setVisibility(View.VISIBLE);
//
//        view.findViewById(R.id.LLmsg_you_text).setVisibility(View.VISIBLE);
//        view.findViewById(R.id.LLmsg_you_img).setVisibility(View.VISIBLE);
//
//        view.findViewById(R.id.LLmsg_other_text).setVisibility(View.VISIBLE);
//        view.findViewById(R.id.LLmsg_other_img).setVisibility(View.VISIBLE);


        if (data.get(position).getType().equals(ChatMsgContainer.typeMSG))
        {
            //Log.d("chatFragAdapter","uname "+uname);
            if (data.get(position).getFrom_id().equals(userId)) {
                //Log.d("chatFragAdapter","if you " + data.get(position).getMessage());
                // you
                setMainViewGoneExcept(holder,holder.llMsgYou.getId());

                if(data.get(position).getMsgType().equals(ChatMsgContainer.message_data_type_string)) {
                    view.findViewById(R.id.LLmsg_you_img).setVisibility(View.GONE);
                    if (!data.get(position).getSendTime().equals("")) {
                        holder.tvDateTimeYou.setText(data.get(position).getSendTime().substring(11, 16));
                    }
                    holder.tvMsgYou.setText(data.get(position).getMessage());
//                    holder.tvMsgYou.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            Log.d("knkn","onclcikasdfasd");
//                        }
//                    });
                }
                else if(data.get(position).getMsgType().equals(ChatMsgContainer.message_data_type_picture))
                {
                    view.findViewById(R.id.LLmsg_you_text).setVisibility(View.GONE);
                    if (!data.get(position).getSendTime().equals("")) {
                        holder.tvDateTimeImageYou.setText(data.get(position).getSendTime().substring(11, 16));
                    }

                    holder.ivPicContainerYou.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // new activity dan show gambar
                            // download di activity ini aja
                            Log.d("knkn","onclcik");
                            ChatImageViewer chatImageViewerFrag = new ChatImageViewer();
                            chatImageViewerFrag.setup(data,data.get(position).getId());
                            LibInspira.ReplaceFragment(fragmentManager, R.id.fragment_container, chatImageViewerFrag);

                        }
                    });

                    Picasso.with(con)
                            .load(GlobalVar.URL_SERVER_PICTURE_PATH+data.get(position).getMessage())
                            .resize(200, 200)
                            .centerCrop()
                            .placeholder(R.drawable.cast_album_art_placeholder)
                            .into(holder.ivPicContainerYou);

//                    final TargetClass tg = new TargetClass();
//                    tg.setup(position);
//                    final Target tg = new Target() {
//                        @Override
//                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                            Log.d("picass","on bitmap 1"+data.get(position).getMessage());
//                            ivPicContainerYou.setImageBitmap(bitmap);
//
//                            //save image dan set message jadi path folder offline
//                            String path = ChatFragment.saveImage(con,bitmap);
//
//                            ChatMsgContainer temp = new ChatMsgContainer();
//                            temp.copy(data.get(position));
//                            temp.setMessage(path);
//                            replaceMessage(temp,"");
//
////                            Log.d("picass","on bitmap "+listChatData.size());
////
//                            Log.d("picass","on bitmap 2"+data.get(position).getMessage());
//                        }
//
//                        @Override
//                        public void onBitmapFailed(Drawable errorDrawable) {
//                            //set gone semua - asumsi file gambar di hapus
//                            //setAllView(viewFinal,View.GONE);
//                        }
//
//                        @Override
//                        public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                        }
//                    };
//                    //targets.add(tg);
//
//
////                    //Log.d("picass","1st load "+data.get(position).getMessage());
//                    Picasso.with(con)
//                            .load(new File(data.get(position).getMessage()))
////                            .networkPolicy(NetworkPolicy.OFFLINE)
//                            .resize(200, 200)
//                            .centerCrop()
//                            .placeholder(R.drawable.cast_album_art_placeholder)
//                            .into(ivPicContainerYou, new Callback() {
//                                @Override
//                                public void onSuccess() {
//                                    Log.d("picass","on success "+data.get(position).getMessage());
//                                }
//
//                                @Override
//                                public void onError() {
//                                    Log.d("picass","on err "+data.get(position).getMessage());
//                                    Picasso.with(con)
//                                            .load(GlobalVar.URL_SERVER_PICTURE_PATH+data.get(position).getMessage())
//                                            .resize(200, 200)
//                                            .centerCrop()
//                                            .into(tg);
//                                    ivPicContainerYou.setTag(tg);
//
////                                    Picasso.with(con)
////                                        .load(GlobalVar.URL_SERVER_PICTURE_PATH+data.get(position).getMessage())
////                                        .resize(200, 200)
////                                        .centerCrop()
////                                        .placeholder(R.drawable.cast_album_art_placeholder)
////                                        .into(ivPicContainerYou);
//                                }
//                            });

                }


                if (ChatMsgContainer.statusSend.equals(data.get(position).getStatus())) {
                    holder.tvStatusYou.setText("S");
                } else if (ChatMsgContainer.statusDelivered.equals(data.get(position).getStatus())) {
                    holder.tvStatusYou.setText("D");
                } else if (ChatMsgContainer.statusRead.equals(data.get(position).getStatus())) {
                    holder.tvStatusYou.setText("R");
                }
                else if (ChatMsgContainer.statusReadDelivered.equals(data.get(position).getStatus())) {
                    holder.tvStatusYou.setText("RD");
                }
                else
                {
                    holder.tvStatusYou.setText("UNK");
                }
            }
            else
            {
                // other
                //Log.d("chatFragAdapter", "if other " + data.get(position).getMessage());
                setMainViewGoneExcept(holder,holder.llMsgOther.getId());

                holder.tvNameOther.setText(data.get(position).getFrom_nama());
                if(data.get(position).getMsgType().equals(ChatMsgContainer.message_data_type_string)) {
                    view.findViewById(R.id.LLmsg_other_img).setVisibility(View.GONE);
                    holder.tvMsgOther.setText(data.get(position).getMessage());
                    if(!data.get(position).getSendTime().equals(""))
                    {
                        holder.tvDateTimeOther.setText(data.get(position).getSendTime().substring(11,16));
                    }
                }
                else if(data.get(position).getMsgType().equals(ChatMsgContainer.message_data_type_picture)){
                    view.findViewById(R.id.LLmsg_other_text).setVisibility(View.GONE);
                    if (!data.get(position).getSendTime().equals("")) {
                        holder.tvDateTimeImageOther.setText(data.get(position).getSendTime().substring(11, 16));
                    }
                    Log.d(TAG,data.get(position).getMessage());

                    holder.ivPicContainerOther.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // new activity dan show gambar
                            // download di activity ini aja
                            Log.d("knkn","onclcik");
                            ChatImageViewer chatImageViewerFrag = new ChatImageViewer();
                            chatImageViewerFrag.setup(data,data.get(position).getId());
                            LibInspira.ReplaceFragment(fragmentManager, R.id.fragment_container, chatImageViewerFrag);

                        }
                    });

                    Picasso.with(con)
                            .load(GlobalVar.URL_SERVER_PICTURE_PATH+data.get(position).getMessage())
                            .resize(200, 200)
                            .centerCrop()
                            .placeholder(R.drawable.cast_album_art_placeholder)
                            .into(holder.ivPicContainerOther);

                }

            }
        } else if (data.get(position).getType().equals(ChatMsgContainer.typeLOG)) {
            setMainViewGoneExcept(holder,holder.llLog.getId());
            holder.tvLog.setText(data.get(position).getMessage());
        }
        else if (data.get(position).getType().equals(ChatMsgContainer.typeLOG_Full)) {

            setMainViewGoneExcept(holder,holder.llLogFull.getId());;
            holder.tvLogFull.setText(data.get(position).getMessage());
        }

        return view;
    }

//    private class TargetClass implements Target{
//        int position;
//        public TargetClass()
//        {
//
//        }
//        public void setup(int position)
//        {
//            this.position = position;
//        }
//
//        public Target getInstance()
//        {
//            return this;
//        }
//
//        @Override
//        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//            Log.d("picass","on bitmap 1"+data.get(this.position).getMessage());
//            ivPicContainerYou.setImageBitmap(bitmap);
//
//            //save image dan set message jadi path folder offline
//            String path = ChatFragment.saveImage(con,bitmap);
//
//            ChatMsgContainer temp = new ChatMsgContainer();
//            temp.copy(data.get(this.position));
//            temp.setMessage(path);
//            replaceMessage(temp,"");
//
////                            Log.d("picass","on bitmap "+listChatData.size());
////
//            Log.d("picass","on bitmap 2"+data.get(this.position).getMessage());
//        }
//
//        @Override
//        public void onBitmapFailed(Drawable errorDrawable) {
//
//        }
//
//        @Override
//        public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//        }
//    }

//    private Target AdapterTarget(final int position)
//    {
//        target = new Target() {
//            @Override
//            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                Log.d("picass","on bitmap 1"+data.get(position).getMessage());
//                ivPicContainerYou.setImageBitmap(bitmap);
//
//                //save image dan set message jadi path folder offline
//                String path = ChatFragment.saveImage(con,bitmap);
//
//                ChatMsgContainer temp = new ChatMsgContainer();
//                temp.copy(data.get(position));
//                temp.setMessage(path);
//                replaceMessage(temp,"");
//
////                            Log.d("picass","on bitmap "+listChatData.size());
////
//                Log.d("picass","on bitmap 2"+data.get(position).getMessage());
//            }
//
//            @Override
//            public void onBitmapFailed(Drawable errorDrawable) {
//                //set gone semua - asumsi file gambar di hapus
//                //setAllView(viewFinal,View.GONE);
//            }
//
//            @Override
//            public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//            }
//        };
//        return target;
//    }

    private void setAllViewVisibility(Holder holder, int status)
    {
        holder.llMsgYou.setVisibility(status);
        holder.llMsgOther.setVisibility(status);

        holder.llLog.setVisibility(status);
        holder.llLogFull.setVisibility(status);

        holder.llMsgYou_text.setVisibility(status);
        holder.llMsgYou_img.setVisibility(status);

        holder.llMsgOther_text.setVisibility(status);
        holder.llMsgOther_img.setVisibility(status);
    }

    private void setMainViewGoneExcept(Holder holder, int id)
    {
        if(id == holder.llMsgYou.getId())
        {
            holder.llMsgOther.setVisibility(View.GONE);
            holder.llLog.setVisibility(View.GONE);
            holder.llLogFull.setVisibility(View.GONE);
        }
        else if(id == holder.llMsgOther.getId())
        {
            holder.llMsgYou.setVisibility(View.GONE);
            holder.llLog.setVisibility(View.GONE);
            holder.llLogFull.setVisibility(View.GONE);
        }
        else if(id == holder.llLog.getId())
        {
            holder.llMsgYou.setVisibility(View.GONE);
            holder.llMsgOther.setVisibility(View.GONE);
            holder.llLogFull.setVisibility(View.GONE);
        }
        else if(id == holder.llLogFull.getId())
        {
            holder.llMsgYou.setVisibility(View.GONE);
            holder.llMsgOther.setVisibility(View.GONE);
            holder.llLog.setVisibility(View.GONE);
        }
    }

}
