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
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class SalesOmzetFragment extends Fragment implements View.OnClickListener{
    private TextView tvInformation, tvNoData, tvTotalOmzet, tvPeriode;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;
    private CheckSalesOmzet checkSalesOmzet;
    private CheckTotalOmzet checkTotalOmzet;


    public SalesOmzetFragment() {
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
        View v = inflater.inflate(R.layout.fragment_sales_omzet, container, false);
        getActivity().setTitle("Sales Omzet");
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
        tvInformation = (TextView) getView().findViewById(R.id.tvInformation);
        tvInformation.setVisibility(View.VISIBLE);
        tvPeriode = (TextView) getView().findViewById(R.id.tvPeriode);
        String filterDate = LibInspira.getShared(global.omzetpreferences, global.omzet.enddate, "");
        if(LibInspira.getShared(global.omzetpreferences, global.omzet.bulantahun, "").equals("bulan")){
            tvPeriode.setText(LibInspira.getFirstDateInSpecificMonth(filterDate, "dd/MMM/yyyy"));
        }else{
            tvPeriode.setText(LibInspira.getFirstDateInSpecificYear(filterDate, "dd/MMM/yyyy"));
        }
        if (LibInspira.FormatDateBasedOnInspiraDateFormat(filterDate, "dd/MMM/yyyy").equals(tvPeriode.getText())){
            tvPeriode.setText(LibInspira.FormatDateBasedOnInspiraDateFormat(filterDate, "dd/MMM/yyyy"));
        }else{
            tvPeriode.setText(tvPeriode.getText() + " - " + LibInspira.FormatDateBasedOnInspiraDateFormat(filterDate, "dd/MMM/yyyy"));
        }
        tvNoData = (TextView) getView().findViewById(R.id.tvNoData);
        tvTotalOmzet = (TextView) getView().findViewById(R.id.tvTotalOmzet);
        itemadapter = new ItemListAdapter(getActivity(), R.layout.list_stock, new ArrayList<ItemAdapter>());
        itemadapter.clear();
        lvSearch = (ListView) getView().findViewById(R.id.lvChoose);
        lvSearch.setAdapter(itemadapter);
        final SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                refreshPage();
                (new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }
        });

        refreshList();

        String actionUrl = "Sales/getSalesOmzet/";
        checkSalesOmzet = new CheckSalesOmzet();
        checkSalesOmzet.execute( actionUrl );

        actionUrl = "Sales/getTotalOmzet/";
        checkTotalOmzet = new CheckTotalOmzet();
        checkTotalOmzet.execute( actionUrl );

    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        //int id = view.getId();
    }

    private void refreshPage(){
        refreshList();
        checkTotalOmzet.cancel(true);
        checkSalesOmzet.cancel(true);

        String actionUrl = "Sales/getSalesOmzet/";
        checkSalesOmzet = new CheckSalesOmzet();
        checkSalesOmzet.execute( actionUrl );

        actionUrl = "Sales/getTotalOmzet/";
        checkTotalOmzet = new CheckTotalOmzet();
        checkTotalOmzet.execute( actionUrl );
    }

    private void refreshList()
    {
        itemadapter.clear();
        list.clear();

        String data = LibInspira.getShared(global.datapreferences, global.data.salesmanomzet, "");
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

                    String nomorsales = parts[0];
                    String namasales = parts[1];
                    String omzet = parts[2];
                    String tanggal = parts[3];
                    String namacustomer = parts[4];

                    if(nomorsales.equals("null")) nomorsales = "";
                    if(namasales.equals("null")) namasales = "";
                    if(omzet.equals("null")) omzet= "-";
                    if(tanggal.equals("null")) tanggal = "";

                    ItemAdapter dataItem = new ItemAdapter();
                    dataItem.setNomorSales(nomorsales);
                    dataItem.setNamaSales(namasales);
                    dataItem.setOmzet(omzet);
                    dataItem.setTanggal(tanggal);
                    dataItem.setNamaCust(namacustomer);
                    list.add(dataItem);

                    if(!dataItem.getNomorSales().equals(LibInspira.getShared(global.userpreferences, global.user.nomor_android, "")))
                    {
                        itemadapter.add(dataItem);
                        itemadapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    private class CheckSalesOmzet extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("nomorsales", LibInspira.getShared(global.omzetpreferences, global.omzet.nomorsales, ""));
                jsonObject.put("enddate", LibInspira.getShared(global.omzetpreferences, global.omzet.enddate, ""));
                jsonObject.put("bulantahun", LibInspira.getShared(global.omzetpreferences, global.omzet.bulantahun, ""));
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
                            String nomorsales = (obj.getString("nomorsales"));
                            String namasales = (obj.getString("namasales"));
                            String omzet = (obj.getString("omzet"));
                            String tanggal = (obj.getString("tanggal"));
                            String namacustomer = (obj.getString("namacustomer"));

                            if(nomorsales.equals("")) nomorsales = "null";
                            if(namasales.equals("")) namasales = "null";
                            if(omzet.equals("")) omzet = "null";
                            if(tanggal.equals("")) tanggal = "null";
                            if(namacustomer.equals("")) namacustomer = "null";

                            tempData = tempData + nomorsales + "~" + namasales + "~" + omzet + "~" + tanggal + "~" + namacustomer + "|";
                        }
                    }

                    if(!tempData.equals(LibInspira.getShared(global.datapreferences, global.data.salesmanomzet, "")))
                    {
                        LibInspira.setShared(
                                global.datapreferences,
                                global.data.salesmanomzet,
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

    private class CheckTotalOmzet extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("nomorsales", LibInspira.getShared(global.omzetpreferences, global.omzet.nomorsales, ""));
                jsonObject.put("enddate", LibInspira.getShared(global.omzetpreferences, global.omzet.enddate, ""));
                jsonObject.put("bulantahun", LibInspira.getShared(global.omzetpreferences, global.omzet.bulantahun, ""));
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
                String totalomzet = "";
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(!obj.has("query")){
                            totalomzet = (obj.getString("totalomzet"));

                            if(totalomzet.equals("")) totalomzet = "null";
                        }
                    }
                    tvTotalOmzet.setText("Rp. " + LibInspira.delimeter(totalomzet));
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
            tvInformation.setVisibility(View.VISIBLE);
        }
    }

    public class ItemAdapter {

        private String nomorsales;
        private String namasales;
        private String nomorcust;
        private String namacust;
        private String omzet;
        private String tanggal;
        //private String totalOmzet;

        public ItemAdapter() {}

        public String getNomorSales() {return nomorsales;}
        public void setNomorSales(String _param) {this.nomorsales = _param;}

        public String getNamaSales() {return namasales;}
        public void setNamaSales(String _param) {this.namasales = _param;}

        public String getNomorCust() {return nomorcust;}
        public void setNomorCust(String _param) {this.nomorcust = _param;}

        public String getNamaCust() {return namacust;}
        public void setNamaCust(String _param) {this.namacust = _param;}

        public String getOmzet() {return omzet;}
        public void setOmzet(String _param) {this.omzet = _param;}

        public String getTanggal() {return tanggal;}
        public void setTanggal(String _param) {this.tanggal = _param;}

//        public String getTotalOmzet() {return totalOmzet;}
//        public void setTotalOmzet(String _param) {this.totalOmzet = _param;}
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
            TextView tvOmzet;
            TextView tvTanggal;
            TextView tvCust;
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
            holder.tvTanggal = (TextView)row.findViewById(R.id.tvKeterangan);
            holder.tvCust = (TextView)row.findViewById(R.id.tvValue);
            holder.tvOmzet = (TextView)row.findViewById(R.id.tvValue1);

            row.setTag(holder);
            setupItem(holder);

//            row.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    view.startAnimation(GlobalVar.listeffect);
//                    LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseKotaFragment());
//                }
//            });

            return row;
        }

        private void setupItem(final Holder holder) {
            holder.tvNama.setText(holder.adapterItem.getNamaSales().toUpperCase());
            holder.tvCust.setVisibility(View.VISIBLE);
            holder.tvCust.setText(holder.adapterItem.getNamaCust());
            holder.tvOmzet.setVisibility(View.VISIBLE);
            holder.tvOmzet.setText("Rp. " + LibInspira.delimeter(holder.adapterItem.getOmzet()));
            holder.tvTanggal.setVisibility(View.VISIBLE);
            holder.tvTanggal.setText(LibInspira.FormatDateBasedOnInspiraDateFormat(holder.adapterItem.getTanggal().toString(), "dd/MMM/yyyy"));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        checkSalesOmzet.cancel(true);
        checkTotalOmzet.cancel(true);
    }
}