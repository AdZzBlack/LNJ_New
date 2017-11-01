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

	// --- Save document melalui qrcode --- //
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
	
	// --- Check in tracking --- //
    function checkIn_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomorthorderjual = (isset($jsonObject["nomorthorderjual"]) ? $this->clean($jsonObject["nomorthorderjual"])     : "");
        $kodecontainer = (isset($jsonObject["kodecontainer"]) ? $this->clean($jsonObject["kodecontainer"])     : "");
        $nomorsopir = (isset($jsonObject["nomorsopir"]) ? $this->clean($jsonObject["nomorsopir"])     : "");
        $type = (isset($jsonObject["type"]) ? $this->clean($jsonObject["type"])     : "");
        $lat = (isset($jsonObject["lat"]) ? $jsonObject["lat"]     : "");
        $lon = (isset($jsonObject["lon"]) ? $jsonObject["lon"]     : "");

        $this->db->trans_begin();
        $query = "	INSERT INTO whcheckin_mobile
                        (nomorthorderjual, kodecontainer, typetracking, nomorsopir, lat, lon, dibuat_pada)
                    VALUES
                        ($nomorthorderjual, '$kodecontainer', '$type', $nomorsopir, $lat, $lon, NOW()) ";

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
}
