<style>
html, body, div, span, applet, object, iframe, h1, h2, h3, h4, h5, h6, p, blockquote, pre, a, abbr, acronym, address, big, cite, code, del, dfn, em, font, img, ins, kbd, q, s, samp, small, strike, strong, sub, sup, tt, var, b, u, i, center, dl, dt, dd, ol, ul, li, fieldset, form, label, legend, table, caption, tbody, tfoot, thead, tr, th, td {
margin:0;padding:0;border:0;outline: 0;}
html{-webkit-text-size-adjust:none;}
div {overflow:hidden;}
ol, ul {list-style: none;}
body {
	background-color:#FFFFFF;
	background-position:center top;
	background-repeat:no-repeat;
	min-height:768px;
	margin-left:auto;
	margin-right:auto;
	width:1039px;
	color:#3C3C3C;
	font-family:arial;
	font-size:12pt;
	position:relative;
}
.clear {
	clear: both;
	content: " ";
	display: block;
	font-size: 0;
	/*height: 0;*/
	line-height: 0;
	visibility: hidden;
	/*width: 0;*/
}
.hidden {display:none;}
a,.btn {color:#CC0000;text-decoration:none;}
a:hover,.btn:hover {color:#FF0000;text-decoration:underline;}
hr {height:1px;color:#666666;width:100%;}
.container {padding-left:20px;padding-right:20px;background-color:#FFFFFF;}
.blank {content: " ";height:20px;}
.blank2 {content: " ";height:5px;}
.bold {font-weight:bold;}
.fleft {float:left;}
.fright {float:right;}
.pleft {padding-left:5px;}
.fgrey {color:#666666;font-weight:bold;}
.fred {color:#FF0000;font-weight:bold;}
.fgreen {color:#006600;font-weight:bold;}
.fblue {color:#000066;font-weight:bold;}
.player {color:#006600;font-weight:bold;}
.title {font-size:14pt;font-weight:bold;}
.small {font-size:10pt;font-weight:normal;}
.h1 {font-weight:bold;}
.field {width:1000px;}
.label,.label2 {padding-left:20px;width:180px;font-weight:bold;text-align:right;float:left;}
.label2 {width:360px;}
.value,.value2,.value3 {padding-left:20px;width:280px;text-align:left;float:left;}
.value2 {width:780px;}
.value3 {width:560px;}
.solid {border:1px solid #666666;}
.twoline {height:45px;}
.oneline {height:30px;}
.header {text-align:center;}
</style>
<script type="text/javascript">
function refresh() 
{
	document.location.href = document.location.href;
}
function add() 
{
	count++; 
	document.getElementById("count").innerHTML = limit - count;
	if (count == limit) 
	{
		count = 0;
		refresh();
	}
}
var limit = 120;
var count = 0;
//var refresh_timer = setInterval("add()", 1000);
</script>