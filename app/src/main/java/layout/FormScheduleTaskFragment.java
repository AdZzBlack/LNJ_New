/******************************************************************************
    Author           : ADI
    Description      : dashboard untuk internal
    History          :

******************************************************************************/
package layout;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.inspira.gms.GlobalVar;
import com.inspira.gms.LibInspira;
import com.inspira.gms.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.inspira.gms.IndexInternal.global;

//import android.app.Fragment;

public class FormScheduleTaskFragment extends Fragment implements View.OnClickListener{
    private DatePickerDialog dp;
    private TimePickerDialog tp;
    private TextView tvDate, tvTime;
    private Button btnNext;
    private Spinner spType;
    private EditText edtReminder, edtDescription;

    public FormScheduleTaskFragment() {
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
        View v = inflater.inflate(R.layout.fragment_form_schedule_task, container, false);
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

        tvDate = (TextView) getView().findViewById(R.id.tvDate);
        tvTime = (TextView) getView().findViewById(R.id.tvTime);
        btnNext = (Button) getView().findViewById(R.id.btnNext);
        spType = (Spinner) getView().findViewById(R.id.spType);
        edtReminder = (EditText) getView().findViewById(R.id.edtReminder);
        edtDescription = (EditText) getView().findViewById(R.id.edtDescription);

        tvDate.setOnClickListener(this);
        tvTime.setOnClickListener(this);
        btnNext.setOnClickListener(this);

        // Define DatePicker
        Calendar newCalendar = Calendar.getInstance();
        dp = new DatePickerDialog(getActivity(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                try {
                    String date = year + "-" + (monthOfYear+1) + "-" + dayOfMonth;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date newdate = sdf.parse(date);
                    date = sdf.format(newdate);

                    Calendar cal = GregorianCalendar.getInstance();
                    cal.setTime(new Date());
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                    Date daysBeforeDate = cal.getTime();

                    if(daysBeforeDate.getTime() > newdate.getTime())
                    {
                        tvDate.setText("[Date]");
                    }
                    else
                    {
                        tvDate.setText(date);
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        dp.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        // Define TimePicker
        Calendar newTime = Calendar.getInstance();
        tp= new TimePickerDialog(getActivity(), R.style.DialogTheme, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                try {
                    String time = selectedHour + ":" + selectedMinute;
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                    Date newtime = sdf.parse(time);
                    time = sdf.format(newtime);
                    tvTime.setText(time);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        }, newTime.get(Calendar.HOUR_OF_DAY), newTime.get(Calendar.MINUTE), true);

        tvDate.setText(LibInspira.getShared(global.schedulepreferences, global.schedule.datesch, "[Date]"));
        tvTime.setText(LibInspira.getShared(global.schedulepreferences, global.schedule.timesch, "[Time]"));
        edtReminder.setText(LibInspira.getShared(global.schedulepreferences, global.schedule.remindersch, "30"));
        edtDescription.setText(LibInspira.getShared(global.schedulepreferences, global.schedule.descriptionsch, ""));
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        view.startAnimation(GlobalVar.buttoneffect);

        if(id==R.id.tvDate)
        {
            dp.show();
        }
        else if(id==R.id.tvTime)
        {
            tp.show();
        }
        else if(id==R.id.btnNext)
        {
            String date = tvDate.getText().toString();
            String time = tvTime.getText().toString();
            String type = spType.getSelectedItem().toString();
            String reminder = edtReminder.getText().toString();
            String description = edtDescription.getText().toString();

            if(date.equals("[Date]") || time.equals("[Time]") || reminder.equals("") || description.equals(""))
            {
                LibInspira.ShowShortToast(getContext(), "Please fill in all fields");
            } else {
                LibInspira.clearShared(global.schedulepreferences);
                LibInspira.setShared(global.schedulepreferences, global.schedule.datesch, date);
                LibInspira.setShared(global.schedulepreferences, global.schedule.timesch, time);
                LibInspira.setShared(global.schedulepreferences, global.schedule.remindersch, reminder);
                LibInspira.setShared(global.schedulepreferences, global.schedule.typesch, type);
                LibInspira.setShared(global.schedulepreferences, global.schedule.descriptionsch, description);
                LibInspira.setShared(global.sharedpreferences, global.shared.position, "schedule");

                if (type.equals("Group Meeting"))
                    LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseGroupFragment());
                else
                    LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new ChooseUserFragment());
            }
        }
    }

    private class fcm_notif extends AsyncTask<String, Void, String> {
        String user_nomor = edtReminder.getText().toString();
        String message = edtDescription.getText().toString();

        JSONObject jsonObject;
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("user_nomor", user_nomor);
                jsonObject.put("message", message);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return LibInspira.executePost(getContext(), urls[0], jsonObject);
        }
    }
}
