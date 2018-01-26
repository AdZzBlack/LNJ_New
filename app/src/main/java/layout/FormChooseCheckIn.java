package layout;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

import com.inspira.lnj.GlobalVar;
import com.inspira.lnj.LibInspira;
import com.inspira.lnj.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FormChooseCheckIn extends Dialog implements View.OnClickListener {
    OnMyDialogResult mDialogResult;
    LinearLayout ll;
    Button btnCheckpoint;
    private GlobalVar global;
    private JSONObject jsonObject;
    private CheckpointList checkpointList;

    public FormChooseCheckIn(@NonNull Context context) {
        super(context);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.form_choose_checkin);
//        ll = (LinearLayout) findViewById(R.id.llContent);
//        findViewById(R.id.btnStartFumigasi).setOnClickListener(this);
//        findViewById(R.id.btnStopFumigasi).setOnClickListener(this);
//        findViewById(R.id.btnInDepo).setOnClickListener(this);
//        findViewById(R.id.btnOutDepo).setOnClickListener(this);
//        findViewById(R.id.btnOutPort).setOnClickListener(this);
//        findViewById(R.id.btnUnloading).setOnClickListener(this);
//        findViewById(R.id.btnPickup).setOnClickListener(this);
//        findViewById(R.id.btnReturn).setOnClickListener(this);
//        findViewById(R.id.btnPortGateIn).setOnClickListener(this);
        checkpointList = new CheckpointList();
        String actionUrl = "Scanning/getCheckpointList/";
        checkpointList.execute(actionUrl);
    }

    @Override
    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.btnStartFumigasi:
//                mDialogResult.finish("startfumigasi");
//                break;
//            case R.id.btnStopFumigasi:
//                mDialogResult.finish("stopfumigasi");
//                break;
//            case R.id.btnInDepo:
//                mDialogResult.finish("indepo");
//                break;
//            case R.id.btnOutDepo:
//                mDialogResult.finish("outdepo");
//                break;
//            case R.id.btnOutPort:
//                mDialogResult.finish("outport");
//                break;
//            case R.id.btnUnloading:
//                mDialogResult.finish("unloading");
//                break;
//            case R.id.btnPickup:
//                mDialogResult.finish("pickup");
//                break;
//            case R.id.btnReturn:
//                mDialogResult.finish("return");
//                break;
//            case R.id.btnPortGateIn:
//                mDialogResult.finish("portgatein");
//                break;
//            default:
//                break;
////            case R.id.btn_cancel:
////                dismiss();
////                break;
//        }
        dismiss();
    }

    public void setDialogResult(OnMyDialogResult dialogResult){
        mDialogResult = dialogResult;
    }

    public interface OnMyDialogResult{
        void finish(String jenis);
    }

    private class CheckpointList extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                jsonObject = new JSONObject();
                jsonObject.put("nomortdsuratjalan", LibInspira.getShared(global.userpreferences,global.user.checkin_nomortdsuratjalan,""));
            } catch (JSONException e) {
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
                    ll = (LinearLayout) findViewById(R.id.llContent);
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        LibInspira.hideLoading();
                        if(!obj.has("query")){  //jika success
                            final String btnName = obj.getString("nama");
                            btnCheckpoint = new Button(getContext());
                            btnCheckpoint.setText(btnName.toUpperCase());
                            final float scale = getContext().getResources().getDisplayMetrics().density;
                            int pixels = (int) (70 * scale + 0.5f);
                            btnCheckpoint.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, pixels));
                            btnCheckpoint.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
//                                    LibInspira.showShortToast(getContext(), btnName);
                                    mDialogResult.finish(btnName.replace(" ", ""));
                                    dismiss();
                                }
                            });
                            ll.addView(btnCheckpoint);
                        }
                        else
                        {
                            LibInspira.showLongToast(getContext(), obj.getString("message"));
                            dismiss();
                        }
                    }
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.showLongToast(getContext(), e.getMessage());
                LibInspira.hideLoading();
                dismiss();
            }
//            LibInspira.BackFragment(getFragmentManager());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Retrieving data", "Loading");
        }
    }
}