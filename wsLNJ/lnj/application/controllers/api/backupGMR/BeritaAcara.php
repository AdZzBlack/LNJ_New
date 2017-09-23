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
class BeritaAcara extends REST_Controller { 

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
        return str_replace( array("\t", "\n", "\r") , "", $string);
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

     // --- POST Login --- //
    function createBeritaAcara_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
        $bangunan_nomor  = (isset($jsonObject["bangunan_nomor"])  ? $this->clean($jsonObject["bangunan_nomor"])  : "");
        $bangunan_kode   = (isset($jsonObject["bangunan_kode"])  ? $this->clean($jsonObject["bangunan_kode"])  : "");
		$keterangan      = (isset($jsonObject["keterangan"])  ? $this->clean($jsonObject["keterangan"])  : "");
		$gambar 	     = (isset($jsonObject["gambar"])  ? $jsonObject["gambar"]  : "");
		$ffl	 	     = (isset($jsonObject["ffl"])  ? $jsonObject["ffl"]  : "");
		$user	 	     = (isset($jsonObject["user"])  ? $jsonObject["user"]  : "");


        $this->db->trans_begin();

        $query = $this->db->insert_string('mhberitaacara', array(
															  'dibuat_oleh'   	  => $user, 
															  'nomormhbangunan'   => $bangunan_nomor, 
															  'kode'		      => $bangunan_kode, 
															  'gambar'        	  => $gambar, 
															  'keterangan'        => $keterangan,
															  'elevasi'           => $ffl,
															)
														);
		$this->db->query($query);

        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => "false" ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));
			
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
                $this->send_gcm_group($regisID, $this->ellipsis($count . ' pending elevasi'),'Berita Acara Elevasi','ChooseApprovalElevasi','','');
            } 
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }
	
	function alldataneedapproval_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (b.namalengkap LIKE '%$search%') "; }

		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "");
        if($nomor != ""){ $nomor = " AND (a.nomor = '$nomor') "; }
		
        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

		$query = "  SELECT  
                        a.nomor,
						a.nomormhbangunan AS nomorbangunan,
						b.namalengkap,
						a.gambar,
						a.keterangan,
						a.elevasi AS elevasi,
						b.elevasi AS elevasiawal
					FROM mhberitaacara a
                    JOIN mhbangunan_view b
						ON a.nomormhbangunan = b.nomor
					WHERE 1 = 1
					AND a.status_disetujui = 0 $search $nomor $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'nomor'    			=> $r['nomor'], 
												'nomorbangunan'    	=> $r['nomorbangunan'], 
												'nama'       		=> $r['namalengkap'],
												'image'       		=> $r['gambar'],
												'keterangan'       	=> $r['keterangan'],
												'elevasi'    		=> $r['elevasi'], 
												'elevasiawal'    	=> $r['elevasiawal'], 
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
	
	function approve_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "");
        if($nomor != ""){ $nomor = " AND (b.nomor = '$nomor') "; }
		
		$nomorbangunan = (isset($jsonObject["nomorbangunan"]) ? $this->clean($jsonObject["nomorbangunan"]) : "");
        if($nomorbangunan != ""){ $nomorbangunan = " AND (nomor = '$nomorbangunan') "; }
		
		$ffl	 	     = (isset($jsonObject["ffl"])  ? $jsonObject["ffl"]  : "");

		$this->db->trans_begin();
		
		$query = "  UPDATE mhberitaacara b
                    SET status_disetujui = 1
					WHERE 1 = 1 $nomor";
        $this->db->query($query);

		$query = "  UPDATE mhbangunan
                    SET elevasi = $ffl
					WHERE 1 = 1 $nomorbangunan";
        $this->db->query($query);
		
        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => $query ));
        }else{ 
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));
			
			$vcgcmid  = $this->db->query("SELECT a.gcmid FROM whuser_mobile a JOIN mhberitaacara b ON b.dibuat_oleh = a.nomormhuser $nomor ORDER BY a.nomor DESC LIMIT 1")->row()->gcmid;
			$this->send_gcm($vcGCMId, $this->ellipsis('Berita Acara Elevasi Approved'),'Approval','BeritaAcara','0','0');
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
	
	function disapprove_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
		
		$nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"]) : "");
        if($nomor != ""){ $nomor = " AND (nomor = '$nomor') "; }
		

		$this->db->trans_begin();
		
		$query = "  UPDATE mhberitaacara
                    SET status_disetujui = 2
					WHERE 1 = 1 $nomor";
        $this->db->query($query);
		
        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => $query ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
}
