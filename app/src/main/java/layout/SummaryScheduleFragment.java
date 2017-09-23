package layout;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import org.json.JSONException;
import org.json.JSONObject;

import static com.inspira.gms.IndexInternal.global;

/**
 * Created by shoma on 30/08/17.
 */

public class SummaryScheduleFragment extends Fragment {
    private String target;
    private String targetID;
    private String customer;
    private String customerID;
    private String customerProspecting;
    private String customerProspectingID;
    private String group;
    private String groupID;
    private String date;
    private String time;
    private String type;
    private String reminder;
    private String description;

    public SummaryScheduleFragment() {
        target = LibInspira.getShared(global.schedulepreferences, global.schedule.targetsch, "Target");
        targetID = LibInspira.getShared(global.schedulepreferences, global.schedule.targetIDsch, "");
        customer = LibInspira.getShared(global.schedulepreferences, global.schedule.customersch, "Customer");
        customerID = LibInspira.getShared(global.schedulepreferences, global.schedule.customerIDsch, "");
        group = LibInspira.getShared(global.schedulepreferences, global.schedule.groupsch, "Group");
        groupID = LibInspira.getShared(global.schedulepreferences, global.schedule.groupIDsch, "");
        customerProspecting = LibInspira.getShared(global.schedulepreferences, global.schedule.customerProspectingsch, "");
        customerProspectingID = LibInspira.getShared(global.schedulepreferences, global.schedule.customerProspectingIDsch, "");
        date = LibInspira.getShared(global.schedulepreferences, global.schedule.datesch, "");
        time = LibInspira.getShared(global.schedulepreferences, global.schedule.timesch, "");
        type = LibInspira.getShared(global.schedulepreferences, global.schedule.typesch, "");
        reminder = LibInspira.getShared(global.schedulepreferences, global.schedule.remindersch, "");
        description = LibInspira.getShared(global.schedulepreferences, global.schedule.descriptionsch, "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preview_schedule_task, container, false);
        getActivity().setTitle("Schedule Preview");
        return view;
    }

    /*****************************************************************************/
    //OnAttach dijalankan pada saat fragment ini terpasang pada Activity penampungnya
    /*****************************************************************************/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((TextView) getView().findViewById(R.id.txvTarget)).setText(target);
        if (type.equals("Group Meeting")) {
            ((TextView) getView().findViewById(R.id.tvTarget)).setText("Target Group");
            ((TextView) getView().findViewById(R.id.txvTarget)).setText(groupID + " " + group);
        }
        else if (type.contains("Customer")) {
            ((TextView) getView().findViewById(R.id.tvCustomerSch)).setVisibility(View.VISIBLE);
            ((TextView) getView().findViewById(R.id.txvColon2)).setVisibility(View.VISIBLE);
            ((TextView) getView().findViewById(R.id.txvCustomer)).setVisibility(View.VISIBLE);
            ((TextView) getView().findViewById(R.id.txvCustomer)).setText(customer);
            if (type.equals("Prospecting Customer"))
                ((TextView) getView().findViewById(R.id.txvCustomer)).setText(customerProspecting);
        }
        ((TextView) getView().findViewById(R.id.txvType)).setText(type);
        ((TextView) getView().findViewById(R.id.txvDate)).setText(date);
        ((TextView) getView().findViewById(R.id.txvTime)).setText(time);
        ((TextView) getView().findViewById(R.id.txvMinute)).setText(reminder + " Minutes");
        ((TextView) getView().findViewById(R.id.txvDescription)).setText(description);
        ((Button) getView().findViewById(R.id.btnSave)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String actionUrl = "Schedule/setSchedule/";
                new setSchedule().execute(actionUrl);
            }
        });
    }

    private class setSchedule extends AsyncTask<String, Void, String> {
        JSONObject jsonObject;
        String creator;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            description.replace('"', '\"');
            description.replace("'", "\'");
            creator = LibInspira.getShared(global.userpreferences, global.user.nomor_android, "");
        }

        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
            try {
                jsonObject.put("creator", creator);
                jsonObject.put("target", targetID);
                jsonObject.put("customer", customerID);
                jsonObject.put("prospecting", customerProspectingID);
                jsonObject.put("group", groupID);
                jsonObject.put("type", type);
                jsonObject.put("reminder", reminder);
                jsonObject.put("date", date);
                jsonObject.put("time", time);
                jsonObject.put("description", description);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("resultQuery", result);
            LibInspira.ShowLongToast(getContext(), "Schedule Created");
            LibInspira.clearShared(global.schedulepreferences);
            LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new DashboardInternalFragment());

        }
    }
}
