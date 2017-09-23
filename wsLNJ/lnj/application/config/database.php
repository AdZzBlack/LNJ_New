<?php  if ( ! defined('BASEPATH')) exit('No direct script access allowed');
/*
| -------------------------------------------------------------------
| DATABASE CONNECTIVITY SETTINGS
| -------------------------------------------------------------------
| This file will contain the settings needed to access your database.
|
| For complete instructions please consult the 'Database Connection'
| page of the User Guide.
|
| -------------------------------------------------------------------
| EXPLANATION OF VARIABLES
| -------------------------------------------------------------------
|
|	['hostname'] The hostname of your database server.
|	['username'] The username used to connect to the database
|	['password'] The password used to connect to the database
|	['database'] The name of the database you want to connect to
|	['dbdriver'] The database type. ie: mysql.  Currently supported:
				 mysql, mysqli, postgre, odbc, mssql, sqlite, oci8
|	['dbprefix'] You can add an optional prefix, which will be added
|				 to the table name when using the  Active Record class
|	['pconnect'] TRUE/FALSE - Whether to use a persistent connection
|	['db_debug'] TRUE/FALSE - Whether database errors should be displayed.
|	['cache_on'] TRUE/FALSE - Enables/disables query caching
|	['cachedir'] The path to the folder where cache files should be stored
|	['char_set'] The character set used in communicating with the database
|	['dbcollat'] The character collation used in communicating with the database
|	['swap_pre'] A default table prefix that should be swapped with the dbprefix
|	['autoinit'] Whether or not to automatically initialize the database.
|	['stricton'] TRUE/FALSE - forces 'Strict Mode' connections
|							- good for ensuring strict SQL while developing
|
| The $active_group variable lets you choose which connection group to
| make active.  By default there is only one group (the 'default' group).
|
| The $active_record variables lets you determine whether or not to load
| the active record class
*/

$active_group = 'default';
$active_record = TRUE;

//================ database default ================// 
	/*$db['default']['hostname'] = 'localhost';
	$db['default']['username'] = 'root';
	$db['default']['password'] = '';
	$db['default']['database'] = 'venus';
	$db['default']['dbdriver'] = 'mysqli';
	$db['default']['dbprefix'] = '';
	$db['default']['pconnect'] = FALSE;
	$db['default']['db_debug'] = FALSE;
	$db['default']['cache_on'] = FALSE;
	$db['default']['cachedir'] = '';
	$db['default']['char_set'] = 'utf8';
	$db['default']['dbcollat'] = 'utf8_general_ci';
	$db['default']['swap_pre'] = '';
	$db['default']['autoinit'] = TRUE;
	$db['default']['stricton'] = FALSE;


//================ database chatting ================//
	$db['db_android']['hostname'] = 'localhost';
	$db['db_android']['username'] = 'root';
	$db['db_android']['password'] = '';
	$db['db_android']['database'] = 'venus_android';
	$db['db_android']['dbdriver'] = 'mysqli';
	$db['db_android']['dbprefix'] = '';
	$db['db_android']['pconnect'] = FALSE;
	$db['db_android']['db_debug'] = FALSE;
	$db['db_android']['cache_on'] = FALSE;
	$db['db_android']['cachedir'] = '';
	$db['db_android']['char_set'] = 'utf8';
	$db['db_android']['dbcollat'] = 'utf8_general_ci';
	$db['db_android']['swap_pre'] = '';
	$db['db_android']['autoinit'] = TRUE;
	$db['db_android']['stricton'] = FALSE;*/




//================ database default ================// 
	/*$db['default']['hostname'] = '108.179.242.234';
	$db['default']['username'] = 'o5j6w6d9_antonnw';
	$db['default']['password'] = 'o5j6w6d9_antonnw';
	$db['default']['database'] = 'o5j6w6d9_venus';
	$db['default']['dbdriver'] = 'mysqli';
	$db['default']['dbprefix'] = '';
	$db['default']['pconnect'] = FALSE;
	$db['default']['db_debug'] = FALSE;
	$db['default']['cache_on'] = FALSE;
	$db['default']['cachedir'] = '';
	$db['default']['char_set'] = 'utf8';
	$db['default']['dbcollat'] = 'utf8_general_ci';
	$db['default']['swap_pre'] = '';
	$db['default']['autoinit'] = TRUE;
	$db['default']['stricton'] = FALSE;


//================ database chatting ================//
	$db['db_android']['hostname'] = '108.179.242.234';
	$db['db_android']['username'] = 'o5j6w6d9_antonnw';
	$db['db_android']['password'] = 'o5j6w6d9_antonnw';
	$db['db_android']['database'] = 'o5j6w6d9_venus-android';
	$db['db_android']['dbdriver'] = 'mysqli';
	$db['db_android']['dbprefix'] = '';
	$db['db_android']['pconnect'] = FALSE;
	$db['db_android']['db_debug'] = FALSE;
	$db['db_android']['cache_on'] = FALSE;
	$db['db_android']['cachedir'] = '';
	$db['db_android']['char_set'] = 'utf8';
	$db['db_android']['dbcollat'] = 'utf8_general_ci';
	$db['db_android']['swap_pre'] = '';
	$db['db_android']['autoinit'] = TRUE;
	$db['db_android']['stricton'] = FALSE;*/



//================ database ANTOBO ================// 
// 	$db['default']['hostname'] = '156.67.210.104';
// 	$db['default']['username'] = 'antonnw_venus';
// 	$db['default']['password'] = 'antonnw_venus';
// 	$db['default']['database'] = 'antonnw_venus';
// 	$db['default']['dbdriver'] = 'mysqli';
// 	$db['default']['dbprefix'] = '';
// 	$db['default']['pconnect'] = FALSE;
// 	$db['default']['db_debug'] = FALSE;
// 	$db['default']['cache_on'] = FALSE;
// 	$db['default']['cachedir'] = '';
// 	$db['default']['char_set'] = 'utf8';
// 	$db['default']['dbcollat'] = 'utf8_general_ci';
// 	$db['default']['swap_pre'] = '';
// 	$db['default']['autoinit'] = TRUE;
// 	$db['default']['stricton'] = FALSE;


// //================ database ANDROID ANTOBO ================//
// 	$db['db_android']['hostname'] = '156.67.210.104';
// 	$db['db_android']['username'] = 'antonnw_venus';
// 	$db['db_android']['password'] = 'antonnw_venus';
// 	$db['db_android']['database'] = 'antonnw_venus-android';
// 	$db['db_android']['dbdriver'] = 'mysqli';
// 	$db['db_android']['dbprefix'] = '';
// 	$db['db_android']['pconnect'] = FALSE;
// 	$db['db_android']['db_debug'] = FALSE;
// 	$db['db_android']['cache_on'] = FALSE;
// 	$db['db_android']['cachedir'] = '';
// 	$db['db_android']['char_set'] = 'utf8';
// 	$db['db_android']['dbcollat'] = 'utf8_general_ci';
// 	$db['db_android']['swap_pre'] = '';
// 	$db['db_android']['autoinit'] = TRUE;
// 	$db['db_android']['stricton'] = FALSE;



/*//================ database DEFAULT VENUS ================// 
	$db['default']['hostname'] = '25.21.88.145';
	$db['default']['username'] = 'root';
	$db['default']['password'] = 'inspire';
	$db['default']['database'] = 'venus';
	$db['default']['dbdriver'] = 'mysqli';
	$db['default']['dbprefix'] = '';
	$db['default']['pconnect'] = FALSE;
	$db['default']['db_debug'] = FALSE;
	$db['default']['cache_on'] = FALSE;
	$db['default']['cachedir'] = '';
	$db['default']['char_set'] = 'utf8';
	$db['default']['dbcollat'] = 'utf8_general_ci';
	$db['default']['swap_pre'] = '';
	$db['default']['autoinit'] = TRUE;
	$db['default']['stricton'] = FALSE;


//================ database ANDROID VENUS ================//
	$db['db_android']['hostname'] = '25.21.88.145';
	$db['db_android']['username'] = 'root';
	$db['db_android']['password'] = 'inspire';
	$db['db_android']['database'] = 'venus-android';
	$db['db_android']['dbdriver'] = 'mysqli';
	$db['db_android']['dbprefix'] = '';
	$db['db_android']['pconnect'] = FALSE;
	$db['db_android']['db_debug'] = FALSE;
	$db['db_android']['cache_on'] = FALSE;
	$db['db_android']['cachedir'] = '';
	$db['db_android']['char_set'] = 'utf8';
	$db['db_android']['dbcollat'] = 'utf8_general_ci';
	$db['db_android']['swap_pre'] = '';
	$db['db_android']['autoinit'] = TRUE;
	$db['db_android']['stricton'] = FALSE;*/







//================ database DEFAULT INSPIRA ================// 
	$db['default']['hostname'] = 'vpn.inspiraworld.com:3308';
    $db['default']['username'] = 'inspiradb';
    $db['default']['password'] = 'inspira2017!!';
    $db['default']['database'] = 'gms_web';
    $db['default']['dbdriver'] = 'mysqli';
    $db['default']['dbprefix'] = '';
    $db['default']['pconnect'] = FALSE;
    $db['default']['db_debug'] = FALSE;
    $db['default']['cache_on'] = FALSE;
    $db['default']['cachedir'] = '';
    $db['default']['char_set'] = 'utf8';
    $db['default']['dbcollat'] = 'utf8_general_ci';
    $db['default']['swap_pre'] = '';
    $db['default']['autoinit'] = TRUE;
    $db['default']['stricton'] = FALSE;



/* End of file database.php */
/* Location: ./application/config/database.php */