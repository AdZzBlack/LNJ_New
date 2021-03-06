/******************************************************************************
    Author           : ADI
    Description      : dashboard untuk internal
    History          :

******************************************************************************/
package layout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.inspira.lnj.IndexInternal.global;
import static com.inspira.lnj.IndexInternal.jsonObject;

//import android.app.Fragment;

public class ChooseUserFragment extends Fragment implements View.OnClickListener{
    private EditText etSearch;
    private ImageButton ibtnSearch;

    private TextView tvInformation, tvNoData;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;
    private GetData getData;

    private String actionUrl;
    private String tempSelectedUsers;

    public String getActionUrl() {
        return actionUrl;
    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public ChooseUserFragment() {
        // Required empty public constructor
        tempSelectedUsers = LibInspira.getShared(global.datapreferences, global.data.selectedUsers, "");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_choose, container, false);
        getActivity().setTitle("Choose User");
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

        if (LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("conversation")) {
            ((RelativeLayout) getView().findViewById(R.id.rlFooter)).setVisibility(View.VISIBLE);
            ((Button) getView().findViewById(R.id.btnCenter)).setVisibility(View.VISIBLE);
            ((Button) getView().findViewById(R.id.btnCenter)).setText("Invite");
            ((Button) getView().findViewById(R.id.btnCenter)).setOnClickListener(this);
        }

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

        if(actionUrl == null) actionUrl = "";
        //modified by Tonny @19-Feb-2018
        if(actionUrl.equals("")) setActionUrl("Master/getUser/");
        getData = new GetData();
        getData.execute( getActionUrl() );

        if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("tracking"))
        {
            getView().findViewById(R.id.rlFooter).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.btnLeft).setVisibility(View.VISIBLE);
            ((Button) getView().findViewById(R.id.btnLeft)).setText("Next");
            ((Button) getView().findViewById(R.id.btnLeft)).setOnClickListener(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(getData != null){
            getData.cancel(true);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("conversation"))
                        LibInspira.setShared(global.datapreferences, global.data.selectedUsers, tempSelectedUsers);
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id==R.id.ibtnSearch)
        {
            search();
        }
        else if(id==R.id.btnLeft)
        {
            String tempnomor = "";
            String tempkode = "";
            String tempnama = "";
            String nama = "";
            int countSelected = 0;
            for(int i = 0;i<itemadapter.getCount();i++)
            {
                if(itemadapter.getItem(i).getIsChoosen())
                {
                    tempnomor += itemadapter.getItem(i).getNomor() + "|";
                    tempkode += itemadapter.getItem(i).getKode() + "|";
                    tempnama += itemadapter.getItem(i).getNama() + "|";
                    nama = itemadapter.getItem(i).getNama();
                    countSelected++;
                }
            }

            if(countSelected>1) nama = "Mass";

            LibInspira.setShared(global.temppreferences, global.temp.selected_nomor_user, tempnomor);
            LibInspira.setShared(global.temppreferences, global.temp.selected_kode_user, tempkode);
            LibInspira.setShared(global.temppreferences, global.temp.selected_nama_user, tempnama);
            LibInspira.setShared(global.temppreferences, global.temp.selected_count, String.valueOf(countSelected));
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new LiveTrackingFragment().newInstance(nama));
        }
        else if(id==R.id.btnCenter)
        {
            if (LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("conversation"))
            {
                LibInspira.setShared(global.datapreferences, global.data.selectedUsers, tempSelectedUsers);
                LibInspira.BackFragment(getActivity().getSupportFragmentManager());
            }
        }
    }

    private void search()
    {
        itemadapter.clear();
        for(int ctr=0;ctr<list.size();ctr++)
        {
            if(etSearch.getText().equals(""))
            {
                itemadapter.add(list.get(ctr));
                itemadapter.notifyDataSetChanged();
            }
            else
            {
                if(LibInspira.contains(list.get(ctr).getNama(),etSearch.getText().toString() ))
                {
                    itemadapter.add(list.get(ctr));
                    itemadapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void refreshList()
    {
        itemadapter.clear();
        list.clear();

        String data = LibInspira.getShared(global.datapreferences, global.data.userlist, "");
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

                    if(parts.length==4)
                    {
                        String nomor = parts[0];
                        String kode = parts[1];
                        String nama = parts[2];
                        String cantracked = parts[3];

                        if(nomor.equals("null")) nomor = "";
                        if(kode.equals("null")) kode = "";
                        if(nama.equals("null")) nama = "";
                        if(cantracked.equals("null")) cantracked = "";

                        ItemAdapter dataItem = new ItemAdapter();
                        dataItem.setNomor(nomor);
                        dataItem.setKode(kode);
                        dataItem.setNama(nama);
                        dataItem.setCantracked(cantracked);
                        dataItem.setIsChoosen(false);
                        list.add(dataItem);

                        if (LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("conversation"))
                        {
                            if (tempSelectedUsers.contains(nama))
                                dataItem.setIsChoosen(true);
                        }

                        if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("tracking")
                                || LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("document"))
                        {
                            if(!nomor.equals(LibInspira.getShared(global.userpreferences, global.user.nomor, "")))
                            {
                                itemadapter.add(dataItem);
                                itemadapter.notifyDataSetChanged();
                            }
                        }
                        else
                        {
                            itemadapter.add(dataItem);
                            itemadapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    }

    private class GetData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
            try {
                jsonObject.put("nomor", LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                            String cantracked = (obj.getString("cantracked"));

                            if(nomor.equals("")) nomor = "null";
                            if(kode.equals("")) kode = "null";
                            if(nama.equals("")) nama = "null";
                            if(cantracked.equals("")) cantracked = "null";

                            tempData = tempData + nomor + "~" + kode + "~" + nama + "~" + cantracked + "|";
                        }
                    }
                    if(!tempData.equals(LibInspira.getShared(global.datapreferences, global.data.userlist, "")))
                    {
                        LibInspira.setShared(
                                global.datapreferences,
                                global.data.userlist,
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
        private String kode;
        private String cantracked;
        private Boolean isChoosen;

        public ItemAdapter() {}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getNama() {return nama;}
        public void setNama(String _param) {this.nama = _param;}

        public String getKode() {return kode;}
        public void setKode(String _param) {this.kode = _param;}

        public String getCantracked() {return cantracked;}
        public void setCantracked(String _param) {this.cantracked = _param;}

        public Boolean getIsChoosen() {return isChoosen;}
        public void setIsChoosen(Boolean _param) {this.isChoosen = _param;}
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

            row.setTag(holder);
            setupItem(holder, row);

            final Holder finalHolder = holder;
            final View finalRow = row;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("document"))
                    {
                        LibInspira.setShared(global.temppreferences, global.temp.selected_nomor_user, finalHolder.adapterItem.getNomor());
                        LibInspira.setShared(global.temppreferences, global.temp.selected_kode_user, finalHolder.adapterItem.getKode());
                        LibInspira.setShared(global.temppreferences, global.temp.selected_nama_user, finalHolder.adapterItem.getNama());
                        LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new QRCodeDocumentFragment());
                    }
                    else if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("tracking"))
                    {
                        if(finalHolder.adapterItem.getCantracked().equals("1"))
                        {
                            if(!finalHolder.adapterItem.isChoosen)
                            {
                                finalRow.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                                finalHolder.adapterItem.setIsChoosen(true);
                            }
                            else
                            {
                                finalRow.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                                finalHolder.adapterItem.setIsChoosen(false);
                            }
//                            LibInspira.setShared(global.temppreferences, global.temp.selected_nomor_user, finalHolder.adapterItem.getNomor());
//                            LibInspira.setShared(global.temppreferences, global.temp.selected_kode_user, finalHolder.adapterItem.getKode());
//                            LibInspira.setShared(global.temppreferences, global.temp.selected_nama_user, finalHolder.adapterItem.getNama());
//                            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new LiveTrackingFragment().newInstance(finalHolder.adapterItem.getNama()));
                        }
                        else
                        {
                            LibInspira.showLongToast(getContext(), "This user can't be tracked");
                        }
                    }
                    else if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("report livetracking") || LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("report deviation"))
                    {
                        LibInspira.setShared(global.temppreferences, global.temp.report_user, finalHolder.adapterItem.getNomor());
                        LibInspira.setShared(global.temppreferences, global.temp.report_user_name, finalHolder.adapterItem.getNama());
                        LibInspira.BackFragment(getActivity().getSupportFragmentManager());
                    }
                    else if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("report doc_from"))
                    {
                        LibInspira.setShared(global.temppreferences, global.temp.report_user_from, finalHolder.adapterItem.getNomor());
                        LibInspira.setShared(global.temppreferences, global.temp.report_user_from_name, finalHolder.adapterItem.getNama());
                        LibInspira.BackFragment(getActivity().getSupportFragmentManager());
                    }
                    else if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("report doc_to"))
                    {
                        LibInspira.setShared(global.temppreferences, global.temp.report_user_to, finalHolder.adapterItem.getNomor());
                        LibInspira.setShared(global.temppreferences, global.temp.report_user_to_name, finalHolder.adapterItem.getNama());
                        LibInspira.BackFragment(getActivity().getSupportFragmentManager());
                    }
                    else if (LibInspira.getShared(global.sharedpreferences, global.shared.position, "").contains("conversation")) {
                        if(finalHolder.adapterItem.getIsChoosen()) {
                            finalHolder.adapterItem.setIsChoosen(false);
                            finalRow.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                            finalHolder.tvNama.setTextColor(getResources().getColor(R.color.colorPrimary));
                            String remove = finalHolder.adapterItem.getNomor() + "~" + finalHolder.adapterItem.getNama() + "|";
                            tempSelectedUsers = tempSelectedUsers.replace(remove, "");
                        } else {
                            finalHolder.adapterItem.setIsChoosen(true);
                            finalRow.setBackgroundColor(getResources().getColor(R.color.colorAccentDanger));
                            finalHolder.tvNama.setTextColor(Color.WHITE);
                            tempSelectedUsers += finalHolder.adapterItem.getNomor() + "~" + finalHolder.adapterItem.getNama() + "|";
                        }
                    }
                }
            });

            return row;
        }

        private void setupItem(final Holder holder, final View row) {
            holder.tvNama.setText(holder.adapterItem.getNama().toUpperCase());
            if(holder.adapterItem.isChoosen)
            {
                row.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                holder.tvNama.setTextColor(Color.WHITE);
            }
            else
            {
                row.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                holder.tvNama.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        }
    }
}
