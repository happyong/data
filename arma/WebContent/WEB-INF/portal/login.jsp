<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ include file="common.jsp"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><fmt:message key="window_title" /></title>
<link rel="shortcut icon" href="${static}/static/ui/images/favicon.ico">
<script type="text/javascript" src="${static}/static/jquery-1.7.1.min.js" charset="utf-8"></script>
<script type="text/javascript" src="${static}/static/ui/jquery.form.js" charset="utf-8"></script>
<script type="text/javascript" src="${static}/static/ui/admin.js" charset="utf-8"></script>
<!--[if lte IE 6]><script type="text/javascript" src="${static}/static/ui/ie6.js"></script><![endif]-->
<link rel="stylesheet" href="${static}/static/font-awesome/css/font-awesome.min.css">
<!--[if IE 7]><link rel="stylesheet" href="${static}/static/font-awesome/css/font-awesome-ie7.min.css"><![endif]-->
<link rel="stylesheet" href="${static}/static/ui/admin.css" type="text/css"/>
<script language="javascript">
$(function() {
	var name = "${login_user_name}";
	if (!isEmpty(name)) {
		$('#loginForm').submit();
		return;
	}
	if (self != top) top.location = "${context}";
});

function login() {
	var username = $(":input[id='username']").val();
	if (isEmpty(username) || username == msg('login_account_tip')) {
		showErrorTip(msg('login_account_alert'));
		return;
	}
	
	var password = $(":input[id='password']").val();
	if (isEmpty(password)) {
		showErrorTip(msg('login_password_alert'));
		return;
	}
	
	$.post("${context}/servlets/login", {"username":username,"password":password}, function(data) {
		if (data.result)
			$('#loginForm').submit();
		else
			showErrorTip(msg('login_alert'));
	});
}

function showErrorTip(html) {
	$("#errorTip").html(html);
}

function isEmpty(val) {
	return (val == null || $.trim(val).length == 0);
}
</script>
</head>
<body>
<div id="wrapper" class="wrapper1">
	<div class="loginpage">
		<div class="header">
			<div class="logo"></div>
		</div>
		<div class="login">
			<form id="loginForm" action="${context}/servlets/portal/console#system" method="post">
				<table width="" cellpadding="0" cellspacing="0">
					<tr>
						<td colspan="3" height="25" valign="middle" align="center">
							<div class="error" id="errorTip"></div>
						</td>
					</tr>
					<tr>
						<td width="117" style="color:#000000" height="32" align="right"><fmt:message key="login_account_label" /></td>
						<td height="45" valign="middle" style="padding-left:10px;"><input type="text" id="username" value="<fmt:message key='login_account_tip' />" class="grayTip" style="width:180px;height:26px;line-height:26px" onkeydown="if(event.keyCode==13) login();"/></td>
						<td>&nbsp;</td>
					</tr>
					<tr>
						<td align="right" style="color:#000000"><fmt:message key="login_password_label" /></td>
						<td height="45" valign="middle" style="padding-left:10px;"><input type="password" id="password" value="111" style="width:180px;height:26px;line-height:26px" onkeydown="if(event.keyCode==13) login();"/></td>
						<td style="padding-left:15px;"><div class="button" style="width:80px;margin-left:0px;height:25px;line-height:25px;" onclick="login()"><fmt:message key="login_button" /></div></td>
					</tr>
				</table>
			</form>
		</div>
		<div class="add_line">
			<div style="float:left" class="left"></div>
			<div style="float:left" class="middle"></div>
			<div style="float:left" class="right"></div>
		</div>
		<div class="footer"></div>
	</div>
</div>

<div style="display:none">
	<div id="msg_login_account_alert"><fmt:message key="login_account_alert" /></div>
	<div id="msg_login_account_tip"><fmt:message key="login_account_tip" /></div>
	<div id="msg_login_alert"><fmt:message key="login_alert" /></div>
	<div id="msg_login_password_alert"><fmt:message key="login_password_alert" /></div>
</div>
</body>
</html>