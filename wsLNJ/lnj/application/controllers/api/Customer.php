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
class Customer extends REST_Controller { 

    function __construct()
    {
        // Construct the parent class
        parent::__construct();

        // Configure limits on our controller methods
        // Ensure you have created the 'limits' table and enabled 'limits' within application/config/rest.php
        $this->methods['event_post']['limit'] = 50000; // 500 requests per hour per event/key
        // $this->methods['event_delete']['limit'] = 50; // 50 requests per hour per event/key
        $this->methods['event_get']['limit'] = 50000; // 500 requests per hour per event/key

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

	// --- POST Customer --- //
	function updateLatLongCustomer_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
		$jsonObject = (json_decode($value , true));

        $customer_nomor = (isset($jsonObject["customer_nomor"]) ? $this->clean($jsonObject["customer_nomor"]) : "");
        $latitude       = (isset($jsonObject["latitude"])       ? $jsonObject["latitude"]                     : "");
        $longitude      = (isset($jsonObject["longitude"])      ? $jsonObject["longitude"]                    : "");

        $data  = array( 'decLatitude' => $latitude, 'decLongitude' => $longitude );
        $where = array( 'intNomor' => $customer_nomor );

        $this->db->update('mcustomer', $data, $where);

    }

    function getDataCustomer_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $customer_nomor = (isset($jsonObject["customer_nomor"]) ? $this->clean($jsonObject["customer_nomor"]) : "");
        if($customer_nomor != ""){ $customer_nomor = " AND a.intNomor = $customer_nomor "; }

        $customer_kode = (isset($jsonObject["customer_kode"]) ? $this->clean($jsonObject["customer_kode"]) : "");
        if($customer_kode != ""){ $customer_kode = " AND a.vcKode = '$customer_kode' "; }

        $query = "  SELECT 
                    a.intNomor AS customer_nomor,
                    a.vcNama AS customer_nama,
                    a.vcKontak AS customer_kontak,
                    a.vcAlamatPenagihan AS customer_alamat,
                    a.vcTeleponPenagihan AS customer_telepon,
                    a.decLatitude AS customer_latitude,
                    a.decLongitude AS customer_longitude,
                    b.vcNama AS kota_nama
                    FROM mcustomer a
                    JOIN mkota b ON a.intNomorMKotaPenagihan = b.intNomor 
                    WHERE a.intStatus = 1 $customer_nomor $customer_kode";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'customer_nomor'     => $r['customer_nomor'], 
                                                'customer_nama'      => $r['customer_nama'], 
                                                'customer_kontak'    => $r['customer_kontak'], 
                                                'customer_alamat'    => $r['customer_alamat'], 
                                                'customer_telepon'   => $r['customer_telepon'], 
                                                'customer_latitude'  => $r['customer_latitude'], 
                                                'customer_longitude' => $r['customer_longitude'], 
                                                'kota_nama'          => $r['kota_nama']
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
	
    function getDataNewCustomer_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $customer_nomor = (isset($jsonObject["customer_nomor"]) ? $this->clean($jsonObject["customer_nomor"]) : "");
        if($customer_nomor != ""){ $customer_nomor = " AND a.intNomor = $customer_nomor "; }

        $customer_kode = (isset($jsonObject["customer_kode"]) ? $this->clean($jsonObject["customer_kode"]) : "");
        if($customer_kode != ""){ $customer_kode = " AND a.vcKode = '$customer_kode' "; }

        $customer_tipe = (isset($jsonObject["customer_tipe"]) ? $this->clean($jsonObject["customer_tipe"]) : "");
        if($customer_tipe != ""){ $customer_tipe = " AND a.isProspek = '$customer_tipe' "; }

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

        $db_android = $this->load->database('db_android', TRUE);

        $query = "  SELECT 
                    a.intNomor AS customer_nomor,
                    a.vcNama AS customer_nama,
                    a.vcKontak AS customer_kontak,
                    a.vcAlamatPenagihan AS customer_alamat,
                    a.vcTeleponPenagihan AS customer_telepon,
                    a.intNomorMKotaPenagihan,
					a.intNomorMJenisCustomer,
					a.intNomorMArea
                    FROM tcustomer_temp a
                    WHERE a.intApproved = 0 AND a.intStatus = 1 $customer_tipe $search ";
        $result = $db_android->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                $kota_nama = $this->db->query("SELECT vcNama FROM mkota where intNomor ='". $r["intNomorMKotaPenagihan"]."'")->row()->vcNama;
				
				$jenis_nama = $this->db->query("SELECT vcNama FROM mjeniscustomer where intNomor ='". $r["intNomorMJenisCustomer"]."'")->row()->vcNama;
				
				$area_nama = $this->db->query("SELECT vcNama FROM marea where intNomor ='". $r["intNomorMArea"]."'")->row()->vcNama;
                array_push($data['data'], array(
                                                'customer_nomor'     => $r['customer_nomor'], 
                                                'customer_nama'      => $r['customer_nama'], 
                                                'customer_kontak'    => $r['customer_kontak'], 
                                                'customer_alamat'    => $r['customer_alamat'], 
                                                'customer_telepon'   => $r['customer_telepon'], 
                                                'customer_latitude'  => "0", 
                                                'customer_longitude' => "0", 
                                                'kota_nama'          => $kota_nama,
												'jenis_nama'         => $jenis_nama,
												'area_nama'          => $area_nama
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

    
    function getCounterDataNewCustomer_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $db_android = $this->load->database('db_android', TRUE);

        $query = "  SELECT 
                    COUNT(a.intNomor) AS customer_baru
                    FROM tcustomer_temp a
                    WHERE a.isProspek = 0 AND a.intApproved = 0 AND a.intStatus = 1";
        $result = $db_android->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array( 'customer_baru' => $r['customer_baru'] ));
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
    function createTempCustomer_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $nomor_jeniscustomer    = (isset($jsonObject["nomor_jeniscustomer"])    ? $this->clean($jsonObject["nomor_jeniscustomer"])  : "");
        $nomor_kota             = (isset($jsonObject["nomor_kota"])             ? $this->clean($jsonObject["nomor_kota"])           : "");
        $nomor_sales            = (isset($jsonObject["nomor_sales"])            ? $this->clean($jsonObject["nomor_sales"])          : "");
        $nomor_area             = (isset($jsonObject["nomor_area"])             ? $this->clean($jsonObject["nomor_area"])           : "");
        $nama                   = (isset($jsonObject["nama"])                   ? $this->clean($jsonObject["nama"])                 : "");
        $telepon                = (isset($jsonObject["telepon"])                ? $this->clean($jsonObject["telepon"])              : "");
        $alamat                 = (isset($jsonObject["alamat"])                 ? $this->clean($jsonObject["alamat"])               : "");
        $isProspek              = (isset($jsonObject["isProspek"])              ? $this->clean($jsonObject["isProspek"])            : "0");
        $customer_nomor         = (isset($jsonObject["customer_nomor"])         ? $this->clean($jsonObject["customer_nomor"])       : "0");

        $bex_nomor = $this->db->query("SELECT intNomorMSales FROM muser_android where intNomorMUser ='". $nomor_sales."'")->row()->intNomorMSales; 

        $db_android = $this->load->database('db_android', TRUE);

        if($customer_nomor=="0"){

            $db_android->trans_begin();

            $query = $db_android->insert_string('tcustomer_temp', array(
                                                                  'intNomorMJenisCustomer'  => $nomor_jeniscustomer, 
                                                                  'intNomorMKotaPengiriman' => $nomor_kota, 
                                                                  'intNomorMKotaPenagihan'  => $nomor_kota, 
                                                                  'intNomorMSales'          => $bex_nomor, 
                                                                  'intNomorMArea'           => $nomor_area, 
                                                                  'vcNama'                  => $nama, 
                                                                  'vcKontak'                => $telepon, 
                                                                  'vcTeleponPenagihan'      => $telepon, 
                                                                  'vcAlamatPenagihan'       => $alamat, 
                                                                  'isProspek'               => $isProspek,
                                                                  'IntApproved'             => "0"
                                                                )
                                            );

            $db_android->query($query);

            if ($db_android->trans_status() === FALSE){
                $db_android->trans_rollback();
                array_push($data['data'], array( 'success' => "false" ));
            }else{
                $db_android->trans_commit();
                array_push($data['data'], array( 'success' => "true" ));

                $customer_nomor = $db_android->insert_id();

                $regisID = array();

                $query_adminsales = "SELECT 
                                    a.intNomorMUser, 
                                    a.vcGCMId 
                                    FROM muser_android a 
                                    WHERE a.intStatus > 0 AND (a.vcGCMId <> '' AND a.vcGCMId IS NOT NULL) AND vcJabatan = 'SALES ADMIN'";
                $result_adminsales = $this->db->query($query_adminsales);

				
                if( $result_adminsales && $result_adminsales->num_rows() > 0){
                    foreach ($result_adminsales->result_array() as $r_adminsales){

                        // START SEND NOTIFICATION
                        $vcGCMId = $r_adminsales['vcGCMId'];
                        if( $vcGCMId != "null" ){      
                            array_push($regisID, $vcGCMId);       
                        }
                        
                    }
                    $sales_nama = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $nomor_sales."'")->row()->vcNama; 
                    $this->send_gcm_group($regisID, $this->ellipsis("Penambahan data customer baru"),'New Customer From : ' . $sales_nama,'NewCustomerList',$customer_nomor,$sales_nama);
                }                 
            }  

        }else{

            $updatequery  = array( 'isProspek' => "0" );
            $where        = array( 'intNomor' => $customer_nomor );

            $result = $db_android->update('tcustomer_temp', $updatequery, $where);

            if($result){
                array_push($data['data'], array( 'success' => "true" ));
            }else{
                array_push($data['data'], array( 'success' => "false" ));
            }

        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }



    // --- APPROVE DISAPPROVE NEW CUSTOMER --- //
    function approveNewCustomer_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $customer_nomor = (isset($jsonObject["customer_nomor"]) ? $this->clean($jsonObject["customer_nomor"]) : "2");
        $admin_nomor    = (isset($jsonObject["admin_nomor"])    ? $this->clean($jsonObject["admin_nomor"])    : "1");

        $db_android = $this->load->database('db_android', TRUE);

        $updatequery  = array( 'IntApproved' => "1", 'intApproveUserID' => $admin_nomor );
        $where        = array( 'intNomor' => $customer_nomor );

        $result = $db_android->update('tcustomer_temp', $updatequery, $where);

        if($result){

            $query = "  SELECT * 
                        FROM tcustomer_temp a
                        WHERE a.intNomor = $customer_nomor";
            $result = $db_android->query($query);

            if( $result && $result->num_rows() > 0){
                foreach ($result->result_array() as $r){

                    $querykode = "  SELECT CONCAT('C',LPAD(IFNULL((MAX(SUBSTRING(vcKode,2)) + 1),1),5,'0')) AS vcKodeBaru
                                    FROM mcustomer 
                                    WHERE SUBSTRING(vcKode,1,1) = 'C'";

                    $kode = $this->db->query($querykode)->row()->vcKodeBaru;

                    $query_insert = $this->db->insert_string('mcustomer', array(
                                                              'vcKode'                  => $kode, 
                                                              'intNomorMJenisCustomer'  => $r['intNomorMJenisCustomer'], 
                                                              'intNomorMKotaPengiriman' => $r['intNomorMKotaPengiriman'], 
                                                              'intNomorMKotaPenagihan'  => $r['intNomorMKotaPenagihan'], 
                                                              'intNomorMSales'          => $r['intNomorMSales'], 
                                                              'intNomorMArea'           => $r['intNomorMArea'], 
                                                              'vcNama'                  => $r['vcNama'], 
                                                              'vcKontak'                => $r['vcKontak'], 
                                                              'vcTeleponPenagihan'      => $r['vcTeleponPenagihan'], 
                                                              'vcAlamatPenagihan'       => $r['vcAlamatPenagihan'] 
                                                            )
                                        );
                    $this->db->query($query_insert);

                    array_push($data['data'], array( 'success' => "true" ));

                    $regisID = array();

                    $query_adminsales = "SELECT 
                                        a.intNomorMSales 
                                        FROM tcustomer_temp a 
                                        WHERE a.intNomor = $customer_nomor";
                    $result_adminsales = $db_android->query($query_adminsales);

                    if( $result_adminsales && $result_adminsales->num_rows() > 0){
                        foreach ($result_adminsales->result_array() as $r_adminsales){

                            $bex_nomor = $this->db->query("SELECT intNomorMUser FROM muser_android where intNomorMSales ='". $r_adminsales['intNomorMSales']."'")->row()->intNomorMUser; 

                            // START SEND NOTIFICATION
                            $vcGCMId = $this->getGCMId($bex_nomor);
                            if( $vcGCMId != "null" ){      
                                array_push($regisID, $vcGCMId);       
                            }
                            
                        }
                        $admin_nama = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $admin_nomor."'")->row()->vcNama; 
                        $this->send_gcm_group($regisID, $this->ellipsis("New Customer Approved"),'New Customer Approved by : ' . $admin_nama,'SalesOrderList',$customer_nomor,$admin_nama);
                    } 

                }
            }
        }else{
            array_push($data['data'], array( 'success' => "false" ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }

    function disapproveNewCustomer_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $customer_nomor = (isset($jsonObject["customer_nomor"]) ? $this->clean($jsonObject["customer_nomor"]) : "");
        $admin_nomor    = (isset($jsonObject["admin_nomor"])    ? $this->clean($jsonObject["admin_nomor"])    : "");
        $keterangan     = (isset($jsonObject["keterangan"])    ? $this->clean($jsonObject["keterangan"])      : "");

        $db_android = $this->load->database('db_android', TRUE);

        $updatequery  = array( 'IntApproved' => "2", 'intApproveUserID' => $admin_nomor, 'vcKeterangan' => $keterangan );
        $where        = array( 'intNomor' => $customer_nomor );

        $result = $db_android->update('tcustomer_temp', $updatequery, $where);

        if($result){
            
            $regisID = array();

            $query_adminsales = "SELECT 
                                a.intNomorMSales 
                                FROM tcustomer_temp a 
                                WHERE a.intStatus > 0 AND a.intNomor = $customer_nomor";
            $result_adminsales = $db_android->query($query_adminsales);

            if( $result_adminsales && $result_adminsales->num_rows() > 0){
                foreach ($result_adminsales->result_array() as $r_adminsales){

                    $bex_nomor = $this->db->query("SELECT intNomorMUser FROM muser_android where intNomorMSales ='". $r_adminsales['intNomorMSales']."'")->row()->intNomorMUser; 

                    // START SEND NOTIFICATION
                    $vcGCMId = $this->getGCMId($bex_nomor);
                    if( $vcGCMId != "null" ){      
                        array_push($regisID, $vcGCMId);       
                    }
                    
                }
                $admin_nama = $this->db->query("SELECT vcNama FROM muser where intNomor ='". $admin_nomor."'")->row()->vcNama; 
                $this->send_gcm_group($regisID, $this->ellipsis($keterangan),'New Customer Disapprove by : ' . $admin_nama,'SalesOrderList',$customer_nomor,$admin_nama);
            } 

            array_push($data['data'], array( 'success' => "true" ));
        }else{
            array_push($data['data'], array( 'success' => "false" ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

////////////////////////////////////////////////////// Customer Prospecting ////////////////////////////////////////////////////
    // by Tonny
    //Untuk mendapatkan data customer prospecting dari tabel tcustomerprospecting
    function getCustomerProspecting_post(){
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $query = "	SELECT
                        a.nomor nomorcustomer,
                        a.kode kodecustomer,
                        a.nama namacustomer,
                        a.alamat alamatcustomer,
                        b.kode kodekotacustomer,
                        b.kota namakotacustomer,
                        a.telepon,
                        a.fax,
                        a.namapengiriman,
                        a.alamatpengiriman,
                        a.nomorkotapengiriman,
                        a.kodekotapengiriman,
                        a.teleponpengiriman,
                        a.namapkp,
                        a.alamatpkp,
                        a.nomorkotapkp,
                        a.kodekotapkp,
                        b.kota namakotapkp,
                        a.npwp,
                        a.nppkp
                    FROM tcustomerprospecting a
                    LEFT JOIN tkota b
                        ON b.nomor = a.nomorkota
                    WHERE a.status = 1";

        $result = $this->db->query($query, array($user, $pass));

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                array_push($data['data'], array(
                                                'nomorcustomer'			=> $r['nomorcustomer'],
                                                'kodecustomer'			=> $r['kodecustomer'],
                                                'namacustomer'			=> $r['namacustomer'],
                                                'alamatcustomer'		=> $r['alamatcustomer'],
                                                'kodekotacustomer'      => $r['kodekotacustomer'],
                                                'namakotacustomer'      => $r['namakotacustomer'],
                                                'telepon'               => $r['telepon'],
                                                'fax'                   => $r['fax'],
                                                'namapengiriman'        => $r['namapengiriman'],
                                                'alamatpengiriman'      => $r['alamatpengiriman'],
                                                'nomorkotapengiriman'   => $r['nomorkotapengiriman'],
                                                'kodekotapengiriman'    => $r['kodekotapengiriman'],
                                                'teleponpengiriman'     => $r['teleponpengiriman'],
                                                'namapkp'               => $r['namapkp'],
                                                'alamatpkp'             => $r['alamatpkp'],
                                                'nomorkotapkp'          => $r['nomorkotapkp'],
                                                'kodekotapkp'           => $r['kodekotapkp'],
                                                'namakotapkp'           => $r['namakotapkp'],
                                                'npwp'                  => $r['npwp'],
                                                'nppkp'                 => $r['nppkp']
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

    // by Tonny
    //Untuk insert data customer prospecting ke tabel tcustomerprospecting
    function setCustomerProspecting_post(){
        $data['data'] = array();
        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));
        $this->db->trans_begin();
        $query = "	INSERT INTO tcustomerprospecting VALUES ()";
        $result = $this->db->query($query, array($user, $pass));
        if ($this->db->trans_status() === FALSE){
            $this->db->trans_rollback();
            array_push($data['data'], array( 'success' => $query ));
        }else{
            $this->db->trans_commit();
            array_push($data['data'], array( 'success' => "true", 'query' => $query));
        }
        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
