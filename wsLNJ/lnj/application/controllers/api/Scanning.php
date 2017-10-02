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

        $this->db->trans_begin();
		$query = "	INSERT INTO whqrcoderequest_mobile
		                (nomormhadmin, tipe, nomordokumen, kodedokumen, dibuat_pada)
		            VALUES
		                ($nomormhadmin, '$tipe', $nomordokumen, '$kodedokumen', NOW()) ";

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
	
	function checkUser_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $hash = (isset($jsonObject["hash"]) ? $jsonObject["hash"]     : "");

        $query = "	SELECT 
                        a.nomor AS nomor,
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
					AND hash = '$hash'";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0)
		{
			foreach ($result->result_array() as $r)
			{
				array_push($data['data'], array(
													'success'						=> "true",
													'user_nomor'    	    		=> $r['nomor'],
                                                    'user_password'					=> $r['password'],
                                                    'user_nomor_pegawai'         	=> $r['nomor_pegawai'],
                                                    'user_kode_pegawai'        		=> $r['kode_pegawai'],
                                                    'user_nama' 					=> $r['nama'],
                                                    'user_role' 					=> $r['role'],
                                                    'user_hash' 					=> $r['hash'],
                                                    'user_cabang' 					=> $r['cabang'],
                                                    'user_nama_cabang' 				=> $r['namacabang'],
                                                    'role_isdriver'					=> $r['isdriver'],
                                                    'role_qrcodereader' 			=> $r['qrcodereader'],
                                                    'role_checkin'					=> $r['checkin'],
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
