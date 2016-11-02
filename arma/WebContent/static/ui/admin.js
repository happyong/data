// base javascript for admin 
// global:
//   curpage, curdetid, portals, rouser
//   detail(), list()
$(function() {
	var height= $(window).height();
	height=height<800?800:height;
	$("#wrapper").height(height);
	
	$("#mediaListTable .media_row").each(function(index, element) {
		$(this).addClass(index%2 == 0 ? "row_odd" : "row_even").hover(function() {
			$(this).find(".media_button").show();
		}, function() {
			$(this).find(".media_button").hide();
		});
	}); 
	
	$(".grayTip").each(function() {
		var objTextBox=$(this);
		var oldText=$.trim(objTextBox.val());
		objTextBox.css("color","#b8b8b8"); 
		objTextBox.focus(function() {
		if(objTextBox.val() == oldText) objTextBox.val("");
			objTextBox.css("color","#000");
		});
		objTextBox.blur(function() {
			if(objTextBox.val()=="") objTextBox.val(oldText).css("color","#b8b8b8");
		});
	});	
	
	$("#button-refresh").click(function() {
		if (curpage=='mediaDetail')	{
			detail(curdetid);
		} else if (curpage=='mediaList') {
			list();
		}
	});  
});

function onMediaDivChanged(page, detid) {
	curpage = page;
	curdetid = (detid ? detid : "");
	$("#button-refresh").css('display',(((curpage=='mediaDetail' && detid) || curpage=='mediaList') ? '' : 'none'));
	$(".media_info").each(function(index, element) {
		$(this).css('display',(curpage==$(this).attr("id") ? '' : 'none'));
	});
}

var types = [];
function menus(str) {
	if (!str || str.length < 1) return;
	types = str.split(",");
}
function show(type) {
	if (!type || !portals || portals.length < 1) return true;
    var i = 0;
	for (; i < types.length; i++) if (type == types[i]) break;
	if (i >= types.length || i >= portals.length) return true;
	return (portals.substr(i, 1) == '1');
}

function loguploaded(type, context, obj) {
	$("#log-list").empty();
	$("#log-uploading").html('').css('display','none');
	$("#log-form").css('display','');
	onMediaDivChanged("mediaLog");
	
	var name = $("#log-name").html();	
	$.post(context + "/servlets/media-admin/uploaded", {"mediaType":type,"name":name,"uploadType":"log","format":"json"}, function(data) {
		if (data.result && data.uploaded) {
			var relative = data.relative;
			var arr = data.uploaded.split(",");
			for (var i = 0; i < arr.length; i += 2) {
				var url = relative + arr[i];
				var $log0 = $(log_row.replace(/\{url\}/gi, url).replace(/\{log\}/gi, arr[i]).replace(/\{size\}/gi, arr[i + 1]));
				$('#log-list').append($log0);			
			}
			$("#logListTable .log_row").each(function(index, element) {
				$(this).addClass(index%2 == 0 ? "row_odd" : "row_even");
			});			
			
			$(".logdownlod").each(function(index, element) {    
				$(this).css("color","#555555").hover(function() {
					$(this).css("color","#cc6600");
				}, function() {
					$(this).css("color","#555555");
				}).click(function() {
					var url = context + '/servlets/admin/sky?type=file&p1=' + $(this).parents("tr:first").find("td:first").html() + '&download=true&admin=' + account;
					window.location = url;
				});
			});
		} 
		$("#logListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
	},"json");
}

var log_row = '<tr class="log_row">';
log_row += '    <td style="display:none">{url}</td>';
log_row += '    <td width="360" align="left" valign="middle">&nbsp;&nbsp;&nbsp;&nbsp;<a href="javascript:void(0)" class="logdownlod"><i class="icon-ok icon-large"></i>&nbsp;&nbsp;{log}</a></td>';
log_row += '    <td align="center" valign="middle">{size}</td>';
log_row += '</tr>';	

function logupload(type, context, obj) {
	if (readonly()) return;
	var date = $("#log-date").val();
	if (isEmpty(date)) {
		alert(msg("label_upload_date_tip"));
		return;
	}
	var name = $("#log-name").html();	
	if (!confirm(msgconfirm(name, "label_upload"))) return; 
				
	clearInterval(uploadInterval);
	$("#log-form").css('display','none');
	var uploadType = $("#log-type").val();
	if (!uploadType) uploadType = "log";
	$.post(context + "/servlets/media-admin/upload", {"mediaType":type,"name":name,"uploadType":uploadType,"date":date,"format":"json"}, function(data) {
		if (data.result && data.uploadId) {
			var uploadId = data.uploadId;
			var html = msg("label_uploading") + "<br/>date (" + date + "), uploadType (" + uploadType + "), uploadId (" + uploadId + ")";
			$("#log-uploading").html(html).css('display','');
			$("#log-id").html(uploadId);
			uploadInterval = setInterval(function(){
				$.post(context + "/servlets/media-admin/uploaddetail", {"uploadId":uploadId,"format":"json"}, function(data) {
		    		var status = data.status;
					if (data.result && status) {
						if (status == "done" || status != "doing") {
							clearInterval(uploadInterval);
							$("#log-form").css('display','');
							var str = (status == "done" ? msg("label_uploaded") : msg("label_uploadfail") + "\n" + msg("fail_reason") + status);
							$("#log-uploading").html(str).css('display','none');
							if (status == "done") loguploaded(type, context, obj);
							alert(str);
						} 
					} else {
						clearInterval(uploadInterval);
						$("#log-form").css('display','');
						var error = msg("label_uploadfail");
						if (data.error) error += "\n" + msg("fail_reason") + data.error;
						alert(error);
					}
				},"json");			
			},10000);
		} else {
			$("#log-form").css('display','');
			var error = msg("label_uploadfail");
			if (data.error) error += "\n" + msg("fail_reason") + data.error;
			alert(error);
		}
	},"json");
}

function configupload(type, context, obj) {
	if (readonly()) return;
	var name = $("#config-name").html();	
	if (!confirm(msgconfirm(name, "label_upload"))) return; 
				
	clearInterval(uploadInterval);
	$("#config-form").css('display','none');
	var uploadType = "config";
	$.post(context + "/servlets/media-admin/upload", {"mediaType":type,"name":name,"uploadType":uploadType,"format":"json"}, function(data) {
		if (data.result && data.uploadId) {
			var uploadId = data.uploadId;
			var html = msg("label_uploading") + "<br/>uploadType (" + uploadType + "), uploadId (" + uploadId + ")";
			$("#config-getting").html(html).css('display','');
			$("#config-id").html(uploadId);
			uploadInterval = setInterval(function(){
				$.post(context + "/servlets/media-admin/uploaddetail", {"uploadId":uploadId,"format":"json"}, function(data) {
		    		var status = data.status;
					if (data.result && status) {
						if (status == "done" || status != "doing") {
							clearInterval(uploadInterval);
							$("#config-form").css('display','');
							var str = (status == "done" ? msg("label_uploaded") : msg("label_uploadfail") + "\n" + msg("fail_reason") + status);
							$("#config-getting").html(str).css('display','none');
							if (status == "done") {
								var url = context + "/servlets/media-admin/configed?name=" + name + "&mediaType=" + type;
								$.get(url,{},function(data){
									$("#config-content").val(data);
								},"html");
								// $("#config-content").load(url);
							}
							alert(str);
						} 
					} else {
						clearInterval(uploadInterval);
						$("#config-form").css('display','');
						var error = msg("label_uploadfail");
						if (data.error) error += "\n" + msg("fail_reason") + data.error;
						alert(error);
					}
				},"json");			
			},10000);
		} else {
			$("#config-form").css('display','');
			var error = msg("label_uploadfail");
			if (data.error) error += "\n" + msg("fail_reason") + data.error;
			alert(error);
		}
	},"json");
}

function config(type, context, obj) {
	if (readonly()) return;
	var content = $("#config-content").val();
	if (isEmpty(content) && type != 'StreamDemuxer') {
		alert(msg("config_tip"));
		return;
	}
	var name = $("#config-name").html();	
	if (!confirm(msgconfirm(name, "config_label"))) return; 			
	
	content = encodeURIComponent(content);
	$.ajax({
		type:"post",
		url:context + "/servlets/media-admin/config?mediaType="+type+"&names="+name+"&format=json",
		processData:false,
		data:"adminConfig="+content,
		dataType:"json",
		processData:false,
		success:function(data) {
			if (data.result && data.apply_name == name)
				list();
			else {
				$("#config-content").focus();
				var error = msg("config_fail");
				if (data.error) error += "\n" + msg("fail_reason") + data.error;
				alert(error);
			}			
		},
		error:function(data) {
			alert(msg("config_error"));
		}
	});
}	

function startup(type, context, obj) {
	if (readonly()) return;
	var name = $(obj).parents("tr:first").find("td:first").html();	
	if (!confirm(msgconfirm(name, "startup_label"))) return; 		
	$.post(context + "/servlets/media-admin/startup", {"mediaType":type,"names":name,"format":"json"}, function(data) {
		if (data.result && data.apply_name == name)
			list();
		else {
			var error = msg("startup_label") + msg("fail");
			if (data.error) error += "\n" + msg("fail_reason") + data.error;
			alert(error);
		}
	},"json");
}

function shutdown(type, context, obj) {
	if (readonly()) return;
	var name = $(obj).parents("tr:first").find("td:first").html();	
	if (!confirm(msgconfirm(name, "shutdown_label"))) return; 				
	$.post(context + "/servlets/media-admin/shutdown", {"mediaType":type,"names":name,"format":"json"}, function(data) {
		if (data.result && data.apply_name == name)
			list();
		else {
			var error = msg("shutdown_label") + msg("fail");
			if (data.error) error += "\n" + msg("fail_reason") + data.error;
			alert(error);
		}
	},"json");
}

function lock(type, context, obj) {
	if (readonly()) return;
	var name = $(obj).parents("tr:first").find("td:first").html();	
	if (!confirm(msgconfirm(name, "lock_label"))) return; 				
	$.post(context + "/servlets/media-admin/lock", {"mediaType":type,"names":name,"format":"json"}, function(data) {
		if (data.result && data.apply_name == name)
			list();
		else {
			var error = msg("lock_label") + msg("fail");
			if (data.error) error += "\n" + msg("fail_reason") + data.error;
			alert(error);
		}
	},"json");
}

function unlock(type, context, obj) {
	if (readonly()) return;
	var name = $(obj).parents("tr:first").find("td:first").html();	
	if (!confirm(msgconfirm(name, "unlock_label"))) return; 				
	$.post(context + "/servlets/media-admin/unlock", {"mediaType":type,"names":name,"format":"json"}, function(data) {
		if (data.result && data.apply_name == name)
			list();
		else {
			var error = msg("unlock_label") + msg("fail");
			if (data.error) error += "\n" + msg("fail_reason") + data.error;
			alert(error);
		}
	},"json");
}

function upgrade(type, context, obj) {
	if (readonly()) return;
	var build = $("#upgrade-build").val();
	if (isEmpty(build)) {
		alert(msg("upgrade_tip"));
		return;
	}
	var oldBuild = $("#upgrade-build2").html();
	if (oldBuild == build) {
		alert(msg("upgrade_diff"));
		return;
	}
	var name = $("#upgrade-name").html();
	if (!confirm(msgconfirm(name, "upgrade_label"))) return; 				
	
	$.post(context + "/servlets/media-admin/upgrade", {"mediaType":type,"names":name,"build":build,"format":"json"}, function(data) {
		if (data.result && data.apply_name == name)
			list();
		else {
			$("#upgrade-build").focus();
			var error = msg("upgrade_fail");
			if (data.error) error += "\n" + msg("fail_reason") + data.error;
			alert(error);
		}
	},"json");
}

function remove(type, context, obj) {
	if (readonly()) return;
	var name = $(obj).parents("tr:first").find("td:first").html();	
	if (!confirm(msgconfirm(name, "remove_label"))) return; 			
	$.post(context + "/servlets/media-admin/remove", {"mediaType":type,"names":name,"format":"json"}, function(data) {
		if (data.result && data.apply_name == name)
			list();
		else {
			var error = msg("remove_label") + msg("fail");
			if (data.error) error += "\n" + msg("fail_reason") + data.error;
			alert(error);
		}
	},"json");
}

function reset(type, context, obj) {
	if (readonly()) return;
	var name = $(obj).parents("tr:first").find("td:first").html();	
	if (!confirm(msgconfirm(name, "reset_label"))) return; 			
	$.post(context + "/servlets/media-admin/reset", {"mediaType":type,"names":name,"format":"json"}, function(data) {
		if (data.result && data.apply_name == name)
			list();
		else {
			var error = msg("reset_label") + msg("fail");
			if (data.error) error += "\n" + msg("fail_reason") + data.error;
			alert(error);
		}
	},"json");
}

function cancel(type, context, obj) {
	if (readonly()) return;
	var name = $(obj).parents("tr:first").find("td:first").html();	
	if (!confirm(msgconfirm(name, "cancel_label"))) return; 			
	$.post(context + "/servlets/media-admin/cancel", {"mediaType":type,"names":name,"format":"json"}, function(data) {
		if (data.result)
			list();
		else {
			var error = msg("cancel_label") + msg("fail");
			alert(error);
		}
	},"json");
}

function fill(name, pf, tip, pairs) {
	var val = $("#" + pf + name).val();
	if (isEmpty(val)) {
		alert(msg(tip + name));
		return false;
	}
	if (pairs) pairs[name] = val;
	return true;
}

function fill2(name, pf, tip, pairs) {
	var val = $("#" + pf + name).val();
	if (isEmpty(val)) {
		alert(msg(tip + name + "_tip"));
		return false;
	}
	if (pairs) pairs[name] = val;
	return true;
}

function vip(pf, pairs) {
	var ip = $("#" + pf + "-ip").val();
	if (!isValidIp(ip)) {
		alert(msg(pf + "_ip_invalid"));
		return false;
	}
	if (pairs) pairs.ip = ip;
	return true;
}

function readonly() {
	if (rouser) {
		alert(msg("user_read_only"));
		return true;
	}
	return false;
}

function unull(val) {
	return (val ? val : "");
}

function unull2(val) {
	return (val ? val : msg("label_none"));
}

function msg(id) {
	return $("#msg_" + id).html();
}

function msgconfirm(name, label_id) {
	var html = $("#msg_confirm_tip").html();
	var label = $("#msg_" + label_id).html();
	var html2 = (html.replace(/\{name\}/gi, name).replace(/\{action\}/gi, (label ? label : "")));
	return html2;
}

function str2xml(str) {
	if($.browser.msie) {  
		xml = new ActiveXObject("Microsoft.XMLDOM");  
		xml.async = false;  
		xml.loadXML(str); 
	} else
		xml = new DOMParser().parseFromString(str, "text/xml");  
	return xml;
}

function xml2str(xml) {
	return $.browser.msie ? xml.xml : (new XMLSerializer()).serializeToString(xml); 
}

// new Date().Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423   
// new Date().Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18  
Date.prototype.Format = function(fmt) {  
	var o = {
		"M+" : this.getMonth()+1,  
		"d+" : this.getDate(), 
		"h+" : this.getHours(),  
		"m+" : this.getMinutes(),  
		"s+" : this.getSeconds(), 
		"q+" : Math.floor((this.getMonth()+3)/3),
		"S"  : this.getMilliseconds()  
	};   
	if(/(y+)/.test(fmt)) 
		fmt=fmt.replace(RegExp.$1, (this.getFullYear()+"").substr(4 - RegExp.$1.length));   
	for(var k in o)
		if(new RegExp("("+ k +")").test(fmt))
			fmt = fmt.replace(RegExp.$1, (RegExp.$1.length==1) ? (o[k]) : (("00"+ o[k]).substr((""+ o[k]).length)));   
	return fmt;   
}

function valMultiSel(id) {
	if (!id) return "";
	var ret = "";
	$("#" + id + " option:selected").each(function() {
		ret += "," + $(this).val();
	});
	return ret ? ret.substring(1) : ret;
}

function isValidIp(ip) {
	if (isEmpty(ip)) return false;
	var exp = /^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
	return exp.test(ip) && (RegExp.$1 < 256 && RegExp.$2 < 256 && RegExp.$3 < 256 && RegExp.$4 < 256);
}

function isValidPort(port) {
	var exp = /^[1-9][0-9]{1,4}$/;
	return exp.test(port);
}

function isEmpty(val) {
	return (!val || $.trim(val).length == 0);
}	
