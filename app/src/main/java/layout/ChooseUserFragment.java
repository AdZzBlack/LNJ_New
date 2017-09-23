package layout;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

/**
 * Created by shoma on 02/09/17.
 */

public class ChooseUserFragment extends Fragment implements View.OnClickListener {
    private EditText etSearch;
    private ImageButton ibtnSearch;

    private TextView tvInformation, tvNoData;
    private ListView lvSearch;
    private ItemListAdapter itemadapter;
    private ArrayList<ItemAdapter> list;
    private String tempSelectedUsers;

    public ChooseUserFragment() {
        // Required empty public constructor
        tempSelectedUsers = LibInspira.getShared(global.datapreferences, global.data.selectedUsers, "");
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
        getActivity().setTitle("Browse Users");
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

        if (LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("Conversation")) {
            ((RelativeLayout) getView().findViewById(R.id.rlFooter)).setVisibility(View.VISIBLE);
            ((Button) getView().findViewById(R.id.btnCenter)).setVisibility(View.VISIBLE);
            ((Button) getView().findViewById(R.id.btnCenter)).setText("Invite");
            ((Button) getView().findViewById(R.id.btnCenter)).setOnClickListener(this);
        }

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

        String actionUrl = "Master/getUsers/";
        new getData().execute( actionUrl );
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch (id) {
            case R.id.ibtnSearch:
                search();
                break;
            case R.id.btnCenter:
                LibInspira.setShared(global.datapreferences, global.data.selectedUsers, tempSelectedUsers);
                LibInspira.BackFragment(getActivity().getSupportFragmentManager());
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                    if (LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("Conversation"))
                        LibInspira.setShared(global.datapreferences, global.data.selectedUsers, tempSelectedUsers);
                }
                return false;
            }
        });
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

        String data = LibInspira.getShared(global.datapreferences, global.data.users, "");

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

                    if(nomor.equals("null")) nomor = "";
                    if(nama.equals("null")) nama = "";

                    ItemAdapter dataItem = new ItemAdapter();
                    dataItem.setNomor(nomor);
                    dataItem.setNama(nama);
                    if (tempSelectedUsers.contains(nama))
                        dataItem.setChoosen(true);


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
                    Log.d("jsonarray length: ", Integer.toString(jsonarray.length()));
                    for (int i = jsonarray.length() - 1; i >= 0; i--) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if (!obj.has("query")) {
                            String nomor = (obj.getString("nomor"));
                            String nama = (obj.getString("nama"));

                            if (nomor.equals("")) nomor = "null";
                            if (nama.equals("")) nama = "null";

                            tempData = tempData + nomor + "~" + nama + "|";
                        } else {
                            Log.d("FAILED: ", obj.getString("query"));
                        }

                    }
                    Log.d("tempData: ", tempData);
                    if(!tempData.equals(LibInspira.getShared(global.datapreferences, global.data.users, "")))
                    {
                        LibInspira.setShared(
                                global.datapreferences,
                                global.data.users,
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
        private Boolean isChoosen = false;

        public ItemAdapter() {}

        public String getNomor() {return nomor;}
        public void setNomor(String _param) {this.nomor = _param;}

        public String getNama() {return nama;}
        public void setNama(String _param) {this.nama = _param;}

        public Boolean getChoosen() {return isChoosen;}
        public void setChoosen(Boolean _param) {this.isChoosen = _param;}
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
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
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

            row.setTag(holder);
            setupItem(holder, row);

            final Holder finalHolder = holder;
            final View finalRow = row;
            final String type = LibInspira.getShared(global.schedulepreferences, global.schedule.typesch, "");
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(GlobalVar.listeffect);

                    if(LibInspira.getShared(global.sharedpreferences, global.shared.position, "").equals("schedule")) {
                        LibInspira.setShared(global.schedulepreferences, global.schedule.targetIDsch, finalHolder.adapterItem.getNomor());
                        LibInspira.setShared(global.schedulepreferences, global.schedule.targetsch, finalHolder.adapterItem.getNama());
                        if (type.equals("Customer"))
                            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseCustomerFragment());
                        else if (type.equals("Prospecting Customer"))
                            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseCustomerProspectingFragment());
                        else if (type.equals("Group Meeting"))
                            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseGroupFragment());
                        else
                            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new SummaryScheduleFragment());
                    }
                    else if (LibInspira.getShared(global.sharedpreferences, global.shared.position, "").contains("Conversation")) {
                        if(finalHolder.adapterItem.getChoosen()) {
                            finalHolder.adapterItem.setChoosen(false);
                            finalRow.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                            finalHolder.tvNama.setTextColor(getResources().getColor(R.color.colorPrimary));
                            String remove = finalHolder.adapterItem.getNomor() + "~" + finalHolder.adapterItem.getNama() + "|";
                            tempSelectedUsers = tempSelectedUsers.replace(remove, "");
                        } else {
                            finalHolder.adapterItem.setChoosen(true);
                            finalRow.setBackgroundColor(getResources().getColor(R.color.colorAccentDanger));
                            finalHolder.tvNama.setTextColor(Color.WHITE);
                            tempSelectedUsers += finalHolder.adapterItem.getNomor() + "~" + finalHolder.adapterItem.getNama() + "|";
                        }
                    }
                }
            });

            return row;
        }

        private void setupItem(final Holder holder, final View row) {
            holder.tvNama.setText(holder.adapterItem.getNama().toUpperCase());
            if(holder.adapterItem.getChoosen())
            {
                row.setBackgroundColor(getResources().getColor(R.color.colorAccentDanger));
                holder.tvNama.setTextColor(Color.WHITE);
            }
            else
            {
                row.setBackgroundColor(getResources().getColor(R.color.colorBackground));
                holder.tvNama.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        }
    }
}
