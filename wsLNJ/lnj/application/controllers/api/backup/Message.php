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
class Message extends REST_Controller { 

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

    function ellipsis($string) {
        $cut = 30;
        $out = strlen($string) > $cut ? substr($string,0,$cut)."..." : $string;
        return $out;
    }

    function error($string) {
        return str_replace( array("\t", "\n") , "", $string);
    }

    function getGCMId($user_nomor){
        $query = "  SELECT 
                    a.intNomorMUser, 
                    a.vcGCMId 
                    FROM muser_android a 
                    WHERE a.intStatus > 0 AND (a.vcGCMId <> '' AND a.vcGCMId IS NOT NULL) AND a.intNomorMUser = $user_nomor ";
        return $this->db->query($query)->row()->vcGCMId;
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

        /*if ($this->gcm->send())
            echo 'Success for all messages';
        else
            echo 'Some messages have errors';

        print_r($this->gcm->status);
        print_r($this->gcm->messagesStatuses);

        die(' Worked.');*/
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

	// --- Send Message Group --- //
	function sendGroupMessage_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $group_nomor = (isset($jsonObject["group_nomor"]) ? $this->clean($jsonObject["group_nomor"]) : "0");
        $user_nomor  = (isset($jsonObject["user_nomor"])  ? $this->clean($jsonObject["user_nomor"])   : "");
        $new_message = (isset($jsonObject["new_message"]) ? $jsonObject["new_message"]                : "");

        $db_android = $this->load->database('db_android', TRUE);

        $db_android->trans_begin();

        $query = $db_android->insert_string('tgroup_chat', array(
                                                              'intNomorTGroupTeam'=>$group_nomor, 
                                                              'intNomorMUser'=>$user_nomor, 
                                                              'txtMessage'=>$new_message
                                                            )
                                        );
        $db_android->query($query);

        if ($db_android->trans_status() === FALSE){
            $db_android->trans_rollback();
            array_push($data['data'], array( 'success' => "false" ));
        }else{
            $db_android->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));

            $regisID = array();

            // START SEND NOTIFICATION
            $query = "  SELECT 
                            b.intNomor, 
                            b.vcNama
                        FROM muser_android a 
                        JOIN muser b ON a.intNomorMUser = b.intNomor
                        WHERE a.intStatus > 0 AND a.intNomorMUser <> $user_nomor 
                        ORDER BY b.vcNama";
            $result = $this->db->query($query);

            if( $result && $result->num_rows() > 0){
                foreach ($result->result_array() as $r){

                    $vcGCMId = $this->getGCMId($r["intNomor"]);
                    if( $vcGCMId != "null" ){      
                        array_push($regisID, $vcGCMId);       
                    }

                }
                $group_nama = $db_android->query("SELECT vcNama FROM tgroup_team where intNomor ='". $group_nomor."'")->row()->vcNama;     
                $this->send_gcm_group($regisID, $this->ellipsis($new_message),'New Message(s), Group : ' . $group_nama,'GroupMessage',$group_nomor,$group_nama);
            }else{      
                array_push($data['data'], array( 'query' => $this->error($query) ));
            }  

        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- GET Message Group --- //
    function getGroupMessage_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $group_nomor = (isset($jsonObject["group_nomor"])  ? $this->clean($jsonObject["group_nomor"]) : "");
        if($group_nomor != ""){ $group_nomor = " AND intNomorTGroupTeam = $group_nomor " ; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $db_android = $this->load->database('db_android', TRUE);

        $query = "  SELECT 
                        a.intNomorMUser, 
                        a.txtMessage, 
                        a.intStatus,
                        a.dtInsertTime
                    FROM tgroup_chat a 
                    WHERE 1 = 1 $group_nomor 
                    ORDER BY a.dtInsertTime DESC
                    $limit ";
        $result = $db_android->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                $user_nama = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $r['intNomorMUser']."'")->row()->vcNama;   
                array_push($data['data'], array(
                                                'intNomorMUser'  => $r['intNomorMUser'], 
                                                'vcNama'         => $user_nama, 
                                                'txtMessage'     => $r['txtMessage'], 
                                                'intStatus'      => $r['intStatus'], 
                                                'dtInsertTime'   => $r['dtInsertTime']
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

    // --- Send Message PRIVATE --- //
    function sendPrivateMessage_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $user_nomor_asal    = (isset($jsonObject["user_nomor_asal"])    ? $this->clean($jsonObject["user_nomor_asal"])   : "");
        $user_nomor_tujuan  = (isset($jsonObject["user_nomor_tujuan"])  ? $this->clean($jsonObject["user_nomor_tujuan"]) : "");
        $new_message        = (isset($jsonObject["new_message"])        ? $jsonObject["new_message"]                     : "");

        $db_android = $this->load->database('db_android', TRUE);

        $db_android->trans_begin();

        $query = $db_android->insert_string('tprivate_chat', array(
                                                              'intNomorMUser_from'=>$user_nomor_asal, 
                                                              'intNomorMUser_to'=>$user_nomor_tujuan, 
                                                              'txtMessage'=>$new_message
                                                            )
                                        );
        $db_android->query($query);

        if ($db_android->trans_status() === FALSE){
            $db_android->trans_rollback();
            array_push($data['data'], array( 'success' => "false" ));
        }else{
            $db_android->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));

            // START SEND NOTIFICATION
            $vcGCMId = $this->getGCMId($user_nomor_tujuan);
            if( $vcGCMId != "null" ){
                $user_nama_asal = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $user_nomor_asal."'")->row()->vcNama; 
                $this->send_gcm($vcGCMId, $this->ellipsis($new_message),'New Message(s) From '.$user_nama_asal,'PrivateMessage',$user_nomor_asal,$user_nama_asal);
            }

        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- GET Message PRIVATE --- //
    function getPrivateMessage_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $user_nomor_asal = (isset($jsonObject["user_nomor_asal"]) ? $this->clean($jsonObject["user_nomor_asal"]) : "");
        if($user_nomor_asal != ""){ $user_nomor_asal = " AND (intNomorMuser_from = $user_nomor_asal OR intNomorMuser_to = $user_nomor_asal)  " ; }

        $user_nomor_tujuan = (isset($jsonObject["user_nomor_tujuan"]) ? $this->clean($jsonObject["user_nomor_tujuan"]) : "");
        if($user_nomor_tujuan != ""){ $user_nomor_tujuan = " AND (intNomorMuser_from = $user_nomor_tujuan OR intNomorMuser_to = $user_nomor_tujuan)  " ; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $db_android = $this->load->database('db_android', TRUE);

        $query = "  SELECT DISTINCT 
                        a.intNomorMUser_from, 
                        a.intNomorMUser_to, 
                        a.txtMessage, 
                        a.intStatus, 
                        a.dtInsertTime
                    FROM tprivate_chat a 
                    WHERE 1 = 1 $user_nomor_asal $user_nomor_tujuan
                    ORDER BY a.dtInsertTime DESC
                    $limit ";
        $result = $db_android->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                $user_nama_asal = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $r['intNomorMUser_from']."'")->row()->vcNama;   
                $user_nama_tujuan = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $r['intNomorMUser_to']."'")->row()->vcNama;   
                array_push($data['data'], array(
                                                'intNomorMUser_from'    => $r['intNomorMUser_from'], 
                                                'intNomorMUser_to'      => $r['intNomorMUser_to'], 
                                                'vcNama_from'           => $user_nama_asal, 
                                                'vcNama_to'             => $user_nama_tujuan, 
                                                'txtMessage'            => $r['txtMessage'], 
                                                'intStatus'             => $r['intStatus'], 
                                                'dtInsertTime'          => $r['dtInsertTime']
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

    // --- POST Login --- //
    function readPrivateMessage_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $user_nomor_asal   = (isset($jsonObject["user_nomor_asal"])   ? $this->clean($jsonObject["user_nomor_asal"])   : "");
        $user_nomor_tujuan = (isset($jsonObject["user_nomor_tujuan"]) ? $this->clean($jsonObject["user_nomor_tujuan"]) : "");

        $data  = array( 'intStatus' => "2" );
        $where = array( 'intNomorMuser_from' => $user_nomor_tujuan, 'intNomorMuser_to' => $user_nomor_asal );

        $db_android = $this->load->database('db_android', TRUE);

        $db_android->update('tprivate_chat', $data, $where);

    }

}
