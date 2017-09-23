/******************************************************************************
    Author           : ADI
    Description      : dashboard untuk internal
    History          :

******************************************************************************/
package layout;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.inspira.gms.IndexInternal.global;
import static com.inspira.gms.IndexInternal.jsonObject;

//import android.app.Fragment;

public class ScheduleTaskFragment extends Fragment implements View.OnClickListener{
    private FloatingActionButton fab;
    private TextView tvInformation, tvNoData;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;

    public ScheduleTaskFragment() {
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
        getActivity().setTitle("Schedule Task");
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
        tvNoData = (TextView) getView().findViewById(R.id.tvNoData);

        itemadapter = new ItemListAdapter(getActivity(), R.layout.list_item, new ArrayList<ItemAdapter>());
        itemadapter.clear();
        lvSearch = (ListView) getView().findViewById(R.id.lvChoose);
        lvSearch.setAdapter(itemadapter);

        fab = (FloatingActionButton) getView().findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
        fab.setOnClickListener(this);

        refreshList();

        String actionUrl = "Schedule/getSchedules/";
        new getData().execute( actionUrl );
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if(id==R.id.fab)
        {
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new FormScheduleTaskFragment());
        }
    }

    private void refreshList()
    {
        itemadapter.clear();
        list.clear();

        String data = LibInspira.getShared(global.datapreferences, global.data.schedule, "");
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
                    String creator = parts[1];
                    String target = parts[2];
                    String customer = parts[3];
                    String prospecting = parts[4];
                    String group = parts[5];
                    String type = parts[6];
                    String date = parts[7];
                    String time = parts[8];

                    if(nomor.equals("")) nomor = "null";
                    if(creator.equals("")) creator = "null";
                    if(target.equals("")) target = "null";
                    if(customer.equals("")) customer = "null";
                    if(prospecting.equals("")) prospecting = "null";
                    if(group.equals("")) group = "null";
                    if(type.equals("")) type = "null";
                    if(date.equals("")) date = "null";
                    if(time.equals("")) time = "null";

                    ItemAdapter dataItem = new ItemAdapter();
                    dataItem.setNomor(nomor);
                    dataItem.setCreator(creator);
                    dataItem.setTarget(target);
                    dataItem.setCustomer(customer);
                    dataItem.setProspecting(prospecting);
                    dataItem.setGroup(group);
                    dataItem.setType(type);
                    dataItem.setDate(date);
                    dataItem.setTime(time);
                    list.add(dataItem);

                    itemadapter.add(dataItem);
                    itemadapter.notifyDataSetChanged();
                }
            }
        }
    }

    private class cancelSchedule extends AsyncTask<String, Void, String> {
        String nomor;

        cancelSchedule(String nomor) {
            this.nomor = nomor;
        }

        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
            try {
                jsonObject.put("nomor", nomor);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            LibInspira.ShowLongToast(getContext(), "Schedule Canceled");
        }
    }

    private class getData extends AsyncTask<String, Void, String> {
        String user;

        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
            try {
                jsonObject.put("user", user);
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
                            String nomor = (obj.getString("nomor"));
                            String creator = (obj.getString("creator"));
                            String target = (obj.getString("target"));
                            String customer = (obj.getString("customer"));
                            String prospecting = (obj.getString("prospecting"));
                            String group = obj.getString("group");
                            String type = obj.getString("type");
                            String date = obj.getString("date");
                            String time = obj.getString("time");

                            if(nomor.equals("")) nomor = "null";
                            if(creator.equals("")) creator = "null";
                            if(target.equals("")) target = "null";
                            if(customer.equals("")) customer = "null";
                            if(prospecting.equals("")) prospecting = "null";
                            if(group.equals("")) group = "null";
                            if(type.equals("")) type = "null";
                            if(date.equals("")) date = "null";
                            if(time.equals("")) time = "null";

                            tempData = tempData + nomor + "~" + creator + "~" + target + "~" + customer + "~" + prospecting + "~" + group + "~" + type + "~" + date + "~" + time + "|";
                        }
                    }
                    if(!tempData.equals(LibInspira.getShared(global.datapreferences, global.data.schedule, "")))
                    {
                        LibInspira.setShared(
                                global.datapreferences,
                                global.data.schedule,
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
            user = LibInspira.getShared(global.userpreferences, global.user.nomor_android, "");
        }
    }

    private class ItemAdapter {

        private String nomor;
        private String creator;
        private String target;
        private String customer;
        private String prospecting;
        private String group;
        private String type;
        private String date;
        private String time;

        public ItemAdapter() {}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getCreator() {return creator;}
        public void setCreator(String _param) {this.creator = _param;}

        public String getTarget() {return target;}
        public void setTarget(String _param) {this.target = _param;}

        public String getType() {return type;}
        public void setType(String _param) {this.type = _param;}

        public String getCustomer() {return customer;}
        public void setCustomer(String _param) {this.customer = _param;}

        public String getProspecting() {return prospecting;}
        public void setProspecting(String _param) {this.prospecting = _param;}

        public String getGroup() {return group;}
        public void setGroup(String _param) {this.group = _param;}

        public String getDate() {return date;}
        public void setDate(String _param) {this.date = _param;}

        public String getTime() {return time;}
        public void setTime(String _param) {this.time = _param;}
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
            TextView tvDatetime;
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
            holder.tvDatetime = (TextView)row.findViewById(R.id.tvBigNote);
            holder.tvKeterangan = (TextView)row.findViewById(R.id.tvKeterangan);
            holder.tvKeterangan1 = (TextView)row.findViewById(R.id.tvKeterangan1);

            row.setTag(holder);
            setupItem(holder);

            final Holder finalholder = holder;
            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (finalholder.adapterItem.getCreator().equals(LibInspira.getShared(global.userpreferences, global.user.nomor_android, ""))) {
                        DialogInterface.OnClickListener dialog = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        String actionUrl = "Master/cancelSchedule/";
                                        new cancelSchedule(finalholder.adapterItem.getNomor()).execute(actionUrl);
                                        actionUrl = "Master/getSchedules/";
                                        new getData().execute(actionUrl);
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setMessage("Do You Want to Cancel This Schedule?").setPositiveButton("Yes", dialog)
                                .setNegativeButton("No", dialog).show();
                    }
                    return true;
                }
            });

            return row;
        }

        private void setupItem(final Holder holder) {
            holder.tvNama.setText(holder.adapterItem.getType().toUpperCase() + " Schedule");
            holder.tvDatetime.setVisibility(View.VISIBLE);
            holder.tvKeterangan.setVisibility(View.VISIBLE);
            holder.tvKeterangan1.setVisibility(View.VISIBLE);
//            holder.tvKeterangan1.
            SimpleDateFormat input = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat output = new SimpleDateFormat("HH:mm");
            try {
                String dateTimeTemp = "";
                Date temp = input.parse(holder.adapterItem.getTime());
                dateTimeTemp = output.format(temp);
                input = new SimpleDateFormat("yyyy-MM-dd");
                output = new SimpleDateFormat("dd MMMM yyyy");
                temp = input.parse(holder.adapterItem.getDate());
                dateTimeTemp += " " + output.format(temp);
                holder.tvDatetime.setText(dateTimeTemp);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (!holder.adapterItem.getCustomer().equals("null"))
                holder.tvKeterangan.setText("Customer: " + holder.adapterItem.getCustomer());
            else if (!holder.adapterItem.getProspecting().equals("null"))
                holder.tvKeterangan.setText("Customer: " + holder.adapterItem.getProspecting());
            else if (!holder.adapterItem.getGroup().equals("null"))
                holder.tvKeterangan.setText("Group: " + holder.adapterItem.getGroup());
            else
                holder.tvKeterangan.setVisibility(View.GONE);

            if (holder.adapterItem.getTarget().equals("null"))
                holder.tvKeterangan1.setVisibility(View.GONE);
            if (holder.adapterItem.getCreator().equals(LibInspira.getShared(global.userpreferences, global.user.nama, "")))
                holder.tvKeterangan1.setText("Target: " + holder.adapterItem.getTarget());
            else
                holder.tvKeterangan1.setText("Creator: " + holder.adapterItem.getCreator());
        }
    }
}
