<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ include file="common.jsp"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script type="text/javascript" src="${static}/static/jquery-1.7.1.min.js" charset="utf-8"></script>
<script type="text/javascript" src="${static}/static/ui/jquery.swfobject.1-1-1.min.js" charset="utf-8"></script>
<!--[if lte IE 6]><script type="text/javascript" src="${static}/static/ui/ie6.js"></script><![endif]-->
<script>
$(function() {
	$("#loading").flash({
		"swf": '${static}/static/ui/images/loading.swf',
		"height": 70,
		"width": 70,
		"flashvars": {},
		"bgcolor":"#000000", 
		"allowFullScreen":true,
		"allowscriptaccess":true,
		"wmode":"transparent",
		"expressInstaller": '${static}/static/ui/images/expressInstall.swf'
	});	 
 });
</script>
<link rel="stylesheet" href="${static}/static/ui/admin_page.css" type="text/css"/>
</head>
<body>
<table width="1000" height="640">
	<tr>
    	<td valign="middle" align="center"><div id="loading"></div></td>
  	</tr>
</table>
<script>
function page() {
	var page = "${param.page}";
	if (!page) return;
	var timer = Boolean("${param.timer}");
	page += (timer ? "?timer=" + "${param.timer}" : "");
	window.location = page;
}
page();
</script>
</body>
</html>