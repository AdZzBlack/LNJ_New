/******************************************************************************
    Author           : Tonny
    Description      : untuk menampilkan detail pekerjaan dalam bentuk list
    History          :

******************************************************************************/
package layout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import java.util.ArrayList;

import static com.inspira.gms.IndexInternal.global;

//import android.app.Fragment;

public class FormSalesOrderDetailJasaListFragment extends FormSalesOrderDetailItemListFragment implements View.OnClickListener{

    public FormSalesOrderDetailJasaListFragment() {
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
        View v = inflater.inflate(R.layout.fragment_sales_order_detail_jasa_list, container, false);
        if(!LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("approval") &&
                !LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("disapproval")){
            getActivity().setTitle("Sales Order - List Pekerjaan");
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
        Log.d("onActivityCreated: ", "list jasa created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    protected void refreshList(){
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
                        String satuan = parts[3];
                        String price = parts[4];
                        String qty = parts[5];
                        String fee = parts[6];
                        String disc = parts[7];
                        //String subtotal = parts[8];
                        String notes = parts[9];

                        if(nomor.equals("null")) nomor = "";
                        if(kode.equals("null")) kode = "";
                        if(nama.equals("null")) nama = "-";
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
                        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan, "");
                        strData = "";
                        refreshList();
                    }
                }
            }
        }
    }

    @Override
    protected void getStrData(){
        strData = LibInspira.getShared(global.temppreferences, global.temp.salesorder_pekerjaan, "");
        //added by Tonny @16-Sep-2017 jika approval atau disapproval, maka hide ibtnDelete
        if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("approval") ||
                LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("disapproval")){
            String ActionUrl = "Order/getSalesOrderPekerjaanList/";
            GetList getList = new GetList();
            getList.execute(ActionUrl);
        }else{
            refreshList();
        }
    }
    @Override
    protected void setStrData(String newdata){
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan, newdata);
        strData = newdata;
    }

    @Override
    protected void setEditData(String index, String nomor, String nama, String kode, String nomorreal, String namareal, String kodereal, String satuan, String price, String qty, String fee, String disc, String notes){
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_index, index);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_nomor, nomor);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_nama, nama);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_kode, kode);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_satuan, satuan);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_price, price);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_qty, qty);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_fee, fee);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_disc, disc);
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_notes, notes);

        LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new FormSalesOrderDetailJasaFragment());
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id==R.id.fab)
        {
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_index, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_nomor, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_nama, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_kode, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_satuan, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_price, "0");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_qty, "");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_fee, "0");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_disc, "0");
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_notes, "");
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new FormSalesOrderDetailJasaFragment());
        }
        else if(id==R.id.btnBack)
        {
            LibInspira.BackFragment(getActivity().getSupportFragmentManager());
        }
        else if(id==R.id.btnNext)
        {
            //pengecekan jika user tidak memilih item dan jasa namun ingin melanjutkan, maka tampilkan pesan error
            if (LibInspira.getShared(global.temppreferences, global.temp.salesorder_item, "").equals("") &&
                LibInspira.getShared(global.temppreferences, global.temp.salesorder_pekerjaan, "").equals("")){
                LibInspira.ShowLongToast(getContext(), "There is no item and pekerjaan to proceed. Please choose item or pekerjaan first.");
            }else {
                LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new SummarySalesOrderFragment());
            }
        }
    }
}
