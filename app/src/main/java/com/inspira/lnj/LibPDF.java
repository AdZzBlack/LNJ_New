package com.inspira.lnj;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * Created by ADI on 8/31/2017.
 */

public class LibPDF {

    private static FragmentActivity fragmentActivity;

    private static PdfPCell cell;
    private String path;
    private static File dir;
    private static File file;

    static Font fheader = new Font(Font.FontFamily.TIMES_ROMAN,10.0f, Font.BOLD, BaseColor.BLACK);
    static Font fsubheader = new Font(Font.FontFamily.TIMES_ROMAN,6.0f, Font.BOLD, BaseColor.BLACK);
    Font ffooter = new Font(Font.FontFamily.TIMES_ROMAN,5.0f, Font.BOLD, BaseColor.BLACK);
    static Font f = new Font(Font.FontFamily.TIMES_ROMAN,5.0f, Font.NORMAL, BaseColor.BLACK);
    Font fbolditalic = new Font(Font.FontFamily.TIMES_ROMAN,6.0f, Font.BOLDITALIC, BaseColor.BLACK);
    Font fred = new Font(Font.FontFamily.TIMES_ROMAN,5.0f, Font.NORMAL, BaseColor.RED);

    public LibPDF(FragmentActivity _fragmentActivity)
    {
        fragmentActivity = _fragmentActivity;

        //membuat folder baru jika belum ada sebelumnya
        path = Environment.getExternalStorageDirectory().getAbsolutePath() + GlobalVar.folderPDF;
        dir = new File(path);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
    }

    public static void displaypdf(File _file)
    {
        File file = _file;
        if(file.exists())
        {
            Intent target = new Intent(Intent.ACTION_VIEW);
            target.setDataAndType(Uri.fromFile(file), "application/pdf");
            target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

            Intent intent = Intent.createChooser(target, "Open File");
            try
            {
                fragmentActivity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Instruct the user to install a PDF reader here, or something
            }
        }
        else
            Toast.makeText(fragmentActivity, "File path is incorrect." , Toast.LENGTH_LONG).show();
    }

    public static PdfPCell spacecell(int _colspan)
    {
        cell = new PdfPCell(new Phrase("", f));
        cell.setColspan(_colspan);
        cell.setBorder(Border.noborder);
        return cell;
    }

    public PdfPCell spacecell(int _colspan, int border)
    {
        cell = new PdfPCell(new Phrase("", f));
        cell.setColspan(_colspan);
        cell.setBorder(border);
        return cell;
    }

    public static void createPDF_livetracking(String data, String tanggal) throws FileNotFoundException, DocumentException
    {
        //create document file
        Document doc = new Document(PageSize.A4, 36, 36, 115, 36);
        try {
            String pdfname = "temp.pdf";

            file = new File(dir, pdfname);
            FileOutputStream fOut = new FileOutputStream(file);
            PdfWriter writer = PdfWriter.getInstance(doc, fOut);

            TableHeader event = new TableHeader();
            writer.setPageEvent(event);

            //open the document
            doc.open();

            try {
                String jobnow = "";

                PdfPTable headertable = new PdfPTable(4);
                PdfPTable bodytable = new PdfPTable(4);
                bodytable.setWidthPercentage(100);
                float[] columnWidth = new float[]{20, 10, 10, 60};
                headertable.setWidths(columnWidth);
                bodytable.setWidths(columnWidth);

                JSONArray jsonarray = new JSONArray(data);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++)
                    {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        String job = (obj.getString("job").equals("null") ? "" : obj.getString("job"));
                        String tgldelivery = (obj.getString("tgldelivery").equals("null") ? "" : obj.getString("tgldelivery"));
                        String vehicleid = (obj.getString("vehicleid").equals("null") ? "" : obj.getString("vehicleid"));
                        String kodecontainer = (obj.getString("kodecontainer").equals("null") ? "" : obj.getString("kodecontainer"));
                        String weight = (obj.getString("weight").equals("null") ? "" : obj.getString("weight"));
                        String driver = (obj.getString("driver").equals("null") ? "" : obj.getString("driver"));
                        String latitude = (obj.getString("latitude").equals("null") ? "" : obj.getString("latitude"));
                        String longitude = (obj.getString("longitude").equals("null") ? "" : obj.getString("longitude"));
                        String datetracking = (obj.getString("datetracking").equals("null") ? "" : obj.getString("datetracking"));

                        if(!jobnow.equals(job))
                        {
                            if(!jobnow.equals(""))
                            {
                                doc.add(bodytable);
                                bodytable = new PdfPTable(4);
                                bodytable.setWidths(columnWidth);
                                bodytable.setWidthPercentage(100);
                                headertable = new PdfPTable(4);
                                headertable.setWidths(columnWidth);

                                doc.newPage();
                            }

                            cell = new PdfPCell(new Phrase("LIVE TRACKING REPORT", fheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(4);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("Per Tanggal " + LibInspira.FormatDateBasedOnInspiraDateFormat(tanggal, "dd-MM-yyyy"), f));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(4);
                            headertable.addCell(cell);

                            headertable.addCell(spacecell(4));

                            cell = new PdfPCell(new Phrase("Delivery Date: " + tgldelivery, fsubheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(4);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("Container: " + kodecontainer.toUpperCase(), fsubheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(3);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("Driver: " + driver, fsubheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(1);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("Weight: " + weight, fsubheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(3);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("Vehicle ID: " + vehicleid, fsubheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(1);
                            headertable.addCell(cell);

                            headertable.addCell(spacecell(4));

                            cell = new PdfPCell(new Phrase("DATE TRACKING", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("LAT", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("LON", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("DESCRIPTION", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            event.setHeader(headertable);
                            jobnow = job;
                        }

                        cell = new PdfPCell(new Phrase(datetracking, f));
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(latitude, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(longitude, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase("", f));
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);
                    }
                    doc.add(bodytable);
                }
                Toast.makeText(fragmentActivity, "PDF Created", Toast.LENGTH_LONG).show();
                displaypdf(file);
            } catch (DocumentException de) {
                Log.e("PDFCreator", "DocumentException:" + de);
            } finally {
                doc.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //added by Tonny @13-Feb-2018
    public static void createPDF_deviation(String data, String tanggal) throws FileNotFoundException, DocumentException
    {
        //create document file
        Document doc = new Document(PageSize.A4, 36, 36, 115, 36);
        int numColumns = 8;
        try {
            String pdfname = "temp.pdf";

            file = new File(dir, pdfname);
            FileOutputStream fOut = new FileOutputStream(file);
            PdfWriter writer = PdfWriter.getInstance(doc, fOut);

            TableHeader event = new TableHeader();
            writer.setPageEvent(event);

            //open the document
            doc.open();

            try {
                String jobnow = "";

                PdfPTable headertable = new PdfPTable(numColumns);
                PdfPTable bodytable = new PdfPTable(numColumns);
                bodytable.setWidthPercentage(100);
                float[] columnWidth = new float[]{20, 10, 10, 10, 10, 10, 10, 20};
                headertable.setWidths(columnWidth);
                bodytable.setWidths(columnWidth);

                JSONArray jsonarray = new JSONArray(data);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++)
                    {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        String job = (obj.getString("job").equals("null") ? "" : obj.getString("job"));
                        String tgldelivery = (obj.getString("tgldelivery").equals("null") ? "" : obj.getString("tgldelivery"));
                        String vehicleid = (obj.getString("vehicleid").equals("null") ? "" : obj.getString("vehicleid"));
                        String kodecontainer = (obj.getString("kodecontainer").equals("null") ? "" : obj.getString("kodecontainer"));
                        String weight = (obj.getString("weight").equals("null") ? "" : obj.getString("weight"));
                        String driver = (obj.getString("driver").equals("null") ? "" : obj.getString("driver"));
                        String latitude = (obj.getString("latitude").equals("null") ? "" : obj.getString("latitude"));
                        String longitude = (obj.getString("longitude").equals("null") ? "" : obj.getString("longitude"));
                        String cp_latitude = (obj.getString("cp_latitude").equals("null") ? "" : obj.getString("cp_latitude"));
                        String cp_longitude = (obj.getString("cp_longitude").equals("null") ? "" : obj.getString("cp_longitude"));
                        String radius_km = (obj.getString("radius_km").equals("null") ? "" : obj.getString("radius_km"));
                        String distance_km = (obj.getString("distance_km").equals("null") ? "" : obj.getString("distance_km"));
                        String datetracking = (obj.getString("datetracking").equals("null") ? "" : obj.getString("datetracking"));

                        if(!jobnow.equals(job))
                        {
                            if(!jobnow.equals(""))
                            {
                                doc.add(bodytable);
                                columnWidth = new float[]{20, 10, 10, 10, 10, 10, 10, 20};
                                headertable = new PdfPTable(numColumns);
                                headertable.setWidths(columnWidth);
                                bodytable = new PdfPTable(numColumns);
                                bodytable.setWidths(columnWidth);
                                bodytable.setWidthPercentage(100);

                                doc.newPage();
                            }

                            cell = new PdfPCell(new Phrase("DEVIATION REPORT", fheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(8);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("Per Tanggal " + LibInspira.FormatDateBasedOnInspiraDateFormat(tanggal, "dd-MM-yyyy"), f));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(8);
                            headertable.addCell(cell);

                            headertable.addCell(spacecell(numColumns));

                            cell = new PdfPCell(new Phrase("Delivery Date: " + tgldelivery, fsubheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(8);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("Container: " + kodecontainer.toUpperCase(), fsubheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(5);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("Driver: " + driver, fsubheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(3);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("Weight: " + weight, fsubheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(5);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("Vehicle ID: " + vehicleid, fsubheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(3);
                            headertable.addCell(cell);

                            headertable.addCell(spacecell(numColumns));

                            cell = new PdfPCell(new Phrase("DATE TRACKING", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("LAT", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("LON", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("CP LAT", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("CP LON", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("RADIUS (KM)", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("DISTANCE (KM)", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("DESCRIPTION", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            event.setHeader(headertable);
                            jobnow = job;
                        }

                        cell = new PdfPCell(new Phrase(datetracking, f));
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(latitude, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(longitude, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(cp_latitude, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(cp_longitude, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(radius_km, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(distance_km, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase("", f));
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);
                    }
                    doc.add(bodytable);
                }
                Toast.makeText(fragmentActivity, "PDF Created", Toast.LENGTH_LONG).show();
                displaypdf(file);
            } catch (DocumentException de) {
                Log.e("PDFCreator", "DocumentException:" + de);
            } finally {
                doc.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createPDF_docdistribution(String _data, String _tanggal) throws FileNotFoundException, DocumentException
    {
        //create document file
        Document doc = new Document(PageSize.A4, 36, 36, 95, 36);
        int numColumns = 7;
        try {
            String pdfname = "temp.pdf";

            file = new File(dir, pdfname);
            FileOutputStream fOut = new FileOutputStream(file);
            PdfWriter writer = PdfWriter.getInstance(doc, fOut);

            TableHeader event = new TableHeader();
            writer.setPageEvent(event);

            //open the document
            doc.open();

            try {
                String jobnow = "";

                PdfPTable headertable = new PdfPTable(numColumns);
                PdfPTable bodytable = new PdfPTable(numColumns);
                bodytable.setWidthPercentage(100);
                float[] columnWidth = new float[]{15, 15, 10, 15, 15, 10, 20};
                headertable.setWidths(columnWidth);
                bodytable.setWidths(columnWidth);

                JSONArray jsonarray = new JSONArray(_data);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++)
                    {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        String kode = (obj.getString("kode").equals("null") ? "" : obj.getString("kode"));
                        String job = (obj.getString("job").equals("null") ? "" : obj.getString("job"));
                        String ref = (obj.getString("ref").equals("null") ? "" : obj.getString("ref"));
                        String nama_from = (obj.getString("nama_from").equals("null") ? "" : obj.getString("nama_from"));
                        String tanggal = (obj.getString("tanggal").equals("null") ? "" : obj.getString("tanggal"));
                        String action = (obj.getString("action").equals("null") ? "" : obj.getString("action"));
                        String nama_to = (obj.getString("nama_to").equals("null") ? "" : obj.getString("nama_to"));
                        String keterangan = (obj.getString("keterangan").equals("null") ? "" : obj.getString("keterangan"));

                        if(!jobnow.equals(job))
                        {
                            if(!jobnow.equals(""))
                            {
                                doc.add(bodytable);
                                columnWidth = new float[]{15, 15, 10, 15, 15, 10, 20};
                                headertable = new PdfPTable(numColumns);
                                headertable.setWidths(columnWidth);
                                bodytable = new PdfPTable(numColumns);
                                bodytable.setWidths(columnWidth);
                                bodytable.setWidthPercentage(100);

                                doc.newPage();
                            }

                            cell = new PdfPCell(new Phrase("DOCUMENT DISTRIBUTION REPORT", fheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(7);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("Per Tanggal " + LibInspira.FormatDateBasedOnInspiraDateFormat(_tanggal, "dd-MM-yyyy"), f));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(7);
                            headertable.addCell(cell);

                            headertable.addCell(spacecell(numColumns));

                            cell = new PdfPCell(new Phrase("Job: " + job, fsubheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(7);
                            headertable.addCell(cell);

                            headertable.addCell(spacecell(numColumns));

                            cell = new PdfPCell(new Phrase("DOCUMENT", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("REFERENCE", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("DATE TIME", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("FROM", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("TO", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("ACTION", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("NOTES", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            event.setHeader(headertable);
                            jobnow = job;
                        }

                        cell = new PdfPCell(new Phrase(kode, f));
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(ref, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(tanggal, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(nama_from, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(nama_to, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(action, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(keterangan, f));
                        cell.setBorder(Border.full);
                        bodytable.addCell(cell);
                    }
                    doc.add(bodytable);
                }
                Toast.makeText(fragmentActivity, "PDF Created", Toast.LENGTH_LONG).show();
                displaypdf(file);
            } catch (DocumentException de) {
                Log.e("PDFCreator", "DocumentException:" + de);
            } finally {
                doc.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //CONTOH LENGKAP
    public static void createPDF_stockrandomperbarang(String data, String tanggal) throws FileNotFoundException, DocumentException
    {
        //create document file
        Document doc = new Document(PageSize.A4, 36, 36, 90, 36);
        try {
            String pdfname = "temp.pdf";

            file = new File(dir, pdfname);
            FileOutputStream fOut = new FileOutputStream(file);
            PdfWriter writer = PdfWriter.getInstance(doc, fOut);

            TableHeader event = new TableHeader();
            writer.setPageEvent(event);

            //open the document
            doc.open();

            try {
                String kodebarangnow = "";
                String bloknow = "0";
                String barcodenow = "0";
                int totalunitbarang = 0;
                int totalunitblok = 0;
                Double totalm2barang = 0.0;
                Double totalm2blok = 0.0;

                PdfPTable headertable = new PdfPTable(11);
                PdfPTable bodytable = new PdfPTable(11);
                bodytable.setWidthPercentage(100);
                float[] columnWidth = new float[]{5, 20, 10, 10, 10, 10, 5, 5, 5, 10, 10};
                headertable.setWidths(columnWidth);
                bodytable.setWidths(columnWidth);

                JSONArray jsonarray = new JSONArray(data);
                if(jsonarray.length() > 0){
                    for (int i = 0; i < jsonarray.length(); i++) {
                        JSONObject obj = jsonarray.getJSONObject(i);
                        String kodegudang = (obj.getString("kodegudang").equals("null") ? "" : obj.getString("kodegudang"));
                        String kodebarang = (obj.getString("kodebarang").equals("null") ? "" : obj.getString("kodebarang"));
                        String namabarang = (obj.getString("namabarang").equals("null") ? "" : obj.getString("namabarang"));
                        String namagudang = (obj.getString("namagudang").equals("null") ? "" : obj.getString("namagudang"));
                        String barcode = (obj.getString("barcode").equals("null") ? "" : obj.getString("barcode"));
                        String bundle = (obj.getString("bundle").equals("null") ? "" : obj.getString("bundle"));
                        String slab = (obj.getString("slab").equals("null") ? "" : obj.getString("slab"));
                        String blok = (obj.getString("blok").equals("null") ? "" : obj.getString("blok"));
                        String peti = (obj.getString("peti").equals("null") ? "" : obj.getString("peti"));
                        String m2 = (obj.getString("m2").equals("null") ? "0" : obj.getString("m2"));
                        String panjang = (obj.getString("panjang").equals("null") ? "0" : obj.getString("panjang"));
                        String lebar = (obj.getString("lebar").equals("null") ? "0" : obj.getString("lebar"));
                        String tebal = (obj.getString("tebal").equals("null") ? "0" : obj.getString("tebal"));
                        String coeff1 = (obj.getString("coeff1").equals("null") ? "+" : obj.getString("coeff1"));
                        String jumlah = (obj.getString("jumlah").equals("null") ? "0" : obj.getString("jumlah"));

                        if(!kodebarangnow.equals(kodebarang))
                        {
                            if(!kodebarangnow.equals(""))
                            {
                                cell = new PdfPCell(new Phrase("TOTAL UNIT PER BLOK : " + totalunitblok, fsubheader));
                                cell.setBorder(Border.top_bottom);
                                cell.setColspan(7);
                                bodytable.addCell(cell);

                                cell = new PdfPCell(new Phrase("TOTAL M2 PER BLOK : ", fsubheader));
                                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                                cell.setBorder(Border.top_bottom);
                                cell.setColspan(3);
                                bodytable.addCell(cell);

                                cell = new PdfPCell(new Phrase(LibInspira.delimeter(String.valueOf(totalm2blok), true), fsubheader));
                                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                                cell.setBorder(Border.top_bottom);
                                bodytable.addCell(cell);

                                totalm2blok = 0.0;
                                totalunitblok = 0;

                                cell = new PdfPCell(new Phrase("TOTAL UNIT PER BARANG : " + totalunitbarang, fsubheader));
                                cell.setBorder(Border.top_bottom);
                                cell.setColspan(7);
                                bodytable.addCell(cell);

                                cell = new PdfPCell(new Phrase("TOTAL M2 PER BARANG : ", fsubheader));
                                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                                cell.setBorder(Border.top_bottom);
                                cell.setColspan(3);
                                bodytable.addCell(cell);

                                cell = new PdfPCell(new Phrase(LibInspira.delimeter(String.valueOf(totalm2barang), true), fsubheader));
                                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                                cell.setBorder(Border.top_bottom);
                                bodytable.addCell(cell);

                                totalm2barang = 0.0;
                                totalunitbarang = 0;

                                doc.add(bodytable);
                                bodytable = new PdfPTable(11);
                                bodytable.setWidths(columnWidth);
                                bodytable.setWidthPercentage(100);
                                headertable = new PdfPTable(11);
                                headertable.setWidths(columnWidth);

                                doc.newPage();
                                bloknow = "0";
                            }

                            cell = new PdfPCell(new Phrase("POSISI STOK RANDOM (DETAIL)", fheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(11);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("Per Tanggal " + LibInspira.FormatDateBasedOnInspiraDateFormat(tanggal, "dd-MM-yyyy"), f));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(11);
                            headertable.addCell(cell);

                            headertable.addCell(spacecell(11));

                            cell = new PdfPCell(new Phrase("NAMA BARANG: " + namabarang.toUpperCase(), fsubheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(7);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("GUDANG: " + namagudang.toUpperCase() + " (" + kodegudang.toUpperCase() + ")", fsubheader));
                            cell.setBorder(Border.noborder);
                            cell.setColspan(4);
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                            headertable.addCell(cell);

                            headertable.addCell(spacecell(11));

                            cell = new PdfPCell(new Phrase(" ", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            cell.setBorder(Border.top_bottom_right);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("BARCODE", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("BUNDLE", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("SLAB", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("BLOK", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("PETI", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("P", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("L", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("T", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("P x L x T", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            headertable.addCell(cell);

                            cell = new PdfPCell(new Phrase("M2", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            cell.setBorder(Border.top_bottom_left);
                            headertable.addCell(cell);

                            event.setHeader(headertable);
                            kodebarangnow = kodebarang;

                            cell = new PdfPCell(new Phrase(namabarang.toUpperCase() + " (" + kodebarang.toUpperCase() + ")", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);
                            cell.setBorder(Border.noborder);
                            cell.setColspan(11);
                            bodytable.addCell(cell);
                        }

                        if(!barcodenow.equals(barcode) || !bloknow.equals(blok) || blok.equals(""))
                        {
                            if(!bloknow.equals("0") && !bloknow.equals(blok))
                            {
                                cell = new PdfPCell(new Phrase("TOTAL UNIT PER BLOK : " + totalunitblok, fsubheader));
                                cell.setBorder(Border.top_bottom);
                                cell.setColspan(7);
                                bodytable.addCell(cell);

                                cell = new PdfPCell(new Phrase("TOTAL M2 PER BLOK : ", fsubheader));
                                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                                cell.setBorder(Border.top_bottom);
                                cell.setColspan(3);
                                bodytable.addCell(cell);

                                cell = new PdfPCell(new Phrase(LibInspira.delimeter(String.valueOf(totalm2blok), true), fsubheader));
                                cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                                cell.setBorder(Border.top_bottom);
                                bodytable.addCell(cell);

                                totalm2blok = 0.0;
                                totalunitblok = 0;
                            }

                            cell = new PdfPCell(new Phrase(" ", fsubheader));
                            cell.setBorder(Border.noborder);
                            bodytable.addCell(cell);

                            cell = new PdfPCell(new Phrase(barcode, fsubheader));
                            cell.setBorder(Border.top);
                            bodytable.addCell(cell);

                            cell = new PdfPCell(new Phrase(bundle, fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            cell.setBorder(Border.top);
                            bodytable.addCell(cell);

                            cell = new PdfPCell(new Phrase(slab, fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            cell.setBorder(Border.top);
                            bodytable.addCell(cell);

                            cell = new PdfPCell(new Phrase(blok, fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            cell.setBorder(Border.top);
                            bodytable.addCell(cell);

                            cell = new PdfPCell(new Phrase(peti, fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            cell.setBorder(Border.top);
                            bodytable.addCell(cell);

                            cell = new PdfPCell(new Phrase(" ", fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            cell.setColspan(4);
                            cell.setBorder(Border.top);
                            bodytable.addCell(cell);

                            cell = new PdfPCell(new Phrase(LibInspira.delimeter(m2, true), fsubheader));
                            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                            cell.setBorder(Border.top);
                            bodytable.addCell(cell);

                            totalm2blok = totalm2blok + Double.parseDouble(m2);
                            totalm2barang = totalm2barang + Double.parseDouble(m2);

                            bloknow = blok;
                            barcodenow = barcode;
                        }

                        cell = new PdfPCell(new Phrase(" ", f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.noborder);
                        cell.setColspan(6);
                        bodytable.addCell(cell);

                        panjang = LibInspira.delimeter(panjang, true);
                        lebar = LibInspira.delimeter(lebar, true);
                        tebal = LibInspira.delimeter(tebal, true);
                        jumlah = LibInspira.delimeter(jumlah, true);

                        if(jumlah.equals("null")) jumlah = "0";

                        if(coeff1.equals("-"))
                        {
                            panjang = "(" + panjang + ")";
                            lebar = "(" + lebar + ")";
                            tebal = "(" + tebal + ")";
                            jumlah = "(" + jumlah + ")";
                        }

                        cell = new PdfPCell(new Phrase(panjang, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.noborder);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(lebar, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.noborder);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(tebal, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.noborder);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(jumlah, f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.noborder);
                        bodytable.addCell(cell);

                        cell = new PdfPCell(new Phrase(" ", f));
                        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
                        cell.setBorder(Border.noborder);
                        bodytable.addCell(cell);

                        totalunitbarang++;
                        totalunitblok++;
                    }
                    cell = new PdfPCell(new Phrase("TOTAL UNIT PER BLOK : " + totalunitblok, fsubheader));
                    cell.setBorder(Border.top_bottom);
                    cell.setColspan(7);
                    bodytable.addCell(cell);

                    cell = new PdfPCell(new Phrase("TOTAL M2 PER BLOK : ", fsubheader));
                    cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    cell.setBorder(Border.top_bottom);
                    cell.setColspan(3);
                    bodytable.addCell(cell);

                    cell = new PdfPCell(new Phrase(LibInspira.delimeter(String.valueOf(totalm2blok), true), fsubheader));
                    cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    cell.setBorder(Border.top_bottom);
                    bodytable.addCell(cell);

                    cell = new PdfPCell(new Phrase("TOTAL UNIT PER BARANG : " + totalunitbarang, fsubheader));
                    cell.setBorder(Border.top_bottom);
                    cell.setColspan(7);
                    bodytable.addCell(cell);

                    cell = new PdfPCell(new Phrase("TOTAL M2 PER BARANG : ", fsubheader));
                    cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    cell.setBorder(Border.top_bottom);
                    cell.setColspan(3);
                    bodytable.addCell(cell);

                    cell = new PdfPCell(new Phrase(LibInspira.delimeter(String.valueOf(totalm2barang), true), fsubheader));
                    cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
                    cell.setBorder(Border.top_bottom);
                    bodytable.addCell(cell);

                    doc.add(bodytable);
                }
                Toast.makeText(fragmentActivity, "PDF Created", Toast.LENGTH_LONG).show();
                displaypdf(file);
            } catch (DocumentException de) {
                Log.e("PDFCreator", "DocumentException:" + de);
            } finally {
                doc.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class TableHeader extends PdfPageEventHelper {
        PdfPTable header;
        PdfTemplate total;

        public void setHeader(PdfPTable header) {
            this.header = header;
        }

        public void onOpenDocument(PdfWriter writer, Document document) {
            total = writer.getDirectContent().createTemplate(30, 16);
        }

        public void onEndPage(PdfWriter writer, Document document) {
            PdfPTable table = new PdfPTable(1);
            try {
                table.setWidths(new int[]{100});
                table.setTotalWidth(527);
                table.setLockedWidth(true);
                table.getDefaultCell().setBorder(Border.noborder);

                table.addCell(header);
                table.writeSelectedRows(0, -1, 34, 803, writer.getDirectContent());
            }
            catch(DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }

        public void onCloseDocument(PdfWriter writer, Document document) {
            ColumnText.showTextAligned(total, Element.ALIGN_LEFT,
                    new Phrase(String.valueOf(writer.getPageNumber() - 1)), 2, 2, 0);
        }
    }

    public static class Border
    {
        static int noborder = 0;
        static int top = 1;
        static int bottom = 2;
        static int top_bottom = 3;
        static int left = 4;
        static int top_left = 5;
        static int bottom_left = 6;
        static int top_bottom_left = 7;
        static int right = 8;
        static int top_right = 9;
        static int bottom_right = 10;
        static int top_bottom_right = 11;
        static int left_right = 12;
        static int top_left_right = 13;
        static int bottom_left_right = 14;
        static int full = 15;
    }
}
