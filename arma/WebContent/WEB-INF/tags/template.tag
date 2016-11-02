<%@ tag pageEncoding="UTF-8" body-content="scriptless"%><%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%><%@ taglib prefix="tags" tagdir="/WEB-INF/tags"%>
<%@ attribute name="className" type="java.lang.String" required="true" rtexprvalue="true"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<link rel="shortcut icon" href="${static}/static/ui/images/favicon.ico">
<script type="text/javascript" src="${static}/static/jquery-1.7.1.min.js" charset="utf-8"></script>
<script type="text/javascript" src="${static}/static/ui/jquery.form.js" charset="utf-8"></script>
<script type="text/javascript" src="${static}/static/ui/jquery-ui.min.js" charset="utf-8"></script>
<script type="text/javascript" src="${static}/static/ui/jquery.mousewheel.min.js" charset="utf-8"></script>
<script type="text/javascript" src="${static}/static/ui/jquery.mCustomScrollbar.js" charset="utf-8"></script>
<script type="text/javascript" src="${static}/static/ui/admin.js" charset="utf-8"></script>
<!--[if lte IE 6]><script type="text/javascript" src="${static}/static/ui/ie6.js"></script><![endif]-->
<link rel="stylesheet" href="${static}/static/ui/jquery-ui.css" type="text/css"/>
<link rel="stylesheet" href="${static}/static/font-awesome/css/font-awesome.min.css">
<!--[if IE 7]><link rel="stylesheet" href="${static}/static/font-awesome/css/font-awesome-ie7.min.css"><![endif]-->
<link rel="stylesheet" href="${static}/static/ui/admin_page.css" type="text/css"/>
<!--[if lte IE 7]><link rel="stylesheet" href="${static}/static/ui/admin_page_ie7.css" type="text/css"/><![endif]-->
<script>
</script>
</head>
<body class="${className}">
<jsp:doBody/> 
</body>
</html>