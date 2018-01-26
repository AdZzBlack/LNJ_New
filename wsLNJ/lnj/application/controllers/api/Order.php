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
        return str_replace( array("\t", "\n", "\r") , " ", $string);
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

	// --- Update untuk menyerahkan document thorderjual ke user lain melalui qrcode --- //
	function updateDoc_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $nomordoc = (isset($jsonObject["nomordoc"]) ? $this->clean($jsonObject["nomordoc"])     : "");
        $nomormhcabang = (isset($jsonObject["nomormhcabang"]) ? $this->clean($jsonObject["nomormhcabang"])     : "");
        $kodedoc = (isset($jsonObject["kodedoc"]) ? $jsonObject["kodedoc"]     : "");
        $nomormhadmin = (isset($jsonObject["nomormhadmin"]) ? $this->clean($jsonObject["nomormhadmin"])     : "");
        $nomorpenerima = (isset($jsonObject["nomorpenerima"]) ? $this->clean($jsonObject["nomorpenerima"])     : "");

        $query = "SELECT nomor FROM thorderjual WHERE nomor = $nomordoc AND (status_serahterima = 0 OR status_serahterima = 1)";  // untuk pengecekan jika document masih belum diaccept oleh user/admin lain
        $result = $this->db->query($query);

        if($result && $result->num_rows() > 0){  //jika document valid, maka lakukan update atau penyerahan dokumen ke user lain
            $this->db->trans_begin();
            $query = "	UPDATE thorderjual SET
                            nomormhadmin_docfinal_date = $nomormhadmin, status_serahterima = 1, nomormhadmin_penerima = $nomorpenerima, docfinal_date = NOW()
                        WHERE
                            nomor = $nomordoc
                        AND kode = '$kodedoc' ";

            $this->db->query($query);

            if ($this->db->trans_status() === FALSE)
            {
                $this->db->trans_rollback();
                array_push($data['data'], array( 'query' => $this->error($query),
                                                 'message' => 'Failed to update the data'));
            }
            else
            {
                //cek apakah dokumen ini sudah pernah disubmit ke tabel tlaporan_dokumen_distribusi
                $query = "  SELECT * FROM tlaporan_dokumen_distribusi WHERE nomorthorderjual = '$nomordoc' AND nomormhadmin_from = '$nomormhadmin' AND nomormhadmin_to = '$nomorpenerima' AND action = 'SUBMIT' ";
                $result = $this->db->query($query);
                if($result){
                    if($result->num_rows() <= 0){ //jika document belum pernah disubmit, maka lakukan insert data baru ke tabel tlaporan_dokumen_distribusi
                        $query = "	SELECT COUNT(*) as datarow, MONTH(NOW()) as month, YEAR(NOW()) as year FROM tlaporan_dokumen_distribusi ";
                        $result = $this->db->query($query);
                        if($result && $result->num_rows() > 0){
                            $row = $result->row();
                            $datarow = $row->datarow;
                            $newkode = "";
                            $month = $row->month;
                            $year = $row->year;
                            $prefix = "DOC";
                            $suffix = substr($year, 2);
                            $numeric = "00001";
                            if($month < 10){
                               $suffix = $suffix . "0" . $month;
                            }else{
                               $suffix = $suffix . $month;
                            }
                            if($datarow > 0){
                                $query = "	SELECT MAX(nomor) AS nomor FROM tlaporan_dokumen_distribusi";
                                $result = $this->db->query($query);
                                if($result && $result->num_rows() > 0){
                                    $row = $result->row();
                                    $maxnomor = $row->nomor;
                                    if($maxnomor < 10){
                                        $numeric = "0000" . $maxnomor;
                                    }else if($maxnomor < 100){
                                        $numeric = "000" . $maxnomor;
                                    }else if($maxnomor < 1000){
                                        $numeric = "00" . $maxnomor;
                                    }else if($maxnomor < 10000){
                                        $numeric = "0" . $maxnomor;
                                    }else{
                                        $numeric = $maxnomor;
                                    }
                                    $newkode = $prefix . $suffix . $numeric;
                                }else{
                                    $this->db->trans_rollback();
                                    array_push($data['data'], array( 'query' => $this->error($query),
                                                                     'message' => 'Failed to update the data'));
                                    if ($data){
                                        // Set the response and exit
                                        $this->response($data['data']); // OK (200) being the HTTP response code
                                    }
                                    die();
                                }
                            }else{
                                $newkode = $prefix . $suffix . $numeric;
                            }
                            $action = 'SUBMIT';
                            $query = " INSERT INTO tlaporan_dokumen_distribusi (nomormhcabang, nomormhadmin_from, nomormhadmin_to, nomorthorderjual, kodethorderjual, kode, action, tanggal, status_aktif) ".
                                     " VALUES ('$nomormhcabang', '$nomormhadmin', '$nomorpenerima', '$nomordoc', '$kodedoc', '$newkode', '$action', NOW(), '1')";
                            $result = $this->db->query($query);
                            if($result){
                                $this->db->trans_commit();
                                array_push($data['data'], array( 'message' => 'Your data has been successfully updated' ));
                            }else{
                                $this->db->trans_rollback();
                                array_push($data['data'], array( 'query' => $this->error($query),
                                                                 'message' => 'Failed to insert the data'));
                            }
                        }else{
                            $this->db->trans_rollback();
                            array_push($data['data'], array( 'query' => $this->error($query),
                                                             'message' => 'Failed to update the data'));
                        }
                    }else{
                        array_push($data['data'], array('query' => $this->error($query),
                                                        'message' => 'This document has already been scanned'));
                    }
                }else{
                    array_push($data['data'], array('query' => $this->error($query),
                                                    'message' => 'Failed to update the data'));
                }
            }
        }
        else{
            array_push($data['data'], array('query' => $this->error($query),
                                            'message' => 'This document has already accepted by other user'));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- Untuk mendapatkan list semua document yg dimiliki user yg login (dokumen yg masih pending / finish) beserta nomor laporan referensinya --- //
    function getDocList_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"])     : ""); //nomor user yg saat ini login / penerima
        $status = (isset($jsonObject["status"]) ? $this->clean($jsonObject["status"])     : ""); //status serah terima
        $status_serahterima = 1;  //status pending
        if($status == 'finish'){
            $status_serahterima = 2;  //status finish
        }

        $query = "	SELECT
                        a.nomor AS nomor,
                        c.nomortlaporan AS nomortlaporan_ref,
                        a.kode AS kode,
                        a.nomormhadmin_docfinal_date AS nomormhadmin,
                        a.docfinal_date AS tanggal,
                        b.nama AS nama
                    FROM thorderjual a
                    JOIN mhadmin b ON a.nomormhadmin_docfinal_date = b.nomor
                    JOIN tlaporan_dokumen_distribusi c ON a.nomormhadmin_penerima = c.nomormhadmin_to
                    WHERE a.status_aktif = 1
                    AND a.nomormhadmin_penerima = $nomor
                    AND a.status_serahterima = $status_serahterima
                    ORDER BY a.docfinal_date DESC ";

        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0)
        {
            foreach ($result->result_array() as $r)
            {
                array_push($data['data'], array(
                                                    'nomor'    	    		=> $r['nomor'],
                                                    'nomortlaporan_ref' 	=> $r['nomortlaporan_ref'],
                                                    'kode'                  => $r['kode'],
                                                    'nomormhadmin'          => $r['nomormhadmin'],
                                                    'tanggal'               => $r['tanggal'],
                                                    'nama' 					=> $r['nama']
                                            )
                );
            }
        }
        else
        {
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- accept documents--- //
    //TODO get nomormhadmin_from and nomortlaporan_ref
    function acceptDoc_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomordoc = (isset($jsonObject["nomordoc"]) ? $this->clean($jsonObject["nomordoc"])     : "");
        $nomormhadmin = (isset($jsonObject["nomormhadmin"]) ? $this->clean($jsonObject["nomormhadmin"])     : "");


        $this->db->trans_begin();
        $query = "	UPDATE thorderjual SET
                        status_serahterima = 2
                    WHERE
                        nomor = $nomordoc
                    AND
                        nomormhadmin_penerima = $nomormhadmin ";

        $this->db->query($query);

        if ($this->db->trans_status() === FALSE)
        {
            $this->db->trans_rollback();
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Failed to update the data'));
        }
        else
        {
            $this->db->trans_commit();
            array_push($data['data'], array( 'message' => 'Your data has been successfully updated' ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- accept documents--- //
    function acceptDocTest_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomordoc = (isset($jsonObject["nomordoc"]) ? $this->clean($jsonObject["nomordoc"])     : "");
        $nomormhadmin = (isset($jsonObject["nomormhadmin"]) ? $this->clean($jsonObject["nomormhadmin"])     : "");

        $this->db->trans_begin();
        $query = "	UPDATE thorderjual SET
                        status_serahterima = 2
                    WHERE
                        nomor = $nomordoc
                    AND
                        nomormhadmin_penerima = $nomormhadmin ";

        $this->db->query($query);

        if ($this->db->trans_status() === FALSE)
        {
            $this->db->trans_rollback();
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Failed to update the data'));
        }
        else
        {
            $query = "	SELECT COUNT(*) as datarow, MONTH(NOW()) as month, YEAR(NOW()) as year FROM tlaporan_dokumen_distribusi ";
            $result = $this->db->query($query);
            if($result && $result->num_rows() > 0){
                $row = $result->row();
                $datarow = $row->datarow;
                $newkode = "";
                $month = $row->month;
                $year = $row->year;
                $prefix = "DOC";
                $suffix = substr($year, 2);
                $numeric = "00001";
                if($month < 10){
                   $suffix = $suffix . "0" . $month;
                }else{
                   $suffix = $suffix . $month;
                }
                if($datarow > 0){
                    $query = "	SELECT MAX(nomor) AS nomor FROM tlaporan_dokumen_distribusi";
                    $result = $this->db->query($query);
                    if($result && $result->num_rows() > 0){
                        $row = $result->row();
                        $maxnomor = $row->nomor;
                        if($maxnomor < 10){
                            $numeric = "0000" . $maxnomor;
                        }else if($maxnomor < 100){
                            $numeric = "000" . $maxnomor;
                        }else if($maxnomor < 1000){
                            $numeric = "00" . $maxnomor;
                        }else if($maxnomor < 10000){
                            $numeric = "0" . $maxnomor;
                        }else{
                            $numeric = $maxnomor;
                        }
                        $newkode = $prefix . $suffix . $numeric;
                    }else{
                        $this->db->trans_rollback();
                        array_push($data['data'], array( 'query' => $this->error($query),
                                                         'message' => 'Failed to update the data'));
                        if ($data){
                            // Set the response and exit
                            $this->response($data['data']); // OK (200) being the HTTP response code
                        }
                        die();
                    }
                }else{
                    $newkode = $prefix . $suffix . $numeric;
                }
                $action = 'ACCEPT';
                $query = " INSERT INTO tlaporan_dokumen_distribusi (nomormhcabang, nomormhadmin_from, nomormhadmin_to, nomortlaporan_ref, nomorthorderjual, kodethorderjual, kode, action, tanggal, status_aktif) ".
                         " VALUES ('$nomormhcabang', '$nomormhadmin', '$nomorpenerima', '$nomortlaporan_ref', '$nomordoc', '$kodedoc', '$newkode', '$action', NOW(), '1') ";
                $result = $this->db->query($query);
                if($result){
                    $this->db->trans_commit();
                    array_push($data['data'], array( 'message' => 'Your data has been successfully updated' ));
                }else{
                    $this->db->trans_rollback();
                    array_push($data['data'], array( 'query' => $this->error($query),
                                                     'message' => 'Failed to insert the data'));
                }
            }else{
                $this->db->trans_rollback();
                array_push($data['data'], array( 'query' => $this->error($query),
                                                 'message' => 'Failed to update the data'));
            }
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // ---reject documents--- //
    function rejectDocTest_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomordoc = (isset($jsonObject["nomordoc"]) ? $this->clean($jsonObject["nomordoc"])     : "");
        $nomortlaporan_ref = (isset($jsonObject["nomortlaporan_ref"]) ? $this->clean($jsonObject["nomortlaporan_ref"])     : "");
        $nomormhadmin = (isset($jsonObject["nomormhadmin"]) ? $this->clean($jsonObject["nomormhadmin"])     : "");

        $this->db->trans_begin();
        $query = "	UPDATE thorderjual SET
                        status_serahterima = 0
                    WHERE
                        nomor = $nomordoc
                    AND
                        nomormhadmin_penerima = $nomormhadmin ";

        $this->db->query($query);

        if ($this->db->trans_status() === FALSE)
        {
            $this->db->trans_rollback();
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Failed to update the data'));
        }
        else
        {
            $query = "	SELECT COUNT(*) as datarow, MONTH(NOW()) as month, YEAR(NOW()) as year FROM tlaporan_dokumen_distribusi ";
            $result = $this->db->query($query);
            if($result && $result->num_rows() > 0){
                $row = $result->row();
                $datarow = $row->datarow;
                $newkode = "";
                $month = $row->month;
                $year = $row->year;
                $prefix = "DOC";
                $suffix = substr($year, 2);
                $numeric = "00001";
                if($month < 10){
                   $suffix = $suffix . "0" . $month;
                }else{
                   $suffix = $suffix . $month;
                }
                if($datarow > 0){
                    $query = "	SELECT MAX(nomor) AS nomor FROM tlaporan_dokumen_distribusi";
                    $result = $this->db->query($query);
                    if($result && $result->num_rows() > 0){
                        $row = $result->row();
                        $maxnomor = $row->nomor;
                        if($maxnomor < 10){
                            $numeric = "0000" . $maxnomor;
                        }else if($maxnomor < 100){
                            $numeric = "000" . $maxnomor;
                        }else if($maxnomor < 1000){
                            $numeric = "00" . $maxnomor;
                        }else if($maxnomor < 10000){
                            $numeric = "0" . $maxnomor;
                        }else{
                            $numeric = $maxnomor;
                        }
                        $newkode = $prefix . $suffix . $numeric;
                    }else{
                        $this->db->trans_rollback();
                        array_push($data['data'], array( 'query' => $this->error($query),
                                                         'message' => 'Failed to update the data'));
                        if ($data){
                            // Set the response and exit
                            $this->response($data['data']); // OK (200) being the HTTP response code
                        }
                        die();
                    }
                }else{
                    $newkode = $prefix . $suffix . $numeric;
                }
                $action = 'REJECT';
                $query = " INSERT INTO tlaporan_dokumen_distribusi (nomormhcabang, nomormhadmin_from, nomormhadmin_to, nomortlaporan_ref, nomorthorderjual, kodethorderjual, kode, action, tanggal, status_aktif) ".
                         " VALUES ('$nomormhcabang', '$nomormhadmin', '$nomorpenerima', '$nomortlaporan_ref', '$nomordoc', '$kodedoc', '$newkode', '$action', NOW(), '1') ";
                $result = $this->db->query($query);
                if($result){
                    $this->db->trans_commit();
                    array_push($data['data'], array( 'message' => 'Your data has been successfully updated' ));
                }else{
                    $this->db->trans_rollback();
                    array_push($data['data'], array( 'query' => $this->error($query),
                                                     'message' => 'Failed to insert the data'));
                }
            }else{
                $this->db->trans_rollback();
                array_push($data['data'], array( 'query' => $this->error($query),
                                                 'message' => 'Failed to update the data'));
            }
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // ---reject documents--- //
    function rejectDoc_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomordoc = (isset($jsonObject["nomordoc"]) ? $this->clean($jsonObject["nomordoc"])     : "");
        $nomormhadmin = (isset($jsonObject["nomormhadmin"]) ? $this->clean($jsonObject["nomormhadmin"])     : "");

        $this->db->trans_begin();
        $query = "	UPDATE thorderjual SET
                        status_serahterima = 0
                    WHERE
                        nomor = $nomordoc
                    AND
                        nomormhadmin_penerima = $nomormhadmin ";

        $this->db->query($query);

        if ($this->db->trans_status() === FALSE)
        {
            $this->db->trans_rollback();
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Failed to update the data'));
        }
        else
        {
            $this->db->trans_commit();
            array_push($data['data'], array( 'message' => 'Your data has been successfully updated' ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
	
	// --- Untuk mendapatkan list semua user untuk diserahi document --- //
    function getUserList_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"])     : ""); //nomor user yg saat ini login

        $query = "	SELECT
                        a.nomor AS nomor,
                        a.kode AS kode,
                        a.sandi AS `password`,
                        a.nomormhpegawai AS nomor_pegawai,
                        d.kode AS kode_pegawai,
                        a.nama AS nama,
                        a.role_android AS role,
                        a.hash AS `hash`,
                        a.nomormhcabang AS cabang,
                        e.nama AS namacabang,
                        b.isdriver AS isdriver,
                        b.qrcodereader AS qrcodereader,
                        b.checkin AS checkin
                    FROM mhadmin a
                    JOIN whrole_mobile b ON a.role_android = b.nomor
                    LEFT JOIN mhpegawai d ON a.nomormhpegawai = d.nomor
                    JOIN mhcabang e ON a.nomormhcabang = e.nomor
                    WHERE a.status_aktif = 1
                    AND a.nomor <> $nomor ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0)
        {
            foreach ($result->result_array() as $r)
            {
                array_push($data['data'], array(
                                                    'success'				=> "true",
                                                    'nomor'    	    		=> $r['nomor'],
                                                    'kode'                  => $r['kode'],
                                                    'password'				=> $r['password'],
                                                    'nomor_pegawai'         => $r['nomor_pegawai'],
                                                    'kode_pegawai'        	=> $r['kode_pegawai'],
                                                    'nama' 					=> $r['nama'],
                                                    'role' 					=> $r['role'],
                                                    'hash' 					=> $r['hash'],
                                                    'cabang' 				=> $r['cabang'],
                                                    'nama_cabang' 			=> $r['namacabang'],
                                                    'isdriver'				=> $r['isdriver'],
                                                    'qrcodereader' 			=> $r['qrcodereader'],
                                                    'checkin'				=> $r['checkin']
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
}
