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
class Profile extends REST_Controller { 

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

       if ($this->gcm->send())
           echo 'Success for all messages';
       else
           echo 'Some messages have errors';

       print_r($this->gcm->status);
       print_r($this->gcm->messagesStatuses);

        die(' Worked.');
    }

	function test_get()
	{
		$result = "a";
		
		$data['data'] = array();
		
		// START SEND NOTIFICATION
        $vcGCMId = $this->getGCMId(18);
		
            $this->send_gcm($vcGCMId, $this->ellipsis('$new_message'),'New Message(s) From ','PrivateMessage','0','0');
        
		
		
		$this->response($vcGCMId);
	}

	/// --- POST Change Password --- //
	function changePassword_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $user = (isset($jsonObject["user_id"]) ? $this->clean($jsonObject["user_id"])     : "");
        $oldpass = md5((isset($jsonObject["oldpass"]) ? $this->clean($jsonObject["oldpass"]) : ""));
		$newpass = md5((isset($jsonObject["newpass"]) ? $this->clean($jsonObject["newpass"]) : ""));
		
		$query = "	SELECT (1) 
					FROM whuser_mobile a 
					WHERE a.nomor = $user 
						AND a.password = '$oldpass'";
		$result = $this->db->query($query);

        if( $result && $result->num_rows() > 0)
		{
			$query2 = "	UPDATE whuser_mobile a 
						SET a.password = ? 
						WHERE a.nomor = ? ";
			$result2 = $this->db->query($query2, array($newpass, $user));
			
			if( $result2 )
			{
				array_push($data['data'], array( 'success'	=> "true", 'pesan'	=> "1"));
			}else{
				array_push($data['data'], array( 'query' => $this->error($query2) ));
			}
        }else if ( $result && $result->num_rows() == 0){
			array_push($data['data'], array( 'success'	=> "true", 'pesan'	=> "2"));
		}else{		
			array_push($data['data'], array( 'query' => $this->error($query) ));
		}  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

}
