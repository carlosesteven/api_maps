<?php 
	include ('conexion_sql.php');

if( $_REQUEST && $_REQUEST["archivo"] )
{
	$foto_enviada = $_REQUEST["archivo"];

	$id_foto = uniqid().".jpg";

	$Image1_path = "archivos/".$id_foto;

	file_put_contents($Image1_path,base64_decode($foto_enviada));

	$latitude=$_REQUEST['latitud'];
	$longitude=$_REQUEST['longitud'];
	$color = $_REQUEST['color'];
	$foto = $id_foto;
	$valida = $DB->query("SELECT * FROM accidentes WHERE latitud = $latitude OR longitud = $longitude");
	if (  count($valida) == 0 ) 
	{
		$DB->query("INSERT INTO accidentes (latitud,longitud,color,foto)
		VALUES(
	    '$latitude',
	    '$longitude',
	    '$color',
	    '$foto'
	    )");
	    
	    $datos["estado"] = "ok";
		$datos["estado"] = "Se registraron los datos.";

	}else{
		$datos["estado"] = "error";
		$datos["estado"] = "Ya se ha registrado el accidente.";
	}

	$datos["peticion_enviada"] = $_REQUEST;	

	//$datos["estado"] = "ok";
	//$datos["detalles"] = "Archivo guardado con exito";	
	//$datos["url_completo"] = $Image1_path;	
	
}else{
	$datos["estado"] = "error";
	$datos["detalles"] = "Archivo no se pudo guardar"; 
}

header('Content-Type: application/json; charset=utf-8');
echo json_encode($datos, JSON_UNESCAPED_UNICODE|JSON_PRETTY_PRINT|JSON_UNESCAPED_SLASHES );

/*
$nombre=$_FILES['archivo']['name'] . uniqid();
$guardado=$_FILES['archivo']['tmp_name'];

$datos = array();

if(!file_exists('files')){
	mkdir('files',0777,true);
	if(file_exists('files')){
		if(move_uploaded_file($guardado, 'files/'.$nombre)){
			$datos["estado"] = "ok";
			$datos["detalles"] = "Archivo guardado con exito";	
		}else{
			$datos["estado"] = "error";
			$datos["detalles"] = "Archivo no se pudo guardar";
		}
	}
}else{
	if(move_uploaded_file($guardado, 'files/'.$nombre)){
		$datos["estado"] = "ok";
		$datos["detalles"] = "Archivo guardado con exito";
	}else{
		$datos["estado"] = "error";
		$datos["estado"] = "Archivo no se pudo guardar";
	}
}
*/

//header('Content-Type: application/json; charset=utf-8');
//echo json_encode($datos, JSON_UNESCAPED_UNICODE|JSON_PRETTY_PRINT|JSON_UNESCAPED_SLASHES );

?>