package com.inspira.gms;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.animation.AlphaAnimation;

/**
 * Created by Tonny on 7/24/2017.
 */

public class GlobalVar {
    public static SharedPreferences sharedpreferences;
    public static SharedPreferences temppreferences;  //added by ADI @19-Aug-2017 //buat data-data yang cuma sementara  //boleh di clear kapan aja
    public static SharedPreferences userpreferences;
    public static SharedPreferences rolepreferences;
    public static SharedPreferences notifpreferences;
    public static SharedPreferences salespreferences;  //added by Tonny @01-Aug-2017
    public static SharedPreferences datapreferences;
    public static SharedPreferences settingpreferences;  //added by Tonny @03-Aug-2017
    public static SharedPreferences salestargetpreferences;  //added by Tonny @07-Aug-2017
    public static SharedPreferences stockmonitoringpreferences;  //added by Tonny @18-Aug-2017
    public static SharedPreferences omzetpreferences;  //added by Tonny @18-Aug-2017
    public static SharedPreferences schedulepreferences;

    public static User user;
    public static Sales sales;  //added by Tonny @01-Aug-2017
    public static Data data;
    public static Shared shared;
    public static Settings settings;  //added by Tonny @03-Aug-2017
    public static Stock stock;  //added by Tonny @18-Aug-2017
    public static Temp temp; //added by ADI @20-Aug-2017
    public static Omzet omzet;  //added by Tonny @23-Aug-2017
    public static Schedule schedule; //added by shodiq @1-sep-2017

    public static AlphaAnimation buttoneffect = new AlphaAnimation(1F, 0.8F);
    public static AlphaAnimation listeffect = new AlphaAnimation(1F, 0.5F);

    public static String webserviceURL = "/wsGMS/gms/index.php/api/";

    public static String folder = "/GMS"; //added by ADI @01-Sep-2017
    public static String folderPDF = folder + "/PDF"; //added by ADI @01-Sep-2017

        public GlobalVar(Context context)
        {
            sharedpreferences = context.getSharedPreferences("global", Context.MODE_PRIVATE);
            temppreferences = context.getSharedPreferences("temp", Context.MODE_PRIVATE); //added by ADI @20-Aug-2017
            userpreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
            rolepreferences = context.getSharedPreferences("role", Context.MODE_PRIVATE);
            notifpreferences = context.getSharedPreferences("notif", Context.MODE_PRIVATE);
            salespreferences = context.getSharedPreferences("sales", Context.MODE_PRIVATE);  //added by Tonny @01-Aug-2017
            datapreferences = context.getSharedPreferences("data", Context.MODE_PRIVATE);
            settingpreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);  //added by Tonny @03-Aug-2017
            stockmonitoringpreferences = context.getSharedPreferences("stock", Context.MODE_PRIVATE);  //added by Tonny @18-Aug-2017
            omzetpreferences = context.getSharedPreferences("omzet", Context.MODE_PRIVATE);  //added by Tonny @25-Aug-2017
            schedulepreferences = context.getSharedPreferences("schedule", Context.MODE_PRIVATE); //added by shodiq @1-sep-2017

            data = new Data();
            user = new User();
            sales = new Sales();  //added by Tonny @01-Aug-2017
            shared = new Shared();
            settings = new Settings();  //added by Tonny @03-Aug-2017
            stock = new Stock();  //added by Tonny @18-Aug-2017
            temp = new Temp(); //added by ADI @20-Aug-2017
            omzet = new Omzet();  //added by Tonny @23-Aug-2017
            schedule = new Schedule(); //added by shodiq @1-sep-2017
        }

        public static void clearDataUser()
        {
            LibInspira.clearShared(userpreferences);
            LibInspira.clearShared(rolepreferences);
            LibInspira.clearShared(notifpreferences);
            LibInspira.clearShared(salespreferences);  //added by Tonny @01-Aug-2017
            LibInspira.clearShared(settingpreferences); //added by Tonny @03-Aug-2017
        }

        public class Shared
        {
            public String server = "server";
            public String position = "position";  //added by Tonny @07-Aug-2017
            public String namasales = "namasales"; //added by Tonny @07-Aug-2017
            public String nomorsales = "nomorsales"; //added by Tonny @07-Aug-2017
            public String periode = "periode"; //added by Tonny @07-Aug-2017 digunakan pada SalesTargetMonthly
            public String tahun = "tahun"; //added by Tonny @07-Aug-2017 digunakan pada SalesTargetMonthly
        }

        //untuk shared preferences yang boleh di clean tiap saat
        public class Temp
        {
            public String scheduletask_date = "scheduletask_date";
            public String scheduletask_time = "scheduletask_time";
            public String scheduletask_reminder = "scheduletask_reminder";
            public String scheduletask_description = "scheduletask_description";
            public String scheduletask_type = "scheduletask_type";

            public String salesorder_type_proyek = "salesorder_type_proyek"; //added by ADI @13-Sep-2017
            public String salesorder_type_task = "salesorder_type_task"; //added by ADI @13-Sep-2017
            public String salesorder_type = "salesorder_type"; //added by ADI @13-Sep-2017

            public String salesorder_disc = "salesorder_disc"; //added by Tonny @06-Sep-2017
            public String salesorder_disc_nominal = "salesorder_disc_nominal"; //added by Tonny @06-Sep-2017
            public String salesorder_ppn = "salesorder_ppn"; //added by Tonny @06-Sep-2017
            public String salesorder_ppn_nominal = "salesorder_ppn_nominal"; //added by Tonny @06-Sep-2017
            public String salesorder_date = "salesorder_date"; //added by Tonny @30-Aug-2017
            public String salesorder_subtotal = "salesorder_subtotal"; //added by Tonny @06-Sep-2017
            public String salesorder_subtotal_fee = "salesorder_subtotal_fee";  //added by Tonny @06-Sep-2017
            public String salesorder_import = "salesorder_import"; //added by Tonny @06-Sep-2017
            public String salesorder_total = "salesorder_total"; //added by Tonny @06-Sep-2017

            public String salesorder_customer_nomor = "salesorder_customer_nomor"; //added by ADI @24-Aug-2017
            public String salesorder_customer_kode = "salesorder_customer_kode"; //added by Tonny @05-Sep-2017
            public String salesorder_customer_nama = "salesorder_customer_nama"; //added by ADI @24-Aug-2017
            public String salesorder_broker_nomor = "salesorder_broker_nomor"; //added by ADI @24-Aug-2017
            public String salesorder_broker_kode = "salesorder_broker_kode"; //added by Tonny @05-Sep-2017
            public String salesorder_broker_nama = "salesorder_broker_nama"; //added by ADI @24-Aug-2017
            public String salesorder_valuta_nomor = "salesorder_valuta_nomor"; //added by ADI @24-Aug-2017
            public String salesorder_valuta_nama = "salesorder_valuta_nama"; //added by ADI @24-Aug-2017
            public String salesorder_valuta_kurs = "salesorder_valuta_kurs"; //added by ADI @24-Aug-2017
            public String salesorder_proyek_nomor = "salesorder_proyek_nomor"; //added by ADI @24-Aug-2017
            public String salesorder_proyek_nama = "salesorder_proyek_nama"; //added by ADI @24-Aug-2017
            public String salesorder_proyek_kode = "salesorder_proyek_kode"; //added by ADI @24-Aug-2017
            public String salesorder_jenis = "salesorder_jenis"; //added by ADI @14-Sep-2017
            public String salesorder_perhitungan_barang_custom = "salesorder_perhitungan_barang_custom"; //added by ADI @14-Sep-2017

            public String salesorder_pekerjaan = "salesorder_pekerjaan"; //added by Tonny @02-Sep-2017 nomorbarang~kodebarang~namabarang~satuan~price~qty~fee~disc~subtotal~notes
            public String salesorder_pekerjaan_index = "salesorder_pekerjaan_index"; //added by Tonny @02-Sep-2017
            public String salesorder_pekerjaan_nomor = "salesorder_pekerjaan_nomor"; //added by Tonny @02-Sep-2017
            public String salesorder_pekerjaan_nama = "salesorder_pekerjaan_nama"; //added by Tonny @02-Sep-2017
            public String salesorder_pekerjaan_kode = "salesorder_pekerjaan_kode"; //added by Tonny @02-Sep-2017
            public String salesorder_pekerjaan_satuan = "salesorder_pekerjaan_satuan"; //added by Tonny @02-Sep-2017
            public String salesorder_pekerjaan_price = "salesorder_pekerjaan_price"; //added by Tonny @02-Sep-2017
            public String salesorder_pekerjaan_qty = "salesorder_pekerjaan_qty"; //added by Tonny @02-Sep-2017
            public String salesorder_pekerjaan_fee = "salesorder_pekerjaan_fee"; //added by Tonny @02-Sep-2017
            public String salesorder_pekerjaan_disc = "salesorder_pekerjaan_disc"; //added by Tonny @02-Sep-2017
            public String salesorder_pekerjaan_subtotal = "salesorder_pekerjaan_subtotal"; //added by Tonny @05-Sep-2017
            public String salesorder_pekerjaan_notes = "salesorder_pekerjaan_notes"; //added by Tonny @02-Sep-2017

            public String salesorder_item = "salesorder_item"; //added by Tonny @01-Sep-2017 nomorbarang~kodebarang~namabarang~satuan~price~qty~fee~disc~subtotal~notes
            public String salesorder_item_index = "salesorder_item_index"; //added by ADI @24-Aug-2017
            public String salesorder_item_nomor_real = "salesorder_item_nomor_real"; //added by ADI @24-Aug-2017
            public String salesorder_item_nama_real = "salesorder_item_nama_real"; //added by ADI @24-Aug-2017
            public String salesorder_item_kode_real = "salesorder_item_kode_real"; //added by ADI @24-Aug-2017
            public String salesorder_item_nomor = "salesorder_item_nomor"; //added by ADI @24-Aug-2017
            public String salesorder_item_nama = "salesorder_item_nama"; //added by ADI @24-Aug-2017
            public String salesorder_item_kode = "salesorder_item_kode"; //added by ADI @24-Aug-2017
            public String salesorder_item_satuan = "salesorder_item_satuan"; //added by ADI @24-Aug-2017
            public String salesorder_item_price = "salesorder_item_price"; //added by ADI @24-Aug-2017
            public String salesorder_item_qty = "salesorder_item_qty"; //added by ADI @24-Aug-2017
            public String salesorder_item_fee = "salesorder_item_fee"; //added by ADI @24-Aug-2017
            public String salesorder_item_disc = "salesorder_item_disc"; //added by ADI @24-Aug-2017
            public String salesorder_item_subtotal = "salesorder_item_subtotal"; //added by Tonny @05-Sep-2017
            public String salesorder_item_notes = "salesorder_item_notes"; //added by Tonny @01-Sep-2017

            //added by Tonny @04-Sep-2017 untuk mengecek apakah salesorder yg akan diinputkan termasuk ppn atau bukan
            public String salesorder_isPPN = "salesorder_isPPN";

            //added by Tonny @16-Sep-2017 untuk menampung nomor sales order yang dipilih dari list (digunakan pada approval)
            public String salesorder_selected_list_nomor = "salesorder_selected_list_nomor";

            //added by Tonny @17-Sep-2017 untuk menampung data summary sementara  //tanggal~namacustomer~namabroker~valuta~subtotal~disc~discnominal~ppn~ppnnominal~total
            public String salesorder_summary = "salesorder_summary";
        }

        public class User
        {
            public String nomor = "nomor";
            public String password = "password";  //added by Tonny @30-Jul-2017
            public String nomor_android = "nomor_android";
            public String nomor_sales = "nomor_sales";
            public String kode_sales = "kode_sales"; //added by Tonny @05-Sep-2017
            public String nama = "nama";
            public String tipe = "tipe";
            public String role = "role";
            public String hash = "hash";
            public String token = "token";
            public String cabang = "cabang"; //added by ADI @07-Aug-2017
            public String namacabang = "namacabang"; //added by Tonny @06-Sep-2017
            public String telp = "telp"; //added by ADI @12-Sep-2017
            public String role_isowner = "role_isowner"; //role untuk mengetahui user termasuk owner atau tidak (0/1)
            public String role_issales = "role_issales"; //role untuk mengetahui user termasuk sales atau tidak (0/1)
            public String role_setting = "role_setting"; //role apakah user dapat melakukan setting atau tidak (0/1)
            public String role_settingtarget = "role_settingtarget"; //role apakah user dapat melakukan setting target atau tidak (0/1)
            public String role_salesorder = "role_salesorder"; //role apakah user dapat melakukan sales order atau tidak (0/1)
            public String role_stockmonitoring = "role_stockmonitoring"; //role apakah user dapat melihat stok atau tidak (0/1)
            public String role_pricelist = "role_pricelist"; //role apakah user dapat melihat pricelist atau tidak (0/1)
            public String role_addscheduletask = "role_addscheduletask"; //role apakah user dapat melihat pricelist atau tidak (0/1)
            public String role_salestracking = "role_salestracking"; //role apakah user dapat melihat trangking dari sales atau tidak (0/1)
            public String role_hpp = "role_hpp"; //role apakah user dapat melihat HPP dari barang (0/1)
            public String role_crossbranch = "role_crossbranch"; //role apakah user dapat melihat data dari cabang lain (0/1)
            public String role_creategroup = "role_creategroup"; //role apakah user dapat membuat dan mengedit (0/1)
        }

        public class Data
        {
            public String user = "user"; // nomor~nama~location~hp
            public String barang = "barang"; // nomor~nama~namajual~kode~satuan~hargajual~tambang~import
            public String pekerjaan = "pekerjaan";  // nomor~kode~nama~kodesatuan~satuan~hargacustomer~hargamandor  //added by Tonny @02-Sep-2017
            public String users = "users"; // nomor~nama //added by Shodiq @3-Sep-2017
            public String groups = "groups"; // nomor~nama //added by Shodiq @3-Sep-2017
            public String schedule = "schedule";
            public String customer = "customer"; // nomor~nama~alamat~telpon~kode
            public String customerprospecting = "customerprospecting"; // nomor~nama~alamat~telpon
            public String broker = "broker"; // nomor~nama~kode
            public String valuta = "valuta"; // nomor~nama~kurs~kode
            public String kota = "kota"; // nomor~nama~nomorpropinsi~kode
            public String proyek = "proyek"; // nomor~nama~alamat~kode
            public String lokasi = "lokasi"; // nomor~nama~gudang~kode //added by ADI @08-Sep-2017

            public String kategori = "kategori"; // nomor~nama~kode
            public String gudang = "gudang"; // nomor~nama~kode~alamat~kota
            public String bentuk = "bentuk"; // nomor~nama~kode
            public String surface = "surface"; // nomor~nama~kode
            public String jenis = "jenis"; // nomor~nama~kode
            public String grade = "grade"; // nomor~nama~kode

            public String periode = "periode"; // bulan~tahun
            public String salesman = "salesman"; // nomorsales~nama   //added by Tonny @05-Aug-2017
            public String salesmanmonthly = "salesmanmonthly";  // nomorsales-nama-target  //added by Tonny @07-Aug-2017
            public String cabang = "cabang";  // nomorcabang~namacabang //added by Tonny @08-Aug-2017
            public String price = "price"; //nomor~kode~nama~harga
            public String pricehpp = "pricehpp"; //nomor~kode~nama~harga~hpp

            //UNTUK MENYIMPAN STOCKPOSISI DAN STOCKPOSISIRANDOM
            public String stockPosisi = "stockposisi";  //nomorgudang~namagudang~nomorbarang~namabarang~satuan~qty~m2 //added by Tonny @20-Aug-2017

            //UNTUK MENYIMPAN DATA SALESMAN OMZET
            public String salesmanomzet = "salesmanomzet";  //nomorsales~namasales~omzet~tanggal  //added by Tonny @25-Aug-2017

            //UNTUK MENYIMPAN DATA FORMATSETTING SALESORDER
            public String salesorder_formatsetting = "salesorder_formatsetting";  //prefix,length,YYMM,ttransaksi,tdorderjual  //added by Tonny @04-Sep-2017

            public String salesorder_prefix_kode = "salesorder_prefix_kode";  //added by Tonny @04-Sep-2017
            public String salesorder_length_kode = "salesorder_length_kode";  //added by Tonny @04-Sep-2017
            public String salesorder_formatdate_kode = "salesorder_formatdate_kode";  //added by Tonny @04-Sep-2017
            public String salesorder_header_kode = "salesorder_header_kode";  //added by Tonny @04-Sep-2017
            public String salesorder_detail_kode = "salesorder_detail_kode";  //added by Tonny @04-Sep-2017

            public String salesorder_nomorurut_kode = "salesorder_nomorurut_kode";  //added by Tonny @04-Sep-2017

            //UNTUK MENYIMPAN DATA SALES ORDER HEADER

            //UNTUK MENYIMPAN DATA SALES ORDER LIST ITEM
            public String salesorder_list_item = "salesorder_list_item";  //kode~tanggal~nomorcabang~cabang~nomorcustomer~kodecustomer~namacustomer  //added by Tonny @01-Sep-2017

            public String selectedUsers = "users"; // nomor~nama //added by Shodiq @8-Sep-2017
            public String selectedGroup = "group"; // nomor~nama //added by Shodiq @9-Sep-2017
        }

        public class Sales  //added by Tonny @01-Aug-2017
        {
            public String target = "target";
            public String omzet = "omzet";
        }

        public class Settings  //added by Tonny @03-Aug-2017
        {
            public String interval = "interval";
            public String radius = "radius";
            public String tracking = "tracking";
            public String jam_awal = "jam_awal";
            public String jam_akhir = "jam_akhir";
        }

        //untuk menampung value filter stock yang sudah dipilih
        public class Stock //added by Tonny @18-Aug-2017
        {
            public String kategori = "kategori";  //added by Tonny @18-Aug-2017
            public String bentuk = "bentuk";  //added by Tonny @19-Aug-2017
            public String jenis = "jenis";  //added by Tonny @19-Aug-2017
            public String grade = "grade";  //added by Tonny @19-Aug-2017
            public String surface = "surface";  //added by Tonny @19-Aug-2017
            public String lokasi = "lokasi";  //added by Tonny @19-Aug-2017
            public String kodegudang = "kodegudang";  //added by Tonny @20-Aug-2017
            public String namagudang = "namagudang";  //added by Tonny @20-Aug-2017
            public String kodebarang = "kodebarang";  //added by Tonny @20-Aug-2017
            public String namabarang = "namabarang";  //added by Tonny @20-Aug-2017
            public String nomorbarang = "nomorbarang";  //added by Tonny @20-Aug-2017
            public String ukuran = "ukuran";  //aaxbb //added by Tonny @20-Aug-2017
            public String tebal = "tebal";  //added by Tonny @20-Aug-2017
            public String motif = "motif";  //added by Tonny @20-Aug-2017
            public String blok = "blok"; //added by ADI @04-Sep-2017
            public String tanggalakhir = "tanggalakhir"; //yyyy-MM-dd //added by Tonny @20-Aug-2017 //modified by ADI @08-Sep-2017
            public String tanggalawal = "tanggalawal"; //yyyy-MM-dd //added by ADI @08-Sep-2017
        }

        //untuk menampung value filter omzet yang sudah dipilih
        public class Omzet //added by Tonny @23-Aug-2017
        {
            public String nomorsales  = "nomorsales";
            public String enddate = "enddate";
            public String bulantahun = "bulantahun";
        }

        public class Schedule
        {
            public String targetsch = "target"; //view only
            public String targetIDsch = "targetID";
            public String customersch = "customer"; //view only
            public String customerIDsch = "customerID";
            public String customerProspectingsch = "customerProspectingsch"; //view only
            public String customerProspectingIDsch = "customerProspectingIDsch";
            public String groupsch = "group";
            public String groupIDsch = "groupID";
            public String datesch = "date";
            public String timesch = "time";
            public String typesch = "type";
            public String remindersch = "reminder";
            public String descriptionsch = "description";
        }

}
