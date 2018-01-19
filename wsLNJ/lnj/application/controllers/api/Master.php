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

	// --- POST get user --- //
	function getUser_post()
	{     
        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

		$query = "	SELECT 
						a.nomor AS `nomor`,
						a.nama AS `nama`,
						a.kode AS kode,
						b.cantracked AS cantracked
					FROM mhadmin a
					JOIN whrole_mobile b ON b.nomor = a.role_android
					WHERE a.status_aktif = 1
					AND a.role_android >= 0
					ORDER BY a.nama;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                								'nomor'					=> $r['nomor'],
                								'nama' 					=> $r['nama'],
                								'kode'                  => $r['kode'],
                								'cantracked'            => $r['cantracked'],
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

    // --- POST get job --- //
    function getJob_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomormhcabang = (isset($jsonObject["nomormhcabang"]) ? $jsonObject["nomormhcabang"]     : "");
        $keyword = (isset($jsonObject["keyword"]) ? $jsonObject["keyword"]     : "");

//        $query = "	SELECT
//                    	a.nomor AS nomor,
//                    	a.kode AS kode,
//                    	a.stuffing_date AS stuffingdate,
//                    	a.invoice_number AS invoice,
//                    	`FC_GENERATE_PORT_NAMA`(a.nomormhport_loading) AS pol,
//                    	`FC_GENERATE_PORT_NAMA`(a.nomormhport_discharge) AS pod
//                    FROM thorderjual a
//                    WHERE status_aktif = 1
//                        AND a.kode LIKE '%$keyword%'
//                    ORDER BY a.kode;";

        $query = "SELECT  a.nomor,
                   a.kode,
                   a.stuffing_date,
                   a.invoice_number,
                   `FC_GENERATE_PORT_NAMA`(a.nomormhport_loading) AS pol,
                   `FC_GENERATE_PORT_NAMA`(a.nomormhport_discharge) AS pod
                  FROM thorderjual a
                  WHERE a.status_aktif = 1
                   AND a.status_selesai = 0
                   AND a.status_cancel = 0
                   AND a.tipe_order = 1
                   AND a.typeofshipment = 1
                   AND a.nomormhcabang = '$nomormhcabang'
                   AND `FC_GENERATE_JUMLAHCONTAINER_FROM_NOMORTHORDERJUAL`(a.nomor) > 0
                   AND DATEDIFF(NOW(),a.tanggal) <= 365
                   AND a.kode LIKE '%$keyword%' ";

        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'kode'                  => $r['kode'],
                                                'stuffingdate'			=> $r['stuffingdate'],
                                                'invoice'               => $r['invoice'],
                                                'pol'                   => $r['pol'],
                                                'pod'                   => $r['pod'],
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

    // --- POST get job container--- //
    function getJobContainer_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomor = (isset($jsonObject["nomor"]) ? $this->clean($jsonObject["nomor"])     : "");

        $query = "	SELECT
                    	a.nomor AS nomor,
                     	`FC_GENERATE_CONTAINER_SIZE`(a.nomormhcontainersize) AS size,
                     	`FC_GENERATE_CONTAINER_TIPE`(a.nomormhcontainertype) AS `type`,
                     	a.kodecontainer AS kode,
                     	a.sealcontainer AS seal
                    FROM tdorderjualcontainer a
                    WHERE a.status_aktif = 1
                        AND a.nomorthorderjual = $nomor;";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomor'					=> $r['nomor'],
                                                'size'                  => $r['size'],
                                                'type'      			=> $r['type'],
                                                'kode'                  => $r['kode'],
                                                'seal'                  => $r['seal']
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
