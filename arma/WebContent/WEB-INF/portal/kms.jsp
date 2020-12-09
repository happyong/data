<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java"%>
<%@ include file="common.jsp"%>
<tags:template className="mediaPage">
<script type="text/javascript" src="${static}/static/ui/jquery.idTabs.min.js" charset="utf-8"></script>
<div style="display:none">${result.jskms}</div>
<script charset="utf-8">
var curdetid = "", curpage = "mediaList", portals='${result.portals}', rouser = Boolean(${login_user.readonly});
$(function() {  	 
	$(".button-return").click(function() {
		var hash = top.location.hash;
		if (hash && hash != "#kms") 
			window.location = "${context}/servlets/portal/" + hash.substring(1) + "?detid=" + dettop + "&time=" + Math.random();
		else
			onMediaDivChanged("mediaList");
	});
	menus('${result.menus}');
	kmsinit(eval('(' + '${result.kmsmeta}' + ')'));
	
	$("#search-ckeyId").change(function() {
		upsckoptions();
		upscdivs();
	});
	$("#search-scks").change(function() {
		upscdivs();
	});
	$("#button-cond-add").click(function() {
		if (sckcount > 5) {
			alert(msg("sc_keys_tip"));
			return;
		}	
		var sckId = $("#search-scks").val(), sckVal = "", key = scanKey(sckId);
		if (key && dkey(key.typeId)) {
			var sckVal = $("#search-starttime").val(), endTime = $("#search-endtime").val(), now = new Date().Format("yyyy.MM.dd");
			if (!sckVal || !endTime || endTime < sckVal) {
				alert(msg("search_duration_tip"));
				return;
			}
			sckVal += sepv + (endTime < now ? "" : endTime);
		} else {
			sckVal = $("#search-txt").val();
			if (isEmpty(sckVal)) {
				alert(msg("sc_txt_tip"));
				return;
			}
		}
		fillsck(sckId, sckVal);
	});	
	$("#button-search").click(function() {
		var ckeyId = $("#search-ckeyId").val(), sortby = $("#search-sortby").val(), desc = chk("search-desc"), conds = valscks();
		if (allk(ckeyId)) {
			$('#tbody').html("");
			$('#search-count').html("0");
			$("#mediaListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
			
			// list the keywords, include category type
			var keys = scanKeys(sortby, desc, conds), count = 0;
			for (var i = 0; i < keys.length; i++) {
				var type = scanType(keys[i].typeId), sks = "";
				if (allk(keys[i].keyId) || kkey(parseInt(keys[i].typeId))) continue;
				count++;
				$.each(unull(keys[i].content).split(sepv), function(idx, item) {
					var key = scanKey(item);
					if (key) sks += ", " + scanName(key);
				});
				if (sks) sks = sks.substring(2);
				var $listkeyrow = $(list_key_row.replace(/\{keyId\}/gi, keys[i].keyId).replace(/\{names\}/gi, scanName(keys[i])).replace(/\{type\}/gi, scanName(type)).replace(/\{enum0\}/gi, keys[i].asEnum).replace(/\{sks\}/gi, sks));
				$('#tbody').append($listkeyrow);				
			}
			$('#search-count').html(count);
	   
			$("#mediaListTable .media_row").each(function(index, element) {
				$(this).addClass(index%2 == 0 ? "row_odd" : "row_even").hover(function() {
					$(this).find(".media_button").show();
				}, function() {
					$(this).find(".media_button").hide();
				});
			}); 

			$(".button-detail-key").click(function() {	
				var keyId = $(this).parents("tr:first").find("td:first").html(), key = scanKey(keyId);
				if (!ckey(key.typeId) && !confirm(msg("label_key_detail_confirm") + keyId + "?")) return;
				fillNew2(key);
			});

			$(".button-remove-key").click(function() {
				if (readonly()) return;
				var keyId = $(this).parents("tr:first").find("td:first").html();
				if (!confirm(msg("label_key_del_confirm")+keyId+"?")) return;
				
				var pairs = {"keyId":keyId,"format":"json"};
				$.post('${context}/servlets/kms/keydelete', pairs, function(data) {
					if (data.result) {
						kmsinit(data);
						list();
					} else {
						var error = msg("label_key_del_fail");
						if (data.error) error += "\n" + msg("fail_reason") + data.error;
						alert(error);
					}
				},"json");
			});
			$("#mediaListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
		} else {
			if (searching) {
				alert(msg("search_time_tip"));
				return;
			}
			search(true);
			searchTimer = setTimeout("search(false)",1000);
			$('#tbody').html("");
			$('#search-count').html("0");
			$("#mediaListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);

			// list the kms with max keywords per km
			var ckeyId = $("#search-ckeyId").val(), sortby = $("#search-sortby").val(), desc = chk("search-desc"), newwin = chk("search-win"), conds = valscks();
			if (newwin > 0) {
				var url = "${context}/servlets/kms/kmlist2?ckeyId="+ckeyId+"&sortby="+sortby+"&desc="+desc+"&conds="+encodeURIComponent(conds);
				window.open(url, 'window2');
				return;
			}
			$.ajax({
				type:"post",
				url:"${context}/servlets/kms/kmlist?ckeyId="+ckeyId+"&sortby="+sortby+"&desc="+desc+"&format=json",
				processData:false,
				data:"conds="+encodeURIComponent(conds),
				dataType:"json",
				processData:false,
				success:function(data) {
					if (data.result) {
						var kms = (data.kms.km ? data.kms.km : []);
						$('#search-count').html(kms.length);
						var ckey = scanKey(data.kms.ckeyId);
						if (ckey) {
							// max keywords per km
							var arr_list = [], max = 6, mw = max * 100, tw = 0, skey = scanKey(sortby);
							if (ckey.content) {
								// scan the all keywords for this category
								var arr_ck = (ckey.content).split(sepv), klist = {};
								for (var i = 0; i < kms.length; i++) {
									// store the keyword td val, <br /> for 1:N type
									kms[i].kset = {};
									if (!kms[i].keys) continue;
									var arr = (kms[i].keys).split(";;"); 
									$.each(arr, function(idx, item) {
										var arr2 = item.split("=", 2), key = scanKey(arr2[0]), tdval = arr2[1];
										if (!key) return;
										klist[arr2[0]] = "scan";
										if (mkey(key.typeId)) {
											tdval = "";
											$.each(arr2[1].split(sepke), function(idx2, item2) {
												tdval += (idx2 == 0 ? "" : "<br/>") + item2;
											});
										}
										kms[i].kset[arr2[0]] = tdval;
										if (tagk(arr2[0])) kms[i].kset[arr2[0] + ".tdw"] = 140;
									});
								}
								// only show max fields, and sort by the km.content
								var cur = 0;
								if (skey && !updk(skey.keyId)) {
									cur++;
									arr_list.push(skey);
								}
								$.each(arr_ck, function(idx, item) {
									if (item == sortby) return;
									var key = scanKey(item);
									if (!klist[item] || !key) return;
									cur++;
									if (cur > max) return false;
									arr_list.push(key);
									if (tagk(key.keyId)) tw = 1;
								});
							}
							// scan the keyword list per km
							var klen = arr_list.length, klen0 = (tw == 1 ? klen - 1 : klen), kmtdw = parseInt(klen0 < 1 ? mw : (tw == 1 ? mw - 140 : mw) / klen0);
							for (var i = 0; i < kms.length; i++) {
								var str = "";
								if (klen == 0) str = kmtd(kmtdw, msg("label_keys"), unull2(null));
								else {
									$.each(arr_list, function(idx, item) {
										var tdval = unull2(kms[i].kset[item.keyId + ""]), tdw = kms[i].kset[item.keyId + ".tdw"];
										str += kmtd((tdw ? tdw : kmtdw), scanName(item), tdval);
									});
								}
								var $listkmrow = $(list_km_row.replace(/\{kmId\}/gi, kms[i].kmId).replace(/\{ckey\}/gi, scanName(ckey)).replace(/\{content\}/gi, unull2(kms[i].content)).replace(/\{updateDate\}/gi, kms[i].updateDate).replace(/\{keys\}/gi, str));
								$('#tbody').append($listkmrow);				
							}
						}
						   
						$("#mediaListTable .media_row").each(function(index, element) {
							$(this).addClass(index%2 == 0 ? "row_odd" : "row_even").hover(function() {
								$(this).find(".media_button").show();
							}, function() {
								$(this).find(".media_button").hide();
							});
						}); 

						$(".button-detail-km").click(function() {	
							var kmId = $(this).parents("tr:first").find("td:first").html();
							if (!confirm(msg("label_km_detail_confirm") + kmId + "?")) return;
							var pairs = {"kmId":kmId,"format":"json"};
							$.post('${context}/servlets/kms/kmdetail', pairs, function(data) {
								if (data.result) fillNew1(data.km);
								else alert(msg("label_km_detail_fail"));
							},"json");
						});

						$(".button-remove-km").click(function() {
							if (readonly()) return;
							var kmId = $(this).parents("tr:first").find("td:first").html();
							if (!confirm(msg("label_km_del_confirm")+kmId+"?")) return;
							
							var pairs = {"kmId":kmId,"format":"json"};
							$.post('${context}/servlets/kms/kmdelete', pairs, function(data) {
								if (data.result) list();
								else {
									var error = msg("label_km_del_fail");
									if (data.error) error += "\n" + msg("fail_reason") + data.error;
									alert(error);
								}
							},"json");
						});
					} else {
						search(false);
						var error = msg("search_fail");
						if (data.error && data.error.value) error += "\n" + msg("fail_reason") + data.error.value;
						alert(error);
					}
					$("#mediaListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);		
				},
				error:function(data) {
					alert(msg("search_error"));
				}
			});
		}
		
		if (detid) {
			detail(detlink, detid);
			detid = "";
		} else
			onMediaDivChanged("mediaList");
	});
	
	$("#button-new1").click(function() {
		fillNew1();
	}); 
	$("#new1-ckeyId").change(function() {
		upkkoptions();
	});
	$("#button-key-add").click(function() {
		var keyId = $("#new1-keys").val(), keyVal = $("#new1-keyVal").val();
		if (isEmpty(keyId) || isEmpty(keyVal)) {
			 alert(msg("new1_key_tip"));
			return;
		}
		fillkey(keyId, keyVal, true);
	});	
	$("#button-new1-ok").click(function() {
		if (readonly()) return;
		var ckeyId = $("#new1-ckeyId").val();
		if (isEmpty(ckeyId)) {
			 alert(msg("new1_ckeyId_tip"));
			return;
		}
		var kmId = unull($("#new1-id0").html()), mk = (kmId ? "update1" : "new1");
		if (!confirm(msg(mk + "_confirm") + (kmId ? " " + kmId : "") + "?")) return; 

		var keys = encodeURIComponent(valkeys());
		var content = encodeURIComponent($("#new1-content").val());
		$.ajax({
			type:"post",
			url:"${context}/servlets/kms/kmupdate?kmId="+kmId+"&ckeyId="+ckeyId+"&format=json",
			processData:false,
			data:"keys="+keys+"&content="+content,
			dataType:"json",
			processData:false,
			success:function(data) {
				if (data.result)
					list();
				else {
					var error = msg(mk + "_fail");
					if (data.error) error += "\n" + msg("fail_reason") + data.error;
					alert(error);
				}			
			},
			error:function(data) {
				alert(msg(mk + "_error"));
			}
		});
	});
	
	$("#button-new2").click(function() {
		fillNew2();
	}); 
	$("#new2-type").change(function() {
		var ck = ckey($("#new2-type").val());
		show("new2-div-content1", ck ? -1 : 1);
		show("new2-div-content2", ck ? 1 : -1);
	}); 	
	$("#button-sk-add").click(function() {
		var skId = $("#new2-keys").val();
		fillsk(skId);
	});	
	$("#button-new2-ok").click(function() {
		if (readonly()) return;
		var pairs = {}, pf = "new2-", tip = "new2_";
		if (!fill2("type", pf, tip, pairs) || !fill2("nameCn", pf, tip, pairs)) return;
		var keyId = unull($("#new2-id0").html()), mk = (keyId ? "update2" : "new2");
		if (!confirm(msg(mk + "_confirm") + (keyId ? " " + keyId : "") + "?")) return; 
		var asEnum = chk("new2-asEnum"), nameEn = $("#new2-nameEn").val();
		var content = (ckey(pairs.type) ? valsks() : encodeURIComponent($("#new2-content").val()));
		$.ajax({
			type:"post",
			url:"${context}/servlets/kms/keyupdate?keyId="+keyId+"&type="+pairs.type+"&asEnum="+asEnum+"&nameEn="+nameEn+"&format=json",
			processData:false,
			data:"nameCn="+pairs.nameCn+"&content="+content,
			dataType:"json",
			processData:false,
			success:function(data) {
				if (data.result) {
					kmsinit(data);
					list();
				} else {
					var error = msg(mk + "_fail");
					if (data.error) error += "\n" + msg("fail_reason") + data.error;
					alert(error);
				}			
			},
			error:function(data) {
				alert(msg(mk + "_error"));
			}
		});
	});
	
	$("#mediaListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
	$("#keyListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
	$("#kmTabs ul").idTabs();

	var end = new Date();
	end.setDate(end.getDate() + 1);
	$("#search-endtime").datepicker({"dateFormat": "yy-mm-dd"}).val(end.Format("yyyy.MM.dd"));
	end.setDate(end.getDate() - 95);
	$("#search-starttime").datepicker({"dateFormat": "yy-mm-dd"}).val(end.Format("yyyy.MM.dd"));
	list();
}); 

var detid = "${detid}", dettop = "${dettop}", detlink = ("${detlink}" != "false");
function list() {
	$("#button-search").trigger("click");
}
function detail(isHref, client) {	
}

var sck_row = '';
sck_row += '<div style="margin-top:5px;margin-left:10px;width:210px;float:left;" id="scks_{index}" >';
sck_row += '    <div style="display:none">{sckId}</div>';
sck_row += '    <div style="display:none">{sckVal0}</div>';
sck_row += '    <div style="width:182px;white-space:nowrap;overflow:hidden;float:left;">{sckName}{sckVal}</div>';
sck_row += '    <div style="width:8px;float:left;">&nbsp;</div>';
sck_row += '    <div style="float:left;" id="scks_{index}_brmv"><a href="javascript:void(0)" style="color:#c42525;margin-left:0px;" title="' + msg("label_sck_del") + '"><i class="icon-remove icon-x"></i>&nbsp;&nbsp;</a></div>';
sck_row += '    <div class="clear"></div>';
sck_row += '</div>';

var sckcount = 0, sckindex;
function fillsck(sckId, sckVal) { // fill search cond
	var key = scanKey(sckId);
	if (!key) return;

	sckcount++;
	sckindex++;
	var $sckrow = $(sck_row.replace(/\{index\}/gi, sckindex).replace(/\{sckId\}/gi, sckId).replace(/\{sckVal0\}/gi, unull(sckVal)).replace(/\{sckVal\}/gi, (sckVal ? ': ' + sckVal : '')).replace(/\{sckName\}/gi, scanName(key)));
	$("#search-conds").prepend($sckrow);
	$("#scks_" + sckindex + "_brmv").click(function() {	
		if (sckcount > 0) sckcount--;
		var keyId = $(this).parent().find("div:first").html();
		upkeys("search-scks", keyId, true);
		$(this).parent().remove();
	});
	upkeys("search-scks", sckId, true, true);
	upscdivs();
}
function valscks() { // get search conds val
	var ctx = {}, ret = "";
	$("#search-conds >div").each(function() {
		var pref = $(this).attr("id");
		if (!pref) return;
		var sckId = $(this).find("div:first").html(), sckVal = $(this).find("div:eq(1)").html(), val = ctx[sckId];
		ctx[sckId] = (val ? val + sepke : "") + sckVal;
	});
	for (var sckId in ctx) ret += ";;" + sckId + "=" + ctx[sckId];
	return (ret ? ret.substring(2) : ret);
}
function upsckoptions() { // update search cond options
	var ckeyId = $("#search-ckeyId").val(), ssbId = $("#search-sortby").val(), ssboptions = (allk(ckeyId) ? "" : opt("", "0", ssbId)), sckoptions = "";
	var sckey = scanKey(ckeyId);
	$.each(unull(sckey.content).split(sepv), function(idx, item) {
		var key = scanKey(item);
		if (!key) return ;
		if (!mkey(key.typeId)) ssboptions += opt(scanName(key), key.keyId, ssbId);
		sckoptions += opt(scanName(key), key.keyId, null);
	});
	sckcount = 0;
	sckindex = 0;
	$('#search-conds').html("");
	opts("search-sortby", ssboptions);
	opts("search-scks", sckoptions);
}
function upscdivs() { // update serach cond divs
	var ckeyId = $("#search-ckeyId").val(), key = scanKey($("#search-scks").val()), demo = scanDemo(scanType(key.typeId));
	var divs = ["search-div-txt", "search-div-date"], pos = (!allk(ckeyId) && key && dkey(key.typeId) ? 1 : 0);
	$("#" + divs[pos] + "-1").attr("title", demo);
	if (pos == 1) $("#" + divs[pos] + "-2").attr("title", demo);
	show(divs[pos], 1);
	show(divs[1 - pos], 0);
	$("#search-txt").val("");
	if (key.asEnum) $("#search-txt").autocomplete({source:scanEnum(key.keyId)});
}

var list_km_row = '';
list_km_row += '<tr class="media_row">';
list_km_row += '    <td style="display:none">{kmId}</td>';
list_km_row += '    <td width="80" align="center" valign="middle"><span title="' + msg("label_kmId") + '">{kmId}</span></td>';
// list_km_row += '    <td width="100" align="center" valign="middle"><span title="' + msg("label_ckey") + '">{ckey}</span></td>';
list_km_row += '    <td width="100" align="center" valign="middle"><span title="' + msg("label_content") + '">{content}</span></td>';
list_km_row += '{keys}';
list_km_row += '    <td width="80" align="center" valign="middle"><span title="' + msg("label_updateDate") + '">{updateDate}</span></td>';
list_km_row += '    <td align="center" valign="middle">';
list_km_row += '        <div class="media_button" style="display:none">';
list_km_row += '            <a href="javascript:void(0)" class="button-detail-km" style="color:#99c15b;" title="' + msg("label_detail") + '"><i class="icon-list icon-large"></i>&nbsp;&nbsp;</a>';
list_km_row += '            <a href="javascript:void(0)" class="button-remove-km" style="color:#c42525;" title="' + msg("label_remove") + '"><i class="icon-trash icon-large"></i>&nbsp;&nbsp;</a>';
list_km_row += '        </div>';
list_km_row += '    </td>';
list_km_row += '</tr>';

var list_key_row = '';
list_key_row += '<tr class="media_row">';
list_key_row += '    <td style="display:none">{keyId}</td>';
list_key_row += '    <td width="100" align="center" valign="middle"><span title="' + msg("label_keyId") + '">{keyId}</span></td>';
list_key_row += '    <td width="160" align="center" valign="middle"><span title="' + msg("label_names") + '">{names}</span></td>';
list_key_row += '    <td width="160" align="center" valign="middle"><span title="' + msg("label_type") + '">{type}</span></td>';
list_key_row += '    <td width="160" align="center" valign="middle"><span title="' + msg("label_enum") + '">{enum0}</span></td>';
list_key_row += '    <td width="260" align="left" valign="middle"><span title="' + msg("label_sks") + '">{sks}</span></td>';
list_key_row += '    <td align="center" valign="middle">';
list_key_row += '        <div class="media_button" style="display:none">';
list_key_row += '            <a href="javascript:void(0)" class="button-detail-key" style="color:#99c15b;" title="' + msg("label_detail") + '"><i class="icon-list icon-large"></i>&nbsp;&nbsp;</a>';
list_key_row += '            <a href="javascript:void(0)" class="button-remove-key" style="color:#c42525;" title="' + msg("label_remove") + '"><i class="icon-trash icon-large"></i>&nbsp;&nbsp;</a>';
list_key_row += '        </div>';
list_key_row += '    </td>';
list_key_row += '</tr>';

function kmtd(width, title, val) { // generate the knowledge key td html
	return '    <td width="' + width + '" align="center" valign="middle"><span title="' + title + '">' + val + '</span></td>';
}

var new1_key_row = '';
new1_key_row += '<div style="margin-top:4px;" id="new1-kks_{index}">';
new1_key_row += '    <div style="float:left;display:none;" id="new1-kks_{index}_keyId">{keyId}</div>';
new1_key_row += '    <div style="width:60px;float:left;display:none;">{index}</div>';
new1_key_row += '    <div style="width:20px;float:left;">&nbsp;</div>';
new1_key_row += '    <div style="width:120px;float:left;">{keyName}</div>';
new1_key_row += '    <div style="width:20px;float:left;">&nbsp;</div>';
new1_key_row += '    <div style="width:486px;white-space:nowrap;overflow:hidden;float:left;" id="new1-kks_{index}_keyVal">{keyVal}</div>';
new1_key_row += '    <div style="width:20px;float:left;">&nbsp;</div>';
new1_key_row += '    <div style="float:left;" id="new1-kks_{index}_brmv"><a href="javascript:void(0)" style="color:#c42525;margin-left:0px;" title="' + msg("label_key_del") + '"><i class="icon-remove icon-x"></i>&nbsp;&nbsp;</a></div>';
new1_key_row += '    <div class="clear"></div>';
new1_key_row += '</div>';

var keycount = 0, keyindex;
function fillkey(keyId, keyVal, chkEnum) { // fill km keyword
	if (keycount > 26) {
		alert(msg("new1_keys_tip"));
		return;
	}
	var key = scanKey(keyId);
	if (!key) return;
	keycount++;
	keyindex++;
	var $new1keyrow = $(new1_key_row.replace(/\{index\}/gi, keyindex).replace(/\{keyId\}/gi, keyId).replace(/\{keyName\}/gi, unull(scanName(key))).replace(/\{keyVal\}/gi, unull(keyVal)));
	$("#new1-kks").prepend($new1keyrow);	
	$("#new1-kks_" + keyindex + "_brmv").click(function() {	
		if (keycount > 0) keycount--;
		$(this).parent().remove();
		var kid = $(this).parent().find("div:first").html();
		upkeys("new1-keys", kid, true);
		$("#keyListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
	});
	upkeys("new1-keys", keyId, true, true);
	if (chkEnum && key.asEnum) addEnum(keyId, keyVal);
	upkkdivs();
	$("#keyListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
}
function valkeys() { // get km keywords val
	var ctx = {}, ret = "";
	$("#new1-kks >div").each(function(){
		var pref = $(this).attr("id");
		if (!pref) return;
		var keyId = $("#" + pref + "_keyId").html(), keyVal = $("#" + pref + "_keyVal").html(), val = ctx[keyId];
		ctx[keyId] = (val ? val + sepke : "") + keyVal;
	});
	for (var keyId in ctx) ret += ";;" + keyId + "=" + ctx[keyId];
	return (ret ? ret.substring(2) : ret);
}
function upkkoptions(km) { // update km keyword options
	var update = (km ? true : false);
	// km tab init
	var kckeyId = $("#new1-ckeyId").val(), kckey = scanKey(kckeyId), kkoptions = "";
	$.each(unull(kckey.content).split(sepv), function(idx, item) {
		var key = scanKey(item);
		if (key && !updk(key.keyId)) kkoptions += opt(scanName(key), key.keyId, null);
	});
	opts("new1-keys", kkoptions);	
	$("#new1-keys").change(function() {
		upkkdivs();
	});
	upkkdivs();
	// keys tab init
	keycount = 0;
	keyindex = 0;
	$("#new1-kks").empty();
	$("#keyListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
	if (update) {
		$.each(unull(km.keys).split(";;"), function(idx, item) {
			var arr = item.split("=", 2), key = scanKey(arr[0]);
			if (!key) return;
			if (mkey(key.typeId)) {
				$.each(arr[1].split(sepke), function(idx2, item2) {
					fillkey(key.keyId, item2);
				});
			}
			else fillkey(key.keyId, arr[1]);
		});
	}
}
function upkkdivs() { // update km keywords divs
	var key = scanKey($("#new1-keys").val());
	$("#new1-div-keyVal").attr("title", scanDemo(scanType(key.typeId)));
	$("#new1-keyVal").val("");
	if (key.asEnum) $("#new1-keyVal").autocomplete({source:scanEnum(key.keyId)});
}
function fillNew1(km) { // fill km
	onMediaDivChanged("kmsNew1");
	var kh = $("#keysTab").is(":hidden");
	if (kh) $("#keysTab").show();
	var update = (km ? true : false);
	$("#new1-id0").html(unull(update ? km.kmId : ""));
	$("#new1-id").html(unull2(update ? km.kmId : ""));
	$("#new1-content").val(update ? km.content : "");
	var ckeyId = $("#new1-ckeyId").val();
	opts("new1-ckeyId", ckeyoptions);
	$("#new1-ckeyId").val(update && km.ckeyId ? km.ckeyId : ckeyId);
	upkkoptions(km);
	if (kh) $("#keysTab").hide();
}

var new2_sk_row = '';
new2_sk_row += '<div style="margin-top:5px;margin-left:10px;width:190px;float:left;" id="new2-sks_{index}" >';
new2_sk_row += '    <div style="display:none">{skId}</div>';
new2_sk_row += '    <div style="width:122px;white-space:nowrap;overflow:hidden;float:left;">{skName}</div>';
new2_sk_row += '    <div style="width:8px;float:left;">&nbsp;</div>';
new2_sk_row += '    <div style="float:left;" id="new2-sks_{index}_brmv"><a href="javascript:void(0)" style="color:#c42525;margin-left:0px;" title="' + msg("label_sk_del") + '"><i class="icon-remove icon-x"></i>&nbsp;&nbsp;</a></div>';
new2_sk_row += '    <div class="clear"></div>';
new2_sk_row += '</div>';

var skcount = 0, skindex;
function fillsk(skId) { // fill sub keyword
	if (skcount > 20) {
		alert(msg("new2_sks_tip"));
		return;
	}
	var key = scanKey(skId);
	if (!key) return;

	skcount++;
	skindex++;
	var $new2skrow = $(new2_sk_row.replace(/\{index\}/gi, skindex).replace(/\{skId\}/gi, skId).replace(/\{skName\}/gi, scanName(key)));
	$("#new2-sks").prepend($new2skrow);
	$("#new2-sks_" + skindex + "_brmv").click(function() {	
		if (skcount > 0) skcount--;
		var keyId = $(this).parent().find("div:first").html();
		upkeys("new2-keys", keyId, false);
		$(this).parent().remove();
	});
	upkeys("new2-keys", skId, false, true);
}
function valsks() { // get sub keywords val
	var ret = "";
	$("#new2-sks >div").each(function() {
		var pref = $(this).attr("id");
		if (!pref) return;
		var skId = $(this).find("div:first").html();
		if (skId) ret += sepv + skId;
	});
	return (ret ? ret.substring(sepv.length) : ret);
}
function fillNew2(key) { // fill keyword
	var update = (key ? true : false);
	$("#new2-id0").html(unull(update ? key.keyId : ""));
	$("#new2-id").html(unull2(update ? key.keyId : ""));
	var type = (update ? scanType(key.typeId) : null);
	// kts list
	var ktarr = kmsmeta.km.kts.kt, ktoptions = '';
	if (ktarr) {
		$.each(ktarr, function(idx, item) {
			if (kkey(item.typeId)) return;
			ktoptions += opt(scanName(item), item.typeId, (update ? key.typeId : null));
		});
	}
	opts("new2-type", ktoptions);
	// autocomplete or not
	chk("new2-asEnum", (update && key.asEnum == 'true' ? 1 : -1));
	$("#new2-nameCn").val(update ? unull(key.nameCn) : "");
	$("#new2-nameEn").val(update ? unull(key.nameEn) : "");
	// sub keyword list or content
	skcount = 0;
	skindex = 0;
	$("#new2-content").val('');
	$("#new2-sks").empty().prepend('<div class="clear"></div>');
	var karr = kmsmeta.km.ks.k, koptions = '';
	if (karr) {
		$.each(karr, function(idx, item) {
			if (ckey(item.typeId) || kkey(item.typeId)) return;
			koptions += opt(scanName(item), item.keyId, null);
		});
	}
	opts("new2-keys", koptions);
	if (ckey($("#new2-type").val())) {
		// sub keyword list
		if (update) {	
			$.each(unull(key.content).split(sepv), function(idx, item) {
				fillsk(item);
			});
		}
		show("new2-div-content1", -1);
		show("new2-div-content2", 1);
	} else if (update) {
		// content
		var pairs = {"keyId":key.keyId,"format":"json"};
		$.post('${context}/servlets/kms/keydetail', pairs, function(data) {
			if (data.result) $("#new2-content").val(data.key.content);
			else alert(msg("label_key_detail_fail"));
		},"json");
		show("new2-div-content2", -1);
		show("new2-div-content1", 1);
	}
	onMediaDivChanged("kmsNew2");
}

var ckeyoptions, sckeyoptions, kmsmeta, sepv = '${result.sepv}', sepke = '${result.sepke}';
function kmsinit(ms) { // init kms meta
	ckeyoptions = "";
	sckeyoptions = "";
	kmsmeta = ms;
	var kearr = kmsmeta.km.kes.ke;
	if (!kearr) kmsmeta.km["kes"] = {"ke":[]};
	else {
		$.each(kearr, function(idx, item) {
			item.enum0 = [];
			var eval = item.enumVal;
			if (eval) {
				var earr = eval.split(sepke);
				$.each(eval.split(sepke), function(idx2, item2) {
					item.enum0.push(item2);
				});
			}
		});
	}
	var karr = kmsmeta.km.ks.k, ckeys = kmsmeta.km.ckeys, ckeyId = $("#search-ckeyId").val();
	do {
		if (!ckeys) {
			alert(msg("meta_ckey_tip"));
			break;
		}
		if (!karr) {
			alert(msg("meta_key_tip"));
			break;
		}
		$.each(ckeys.split(sepv), function(idx, item) {
			var key = scan(karr, "keyId", item);
			if (!key) return ;
			if (!allk(key.keyId)) ckeyoptions += opt(scanName(key), key.keyId, null);
			sckeyoptions += opt(scanName(key), key.keyId, ckeyId);
		});
	}
	while (false);

	opts("search-ckeyId", sckeyoptions);
	upsckoptions();
	upscdivs();
	var type = scanType(3), demo= scanDemo(type);
	$("#new2-div-nameCn").attr("title", demo);
	$("#new2-div-nameEn").attr("title", demo);
}

function addEnum(keyId, eval) {
	var kearr = kmsmeta.km.kes.ke, e = scan(kearr, "keyId", keyId);
	if (e) {
		var hit = false;
		$.each(e.enum0, function(idx, item) {
			if (item == eval) {
				hit = true;
				return false;
			}
		});
		if (!hit) e.enum0.push(eval);
	} else kearr.push({"keyId":keyId,"enum0":[eval]});
}

function upkeys(id, keyId, chkmkey, del) {
	var key = scanKey(keyId);
	if (!id || !key) return;
	if (chkmkey && mkey(key.typeId)) return;
	if (del) {
		$('#' + id + ' option[value="' + keyId + '"]').remove();
		return;
	}
	var base = parseInt(keyId), last = parseInt($('#' + id + ' option:last').val()), koptions = opt(scanName(key), key.keyId, null);
	if (!last || last < base) {
		$('#' + id).append(koptions);
		return;
	}
	$('#' + id + ' option').each(function() {
	    if(parseInt($(this).val()) > base) {
	    	$(koptions).insertBefore($(this));
	    	return false;
	     }
	});
}

function scanName(obj) {
	return (obj ? unull(obj.nameCn ? obj.nameCn : obj.nameEn) : "");
}
function scanDemo(obj) {
	return (obj ? unull(obj.demoCn ? obj.demoCn : obj.demoEn) : "");
}
function scanEnum(keyId) {
	var e = scan(kmsmeta.km.kes.ke, "keyId", keyId);
	return (e ? e.enum0 : []);
}
function scanType(typeId) {
	var ktarr = kmsmeta.km.kts.kt, type = null;
	if (typeId < 1) return type;
	if (!ktarr) {
		alert(msg("meta_type_tip"));
		return type;
	}			
	type = scan(ktarr, "typeId", typeId);
	if (!type) {
		alert(msg("meta_typeId_tip") + typeId);
		return type;
	}
	return type;
}
function scanKey(keyId) {
	var karr = kmsmeta.km.ks.k, key = null;
	if (keyId < 1) return key;
	if (!karr) {
		alert(msg("meta_key_tip"));
		return key;
	}			
	key = scan(karr, "keyId", keyId);
	if (!key) {
		alert(msg("meta_keyId_tip") + keyId);
		return key;
	}
	return key;
}
function scanKeys(sortby, desc, conds) {
	var karr = kmsmeta.km.ks.k, ret = [], sbkey = scanKey(sortby), cds = [];
	if (!karr) {
		alert(msg("meta_key_tip"));
		return ret;
	}
	$.each(unull(conds).split(";;"), function(idx, item) {
		var arr = item.split('=', 2), key = scanKey(arr[0]);
		if (key) cds.push({"name":key.content, "base":arr[1]});
	});
	$.each(karr, function(idx, item) {
		var hit = true;
		$.each(cds, function(idx2, item2) {
			if (item[item2.name] != item2.base) {
				hit = false;
				return false;
			}
		});
		if (hit) ret.push(item);
	});
	sort(ret, sbkey.content, desc < 1, (ikey(sbkey.typeId) ? parseInt : null));
	return ret;
}
function scan(arr, key, val) {
	var ret = null;
	if (!arr || !key) return ret;
	$.each(arr, function(idx, item) {
		if (item[key] != val) return;
		ret = item;
		return false;
	});
	return ret;
}

function sort(arr, key, asc, func) {
    return arr.sort(function(a, b) {
        var x = (key ? a[key] : a), y = (key ? b[key] : b);
    	if (func) {
    		x = func(x);
    		y = func(y);
    	}
        var ret = ((x < y) ? -1 : ((x > y) ? 1 : 0));
        return (asc ? ret : 0 - ret);
    });
}
function opt(txt, val, sval) {
	return '<option value="' + val + '"' + (val == sval ? ' selected>' : '>') + txt + '</option>';
}
function opts(id, options) {
	if (options == 'get') {
		var ret = "";
		$('#' + id + ' option').each(function() {
			ret += opt($(this).text(), $(this).val());
		});
		return ret;
	}
	$("#" + id + " option").remove();
	$("#" + id).append(unull(options));
}
function chk(id, val) {
	if (!id) return false;
	if (val) {
		if (val > 0) $("#" + id).attr('checked', 'true');
		else $("#" + id).removeAttr('checked');
	}
	return ($("#" + id).attr('checked') ? 1 : -1);
}
function show(id, val) {
	$("#" + id).css('display',(val > 0 ? '' : 'none'));
}

function allk(keyId) { // 所有关键字
	return parseInt(keyId) == 1;
}
function tagk(keyId) { // tag type
	return parseInt(keyId) == 2000;
}
function updk(keyId) { // updateDate type
	return parseInt(keyId) == 3000;
}

function ckey(typeId) { // category type
	return parseInt(typeId) == 1;
}
function dkey(typeId) { // date type
	return parseInt(typeId) == 11;
}
function ikey(typeId) { // int type
	return (parseInt(typeId) == 2 || parseInt(typeId) == 13);
}
function kkey(typeId) { // keyword field type
	return (!ckey(typeId) && typeId < 10);
}
function mkey(typeId) { // multi-value type
	return parseInt(typeId) >= 50;
}

var detail_row = '';
detail_row += '<div style="margin-top:10px;">';
detail_row += '    <div style="width:160px;float:left;">{dtname}:</div><div style="{dtbreak}float:left;">{dtvalue}</div><div class="clear">';
detail_row += '</div>';  	

var searchTimer, searching = false;
function search(flag) {
	searching = flag;
	$("#button-search").css("cursor", (flag ? "default" : "pointer"));
	if (!flag) clearTimeout(searchTimer);
}
</script>
  
<div class="main ui-widget-content">
    <div style="margin-left:0px;height:40px;margin-bottom:0px;margin-top:0px;background-color:#A3D4F2">
        <div class="thumb_button" style="background-color:#7bbfe6;" title="<fmt:message key="label_kms" />"><fmt:message key="label_kms" /></div>
        <div class="button" id="button-refresh" style="margin-top:8px;margin-right:20px;background-color:#7bbfe6;color:#000;height:24px;line-height:24px;float:right;" title="<fmt:message key="button_refresh" />"><i class="icon-refresh icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_refresh" /></div>
        <div class="clear"></div>
    </div>
    <div id="mediaList" class="media_info" style="display:">
        <div style="width:100%;height:144px;background-color:#e6e6e6;">
	    	<div style="margin-left:20px;">
	    		<div class="search-icon" style="margin-top:10px;">&nbsp;</div>
	    		<div style="margin-left:10px;margin-top:10px;float:left;">
	    			<div>
			    		<div style="width:100px;margin-top:3px;margin-left:138px;float:left;"><fmt:message key="kms_list_new_win" /></div>
			    		<div style="width:70px;float:left;"><input type="checkbox" id="search-win"/></div>
			    		<div style="width:70px;margin-top:3px;float:left;"><fmt:message key="kms_ckeyId" /></div>
			    		<div style="width:170px;float:left;">
							<select id="search-ckeyId" style="width:120px;">
								<!--<option value=""><fmt:message key="kms_ckeyId_all" /></option>-->
							</select>
			    		</div>
			    		<div style="width:70px;margin-top:3px;float:left;"><fmt:message key="kms_sortby" /></div>
			    		<div style="width:140px;float:left;"><select id="search-sortby" style="width:120px;"></select></div>
			    		<div style="width:30px;float:left;" title="<fmt:message key="kms_desc" />"><input type="checkbox" id="search-desc"/></div>
			    		<div class="clear"></div>	
		    		</div>
	    			<div style="margin-top:10px;">
			    		<div id="search-div-base" style="width:100px;margin-left:152px;float:left;"><fmt:message key="kms_cond_sel" /></div>
			    		<div style="width:150px;float:left;"><select id="search-scks" style="width:120px;"></select></div>
				    	<div style="width:60px;float:left;"><fmt:message key="kms_equal" /></div>
		    			<div id="search-div-txt" style="float:left;">
				    		<div id="search-div-txt-1" style="width:340px;float:left;"><input id="search-txt" style="width:310px;"/></div>
				    		<div class="clear"></div>
			    		</div>
						<div id="search-div-date" style="float:left;display:none;">
				    		<div id="search-div-date-1" style="width:150px;float:left;"><input id="search-starttime" style="width:120px;"/></div>
				    		<div style="width:40px;margin-top:3px;float:left;"><fmt:message key="kms_to" /></div>
				    		<div id="search-div-date-2" style="width:150px;float:left;"><input id="search-endtime" value="" style="width:120px;"/></div>
				    		<div class="clear"></div>
			    		</div>
			    		<div class="clear"></div>
		    		</div>
					<div style="margin-top:10px;">
		    			<div style="width:100px;float:left;"><fmt:message key="kms_conds" /></div>
		    			<div style="width:10px;float:left;">&nbsp;&nbsp;</div>
			    		<div id="search-conds" style="width:666px;min-height:54px;border:1px solid #b3d57e;float:left;"></div>
			    		<div class="clear"></div>
		    		</div>
	    		</div>
	    		<div style="width:100px;margin-right:20px;float:right;">
            		<div id="button-search" style="margin-top:10px;cursor:pointer;text-align:center;background-color:#7BBFE6;height:24px;line-height:24px;width:100px;"><i class="icon-search icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_search" /></div>
            		<div id="button-cond-add" style="margin-top:6px;cursor:pointer;text-align:center;background-color:#b3d57e;height:24px;line-height:24px;width:100px;"><i class="icon-plus icon-large"></i>&nbsp;&nbsp;<fmt:message key="kms_cond_add" /></div>
	    		</div>
            	<div class="clear"></div>
	    	</div>
        </div>       
        <div style="width:100%;margin-top:1px;height:35px;background-color:#e0eecb;">
            <div id="thead" style="margin-top:5px;color:#859b52;font-weight:bold;margin-left:20px;height:24px;line-height:24px;width:400px;float:left;"><fmt:message key="label_total" />: <span id="search-count"></span>&nbsp;&nbsp;&nbsp;&nbsp;<fmt:message key="kms_records" /> ${result.maxRecords} <fmt:message key="kms_records2" /></div>
            <div id="button-new2" class="button" style="margin-top:5px;margin-right:20px;margin-left:0px;background-color:#b3d57e;height:24px;line-height:24px;width:132px;float:right;"><i class="icon-gear icon-large"></i>&nbsp;&nbsp;<fmt:message key="kms_new_key" />&nbsp;&nbsp;</div>
            <div id="button-new1" class="button" style="margin-top:5px;margin-right:20px;margin-left:0px;background-color:#b3d57e;height:24px;line-height:24px;width:132px;float:right;"><i class="icon-gear icon-large"></i>&nbsp;&nbsp;<fmt:message key="kms_new_km" /></div>
            <div class="clear"></div>
        </div>        
        <div style="position:relative;margin:0px;margin-top:1px;width:100%">
            <div id="mediaListTable" class="scroller_table" style="width:100%;height:400px;">
                <div class="customScrollBox" style="width:100%;position:relative;">
                    <div class="container" style="width:100%;">
                        <div class="content" style="width:100%;">
                            <!-- scroller table -->
                            <table cellpadding="0" width="100%" class="ui-table" cellspacing="0">
                                <tbody id="tbody">
                                </tbody>
                            </table>
                        </div>
                    </div>
                    <div class="dragger_container" style="height:400px;">
                        <div class="dragger"></div>
                    </div>
                </div>
            </div>
            <div class="clear"></div>
        </div>
    </div>
    <div id="kmsNew1" class="media_info" style="display:none;width:760px;margin-left:120px;">
    	<div style="margin-top:30px;margin-left:260px;font-weight:bold;font-size:18px;"><fmt:message key="kms_new_km_info" /></div>
		<div id="kmTabs" class="usual" style="width:100%;margin-top:20px;"> 
			<ul> 
				<li><a href="#kmTab" class="selected"><fmt:message key="kms_tab_km" /></a></li> 
				<li><a href="#keysTab"><fmt:message key="kms_tab_keys" /></a></li>
			</ul> 
			<div id="kmTab" class="tabs">
				<div style="margin-top:10px;">
		    		<div style="width:80px;float:left;"><fmt:message key="kms_new1_id" /></div>
		    		<div style="float:left;display:none;" id="new1-id0"></div>
		    		<div style="width:300px;float:left;" id="new1-id"></div>
		    		<div style="width:80px;float:left;"><fmt:message key="kms_new1_ckeyId" /></div>
		    		<div style="float:left;">
						<select id="new1-ckeyId" style="width:200px;">
							<!--<option value=""><fmt:message key="kms_ckeyId_all" /></option>-->
						</select>
		    		</div>
		    		<div class="clear"></div>
		    	</div>
			    <div style="margin-top:10px;"><fmt:message key="kms_new1_content" /></div>
			    <div style="margin-top:10px;"><textarea id="new1-content" cols="84" rows="13"></textarea></div>
			</div>
			<div id="keysTab" class="tabs">
		    	<div style="margin-top:20px;">
		    		<div style="width:120px;float:left;"><fmt:message key="kms_new1_key_sel" /></div>
		    		<div style="width:150px;float:left;"><select id="new1-keys" style="width:120px;"></select></div>
				    <div style="width:60px;float:left;"><fmt:message key="kms_equal" /></div>
		    		<div id="new1-div-keyVal" style="width:230px;float:left;"><input id="new1-keyVal" style="width:200px;"/></div>
	    			<div id="button-key-add" class="button" style="float:left;width:140px;background-color:#b3d57e;margin-top:-4px;margin-left:0px;"><i class="icon-plus icon-large"></i>&nbsp;&nbsp;<fmt:message key="kms_new1_key_add" /></div>
		    		<div class="clear"></div>
		    	</div>
	    		<div style="width:560px;margin-top:10px;"><fmt:message key="kms_new1_keys" /></div>
	            <div id="keyListTable" class="scroller_table" style="position:relative;margin-top:5px;border:1px solid #b3d57e;width:700px;height:220px;">
	                <div class="customScrollBox" style="width:100%;height:100%;position:relative;">
	                    <div class="container" style="width:100%;">
	                        <div class="content" style="width:100%;">
	                            <div id="new1-kks"></div>
	                        </div>
	                    </div>
	                    <div class="dragger_container" style="height:220px;">
	                        <div class="dragger"></div>
	                    </div>
	                </div>
	            </div>	
			</div>  
		</div> 
        <div style="margin-top:30px;height:40px;">
            <div id="button-new1-ok" class="button" style="float:left;margin-left:260px;"><i class="icon-ok icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_submit" /></div>
            <div class="button-return button" style="float:left;"><i class="icon-reply icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_cancel" /></div>
            <div class="clear"></div>
        </div>
    </div>  
    <div id="kmsNew2" class="media_info" style="display:none;width:680px;margin-left:240px;">
    	<div style="margin-top:30px;margin-left:140px;font-weight:bold;font-size:18px;"><fmt:message key="kms_new_key_info" /></div>
		<div style="margin-top:20px;">
    		<div style="width:140px;float:left;"><fmt:message key="kms_new2_id" /></div><div style="float:left;display:none;" id="new2-id0"></div><div style="float:left;" id="new2-id"></div><div class="clear"></div>
    	</div>
    	<div style="margin-top:10px;">
    		<div style="width:140px;float:left;"><fmt:message key="kms_new2_type" /></div>
    		<div style="float:left;"><select id="new2-type" style="width:140px;"></select></div>
    		<div style="width:70px;float:left;">&nbsp;&nbsp;</div>
    		<div style="width:140px;float:left;"><fmt:message key="kms_new2_enum" /></div>
    		<div style="float:left;"><input type="checkbox" id="new2-asEnum"/></div>
    		<div class="clear"></div>
    	</div>
    	<div style="margin-top:10px;">
    		<div style="width:140px;float:left;"><fmt:message key="kms_new2_nameCn" /></div>
    		<div style="float:left;" id="new2-div-nameCn"><input id="new2-nameCn" value="" style="width:360px;"/>&nbsp;&nbsp;*</div>
    		<div class="clear"></div>
    	</div>
    	<div style="margin-top:10px;">
    		<div style="width:140px;float:left;"><fmt:message key="kms_new2_nameEn" /></div>
    		<div style="float:left;" id="new2-div-nameEn"><input id="new2-nameEn" value="" style="width:360px;"/></div>
    		<div class="clear"></div>
    	</div>
    	<div id="new2-div-content1" style="margin-top:10px;height:248px;display:none;">
    		<div style="width:160px;margin-top:0px;"><fmt:message key="kms_new2_content1" /></div>
			<div style="margin-top:10px;"><textarea id="new2-content" cols="76" rows="13"></textarea></div>
    	</div>
    	<div id="new2-div-content2" style="margin-top:15px;height:248px;display:none;">
	    	<div style="margin-top:5px;">
	    		<div style="width:140px;float:left;"><fmt:message key="kms_new2_content2_sel" /></div>
	    		<div style="float:left;"><select id="new2-keys" style="width:188px;"></select></div>
    			<div style="width:10px;float:left;">&nbsp;&nbsp;</div>
    			<div id="button-sk-add" class="button" style="float:left;width:170px;background-color:#b3d57e;margin-top:-4px;margin-left:0px;"><i class="icon-plus icon-large"></i>&nbsp;&nbsp;<fmt:message key="kms_new2_key_add" /></div>
	    		<div class="clear"></div>
	    	</div>
    		<div style="width:560px;margin-top:5px;"><fmt:message key="kms_new2_content2_cur" /></div>
    		<div id="new2-sks" style="width:600px;height:180px;margin-top:5px;border:1px solid #b3d57e;"></div>
    	</div>
        <div style="margin-top:30px;height:40px;">
            <div id="button-new2-ok" class="button" style="float:left;margin-left:140px;"><i class="icon-ok icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_submit" /></div>
            <div class="button-return button" style="float:left;"><i class="icon-reply icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_cancel" /></div>
            <div class="clear"></div>
        </div>
    </div>
</div>
<div class="clear"></div>
</tags:template>
