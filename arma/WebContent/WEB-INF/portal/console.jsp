<%@ page contentType="text/html; charset=utf-8" language="java" import="java.sql.*" errorPage="" %>
<%@ include file="common.jsp"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><fmt:message key="window_title" /></title>
<link rel="shortcut icon" href="${static}/static/ui/images/favicon.ico">
<script type="text/javascript" src="${static}/static/jquery-1.7.1.min.js" charset="utf-8"></script>
<script type="text/javascript" src="${static}/static/ui/admin.js" charset="utf-8"></script>
<!--[if lte IE 6]><script type="text/javascript" src="${static}/static/ui/ie6.js"></script><![endif]-->
<link rel="stylesheet" href="${static}/static/font-awesome/css/font-awesome.min.css">
<!--[if IE 7]><link rel="stylesheet" href="${static}/static/font-awesome/css/font-awesome-ie7.min.css"><![endif]-->
<link rel="stylesheet" href="${static}/static/ui/admin.css" type="text/css"/>
<script>
var portals='${result.portals}';
window.onbeforeunload = function() {
}

$(function() {
	$(".logo").click(function() {
		window.location = "${context}";
	});
	
	//logout link
	$("#logout").click(function() { 
	   	$.post("${context}/servlets/logout", {}, function(data) {
			window.location.reload(false);
		});
	});
	
	menus('${result.menus}');
	var width = 188, items = [];
	var admin = Boolean(${login_user.admin});
	if (admin && show("funds")) {
		width += 55;
		items.push({"cls":"lives_item_link","link":"funds"});
	}
	if (admin && show("kms")) {
		width += 55;
		items.push({"cls":"lives_item_link","link":"kms"});
	}
	if (admin && show("charts")) {
		width += 55;
		items.push({"cls":"charts_item_link","link":"charts"});
	}
	items.push({"cls":"system_item_link","link":"system"});
	var colors = [{"bk":"#b3d57e","bk2":"#99c15b"},{"bk":"#fed762","bk2":"#f8bd1d"},{"bk":"#7bbfe6","bk2":"#50a1cf"},{"bk":"#ca84ba","bk2":"#bb67a8"}];
	var lenc = colors.length, lenm = items.length;
	for (var i = 0; i < lenm; i++) {
		var item = items[i], color = colors[i % lenc];
		var label = msg(item.link);
		var $mli = $(menu_li.replace(/\{class\}/gi, item.cls).replace(/\{link\}/gi, "#" + item.link).replace(/\{bk\}/gi, color.bk).replace(/\{bk2\}/gi, color.bk2).replace(/\{label\}/gi, label));
		$('#menu_ul').append($mli);
	}
	$("#user_div").css("right", width + "px");
	 
	$(".menu_item").each(function(index, element) {    
		var linkText= $(this).find("span").html();
		var linkClass = $(this).attr("data-class");
		var title= $(this).children("span").html();
		$(this).addClass($(this).attr("data-class")).attr("title",title).children("span").addClass(linkClass+"_text").hide();	
		$(this).hover(function() {
			$(this).addClass(linkClass+"_hover");
		}, function() {
			$(this).removeClass(linkClass+"_hover"); 
		}).click(function() {
			window.location = $(this).attr("data-link");		
			showPage( $(this).attr("data-link"));
		});
	});

	$("#preloadPage").hide();
	var hash=window.location.hash;
	if (hash.length == 0) {
		var item = $(".menu_item:last");
		if (item) hash = item.attr("data-link");
	}
	
	showPage(hash);
});

var currentPage = null;
//page is like #upload
function showPage(page) {
	if (page == currentPage) {
		//just reload page
		var currentPageId= currentPage+"Frame"; 
		var src=$(currentPageId).attr("src");
		if (src.indexOf("time=") > 0) src = src.substring(0,src.indexOf("time="));
		$(currentPageId).attr("src", src + "time=" + Math.random()); 
		return;  
	}
	
	if (currentPage == '#charts') showCharts(false); 
	
	var pageId= page+"Frame";
	var currentPageId= currentPage+"Frame";
	$(currentPageId).css("left","-1000px");
	if ($(pageId).length > 0) {
		$(pageId).css("left","0px");
		currentPage = page; 
		if (currentPage == '#charts') showCharts(true); 
	} else {
		currentPage = page;
		var domPageId = pageId.substring(1);
		$('<iframe id="'+pageId.substring(1)+'" src="${context}/servlets/portal/loading?page=${context}/servlets/portal/'+page.substring(1)+'&time='+Math.random()+'" class="page" width="1000" height="800" allowtransparency="true" frameborder="0" scrolling="auto"></iframe>').appendTo("#pageContent").css("left","1000px");
		$("#"+domPageId).css("left","0px"); 
	}
	onPageChanged(page);
}

function showCharts(flag) {
	var src = "${context}/servlets/portal/loading?page=${context}/servlets/portal/charts&time=" + Math.random() + "&timer=" + (flag ? "start" : "stop");
	$("#chartsFrame").attr("src", src); 
}

function onPageChanged(page) {
	var hash = page;
	$(".menu_item").each(function(index, element) {
		if (hash==$(this).attr("data-link")) {
			var className=$(this).attr("data-class");
			$(this).addClass(className+"_selected"); 
			$(this).children("span").show();
		} else {
			var className=$(this).attr("data-class");
			$(this).removeClass(className+"_selected"); 
			$(this).children("span").hide(); 
		}	  
	});
}

var menu_li = '<li class="menu_item" data-class="{class}" data-link="{link}" style="background-color:{bk};-moz-user-select:none;" ';
menu_li += 'onmouseover="this.style.backgroundColor=\'{bk2}\'" onmouseout="this.style.backgroundColor=\'{bk}\'"><span>{label}</span></li>';
</script>
</head>
<body>
<div id="wrapper" class="wrapper">
    <div class="mainpage">
        <div class="top"></div>
        <div class="header">
            <div class="logo"></div>
            <div class="menu">
                <ul id="menu_ul"></ul>
                <div class="clear"></div>
                <div class="menu_tip_container"></div>
                <div class="clear"></div>
            </div>
            <div class="user" id="user_div">Hi, ${login_user.name}<c:if test="${can_logout}"><span id="logout" style="cursor:pointer;margin-left:15px;text-decoration:underline;"><fmt:message key="label_logout" /></span></c:if></div>
        </div>
        <div class="content" id="pageContent">
            <!-- loading page -->
            <iframe id="preloadPage" class="page" src="${context}/servlets/portal/loading" allowtransparency="true" width="1000" height="640" frameborder="0" scrolling="auto"></iframe>
        </div>
        <div class="footer"></div>
        <div class="bottom"></div>
    </div>
</div>
<div style="display:none">
	<div id="msg_funds"><fmt:message key="label_funds" /></div><div id="msg_kms"><fmt:message key="label_kms" /></div>
	<div id="msg_charts"><fmt:message key="label_charts" /></div><div id="msg_system"><fmt:message key="label_system" /></div>
</div>
</body>
</html>