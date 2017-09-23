<?php

defined('BASEPATH') OR exit('No direct script access allowed');

// This can be removed if you use __autoload() in config.php OR use Modular Extensions
require APPPATH . '/libraries/REST_Controller.php';

/**
 * This is an example of a few basic event interaction methods you could use
 * all done with a hardcoded array
 *
 * @package         CodeIgniter
 * @subpackage      Rest Server
 * @category        Controller
 * @author          Phil Sturgeon, Chris Kacerguis
 * @license         MIT
 * @link            https://github.com/chriskacerguis/codeigniter-restserver
 */
class Order extends REST_Controller {

    function __construct()
    {
        // Construct the parent class
        parent::__construct();

        // Configure limits on our controller methods
        // Ensure you have created the 'limits' table and enabled 'limits' within application/config/rest.php
        $this->methods['event_post']['limit'] = 500000000; // 500 requests per hour per event/key
        // $this->methods['event_delete']['limit'] = 50; // 50 requests per hour per event/key
        $this->methods['event_get']['limit'] = 500000000; // 500 requests per hour per event/key

        header("Access-Control-Allow-Origin: *");
        header("Access-Control-Allow-Methods: GET, POST");
        header("Access-Control-Allow-Headers: Origin, Content-Type, Accept, Authorization");
    }

	function ellipsis($string) {
        $cut = 30;
        $out = strlen($string) > $cut ? substr($string,0,$cut)."..." : $string;
        return $out;
    }
	
    function clean($string) {
        return preg_replace("/[^[:alnum:][:space:]]/u", '', $string); // Replaces multiple hyphens with single one.
    }

    function error($string) {
        return str_replace( array("\t", "\n", "\r") , "", $string);
    }

    public function send_gcm($registrationId,$message,$title,$fragment,$nomor,$nama)
    {
        $this->load->library('gcm');

        $this->gcm->setMessage($message);
        $this->gcm->setTitle($title);
        $this->gcm->setFragment($fragment);
        $this->gcm->setNomor($nomor);
        $this->gcm->setNama($nama);

        $this->gcm->addRecepient($registrationId);

        $this->gcm->setData(array(
            'some_key' => 'some_val'
        ));

        $this->gcm->setTtl(500);
        $this->gcm->setTtl(false);

        $this->gcm->setGroup('Test');
        $this->gcm->setGroup(false);

        $this->gcm->send();

       if ($this->gcm->send())
           echo 'Success for all messages';
       else
           echo 'Some messages have errors';

       print_r($this->gcm->status);
       print_r($this->gcm->messagesStatuses);

        die(' Worked.');
    }

	public function send_gcm_group($registrationId,$message,$title,$fragment,$nomor,$nama)
    {
        $this->load->library('gcm');

        $this->gcm->setMessage($message);
        $this->gcm->setTitle($title);
        $this->gcm->setFragment($fragment);
        $this->gcm->setNomor($nomor);
        $this->gcm->setNama($nama);

        foreach ($registrationId as $regisID) {
            $this->gcm->addRecepient($regisID);
        }

        $this->gcm->setTtl(500);
        $this->gcm->setTtl(false);

        $this->gcm->setGroup('Test');
        $this->gcm->setGroup(false);

        $this->gcm->send();
    }

    //added by Tonny
    //untuk mendapatkan nomor salesorder header yg sekarang
    function getNomorCountHeader_post()
    {
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        $kodecount = (isset($jsonObject["kodecount"]) ? $this->clean($jsonObject["kodecount"])     : "");
        $query = "SELECT akhir counter FROM tcount WHERE kode = '$kodecount'";
        $result = $this->db->query($query);
        if($result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'counter'		=> $r['counter']
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }
    }

	// --- POST insert new order jual --- //
	//added by Tonny
	function insertNewOrderJual_post()
	{
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        ////////////////////////////////////////////////////////////////////////////////HEADER/////////////////////////////////////////////////////////////////////////////
        $nomorcustomer = (isset($jsonObject["nomorcustomer"]) ? $this->clean($jsonObject["nomorcustomer"])     : "");
        $kodecustomer = (isset($jsonObject["kodecustomer"]) ? $this->clean($jsonObject["kodecustomer"])     : "");
        $nomorbroker = (isset($jsonObject["nomorbroker"]) ? $this->clean($jsonObject["nomorbroker"])     : "");
        $kodebroker = (isset($jsonObject["kodebroker"]) ? $this->clean($jsonObject["kodebroker"])     : "");
        $nomorsales  = (isset($jsonObject["nomorsales"]) ? $this->clean($jsonObject["nomorsales"])     : "");
        $kodesales = (isset($jsonObject["kodesales"]) ? $this->clean($jsonObject["kodesales"])     : "");
        $subtotal = (isset($jsonObject["subtotal"]) ? $this->clean($jsonObject["subtotal"])     : "");
        $subtotaljasa = (isset($jsonObject["subtotaljasa"]) ? $this->clean($jsonObject["subtotaljasa"])     : "");
        $subtotalbiaya = (isset($jsonObject["subtotalbiaya"]) ? $this->clean($jsonObject["subtotalbiaya"])     : "");
        $disc = (isset($jsonObject["disc"]) ? $this->clean($jsonObject["disc"])     : "0");
        $discnominal = (isset($jsonObject["discnominal"]) ? $this->clean($jsonObject["discnominal"])     : "0");
        $dpp = (isset($jsonObject["dpp"]) ? $this->clean($jsonObject["dpp"])     : "0");
        $ppn = (isset($jsonObject["ppn"]) ? $this->clean($jsonObject["ppn"])     : "0");
        $ppnnominal = (isset($jsonObject["ppnnominal"]) ? $this->clean($jsonObject["ppnnominal"])     : "");
        $total = (isset($jsonObject["total"]) ? $this->clean($jsonObject["total"])     : "");
        $totalrp = (isset($jsonObject["totalrp"]) ? $this->clean($jsonObject["totalrp"])     : "");
        $pembuat = (isset($jsonObject["pembuat"]) ? $this->clean($jsonObject["pembuat"])     : "");
        $nomorcabang = (isset($jsonObject["nomorcabang"]) ? $this->clean($jsonObject["nomorcabang"])     : "");
        $cabang = (isset($jsonObject["cabang"]) ? $this->clean($jsonObject["cabang"])     : "");
        $valuta = (isset($jsonObject["valuta"]) ? $this->clean($jsonObject["valuta"])     : "");
        $kurs = (isset($jsonObject["kurs"]) ? $this->clean($jsonObject["kurs"])     : "");
        $jenispenjualan = (isset($jsonObject["jenispenjualan"]) ? $this->clean($jsonObject["jenispenjualan"])     : "");
        $isbarangimport = (isset($jsonObject["isbarangimport"]) ? $this->clean($jsonObject["isbarangimport"])     : "");
        $isppn = (isset($jsonObject["isppn"]) ? $this->clean($jsonObject["isppn"])     : "");
        $proyek = (isset($jsonObject["proyek"]) ? $this->clean($jsonObject["proyek"])     : "");
        $user = (isset($jsonObject["user"]) ? $this->clean($jsonObject["user"])     : "");

		$this->db->trans_begin();

        $query = "INSERT INTO thorderjual (
                    Tanggal,
                    NomorCustomer,
                    KodeCustomer,
                    NomorBroker,
                    KodeBroker,
                    NomorSales,
                    KodeSales,
                    SubTotal,
                    SubtotalJasa,
                    SubtotalBiaya,
                    Disc,
                    DiscNominal,
                    DPP,
                    PPN,
                    PPNNominal,
                    Total,
                    TotalRp,
                    Pembuat,
                    NomorCabang,
                    Cabang,
                    Booking,
                    Valuta,
                    Kurs,
                    JenisPenjualan,
                    Proyek,
                    IsBarangImport,
                    IsPPN,
                    status,
                    dibuat_oleh,
                    dibuat_pada)
                  VALUES (
                    NOW(),
                    $nomorcustomer,
                    '$kodecustomer',
                    $nomorbroker,
                    '$kodebroker',
                    $nomorsales,
                    '$kodesales',
                    $subtotal,
                    $subtotaljasa,
                    $subtotalbiaya,
                    $disc,
                    $discnominal,
                    $dpp,
                    $ppn,
                    $ppnnominal,
                    $total,
                    $totalrp,
                    '$pembuat',
                    $nomorcabang,
                    '$cabang',
                    0,
                    '$valuta',
                    $kurs,
                    '$jenispenjualan',
                    $proyek,
                    $isbarangimport,
                    $isppn,
                    1,
                    $user,
                    NOW())";
        $this->db->query($query);

        $interval  = $this->db->query("SELECT intnilai FROM whsetting_mobile WHERE intNomor = 1 LIMIT 1")->row()->intnilai;
        $rows =  $this->db->insert_id();
        array_push($data['data'], array( 'query' => $rows ));
        $nomor = $rows['nomor'];

        ////////////////////////////////////////////////////////////////////////////////DETAIL/////////////////////////////////////////////////////////////////////////////
        $noitem = FALSE;
        $dataitemdetail = (isset($jsonObject["dataitemdetail"]) ? $jsonObject["dataitemdetail"]     : "");
        $datapekerjaandetail = (isset($jsonObject["datapekerjaandetail"]) ? $jsonObject["datapekerjaandetail"]     : "");

        $pieces = explode('|', $dataitemdetail);
        if (count($pieces) > 1){
            for ($i = 0; $i < count($pieces); $i++) {
                if($pieces[$i]!="")
                {
                    $parts = explode("~", $pieces[$i]);
                    //nomorbarang~kodebarang~namabarang~nomorbarangreal~kodebarangreal~namabarangreal~satuan~harga~qty~fee~disc~subtotal~notes
                    $nomorheader = $nomor;
                    $nomorbarang = $parts[0];
                    $kodebarang = $parts[1];
                    $nomorbarangreal = $parts[3];
                    $kodebarangreal = $parts[4];
                    $qty = $parts[8];
                    $jumlah = $parts[8];
                    $harga = $parts[7];
                    $fee = $parts[9];
                    $hargamandor = $parts[9];
                    $disc1 = $parts[10];
                    $disc1nominal = $harga * $disc1 / 100 ;
                    $netto = $harga - $disc1nominal + $fee;
                    $subtotald = $parts[11];
                    $nomorbarangjual = $nomorbarang;
                    $kodebarangjual = $kodebarang;
                    $keterangandetail = $parts[12];

                    //insert barang
                    $query = "INSERT INTO tdorderjual (
                                NomorHeader,
                                NomorBarang,
                                KodeBarang,
                                Qty,
                                Jumlah,
                                Harga,
                                Fee,
                                HargaMandor,
                                Disc1,
                                Disc1Nominal,
                                Disc2,
                                Disc2Nominal,
                                Netto,
                                Subtotal,
                                NomorPekerjaan,
                                KodePekerjaan,
                                NomorBarangJual,
                                KodeBarangJual,
                                KeteranganDetail,
                                dibuat_oleh,
                                dibuat_pada)
                              VALUES (
                                $nomorheader,
                                $nomorbarangreal,
                                '$kodebarangreal',
                                $qty,
                                $jumlah,
                                $harga,
                                $fee,
                                $hargamandor,
                                $disc1,
                                $disc1nominal,
                                0,
                                0,
                                $netto,
                                $subtotald,
                                0,
                                '',
                                $nomorbarangjual,
                                '$kodebarangjual',
                                '$keterangandetail',
                                $user,
                                NOW())";
                    $this->db->query($query);
                }
            }
        }

        $pieces = explode('|', $datapekerjaandetail);
        if (count($pieces) > 1){
            for ($i = 0; $i < count($pieces); $i++) {
                if($pieces[$i]!="")
                {
                    $parts = explode("~", $pieces[$i]);  //nomorbarang~kodebarang~namabarang~satuan~harga~qty~fee~disc~subtotal~notes
                    array_push($data['data'], array( 'query' => $parts ));
                    $nomorheader = $nomor;
                    $nomorpekerjaan = $parts[0];
                    $kodepekerjaan = $parts[1];
                    $qty = $parts[5];
                    $jumlah = $parts[5];
                    $harga = $parts[4];
                    $fee = 0;
                    $hargamandor = 0;
                    $disc1 = 0;
                    $disc1nominal = 0;
                    $subtotald = $parts[8];
                    $keterangandetail = $parts[9];
                    $netto = $harga - $disc1nominal + $fee;

                    //insert pekerjaan
                    $query = "INSERT INTO tdorderjual (
                                NomorHeader,
                                NomorPekerjaan,
                                KodePekerjaan,
                                Qty,
                                Jumlah,
                                Harga,
                                Fee,
                                HargaMandor,
                                Disc1,
                                Disc1Nominal,
                                Disc2,
                                Disc2Nominal,
                                Netto,
                                Subtotal,
                                KeteranganDetail,
                                Jasa,
                                dibuat_oleh,
                                dibuat_pada)
                              VALUES (
                                $nomorheader,
                                $nomorpekerjaan,
                                '$kodepekerjaan',
                                $qty,
                                $jumlah,
                                $harga,
                                $fee,
                                $hargamandor,
                                $disc1,
                                $disc1nominal,
                                0,
                                0,
                                $netto,
                                $subtotald,
                                '$keterangandetail',
                                1,
                                $user,
                                NOW())";
                    $this->db->query($query);
                }
            }
        }

        if ($this->db->trans_status() === FALSE)
        {
            $this->db->trans_rollback();
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => 'true' ));
        }
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

	//untuk menampilkan data di salesorder list
	function getSalesOrderList_post(){
		$data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		$nomorsales = (isset($jsonObject["nomorsales"]) ? $this->clean($jsonObject["nomorsales"])     : "");
		$approve = (isset($jsonObject["approve"]) ? $this->clean($jsonObject["approve"])     : "0");
		$kode = (isset($jsonObject["kode"]) ? $jsonObject["kode"]     : "");
		$cabang = (isset($jsonObject["cabang"]) ? $this->clean($jsonObject["cabang"])     : "");

        if($nomorsales!="") $nomorsales = " AND a.nomorsales = '" . $nomorsales . "'";

        $query = "SELECT
                  a.Nomor nomor,
                  a.Kode kode,
                  a.Tanggal tanggal,
                  a.NomorCabang nomorcabang,
                  a.Cabang cabang,
                  a.NomorCustomer nomorcustomer,
                  a.KodeCustomer kodecustomer,
                  b.nama namacustomer
                  FROM thorderjual a
                  JOIN tcustomer b
                    ON b.nomor = a.nomorcustomer
                  WHERE a.status = 1
                    $nomorsales
                    AND a.approve = $approve
                    AND a.NomorCabang = $cabang
                    AND a.kode REGEXP '$kode'";

        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode'					=> $r['kode'],
                                                'tanggal' 			    => $r['tanggal'],
                                                'nomorcabang' 			=> $r['nomorcabang'],
                                                'cabang' 			    => $r['cabang'],
                                                'nomorcustomer' 	    => $r['nomorcustomer'],
                                                'kodecustomer' 			=> $r['kodecustomer'],
                                                'namacustomer' 			=> $r['namacustomer']
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
	}

    //added by Tonny
    // untuk mendapatkan format setting untuk sales order
    //output -> [prefix],[length nomor urut],[format bulan tahun],[header transaksi],[detail transaksi] -> 'SOP,5,YYMM,TTRANSAKSI,TDORDERJUAL'
    function getFormatSettingSalesOrder_post(){
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        $nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"])     : "219");
        $query = "SELECT formatsetting FROM tformatsetting WHERE nomor = $nomor";
        $result = $this->db->query($query);
        if($result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'formatsetting'			=> $r['formatsetting']
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    //added by Tonny
    //untuk mendapatkan nomor baru untuk salesorder header
    function getCounterHeader_post(){
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        $prefix = (isset($jsonObject["prefix"]) ? $this->clean($jsonObject["prefix"])     : "SOP");  //input merupakan kodetransaksi
        //$query = "SELECT MAX(SUBSTR(kode, LOCATE('/', kode, 8) + 1)) AS counter FROM thorderjual WHERE kode LIKE '%$prefix%'";
        $query = "SELECT MAX(SUBSTR(kode, LOCATE('/', kode, LOCATE('/', kode) + 1) + 1)) AS counter FROM thorderjual WHERE kode LIKE '%SOP%'";
        $result = $this->db->query($query);
        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'counter'	=> $r['counter']
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    //added by Tonny
    //untuk mendapatkan nomor baru untuk salesorder detail
    function getNomorHeader($_headerkode){
        $result = $this->db->query("SELECT akhir counter FROM tcount WHERE kode = '$_headerkode'")->row()->counter;
        return $result + 1;
    }

    //added by Tonny
    //untuk mendapatkan nomor baru untuk salesorder detail
    function getNomorDetail($_detailkode){
        $result = $this->db->query("SELECT akhir counter FROM tcount WHERE kode = '$_detailkode'")->row()->counter;
        return $result + 1;
    }

    //untuk testing saja
    function getHeader_get(){
        $this->response($this->getNomorHeader('ttransaksi'));
    }

    //added by Tonny
    //untuk mendapatkan list item berdasarkan nomor thorderjual
    function getSalesOrderItemList_post(){
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        $nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"])     : "");  //input merupakan nomor thorderjual
        //data yang diperlukan
        //nomorbarang~kodebarang~namabarang~satuan~price~qty~fee~disc~subtotal~notes
        $query = "SELECT a.nomorbarang, a.kodebarang, b.nama namabarang, b.satuan, a.harga price, a.qty, a.fee, a.disc1 disc, a.subtotal, a.keterangandetail AS notes
                  FROM tdorderjual a
                  JOIN vwbarang b ON a.nomorbarang = b.nomor
                  WHERE
                    a.nomorheader = $nomor
                    AND a.nomorpekerjaan = ''
                    AND a.kodepekerjaan = '' ";
        $result = $this->db->query($query);
        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'nomorbarang'	=> $r['nomorbarang'],
                                                'kodebarang'	=> $r['kodebarang'],
                                                'namabarang'	=> $r['namabarang'],
                                                'satuan'    	=> $r['satuan'],
                                                'price'     	=> $r['price'],
                                                'qty'       	=> $r['qty'],
                                                'fee'	        => $r['fee'],
                                                'disc'	        => $r['disc'],
                                                'subtotal'  	=> $r['subtotal'],
                                                'notes'     	=> $r['notes']
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    //added by Tonny
    //untuk mendapatkan list pekerjaan berdasarkan nomor thorderjual
    function getSalesOrderPekerjaanList_post(){
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        $nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"])     : "");  //input merupakan nomor thorderjual
        //data yang diperlukan
        //nomorbarang~kodebarang~namabarang~satuan~price~qty~fee~disc~subtotal~notes
        $query = "SELECT a.nomorpekerjaan AS nomorbarang, a.kodepekerjaan AS kodebarang, b.nama namabarang, b.satuan, a.harga price, a.qty, a.fee, a.disc1 disc, a.subtotal, a.keterangandetail AS notes
                  FROM tdorderjual a
                  JOIN vwpekerjaan b ON a.nomorpekerjaan = b.nomor
                  WHERE
                    a.nomorheader = $nomor
                    AND a.nomorbarang is null
                    AND a.kodebarang is null ";
        $result = $this->db->query($query);
        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'nomorbarang'	=> $r['nomorbarang'],
                                                'kodebarang'	=> $r['kodebarang'],
                                                'namabarang'	=> $r['namabarang'],
                                                'satuan'    	=> $r['satuan'],
                                                'price'     	=> $r['price'],
                                                'qty'       	=> $r['qty'],
                                                'fee'	        => $r['fee'],
                                                'disc'	        => $r['disc'],
                                                'subtotal'  	=> $r['subtotal'],
                                                'notes'     	=> $r['notes']
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    //added by Tonny
    //untuk mendapatkan summary dari thorderjual
    function getSalesOrderSummary_post(){
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        $nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"])     : "");  //input merupakan nomor thorderjual
        //data yang diperlukan
        //tanggal~customer~broker~valuta~subtotal~grandtotal
        $query = "SELECT a.tanggal, b.nama namacustomer, c.nama namabroker, a.valuta, a.subtotal, a.disc, a.discnominal, a.ppn, a.ppnnominal, a.total
                  FROM thorderjual a
                      JOIN tcustomer b ON a.nomorcustomer = b.nomor
                      JOIN thbroker c ON a.nomorbroker = c.nomor
                  WHERE
                    a.nomor = $nomor";
        $result = $this->db->query($query);
        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'tanggal'   	=> $r['tanggal'],
                                                'namacustomer'	=> $r['namacustomer'],
                                                'namabroker'	=> $r['namabroker'],
                                                'valuta'    	=> $r['valuta'],
                                                'subtotal'     	=> $r['subtotal'],
                                                'disc'       	=> $r['disc'],
                                                'discnominal'	=> $r['discnominal'],
                                                'ppn'	        => $r['ppn'],
                                                'ppnnominal'  	=> $r['ppnnominal'],
                                                'total'     	=> $r['total']
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // by Tonny
    //Untuk insert data customer prospecting ke tabel tcustomerprospecting
    function setApprove_post(){
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        $nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"])     : "");  //input merupakan nomor thorderjual
        //$nomor = '253';
        $query = "SELECT approve FROM thorderjual WHERE nomor = $nomor ";
        $result = $this->db->query($query);
//        echo $result->row()->approve;
        $approve = 1;
        $errormsg = '';
        //cek jika sudah diapprove atau belum
        if($result && $result->num_rows() > 0){
            $approve = $result->row()->approve;
            if($approve == 0){
                //jika belum diapprove, maka lanjutkan untuk memastikan hargajual barang > hpp
                $query = "SELECT a.hpp, a.hargajualidr
                          FROM tbarang a
                          JOIN tdorderjual b
                            ON a.nomor = b.nomorbarang
                          WHERE b.nomorheader = $nomor";
                $result = $this->db->query($query);
                $hpp = 0;
                if($result && $result->num_rows() > 0){
                    $iserror = false;
                    //pengecekan jika harga jual barang melebihi hpp
                    foreach ($result->result_array() as $row){
                        if($row['hpp'] >= $row['hargajualidr']){
                            $iserror = true;
                            $errormsg = 'harga jual value must be more than hpp value';
                        }elseif($row['hpp'] <= 0){  //pengecekan jika hpp = 0
                            $iserror = true;
                            $errormsg = 'hpp value must be more than 0';
                        }
                    }
                    if($iserror){
                        array_push($data['data'], array( 'error' => $this->error($errormsg) ));
                    }else{
                        $query = "UPDATE thorderjual set approve = 1 WHERE nomor = $nomor ";
                        $result = $this->db->query($query);
                        if($result){
                            array_push($data['data'], array( 'success' => 'true' ));
                        }else{
                            array_push($data['data'], array( 'error' => $this->error($query) ));
                        }
                        if ($data){
                            // Set the response and exit
                            $this->response($data['data']); // OK (200) being the HTTP response code
                        }
                    }
				}else{
				    $errormsg = 'data not found';
				    array_push($data['data'], array( 'error' => $this->error($errormsg) ));
				}
            }else{
                $errormsg = 'data has been already approved';
                array_push($data['data'], array( 'error' => $this->error($errormsg) ));
            }
        }else{
            $errormsg = 'data not found';
            array_push($data['data'], array( 'error' => $this->error($errormsg) ));
        }
    }
}
