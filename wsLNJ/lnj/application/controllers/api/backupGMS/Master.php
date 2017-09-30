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
class Master extends REST_Controller { 

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
	
	function test_get()
	{
		
		$result = "a";
		
		$data['data'] = array();
		
		// START SEND NOTIFICATION
        $vcGCMId = $this->getGCMId(3);
		
        $this->send_gcm($vcGCMId, $this->ellipsis('$new_message'),'New Message(s) From ','PrivateMessage','0','0');
		
		$this->response($vcGCMId);
		
		/*
		$regisID = array();
			
		$query_getuser = " SELECT 
							a.gcmid
							FROM whuser_mobile a 
							JOIN whrole_mobile b ON a.nomorrole = b.nomor
							WHERE a.status_aktif > 0 AND (a.gcmid <> '' AND a.gcmid IS NOT NULL) AND b.approveberitaacara = 1 ";
		$result_getuser = $this->db->query($query_getuser);

		if( $result_getuser && $result_getuser->num_rows() > 0){
			foreach ($result_getuser->result_array() as $r_user){

				// START SEND NOTIFICATION
				$vcGCMId = $r_user['gcmid'];
				if( $vcGCMId != "null" ){      
					array_push($regisID, $vcGCMId);       
				}
				
			}
			$count = $this->db->query("SELECT COUNT(1) AS elevasi_baru FROM mhberitaacara a WHERE a.status_disetujui = 0")->row()->elevasi_baru; 
			$this->send_gcm_group($regisID, $this->ellipsis("Berita Acara Elevasi"),$count . ' pending elevasi','ChooseApprovalElevasi','','');
		} 
		*/
	}

	// --- POST get contact --- //
	function getContact_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));
		
		$query = "	SELECT 
						a.nomor AS `nomor`,
						a.nomortuser AS `nomorTUser`,
						a.nomorthsales AS `nomorTHSales`,
						b.kode AS `nama`,
						IFNULL(a.telp, '') AS telp
					FROM whuser_mobile a
					JOIN tuser b
						ON b.nomor = a.nomortuser
					WHERE b.status = 1
					ORDER BY b.kode DESC;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                								'nomor'					=> $r['nomor'],
												'nomorTUser' 			=> $r['nomorTUser'],
                                                'nomorTHSales'         	=> $r['nomorTHSales'], 
                								'nama' 					=> $r['nama'],
                								'telp' 					=> $r['telp']
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
	
	// --- POST get barang --- //
	function getBarang_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));
		
		$query = "	SELECT 
						a.nomor AS `nomor`,
						a.kode AS `kode`,
						a.nama AS `nama`,
						a.NamaJual AS `namajual`,
						b.satuan AS `satuan`,
						a.HargaJualIDR AS `hargajual`,
						b.BarangTambang AS `tambang`,
						b.BarangImport AS `import`
					FROM tbarang a
					JOIN vwbarang b ON a.nomor = b.nomor
					WHERE a.aktif = 1
					ORDER BY a.nama DESC;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                								'nomor'					=> $r['nomor'],
												'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama'], 
                								'namajual' 				=> $r['namajual'],
                								'satuan' 				=> $r['satuan'],
                								'hargajual' 			=> $r['hargajual'],
                								'tambang' 	    		=> $r['tambang'],
                								'import' 	    		=> $r['import'],
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

    // --- POST get barang --- //
    function getBarangImport_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor AS `nomor`,
                        a.kode AS `kode`,
                        a.nama AS `nama`,
                        a.NamaJual AS `namajual`,
                        b.satuan AS `satuan`,
                        a.HargaJualIDR AS `hargajual`
                    FROM tbarang a
                    JOIN vwbarang b ON a.nomor = b.nomor
                    WHERE a.aktif = 1
                        AND a.BarangImport = 1
                    ORDER BY a.nama DESC;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama'],
                                                'namajual' 				=> $r['namajual'],
                                                'satuan' 				=> $r['satuan'],
                                                'hargajual' 			=> $r['hargajual'],
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

    // --- POST get barang --- //
    function getBarangLokal_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor AS `nomor`,
                        a.kode AS `kode`,
                        a.nama AS `nama`,
                        a.NamaJual AS `namajual`,
                        b.satuan AS `satuan`,
                        a.HargaJualIDR AS `hargajual`
                    FROM tbarang a
                    JOIN vwbarang b ON a.nomor = b.nomor
                    WHERE a.aktif = 1
                        AND a.BarangImport = 0
                    ORDER BY a.nama DESC;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama'],
                                                'namajual' 				=> $r['namajual'],
                                                'satuan' 				=> $r['satuan'],
                                                'hargajual' 			=> $r['hargajual'],
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

    //  added by Tonny
    // --- POST get pekerjaan / jasa --- //
    function getPekerjaan_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor AS `nomor`,
                        a.kode AS `kode`,
                        a.nama AS `nama`,
                        a.kodesatuan,
                        b.satuan AS `satuan`,
                        a.hargacustomer,
                        a.hargamandor
                    FROM tpekerjaan a
                    JOIN vwpekerjaan b ON a.nomor = b.nomor
                    WHERE a.aktif = 1
                    ORDER BY a.nama DESC;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama'],
                                                'kodesatuan' 			=> $r['kodesatuan'],
                                                'satuan' 				=> $r['satuan'],
                                                'hargacustomer'			=> $r['hargacustomer'],
                                                'hargamandor'			=> $r['hargamandor']
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
	
	// --- POST get customer --- //
	function getCustomer_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));
		
		$query = "	SELECT 
						a.nomor AS `nomor`,
						a.kode AS `kode`,
						a.nama AS `nama`,
						a.alamat AS `alamat`,
						a.telepon AS `telepon`
					FROM tcustomer a
					WHERE a.aktif = 1
					ORDER BY a.nama DESC;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                								'nomor'					=> $r['nomor'],
												'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama'], 
                								'alamat' 				=> $r['alamat'],
												'telepon' 				=> $r['telepon']
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
	
	// --- POST get kota --- //
	function getKota_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));
		
		$query = "	SELECT 
						a.nomor AS `nomor`,
						a.kode AS `kode`,
						a.kota AS `nama`,
						a.nomorpropinsi AS `nomorpropinsi`
					FROM tkota a
					WHERE a.aktif = 1
					ORDER BY a.kota DESC;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                								'nomor'					=> $r['nomor'],
												'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama'], 
                								'nomorpropinsi'			=> $r['nomorpropinsi']
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

    // --- POST get valuta --- //
    function getValuta_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor AS `nomor`,
                        a.kode AS `kode`,
                        a.valuta AS `nama`,
                        (SELECT b.kurs FROM tsettingkurs b WHERE b.nomorheader=a.nomor ORDER BY b.nomor DESC LIMIT 1) AS `kurs`
                    FROM tvaluta a
                    WHERE a.status = 1
                    ORDER BY a.valuta;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama'],
                                                'kurs'			        => $r['kurs']
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

    // --- POST get broker --- //
    function getBroker_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor AS `nomor`,
                        a.kode AS `kode`,
                        a.nama AS `nama`
                    FROM thbroker a
                    WHERE a.aktif = 1
                    ORDER BY a.nama;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama'],
                                                'kurs'			        => $r['kurs']
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

    // --- POST get kategori --- //
    function getKategori_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor AS `nomor`,
                        a.kode AS `kode`,
                        a.kategori AS `nama`
                    FROM tkategoribarang a
                    WHERE a.status = 1
                    ORDER BY a.kategori;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama']
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

    // --- POST get gudang --- //
    function getGudang_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor AS `nomor`,
                        a.kode AS `kode`,
                        a.nama AS `nama`,
                        a.alamat AS `alamat`,
                        a.kota AS `kota`
                    FROM thgudang a
                    WHERE a.status = 1
                    ORDER BY a.nama;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama'],
                                                'alamat'				=> $r['alamat'],
                                                'kota' 					=> $r['kota'],
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

    // --- POST get bentuk --- //
    function getBentuk_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor AS `nomor`,
                        a.kode AS `kode`,
                        a.bentuk AS `nama`
                    FROM tbentukbarang a
                    WHERE a.status = 1
                    ORDER BY a.bentuk;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama']
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

    // --- POST get surface --- //
    function getSurface_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor AS `nomor`,
                        a.kode AS `kode`,
                        a.surface AS `nama`
                    FROM tsurfacebarang a
                    WHERE a.status = 1
                    ORDER BY a.surface;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama']
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

    // --- POST get jenis --- //
    function getJenis_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor AS `nomor`,
                        a.kode AS `kode`,
                        a.jenis AS `nama`
                    FROM tjenisbarang a
                    WHERE a.status = 1
                    ORDER BY a.jenis;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama']
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

    // --- POST get grade --- //
    function getGrade_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor AS `nomor`,
                        a.kode AS `kode`,
                        a.grade AS `nama`
                    FROM tgradebarang a
                    WHERE a.status = 1
                    ORDER BY a.grade;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama']
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

    // --- POST get proyek --- //
    function getProyek_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor AS `nomor`,
                        a.kode AS `kode`,
                        a.nama AS `nama`,
                        a.alamat AS `alamat`
                    FROM tproyek a
                    WHERE a.status = 1
                    ORDER BY a.nama;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama'],
                                                'alamat'      		   	=> $r['alamat']
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

    // --- POST get lokasi --- //
    function getLokasi_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor AS `nomor`,
                        a.kode AS `kode`,
                        a.nama AS `nama`,
                        b.nama AS gudang
                    FROM tlokasi a
                    JOIN thgudang b ON a.nomorheader = b.nomor
                    WHERE a.status = 1
                    ORDER BY a.nama;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama'      		   	=> $r['nama'],
                                                'gudang'      		   	=> $r['gudang'],
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

    //--- Added by Tonny --- //
    // --- POST get sales --- //
    function getSales_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "SELECT DISTINCT
                    #a.nomor AS `nomor`,
                    #a.nomortuser AS `nomortuser`,
                    a.nomorthsales AS `nomorsales`,
                    b.kode AS `nama`
                 FROM whuser_mobile a
                 JOIN tuser b
                    ON b.nomor = a.nomortuser
                 WHERE b.status = 1
                    AND a.nomorthsales > 0
                 ORDER BY b.kode DESC;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                //'nomor'					=> $r['nomor'],
                                                //'nomortuser' 			=> $r['nomortuser'],
                                                'nomorsales'         	=> $r['nomorsales'],
                                                'nama' 					=> $r['nama']
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

    //--- Added by Tonny --- //
    // --- POST get cabang --- //
    function getCabang_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor AS `nomor`,
                        a.cabang AS `cabang`
                    FROM tcabang a
                    WHERE a.aktif = 1
                    ORDER BY a.cabang;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomorcabang'					=> $r['nomor'],
                                                'namacabang' 					=> $r['cabang'],
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

    //--- Added by Tonny --- //
    // --- POST get price --- //
    function getPrice_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        $cabang = (isset($jsonObject["cabang"]) ? $this->clean($jsonObject["cabang"])     : "a");
        $query = "	SELECT
                        b.nomor AS nomor,
                        b.kode AS kode,
                        b.NamaJual AS nama,
                        a.HargaJual AS harga
                    FROM tdharga a
                    JOIN tbarang b ON a.NomorBarang = b.nomor
                    WHERE b.Aktif = 1
                     AND a.nomorheader = $cabang
                    ORDER BY b.NamaJual DESC";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama' 					=> $r['nama'],
                                                'harga' 				=> $r['harga']
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

    //--- Added by Tonny --- //
    // --- POST get PriceHPP --- //
    function getPriceHPP_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        $cabang = (isset($jsonObject["cabang"]) ? $this->clean($jsonObject["cabang"])     : "a");
        $query = "	SELECT b.nomor AS nomor,
                        b.kode AS kode,
                        b.NamaJual AS nama,
                        a.HargaJual AS harga,
                        a.HPP AS hpp
                    FROM tdharga a
                    JOIN tbarang b ON a.NomorBarang = b.nomor
                    WHERE b.Aktif = 1
                     AND a.nomorheader = $cabang
                    ORDER BY b.NamaJual DESC";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode' 					=> $r['kode'],
                                                'nama' 					=> $r['nama'],
                                                'harga' 				=> $r['harga'],
                                                'hpp' 				    => $r['hpp']
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
    
    //--- Added by Shodiq ---//
    function getUsers_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "SELECT nomor, userid as nama FROM whuser_mobile where tipeuser = 0 order by nama desc";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'         	=> $r['nomor'],
                                                'nama' 				=> $r['nama']
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
}
