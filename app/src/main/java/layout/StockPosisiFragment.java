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

import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.inspira.gms.IndexInternal.global;
import static com.inspira.gms.IndexInternal.jsonObject;

//import android.app.Fragment;

public class StockPosisiFragment extends Fragment implements View.OnClickListener{
    protected String actionUrl = "Stock/getStockPosisi/";
    private EditText etSearch;
    private ImageButton ibtnSearch;

    private TextView tvInformation, tvNoData;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;

    public StockPosisiFragment() {
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
        getActivity().setTitle("Posisi Stok");
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

        itemadapter = new ItemListAdapter(getActivity(), R.layout.list_stock, new ArrayList<ItemAdapter>());
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

        //actionUrl = "Stock/getStockPosisi/";
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
                if(!list.get(ctr).getNomorBarang().equals(LibInspira.getShared(global.userpreferences, global.user.nomor_android, "")))
                {
                    itemadapter.add(list.get(ctr));
                    itemadapter.notifyDataSetChanged();
                }
            }
            else
            {
                if(LibInspira.contains(list.get(ctr).getNamaBarang(),etSearch.getText().toString() ))
                {
                    if(!list.get(ctr).getNomorBarang().equals(LibInspira.getShared(global.userpreferences, global.user.nomor_android, "")))
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

        String data = LibInspira.getShared(global.datapreferences, global.data.stockPosisi, "");
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

                    String nomorgudang = parts[0];
                    String namagudang = parts[1];
                    String nomorbarang = parts[2];
                    String namabarang = parts[3];
                    String satuan = parts[4];
                    String qty = parts[5];
                    String m2 = parts[6];

                    if(nomorgudang.equals("")) nomorgudang = "null";
                    if(namagudang.equals("")) namagudang = "null";
                    if(nomorbarang.equals("")) nomorbarang = "null";
                    if(namabarang.equals("")) namabarang = "null";
                    if(satuan.equals("")) satuan = "null";
                    if(qty.equals("")) qty = "0";
                    if(m2.equals("")) m2 = "0";

                    ItemAdapter dataItem = new ItemAdapter();
                    dataItem.setNomorGudang(nomorgudang);
                    dataItem.setNamaGudang(namagudang);
                    dataItem.setNomorBarang(nomorbarang);
                    dataItem.setNamaBarang(namabarang);
                    dataItem.setSatuan(satuan);
                    dataItem.setQty(qty);
                    dataItem.setM2(m2);
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
            String nomorbarang = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.nomorbarang, "");
            String namagudang = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.namagudang, "");
            String kategori = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.kategori, "");
            String bentuk = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.bentuk, "");
            String jenis = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.jenis, "");
            String grade = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.grade, "");
            String surface = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.surface, "");
            String ukuran = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.ukuran, "");
            String tebal = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.tebal, "");
            String motif = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.motif, "");
            String tanggal = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.tanggalakhir, "");
            String nomorcabang = LibInspira.getShared(global.userpreferences, global.user.cabang, "");
            String kodegudang = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.kodegudang, "");
            try {
                jsonObject.put("kodegudang", kodegudang);
                jsonObject.put("nomorbarang", nomorbarang);
                jsonObject.put("namagudang", namagudang);
                jsonObject.put("kategori", kategori);
                jsonObject.put("bentuk", bentuk);
                jsonObject.put("jenis", jenis);
                jsonObject.put("grade", grade);
                jsonObject.put("surface", surface);
                jsonObject.put("ukuran", ukuran);
                jsonObject.put("tebal", tebal);
                jsonObject.put("motif", motif);
                jsonObject.put("tanggal", tanggal);
                jsonObject.put("nomorcabang", nomorcabang);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return LibInspira.executePost(getContext(), urls[0], jsonObject, 60000);
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
                    //for (int i = jsonarray.length() - 1; i >= 0; i--) {
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            String kodegudang = (obj.getString("kodegudang"));
                            String namagudang = (obj.getString("namagudang"));
                            String kodebarang = (obj.getString("kodebarang"));
                            String namabarang = (obj.getString("namabarang"));
                            String satuan = (obj.getString("satuan"));
                            String qty = (obj.getString("qty"));
                            String m2 = (obj.getString("m2"));

                            if(kodegudang.equals("")) kodegudang = "null";
                            if(namagudang.equals("")) namagudang = "null";
                            if(kodebarang.equals("")) kodebarang = "null";
                            if(namabarang.equals("")) namabarang = "null";
                            if(satuan.equals("")) satuan = "null";
                            if(qty.equals("")) qty = "0";
                            if(m2.equals("")) m2 = "0";

                            tempData = tempData + kodegudang + "~" + namagudang + "~" + kodebarang + "~" + namabarang + "~" + satuan + "~" + qty + "~" + m2 + "|";
                        }
                    }

                    if(!tempData.equals(LibInspira.getShared(global.datapreferences, global.data.stockPosisi, "")))
                    {
                        LibInspira.setShared(
                                global.datapreferences,
                                global.data.stockPosisi,
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

        private String nomorgudang;
        private String namagudang;
        private String nomorbarang;
        private String namabarang;
        private String satuan;
        private String qty;
        private String m2;

        public ItemAdapter() {}

        public String getNomorGudang() {return nomorgudang;}
        public void setNomorGudang(String _param) {this.nomorgudang= _param;}

        public String getNamaGudang() {return namagudang;}
        public void setNamaGudang(String _param) {this.namagudang = _param;}

        public String getNomorBarang() {return nomorbarang;}
        public void setNomorBarang(String _param) {this.nomorbarang = _param;}

        public String getNamaBarang() {return namabarang;}
        public void setNamaBarang(String _param) {this.namabarang = _param;}

        public String getSatuan() {return satuan;}
        public void setSatuan(String _param) {this.satuan = _param;}

        public String getQty() {return qty;}
        public void setQty(String _param) {this.qty = _param;}

        public String getM2() {return m2;}
        public void setM2(String _param) {this.m2 = _param;}
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
            TextView tvValue, tvValue1;
            TextView tvKeterangan;
            ImageView ivCall;
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
            holder.tvValue = (TextView)row.findViewById(R.id.tvValue);
            holder.tvValue1 = (TextView)row.findViewById(R.id.tvValue1);
            holder.tvKeterangan = (TextView)row.findViewById(R.id.tvKeterangan);
            holder.ivCall = (ImageView)row.findViewById(R.id.ivCall);

            row.setTag(holder);
            setupItem(holder);

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseKotaFragment());
                }
            });

            final Holder finalHolder = holder;
            holder.tvNama.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //String nomeruser = finalHolder.adapterItem.getNomor();
                }
            });

            return row;
        }

        private void setupItem(final Holder holder) {
            holder.tvNama.setText(holder.adapterItem.getNamaBarang().toUpperCase());
            holder.tvKeterangan.setVisibility(View.VISIBLE);
            holder.tvKeterangan.setText("Gudang: " + holder.adapterItem.getNamaGudang());
            holder.tvValue.setVisibility(View.VISIBLE);
            holder.tvValue.setText("Qty: " + holder.adapterItem.getQty());
            holder.tvValue1.setVisibility(View.VISIBLE);
            holder.tvValue1.setText(LibInspira.delimeter(holder.adapterItem.getM2()) + " " + holder.adapterItem.getSatuan());
        }
    }
}
