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
import android.widget.Toast;

import com.inspira.gms.IndexInternal;
import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.inspira.gms.IndexInternal.global;
import static com.inspira.gms.IndexInternal.jsonObject;

//import android.app.Fragment;

public class ChooseBarangFragment extends Fragment implements View.OnClickListener{
    private EditText etSearch;
    private ImageButton ibtnSearch;

    private TextView tvInformation, tvNoData;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;

    protected String itemType;

    protected String actionUrl = "Master/getBarang/";

    public ChooseBarangFragment() {
        // Required empty public constructor
    }

    public ChooseBarangFragment(String type) {
        itemType = type;
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
        getActivity().setTitle("Barang");
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

        getView().findViewById(R.id.rlSearch).setVisibility(View.VISIBLE);
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

        if(actionUrl.equals("")){
            actionUrl = "Master/getBarang/";
        }
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

        String data = LibInspira.getShared(global.datapreferences, global.data.barang, "");
        String[] pieces = data.trim().split("\\|");

        try
        {
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
                        String nama = parts[1];
                        String namajual = parts[2];
                        String kode = parts[3];
                        String satuan = parts[4];
                        String hargajual = parts[5];
                        String barangtambang = parts[6];
                        String barangimport = parts[7];

                        if(nomor.equals("null")) nomor = "";
                        if(nama.equals("null")) nama = "";
                        if(namajual.equals("null")) namajual = "";
                        if(kode.equals("null")) kode = "";
                        if(satuan.equals("null")) satuan = "";
                        if(hargajual.equals("null")) hargajual = "";
                        if(barangtambang.equals("null")) barangtambang = "";
                        if(barangimport.equals("null")) barangimport = "";

                        if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_jenis, "").equals("") && LibInspira.getShared(global.temppreferences, global.temp.salesorder_import, "").equals(""))
                        {
                            ItemAdapter dataItem = new ItemAdapter();
                            dataItem.setNomor(nomor);
                            dataItem.setNama(nama);
                            dataItem.setNamajual(namajual);
                            dataItem.setKode(kode);
                            dataItem.setSatuan(satuan);
                            dataItem.setHargajual(hargajual);
                            list.add(dataItem);

                            itemadapter.add(dataItem);
                            itemadapter.notifyDataSetChanged();
                        }
                        else if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_jenis, "").equals(barangtambang) && LibInspira.getShared(global.temppreferences, global.temp.salesorder_import, "").equals(barangimport))
                        {
                            ItemAdapter dataItem = new ItemAdapter();
                            dataItem.setNomor(nomor);
                            dataItem.setNama(nama);
                            dataItem.setNamajual(namajual);
                            dataItem.setKode(kode);
                            dataItem.setSatuan(satuan);
                            dataItem.setHargajual(hargajual);
                            list.add(dataItem);

                            itemadapter.add(dataItem);
                            itemadapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            if(itemadapter.getCount()==0)
            {
                tvNoData.setVisibility(View.VISIBLE);
            }
        }
        catch (Exception e)
        {
            tvNoData.setVisibility(View.VISIBLE);
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
                            String namajual = (obj.getString("namajual"));
                            String kode = (obj.getString("kode"));
                            String satuan = (obj.getString("satuan"));
                            String hargajual = (obj.getString("hargajual"));
                            String barangtambang = (obj.getString("tambang"));
                            String barangimport = (obj.getString("import"));

                            if(nomor.equals("")) nomor = "null";
                            if(nama.equals("")) nama = "null";
                            if(namajual.equals("")) namajual = "null";
                            if(kode.equals("")) kode = "null";
                            if(satuan.equals("")) satuan = "null";
                            if(hargajual.equals("")) hargajual = "null";
                            if(barangtambang.equals("")) barangtambang = "null";
                            if(barangimport.equals("")) barangimport = "null";

                            tempData = tempData + nomor + "~" + nama + "~" + namajual + "~" + kode + "~" + satuan + "~" + hargajual + "~" + barangtambang + "~" + barangimport + "|";
                        }
                    }
                    if(!tempData.equals(LibInspira.getShared(global.datapreferences, global.data.barang, "")))
                    {
                        LibInspira.setShared(
                                global.datapreferences,
                                global.data.barang,
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
        private String namajual;
        private String kode;
        private String satuan;
        private String hargajual;
        private String barangtambang;
        private String barangimport;

        public ItemAdapter() {}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getNama() {return nama;}
        public void setNama(String _param) {this.nama = _param;}

        public String getNamajual() {return namajual;}
        public void setNamajual(String _param) {this.namajual = _param;}

        public String getKode() {return kode;}
        public void setKode(String _param) {this.kode = _param;}

        public String getSatuan() {return satuan;}
        public void setSatuan(String _param) {this.satuan = _param;}

        public String getHargajual() {return hargajual;}
        public void setHargajual(String _param) {this.hargajual = _param;}

        public String getBarangtambang() {return barangtambang;}
        public void setBarangtambang(String _param) {this.barangtambang = _param;}

        public String getBarangimport() {return barangimport;}
        public void setBarangimport(String _param) {this.barangimport = _param;}
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
                        if(itemType.equals("itemreal"))
                        {
                            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nomor_real, finalHolder.adapterItem.getNomor());
                            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nama_real, finalHolder.adapterItem.getNama());
                            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_kode_real, finalHolder.adapterItem.getKode());
                            if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_item_nomor, "").equals(""))
                            {
                                LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nomor, finalHolder.adapterItem.getNomor());
                                LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nama, finalHolder.adapterItem.getNama());
                                LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_kode, finalHolder.adapterItem.getKode());
                                LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_satuan, finalHolder.adapterItem.getSatuan());
                                LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_price, finalHolder.adapterItem.getHargajual());
                                LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_qty, "0");
                                LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_disc, "0");
                                LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_fee, "0");
                            }
                        }
                        else if(itemType.equals("item"))
                        {
                            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nomor, finalHolder.adapterItem.getNomor());
                            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nama, finalHolder.adapterItem.getNama());
                            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_kode, finalHolder.adapterItem.getKode());
                            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_satuan, finalHolder.adapterItem.getSatuan());
                            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_price, finalHolder.adapterItem.getHargajual());
                            if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_item_nomor_real, "").equals(""))
                            {
                                LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nomor_real, finalHolder.adapterItem.getNomor());
                                LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nama_real, finalHolder.adapterItem.getNama());
                                LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_kode_real, finalHolder.adapterItem.getKode());
                                LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_qty, "0");
                                LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_disc, "0");
                                LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_fee, "0");
                            }
                        }
                        LibInspira.BackFragment(getActivity().getSupportFragmentManager());
                    }
                    else if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("stockmutasi") || LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("stockkartu"))
                    {
                        LibInspira.setShared(global.stockmonitoringpreferences, global.stock.namabarang, finalHolder.adapterItem.getNama());
                        LibInspira.setShared(global.stockmonitoringpreferences, global.stock.kodebarang, finalHolder.adapterItem.getKode());
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
