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
class ContainerLoading extends REST_Controller {

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

    // --- Insert History Driver--- //
    function AddArchieve_post()
    {
        $data['data'] = array();

        $value = file_get_contents('php://input');
        $jsonObject = (json_decode($value , true));

        $kodecontainer = (isset($jsonObject["kodecontainer"]) ? $this->clean($jsonObject["kodecontainer"])     : "");
        $nomorcontainer = (isset($jsonObject["nomorcontainer"]) ? $this->clean($jsonObject["nomorcontainer"])     : "");
        $photoempty = (isset($jsonObject["photoempty"]) ? $jsonObject["photoempty"]     : "");
        $photosealed = (isset($jsonObject["photosealed"]) ? $jsonObject["photosealed"]     : "");
        $photosealedport = (isset($jsonObject["photosealedport"]) ? $jsonObject["photosealedport"]     : "");
        $photoother = (isset($jsonObject["photoother"]) ? $jsonObject["photoother"]     : "");
        $user_nomor = (isset($jsonObject["user_nomor"]) ? $this->clean($jsonObject["user_nomor"])     : "");

        $this->db->trans_begin();
        $query = " UPDATE tdorderjualcontainer SET
                    kodecontainer = '$kodecontainer'
                    WHERE nomor = $nomorcontainer";
        $this->db->query($query);

        $pieces = explode("|", $photoempty);
        for ($i = 0; $i < count($pieces) - 1; $i++) {
            $query1 = " INSERT INTO mharchievedfiles (nomorfile, kodefile, namatable, kode, kategori, nama, directory, status_aktif, dibuat_oleh, dibuat_pada)
                        VALUES
                        (0, '$kodecontainer', 'tdorderjualcontainer', 'test', 'CONTAINER EMPTY', '" . $pieces[$i] . "', 'CONTAINER EMPTY/" . $pieces[$i]. "', 1, $user_nomor, NOW()) ";
            $this->db->query($query1);
        }

        $pieces = explode("|", $photosealed);
        for ($i = 0; $i < count($pieces) - 1; $i++) {
            $query = " INSERT INTO mharchievedfiles (nomorfile, kodefile, namatable, kode, kategori, nama, directory, status_aktif, dibuat_oleh, dibuat_pada)
                        VALUES
                        (0, '$kodecontainer', 'tdorderjualcontainer', 'test', 'CONTAINER SEALED', '" . $pieces[$i] . "', 'CONTAINER SEALED/" . $pieces[$i]. "', 1, $user_nomor, NOW()) ";
            $this->db->query($query);
        }

        $pieces = explode("|", $photosealedport);
        for ($i = 0; $i < count($pieces) - 1; $i++) {
            $query = " INSERT INTO mharchievedfiles (nomorfile, kodefile, namatable, kode, kategori, nama, directory, status_aktif, dibuat_oleh, dibuat_pada)
                        VALUES
                        (0, '$kodecontainer', 'tdorderjualcontainer', 'test', 'CONTAINER SEALED PORT', '" . $pieces[$i] . "', 'CONTAINER SEALED PORT/" . $pieces[$i]. "', 1, $user_nomor, NOW()) ";
            $this->db->query($query);
        }

        $pieces = explode("|", $photoother);
        for ($i = 0; $i < count($pieces) - 1; $i++) {
            $query = " INSERT INTO mharchievedfiles (nomorfile, kodefile, namatable, kode, kategori, nama, directory, status_aktif, dibuat_oleh, dibuat_pada)
                        VALUES
                        (0, '$kodecontainer', 'tdorderjualcontainer', 'test', 'CONTAINER OTHERS', '" . $pieces[$i] . "', 'CONTAINER OTHERS/" . $pieces[$i]. "', 1, $user_nomor, NOW()) ";
            $this->db->query($query);
        }

        if ($this->db->trans_status() === FALSE)
        {
            $this->db->trans_rollback();
            array_push($data['data'], array( 'query' => $this->error($query),
                                             'message' => 'Failed to insert container loading'));
        }
        else
        {
            $this->db->trans_commit();
            array_push($data['data'], array( 'message' => 'Your data has been successfully inserted',
                                                'test' => $query1));
        }

        if ($data){
            // Set the response and exit
            $this->response($data['data']); // OK (200) being the HTTP response code
        }
    }
}