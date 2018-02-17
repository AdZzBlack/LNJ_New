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
class Scanning extends REST_Controller {

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

	// --- Save document melalui qrcode ke tabel whqrcoderequest_mobile --- //
	function saveDoc_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $nomormhadmin = (isset($jsonObject["nomormhadmin"]) ? $this->clean($jsonObject["nomormhadmin"])     : "");
        $tipe = (isset($jsonObject["tipe"]) ? $this->clean($jsonObject["tipe"])     : "");
        $nomordokumen = (isset($jsonObject["nomordokumen"]) ? $this->clean($jsonObject["nomordokumen"])     : "");
		$kodedokumen = (isset($jsonObject["kodedokumen"]) ? $this->clean($jsonObject["kodedokumen"])     : "");
		$urldokumen = (isset($jsonObject["urldokumen"]) ? $jsonObject["urldokumen"]     : "");

        $this->db->trans_begin();
		$query = "	INSERT INTO whqrcoderequest_mobile
		                (nomormhadmin, tipe, nomordokumen, kodedokumen, url_menu, dibuat_pada)
		            VALUES
		                ($nomormhadmin, '$tipe', $nomordokumen, '$kodedokumen', '$urldokumen', NOW()) ";

        $this->db->query($query);

		if ($this->db->trans_status() === FALSE)
        {
            $this->db->trans_rollback();
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Failed to add the data'));
        }
        else
        {
            $this->db->trans_commit();
            array_push($data['data'], array( 'message' => 'Your data has been successfully added' ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- check valid Delivery Order and then assign (update) it to a driver --- //
    function saveDeliveryOrder_post()
    {
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomormhadmin = (isset($jsonObject["nomormhadmin"]) ? $this->clean($jsonObject["nomormhadmin"])     : "");
        $nomorthsuratjalan = (isset($jsonObject["nomorthsuratjalan"]) ? $this->clean($jsonObject["nomorthsuratjalan"])     : "");
        $nomortdsuratjalan = (isset($jsonObject["nomortdsuratjalan"]) ? $this->clean($jsonObject["nomortdsuratjalan"])     : "");
        $doctype = (isset($jsonObject["doctype"]) ? $this->clean($jsonObject["doctype"])     : "");
        $query = "  SELECT status_selesai FROM thsuratjalan WHERE nomor = $nomorthsuratjalan ";
        if($doctype == "tdsuratjalan")
            $query = "  SELECT status_selesai, nomormhadmin_driver FROM tdsuratjalan WHERE nomor = $nomortdsuratjalan ";
        $result = $this->db->query($query);
        if($result && $result->num_rows() > 0){
//            foreach ($result->result_array() as $r)
//            {
//                array_push($data['data'], array(
//                                                'status_selesai'    	    => $r['status_selesai'],
//                                                'nomormhadmin_driver'    	=> $r['nomormhadmin_driver']
//                                        )
//                );
//            }
            $row = $result->row();
            $status_selesai = $row->status_selesai;
            $nomormhadmin_driver = $row->nomormhadmin_driver;
            if($status_selesai == 0 && $nomormhadmin_driver == 0){
                $this->db->trans_begin();
                $query = "  UPDATE tdsuratjalan SET nomormhadmin_driver = $nomormhadmin WHERE nomor = $nomortdsuratjalan ";
                $result = $this->db->query($query);
                if($result){
                    $this->db->trans_commit();
                    array_push($data['data'], array( 'message' => 'Document has been successfully added to the list' ));
                }else{
                    $this->db->trans_rollback();
                    array_push($data['data'], array( 'query' => $this->error($query),
                                                     'message' => 'Failed to add the data'));
                }
            }else if($nomormhadmin_driver > 0){
                if($nomormhadmin_driver != $nomormhadmin){
                    array_push($data['data'], array( 'query' => $this->error($query),
                                                     'message' => 'This document is already given to another driver'));
                }else{
                    array_push($data['data'], array( 'query' => $this->error($query),
                                                     'message' => 'This document is already scanned'));
                }
            }else{
                array_push($data['data'], array( 'query' => $this->error($query),
                                                 'message' => 'This document is already delivered, please scan another document'));
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Document not found. Please try again with a valid document'));
        }
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- get list suratjalan yang dibawa oleh driver dari tabel tdsuratjalan --- //
    function getDeliveryOrderList_post()
    {
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        $nomormhadmin = (isset($jsonObject["nomormhadmin"]) ? $this->clean($jsonObject["nomormhadmin"])     : "");
        $query = "  SELECT nomor FROM tdsuratjalan WHERE status_selesai = 0 AND nomormhadmin_driver = $nomormhadmin AND keterangan_batal IS NULL OR keterangan_batal = '' ";
        $result = $this->db->query($query);
        if($result){
            if($result->num_rows() > 0){
                foreach ($result->result_array() as $r)
                {
                    array_push($data['data'], array(
                                                    'nomor'    	    => $r['nomor']
                                            )
                    );
                }
            }else{
                array_push($data['data'], array('message' => 'No data'));
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Failed to retrieve the data'));
        }
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- menghapus/membatalkan suratjalan yang dibawa oleh driver dari tabel tdsuratjalan dengan MENGUPDATE keterangan_batal --- //
    function deleteDeliveryOrder_post()
    {
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomormhadmin = (isset($jsonObject["nomormhadmin"]) ? $this->clean($jsonObject["nomormhadmin"])     : "");
        $nomortdsuratjalan = (isset($jsonObject["nomortdsuratjalan"]) ? $this->clean($jsonObject["nomortdsuratjalan"])     : "");
        $keterangan_batal = (isset($jsonObject["keterangan_batal"]) ? $this->clean($jsonObject["keterangan_batal"])     : "");
        $query = "  UPDATE tdsuratjalan SET keterangan_batal = '$keterangan_batal', status_selesai = 0 WHERE nomormhadmin_driver = $nomormhadmin ";
        $this->db->trans_begin();
        $result = $this->db->query($query);
        if(!$result){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Delete data failed'));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'message' => 'Success'));
        }
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

	// --- get checkpoint dari skenario --- //
	function getCheckpointList_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $doctype = (isset($jsonObject["doctype"]) ? $this->clean($jsonObject["doctype"])     : "");
        $nomorthsuratjalan = (isset($jsonObject["nomorthsuratjalan"]) ? $this->clean($jsonObject["nomorthsuratjalan"])     : "");
        $nomortdsuratjalan = (isset($jsonObject["nomortdsuratjalan"]) ? $this->clean($jsonObject["nomortdsuratjalan"])     : "");
        $query = "  SELECT nomormhskenario_manual FROM tdsuratjalan WHERE nomor = $nomortdsuratjalan";
        if($doctype == "tdsuratjalan"){
            $query = "  SELECT nomormhskenario_manual FROM tdsuratjalan WHERE nomor = $nomortdsuratjalan ";
        }
//        }else{
//            $query = "  SELECT nomormhskenario_manual FROM thsuratjalan WHERE nomor = $nomorthsuratjalan ";
//        }
        $result = $this->db->query($query);
        if($result && $result->num_rows() > 0){
            $row = $result->row();
            $nomorskenario = $row->nomormhskenario_manual;
            if($nomorskenario > 0){
                $query = "  SELECT a.nomor, a.nama FROM mhcheckpoint a " .
                         "  JOIN mdskenario b " .
                         "    ON b.nomormhcheckpoint = a.nomor" .
                         "  JOIN tdsuratjalan c " .
                         "    ON c.nomormhskenario_manual = b.nomormhskenario " .
                         "  WHERE b.nomormhskenario = $nomorskenario ";
                $result = $this->db->query($query);
                if($result && $result->num_rows() > 0){
                    foreach ($result->result_array() as $r)
                    {
                        array_push($data['data'], array(
                                                        'nomor'    	    => $r['nomor'],
                                                        'nama'    	    => $r['nama']
                                                )
                        );
                    }
                }else{
                    array_push($data['data'], array( 'query' => $this->error($query),
                                                     'message' => 'Failed to add the data'));
                }
            }else{
                array_push($data['data'], array( 'query' => $this->error($query),
                                                                     'message' => 'There is no scenario for this document'));
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Failed to retrieve the data'));
        }
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- get checkpoint dari skenario --- //
    function getCheckInHistory_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomorthsuratjalan = (isset($jsonObject["nomorthsuratjalan"]) ? $this->clean($jsonObject["nomorthsuratjalan"])     : "");
        $nomortdsuratjalan = (isset($jsonObject["nomortdsuratjalan"]) ? $this->clean($jsonObject["nomortdsuratjalan"])     : "");
        $nomoruser = (isset($jsonObject["nomoruser"]) ? $this->clean($jsonObject["nomoruser"])     : "");
        $query = "  SELECT kodecontainer, checkpoint, nomorsopir, lat, lon, dibuat_pada AS tanggal FROM whcheckin_mobile WHERE nomortdsuratjalan = $nomortdsuratjalan AND nomorsopir = $nomoruser";
        $result = $this->db->query($query);
        if($result){
            if($result->num_rows() > 0){
                foreach ($result->result_array() as $r)
                {
                    array_push($data['data'], array(
                                                    'kodecontainer'    	    => $r['kodecontainer'],
                                                    'checkpoint'    	    => $r['checkpoint'],
                                                    'lat'            	    => $r['lat'],
                                                    'lon'    	            => $r['lon'],
                                                    'tanggal'    	        => $r['tanggal']
                                            )
                    );
                }
            }else{
                array_push($data['data'], array( 'query' => $this->error($query),
                                                 'message' => 'No history data for this document'));
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Failed to retrieve the data'));
        }
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

	// --- TIDAK DIPAKAI --- //
    function checkIn_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomorthsuratjalan = (isset($jsonObject["nomorthsuratjalan"]) ? $this->clean($jsonObject["nomorthsuratjalan"])     : "");
        $nomortdsuratjalan = (isset($jsonObject["nomortdsuratjalan"]) ? $this->clean($jsonObject["nomortdsuratjalan"])     : "");
        $nomorsopir = (isset($jsonObject["nomorsopir"]) ? $this->clean($jsonObject["nomorsopir"])     : "");
        $checkpoint = (isset($jsonObject["checkpoint"]) ? $this->clean($jsonObject["checkpoint"])     : "");
        $lat = (isset($jsonObject["lat"]) ? $jsonObject["lat"]     : "");
        $lon = (isset($jsonObject["lon"]) ? $jsonObject["lon"]     : "");

        $query = "  SELECT kodecontainer FROM tdsuratjalan WHERE nomor = $nomortdsuratjalan ";
        $result = $this->db->query($query);

        if($result && $result->num_rows() > 0){
            $row = $result->row();
//            $nomorcontainer = $row->nomormhcontainer;
            $kodecontainer = $row->kodecontainer;
            $this->db->trans_begin();
            $query = "	INSERT INTO whcheckin_mobile
                            (nomortdsuratjalan, kodecontainer, checkpoint, nomorsopir, lat, lon, dibuat_pada)
                        VALUES
                            ($nomortdsuratjalan, '$kodecontainer', '$checkpoint', $nomorsopir, $lat, $lon, NOW()) ";

            $this->db->query($query);

            if ($this->db->trans_status() === FALSE)
            {
                $this->db->trans_rollback();
                array_push($data['data'], array( 'query' => $this->error($query),
                                                 'message' => 'Failed to add the data'));
            }
            else
            {
                $this->db->trans_commit();
                array_push($data['data'], array( 'message' => 'Your data has been successfully added' ));
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query),
                                                         'message' => 'Failed to add the data'));
        }
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- Check in tracking --- //
    function endCheckIn_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

//        $nomorthsuratjalan = (isset($jsonObject["nomorthsuratjalan"]) ? $this->clean($jsonObject["nomorthsuratjalan"])     : "");  //BELUM DIPAKAI
        $nomortdsuratjalan = (isset($jsonObject["nomortdsuratjalan"]) ? $this->clean($jsonObject["nomortdsuratjalan"])     : "");
        $nomorsopir = (isset($jsonObject["nomorsopir"]) ? $this->clean($jsonObject["nomorsopir"])     : "");
        $checkpoint = 'SELESAI';
        $lat = (isset($jsonObject["lat"]) ? $jsonObject["lat"]     : "");
        $lon = (isset($jsonObject["lon"]) ? $jsonObject["lon"]     : "");

        $query = "  SELECT kodecontainer FROM tdsuratjalan WHERE nomor = $nomortdsuratjalan ";
        $result = $this->db->query($query);

        if($result && $result->num_rows() > 0){
            $row = $result->row();
//            $nomorcontainer = $row->nomormhcontainer;
            $kodecontainer = $row->kodecontainer;
            $this->db->trans_begin();
            $query = "	INSERT INTO whcheckin_mobile
                            (nomortdsuratjalan, kodecontainer, checkpoint, nomorsopir, lat, lon, dibuat_pada)
                        VALUES
                            ($nomortdsuratjalan, '$kodecontainer', '$checkpoint', $nomorsopir, $lat, $lon, NOW()) ";

            $this->db->query($query);

            if ($this->db->trans_status() === FALSE)
            {
                $this->db->trans_rollback();
                array_push($data['data'], array( 'query' => $this->error($query),
                                                 'message' => 'Failed to submit the data'));
            }
            else
            {
                $query = "	UPDATE tdsuratjalan SET status_selesai = 1 WHERE nomor = $nomortdsuratjalan ";
                $this->db->query($query);
                if ($this->db->trans_status() === FALSE)
                {
                    $this->db->trans_rollback();
                    array_push($data['data'], array( 'query' => $this->error($query),
                                                     'message' => 'Failed to submit the data'));
                }
                else
                {
                    $this->db->trans_commit();
                    array_push($data['data'], array( 'message' => 'Delivery Order has been completed' ));
                }
            }
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- Get lat lon, and radius from next waypoint --- //
    function GetNextWP_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $user_nomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"])     : "");
        $job_nomor = (isset($jsonObject["job_nomor"]) ? $this->clean($jsonObject["job_nomor"])     : "");

        //get current nomormdskenario
        $query = "SELECT FC_GENERATE_NOMORMDSKENARIO_CHECKPOINT_NOW('$job_nomor','AUTO') AS nomormdskenario_now ";
        $result = $this->db->query($query);
        $nomormdskenario_now = '0';
        if($result && $result->num_rows() > 0){
            $row = $result->row();
            $nomormdskenario_now = $row->nomormdskenario_now;
        }else{
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Failed to retrieve the last scenario'));
            if ($data){
                // Set the response and exit
                $this->response($data['data']); // OK (200) being the HTTP response code
            }
            die();
        }

        //if there is no nomormdskenario_now
        if($nomormdskenario_now == '0' || $nomormdskenario_now == ''){
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'There is no new scenario '. $nomormdskenario_now));
        }else{
            //get next DO checkpoint, waypoint, radius
            $query = " SELECT a.nomor AS nomormdskenario, a.nomormhwaypoint, a.tipe, b.nama AS namacheckpoint, c.radius, c.latitude, c.longitude, c.idle_time
                       FROM mdskenario a
                       LEFT JOIN mhcheckpoint b
                         ON b.nomor = a.nomormhcheckpoint
                       JOIN mhwaypoint c
                         ON c.nomor = a.nomormhwaypoint
                       WHERE a.nomor = '$nomormdskenario_now'
                       LIMIT 1 ";
            $result = $this->db->query($query);
            if($result){
                if($result->num_rows() > 0){
                    foreach ($result->result_array() as $r)
                    {
                        array_push($data['data'], array(
                                                        'nomormdskenario_next'	=> $r['nomormdskenario'],
                                                        'nomormhwaypoint_next'  => $r['nomormhwaypoint'],
                                                        'namacheckpoint_next'   => $r['namacheckpoint'],
                                                        'checkin_type'          => $r['tipe'],
                                                        'radius_next'           => $r['radius'],
                                                        'latitude_next'         => $r['latitude'],
                                                        'longitude_next'        => $r['longitude'],
                                                        'idletime_next'         => $r['idle_time']
                                                )
                        );
                    }
                }else{
                    array_push($data['data'], array( 'query' => $this->error($query),
                                                     'message' => 'No new waypoint'));
                }
            }else{
                array_push($data['data'], array( 'query' => $this->error($query),
                                                 'message' => 'Failed to get next waypoint'));
            }
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    //--- Checkin to next skenario --- //  FROM LIANA (TEST)
    function InsertCheckin_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomorsopir = (isset($jsonObject["nomorsopir"]) ? $this->clean($jsonObject["nomorsopir"])     : "");
        $nomortdsuratjalan = (isset($jsonObject["nomortdsuratjalan"]) ? $this->clean($jsonObject["nomortdsuratjalan"])     : "");
        $checkpoint = (isset($jsonObject["checkpoint"]) ? $this->clean($jsonObject["checkpoint"])     : "");
        $checkin_type = (isset($jsonObject["checkin_type"]) ? $this->clean($jsonObject["checkin_type"])     : "");
        $checkin_mode = (isset($jsonObject["checkin_mode"]) ? $this->clean($jsonObject["checkin_mode"])     : "");
        $lat = (isset($jsonObject["latitude"]) ? $jsonObject["latitude"]     : "");
        $lon = (isset($jsonObject["longitude"]) ? $jsonObject["longitude"]     : "");

        //get next nomormdskenario AUTO
        $query = "SELECT FC_GENERATE_NOMORMDSKENARIO_CHECKPOINT_NOW('$nomortdsuratjalan','AUTO') AS nomormdskenario_new ";
        if($checkin_mode == 'MANUAL'){
            $query = "SELECT FC_GENERATE_NOMORMDSKENARIO_CHECKPOINT_NOW('$nomortdsuratjalan','MANUAL') AS nomormdskenario_new ";
        }
        $result = $this->db->query($query);

        if($result && $result->num_rows() > 0){
            $row = $result->row();
            $nomormdskenario_new = $row->nomormdskenario_new;
            if($nomormdskenario_new == 0 || $nomormdskenario_new == ''){
                array_push($data['data'], array('query' => $this->error($query),
                                                'message' => 'No scenario auto for this DO'));
            }else{
                if($checkin_mode == 'MANUAL'){
                    $query = "SELECT tipe FROM mdskenario WHERE nomor = '$nomormdskenario_new' ";
                    $result = $this->db->query($query);
                    $checkin_type = "IN";
                    if($result && $result->num_rows() > 0){
                        $row = $result->row();
                        $checkin_type = $row->tipe;
                    }else{
                        array_push($data['data'], array('message' => 'Failed to retrieve checkin_type'));
                        if ($data){
                            // Set the response and exit
                            $this->response($data['data']); // OK (200) being the HTTP response code
                        }
                        die();
                    }
                }
                $query = "SELECT kodecontainer FROM tdsuratjalan WHERE nomor = $nomortdsuratjalan ";
                $result = $this->db->query($query);
                if($result && $result->num_rows() > 0){
                    $row = $result->row();
                    $kodecontainer = $row->kodecontainer;
                    $this->db->trans_begin();
                    $query = "	INSERT INTO whcheckin_mobile
                                    (nomortdsuratjalan, nomormdskenario, kodecontainer, checkpoint, checkin_type, checkin_mode, nomorsopir, lat, lon, dibuat_pada)
                                VALUES
                                    ('$nomortdsuratjalan', '$nomormdskenario_new', '$kodecontainer', '$checkpoint', '$checkin_type', '$checkin_mode', '$nomorsopir', '$lat', '$lon', NOW()) ";
                    $this->db->query($query);
                    if ($this->db->trans_status() === FALSE)
                    {
                        $this->db->trans_rollback();
                        array_push($data['data'], array('query' => $this->error($query),
                                                        'message' => 'Failed to insert whcheckin'));
                    }else{
                        $this->db->trans_commit();
                        array_push($data['data'], array('message' => 'insert whcheckin success'));
                    }
                }
            }
        }else{
            array_push($data['data'], array('query' => $this->error($query),
                                            'message' => 'No scenario data'));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
}
