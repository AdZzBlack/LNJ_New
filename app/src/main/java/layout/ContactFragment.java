/******************************************************************************
    Author           : ADI
    Description      : dashboard untuk internal
    History          :

******************************************************************************/
package layout;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.inspira.lnj.BackgroundTask.listChatData;
import static com.inspira.lnj.BackgroundTask.mSocket;
import static com.inspira.lnj.IndexInternal.chatFrag;
import static com.inspira.lnj.IndexInternal.global;
import static com.inspira.lnj.IndexInternal.jsonObject;

//import android.app.Fragment;

public class ContactFragment extends Fragment implements View.OnClickListener{
    private EditText etSearch;
    private ImageButton ibtnSearch;

    private String TAG = "contactFrag";

    private TextView tvInformation, tvNoData;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;

    public ContactFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        super.onResume();
        LibInspira.setShared(GlobalVar.chatPreferences, GlobalVar.chat.chat_menu_position, "indexInternal");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_choose, container, false);
        getActivity().setTitle("Contact");
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
        list = new ArrayList<ItemAdapter>();

        ((RelativeLayout) getView().findViewById(R.id.rlSearch)).setVisibility(View.VISIBLE);
        tvInformation = (TextView) getView().findViewById(R.id.tvInformation);
        tvNoData = (TextView) getView().findViewById(R.id.tvNoData);
        etSearch = (EditText) getView().findViewById(R.id.etSearch);

        itemadapter = new ItemListAdapter(getActivity(), R.layout.list_item, new ArrayList<ItemAdapter>());
        itemadapter.clear();
        lvSearch = (ListView) getView().findViewById(R.id.lvChoose);
        lvSearch.setAdapter(itemadapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                search();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        refreshList();

        String actionUrl = "Master/getUser/";
        new getData().execute( actionUrl );
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id==R.id.ibtnSearch)
        {
            search();
        }
    }

    private void search()
    {
        itemadapter.clear();
        for(int ctr=0;ctr<list.size();ctr++)
        {
            if(etSearch.getText().equals(""))
            {
                if(!list.get(ctr).getNomor().equals(LibInspira.getShared(global.userpreferences, global.user.nomor, "")))
                {
                    itemadapter.add(list.get(ctr));
                    itemadapter.notifyDataSetChanged();
                }
            }
            else
            {
                if(LibInspira.contains(list.get(ctr).getNama(),etSearch.getText().toString() ))
                {
                    if(!list.get(ctr).getNomor().equals(LibInspira.getShared(global.userpreferences, global.user.nomor, "")))
                    {
                        itemadapter.add(list.get(ctr));
                        itemadapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    private void refreshList()
    {
        itemadapter.clear();
        list.clear();

        String data = LibInspira.getShared(global.datapreferences, global.data.contact, "");
        String[] pieces = data.trim().split("\\|");
        if(pieces.length==1 && pieces[0].equals(""))
        {
            tvNoData.setVisibility(View.VISIBLE);
        }
        else
        {
            tvNoData.setVisibility(View.GONE);
            for(int i=0 ; i < pieces.length ; i++){
                if(!pieces[i].equals(""))
                {
                    String[] parts = pieces[i].trim().split("\\~");

                    String nomor = parts[0];
                    String kode = parts[1];
                    String nama = parts[2];

                    if(nomor.equals("null")) nomor = "";
                    if(nama.equals("null")) nama = "";
                    if(kode.equals("null")) kode = "-";

                    ItemAdapter dataItem = new ItemAdapter();
                    dataItem.setNomor(nomor);
                    dataItem.setNama(nama);
                    dataItem.setKode(kode);
                    list.add(dataItem);

                    if(!dataItem.getNomor().equals(LibInspira.getShared(global.userpreferences, global.user.nomor, "")))
                    {
                        itemadapter.add(dataItem);
                        itemadapter.notifyDataSetChanged();

                        JSONObject jsonObject;
                        jsonObject = new JSONObject();
                        try {
                            jsonObject.put("creator",LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
                            jsonObject.put("member", nomor);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG,jsonObject.toString());

                        mSocket.emit("createNewRoom",jsonObject.toString());
                    }
                }
            }
        }
    }

    private class getData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("resultQuery", result);
            try
            {
                String tempData= "";
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String kode = (obj.getString("kode"));
                            String nama = (obj.getString("nama"));
//                            String hp = (obj.getString("nomorhp"));

                            if(nomor.equals("")) nomor = "null";
                            if(kode.equals("")) kode = "null";
                            if(nama.equals("")) nama = "null";

                            tempData = tempData + nomor + "~" + kode + "~" + nama + "|";
                        }
                    }

                    if(!tempData.equals(LibInspira.getShared(global.datapreferences, global.data.contact, "")))
                    {
                        LibInspira.setShared(
                                global.datapreferences,
                                global.data.contact,
                                tempData
                        );
                        refreshList();
                    }
                }
                tvInformation.animate().translationYBy(-80);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                tvInformation.animate().translationYBy(-80);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            tvInformation.setVisibility(View.VISIBLE);
        }
    }

    public class ItemAdapter {

        private String nomor;
        private String nama;
        private String hp;
        private String kode;

        public ItemAdapter() {}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getNama() {return nama;}
        public void setNama(String _param) {this.nama = _param;}

        public String getHp() {return hp;}
        public void setHp(String _param) {this.hp = _param;}

        public String getKode() {return kode;}
        public void setKode(String _param) {this.kode = _param;}
    }

    public class ItemListAdapter extends ArrayAdapter<ItemAdapter> {

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
            TextView tvLocation;
            ImageView ivCall;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
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
            holder.tvLocation = (TextView)row.findViewById(R.id.tvKeterangan);
            holder.ivCall = (ImageView)row.findViewById(R.id.ivCall);

            row.setTag(holder);
            setupItem(holder);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(GlobalVar.listeffect);
                    LibInspira.setShared(
                            global.chatPreferences,
                            global.chat.chat_to_id,
                            items.get(position).getNomor()
                    );
                    Log.d("msglala","row click "+listChatData.size());

                    if (chatFrag != null)chatFrag.resetAdapter();  //added by Tonny @06-Apr-2018 untuk mereset data chat (supaya tidak nyantol ke room lain)

                    for(int i=0;i < listChatData.size();i++) {
                        if(listChatData.get(i).getMroomInfo().getType().equals(ChatData.roomInfo.roomTypePC))
                        {
                            Log.d("msglala","if "+listChatData.get(i).getMroomInfo().getIdRoom());
                            if (listChatData.get(i).getMroomInfo().getRoomName().split("-")[1].equals(items.get(position).getNomor()+"|"+LibInspira.getShared(global.userpreferences, global.user.nomor, ""))||
                                    listChatData.get(i).getMroomInfo().getRoomName().split("-")[1].equals(LibInspira.getShared(global.userpreferences, global.user.nomor, "") + "|" + items.get(position).getNomor())) {
                                Log.d("msglala",listChatData.get(i).getMroomInfo().getIdRoom());
                                Log.d("msglala","5 size "+listChatData.get(i).getChatMsgData().size()+"");
                                chatFrag.setAdapter(listChatData.get(i));
                            }
                        }
                        else
                        {
                            //Group chat
                        }
                    }
                    chatFrag.setChatName(items.get(position).getNama());
                    LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, chatFrag);
                }
            });

            return row;
        }

        private void setupItem(final Holder holder) {
            holder.tvNama.setText(holder.adapterItem.getNama().toUpperCase());
        }
    }
}
