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

public class ChooseCustomerProspectingFragment extends Fragment implements View.OnClickListener{
    private EditText etSearch;

    private TextView tvInformation, tvNoData;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;
    private CheckCustomerProspecting checkCustomerProspecting;

    public ChooseCustomerProspectingFragment() {
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
        getActivity().setTitle("Customer Prospecting");
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

        String actionUrl = "Customer/getCustomerProspecting/";
        checkCustomerProspecting = new CheckCustomerProspecting();
        checkCustomerProspecting.execute( actionUrl );
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        checkCustomerProspecting.cancel(true);
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
                if(!list.get(ctr).getNomor().equals(LibInspira.getShared(global.userpreferences, global.user.nomor_android, "")))
                {
                    itemadapter.add(list.get(ctr));
                    itemadapter.notifyDataSetChanged();
                }
            }
            else
            {
                if(LibInspira.contains(list.get(ctr).getNama(),etSearch.getText().toString() ))
                {
                    if(!list.get(ctr).getNomor().equals(LibInspira.getShared(global.userpreferences, global.user.nomor_android, "")))
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

        String data = LibInspira.getShared(global.datapreferences, global.data.customerprospecting, "");
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

                    if(nomor.equals("null")) nomor = "";
                    if(nama.equals("null")) nama = "";
                    if(alamat.equals("null")) alamat = "-";
                    if(telepon.equals("null")) telepon = "";

                    ItemAdapter dataItem = new ItemAdapter();
                    dataItem.setNomor(nomor);
                    dataItem.setNama(nama);
                    dataItem.setAlamat(alamat);
                    dataItem.setTelepon(telepon);
                    list.add(dataItem);

                    if(!dataItem.getNomor().equals(LibInspira.getShared(global.userpreferences, global.user.nomor_android, "")))
                    {
                        itemadapter.add(dataItem);
                        itemadapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    private class CheckCustomerProspecting extends AsyncTask<String, Void, String> {
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
                            String nomor = (obj.getString("nomorcustomer"));
                            String nama = (obj.getString("namacustomer"));
                            String alamat = (obj.getString("alamatcustomer"));
                            String telepon = (obj.getString("telepon"));

                            if(nomor.equals("")) nomor = "null";
                            if(nama.equals("")) nama = "null";
                            if(alamat.equals("")) alamat = "null";
                            if(telepon.equals("")) telepon = "null";

                            tempData = tempData + nomor + "~" + nama + "~" + alamat + "~" + telepon + "|";
                        }
                    }

                    if(!tempData.equals(LibInspira.getShared(global.datapreferences, global.data.user, "")))
                    {
                        LibInspira.setShared(
                                global.datapreferences,
                                global.data.customerprospecting,
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

        private String nomorcustomer;
        private String kodecustomer;
        private String namacustomer;
        private String alamatcustomer;
        private String kodekotacustomer;
        private String namakotacustomer;
        private String telepon;
        private String fax;
        private String namapengiriman;
        private String alamatpengiriman;
        private String nomorkotapengiriman;
        private String kodekotapengiriman;
        private String teleponpengiriman;
        private String namapkp;
        private String alamatpkp;
        private String nomorkotapkp;
        private String kodekotapkp;
        private String namakotapkp;
        private String npwp;
        private String nppkp;

        public ItemAdapter() {}

        public String getNomor() {return nomorcustomer;}
        public void setNomor(String _param) {this.nomorcustomer = _param;}

        public String getKodeCustomer() {return kodecustomer;}
        public void setKodeCustomer(String _param) {this.kodecustomer = _param;}

        public String getNama() {return namacustomer;}
        public void setNama(String _param) {this.namacustomer = _param;}

        public String getAlamat() {return alamatcustomer;}
        public void setAlamat(String _param) {this.alamatcustomer = _param;}

        public String getKodeKotaCustomer() {return kodekotacustomer;}
        public void setKodeKotaCustomer(String _param) {this.kodekotacustomer = _param;}

        public String getNamaKotaCustomer() {return namakotacustomer;}
        public void setNamaKotaCustomer(String _param) {this.namakotacustomer = _param;}

        public String getTelepon() {return telepon;}
        public void setTelepon(String _param) {this.telepon = _param;}

        public String getFax() {return fax;}
        public void setFax(String _param) {this.fax = _param;}

        public String getNamaPengiriman() {return namapengiriman;}
        public void setNamaPengiriman(String _param) {this.namapengiriman= _param;}

        public String getAlamatPengiriman() {return alamatpengiriman;}
        public void setAlamatPengiriman(String _param) {this.alamatpengiriman = _param;}

        public String getNomorKotaPengiriman() {return nomorkotapengiriman;}
        public void setNomorKotaPengiriman(String _param) {this.nomorkotapengiriman = _param;}

        public String getTeleponPengiriman() {return teleponpengiriman;}
        public void setTeleponPengiriman(String _param) {this.teleponpengiriman = _param;}

        public String getNamaPkp() {return namapkp;}
        public void setNamaPkp(String _param) {this.namapkp = _param;}

        public String getAlamatPkp() {return alamatpkp;}
        public void setAlamatPkp(String _param) {this.alamatpkp = _param;}

        public String getNomorKotaPkp() {return nomorkotapkp;}
        public void setNomorKotaPkp(String _param) {this.nomorkotapkp = _param;}

        public String getKodeKotaPkp() {return kodekotapkp;}
        public void setKodeKotaPkp(String _param) {this.kodekotapkp = _param;}

        public String getNamaKotaPkp() {return namakotapkp;}
        public void setNamaKotaPkp(String _param) {this.namakotapkp = _param;}

        public String getNpwp() {return npwp;}
        public void setNpwp(String _param) {this.npwp = _param;}

        public String getNppkp() {return nppkp;}
        public void setNppkp(String _param) {this.nppkp = _param;}

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
            TextView tvLocation;
            //ImageView ivCall;
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
            holder.tvLocation = (TextView)row.findViewById(R.id.tvKeterangan);
            //holder.ivCall = (ImageView)row.findViewById(R.id.ivCall);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalHolder = holder;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(GlobalVar.listeffect);
                    if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("schedule")) {
                        LibInspira.setShared(global.schedulepreferences, global.schedule.customerProspectingIDsch, finalHolder.adapterItem.getNomor());
                        LibInspira.setShared(global.schedulepreferences, global.schedule.customerProspectingsch, finalHolder.adapterItem.getNama());
                        LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new SummaryScheduleFragment());
                    }
//                    LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseKotaFragment());
                }
            });

            return row;
        }

        private void setupItem(final Holder holder) {
            holder.tvNama.setText(holder.adapterItem.getNama().toUpperCase() + " (" + holder.adapterItem.getTelepon() + ")");
            holder.tvLocation.setVisibility(View.VISIBLE);
            holder.tvLocation.setText(holder.adapterItem.getAlamat());
        }
    }
}
