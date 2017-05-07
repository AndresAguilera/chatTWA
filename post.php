<?php

$tipo = $_POST['tipo'];
if($tipo === 'POST'){
    $msg = $_POST['msg'];
    $myfile = fopen("chatlog.txt", "w") or die("Unable to open file!");
    $txt = $msg;
    fwrite($myfile, $txt);
    fclose($myfile);
}
?>