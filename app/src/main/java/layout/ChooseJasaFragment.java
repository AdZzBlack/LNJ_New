/******************************************************************************
    Author           : Tonny
    Description      : untuk browse pekerjaan / jasa
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

import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.inspira.gms.IndexInternal.global;
import static com.inspira.gms.IndexInternal.jsonObject;

//import android.app.Fragment;

public class ChooseJasaFragment extends Fragment implements View.OnClickListener{
    private EditText etSearch;
    private ImageButton ibtnSearch;

    private TextView tvInformation, tvNoData;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;

    public ChooseJasaFragment() {
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
        getActivity().setTitle("Choose Pekerjaan");
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

        String actionUrl = "Master/getPekerjaan/";
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

        String data = LibInspira.getShared(global.datapreferences, global.data.pekerjaan, "");
        String[] pieces = data.trim().split("\\|");

        if(pieces.length==1)
        {
            tvNoData.setVisibility(View.VISIBLE);
        }
        else
        {
            tvNoData.setVisibility(View.GONE);
            for(int i=0 ; i < pieces.length ; i++){
                Log.d("item", pieces[i] + "a");
                if(!pieces[i].equals(""))
                {
                    String[] parts = pieces[i].trim().split("\\~");

                    String nomor = parts[0];
                    String kode = parts[1];
                    String nama = parts[2];
                    String kodesatuan = parts[3];
                    String satuan = parts[4];
                    String hargacustomer = parts[5];
                    String hargamandor = parts[6];

                    if(nomor.equals("null")) nomor = "";
                    if(kode.equals("null")) kode = "";
                    if(nama.equals("null")) nama = "";
                    if(kodesatuan.equals("null")) kodesatuan = "";
                    if(satuan.equals("null")) satuan = "";
                    if(hargacustomer.equals("null")) hargacustomer = "";
                    if(hargamandor.equals("null")) hargamandor = "";

                    ItemAdapter dataItem = new ItemAdapter();
                    dataItem.setNomor(nomor);
                    dataItem.setKode(kode);
                    dataItem.setNama(nama);
                    dataItem.setKodeSatuan(kodesatuan);
                    dataItem.setSatuan(satuan);
                    dataItem.setHargaCustomer(hargacustomer);
                    dataItem.setHargaMandor(hargamandor);
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
                            String kode = (obj.getString("kode"));
                            String nama = (obj.getString("nama"));
                            String kodesatuan = (obj.getString("kodesatuan"));
                            String satuan = (obj.getString("satuan"));
                            String hargacustomer = (obj.getString("hargacustomer"));
                            String hargamandor = (obj.getString("hargamandor"));

                            if(nomor.equals("")) nomor = "null";
                            if(kode.equals("")) kode = "null";
                            if(nama.equals("")) nama = "null";
                            if(kodesatuan.equals("")) kodesatuan = "null";
                            if(satuan.equals("")) satuan = "null";
                            if(hargacustomer.equals("")) hargacustomer = "null";
                            if(hargamandor.equals("")) hargamandor = "null";

                            tempData = tempData + nomor + "~" + kode + "~" + nama + "~" + kodesatuan + "~" + satuan + "~" + hargacustomer + "~" + hargamandor + "|";
                        }
                    }
                    if(!tempData.equals(LibInspira.getShared(global.datapreferences, global.data.pekerjaan, "")))
                    {
                        LibInspira.setShared(
                                global.datapreferences,
                                global.data.pekerjaan,
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
        private String kode;
        private String nama;
        private String kodesatuan;
        private String satuan;
        private String hargacustomer;
        private String hargamandor;

        public ItemAdapter() {}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getNama() {return nama;}
        public void setNama(String _param) {this.nama = _param;}

        public String getKodeSatuan() {return kodesatuan;}
        public void setKodeSatuan(String _param) {this.kodesatuan = _param;}

        public String getKode() {return kode;}
        public void setKode(String _param) {this.kode = _param;}

        public String getSatuan() {return satuan;}
        public void setSatuan(String _param) {this.satuan = _param;}

        public String getHargaCustomer() {return hargacustomer;}
        public void setHargaCustomer(String _param) {this.hargacustomer = _param;}

        public String getHargaMandor() {return hargamandor;}
        public void setHargaMandor(String _param) {this.hargamandor = _param;}
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

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("salesorder"))
                    {
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_nomor, finalHolder.adapterItem.getNomor());
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_nama, finalHolder.adapterItem.getNama());
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_kode, finalHolder.adapterItem.getKode());
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_satuan, finalHolder.adapterItem.getSatuan());
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_price, finalHolder.adapterItem.getHargaCustomer());
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_qty, "0");
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_disc, "0");
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_fee, "0");
                        LibInspira.BackFragment(getActivity().getSupportFragmentManager());
                    }
                }
            });

            return row;
        }

        private void setupItem(final Holder holder) {
            holder.tvNama.setText(holder.adapterItem.getNama().toUpperCase());
            holder.tvKeterangan.setVisibility(View.VISIBLE);
            holder.tvKeterangan.setText("Kode: " + holder.adapterItem.getKode().toUpperCase());
        }
    }
}
