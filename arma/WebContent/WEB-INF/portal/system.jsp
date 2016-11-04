<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java"%>
<%@ include file="common.jsp"%>
<tags:template className="mediaPage">
<script type="text/javascript" src="${static}/static/ui/jquery.uploadify.js" charset="utf-8"></script>
<link rel="stylesheet" href="${static}/static/ui/uploadify.css" type="text/css"/>
<div style="display:none">${result.jssystem}</div>
<script>
var curdetid = "", curpage = "systemInfo", portals='${result.portals}', rouser = Boolean(${login_user.readonly});
$(function() {
	$("#uploadify").uploadify({
		'swf':'${static}/static/ui/uploadify.swf',
		'uploader':'${context}/servlets/agent/upload',
		'fileSizeLimit':'100MB', 
		'fileTypeDesc':msg('label_uploadify_desc'),
		'fileTypeExts':'*.zip',
		'auto':false,
		'multi':false,
		'width':200,
		'height':24,
		'method':'get',
		"formData":{'admin':account,'name':'_component_'},
		'buttonText':msg('label_uploadify_button'),
		'onUploadSuccess': function(file, data, response) {
			alert('Release ' + file.name + ' (' + file.size + ' bytes) uploaded. '); 
		}	        	
	});
	menus('${result.menus}');
	var admin = Boolean(${login_user.admin});	
	$(".menu-funds").css("display", admin && show("funds") ? "" : "none");
	$(".menu-charts").css("display", admin && show("charts") ? "" : "none");
		        		 
	$(".button-return").click(function() {
		onMediaDivChanged("systemInfo");
	}); 
	
	$(".list-menu").each(function(index, element) { 
		$(this).css("color","#3c3c3c").hover(function() {
			$(this).css("color","#cc6600");
		}, function() {
			$(this).css("color","#3c3c3c");
		}).click(function() {
			if (!Boolean(${login_user.admin})) {
				alert(msg("user_read_only"));
				return;
			}
			$("#button-svc-cancel").trigger("click");
			onSvcListPageChanged($(this).attr("data-link"));
			onMediaDivChanged("systemService");
		});
	});
	 
	$(".list-link").each(function(index, element) {    
		$(this).css("color","#555555").hover(function() {
			$(this).css("color","#cc6600");
		}, function() {
			$(this).css("color","#555555");
		}).click(function() {
			$('#svc-ids').empty();
			$('#svc-params').empty();
			$('#svc-release').css("display","none");
			$('#svc-user').css("display","none");
			$('#svc-userdel').css("display","none");
			$('#svc-fundflush').css("display","none");
			
			var nosvc = ($(this).attr("data-link") == 'nosvc');
			var html = $.trim($(this).html());
			switch(html) {
				case "/servlets/kms/knew":
					appendSvcParamRow(nosvc, "kmIdBase", "*", "1601101");
					appendSvcParamRow(nosvc, "kmCount", "*", "10");
					appendSvcParamRow(nosvc, "ckeyId", "*", "160");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/kms/kfmt":
					appendSvcParamRow(nosvc, "kmIdBase", "*");
					appendSvcParamRow(nosvc, "fmtKeys", "*", "0");
					appendSvcParamRow(nosvc, "kmIdMin");
					appendSvcParamRow(nosvc, "kmIdMax");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/kms/kcfmt":
					appendSvcParamRow(nosvc, "ckeyId", "*", "160");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/kms/kdfmt":
					appendSvcParamRow(nosvc, "timeBase", "", new Date().Format("yyyy-MM-dd hh:mm:ss"));
					appendSvcParamRow(nosvc, "minutes", "", "0");
					appendSvcParamRow(nosvc, "kmIdMin");
					appendSvcParamRow(nosvc, "kmIdMax");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/kms/kccopy":
					appendSvcParamRow(nosvc, "kmIdMin");
					appendSvcParamRow(nosvc, "kmIdMax");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/kms/kknew":
					appendSvcParamRow(nosvc, "keyId", "*", "2001");
					appendSvcParamRow(nosvc, "keyVal", "*");
					appendSvcParamRow(nosvc, "fmtKeys", "*", "0");
					appendSvcParamRow(nosvc, "kmIdMin", "*");
					appendSvcParamRow(nosvc, "kmIdMax", "*");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/kms/kknew4tags":
					appendSvcParamRow(nosvc, "keyId", "*", "2001");
					appendSvcParamRow(nosvc, "keyVal", "*");
					appendSvcParamRow(nosvc, "fmtKeys", "*", "0");
					appendSvcParamRow(nosvc, "tags", "*", "529舟山、530徐州");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/kms/kkupdatetags":
					appendSvcParamRow(nosvc, "tags", "*", "东拖830→海监137→海警2337");
					appendSvcParamRow(nosvc, "baseTag");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/kms/kkcopy":
					appendSvcParamRow(nosvc, "kmIdSrc", "*");
					appendSvcParamRow(nosvc, "kmIdDest", "*");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/kms/kkdcopy":
					appendSvcParamRow(nosvc, "kmIdMin");
					appendSvcParamRow(nosvc, "kmIdMax");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/kms/kutil":
					appendSvcParamRow(nosvc, "method", "*", "keyword");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/serverlog":
					appendSvcParamRow(nosvc, "logdate", "*", new Date().Format("yyyy-MM-dd"));
					break;
				case "/servlets/portal/locale":
					appendSvcParamRow(nosvc, "locale", "*", "zh_CN");
					break;
				case "/servlets/admin/user":
					$('#svc-ids').html("username,password,roles");
					$('#svc-user').css("display","");
					break;
				case "/servlets/admin/userdel":
					$('#svc-ids').html("delname");
					$('#svc-userdel').css("display","");
					break;
				case "/servlets/admin/shutdown":
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/funds/fundflush":
					$('#svc-ids').html("type");
					$('#svc-fundflush').css("display","");
					break;
				case "/servlets/admin/sky4sql":
					appendSvcParamRow(nosvc, "p1", "", "select code, name, name_s, manager, start_date from t_symbol where type=311");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/admin/sky4xml":
					appendSvcParamRow(nosvc, "p1", "*", "http://");
					appendSvcParamRow(nosvc, "p2", "", "client?");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/admin/sky4file":
					appendSvcParamRow(nosvc, "p1", "", "/WEB-INF/portal/error404.jsp");
					appendSvcParamRow(nosvc, "p2", "", "absolute?");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/admin/sky4release":
					$('#svc-release').css("display","");	        	
					break;
				case "/servlets/admin/encrypt":
					appendSvcParamRow(nosvc, "code", "*", "word");
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				case "/servlets/admin/time":
					appendSvcParamRow(nosvc, "time", "*", new Date().Format("yyyy-MM-dd hh:mm:ss.S"));
					appendSvcParamRow(true, "admin&nbsp;&nbsp;*", "");
					break;
				default:
			}	
			$("#svc-name").html(html);	
			$("#svc-name2").css("display","");
			$("#button-svc-submit").css("display",nosvc?"none":"");
			$("#svc-result").css("display","none");	
			onSvcDetailPageChanged("detail");
		});
	});	 
	
	$("#button-svc-submit").click(function() {
		if (readonly()) return;
		var pairs = {"format":"xml"};
		var ids = $('#svc-ids').html();
		var arr = ids.split(",");
		for (var i = 0; i < arr.length; i++) {
			var id = "param-" + arr[i], value = (id == 'param-roles' ? valMultiSel(id) : $("#" + id).val());
			var is_required = ($("#" + id).attr("class") == "required");
			if (is_required && isEmpty(value)) {
				alert(arr[i] + " " + msg("label_post_tip"));
				return;
			}
			pairs[arr[i]] = value;
		}
		var popup = false;
		var svc = $("#svc-name").html(), svc2 = svc;
		switch(svc) {
			case "/serverlog":
				var value = pairs.logdate;
				value = ((new Date().Format("yyyy-MM-dd")) == value ? "" : value.replace(/\-/g, ""));
				var file = "{serverlog}" + (value ? "_" + value + ".log" : "");
				pairs = {};
				pairs.type = "file";
				pairs.p1 = file;
				pairs.admin = account;
				popup = true;
				svc2 = "/servlets/admin/sky";
				break;			
			case "/servlets/admin/sky4sql":
				pairs.type = "sql";
				pairs.admin = account;
				popup = true;
				svc2 = "/servlets/admin/sky";
				break;			
			case "/servlets/admin/sky4xml":
				pairs.type = "xml";
				pairs.admin = account;
				popup = true;
				svc2 = "/servlets/admin/sky";
				break;
			case "/servlets/admin/sky4file":
				pairs.type = "file";
				pairs.admin = account;
				popup = true;
				svc2 = "/servlets/admin/sky";
				break;
			case "/servlets/admin/sky4release":
				$("#uploadify").uploadify('upload','*');
				return;
			case "/servlets/admin/user":
			case "/servlets/admin/userdel":
			case "/servlets/admin/shutdown":
			case "/servlets/funds/fundflush":
			case "/servlets/admin/encrypt":
			case "/servlets/admin/time":
				pairs.admin = account;
				break;
			default:
		}
		if (popup) {
			var url = "${context}" + svc2 + "?", u = "";
			for (var key in pairs) u += ("&" + key + "=" + pairs[key]);
			if (u) u = u.substring(1);
			window.open(url + u, 'window2');
			return;
		}
		if (!confirm(msgconfirm(svc2, ""))) return; 
		
		$("#svc-result").html(msg("label_posting")).css("display","");	
		$.post("${context}" + svc2, pairs, function(data) {
			switch(svc) {		
				case "/servlets/portal/locale":
					window.location = '?time='+Math.random();
					break;	
				default:
					$("#svc-result").html(msg("label_post_result") + '<br/><textarea cols="60" rows="10">' + xml2str(data) + '</textarea>');
			}
		},"xml");
	}); 	
	 
	$("#button-svc-cancel").click(function() {
		if ('/servlets/admin/sky4release' == $("#svc-name").html()) $("#uploadify").uploadify('cancel');
		$("#svc-name").html('');
		$("#svc-name2").css("display","none");
		$('#svc-release').css("display","none");
		$('#svc-user').css("display","none");
		$('#svc-userdel').css("display","none");
		$('#svc-fundflushl').css("display","none");
		onSvcDetailPageChanged("select");
	}); 
});

function parse(start, end, str) {
	return parseInt(str.substring(start, end), 10);
} 

var detid = "${detid}", account = '${result.account}';
var svc_input_row = '';
svc_input_row += '<div style="margin-top:6px;">';
svc_input_row += '	<div style="width:120px;float:left;">{id}:</div>';
svc_input_row += '	<div style="float:left;"><input id="param-{id}" value="{value}" class="{class}" style="width:270px;"/>&nbsp;&nbsp;{description}</div>';
svc_input_row += '	<div class="clear"></div>';
svc_input_row += '</div> ';
var svc_text_row = '<div style="width:480px;margin-top:6px;word-wrap:break-word;word-break:break-all;">{description}</div> ';

function appendSvcParamRow(nosvc, id, description, value) {
	if (nosvc) {
		$('#svc-params').append($(svc_text_row.replace(/\{description\}/gi, id)));
		$('#svc-params').append($(svc_text_row.replace(/\{description\}/gi, "&nbsp;")));
		$('#svc-params').append($(svc_text_row.replace(/\{description\}/gi, (description ? description : ''))));
	} else {
		description = (description ? description : '');
		var $row = $(svc_input_row.replace(/\{id\}/gi, id).replace(/\{value\}/gi, (value ? value : '')).replace(/\{class\}/gi, (description ? 'required' : '')).replace(/\{description\}/gi, description));
		$('#svc-params').append($row);	
		var html = $('#svc-ids').html();
		$('#svc-ids').html((html ? html + "," : "") + id);
	}
}

function onSvcListPageChanged(page) {
	var id = "svc-" + page;
	$(".vos-service").each(function(index, element) {
		$(this).css('display',(id==$(this).attr("id") ? '' : 'none'));
	});
}

function onSvcDetailPageChanged(page) {
	var id = "svc-" + page;
	$(".svc-detail").each(function(index, element) {
		$(this).css('display',(id==$(this).attr("id") ? '' : 'none'));
	});
}
</script>
  
<div class="main ui-widget-content">
    <div style="margin-left:0px;height:40px;margin-bottom:0px;margin-top:0px;background-color:#A3D4F2">
        <div class="thumb_button" style="background-color:#7bbfe6;"><fmt:message key="label_system" /></div>
        <div class="clear"></div>
    </div>
    <div id="systemInfo" class="media_info">
    	<div style="width:300px;height:100%;background-color:#ddeeff;float:left;">
            <div style="margin-top:20px;margin-left:40px;color:#859b52;width:200px;height:30px;line-height:30px;font-size:15px;font-weight:bold;"><fmt:message key="system_api" /></div>
            <div style="margin-top:20px;margin-left:60px;width:230px;">
                <ul>
                    <li class="list-menu" data-link="kms"><fmt:message key="system_api_kms" /></li>   
                    <li class="list-menu" data-link="debug"><fmt:message key="system_api_debug" /></li>           
                </ul>
            </div>	
        </div>
    	<div style="margin-top:10px;margin-left:60px;width:540px;height:540px;float:left;">
            <div style="margin-top:10px;color:#859b52;width:200px;height:30px;line-height:30px;font-size:15px;font-weight:bold;"><fmt:message key="system_info" /></div>
            <div style="margin-top:10px;margin-left:20px;width:510px;">
                <ul>
                    <c:if test="${login_user.admin}">
                    <li class="list-info"><fmt:message key="system_info_boot" />&nbsp;&nbsp;${result.hostStartup.value}</li>
                    </c:if>
                    <li class="list-info"><fmt:message key="system_info_host" />&nbsp;&nbsp;${result.hostName.value}</li>
                    <c:if test="${login_user.admin}">
                    <li class="list-info"><fmt:message key="system_info_build_jdk" />&nbsp;&nbsp;${result.build4Java.value}</li>
                    <li class="list-info"><fmt:message key="system_info_build_server" />&nbsp;&nbsp;<c:choose><c:when test="${empty result.build4Server.value}"><fmt:message key="label_none" /></c:when><c:otherwise>${result.build4Server.value}</c:otherwise></c:choose></li>
                    <li class="list-info"><fmt:message key="system_info_build_common" />&nbsp;&nbsp;${result.build4Common.value}</li>
                    <li class="list-info"><fmt:message key="system_info_build_savanna" />&nbsp;&nbsp;${result.build4Savanna.value}</li>
					</c:if>
                </ul>
            </div>	        
        </div>
        <div class="clear"></div>
    </div>
    <div id="systemService" class="media_info" style="display:none;">
    	<div style="width:420px;height:100%;background-color:#ddeeff;float:left;">
	    	<div style="margin-top:40px;margin-left:40px;">
	            <div id="svc-kms" class="vos-service" style="margin-top:20px;margin-left:20px;display:none;">
		            <div style="margin-top:10px;color:#859b52;width:320px;height:30px;line-height:30px;font-size:15px;font-weight:bold;"><fmt:message key="system_api_kms" /><fmt:message key="system_api_list" /></div>
		            <div style="margin-top:10px;margin-left:20px;width:320px;">
		                <ul>
		                    <li class="list-link">/servlets/kms/knew</li>
		                    <li class="list-link">/servlets/kms/kfmt</li>
		                    <li class="list-link">/servlets/kms/kcfmt</li>
		                    <li class="list-link">/servlets/kms/kdfmt</li>
		                    <li class="list-link">/servlets/kms/kccopy</li>
		                    <li class="list-link">/servlets/kms/kknew</li>
		                    <li class="list-link">/servlets/kms/kknew4tags</li>
		                    <li class="list-link">/servlets/kms/kkupdatetags</li>
		                    <li class="list-link">/servlets/kms/kkcopy</li>
		                    <li class="list-link">/servlets/kms/kkdcopy</li>
		                    <li class="list-link">/servlets/kms/kutil</li>
		                </ul>
		            </div>
	            </div>
	            <div id="svc-debug" class="vos-service" style="margin-top:20px;margin-left:20px;display:none;">
		            <div style="margin-top:10px;color:#859b52;width:320px;height:30px;line-height:30px;font-size:15px;font-weight:bold;"><fmt:message key="system_api_debug" /><fmt:message key="system_api_list" /></div>
		            <div style="margin-top:10px;margin-left:20px;width:320px;">
		                <ul>
		                    <li class="list-link">/serverlog</li>
		                    <li class="list-link">/servlets/portal/locale</li>
		                    <li class="list-link">/servlets/admin/user</li>
		                    <li class="list-link">/servlets/admin/userdel</li>
		                    <li class="list-link">/servlets/admin/shutdown</li>
		                    <li class="list-link">/servlets/funds/fundflush</li>
		                    <li class="list-link">/servlets/admin/sky4sql</li>
		                    <li class="list-link">/servlets/admin/sky4xml</li>
		                    <li class="list-link">/servlets/admin/sky4file</li>
		                    <li class="list-link">/servlets/admin/sky4release</li>
		                    <li class="list-link">/servlets/admin/encrypt</li>
		                    <li class="list-link">/servlets/admin/time</li>
		                </ul>
		            </div>
	            </div>
	            <div style="margin-top:40px;margin-left:20px;height:40px;">
		            <div class="button-return button" style="margin-left:0px;float:left;"><i class="icon-reply icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_return" /></div>
		            <div class="clear"></div>
	            </div>	        
	        </div>
        </div>
    	<div style="margin-top:20px;margin-left:20px;width:540px;height:100%;float:left;">
            <div style="margin-top:20px;margin-left:20px;color:#859b52;width:100%;height:30px;line-height:30px;font-size:15px;font-weight:bold;"><fmt:message key="system_api_info" /> <span id="svc-name2"><fmt:message key="system_square_left" /><span id="svc-name"></span><fmt:message key="system_square_right" /></span></div>
            <div id="svc-detail" class="svc-detail" style="margin-top:10px;margin-left:20px;display:none;">
            	<div style="margin-top:10px;"><fmt:message key="system_api_param" /></div>
		    	<div id="svc-params" style="margin-top:10px;"></div>
		    	<div id="svc-release" style="margin-top:20px;margin-left:20px;display:none;"><input type="file" id="uploadify" /></div>
		    	<div id="svc-user" style="margin-top:20px;margin-left:20px;display:none;">
					<div style="margin-top:6px;">
						<div style="width:120px;float:left;">username:</div>
						<div style="float:left;"><input id="param-username" class="required" style="width:270px;"/>&nbsp;&nbsp;*</div>
						<div class="clear"></div>
					</div>
					<div style="margin-top:6px;">
						<div style="width:120px;float:left;">password:</div>
						<div style="float:left;"><input id="param-password" class="required" style="width:270px;"/>&nbsp;&nbsp;*</div>
						<div class="clear"></div>
					</div>
					<div style="margin-top:6px;">
						<div style="width:120px;float:left;">roles:</div>
						<div style="float:left;"><select id="param-roles" class="required" multiple="multiple" size="6" style="width:200px;">
							<option value="admin">Administrator</option>
							<option value="readonly">ReadOnly</option>
							<option value="guest">Guest</option>
						</select>&nbsp;&nbsp;*</div>
						<div class="clear"></div>
					</div>
					<div style="width:480px;margin-top:6px;word-wrap:break-word;word-break:break-all;">admin&nbsp;&nbsp;*</div>
		    	</div>		    	
		    	<div id="svc-userdel" style="margin-top:20px;margin-left:20px;display:none;">
					<div style="margin-top:6px;">
						<div style="width:120px;float:left;">username:</div>
						<div style="float:left;"><select id="param-delname" class="required" style="width:200px;">
		    				<option value=""><fmt:message key="label_select" /></option>
		    				<c:forEach items="${result.users}" var="item">
		    				<option value="${item}">${item}</option></c:forEach>
						</select>&nbsp;&nbsp;*</div>
						<div class="clear"></div>
					</div>
					<div style="width:480px;margin-top:6px;word-wrap:break-word;word-break:break-all;">admin&nbsp;&nbsp;*</div>
		    	</div>
		    	<div id="svc-fundflush" style="margin-top:20px;margin-left:20px;display:none;">
					<div style="margin-top:6px;">
						<div style="width:120px;float:left;">flush type:</div>
						<div style="float:left;"><select id="param-type" class="required" style="width:200px;">
		    				<option value=""><fmt:message key="label_select" /></option>
							<option value="daily">Daily</option>
							<option value="quotes_daily">Quotes Daily</option>
							<option value="nets_daily">Nets Daily</option>
							<option value="symbols">Symbols</option>
							<option value="all">All</option>
						</select>&nbsp;&nbsp;*</div>
						<div class="clear"></div>
					</div>
					<div style="width:480px;margin-top:6px;word-wrap:break-word;word-break:break-all;">admin&nbsp;&nbsp;*</div>
		    	</div>		    	
		        <div style="margin-top:40px;height:40px;">
		            <div id="button-svc-submit" class="button" style="margin-left:0px;float:left;margin-right:40px;"><i class="icon-ok icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_submit" /></div>
		            <div id="button-svc-cancel" class="button" style="margin-left:0px;float:left;"><i class="icon-reply icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_cancel" /></div>
		            <div class="clear"></div>
		        </div>	
		    	<div id="svc-result" style="margin-top:30px;"></div>
		    	<div id="svc-ids" style="display:none;"></div>	    	 	            
            </div>	 
            <div id="svc-select" class="svc-detail" style="margin-top:10px;margin-left:20px;">
		    	<div style="margin-top:10px;"><fmt:message key="system_api_tip" /></div>
            </div>	 
            <div id="svc-access" class="svc-detail" style="margin-top:10px;margin-left:20px;display:none;">
		    	<div style="margin-top:10px;"><fmt:message key="system_api_access" /></div>
            </div>	       
        </div>
        <div class="clear"></div>
    </div>
</div>
<div class="clear"></div>
</tags:template>
