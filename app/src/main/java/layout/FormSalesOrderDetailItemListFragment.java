/******************************************************************************
    Author           : ADI
    Description      : untuk menampilkan detail item dalam bentuk list
    History          :

******************************************************************************/
package layout;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

public class FormSalesOrderDetailItemListFragment extends Fragment implements View.OnClickListener{
    private ListView lvSearch;
    protected ItemListAdapter itemadapter;
    protected ArrayList<ItemAdapter> list;
    protected String jenisDetail;  //added by Tonny @16-Sep-2017
    private Button btnBack, btnNext;
    private FloatingActionButton fab;
    protected String strData;

    public FormSalesOrderDetailItemListFragment() {
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
        View v = inflater.inflate(R.layout.fragment_sales_order_detail_item_list, container, false);
        if(!LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("approval") &&
                !LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("disapproval")){
            getActivity().setTitle("Sales Order - List Item");
        }
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

        itemadapter = new ItemListAdapter(getActivity(), R.layout.list_item_salesorder, new ArrayList<ItemAdapter>());
        itemadapter.clear();
        lvSearch = (ListView) getView().findViewById(R.id.lvChoose);
        lvSearch.setAdapter(itemadapter);

        fab = (FloatingActionButton) getView().findViewById(R.id.fab);
        btnBack = (Button) getView().findViewById(R.id.btnBack);
        btnNext = (Button) getView().findViewById(R.id.btnNext);

        //added by Tonny @16-Sep-2017 jika approval atau disapproval, maka hide btnBack dan btnNext
        if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("approval") ||
                LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("disapproval")){
            btnBack.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);
            fab.setVisibility(View.GONE);
        }else{
            fab.setOnClickListener(this);
            btnBack.setOnClickListener(this);
            btnNext.setOnClickListener(this);
        }

        //refreshList();
        getStrData();
        Log.d("onActivityCreated: ", "list item created");
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
//            search();
        }
        else if(id==R.id.fab)
        {
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_index, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nomor, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nama, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_kode, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nomor_real, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nama_real, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_kode_real, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_satuan, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_price, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_qty, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_fee, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_disc, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_notes, "");

            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new FormSalesOrderDetailItemFragment());
        }
        else if(id==R.id.btnBack)
        {
            LibInspira.BackFragment(getActivity().getSupportFragmentManager());
        }
        else if(id==R.id.btnNext)
        {
            if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_item, "").equals("")){
                LibInspira.ShowShortToast(getContext(), "No item selected. Please add item to proceed");
            }else{
                LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new FormSalesOrderDetailJasaListFragment());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        strData = LibInspira.getShared(global.temppreferences, global.temp.salesorder_item, "");
    }

     protected void refreshList()
    {
        itemadapter.clear();
        list.clear();
        //getStrData();  //added by Tonny @07-Sep-2017
        if (strData.equals("")){
            return;
        }
        String data = strData;
        String[] pieces = data.trim().split("\\|");
        if((pieces.length==1 && pieces[0].equals("")))
        {
            //do nothing
        }
        else
        {
            for(int i=0 ; i < pieces.length ; i++){
                Log.d("Index", data);
                if(!pieces[i].equals(""))
                {
                    String[] parts = pieces[i].trim().split("\\~");
                    Log.d("pieces: ", pieces[i]);
                    try {
                        String nomor = parts[0];
                        String kode = parts[1];
                        String nama = parts[2];
                        String nomorreal = parts[3];
                        String kodereal = parts[4];
                        String namareal = parts[5];
                        String satuan = parts[6];
                        String price = parts[7];
                        String qty = parts[8];
                        String fee = parts[9];
                        String disc = parts[10];
                        //String subtotal = parts[11];
                        String notes = parts[12];

                        if(nomor.equals("null")) nomor = "";
                        if(kode.equals("null")) kode = "";
                        if(nama.equals("null")) nama = "-";
                        if(nomorreal.equals("null")) nomor = "";
                        if(kodereal.equals("null")) kode = "";
                        if(namareal.equals("null")) nama = "-";
                        if(satuan.equals("null")) satuan = "";
                        if(price.equals("null")) price = "";
                        if(qty.equals("null")) qty = "";
                        if(fee.equals("null")) fee = "";
                        if(disc.equals("null")) disc = "";
                        if(notes.equals("null")) notes = "";

                        ItemAdapter dataItem = new ItemAdapter();
                        dataItem.setIndex(i);
                        dataItem.setNomor(nomor);
                        dataItem.setNama(nama);
                        dataItem.setKode(kode);
                        dataItem.setNomorReal(nomorreal);
                        dataItem.setNamaReal(namareal);
                        dataItem.setKodeReal(kodereal);
                        dataItem.setSatuan(satuan);
                        dataItem.setPrice(price);
                        dataItem.setQty(qty);
                        dataItem.setFee(fee);
                        dataItem.setDisc(disc);
                        dataItem.setNotes(notes);

                        list.add(dataItem);

                        itemadapter.add(dataItem);
                        itemadapter.notifyDataSetChanged();
                    }catch (Exception e){
                        e.printStackTrace();
                        LibInspira.ShowShortToast(getContext(), "The current data is invalid. Please add new data.");
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item, "");
                        strData = "";
                        refreshList();
                    }
                }
            }
        }
    }

    protected void deleteSelectedItem(int _index){
        String newdata = "";
        getStrData();
        if(!strData.equals(""))
        {
            String[] pieces = strData.trim().split("\\|");
            for(int i=0 ; i < pieces.length ; i++){
//                String string = pieces[i];
//                String[] parts = string.trim().split("\\~");
                if(i != _index)
                {
                    newdata = newdata + pieces[i] + "|";
                }
            }
        }
        setStrData(newdata);
        refreshList();
    }



    protected void getStrData(){
        strData = LibInspira.getShared(global.temppreferences, global.temp.salesorder_item, "");
        //added by Tonny @16-Sep-2017 jika approval atau disapproval, maka hide ibtnDelete
        if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("approval") ||
                LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("disapproval")){
            String ActionUrl = "Order/getSalesOrderItemList/";
            if(jenisDetail.equals("jasa")){
                ActionUrl = "Order/getSalesOrderJasaList/";
            }
            GetList getList = new GetList();
            getList.execute(ActionUrl);
        }else{
            refreshList();
        }
    }

    protected class GetList extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
            try {
                //"nomor" == nomor header
                jsonObject.put("nomor", LibInspira.getShared(global.temppreferences, global.temp.salesorder_selected_list_nomor, ""));
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
                            //nomorbarang~kodebarang~namabarang~satuan~price~qty~fee~disc~subtotal~notes
                            String nomorbarang = (obj.getString("nomorbarang"));
                            String kodebarang = (obj.getString("kodebarang"));
                            String namabarang = (obj.getString("namabarang"));
                            String satuan = (obj.getString("satuan"));
                            String price = (obj.getString("price"));
                            String qty = (obj.getString("qty"));
                            String fee = (obj.getString("fee"));
                            String disc = (obj.getString("disc"));
                            String subtotal = (obj.getString("subtotal"));
                            String notes = (obj.getString("notes"));

                            if(nomorbarang.equals("")) nomorbarang = "null";
                            if(kodebarang.equals("")) kodebarang = "null";
                            if(namabarang.equals("")) namabarang = "null";
                            if(satuan.equals("")) satuan = "null";
                            if(price.equals("")) price = "null";
                            if(qty.equals("")) qty = "null";
                            if(fee.equals("")) fee = "null";
                            if(disc.equals("")) disc = "null";
                            if(subtotal.equals("")) subtotal= "null";
                            if(notes.equals("")) notes = "null";

                            tempData = tempData + nomorbarang + "~" + kodebarang + "~" + namabarang + "~"
                                    + satuan + "~" + price + "~" + qty + "~" + fee + "~" + disc + "~" + subtotal + "~" + notes + "|";
                        }
                    }
                    if(!tempData.equals(LibInspira.getShared(global.temppreferences, global.temp.salesorder_item, "")))
                    {
                        //added by Tonny @16-Sep-2017
                        setStrData(tempData);
                        refreshList();
                    }
                }
                //tvInformation.animate().translationYBy(-80);
            }
            catch(Exception e)
            {
                e.printStackTrace();
                //tvInformation.animate().translationYBy(-80);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //tvInformation.setVisibility(View.VISIBLE);
        }
    }

    protected void setStrData(String newdata){
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item, newdata);
        strData = newdata;
    }

    protected void setEditData(String index, String nomor, String nama, String kode, String nomorreal, String namareal, String kodereal, String satuan, String price, String qty, String fee, String disc, String notes){
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_index, index);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nomor, nomor);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nama, nama);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_kode, kode);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nomor_real, nomorreal);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_nama_real, namareal);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_kode_real, kodereal);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_satuan, satuan);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_price, price);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_qty, qty);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_fee, fee);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_disc, disc);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_notes, notes);
        LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new FormSalesOrderDetailItemFragment());
    }

    public class ItemAdapter {

        private int index;
        private String nomor;
        private String nama;
        private String kode;
        private String nomorreal;
        private String namareal;
        private String kodereal;
        private String satuan;
        private String price;
        private String qty;
        private String fee;
        private String disc;
        private String notes;

        public ItemAdapter() {}

        public int getIndex() {return index;}
        public void setIndex(int _param) {this.index = _param;}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getNama() {return nama;}
        public void setNama(String _param) {this.nama = _param;}

        public String getKode() {return kode;}
        public void setKode(String _param) {this.kode = _param;}

        public String getNomorReal() {return nomorreal;}
        public void setNomorReal(String _param) {this.nomorreal = _param;}

        public String getNamaReal() {return namareal;}
        public void setNamaReal(String _param) {this.namareal = _param;}

        public String getKodeReal() {return kodereal;}
        public void setKodeReal(String _param) {this.kodereal = _param;}

        public String getSatuan() {return satuan;}
        public void setSatuan(String _param) {this.satuan = _param;}

        public String getPrice() {return price;}
        public void setPrice(String _param) {this.price = _param;}

        public String getQty() {return qty;}
        public void setQty(String _param) {this.qty = _param;}

        public String getFee() {return fee;}
        public void setFee(String _param) {this.fee = _param;}

        public String getDisc() {return disc;}
        public void setDisc(String _param) {this.disc = _param;}

        public String getNotes() {return notes;}
        public void setNotes(String _param) {this.notes = _param;}
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
            TextView tvKode, tvNama, tvSatuan, tvPrice, tvQty, tvFee, tvDisc, tvNotes;
            ImageButton ibtnDelete;
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

            holder.tvNama = (TextView)row.findViewById(R.id.tvNama);
            holder.tvKode = (TextView)row.findViewById(R.id.tvKode);
            holder.tvSatuan = (TextView)row.findViewById(R.id.tvSatuan);
            holder.tvPrice = (TextView)row.findViewById(R.id.tvPrice);
            holder.tvQty = (TextView)row.findViewById(R.id.tvQty);
            holder.tvFee = (TextView)row.findViewById(R.id.tvFee);
            holder.tvDisc = (TextView)row.findViewById(R.id.tvDisc);
            holder.tvNotes = (TextView)row.findViewById(R.id.tvNotes);
            holder.ibtnDelete = (ImageButton) row.findViewById(R.id.ibtnDelete);

            row.setTag(holder);
            setupItem(holder);

            //added by Tonny @16-Sep-2017 jika approval atau disapproval, maka hide ibtnDelete dan hilangkan listener click
            if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("approval") ||
                    LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("disapproval")){
                holder.ibtnDelete.setVisibility(View.GONE);
            }else{
                final Holder finalHolder = holder;
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setEditData(String.valueOf(finalHolder.adapterItem.getIndex()),
                                finalHolder.adapterItem.getNomor(),
                                finalHolder.adapterItem.getNama(),
                                finalHolder.adapterItem.getKode(),
                                finalHolder.adapterItem.getNomorReal(),
                                finalHolder.adapterItem.getNamaReal(),
                                finalHolder.adapterItem.getKodeReal(),
                                finalHolder.adapterItem.getSatuan(),
                                finalHolder.adapterItem.getPrice(),
                                finalHolder.adapterItem.getQty(),
                                finalHolder.adapterItem.getFee(),
                                finalHolder.adapterItem.getDisc(),
                                finalHolder.adapterItem.getNotes()
                        );
                    }
                });
                holder.ibtnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteSelectedItem(finalHolder.adapterItem.getIndex());
                    }
                });
            }
            return row;
        }

        private void setupItem(final Holder holder) {
            holder.tvNama.setText(holder.adapterItem.getNama().toUpperCase());
            holder.tvKode.setText(holder.adapterItem.getKode().toUpperCase());
            //holder.tvSatuan.setText(holder.adapterItem.getSatuan().toUpperCase());
            holder.tvPrice.setText(LibInspira.delimeter(holder.adapterItem.getPrice().toUpperCase()));
            holder.tvQty.setText(LibInspira.delimeter(holder.adapterItem.getQty().toUpperCase()) + " " + holder.adapterItem.getSatuan().toUpperCase());
            holder.tvFee.setText(LibInspira.delimeter(holder.adapterItem.getFee().toUpperCase()));
            holder.tvDisc.setText(LibInspira.delimeter(holder.adapterItem.getDisc().toUpperCase()));
            holder.tvNotes.setText(holder.adapterItem.getNotes());
        }
    }
}
