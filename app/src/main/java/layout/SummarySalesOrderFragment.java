/******************************************************************************
 Author           : Tonny
 Description      : untuk menampilkan preview salesorder sebelum melakukan insert data
 History          :

 ******************************************************************************/
package layout;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.inspira.gms.IndexInternal.global;
import static com.inspira.gms.IndexInternal.jsonObject;

//import android.app.Fragment;

public class SummarySalesOrderFragment extends Fragment implements View.OnClickListener{
    private TextView tvCustomer, tvBroker, tvValuta, tvDate, tvSubtotal, tvGrandTotal, tvDiscNominal, tvPPNNominal;
    private TextView tvPPN, tvDisc; //added by Tonny @17-Sep-2017  //untuk tampilan pada approval
    private EditText etDisc, etPPN;
    private Button btnSave;
    private InsertingData insertingData;

    public SummarySalesOrderFragment() {
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
        View v = inflater.inflate(R.layout.fragment_summary_sales_order, container, false);
        //tidak ganti title jika dipanggil pada saat approval atau disapproval
        if(!LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("approval") &&
                !LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("disapproval")) {
            getActivity().setTitle("Sales Order Summary");
        }else{  //added by Tonny @17-Sep-2017  //jika dipakai untuk approval, maka layout menggunakan fragment_summary_sales_order_approval untuk view saja
            v = inflater.inflate(R.layout.fragment_summary_sales_order_approval, container, false);
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
        tvDate = (TextView) getView().findViewById(R.id.tvDate);
        tvCustomer = (TextView) getView().findViewById(R.id.tvCustomer);
        tvBroker = (TextView) getView().findViewById(R.id.tvBroker);
        tvValuta = (TextView) getView().findViewById(R.id.tvValuta);
        tvSubtotal = (TextView) getView().findViewById(R.id.tvSubtotal);
        tvGrandTotal = (TextView) getView().findViewById(R.id.tvGrandTotal);
        tvDiscNominal = (TextView) getView().findViewById(R.id.tvDiscNominal);
        tvPPNNominal = (TextView) getView().findViewById(R.id.tvPPNNominal);
        btnSave = (Button) getView().findViewById(R.id.btnSave);

        tvDate.setText(LibInspira.getShared(global.temppreferences, global.temp.salesorder_date, ""));
        tvCustomer.setText(LibInspira.getShared(global.temppreferences, global.temp.salesorder_customer_nama, ""));
        tvBroker.setText(LibInspira.getShared(global.temppreferences, global.temp.salesorder_broker_nama, ""));
        tvValuta.setText(LibInspira.getShared(global.temppreferences, global.temp.salesorder_valuta_nama, ""));

        if(!LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("approval") &&
                !LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("disapproval")){
            etPPN = (EditText) getView().findViewById(R.id.etPPN);
            etDisc = (EditText) getView().findViewById(R.id.etDisc);
            etDisc.setText("0");
            etPPN.setText("0");
            btnSave.setOnClickListener(this);
            etDisc.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    LibInspira.formatNumberEditText(etDisc, this, true, false);
                    tvDiscNominal.setText(LibInspira.delimeter(getNominalDiskon().toString()));
                    tvPPNNominal.setText(LibInspira.delimeter(getNominalPPN().toString()));
                    tvGrandTotal.setText("Rp. " + LibInspira.delimeter(getGrandTotal().toString()));
                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_disc, etDisc.getText().toString().replace(",", ""));
                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_disc_nominal, tvDiscNominal.getText().toString().replace(",", ""));
                }
            });

            etPPN.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    LibInspira.formatNumberEditText(etPPN, this, true, false);
                    tvPPNNominal.setText(LibInspira.delimeter(getNominalPPN().toString()));
                    tvGrandTotal.setText("Rp. " + LibInspira.delimeter(getGrandTotal().toString()));
                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_ppn, etPPN.getText().toString().replace(",", ""));
                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_ppn_nominal, tvPPNNominal.getText().toString().replace(",", ""));
                }
            });

            tvSubtotal.setText(LibInspira.delimeter(getSubtotal().toString()));
            tvGrandTotal.setText("Rp. " + LibInspira.delimeter(getGrandTotal().toString()));

            if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_task, "").equals("nonppn"))
            {
                getView().findViewById(R.id.trPPN).setVisibility(View.GONE);
            }

        }else{  //added by Tonny @17-Sep-2017  jika untuk approval, beberapa property dihilangkan/diganti
            tvPPN = (TextView) getView().findViewById(R.id.tvPPN);
            tvDisc = (TextView) getView().findViewById(R.id.tvDisc);
            btnSave.setVisibility(View.GONE);
            tvSubtotal.setText(LibInspira.delimeter(LibInspira.getShared(global.temppreferences, global.temp.salesorder_subtotal, "")));
            tvGrandTotal.setText("Rp. " + LibInspira.delimeter(LibInspira.getShared(global.temppreferences, global.temp.salesorder_total, "")));
            tvDisc.setText(LibInspira.delimeter(LibInspira.getShared(global.temppreferences, global.temp.salesorder_disc, "")));
            tvDiscNominal.setText(LibInspira.delimeter(LibInspira.getShared(global.temppreferences, global.temp.salesorder_disc_nominal, "")));
            tvPPN.setText(LibInspira.delimeter(LibInspira.getShared(global.temppreferences, global.temp.salesorder_ppn, "")));
            tvPPNNominal.setText(LibInspira.delimeter(LibInspira.getShared(global.temppreferences, global.temp.salesorder_ppn_nominal, "")));
        }


    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id==R.id.btnSave)
        {
            LibInspira.alertBoxYesNo("Save Sales Order", "Do you want to add the current data?", getActivity(), new Runnable() {
                public void run() {
                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_subtotal, getSubtotal().toString());
                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_total, getGrandTotal().toString());
                    sendData();
                }
            }, new Runnable() {
                public void run() {}
            });
        }
        else if(id==R.id.btnBack)
        {
            LibInspira.BackFragment(getActivity().getSupportFragmentManager());
        }
    }

    //untuk mendapatkan nominal diskon
    private Double getNominalDiskon(){
        Double discNominal = 0.0;
        if (tvSubtotal.getText().toString().equals("")){
            tvSubtotal.setText("0.0");
        }else if (etDisc.getText().toString().equals("")){
            etDisc.setText("0.0");
        }else {
            if (!tvSubtotal.getText().toString().equals("")) {
                Double subtotal = Double.parseDouble(tvSubtotal.getText().toString().replace(",", ""));
                Double disc = Double.parseDouble(etDisc.getText().toString().replace(",", ""));
                discNominal = disc * subtotal / 100;
            }
        }
        return discNominal;
    }

    //untuk mendapatkan nominal ppn (setelah diskon)
    private Double getNominalPPN(){
        Double ppnNominal = 0.0;
        if (tvSubtotal.getText().toString().equals("")){
            tvSubtotal.setText("0.0");
        }else if(etPPN.getText().toString().equals("")){
            etPPN.setText("0.0");
        }else if (etDisc.getText().toString().equals("")){
            etDisc.setText("0.0");
        }else {
            Double subtotal = Double.parseDouble(tvSubtotal.getText().toString().replace(",", ""));
            Double ppn = Double.parseDouble(etPPN.getText().toString().replace(",", ""));
            Double disc = Double.parseDouble(etDisc.getText().toString().replace(",", ""));
            Double discNominal = disc * subtotal / 100;
            ppnNominal = ppn * (subtotal - discNominal) / 100;
        }
        return ppnNominal;
    }
    //untuk mendapatkan nominal subtotal dari item dan pekerjaan
    private Double getSubtotal(){
        String data = LibInspira.getShared(global.temppreferences, global.temp.salesorder_item, "");
        Double dblSubtotal = 0.0;
        Double dblItemSubtotal = 0.0;
        Double dblPekerjaanSubtotal = 0.0;
        Double dblFeeSubtotal = 0.0;
        Log.d("data", data);
        String[] pieces = data.trim().split("\\|");
        Log.d("pieces length", Integer.toString(pieces.length));
        if((pieces.length >= 1 && !pieces[0].equals(""))){
            for(int i=0 ; i < pieces.length ; i++){
                if(!pieces[i].equals(""))
                {
                    String[] parts = pieces[i].trim().split("~");
                    String fee = parts[9];
                    String subtotal = parts[11];
                    if(fee.equals("")) fee = "0";
                    if(subtotal.equals("")) subtotal = "0";
                    dblFeeSubtotal = dblFeeSubtotal+ Double.parseDouble(fee);
                    dblItemSubtotal = dblItemSubtotal + Double.parseDouble(subtotal);
                    dblSubtotal = dblSubtotal + Double.parseDouble(subtotal);
                    Log.d("subtotal item [" + i + "]", subtotal);
                }
            }
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_item_subtotal, dblItemSubtotal.toString());
        }

        data = LibInspira.getShared(global.temppreferences, global.temp.salesorder_pekerjaan, "");
        pieces = data.trim().split("\\|");
        if((pieces.length >= 1 && !pieces[0].equals(""))){
            for(int i=0 ; i < pieces.length ; i++){
                if(!pieces[i].equals(""))
                {
                    String[] parts = pieces[i].trim().split("~");
                    String fee = parts[6];
                    String subtotal = parts[8];
                    if(fee.equals("")) fee = "0";
                    if(subtotal.equals("")) subtotal = "0";
                    LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_subtotal, subtotal);
                    dblFeeSubtotal = dblFeeSubtotal+ Double.parseDouble(fee);
                    dblPekerjaanSubtotal = dblPekerjaanSubtotal + Double.parseDouble(subtotal);
                    dblSubtotal = dblSubtotal + Double.parseDouble(subtotal);
                }
            }
        }
        else
        {
            LibInspira.setShared(global.temppreferences, global.temp.salesorder_pekerjaan_subtotal, "0");
        }
        LibInspira.setShared(global.temppreferences, global.temp.salesorder_subtotal_fee, dblFeeSubtotal.toString());
        return dblSubtotal;
    }

    private Double getGrandTotal(){
        Double grandtotal = getSubtotal() - getNominalDiskon() + getNominalPPN();
        return grandtotal;
    }


    //added by Tonny @02-Sep-2017
    //untuk menjalankan perintah send data ke web service
    private void sendData(){
        String actionUrl = "Order/insertNewOrderJual/";
        insertingData = new InsertingData();
        insertingData.execute(actionUrl);
    }

    //added by Tonny @04-Sep-2017
    //class yang digunakan untuk insert data
    private class InsertingData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
                //---------------------------------------------HEADER-----------------------------------------------------//
            try {
                jsonObject.put("nomorcustomer", LibInspira.getShared(global.temppreferences, global.temp.salesorder_customer_nomor, ""));
                jsonObject.put("kodecustomer", LibInspira.getShared(global.temppreferences, global.temp.salesorder_customer_kode, ""));
                jsonObject.put("nomorbroker", LibInspira.getShared(global.temppreferences, global.temp.salesorder_broker_nomor, ""));
                jsonObject.put("kodebroker", LibInspira.getShared(global.temppreferences, global.temp.salesorder_broker_kode, ""));
                jsonObject.put("nomorsales", LibInspira.getShared(global.userpreferences, global.user.nomor_sales, ""));
                jsonObject.put("kodesales", LibInspira.getShared(global.userpreferences, global.user.kode_sales, ""));
                jsonObject.put("subtotal", LibInspira.getShared(global.temppreferences, global.temp.salesorder_subtotal, ""));
                jsonObject.put("subtotaljasa", LibInspira.getShared(global.temppreferences, global.temp.salesorder_pekerjaan_subtotal, ""));
                jsonObject.put("subtotalbiaya", LibInspira.getShared(global.temppreferences, global.temp.salesorder_subtotal_fee, ""));
                jsonObject.put("disc", LibInspira.getShared(global.temppreferences, global.temp.salesorder_disc, "0"));
                jsonObject.put("discnominal", LibInspira.getShared(global.temppreferences, global.temp.salesorder_disc_nominal, "0"));
                jsonObject.put("dpp", LibInspira.getShared(global.temppreferences, global.temp.salesorder_total, ""));
                jsonObject.put("ppn", LibInspira.getShared(global.temppreferences, global.temp.salesorder_ppn, "0"));
                jsonObject.put("ppnnominal", LibInspira.getShared(global.temppreferences, global.temp.salesorder_ppn_nominal, "0"));
                jsonObject.put("total", LibInspira.getShared(global.temppreferences, global.temp.salesorder_total, "0"));
                jsonObject.put("totalrp", Double.toString(getGrandTotal() * Double.parseDouble(LibInspira.getShared(global.temppreferences, global.temp.salesorder_valuta_kurs, ""))));
                jsonObject.put("pembuat", LibInspira.getShared(global.userpreferences, global.user.nama, ""));
                jsonObject.put("nomorcabang", LibInspira.getShared(global.userpreferences, global.user.cabang, ""));
                jsonObject.put("cabang", LibInspira.getShared(global.temppreferences, global.user.namacabang, ""));
                jsonObject.put("valuta", LibInspira.getShared(global.temppreferences, global.temp.salesorder_valuta_nama, ""));
                jsonObject.put("kurs", LibInspira.getShared(global.temppreferences, global.temp.salesorder_valuta_kurs, ""));
                jsonObject.put("jenispenjualan", "Material");
                jsonObject.put("isbarangimport", LibInspira.getShared(global.temppreferences, global.temp.salesorder_import, ""));
                jsonObject.put("isppn", LibInspira.getShared(global.temppreferences, global.temp.salesorder_isPPN, ""));
                if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_proyek, "").equals("proyek"))
                {
                    jsonObject.put("proyek", 1);
                }
                else if(LibInspira.getShared(global.temppreferences, global.temp.salesorder_type_proyek, "").equals("nonproyek"))
                {
                    jsonObject.put("proyek", 0);
                }
                jsonObject.put("user", LibInspira.getShared(global.userpreferences, global.user.nomor, ""));


                //-------------------------------------------------------------------------------------------------------//
                //---------------------------------------------DETAIL----------------------------------------------------//
                jsonObject.put("dataitemdetail", LibInspira.getShared(global.temppreferences, global.temp.salesorder_item, ""));  //mengirimkan data item
                jsonObject.put("datapekerjaandetail", LibInspira.getShared(global.temppreferences, global.temp.salesorder_pekerjaan, ""));  //mengirimkan data pekerjaan
                Log.d("detailitemdetail", LibInspira.getShared(global.temppreferences, global.temp.salesorder_item, ""));
                Log.d("detailpekerjaandetail", LibInspira.getShared(global.temppreferences, global.temp.salesorder_pekerjaan, ""));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("resultQuery", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            LibInspira.hideLoading();
                            LibInspira.ShowShortToast(getContext(), "Data has been successfully added");
                            LibInspira.clearShared(global.temppreferences); //hapus cache jika data berhasil ditambahkan
                            LibInspira.BackFragmentCount(getFragmentManager(), 6);  //kembali ke menu depan sales order
                        }
                        else
                        {
                            LibInspira.ShowShortToast(getContext(), "Adding new data failed");
                            LibInspira.hideLoading();
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.ShowShortToast(getContext(), "Adding new data failed");
                LibInspira.hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Inserting Data", "Loading");
            //tvInformation.setVisibility(View.VISIBLE);
        }
    }
}
