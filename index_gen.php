<?php 
function append($txt) {
	file_put_contents('./html/index.html', $txt.PHP_EOL , FILE_APPEND | LOCK_EX);
}

$data = array_diff(scandir("./html"), array('.', '..', 'script.js', 'style.css'));

append("<h1><b>x86/x64 Instruction Set Reference</b></h1></br></br>");

foreach ($data as $key ) {
	append("<a href=\"" . $key . "\">" . $key . "</a></br>");
}