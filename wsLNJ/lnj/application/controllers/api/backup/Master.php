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
        $this->methods['event_post']['limit'] = 50000; // 500 requests per hour per event/key
        // $this->methods['event_delete']['limit'] = 50; // 50 requests per hour per event/key
        $this->methods['event_get']['limit'] = 50000; // 500 requests per hour per event/key

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

    // --- GET GUDANG DATA --- //
    function alldatagudang_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $intNomorMCabang = (isset($jsonObject["cabang_nomor"]) ? $this->clean($jsonObject["cabang_nomor"]) : "");
        if($intNomorMCabang != ""){ $intNomorMCabang = " AND a.intNomorMCabang = " . $intNomorMCabang; }

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT  
                        a.intNomor,
                        a.vcKode,
                        a.vcNama, 
                        a.intNomorMCabang 
                    FROM mgudang a
                    WHERE a.intStatus > 0 $intNomorMCabang $search
                    ORDER BY a.vcNama 
                    $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'    => $r['intNomor'], 
                                                'vcKode'                => $r['vcKode'], 
                                                'vcNama'                => $r['vcNama'],
                                                'intNomorMCabang'       => $r['intNomorMCabang']
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

    // --- GET CUSTOMER DATA --- //
    function alldatacustomer_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%' OR a.vcKontak LIKE '%$search%' OR a.vcAlamatPenagihan LIKE '%$search%' OR b.vcNama LIKE '%$search%' OR c.vcNama LIKE '%$search%' OR d.vcNama LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT  
                        a.intNomor, a.vcKode, a.vcNama, a.vcAlamatPenagihan, a.vcTeleponPenagihan, a.intNomorMArea, a.intNomorMKotaPenagihan, a.intNomorMSales, 
                        b.vcNama AS sales_nama, 
                        c.vcNama AS area_nama, 
                        d.vcNama AS kota_nama 
                    FROM mcustomer a
                    JOIN msales b ON a.intNomorMSales = b.intNomor 
                    JOIN marea c ON a.intNomorMArea = c.intNomor 
                    JOIN mkota d ON a.intNomorMKotaPenagihan = d.intNomor 
                    WHERE a.intStatus > 0 $search 
                    ORDER BY a.vcNama 
                    $limit ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'               => $r['intNomor'], 
                                                'vcKode'                 => $r['vcKode'], 
                                                'vcNama'                 => $r['vcNama'],
                                                'vcAlamatPenagihan'      => $r['vcAlamatPenagihan'], 
                                                'vcTeleponPenagihan'     => $r['vcTeleponPenagihan'], 
                                                'intNomorMArea'          => $r['intNomorMArea'], 
                                                'intNomorMSales'         => $r['intNomorMSales'], 
                                                'intNomorMKotaPenagihan' => $r['intNomorMKotaPenagihan'], 
                                                'sales_nama'             => $r['sales_nama'],
                                                'area_nama'              => $r['area_nama'],
                                                'kota_nama'              => $r['kota_nama']
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


    // --- GET JENIS BIAYA DATA --- //
    function alldatajenisbiaya_post(){      
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

        $query = " SELECT a.intNomor, a.vcNama FROM mjenisbiaya a WHERE a.intStatus > 0 $search ORDER BY a.vcNama ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'               => $r['intNomor'],
                                                'vcNama'                 => $r['vcNama']
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

     // --- GET MPROYEK --- //
    function alldataproyek_post(){      
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

		$limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }
		
        $query = " SELECT a.intNomor, a.vcNama, a.vcKeterangan FROM mproyek a WHERE a.intStatus > 0 $search ORDER BY a.vcNama ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'               => $r['intNomor'],
                                                'vcNama'                 => $r['vcNama'],
                                                'vcKeterangan'           => $r['vcKeterangan']
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

    // --- GET JENIS CUSTOMER DATA --- //
    function alldatajenispenjualan_post(){      
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

        $query = " SELECT a.intNomor, a.vcNama FROM mjenispenjualan a WHERE a.intStatus > 0 $search ORDER BY a.vcNama ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'               => $r['intNomor'],
                                                'vcNama'                 => $r['vcNama']
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
    
    // --- GET JENIS CUSTOMER DATA --- //
    function alldatajeniscustomer_post(){      
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

        $query = " SELECT a.intNomor, a.vcNama FROM mjeniscustomer a WHERE a.intStatus > 0 $search ORDER BY a.vcNama ";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'               => $r['intNomor'],
                                                'vcNama'                 => $r['vcNama']
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


    // --- GET VALUTA DATA --- //
    function alldatavaluta_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = " SELECT DISTINCT 
                    a.intNomor, 
                    a.vcNama,
                    IFNULL(b.dtTanggal,CURRENT_DATE) AS dtTanggal, 
                    IFNULL(c.decKurs,1) AS decKurs
                    FROM mvaluta a 
                    LEFT JOIN (
                            SELECT intNomorMValuta,MAX(dtTanggal) AS dtTanggal
                            FROM tkurs
                            GROUP BY intNomorMValuta) b ON b.intNomorMValuta = a.intNomor
                    LEFT JOIN tkurs c ON c.intNomorMValuta = a.intNomor AND b.dtTanggal = c.dtTanggal
                    WHERE a.intStatus > 0 $search
                    ORDER BY a.vcNama, b.dtTanggal DESC 
                    $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'  => $r['intNomor'],
                                                'vcNama'    => $r['vcNama'],
                                                'dtTanggal' => $r['dtTanggal'],
                                                'decKurs'   => $r['decKurs']
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


    // --- GET AREA DATA --- //
    function alldataarea_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $intNomorMArea = (isset($jsonObject["area_nomor"]) ? $this->clean($jsonObject["area_nomor"]) : "");
        if($intNomorMArea != ""){ $intNomorMArea = " AND a.intNomorMArea = " . $intNomorMArea; }

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = " SELECT a.intNomor, a.vcNama, a.decOmsetTarget FROM marea a WHERE a.intStatus > 0 $intNomorMArea $search ORDER BY a.vcNama $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'       => $r['intNomor'],
                                                'vcNama'         => $r['vcNama'],
                                                'decOmsetTarget' => $r['decOmsetTarget']
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

     // --- GET KOTA DATA --- //
    function alldatakota_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $intNomorMKota = (isset($jsonObject["kota_nomor"]) ? $this->clean($jsonObject["kota_nomor"]) : "");
        if($intNomorMKota != ""){ $intNomorMKota = " AND a.intNomor = " . $intNomorMKota; }

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = " SELECT a.intNomor, a.vcNama FROM mkota a WHERE a.intStatus > 0 $intNomorMKota $search ORDER BY a.vcNama $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'       => $r['intNomor'],
                                                'vcNama'         => $r['vcNama']
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


    // --- GET APPROVAL BY DATA --- //
    function alldataapprovalby_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT a.intNomor, a.vcNama, a.vcJabatan  
					FROM muser a 
					WHERE a.intStatus = 1 AND intCanSignApproval = 1 $search 
					ORDER BY a.vcJabatan, a.vcNama  
                    $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'  => $r['intNomor'],
                                                'vcNama'    => $r['vcNama'],
                                                'vcJabatan' => $r['vcJabatan'],
                                                'vcHp' 		=> $r['vcHP']
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


    // --- GET BEX DATA --- //
    function alldatabex_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $intNomorMSales = (isset($jsonObject["sales_nomor"]) ? $this->clean($jsonObject["sales_nomor"]) : "");
        if($intNomorMSales != ""){ $intNomorMSales = " AND a.intNomorMSales = " . $intNomorMSales; }

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%' OR b.vcNama LIKE '%$search%' OR b.vcNamaSM LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT  
                        a.intNomor, a.vcKode, a.vcNama, a.vcHP,   
                        b.vcNama AS team_nama,
                        b.vcNamaSM AS team_SM 
                    FROM msales a
                    JOIN msalesteam b ON a.intNomorMSalesTeam = b.intNomor 
                    WHERE a.intStatus > 0 $intNomorMSales $search 
                    ORDER BY a.vcNama 
                    $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'      => $r['intNomor'], 
                                                'vcKode'        => $r['vcKode'], 
                                                'vcNama'        => $r['vcNama'],
                                                'vcHp'          => $r['vcHP'],
                                                'team_nama'     => $r['team_nama'], 
                                                'team_SM'       => $r['team_SM']
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

    // --- GET SUPPLIER DATA --- //
    function alldatasupplier_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%' OR a.vcKontak LIKE '%$search%' OR b.vcNama LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT  
                        a.intNomor, a.vcKode, a.vcNama, a.vcKontak, a.vcAlamat, a.intNomorMKota, 
                        b.vcNama AS kota_nama 
                    FROM msupplier a
                    JOIN mkota b ON a.intNomorMKota = b.intNomor 
                    WHERE a.intStatus > 0 $search  
                    ORDER BY a.vcNama 
                    $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'      => $r['intNomor'], 
                                                'vcKode'        => $r['vcKode'], 
                                                'vcNama'        => $r['vcNama'],
                                                'vcKontak'      => $r['vcKontak'], 
                                                'vcAlamat'      => $r['vcAlamat'],
                                                'intNomorMKota' => $r['intNomorMKota'],
                                                'kota_nama'     => $r['kota_nama']
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


    // --- GET AREA DATA --- //
    function alldatakontak_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $intNomorMUser_to = "";
        $intNomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"]) : "");
        if($intNomor != ""){ 
            $intNomorMUser_to = $intNomor; 
            $intNomor         = " AND a.intNomorMUser <> " . $intNomor; 
        }

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (b.vcNama LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT 
						b.intNomor, 
						b.vcNama,
                        a.vcJabatan,
                        c.vcHP
					FROM muser_android a 
					JOIN muser b ON a.intNomorMUser = b.intNomor
                    JOIN msales c ON a.intNomorMSales = c.intNomor 
					WHERE a.intStatus > 0 $intNomor $search 
					ORDER BY b.vcNama 
					$limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                $db_android   = $this->load->database('db_android', TRUE);
                $countMessage = $db_android->query("SELECT COUNT(a.txtMessage) AS countMessage FROM tprivate_chat a WHERE a.intStatus = 1 AND intNomorMUser_from ='". $r['intNomor']."' AND intNomorMUser_to ='".$intNomorMUser_to."'")->row()->countMessage;
                
                $user_lat = "0";
                $user_long = "0";
                $queryLatLong = "SELECT a.decLatitude,a.decLongitude,a.dtInsertDate FROM ttracking a WHERE a.intStatus > 0 AND a.intNomorMUser = '". $r['intNomor'] ."' ORDER BY a.dtInsertDate DESC LIMIT 1";
                $resultLatLong = $db_android->query($queryLatLong);
                if( $resultLatLong && $resultLatLong->num_rows() > 0){
                    foreach ($resultLatLong->result_array() as $rs){
                        $current = strtotime(date("Y-m-d"));
						$date    = strtotime($rs["dtInsertDate"]);

						$datediff = $date - $current;
						$difference = floor($datediff/(60*60*24));
						if($difference == 0)
						{
	                        $user_lat = $rs["decLatitude"];
	                        $user_long = $rs["decLongitude"];
						}
                    }
                }

                array_push($data['data'], array(
                                                'intNomor'      => $r['intNomor'],
                                                'vcNama'        => $r['vcNama'],
                                                'vcJabatan'     => $r['vcJabatan'],
                                                'vcHp'          => $r['vcHP'],
                                                'countMessage'  => $countMessage,
                                                'decLatitude'   => $user_lat,
                                                'decLongitude'  => $user_long
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


    function alldatabarang_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $barang_nomor = (isset($jsonObject["barang_nomor"]) ? $this->clean($jsonObject["barang_nomor"]) : "");
        if($barang_nomor != ""){ $barang_nomor = " AND a.intNomor = $barang_nomor "; }

        $barang_kode = (isset($jsonObject["barang_kode"]) ? $this->clean($jsonObject["barang_kode"]) : "");
        $barang_nama = (isset($jsonObject["barang_nama"]) ? $this->clean($jsonObject["barang_nama"]) : "%");
        $tipenamabarang = (isset($jsonObject["tipenamabarang"]) ? $this->clean($jsonObject["tipenamabarang"]) : "jual");

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ 
            if($tipenamabarang == "jual"){
                $search = "HAVING (a.vcKode = '$search' OR `Nama Jual` LIKE '%$search%')"; 
            }else if($tipenamabarang == "beli"){
                $search = "HAVING (a.vcKode = '$search' OR `Nama Beli` LIKE '%$search%')"; 
            }
        }else{
            $search = "HAVING (a.vcKode = '' OR `Nama Jual` LIKE '%' OR `Nama Beli` LIKE '%')"; 
        }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "SELECT a.intNomor,
                           a.intNomorMSatuan1,
                           a.intNomorMSatuan2,
                           a.intNomorMSatuan3, 
                           a.vcKode AS `Kode Barang`, 
                       a.vcNamaBeli AS `Nama Beli`, 
                       F_Barang_GetNamaJual (a.vcNamaBeli, a1.vcNama, a2.vcNama, a3.vcNama, a4.vcNama, a.decP, a.decL, a.decT) AS `Nama Jual`,
                           a5.vcNama AS `Satuan 1`,
                           1 AS INVCOL,
                           a6.vcNama AS `Satuan 2`,
                           a.decKonversi2 AS `Konversi 2`,
                           a7.vcNama AS `Satuan 3`,
                           a.decKonversi3 AS `Konversi 3`
                FROM mbarang a 
                     JOIN mbrand   a1 ON a1.intNomor = a.intNomorMBrand 
                     JOIN mtipe    a2 ON a2.intNomor = a.intNomorMTipe 
                     JOIN mgrade   a3 ON a3.intNomor = a.intNomorMGrade 
                     JOIN msurface a4 ON a4.intNomor = a.intNomorMSurface 
                     JOIN msatuan  a5 ON a5.intNomor = a.intNomorMSatuan1
                     JOIN msatuan  a6 ON a6.intNomor = a.intNomorMSatuan2
                     JOIN msatuan  a7 ON a7.intNomor = a.intNomorMSatuan3
                WHERE a.intNomor > 0 AND a.intStatus > 0 AND a.intAktif > 0 AND a.intKomoditi = 1 $barang_nomor 
                $search 
                ORDER BY `Nama Jual` 
                $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'          => $r['intNomor'], 
                                                'intNomorMSatuan1'  => $r['intNomorMSatuan1'], 
                                                'intNomorMSatuan2'  => $r['intNomorMSatuan2'],
                                                'intNomorMSatuan3'  => $r['intNomorMSatuan3'], 
                                                'barang_kode'       => $r['Kode Barang'],
                                                'nama_beli'         => $r['Nama Beli'],
                                                'nama_jual'         => $r['Nama Jual'], 
                                                'nama_satuan1'      => $r['Satuan 1'],
                                                'invcol'            => $r['INVCOL'],
                                                'nama_satuan2'      => $r['Satuan 2'],
                                                'nama_konversi2'    => $r['Konversi 2'],
                                                'nama_satuan3'      => $r['Satuan 3'],
                                                'nama_konversi3'    => $r['Konversi 3']
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

    function alldatasatuanharga_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $barang_nomor = (isset($jsonObject["barang_nomor"]) ? $this->clean($jsonObject["barang_nomor"]) : "2678");
        $kurs_nomor = (isset($jsonObject["kurs_nomor"]) ? $this->clean($jsonObject["kurs_nomor"]) : "1");

        $kurs_nama = "";
        if($kurs_nomor != ""){ 
            $kurs_nama = $this->db->query("SELECT vcNama FROM mvaluta where intNomor ='". $kurs_nomor."'")->row()->vcNama;   
        }

        $query = "  select intNomor, vcNama as Satuan, intNomorMSatuan1, decKonversi, decHarga  
                    from (select 1 as intOrder, b.intNomor , b.vcNama, a.intNomorMSatuan1 as intNomorMSatuan1, 1 as decKonversi, 
                    CASE WHEN $kurs_nomor = 1 THEN a.decHargaPL1 
                        WHEN $kurs_nomor = 2 THEN a.decHargaPL1USD 
                        WHEN $kurs_nomor = 3 THEN a.decHargaPL1RMB 
                        END as decHarga 
                    from mbarang a JOIN msatuan b ON b.intNomor = a.intNomorMSatuan1 
                    where a.intNomor = $barang_nomor and b.intNomor > 0 
                    UNION ALL 
                    select 2 as intOrder, b.intNomor , b.vcNama, a.intNomorMSatuan1 as intNomorMSatuan1, decKonversi2 as decKonversi, 
                    CASE WHEN $kurs_nomor = 1 THEN a.decHargaPL2 
                        WHEN $kurs_nomor = 2 THEN a.decHargaPL2USD 
                        WHEN $kurs_nomor = 3 THEN a.decHargaPL2RMB 
                        END as decHarga 
                    from mbarang a JOIN msatuan b ON b.intNomor = a.intNomorMSatuan2 
                    where a.intNomor = $barang_nomor and b.intNomor > 0 
                    UNION ALL 
                    select 3 as intOrder, b.intNomor , b.vcNama, a.intNomorMSatuan1 as intNomorMSatuan1, decKonversi2 * decKonversi3 as decKonversi, 
                    CASE WHEN $kurs_nomor = 1 THEN a.decHargaPL3 
                        WHEN $kurs_nomor = 2 THEN a.decHargaPL3USD 
                        WHEN $kurs_nomor = 3 THEN a.decHargaPL3RMB 
                        END as decHarga 
                    from mbarang a JOIN msatuan b ON b.intNomor = a.intNomorMSatuan3 
                    where a.intNomor = $barang_nomor and b.intNomor > 0) a 
                    order by a.intOrder";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            $arr = array();

            foreach ($result->result_array() as $r){
                array_push($arr, array(
                                                'intNomor'          => $r['intNomor'], 
                                                'intNomorMSatuan1'  => $r['intNomorMSatuan1'],
                                                'decKonversi'       => $r['decKonversi'], 
                                                'decHarga'          => $r['decHarga'],
                                                'Satuan'            => $r['Satuan']
                                        )
                );
            }

            array_push($data['data'], array(
                                            'intNomor'          => $barang_nomor, 
                                            'intNomorMSatuan1'  => $arr[0]['intNomor'], 
                                            'intNomorMSatuan2'  => $arr[1]['intNomor'],
                                            'intNomorMSatuan3'  => $arr[2]['intNomor'], 
                                            'decHargaPL1'       => $arr[0]['decHarga'], 
                                            'decHargaPL2'       => $arr[1]['decHarga'],
                                            'decHargaPL3'       => $arr[2]['decHarga'],
                                            'nama_satuan1'      => $arr[0]['Satuan'],
                                            'nama_satuan2'      => $arr[1]['Satuan'],
                                            'nama_satuan3'      => $arr[2]['Satuan']
                                            )
            );
        }else{      
            array_push($data['data'], array( 'query' => $this->error($query) ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    function alldatasatuanhargalama_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $barang_nomor = (isset($jsonObject["barang_nomor"]) ? $this->clean($jsonObject["barang_nomor"]) : "");
        if($barang_nomor != ""){ $barang_nomor = " AND a.intNomor = $barang_nomor "; }

        $kurs_nomor = (isset($jsonObject["kurs_nomor"]) ? $this->clean($jsonObject["kurs_nomor"]) : "1");
        if($kurs_nomor != ""){ 
            $kurs_nama = $this->db->query("SELECT vcNama FROM mvaluta where intNomor ='". $kurs_nomor."'")->row()->vcNama;   
            if($kurs_nama == "IDR") { $kurs_nama = ""; }
            $kurs_nomor = "";
            $kurs_nomor .= "a.decHargaPL1".$kurs_nama.",";
            $kurs_nomor .= "a.decHargaPL2".$kurs_nama.",";
            $kurs_nomor .= "a.decHargaPL3".$kurs_nama."";
        }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT a.intNomor, 
                           a.intNomorMSatuan1,
                           a.intNomorMSatuan2,
                           a.intNomorMSatuan3, 
                           $kurs_nomor, 
                           a5.vcNama AS `Satuan 1`,
                           a6.vcNama AS `Satuan 2`,
                           a7.vcNama AS `Satuan 3`
                    FROM mbarang a 
                         JOIN msatuan  a5 ON a5.intNomor = a.intNomorMSatuan1
                         JOIN msatuan  a6 ON a6.intNomor = a.intNomorMSatuan2
                         JOIN msatuan  a7 ON a7.intNomor = a.intNomorMSatuan3
                    WHERE a.intNomor > 0 AND a.intStatus > 0 AND a.intAktif > 0 AND a.intKomoditi = 1 $barang_nomor  
                    $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor'          => $r['intNomor'], 
                                                'intNomorMSatuan1'  => $r['intNomorMSatuan1'], 
                                                'intNomorMSatuan2'  => $r['intNomorMSatuan2'],
                                                'intNomorMSatuan3'  => $r['intNomorMSatuan3'], 
                                                'decHargaPL1'       => $r['decHargaPL1'.$kurs_nama], 
                                                'decHargaPL2'       => $r['decHargaPL2'.$kurs_nama],
                                                'decHargaPL3'       => $r['decHargaPL3'.$kurs_nama],
                                                'nama_satuan1'      => $r['Satuan 1'],
                                                'nama_satuan2'      => $r['Satuan 2'],
                                                'nama_satuan3'      => $r['Satuan 3']
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

    function alldatacolorshade_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $barang_nomor = (isset($jsonObject["barang_nomor"]) ? $this->clean($jsonObject["barang_nomor"]) : "");
        if($barang_nomor != ""){ $barang_nomor = " AND a.intNomor = $barang_nomor "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT DISTINCT a.intNomor, a.vcKode, b.vcShade 
                    FROM mbarang a 
                    JOIN tlaporanstok b ON a.intNomor = b.intNomorMBarang 
                    WHERE a.intNomor > 0 AND a.intStatus > 0 AND a.intAktif > 0 AND a.intKomoditi = 1 $barang_nomor  
                    $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor' => $r['intNomor'], 
                                                'vcKode'   => $r['vcKode'], 
                                                'vcShade'  => $r['vcShade'] 
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

    function alldatabrand_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $brand_nomor = (isset($jsonObject["brand_nomor"]) ? $this->clean($jsonObject["brand_nomor"]) : "");
        if($brand_nomor != ""){ $brand_nomor = " AND a.intNomor = " . $brand_nomor; }

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT 
                        a.intNomor,
                        a.vcNama
                    FROM mbrand a
                    WHERE intStatus = 1 $search $brand_nomor 
                    ORDER BY a.vcNama 
                    $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor' => $r['intNomor'], 
                                                'vcNama'   => $r['vcNama']
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

    function alldatatipe_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $tipe_nomor = (isset($jsonObject["tipe_nomor"]) ? $this->clean($jsonObject["tipe_nomor"]) : "");
        if($tipe_nomor != ""){ $tipe_nomor = " AND a.intNomor = " . $tipe_nomor; }

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT 
                        a.intNomor,
                        a.vcNama
                    FROM mtipe a
                    WHERE intStatus = 1 $search $tipe_nomor 
                    ORDER BY a.vcNama 
                    $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor' => $r['intNomor'], 
                                                'vcNama'   => $r['vcNama']
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


    function alldatakategori_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $kategori_nomor = (isset($jsonObject["kategori_nomor"]) ? $this->clean($jsonObject["kategori_nomor"]) : "");
        if($kategori_nomor != ""){ $kategori_nomor = " AND a.intNomor = " . $kategori_nomor; }

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT 
                        a.intNomor,
                        a.vcNama
                    FROM mkategori a
                    WHERE intStatus = 1 $search $kategori_nomor 
                    ORDER BY a.vcNama 
                    $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor' => $r['intNomor'], 
                                                'vcNama'   => $r['vcNama']
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

    function alldatagroupbarang_post(){     
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $groupbarang_nomor = (isset($jsonObject["groupbarang_nomor"]) ? $this->clean($jsonObject["groupbarang_nomor"]) : "");
        if($groupbarang_nomor != ""){ $groupbarang_nomor = " AND a.intNomor = " . $groupbarang_nomor; }

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $query = "  SELECT 
                        a.intNomor,
                        a.vcNama
                    FROM mgroup a
                    WHERE intStatus = 1 $search $groupbarang_nomor 
                    ORDER BY a.vcNama 
                    $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'intNomor' => $r['intNomor'], 
                                                'vcNama'   => $r['vcNama']
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
