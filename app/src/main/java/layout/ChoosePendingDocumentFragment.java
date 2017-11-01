/******************************************************************************
    Author           : Tonny
    Description      : List document pending
    History          :

******************************************************************************/
package layout;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

public class ChoosePendingDocumentFragment extends Fragment implements View.OnClickListener{
    private EditText etSearch;
    private ImageButton ibtnSearch;

    private TextView tvInformation, tvNoData;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;
    private GetData getData;
    private SetDocumentStatus setDocumentStatus;

    protected String status = "Pending";

    public ChoosePendingDocumentFragment() {
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
        View v = inflater.inflate(R.layout.fragment_choose, container, false);
        getActivity().setTitle("Pending Document");
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

        itemadapter = new ItemListAdapter(getActivity(), R.layout.list_doc, new ArrayList<ItemAdapter>());
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

        String actionUrl = "Order/getDocList/";
        getData = new GetData();
        getData.execute( actionUrl );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(getData != null){
            getData.cancel(true);
        }
        if(setDocumentStatus != null){
            setDocumentStatus.cancel(true);
        }
        status = "";
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

    protected void refreshList()
    {
        itemadapter.clear();
        list.clear();

        String data = LibInspira.getShared(global.datapreferences, global.data.doclist, "");
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
                    String[] parts = pieces[i].trim().split("~");

                    String nomor = parts[0];
                    String kode = parts[1];
                    String nomormhadmin = parts[2];
                    String tanggal = parts[3];
                    String nama = parts[4];

                    if(nomor.equals("")) nomor = "null";
                    if(kode.equals("")) kode = "null";
                    if(nomormhadmin.equals("")) nomormhadmin = "null";
                    if(tanggal.equals("")) tanggal = "null";
                    if(nama.equals("")) nama = "null";

                    ItemAdapter dataItem = new ItemAdapter();
                    dataItem.setNomor(nomor);
                    dataItem.setKode(kode);
                    dataItem.setNomormhadmin(nomormhadmin);
                    dataItem.setTanggal(tanggal);
                    dataItem.setNama(nama);
                    list.add(dataItem);

                    itemadapter.add(dataItem);
                    itemadapter.notifyDataSetChanged();
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
                jsonObject.put("status", status);
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
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String kode = (obj.getString("kode"));
                            String nomormhadmin = (obj.getString("nomormhadmin"));
                            String tanggal = (obj.getString("tanggal"));
                            String nama = (obj.getString("nama"));

                            if(nomor.equals("")) nomor = "null";
                            if(kode.equals("")) kode = "null";
                            if(nomormhadmin.equals("")) nomormhadmin = "null";
                            if(tanggal.equals("")) tanggal = "null";
                            if(nama.equals("")) nama = "null";

                            tempData = tempData + nomor + "~" + kode + "~" + nomormhadmin + "~" + tanggal + "~" + nama + "|";
                        }
                    }
                    if(!tempData.equals(LibInspira.getShared(global.datapreferences, global.data.doclist, "")))
                    {
                        LibInspira.setShared(
                                global.datapreferences,
                                global.data.doclist,
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
        private String nomormhadmin;
        private String tanggal;
        private String kode;


        public ItemAdapter() {}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getNama() {return nama;}
        public void setNama(String _param) {this.nama = _param;}

        public String getNomormhadmin() {return nomormhadmin;}
        public void setNomormhadmin(String _param) {this.nomormhadmin = _param;}

        public String getTanggal() {return tanggal;}
        public void setTanggal(String _param) {this.tanggal = _param;}

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
            TextView tvKodeDoc, tvTanggal, tvNama;
            ImageButton ibtnAccept, ibtnReject;
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

            holder.tvKodeDoc = (TextView) row.findViewById(R.id.tvKodeDoc);
            holder.tvTanggal = (TextView) row.findViewById(R.id.tvTanggal);
            holder.tvNama = (TextView) row.findViewById(R.id.tvNama);
            holder.ibtnAccept = (ImageButton)row.findViewById(R.id.ibtnAccept);
            holder.ibtnReject = (ImageButton)row.findViewById(R.id.ibtnReject);

            row.setTag(holder);
            setupItem(holder, row);

            final Holder finalHolder = holder;
            final View finalRow = row;
//            row.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_nomor_doc, finalHolder.adapterItem.getNomor());
//                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_kode_doc, finalHolder.adapterItem.getKode());
//                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_nomormhadmin, finalHolder.adapterItem.getNomormhadmin());
//                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_tanggal, finalHolder.adapterItem.getTanggal());
//                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_namamhadmin, finalHolder.adapterItem.getNama());
//                }
//            });

            if(status == "finish"){
                holder.ibtnAccept.setVisibility(View.INVISIBLE);
            }else{
                holder.ibtnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LibInspira.alertBoxYesNo("Accept Document", "Do you want to accept this document?", getActivity(), new Runnable() {
                            @Override
                            public void run() {
                                LibInspira.setShared(global.temppreferences, global.temp.selected_nomor_doc, finalHolder.adapterItem.getNomor());
                                setStatus(1);
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {
                                //do nothing
                            }
                        });
                    }
                });
            }

            holder.ibtnReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LibInspira.alertBoxYesNo("Reject Document", "Do you want to reject this document?", getActivity(), new Runnable() {
                        @Override
                        public void run() {
                            LibInspira.setShared(global.temppreferences, global.temp.selected_nomor_doc, finalHolder.adapterItem.getNomor());
                            setStatus(0);
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            //do nothing
                        }
                    });
                }
            });

            return row;
        }

        private void setupItem(final Holder holder, final View row) {
            holder.tvKodeDoc.setText(holder.adapterItem.getKode().toUpperCase());
            holder.tvTanggal.setText(LibInspira.FormatDateBasedOnInspiraDateFormat(holder.adapterItem.getTanggal(), "dd MMM yyyy"));
            holder.tvNama.setText(holder.adapterItem.getNama().toUpperCase());
        }
    }

    //added by Tonny @02-Nov-2017
    private void setStatus(int _doAccept){
        String actionUrl = "Order/acceptDoc/";
        if(_doAccept == 0){
            actionUrl = "Order/rejectDoc/";
        }
        setDocumentStatus = new SetDocumentStatus();
        setDocumentStatus.execute(actionUrl);
    }

    private class SetDocumentStatus extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
            try {
                jsonObject.put("nomordoc", LibInspira.getShared(global.temppreferences, global.temp.selected_nomor_doc, ""));
                jsonObject.put("nomormhadmin", LibInspira.getShared(global.userpreferences, global.user.nomor, ""));
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
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if (!obj.has("query")) {
                            LibInspira.setShared(global.datapreferences, global.data.doclist, "");
                            refreshList();
                            String actionUrl = "Order/getDocList/";
                            getData = new GetData();
                            getData.execute(actionUrl);
                        } else {
                            Log.wtf("result query ", result);
                        }
                    }
                }
                LibInspira.hideLoading();
            }
            catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Processing Documents", "Loading...");
        }
    }

}
