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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.inspira.gms.GlobalVar;
import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.inspira.gms.IndexInternal.global;
import static com.inspira.gms.IndexInternal.jsonObject;

//import android.app.Fragment;

public class ChooseCustomerFragment extends Fragment implements View.OnClickListener{
    private EditText etSearch;
    private ImageButton ibtnSearch;

    private TextView tvInformation, tvNoData;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;

    public ChooseCustomerFragment() {
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
        getActivity().setTitle("Customer");
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

        String actionUrl = "Master/getCustomer/";
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

        String data = LibInspira.getShared(global.datapreferences, global.data.customer, "");
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
                    String nama = parts[1];
                    String alamat = parts[2];
                    String telepon = parts[3];
                    String kode = parts[4];

                    if(nomor.equals("null")) nomor = "";
                    if(nama.equals("null")) nama = "";
                    if(alamat.equals("null")) alamat = "-";
                    if(telepon.equals("null")) telepon = "-";
                    if(kode.equals("null")) kode = "";

                    ItemAdapter dataItem = new ItemAdapter();
                    dataItem.setNomor(nomor);
                    dataItem.setNama(nama);
                    dataItem.setAlamat(alamat);
                    dataItem.setTelepon(telepon);
                    dataItem.setKode(kode);
                    list.add(dataItem);

                    itemadapter.add(dataItem);
                    itemadapter.notifyDataSetChanged();
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
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String nomor = (obj.getString("nomor"));
                            String nama = (obj.getString("nama"));
                            String alamat = (obj.getString("alamat"));
                            String telepon = (obj.getString("telepon"));
                            String kode = (obj.getString("kode"));

                            if(nomor.equals("")) nomor = "null";
                            if(nama.equals("")) nama = "null";
                            if(alamat.equals("")) alamat = "null";
                            if(alamat.equals("")) telepon = "null";
                            if(kode.equals("")) kode = "null";

                            tempData = tempData + nomor + "~" + nama + "~" + alamat + "~" + telepon + "~" + kode + "|";
                        }
                    }
                    if(!tempData.equals(LibInspira.getShared(global.datapreferences, global.data.customer, "")))
                    {
                        LibInspira.setShared(
                                global.datapreferences,
                                global.data.customer,
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
        private String alamat;
        private String telepon;
        private String kode;

        public ItemAdapter() {}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getNama() {return nama;}
        public void setNama(String _param) {this.nama = _param;}

        public String getAlamat() {return alamat;}
        public void setAlamat(String _param) {this.alamat = _param;}

        public String getTelepon() {return telepon;}
        public void setTelepon(String _param) {this.telepon = _param;}

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
            TextView tvKeterangan;
            TextView tvKeterangan1;
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
            holder.tvKeterangan = (TextView)row.findViewById(R.id.tvKeterangan);
            holder.tvKeterangan1 = (TextView)row.findViewById(R.id.tvKeterangan1);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(GlobalVar.listeffect);
                    if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("salesorder"))
                    {
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_customer_nomor, finalHolder.adapterItem.getNomor());
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_customer_kode, finalHolder.adapterItem.getKode());
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_customer_nama, finalHolder.adapterItem.getNama());
                        LibInspira.BackFragment(getActivity().getSupportFragmentManager());
                    }
                    else if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("schedule")) {
                        LibInspira.setShared(global.schedulepreferences, global.schedule.customerIDsch, finalHolder.adapterItem.getNomor());
                        LibInspira.setShared(global.schedulepreferences, global.schedule.customersch, finalHolder.adapterItem.getNama());
                        SummaryScheduleFragment summaryScheduleFragment = new SummaryScheduleFragment();
                        LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, summaryScheduleFragment);
                    }
                }
            });

            return row;
        }

        private void setupItem(final Holder holder) {
            holder.tvNama.setText(holder.adapterItem.getNama().toUpperCase());
            holder.tvKeterangan.setVisibility(View.VISIBLE);
            holder.tvKeterangan1.setVisibility(View.VISIBLE);
            holder.tvKeterangan.setText("Alamat: " + holder.adapterItem.getAlamat());
            holder.tvKeterangan1.setText("Telpon: " + holder.adapterItem.getTelepon());
        }
    }
}
