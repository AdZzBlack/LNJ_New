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
class Gcm extends REST_Controller { 

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

    function clean($string) {
        return preg_replace("/[^[:alnum:][:space:]]/u", '', $string); // Replaces multiple hyphens with single one.
    }

    function error($string) {
        return str_replace( array("\t", "\n") , "", $string);
    }

	// --- POST Login --- //
	function updateGCM_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $user_nomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"]) : "");
        $gcmid      = (isset($jsonObject["gcmid"])      ? $jsonObject["gcmid"]                    : "");

        $data  = array( 'vcGCMId'       => $gcmid );
        $where = array( 'intNomorMUser' => $user_nomor );

        $this->db->update('muser_android', $data, $where);

    }

    // --- POST Login --- //
    function sendNotificationGCM_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $api_key = "AIzaSyA8tLQw3T5sH3D_5OWZPKveTyJRstVqSHw";

        $user_nomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"]) : "");
        if($user_nomor != ""){ $user_nomor = " AND intNomorMuser = $user_nomor " ; }

        $query = "  SELECT 
                    a.intNomorMuser, 
                    a.vcGCMId 
                    FROM muser_android a 
                    WHERE a.intStatus > 0 AND (a.vcGCMId <> '' AND a.vcGCMId IS NOT NULL) $user_nomor";
        $result = $this->db->query($query, array($user, $pass));

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                $db_android   = $this->load->database('db_android', TRUE);
                $countMessage = $db_android->query("SELECT COUNT(a.txtMessage) AS countMessage FROM tprivate_chat a WHERE a.intStatus = 1 AND intNomorMUser_to ='". $r['intNomorMuser']."'")->row()->countMessage;

                $msg = array
                (
                'message' => 'Ada '.$countMessage.' pesan baru !',
                'title' => 'Private Message'
                );
                
                $fields = array
                (
                    'registration_ids'  => $r['vcGCMId'],
                    'data'          => $msg
                );
                 
                $headers = array
                (
                    'Authorization: key=' . $api_key,
                    'Content-Type: application/json'
                );
                 
                $ch = curl_init();
                curl_setopt( $ch,CURLOPT_URL, 'https://android.googleapis.com/gcm/send' );
                curl_setopt( $ch,CURLOPT_POST, true );
                curl_setopt( $ch,CURLOPT_HTTPHEADER, $headers );
                curl_setopt( $ch,CURLOPT_RETURNTRANSFER, true );
                curl_setopt( $ch,CURLOPT_SSL_VERIFYPEER, false );
                curl_setopt( $ch,CURLOPT_POSTFIELDS, json_encode( $fields ) );
                $result = curl_exec($ch );
                curl_close( $ch );

                array_push($data['data'], array( 'result' => $result ));
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
