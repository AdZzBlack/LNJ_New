package layout;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.inspira.gms.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shoma on 28/08/17.
 */

public class AddressesFragment extends Fragment {

    private String[] addresses;
    private String[] datetime;
    private TextView tvNoData;
    private ListView lvSearch;
    private AddressesFragment.ItemListAdapter itemadapter;
    private ArrayList<AddressesFragment.ItemAdapter> list;

    public AddressesFragment() {
    }

    public void setDataFragment(String[] addresses, String[] datetime) {
        this.addresses = addresses;
        this.datetime = datetime;
    }

    public class ItemAdapter {

        private String datetime;
        private String location;

        ItemAdapter(String location, String datetime) {
            this.location = location;
            this.datetime = datetime;
        }

        public String getDatetime() {
            return datetime;
        }

        public String getLocation() {
            return location;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_choose, container, false);
        getActivity().setTitle("Address");
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        list = new ArrayList<AddressesFragment.ItemAdapter>();

        tvNoData = (TextView) getView().findViewById(R.id.tvNoData);

        itemadapter = new AddressesFragment.ItemListAdapter(getActivity(), R.layout.list_item, new ArrayList<AddressesFragment.ItemAdapter>());
        itemadapter.clear();
        lvSearch = (ListView) getView().findViewById(R.id.lvChoose);
        lvSearch.setAdapter(itemadapter);

        refreshList();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private void refreshList()
    {
        itemadapter.clear();
        list.clear();

        if(addresses.length < 1)
        {
            tvNoData.setVisibility(View.VISIBLE);
        }
        else
        {
            tvNoData.setVisibility(View.GONE);
            for(int i = 0; i < addresses.length; i++) {
                AddressesFragment.ItemAdapter dataItem = new AddressesFragment.ItemAdapter(addresses[i], datetime[i]);
                list.add(dataItem);
                itemadapter.add(dataItem);
                itemadapter.notifyDataSetChanged();
            }
        }
    }

    public class ItemListAdapter extends ArrayAdapter<AddressesFragment.ItemAdapter> {

        private List<AddressesFragment.ItemAdapter> items;
        private int layoutResourceId;
        private Context context;

        public ItemListAdapter(Context context, int layoutResourceId, List<AddressesFragment.ItemAdapter> items) {
            super(context, layoutResourceId, items);
            this.layoutResourceId = layoutResourceId;
            this.context = context;
            this.items = items;
        }

        public List<AddressesFragment.ItemAdapter> getItems() {
            return items;
        }

        public class Holder {
            AddressesFragment.ItemAdapter adapterItem;
            TextView tvDateTime;
            TextView tvLocation;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            AddressesFragment.ItemListAdapter.Holder holder = null;

            if(row==null)
            {
                LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);
            }

            holder = new AddressesFragment.ItemListAdapter.Holder();
            holder.adapterItem = items.get(position);

            holder.tvDateTime = (TextView)row.findViewById(R.id.tvName);
            holder.tvLocation = (TextView)row.findViewById(R.id.tvKeterangan);

            row.setTag(holder);
            setupItem(holder);

            return row;
        }

        private void setupItem(final AddressesFragment.ItemListAdapter.Holder holder) {
            holder.tvDateTime.setText(holder.adapterItem.getDatetime());
            holder.tvLocation.setVisibility(View.VISIBLE);
            holder.tvLocation.setText("Location: " + holder.adapterItem.getLocation());
        }
    }
}
