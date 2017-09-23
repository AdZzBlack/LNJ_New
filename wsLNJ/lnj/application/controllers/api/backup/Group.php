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
class Group extends REST_Controller { 

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
    function createNewGroup_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = json_decode($value);

        $db_android = $this->load->database('db_android', TRUE);

        $db_android->trans_begin();

        $group_nomor = "";
        $admin_nomor = "";

        foreach ($jsonObject->TeamGroup as $group) {
           
            $vcNama      = $group->group_nama;
            $admin_nomor = $group->user_nomor;
            
            $query = $db_android->insert_string('tgroup_team', array(
                                                              'vcNama'        => $vcNama,
                                                              'intNomorMUser' => $admin_nomor
                                                            )
                                        );
            $db_android->query($query);
            $group_nomor = $db_android->insert_id();
        }
        
        if($group_nomor != ""){
            foreach ($jsonObject->UserGroup as $team) {
                $intNomorMUser = json_decode($team, TRUE);

                for($i = 0; $i < count($team); $i++ ){
                    $query = $db_android->insert_string('tdgroup_team', array(
                                                                              'intNomorTGroupTeam'    => $group_nomor,
                                                                              'intNomorMUser'         => $team[$i],
                                                                              'intNomorMUserInsertBy' => $admin_nomor
                                                                            )
                    );
                    $db_android->query($query);
                }
            }        

        }

        if ($db_android->trans_status() === FALSE){
            $db_android->trans_rollback();
            array_push($data['data'], array( 'success' => "false" ));
        }else{
            $db_android->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    // --- POST Login --- //
    function updateGroup_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = json_decode($value);

        $db_android = $this->load->database('db_android', TRUE);

        $db_android->trans_begin();

        $group_nomor = "";
        $admin_nomor = "";

        foreach ($jsonObject->TeamGroup as $group) {
           
            $group_nama  = $group->group_nama;
            $group_nomor = $group->group_nomor;
            
            $db_android->query("UPDATE tgroup_team SET vcNama = '$group_nama' WHERE intNomor = $group_nomor");
        }
        
        if($group_nomor != ""){
            $db_android->query("DELETE FROM tdgroup_team WHERE intNomorTGroupTeam = $group_nomor");
            
            foreach ($jsonObject->UserGroup as $team) {
                $intNomorMUser = json_decode($team, TRUE);

                for($i = 0; $i < count($team); $i++ ){
                    $query = $db_android->insert_string('tdgroup_team', array(
                                                                              'intNomorTGroupTeam'    => $group_nomor,
                                                                              'intNomorMUser'         => $team[$i],
                                                                              'intNomorMUserInsertBy' => $admin_nomor
                                                                            )
                    );
                    $db_android->query($query);
                }
            }        

        }

        if ($db_android->trans_status() === FALSE){
            $db_android->trans_rollback();
            array_push($data['data'], array( 'success' => "false" ));
        }else{
            $db_android->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }

    }

    // --- POST Login --- //
    function addGroupTeam_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $group_nomor    = (isset($jsonObject["group_nomor"])    ? $this->clean($jsonObject["group_nomor"])    : "");
        $user_nomor     = (isset($jsonObject["user_nomor"])     ? $this->clean($jsonObject["user_nomor"])     : "");
        $insertby_nomor = (isset($jsonObject["insertby_nomor"]) ? $this->clean($jsonObject["insertby_nomor"]) : "");

        $db_android = $this->load->database('db_android', TRUE);

        $db_android->trans_begin();

        $query = $db_android->insert_string('tdgroup_team', array(
                                                              'intNomorTGroupTeam'    => $group_nomor,
                                                              'intNomorMUser'         => $user_nomor,
                                                              'intNomorMUserInsertBy' => $insertby_nomor
                                                            )
                                        );
        $db_android->query($query);

        if ($db_android->trans_status() === FALSE){
            $db_android->trans_rollback();
            array_push($data['data'], array( 'success' => "false" ));
        }else{
            $db_android->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));
        }  

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }

    function removeGroupTeam_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $group_nomor    = (isset($jsonObject["group_nomor"])    ? $this->clean($jsonObject["group_nomor"])    : "");
        $user_nomor     = (isset($jsonObject["user_nomor"])     ? $this->clean($jsonObject["user_nomor"])     : "");
        $removeby_nomor = (isset($jsonObject["removeby_nomor"]) ? $this->clean($jsonObject["removeby_nomor"]) : "");

        $db_android = $this->load->database('db_android', TRUE);

        $db_android->trans_begin();

        $db_android->query("UPDATE tgroup_team SET intStatus = 0 WHERE intNomor = $group_nomor");
        $db_android->query("UPDATE tdgroup_team SET intStatus = 0 ,intNomorMUserRemoveBy = '$group_nama' WHERE intNomorTGroupTeam = $group_nomor AND intNomorMUser = $user_nomor");

        if ($db_android->trans_status() === FALSE){
            $db_android->trans_rollback();
            array_push($data['data'], array( 'success' => "false" ));
        }else{
            $db_android->trans_commit();
            array_push($data['data'], array( 'success' => "true" ));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }



    function getGroupTeam_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $user_nomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"])   : "32");
        if($user_nomor != ""){ $user_nomor = " AND a.intNomorMUser = $user_nomor "; }

        $search = (isset($jsonObject["search"]) ? $this->clean($jsonObject["search"]) : "");
        if($search != ""){ $search = " AND (a.vcNama LIKE '%$search%') "; }

        $limit = (isset($jsonObject["limit"]) ? $this->clean($jsonObject["limit"]) : "");
        $masterlimit = 10;
        if($limit != ""){ $limit = " LIMIT " . intval($limit) * $masterlimit . ", " . $masterlimit ; }

        $db_android = $this->load->database('db_android', TRUE);

        $query = "  SELECT 
					a.intNomor AS group_nomor,
					a.intNomorMUser AS admin_nomor,
					a.vcNama AS group_nama,
					a.intStatus
					FROM tgroup_team a 
					WHERE intStatus = 1 AND intNomor = 0
					UNION
					SELECT 
					a.intNomorTGroupTeam AS group_nomor,
					b.intNomorMUser AS admin_nomor,
					b.vcNama AS group_nama,
					a.intStatus
					FROM tdgroup_team a
					JOIN tgroup_team b ON a.intNomorTGroupTeam = b.intNomor AND b.intStatus = 1 
					WHERE a.intStatus = 1 $user_nomor $search
                    $limit";
        $result = $db_android->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){
                array_push($data['data'], array(
                                                'group_nomor' => $r['group_nomor'], 
                                                'group_nama'  => $r['group_nama']
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


    function getDetailGroupTeam_post(){     

        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $group_nomor = $this->clean($jsonObject["group_nomor"]);
        if($group_nomor != ""){ $group_nomor = " AND intNomorTGroupTeam = $group_nomor "; }

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
                    WHERE a.intStatus > 0  
                    ORDER BY b.vcNama 
                    $limit";
        $result = $this->db->query($query);

        if( $result && $result->num_rows() > 0){
            foreach ($result->result_array() as $r){

                $db_android   = $this->load->database('db_android', TRUE);
                
                $user_lat = "0";
                $user_long = "0";
                $queryLatLong = "SELECT a.decLatitude,a.decLongitude FROM ttracking a WHERE a.intStatus > 0 AND a.intNomorMUser = '". $r['intNomor'] ."' ORDER BY a.dtInsertDate DESC LIMIT 1";
                $resultLatLong = $db_android->query($queryLatLong);
                if( $resultLatLong && $resultLatLong->num_rows() > 0){
                    foreach ($resultLatLong->result_array() as $rs){
                        $user_lat = $rs["decLatitude"];
                        $user_long = $rs["decLongitude"];
                    }
                }

                $user_ingroup = "false";
                $queryingroup = "   SELECT 
                                    a.intNomor,
                                    a.intNomorTGroupTeam AS group_nomor,
                                    a.intNomorMUser AS usergroup_nomor
                                    FROM tdgroup_team a 
                                    WHERE a.intStatus = 1 AND a.intNomorMUser = ".$r['intNomor']." $group_nomor";
                $resultingroup = $db_android->query($queryingroup);
                if( $resultingroup && $resultingroup->num_rows() > 0){
                    foreach ($resultingroup->result_array() as $rg){
                        if($r['intNomor'] == $rg["usergroup_nomor"]){
                            $user_ingroup = "true";
                        }
                    }
                }

                array_push($data['data'], array(
                                                'intNomor'      => $r['intNomor'],
                                                'vcNama'        => $r['vcNama'],
                                                'vcJabatan'     => $r['vcJabatan'],
                                                'vcHp'          => $r['vcHP'],
                                                'decLatitude'   => $user_lat,
                                                'decLongitude'  => $user_long,
                                                'exists'        => $user_ingroup
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
