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
class Track extends REST_Controller {

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

    // --- Insert History Driver--- //
    function InsertHistory_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $user_nomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"])     : "");
        $job_nomor = (isset($jsonObject["job_nomor"]) ? $this->clean($jsonObject["job_nomor"])     : "");
        $latitude = (isset($jsonObject["latitude"]) ? $this->clean($jsonObject["latitude"])     : "");
        $longitude = (isset($jsonObject["longitude"]) ? $this->clean($jsonObject["longitude"])     : "");

        $this->db->trans_begin();
        $query = " INSERT INTO whhistory_mobile
                    (nomorjob, nomoruser, lat, lon)
                    VALUES
                    ($job_nomor, $user_nomor, $latitude, $longitude) ";
        $this->db->query($query);

        $this->db->query($query);

        if ($this->db->trans_status() === FALSE)
        {
            $this->db->trans_rollback();
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Failed to insert history'));
        }
        else
        {
            $this->db->trans_commit();
            array_push($data['data'], array( 'message' => 'Your data has been successfully inserted' ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

	// --- Get all waypoints data --- //
	function getWaypoints_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $query = "SELECT nomor, kode, nama, durasi, radius, latitude, longitude, keterangan ".
                 "FROM ".
                 "  mhwaypoint ".
                 "WHERE ".
                 "  status_aktif = 1 ".
                 "  AND ".
                 "  radius is not null ".
                 "  AND ".
                 "  latitude is not null ".
                 "  AND ".
                 "  longitude is not null ";  // untuk pengecekan jika document masih belum diaccept oleh user/admin lain
        $result = $this->db->query($query);

        if($result && $result->num_rows() > 0){  //jika document valid, maka lakukan update atau penyerahan dokumen ke user lain
            foreach ($result->result_array() as $r)
            {
                array_push($data['data'], array(
                                                'nomor'    	    		=> $r['nomor'],
                                                'kode'                  => $r['kode'],
                                                'nama'                  => $r['nama'],
                                                'durasi'                => $r['durasi'],
                                                'radius'                => $r['radius'],
                                                'latitude'              => $r['latitude'],
                                                'longitude'             => $r['longitude'],
                                                'keterangan' 			=> $r['keterangan']
                                        )
                );
            }
        }else{
            array_push($data['data'], array('query' => $this->error($query),
                                            'message' => 'No waypoint data'));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- Untuk mendapatkan list semua event dari tabel mhcheckpoint
    function getEvent_post()
    {
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "SELECT nomor, kode, nama ".
                 "FROM ".
                 "  mhcheckpoint ".
                 "WHERE ".
                 "  status_aktif = 1 ";
        $result = $this->db->query($query);

        if($result && $result->num_rows() > 0){  //jika document valid, maka lakukan update atau penyerahan dokumen ke user lain
            foreach ($result->result_array() as $r)
            {
                array_push($data['data'], array(
                                                'nomor'    	    		=> $r['nomor'],
                                                'kode'                  => $r['kode'],
                                                'nama'                  => $r['nama']
                                        )
                );
            }
        }else{
            array_push($data['data'], array('query' => $this->error($query),
                                            'message' => 'No Event data'));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- Untuk mendapatkan list semua event yang telah dipilih (untuk tampilan checked)
    function getEventDetail_post()
    {
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomormhwaypoint = (isset($jsonObject["nomormhwaypoint"]) ? $this->clean($jsonObject["nomormhwaypoint"])     : "");

        $query = "SELECT nomormhcheckpoint ".
                 "FROM ".
                 "  mdwaypoint ".
                 "WHERE ".
                 "  nomormhwaypoint = $nomormhwaypoint ".
                 "  AND status_aktif = 1 ";
        $result = $this->db->query($query);

        if($result && $result->num_rows() > 0){  //jika document valid, maka lakukan update atau penyerahan dokumen ke user lain
            foreach ($result->result_array() as $r)
            {
                array_push($data['data'], array(
                                                'nomormhcheckpoint' 	   => $r['nomormhcheckpoint']
                                        )
                );
            }
        }else{
            array_push($data['data'], array('query' => $this->error($query),
                                            'message' => 'No Event data'));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- insert waypoints--- //
    function insertWaypoint_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nama = (isset($jsonObject["nama"]) ? $this->clean($jsonObject["nama"])     : "");
        $duration = (isset($jsonObject["duration"]) ? $this->clean($jsonObject["duration"])     : "");
        $radius = (isset($jsonObject["radius"]) ? $this->clean($jsonObject["radius"])     : "");
        $latitude = (isset($jsonObject["latitude"]) ? $jsonObject["latitude"]     : "");
        $longitude = (isset($jsonObject["longitude"]) ? $jsonObject["longitude"]     : "");
        $keterangan = (isset($jsonObject["keterangan"]) ? $this->clean($jsonObject["keterangan"])     : "");

        $arrnomorcheckpoint = (isset($jsonObject["arrnomorcheckpoint"]) ? $jsonObject["arrnomorcheckpoint"]     : "");

        $query = "SELECT MAX(substr(kode, 3) + 1) AS maxkode FROM mhwaypoint";  //untuk mendapatkan no urut baru pada tabel mhwaypoint
        $result = $this->db->query($query);
        $nourut = 0;

        if($result && $result->num_rows() > 0){
            $row = $result->row();
            $nourut = $row->maxkode;
//            array_push($data['data'], array('nourut' => $nourut));
//
//            if ($data){
//                // Set the response and exit
//                $this->response($data['data']); // OK (200) being the HTTP response code
//            }
//            die();
            $formattedurutan = "null";
            if($nourut < 10){
                $formattedurutan = "0000".$nourut;
            }else if($nourut < 100){
                $formattedurutan = "000".$nourut;
            }else if($nourut < 1000){
                $formattedurutan = "00".$nourut;
            }else if($nourut < 10000){
                $formattedurutan = "0".$nourut;
            }else{
                $formattedurutan = $nourut;
            }
            $newkode = "WP".$formattedurutan;

            $this->db->trans_begin();
            $query = "	INSERT INTO mhwaypoint (kode, nama, durasi, radius, latitude, longitude, keterangan) VALUES ('$newkode', '$nama', $duration, $radius, $latitude, $longitude, '$keterangan') ";
            $this->db->query($query);
            if ($this->db->trans_status() === FALSE)
            {
                $this->db->trans_rollback();
                array_push($data['data'], array( 'query' => $this->error($query),
                                                 'message' => 'Failed to save the data'));
            }
            else
            {
                $lastid = 0;
                //get last insert id on header
                $query = "	SELECT LAST_INSERT_ID() AS lastid";
                $result = $this->db->query($query);
                if($result && $result->num_rows() > 0){
                    $row = $result->row();
                    $lastid = $row->lastid;
                }else{
                    $this->db->trans_rollback();
                    array_push($data['data'], array( 'query' => $this->error($query),
                                                     'message' => 'Failed to save the data'));
                    if ($data){
                        // Set the response and exit
                        $this->response($data['data']); // OK (200) being the HTTP response code
                    }
                    die();
                }
                ///////////////////////////////
                //insert mdwaypoint
                $pieces = explode("~", $arrnomorcheckpoint);
                for ($i = 0; $i < count($pieces) - 1; $i++) {
                    $query = " INSERT INTO mdwaypoint (nomormhwaypoint, nomormhcheckpoint) VALUES ($lastid, $pieces[$i]) ";
                    $this->db->query($query);
                    if ($this->db->trans_status() === FALSE)
                    {
                        $this->db->trans_rollback();
                        array_push($data['data'], array( 'query' => $this->error($query),
                                                         'message' => 'Failed to save the data'));
                        if ($data){
                            // Set the response and exit
                            $this->response($data['data']); // OK (200) being the HTTP response code
                        }
                        die();
                    }
                }
                ////
                if ($this->db->trans_status() === FALSE)
                {
                    $this->db->trans_rollback();
                    array_push($data['data'], array( 'query' => $this->error($query),
                                                     'message' => 'Failed to save the data'));
                    if ($data){
                        // Set the response and exit
                        $this->response($data['data']); // OK (200) being the HTTP response code
                    }
                    die();
                }
                else
                {
                    $this->db->trans_commit();
                    array_push($data['data'], array( 'message' => 'Your data has been successfully saved' ));
                }
            }
        }else{
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Failed to save the data'));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- update waypoint--- //
    function updateWaypoint_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"])     : "");
        $nama = (isset($jsonObject["nama"]) ? $this->clean($jsonObject["nama"])     : "");
        $duration = (isset($jsonObject["duration"]) ? $this->clean($jsonObject["duration"])     : "");
        $radius = (isset($jsonObject["radius"]) ? $this->clean($jsonObject["radius"])     : "");
        $latitude = (isset($jsonObject["latitude"]) ? $jsonObject["latitude"]     : "");
        $longitude = (isset($jsonObject["longitude"]) ? $jsonObject["longitude"]     : "");
        $keterangan = (isset($jsonObject["keterangan"]) ? $this->clean($jsonObject["keterangan"])     : "");
        $arrnomorcheckpoint = (isset($jsonObject["arrnomorcheckpoint"]) ? $jsonObject["arrnomorcheckpoint"]     : "");

        $this->db->trans_begin();
        $query = " UPDATE mhwaypoint SET nama = '$nama', durasi = $duration, radius = $radius, latitude = $latitude, longitude = $longitude, keterangan = '$keterangan' WHERE nomor = $nomor ";

        $this->db->query($query);

        if ($this->db->trans_status() === FALSE)
        {
            $this->db->trans_rollback();
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Failed to update the data'));
        }
        else
        {
            $query = " DELETE FROM mdwaypoint WHERE nomormhwaypoint = $nomor ";
            $this->db->query($query);
            if ($this->db->trans_status() === FALSE)
            {
                $this->db->trans_rollback();
                array_push($data['data'], array( 'query' => $this->error($query),
                                                 'message' => 'Failed to update the data'));
            }else{
                $pieces = explode("~", $arrnomorcheckpoint);
                for ($i = 0; $i < count($pieces) - 1; $i++) {
                    $query = " INSERT INTO mdwaypoint (nomormhwaypoint, nomormhcheckpoint) VALUES ($nomor, $pieces[$i]) ";
                    $this->db->query($query);
                    if ($this->db->trans_status() === FALSE)
                    {
                        $this->db->trans_rollback();
                        array_push($data['data'], array( 'query' => $this->error($query),
                                                         'message' => 'Failed to save the data'));
                        if ($data){
                            // Set the response and exit
                            $this->response($data['data']); // OK (200) being the HTTP response code
                        }
                        die();
                    }
                }
                if ($this->db->trans_status() === FALSE)
                {
                    $this->db->trans_rollback();
                    array_push($data['data'], array( 'query' => $this->error($query),
                                                     'message' => 'Failed to update the data'));
                }else{
                    $this->db->trans_commit();
                    array_push($data['data'], array( 'message' => 'Your data has been successfully updated' ));
                }
            }
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- delete waypoints--- //
    function deleteWaypoint_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"])     : "");

        $this->db->trans_begin();
        $query = "DELETE FROM mhwaypoint WHERE nomor = $nomor ";

        $this->db->query($query);

        if ($this->db->trans_status() === FALSE)
        {
            $this->db->trans_rollback();
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Failed to delete the data'));
        }
        else
        {
            $this->db->trans_commit();
            array_push($data['data'], array( 'message' => 'Your data has been successfully deleted' ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
}