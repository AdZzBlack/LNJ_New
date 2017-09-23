/******************************************************************************
 Author           : Tonny
 Description      : filter untuk stock monitoring
 History          :

 ******************************************************************************/
package layout;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.inspira.gms.LibInspira;
import com.inspira.gms.LibPDF;
import com.inspira.gms.R;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.inspira.gms.IndexInternal.global;
import static com.inspira.gms.IndexInternal.jsonObject;

//import android.app.Fragment;

public class FilterStockFragment extends Fragment implements View.OnClickListener{
    private TextView tvGudang, tvKategori, tvBentuk, tvSurface, tvJenis, tvGrade, tvBarang, tvLokasi;
    private ImageButton iBtnGudang, iBtnKategori, iBtnBentuk, iBtnSurface, iBtnJenis, iBtnGrade, iBtnBarang, iBtnLokasi;
    private EditText edtBarang, edtUkuran1, edtUkuran2, edtTebal, edtMotif, edtBlok;
    private DatePickerDialog dp;
    private TextView tvEndDate, tvStartDate;
    private int dateType;

    public FilterStockFragment() {
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
        View v = inflater.inflate(R.layout.fragment_filter_stock, container, false);
        getActivity().setTitle("Filter Stock Monitoring");
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
        tvKategori = (TextView) getView().findViewById(R.id.tvKategori);
        tvBentuk = (TextView) getView().findViewById(R.id.tvBentuk);
        tvSurface = (TextView) getView().findViewById(R.id.tvSurface);
        tvJenis = (TextView) getView().findViewById(R.id.tvJenis);
        tvGrade = (TextView) getView().findViewById(R.id.tvGrade);
        tvGudang = (TextView) getView().findViewById(R.id.tvGudang);
        tvBarang = (TextView) getView().findViewById(R.id.tvBarang);
        tvLokasi = (TextView) getView().findViewById(R.id.tvLokasi);

        iBtnKategori = (ImageButton) getView().findViewById(R.id.ibtnClearKategori);
        iBtnBentuk = (ImageButton) getView().findViewById(R.id.ibtnClearBentuk);
        iBtnSurface = (ImageButton) getView().findViewById(R.id.ibtnClearSurface);
        iBtnJenis = (ImageButton) getView().findViewById(R.id.ibtnClearJenis);
        iBtnGrade = (ImageButton) getView().findViewById(R.id.ibtnClearGrade);
        iBtnGudang = (ImageButton) getView().findViewById(R.id.ibtnClearGudang);
        iBtnBarang = (ImageButton) getView().findViewById(R.id.ibtnClearBarang);
        iBtnLokasi = (ImageButton) getView().findViewById(R.id.ibtnClearLokasi);

        edtBarang = (EditText) getView().findViewById(R.id.edtBarang);
        edtTebal = (EditText) getView().findViewById(R.id.edtTebal);
        edtMotif = (EditText) getView().findViewById(R.id.edtMotif);
        edtUkuran1 = (EditText) getView().findViewById(R.id.edtUkuran1);
        edtUkuran2 = (EditText) getView().findViewById(R.id.edtUkuran2);
        edtBlok = (EditText) getView().findViewById(R.id.edtBlok);

        tvEndDate = (TextView) getView().findViewById(R.id.tvEndDate);
        tvEndDate.setOnClickListener(this);
        tvStartDate = (TextView) getView().findViewById(R.id.tvStartDate);
        tvStartDate.setOnClickListener(this);

        tvKategori.setOnClickListener(this);
        tvBentuk.setOnClickListener(this);
        tvSurface.setOnClickListener(this);
        tvJenis.setOnClickListener(this);
        tvGrade.setOnClickListener(this);
        tvGudang.setOnClickListener(this);
        tvBarang.setOnClickListener(this);
        tvLokasi.setOnClickListener(this);

        iBtnKategori.setOnClickListener(this);
        iBtnGudang.setOnClickListener(this);
        iBtnGrade.setOnClickListener(this);
        iBtnJenis.setOnClickListener(this);
        iBtnSurface.setOnClickListener(this);
        iBtnBentuk.setOnClickListener(this);
        iBtnBarang.setOnClickListener(this);
        iBtnLokasi.setOnClickListener(this);

        tvKategori.setText(LibInspira.getShared(global.stockmonitoringpreferences, global.stock.kategori, ""));
        tvBentuk.setText(LibInspira.getShared(global.stockmonitoringpreferences, global.stock.bentuk, ""));
        tvSurface.setText(LibInspira.getShared(global.stockmonitoringpreferences, global.stock.surface, ""));
        tvJenis.setText(LibInspira.getShared(global.stockmonitoringpreferences, global.stock.jenis, ""));
        tvGrade.setText(LibInspira.getShared(global.stockmonitoringpreferences, global.stock.grade, ""));
        tvGudang.setText(LibInspira.getShared(global.stockmonitoringpreferences, global.stock.namagudang, ""));
        tvBarang.setText(LibInspira.getShared(global.stockmonitoringpreferences, global.stock.namabarang, ""));
        tvLokasi.setText(LibInspira.getShared(global.stockmonitoringpreferences, global.stock.lokasi, ""));

        if(LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockposition") || LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockpositionrandom"))
        {
            getView().findViewById(R.id.trBarang).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trGudang).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trKategori).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trJenis).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trGrade).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trBentuk).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trUkuran).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trTebal).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trMotif).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trSurface).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trEndDate).setVisibility(View.VISIBLE);
        }
        else if(LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockrandomperbarang"))
        {
            getView().findViewById(R.id.trBarang).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trGudang).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trKategori).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trJenis).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trGrade).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trBentuk).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trUkuran).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trTebal).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trMotif).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trSurface).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trEndDate).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trBlok).setVisibility(View.VISIBLE);
        }
        else if(LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockrandomperlokasi"))
        {
            getView().findViewById(R.id.trBarang).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trGudang).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trKategori).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trJenis).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trGrade).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trBentuk).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trUkuran).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trTebal).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trMotif).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trSurface).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trEndDate).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trLokasi).setVisibility(View.VISIBLE);
        }
        else if(LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockmutasi") || LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockkartu"))
        {
            getView().findViewById(R.id.trBarangButton).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trGudang).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trStartDate).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.trEndDate).setVisibility(View.VISIBLE);
        }

        // Define DatePicker
        Calendar newCalendar = Calendar.getInstance();
        dp = new DatePickerDialog(getActivity(), R.style.DialogTheme, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                try {
                    String date = year + "-" + (monthOfYear+1) + "-" + dayOfMonth;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date newdate = sdf.parse(date);
                    date = sdf.format(newdate);

                    if(dateType==1) tvStartDate.setText(date);
                    else if(dateType==2) tvEndDate.setText(date);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        getView().findViewById(R.id.btnFilterUpdate).setOnClickListener(this);

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c.getTime());
        tvEndDate.setText(formattedDate);
        tvStartDate.setText(formattedDate);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        //menyimpan filter
        LibInspira.setShared(global.stockmonitoringpreferences, global.stock.nomorbarang, edtBarang.getText().toString());
        LibInspira.setShared(global.stockmonitoringpreferences, global.stock.tebal, edtTebal.getText().toString());
        LibInspira.setShared(global.stockmonitoringpreferences, global.stock.motif, edtMotif.getText().toString());
        LibInspira.setShared(global.stockmonitoringpreferences, global.stock.blok, edtBlok.getText().toString());
        LibInspira.setShared(global.stockmonitoringpreferences, global.stock.tanggalawal, tvStartDate.getText().toString());
        LibInspira.setShared(global.stockmonitoringpreferences, global.stock.tanggalakhir, tvEndDate.getText().toString());
        if(edtUkuran1.getText().toString().equals("") && edtUkuran2.getText().toString().equals(""))
        {
            LibInspira.setShared(global.stockmonitoringpreferences, global.stock.ukuran, "");
        }
        else
        {
            LibInspira.setShared(global.stockmonitoringpreferences, global.stock.ukuran, edtUkuran1.getText().toString() + "x" + edtUkuran2.getText().toString());
        }

        if (id == R.id.btnFilterUpdate){
            LibInspira.setShared(global.datapreferences, global.data.stockPosisi, "");  //added by Tonny @21-Aug-2017  clear data stockposisi
            if(LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockposition"))
            {
                LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new StockPosisiFragment());
            }
            else if(LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockpositionrandom"))
            {
                LibInspira.ReplaceFragment(getFragmentManager(), R.id.fragment_container, new StockPosisiRandomFragment());
            }
            else if(LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockrandomperbarang"))
            {
                String actionUrl = "Stock/getStockRandomPerBarang/";
                new getData().execute( actionUrl );
            }
            else if(LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockrandomperlokasi"))
            {
                String actionUrl = "Stock/getStockRandomPerLokasi/";
                new getData().execute( actionUrl );
            }
            else if(LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockmutasi"))
            {
                String actionUrl = "Stock/getStockMutasi/";
                new getData().execute( actionUrl );
            }
            else if(LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockkartu"))
            {
                String actionUrl = "Stock/getStockKartu/";
                new getData().execute( actionUrl );
            }
        }
        else if (id == R.id.tvStartDate){
            dateType = 1;
            dp.show();
        }
        else if (id == R.id.tvEndDate){
            dateType = 2;
            dp.show();
        }
        else if (id == R.id.tvKategori){
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseKategoriFragment());
        }
        else if (id == R.id.tvGudang){
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseGudangFragment());
        }
        else if (id == R.id.tvBentuk){
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseBentukFragment());
        }
        else if (id == R.id.tvJenis){
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseJenisFragment());
        }
        else if (id == R.id.tvGrade){
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseGradeFragment());
        }
        else if (id == R.id.tvSurface){
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseSurfaceFragment());
        }
        else if (id == R.id.tvBarang){
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseBarangFragment());
        }
        else if (id == R.id.tvLokasi){
            LibInspira.ReplaceFragment(getActivity().getSupportFragmentManager(), R.id.fragment_container, new ChooseLokasiFragment());
        }
        else if (id == R.id.ibtnClearKategori){
            LibInspira.setShared(global.stockmonitoringpreferences, global.stock.kategori, "");
            tvKategori.setText("");
        }
        else if (id == R.id.ibtnClearGudang){
            LibInspira.setShared(global.stockmonitoringpreferences, global.stock.namagudang, "");
            LibInspira.setShared(global.stockmonitoringpreferences, global.stock.kodegudang, "");
            tvGudang.setText("");
        }
        else if (id == R.id.ibtnClearBarang){
            LibInspira.setShared(global.stockmonitoringpreferences, global.stock.namabarang, "");
            LibInspira.setShared(global.stockmonitoringpreferences, global.stock.kodebarang, "");
            tvBarang.setText("");
        }
        else if (id == R.id.ibtnClearBentuk){
            LibInspira.setShared(global.stockmonitoringpreferences, global.stock.bentuk, "");
            tvBentuk.setText("");
        }
        else if (id == R.id.ibtnClearGrade){
            LibInspira.setShared(global.stockmonitoringpreferences, global.stock.grade, "");
            tvGrade.setText("");
        }
        else if (id == R.id.ibtnClearJenis){
            LibInspira.setShared(global.stockmonitoringpreferences, global.stock.jenis, "");
            tvJenis.setText("");
        }
        else if (id == R.id.ibtnClearSurface){
            LibInspira.setShared(global.stockmonitoringpreferences, global.stock.surface, "");
            tvSurface.setText("");
        }
        else if (id == R.id.ibtnClearLokasi){
            LibInspira.setShared(global.stockmonitoringpreferences, global.stock.lokasi, "");
            tvLokasi.setText("");
        }
    }

    private void createPDF(String data)
    {
        LibPDF pdf = new LibPDF(getActivity());
        try
        {
            if(LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockrandomperbarang"))
            {
                pdf.createPDF_stockrandomperbarang(data,
                        LibInspira.getShared(global.stockmonitoringpreferences, global.stock.tanggalakhir, ""));
            }
            else if(LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockrandomperlokasi"))
            {
                pdf.createPDF_stockrandomperlokasi(data,
                        LibInspira.getShared(global.stockmonitoringpreferences, global.stock.tanggalakhir, ""));
            }
            else if(LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockmutasi"))
            {
                pdf.createPDF_stockmutasi(data,
                        LibInspira.getShared(global.stockmonitoringpreferences, global.stock.tanggalawal, ""),
                        LibInspira.getShared(global.stockmonitoringpreferences, global.stock.tanggalakhir, ""));
            }
            else if(LibInspira.getShared(global.sharedpreferences, global.shared.position,"").equals("stockkartu"))
            {
                pdf.createPDF_stockkartu(data,
                        LibInspira.getShared(global.stockmonitoringpreferences, global.stock.tanggalawal, ""),
                        LibInspira.getShared(global.stockmonitoringpreferences, global.stock.tanggalakhir, ""),
                        LibInspira.getShared(global.stockmonitoringpreferences, global.stock.namagudang, ""),
                        LibInspira.getShared(global.stockmonitoringpreferences, global.stock.kodegudang, ""));
            }
        }
        catch (FileNotFoundException e) {e.printStackTrace();}
        catch (DocumentException e) {e.printStackTrace();}
    }


    private class getData extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            jsonObject = new JSONObject();
            String nomorbarang = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.nomorbarang, "");
            String namagudang = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.namagudang, "");
            String kategori = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.kategori, "");
            String bentuk = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.bentuk, "");
            String jenis = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.jenis, "");
            String grade = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.grade, "");
            String surface = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.surface, "");
            String ukuran = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.ukuran, "");
            String tebal = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.tebal, "");
            String motif = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.motif, "");
            String tanggal = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.tanggalakhir, "");

            String nomorcabang = LibInspira.getShared(global.userpreferences, global.user.cabang, "");
            String kodegudang = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.kodegudang, "");

            String blok = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.blok, "");
            String lokasi = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.lokasi, "");

            String kodebarang = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.kodebarang, "");
            String tanggalawal = LibInspira.getShared(global.stockmonitoringpreferences, global.stock.tanggalawal, "");

            try {
                jsonObject.put("kodebarang", kodebarang);
                jsonObject.put("nomorbarang", nomorbarang);
                jsonObject.put("kodegudang", kodegudang);
                jsonObject.put("namagudang", namagudang);
                jsonObject.put("kategori", kategori);
                jsonObject.put("bentuk", bentuk);
                jsonObject.put("jenis", jenis);
                jsonObject.put("grade", grade);
                jsonObject.put("surface", surface);
                jsonObject.put("ukuran", ukuran);
                jsonObject.put("tebal", tebal);
                jsonObject.put("motif", motif);
                jsonObject.put("tanggalawal", tanggalawal);
                jsonObject.put("tanggal", tanggal);
                jsonObject.put("nomorcabang", nomorcabang);

                jsonObject.put("blok", blok);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return LibInspira.executePost(getContext(), urls[0], jsonObject, 60000);
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            Log.d("resultQuery", result);
            try
            {
                LibInspira.hideLoading();
                boolean error = false;
                JSONArray jsonarray = new JSONArray(result);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        if(obj.has("query")){
                            LibInspira.ShowShortToast(getContext(), "Failed get data");
                            error = true;
                        }
                    }
                }
                else
                {
                    error = true;
                }

                if(!error)
                {
                    createPDF(result);
                }
            }
            catch(Exception e)
            {
                e.printStackTrace();
                LibInspira.hideLoading();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            LibInspira.showLoading(getContext(), "Getting Stock Report", "please waiting...");
        }
    }
}
