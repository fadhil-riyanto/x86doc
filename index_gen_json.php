<?php 
function append($txt) {
	file_put_contents('./html/index.html', $txt.PHP_EOL , FILE_APPEND | LOCK_EX);
}

$data = array_diff(scandir("./html"), array('.', '..', 'script.js', 'style.css', 'index.css', 'data.json'));

$packed = [];
foreach($data as $key => $data_s ) {

	$exp_ = explode("_", explode(".", str_replace(",", "_", $data_s))[0]);
	
	$full_noraw = [
		"title" => explode(".", $data_s)[0],
		"keyword" => $exp_,
		"file" => $data_s
	];
	array_push($packed, $full_noraw);
}


file_put_contents("./html/data.json", json_encode([
	"data" => $packed
], JSON_PRETTY_PRINT));


// var_dump($packed);