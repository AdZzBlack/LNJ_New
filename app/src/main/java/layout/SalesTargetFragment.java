package layout;


import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.inspira.gms.IndexInternal.global;


/**
 * A simple {@link Fragment} subclass.
 */
public class SalesTargetFragment extends Fragment implements View.OnClickListener{
    EditText edtTarget;
    Spinner spBulan, spTahun;
    TextView tvSales;
    Button btnAdd, btnSet;
    ListView lvGridSales;
    private String nomorsales = "";
    private Integer batasTahun = 5;
    private ItemListAdapter itemListAdapter;
    public SalesTargetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sales_target, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivity().setTitle("New Sales Target");
        spBulan = (Spinner) getView().findViewById(R.id.spBulan);
        spTahun = (Spinner) getView().findViewById(R.id.spTahun);
        edtTarget = (EditText) getView().findViewById(R.id.edtTarget);
        tvSales = (TextView) getView().findViewById(R.id.tvSales);
        btnAdd = (Button) getView().findViewById(R.id.btnAdd);
        btnSet = (Button) getView().findViewById(R.id.btnSet);
        lvGridSales = (ListView) getView().findViewById(R.id.lvGridSales);

        nomorsales = LibInspira.getShared(global.sharedpreferences, global.shared.nomorsales, "");
        tvSales.setText(LibInspira.getShared(global.sharedpreferences, global.shared.namasales, ""));
        if (itemListAdapter == null){
            itemListAdapter = new ItemListAdapter(getActivity(), R.layout.list_item, new ArrayList<ItemAdapter>());
            itemListAdapter.clear();
        }
        lvGridSales.setAdapter(itemListAdapter);

        ArrayAdapter<String> adapter;
        List<String> list;


        Integer year = Calendar.getInstance().get(Calendar.YEAR);
        list = new ArrayList<String>();

        //added by Tonny @06-Aug-2017  untuk mengisi spTahun sebanyak batasTahun dari tahun sekarang
        for (int i = 0; i < batasTahun; i++){
            list.add(year.toString());
            year ++;
        }
        adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTahun.setAdapter(adapter);

        btnAdd.setOnClickListener(this);
        btnSet.setOnClickListener(this);
        tvSales.setOnClickListener(this);
        lvGridSales.setAdapter(itemListAdapter);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        LibInspira.setShared(global.sharedpreferences, global.shared.position, "sales target");
        if(id == R.id.tvSales){
            //tampilkan browse sales
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseSalesmanFragment());
        }else if (id == R.id.btnAdd){
            if(
            edtTarget.getText().toString().equals("") ||
            tvSales.getText().toString().equals(""))
            {
                //jika data kosong, tampilkan pesan error
                LibInspira.ShowShortToast(getContext(), "Please fill in all data");
            }else{
                Boolean same = false;
                ItemAdapter itemAdapter = new ItemAdapter();
                itemAdapter.setNomorSales(nomorsales.toString());
                itemAdapter.setBulan(spBulan.getSelectedItem().toString());
                itemAdapter.setTahun(spTahun.getSelectedItem().toString());
                itemAdapter.setTarget(edtTarget.getText().toString());
                itemAdapter.setNama(tvSales.getText().toString());
                if (itemListAdapter.getCount() > 0) {
                    for (int i = 0; i < itemListAdapter.getCount(); i++) {
                        if (
                                itemListAdapter.getItem(i).getBulan().equals(itemAdapter.getBulan()) &&
                                itemListAdapter.getItem(i).getTahun().equals(itemAdapter.getTahun()) &&
                                itemListAdapter.getItem(i).getNama().equals(itemAdapter.getNama()))
                        {
                            same = true;
                        }
                    }
                }
                if (same) {
                    Toast.makeText(getContext(), "Sales already targeted", Toast.LENGTH_LONG).show();
                }else {
                    //tambahkan data ke lvGridSales
//                    ItemAdapter itemAdapter = new ItemAdapter();
//                    itemAdapter.setBulan(spBulan.getSelectedItem().toString());
//                    itemAdapter.setTahun(spTahun.getSelectedItem().toString());
//                    itemAdapter.setTarget(edtTarget.getText().toString());
//                    itemAdapter.setNama(tvSales.getText().toString());
                    itemListAdapter.add(itemAdapter);
                    itemListAdapter.notifyDataSetChanged();
                }
            }
        }else if (id == R.id.btnSet){
            if(lvGridSales.getCount() > 0) {
                //jika listview terdapat item, maka simpan data
                LibInspira.ShowShortToast(getContext(), "Adding new sales target...");
                String actionUrl = "Sales/setTarget";
                new setTarget().execute(actionUrl);
            }else{
                //jika listview kosong, tampilkan pesan error
                LibInspira.ShowShortToast(getContext(), "There is no data to save");
            }
        }
    }

    /******************************************************************************
        Procedure : setTarget
        Author    : Tonny
        Date      : 07-Aug-2017
        Function  : Untuk melakukan insert pada whtarget_mobile
    ******************************************************************************/
    private class setTarget extends AsyncTask<String, Void, String> {
        JSONObject jsonObject;
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();

                if (itemListAdapter.getCount() > 0) {
                    String rawdata = "";
                    for (int i = 0; i < itemListAdapter.getCount(); i++) {
                        rawdata = itemListAdapter.getItem(i).getNomorSales() + "~" +
                        itemListAdapter.getItem(i).getBulan() + "~" +
                        itemListAdapter.getItem(i).getTahun() + "~" +
                        itemListAdapter.getItem(i).getTarget() + "~" + "|";
                        Log.d("rawdata", rawdata);
                    }
                    jsonObject.put("rawdata", rawdata);
                }
                //jsonObject.put("rawdata", LibInspira.getShared(global.userpreferences,global.user.hash,""));
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("tes", result);
            try {
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        String success = obj.getString("success");
                        if(success.equals("true")){
                            LibInspira.ShowShortToast(getContext(), "Target has been successfully saved");
                            LibInspira.hideLoading();
                            itemListAdapter.clear();
                            LibInspira.setShared(global.sharedpreferences, global.shared.nomorsales, "");
                            LibInspira.setShared(global.sharedpreferences, global.shared.namasales, "");

                            LibInspira.BackFragment(getFragmentManager()); // back to ChoosePeriode
                        }
                        else
                        {
                            LibInspira.ShowShortToast(getContext(), "Saving data failed");
                            LibInspira.hideLoading();
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                Toast.makeText(getContext(), "Saving target failed", Toast.LENGTH_LONG).show();
                LibInspira.hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Adding data", "Loading");
        }
    }
    
    //get set parameter sales
    public class ItemAdapter {

        private String bulan;
        private String tahun;
        private String target;
        private String nama;
        private String nomorsales;

        public ItemAdapter() {}

        public String getNomorSales() {return nomorsales;}
        public void setNomorSales(String _param) {this.nomorsales = _param;}

        public String getBulan() {return bulan;}
        public void setBulan(String _param) {this.bulan = _param;}

        public String getTahun() {return tahun;}
        public void setTahun(String _param) {this.tahun = _param;}

        public String getTarget() {return target;}
        public void setTarget(String _param) {this.target = _param;}

        public String getNama() {return nama;}
        public void setNama(String _param) {this.nama= _param;}
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
            TextView tvName, tvPeriode, tvTarget;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ItemListAdapter.Holder holder = null;

            if(row==null)
            {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
            }

            holder = new ItemListAdapter.Holder();
            holder.adapterItem = items.get(position);

            holder.tvName = (TextView)row.findViewById(R.id.tvName);
            holder.tvPeriode = (TextView)row.findViewById(R.id.tvKeterangan);
            holder.tvTarget = (TextView)row.findViewById(R.id.tvKeterangan1);
            holder.tvPeriode.setVisibility(View.VISIBLE);
            holder.tvTarget.setVisibility(View.VISIBLE);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    LibInspira.alertbox("Delete", "Delete " + finalHolder.adapterItem.getNama() + " ?", getActivity(), new Runnable(){
                        public void run() {
                            itemListAdapter.remove(finalHolder.adapterItem);
                        }
                    }, new Runnable(){
                        public void run() {
                            //jika cancel, maka tidak melakukan apa2
                        }
                    });
                    return true;
                }
                });
            return row;
        }

        private void setupItem(final SalesTargetFragment.ItemListAdapter.Holder holder) {
            holder.tvName.setText(holder.adapterItem.getNama());
            holder.tvPeriode.setText("Periode: " + LibInspira.getMonth(holder.adapterItem.getBulan()) + " " + holder.adapterItem.getTahun());
            holder.tvTarget.setText("Target: Rp. " + LibInspira.delimeter(holder.adapterItem.getTarget(), false));
        }
    }
}
