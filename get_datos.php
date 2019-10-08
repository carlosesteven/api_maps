<?php
include ('conexion_sql.php');

echo json_encode( $DB->query("SELECT * FROM accidentes"),JSON_UNESCAPED_UNICODE|JSON_PRETTY_PRINT|JSON_UNESCAPED_SLASHES );




?>