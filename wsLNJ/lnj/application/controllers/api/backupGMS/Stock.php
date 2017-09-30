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
class Stock extends REST_Controller {

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
        return str_replace( array("\t", "\n") , "", $string);
    }

	function getGCMId($user_nomor){
        $query = "  SELECT
                    a.gcmid
                    FROM whuser_mobile a
                    WHERE a.status_aktif > 0 AND (a.gcmid <> '' AND a.gcmid IS NOT NULL) AND a.nomor = $user_nomor ";
        return $this->db->query($query)->row()->gcmid;
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

	//--- Added by Tonny --- //
    // --- POST filter Kategori --- //
    function getKategori_post()
    {
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = 'SELECT DISTINCT kategori FROM vwbarang ORDER BY kategori ';
        $result = $this->db->query($query);
        if( $result && $result->num_rows() > 0)
        {
            foreach ($result->result_array() as $r)
            {
                array_push($data['data'], array(
                                                    'kategori'						=> $r['kategori']
                                                )
                );
            }
        }
        else
        {
            array_push($data['data'], array( 'success' => "false" ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    //--- Added by Tonny --- //
    // --- POST filter Bentuk--- //
    function getBentuk_post()
    {
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = 'SELECT DISTINCT bentuk FROM vwbarang ORDER BY bentuk ';
        $result = $this->db->query($query);
        if( $result && $result->num_rows() > 0)
        {
            foreach ($result->result_array() as $r)
            {
                array_push($data['data'], array(
                                                    'bentuk'				=> $r['bentuk']
                                                )
                );
            }
        }
        else
        {
            array_push($data['data'], array( 'success' => "false" ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    //--- Added by Tonny --- //
    // --- POST filter Surface--- //
    function getSurface_post()
    {
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = 'SELECT DISTINCT surface FROM vwbarang ORDER BY surface ';
        $result = $this->db->query($query);
        if( $result && $result->num_rows() > 0)
        {
            foreach ($result->result_array() as $r)
            {
                array_push($data['data'], array(
                                                    'surface'		=> $r['surface']
                                                )
                );
            }
        }
        else
        {
            array_push($data['data'], array( 'success' => "false" ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    //--- Added by Tonny --- //
    // --- POST filter jenis--- //
    function getJenis_post()
    {
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = 'SELECT DISTINCT jenis FROM vwbarang ORDER BY jenis ';
        $result = $this->db->query($query);
        if( $result && $result->num_rows() > 0)
        {
            foreach ($result->result_array() as $r)
            {
                array_push($data['data'], array(
                                                    'jenis'		=> $r['jenis']
                                                )
                );
            }
        }
        else
        {
            array_push($data['data'], array( 'success' => "false" ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    //--- Added by Tonny --- //
    // --- POST filter gudang--- //
    function getGudang_post()
    {
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = 'SELECT DISTINCT
                    kode kodegudang, nama namagudang
                  FROM thgudang
                  ORDER BY namagudang ';
        $result = $this->db->query($query);
        if( $result && $result->num_rows() > 0)
        {
            foreach ($result->result_array() as $r)
            {
                array_push($data['data'], array(
                                                    'kodegudang'	=> $r['kodegudang'],
                                                    'namagudang'	=> $r['namagudang']
                                                )
                );
            }
        }
        else
        {
            array_push($data['data'], array( 'success' => "false" ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    //--- Added by Tonny --- //
    // --- POST filter grade--- //
    function getGrade_post()
    {
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = 'SELECT DISTINCT grade FROM vwbarang ORDER BY grade';
        $result = $this->db->query($query);
        if( $result && $result->num_rows() > 0)
        {
            foreach ($result->result_array() as $r)
            {
                array_push($data['data'], array(
                                                    'grade'		=> $r['grade']
                                                )
                );
            }
        }
        else
        {
            array_push($data['data'], array( 'success' => "false" ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    //--- Added by Tonny --- //
    // --- POST stock posisi--- //
    function getStockPosisi_post(){
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $kodegudang = (isset($jsonObject["kodegudang"]) ? $this->clean($jsonObject["kodegudang"])     : "");
        $nomorbarang = (isset($jsonObject["nomorbarang"]) ? $this->clean($jsonObject["nomorbarang"])     : "");
        $kategori = (isset($jsonObject["kategori"]) ? $this->clean($jsonObject["kategori"])     : "");
        $bentuk = (isset($jsonObject["bentuk"]) ? $this->clean($jsonObject["bentuk"])     : "");
        $jenis = (isset($jsonObject["jenis"]) ? $this->clean($jsonObject["jenis"])     : "");
        $grade = (isset($jsonObject["grade"]) ? $this->clean($jsonObject["grade"])     : "");
        $surface = (isset($jsonObject["surface"]) ? $this->clean($jsonObject["surface"])     : "");
        $ukuran = (isset($jsonObject["ukuran"]) ? $this->clean($jsonObject["ukuran"])     : "");
        $tebal = (isset($jsonObject["tebal"]) ? $this->clean($jsonObject["tebal"])     : "");
        $motif = (isset($jsonObject["motif"]) ? $this->clean($jsonObject["motif"])     : "");
        $tanggal = (isset($jsonObject["tanggal"]) ? $this->clean($jsonObject["tanggal"])     : "");
        $nomorcabang = (isset($jsonObject["nomorcabang"]) ? $this->clean($jsonObject["nomorcabang"])     : "");

        $query = "SELECT c.kode kodegudang, c.nama namagudang, b.kode kodebarang, b.nama namabarang, b.satuan, sum(a.qty) qty, sum(a.jumlah) m2
                 FROM thlaporanstok a
                   join vwbarang b
                     on a.nomorbarang = b.nomor
                   join thgudang c
                     on a.nomorgudang = c.nomor
                 WHERE a.status<>0
                 AND c.kode like '%$kodegudang%'
                 AND b.kode like '%$nomorbarang%'
                 AND b.kategori like '%$kategori%'
                 AND b.jenis like '%$jenis%'
                 AND b.grade like '%$grade%'
                 AND b.bentuk like '%$bentuk%'
                 AND b.ukuran like '%$ukuran%'
                 AND b.tebal like '%$tebal%'
                 AND b.motif like '%$motif%'
                 AND b.surface like '%$surface%'
                 AND a.tanggal <= '$tanggal'
                 AND c.nomorcabang = '$nomorcabang'
                 GROUP BY c.kode, c.nama, b.kode, b.nama, b.satuan
                 HAVING (sum(a.jumlah) <> 0)
                 ORDER BY c.kode, b.satuan, b.nama";
        $this->db->query($query);
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'kodegudang'			=> $r['kodegudang'],
                                                'namagudang'			=> $r['namagudang'],
                                                'kodebarang'			=> $r['kodebarang'],
                                                'namabarang'			=> $r['namabarang'],
                                                'satuan'                => $r['satuan'],
                                                'qty' 					=> $r['qty'],
                                                'm2' 					=> $r['m2']
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
            //$this->response($query); // OK (200) being the HTTP response code
        }
    }

    //--- Added by Tonny --- //
    // --- POST stock posisi random--- //
    function getStockPosisiRandom_post(){
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $kodegudang = (isset($jsonObject["kodegudang"]) ? $this->clean($jsonObject["kodegudang"])     : "");
        $nomorbarang = (isset($jsonObject["nomorbarang"]) ? $this->clean($jsonObject["nomorbarang"])     : "");
        $kategori = (isset($jsonObject["kategori"]) ? $this->clean($jsonObject["kategori"])     : "");
        $bentuk = (isset($jsonObject["bentuk"]) ? $this->clean($jsonObject["bentuk"])     : "");
        $jenis = (isset($jsonObject["jenis"]) ? $this->clean($jsonObject["jenis"])     : "");
        $grade = (isset($jsonObject["grade"]) ? $this->clean($jsonObject["grade"])     : "");
        $surface = (isset($jsonObject["surface"]) ? $this->clean($jsonObject["surface"])     : "");
        $ukuran = (isset($jsonObject["ukuran"]) ? $this->clean($jsonObject["ukuran"])     : "");
        $tebal = (isset($jsonObject["tebal"]) ? $this->clean($jsonObject["tebal"])     : "");
        $motif = (isset($jsonObject["motif"]) ? $this->clean($jsonObject["motif"])     : "");
        $tanggal = (isset($jsonObject["tanggal"]) ? $this->clean($jsonObject["tanggal"])     : "");
        $nomorcabang = (isset($jsonObject["nomorcabang"]) ? $this->clean($jsonObject["nomorcabang"])     : "");

        $query = "SELECT c.kode kodegudang, c.nama namagudang, b.kode kodebarang, b.nama namabarang, b.satuan, sum(a.qty) qty, sum(a.jumlah) m2
                 FROM thlaporanstok a
                   join vwbarang b
                     on a.nomorbarang = b.nomor
                   join thgudang c
                     on a.nomorgudang = c.nomor
                 WHERE a.status<>0
                 AND c.kode like '%$kodegudang%'
                 AND b.random = 1
                 AND b.kode like '%$nomorbarang%'
                 AND b.kategori like '%$kategori%'
                 AND b.jenis like '%$jenis%'
                 AND b.grade like '%$grade%'
                 AND b.bentuk like '%$bentuk%'
                 AND b.ukuran like '%$ukuran%'
                 AND b.tebal like '%$tebal%'
                 AND b.motif like '%$motif%'
                 AND b.surface like '%$surface%'
                 AND a.tanggal <= '$tanggal'
                 AND c.nomorcabang = '$nomorcabang'
                 GROUP BY c.kode, c.nama, b.kode, b.nama, b.satuan
                 HAVING (sum(a.jumlah) <> 0)
                 ORDER BY c.kode, b.satuan, b.nama";
        $this->db->query($query);
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'kodegudang'			=> $r['kodegudang'],
                                                'namagudang'			=> $r['namagudang'],
                                                'kodebarang'			=> $r['kodebarang'],
                                                'namabarang'			=> $r['namabarang'],
                                                'satuan'                => $r['satuan'],
                                                'qty' 					=> $r['qty'],
                                                'm2' 					=> $r['m2']
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
            //$this->response($query); // OK (200) being the HTTP response code
        }
    }

    //--- Added by Tonny --- //
    // --- POST stock random per barang--- //
    function getStockRandomPerBarang_post(){
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $kodegudang = (isset($jsonObject["kodegudang"]) ? $this->clean($jsonObject["kodegudang"])     : "");
        $nomorbarang = (isset($jsonObject["nomorbarang"]) ? $this->clean($jsonObject["nomorbarang"])     : "");
        $kategori = (isset($jsonObject["kategori"]) ? $this->clean($jsonObject["kategori"])     : "");
        $bentuk = (isset($jsonObject["bentuk"]) ? $this->clean($jsonObject["bentuk"])     : "");
        $jenis = (isset($jsonObject["jenis"]) ? $this->clean($jsonObject["jenis"])     : "");
        $grade = (isset($jsonObject["grade"]) ? $this->clean($jsonObject["grade"])     : "");
        $surface = (isset($jsonObject["surface"]) ? $this->clean($jsonObject["surface"])     : "");
        $ukuran = (isset($jsonObject["ukuran"]) ? $this->clean($jsonObject["ukuran"])     : "");
        $tebal = (isset($jsonObject["tebal"]) ? $this->clean($jsonObject["tebal"])     : "");
        $motif = (isset($jsonObject["motif"]) ? $this->clean($jsonObject["motif"])     : "");
        $blok = (isset($jsonObject["blok"]) ? $this->clean($jsonObject["blok"])     : "");
        $tanggal = (isset($jsonObject["tanggal"]) ? $this->clean($jsonObject["tanggal"])     : "");
        $nomorcabang = (isset($jsonObject["nomorcabang"]) ? $this->clean($jsonObject["nomorcabang"])     : "");

        $query = "SELECT d.kode kodegudang, d.nama namagudang, e.nama lokasi, b.kode kodebarang, b.nama namabarang,
                  c.barcode, c.bundle, c.slab, c.blok, c.peti, a.jumlah as m2, f.coeff1, f.panjang, f.lebar, f.tebal, f.jumlah, '' as kodebarangpacking, '' as namabarangpacking, 0 qty, 0 as jumlahdetail, b.packing
                  FROM (select nomorgudang, nomorbarang, nomorbarcode, nomorlokasi, sum(qty) qty, sum(jumlah) jumlah
                   FROM thlaporanstok
                   WHERE status<>0
                   AND tanggal <= '$tanggal'
                   GROUP BY nomorgudang, nomorbarang, nomorbarcode, nomorlokasi
                   HAVING SUM(qty)<>0) a
                    JOIN vwbarang b ON a.nomorbarang=b.nomor
                    JOIN thbarcode c ON a.nomorbarcode=c.nomor
                    JOIN thgudang d ON a.nomorgudang=d.nomor
                    JOIN tlokasi e ON a.nomorlokasi=e.nomor
                    JOIN tdbarcode f ON c.nomor=f.nomorheader
                  WHERE b.random=1 AND b.packing=0
                  AND d.kode LIKE '%$kodegudang%'
                  AND b.kode LIKE '%$kodebarang%'
                  AND b.kategori LIKE '%$kategori%'
                  AND b.jenis LIKE '%$jenis%'
                  AND b.grade LIKE '%$grade%'
                  AND b.bentuk LIKE '%$bentuk%'
                  AND b.ukuran LIKE '%$ukuran%'
                  AND b.tebal LIKE '%$tebal%'
                  AND b.motif LIKE '%$motif%'
                  AND b.surface LIKE '%$surface%'
                  AND c.blok LIKE '%$blok%'
                  ORDER BY b.nama, c.blok";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'kodegudang'			=> $r['kodegudang'],
                                                'namagudang'			=> $r['namagudang'],
                                                'kodebarang'			=> $r['kodebarang'],
                                                'namabarang'			=> $r['namabarang'],
                                                'barcode'	    		=> $r['barcode'],
                                                'bundle'	    		=> $r['bundle'],
                                                'slab'	    		    => $r['slab'],
                                                'blok'	    	    	=> $r['blok'],
                                                'peti'	    		    => $r['peti'],
                                                'm2'	    	    	=> $r['m2'],
                                                'panjang'	    		=> $r['panjang'],
                                                'lebar'	    	    	=> $r['lebar'],
                                                'tebal'	    	    	=> $r['tebal'],
                                                'coeff1'	    		=> $r['coeff1'],
                                                'jumlah'	    		=> $r['jumlah'],
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
            //$this->response($query); // OK (200) being the HTTP response code
        }
    }

    //--- Added by ADI --- //
    // --- POST stock random per lokasi--- //
    function getStockRandomPerLokasi_post(){
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $kodegudang = (isset($jsonObject["kodegudang"]) ? $this->clean($jsonObject["kodegudang"])     : "%");
        $nomorbarang = (isset($jsonObject["nomorbarang"]) ? $this->clean($jsonObject["nomorbarang"])     : "%");
        $kategori = (isset($jsonObject["kategori"]) ? $this->clean($jsonObject["kategori"])     : "%");
        $bentuk = (isset($jsonObject["bentuk"]) ? $this->clean($jsonObject["bentuk"])     : "%");
        $jenis = (isset($jsonObject["jenis"]) ? $this->clean($jsonObject["jenis"])     : "%");
        $grade = (isset($jsonObject["grade"]) ? $this->clean($jsonObject["grade"])     : "%");
        $surface = (isset($jsonObject["surface"]) ? $this->clean($jsonObject["surface"])     : "%");
        $ukuran = (isset($jsonObject["ukuran"]) ? $this->clean($jsonObject["ukuran"])     : "%");
        $tebal = (isset($jsonObject["tebal"]) ? $this->clean($jsonObject["tebal"])     : "%");
        $motif = (isset($jsonObject["motif"]) ? $this->clean($jsonObject["motif"])     : "%");
        $lokasi = (isset($jsonObject["lokasi"]) ? $this->clean($jsonObject["lokasi"])     : "%");
        $tanggal = (isset($jsonObject["tanggal"]) ? $this->clean($jsonObject["tanggal"])     : "%");
        $nomorcabang = (isset($jsonObject["nomorcabang"]) ? $this->clean($jsonObject["nomorcabang"])     : "%");

         if($kodegudang == "") $kodegudang = "%";
         if($nomorbarang == "") $nomorbarang = "%";
         if($kategori == "") $kategori = "%";
         if($bentuk == "") $bentuk = "%";
         if($jenis == "") $jenis = "%";
         if($grade == "") $grade = "%";
         if($surface == "") $surface = "%";
         if($ukuran == "") $ukuran = "%";
         if($tebal == "") $tebal = "%";
         if($motif == "") $motif = "%";
         if($lokasi == "") $lokasi = "%";

        $query = "CALL rp_posisistok_random_lokasi('$kodegudang', '$nomorbarang', '$kategori', '$jenis', '$grade', '$bentuk', '$ukuran', '$tebal', '$motif', '$surface', '$lokasi', '$tanggal')";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'kodegudang'			=> $r['kodegudang'],
                                                'namagudang'			=> $r['namagudang'],
                                                'kodebarang'			=> $r['kodebarang'],
                                                'namabarang'			=> $r['namabarang'],
                                                'barcode'	    		=> $r['barcode'],
                                                'bundle'	    		=> $r['bundle'],
                                                'slab'	    		    => $r['slab'],
                                                'blok'	    	    	=> $r['blok'],
                                                'peti'	    		    => $r['peti'],
                                                'm2'	    	    	=> $r['m2'],
                                                'panjang'	    		=> $r['panjang'],
                                                'lebar'	    	    	=> $r['lebar'],
                                                'tebal'	    	    	=> $r['tebal'],
                                                'coeff1'	    		=> $r['coeff1'],
                                                'jumlah'	    		=> $r['jumlah'],
                                                'lokasi'	    		=> $r['lokasi'],
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
//            $this->response($this->error($query)); // OK (200) being the HTTP response code
        }
    }

    //--- Added by ADI --- //
    // --- POST stock mutasi--- //
    function getStockMutasi_post(){
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $kodebarang = (isset($jsonObject["kodebarang"]) ? $this->clean($jsonObject["kodebarang"])     : "");
        $kodegudang = (isset($jsonObject["kodegudang"]) ? $this->clean($jsonObject["kodegudang"])     : "");
        $tanggalawal = (isset($jsonObject["tanggalawal"]) ? $this->clean($jsonObject["tanggalawal"])     : "20140908");
        $tanggal = (isset($jsonObject["tanggal"]) ? $this->clean($jsonObject["tanggal"])     : "20170908");

        if($kodebarang=="") $kodebarang = "%";
        if($kodegudang=="") $kodegudang = "%";

        $query = "CALL RP_MUTASI_STOK ('$kodebarang', '$kodegudang', '$tanggalawal', '$tanggal')";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'kodegudang'			=> $r['KODEGUDANG'],
                                                'namagudang'			=> $r['NAMAGUDANG'],
                                                'kodebarang'			=> $r['KODEBARANG'],
                                                'namabarang'			=> $r['NAMABARANG'],
                                                'qtyawal'	    		=> $r['QTYAWAL'],
                                                'jumlahawal'	    	=> $r['JUMLAHAWAL'],
                                                'qtymasuk'	    		=> $r['QTYMASUK'],
                                                'jumlahmasuk'	    	=> $r['JUMLAHMASUK'],
                                                'qtykeluar'	    		=> $r['QTYKELUAR'],
                                                'jumlahkeluar'	    	=> $r['JUMLAHKELUAR'],
                                                'qtyakhir'	    		=> $r['QTYAKHIR'],
                                                'jumlahakhir'	    	=> $r['JUMLAHAKHIR'],
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
            //$this->response($query); // OK (200) being the HTTP response code
        }
    }

    //--- Added by ADI --- //
    // --- POST stock kartu--- //
    function getStockKartu_post(){
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $kodebarang = (isset($jsonObject["kodebarang"]) ? $this->clean($jsonObject["kodebarang"])     : "");
        $kodegudang = (isset($jsonObject["kodegudang"]) ? $this->clean($jsonObject["kodegudang"])     : "");
        $tanggalawal = (isset($jsonObject["tanggalawal"]) ? $this->clean($jsonObject["tanggalawal"])     : "20140908");
        $tanggal = (isset($jsonObject["tanggal"]) ? $this->clean($jsonObject["tanggal"])     : "20170908");

        if($kodebarang=="") $kodebarang = "%";
        if($kodegudang=="") $kodegudang = "%";

        $query = "CALL RP_KARTU_STOK ('$kodebarang', '$kodegudang', '$tanggalawal', '$tanggal')";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'tanggal'   			=> $r['TANGGAL'],
                                                'entity'    			=> $r['ENTITY'],
                                                'keterangan'			=> $r['KETERANGAN'],
                                                'qtyawal'	    		=> $r['QTYAWAL'],
                                                'jumlahawal'	    	=> $r['JUMLAHAWAL'],
                                                'qtymasuk'	    		=> $r['QTYMASUK'],
                                                'jumlahmasuk'	    	=> $r['JUMLAHMASUK'],
                                                'qtykeluar'	    		=> $r['QTYKELUAR'],
                                                'jumlahkeluar'	    	=> $r['JUMLAHKELUAR'],
                                                'qtyakhir'	    		=> $r['QTYAKHIR'],
                                                'jumlahakhir'	    	=> $r['JUMLAHAKHIR'],
                                                )
                );
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
            //$this->response($query); // OK (200) being the HTTP response code
        }
    }
}
