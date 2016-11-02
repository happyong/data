<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java"%>
<%@ include file="common.jsp"%>
<tags:template className="mediaPage">
<script type="text/javascript" src="${static}/static/highcharts.js" charset="utf-8"></script>
<div style="display:none">${result.jsfunds}</div>
<script charset="utf-8">
var curdetid = "", curpage = "mediaList", portals='${result.portals}', rouser = Boolean(${login_user.readonly});
$(function() {  	 
	$(".button-return").click(function() {
		onMediaDivChanged("mediaList");
	});
	menus('${result.menus}'); 
	chartInit();
	
	$("#button-search").click(function() {
		clearInterval(chartInterval);
		var symbol = $("#search-symbol").val();
		var sort_type = $("#search-sorttype").val();
		var sort = $("#search-sort").val();
		if (searching) {
			alert(msg("search_time_tip"));
			return;
		}
		
		search(true);
		searchTimer = setTimeout("search(false)",1000);
		$('#tbody').html("");
		$('#search-count').html("0");
		$("#mediaListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
		
		var pairs = {"format":"json","symbol":symbol,"sortType":sort_type,"sort":sort};
		$.post("${context}/servlets/funds/funds", pairs, function(data) {
			if (data.result) {
				var count = parseInt(data.funds.count);
				if (count < 1) return;
				var funds = (count == 1 ? [data.funds.fund] : (data.funds.fund ? data.funds.fund : []));
				$('#search-count').html(funds.length);
				for (var i = 0; i < funds.length; i++) {
					var codeB = funds[i].code, base = "", extend = "", net = "", quote = "", bean;
					extend += (funds[i].m ? (extend ? "<br/>" : "") + funds[i].m : "");
					extend += (funds[i].sd ? (extend ? "<br/>" : "") + funds[i].sd : "");
					extend += (funds[i].ed ? (extend ? "<br/>" : "") + funds[i].ed : "");
					extend += '<br/><a href="javascript:void(0)" class="button-chart" style="color:#99c15b;" title="' + msg("label_chart") + '"><i class="icon-bar-chart icon-large"></i>&nbsp;&nbsp;</a>';
					extend += '&nbsp;&nbsp;<a href="javascript:void(0)" class="button-edit" style="color:#99c15b;" title="' + msg("label_edit") + '"><i class="icon-edit-sign icon-large"></i>&nbsp;&nbsp;</a>';
					for (var j = 0; j < 3; j++) {
						bean = funds[i]["stat" + j];
						if (!bean) continue;
						base += (bean.code ? (base ? "<br/>" : "") + bean.code : "");
						base += (bean.n2 ? (base ? ", " : "") + bean.n2 : "");
						base += (bean.e ? (base ? ", " : "") + bean.e : "");
						if (j == 1) base += (bean.n ? (base ? "<br/>" : "") + bean.n : "");
						net += (bean.net ? (net ? "<br/>" : "") + bean.net : "");
						if (j == 0) quote += (bean.qm ? (quote ? "<br/>" : "") + "&nbsp;&nbsp;&nbsp;&nbsp;" + bean.qm : "");
						if (j != 2) quote += (bean.q ? (quote ? "<br/>" : "") + "&nbsp;&nbsp;&nbsp;&nbsp;" + bean.q : "");
					}
					var $fund = $(fund_row.replace(/\{codeB\}/gi, codeB).replace(/\{base\}/gi, base).replace(/\{extend\}/gi, extend).replace(/\{net\}/gi, net).replace(/\{quote\}/gi, quote));
					$('#tbody').append($fund);				
				}
 	   
				$("#mediaListTable .media_row").each(function(index, element) {
					$(this).addClass(index%2 == 0 ? "row_odd" : "row_even").hover(function() {
						$(this).find(".media_button").show();
					}, function() {
						$(this).find(".media_button").hide();
					});
				});
				
				$(".button-chart").click(function() {
					if (readonly()) return;
					var codeB = $(this).parents("tr:first").find("td:first").html();
					curdetid = (codeB ? codeB : "");
					detail();
				}); 
				
				$(".button-edit").click(function() {
					if (readonly()) return;
					var codeB = $(this).parents("tr:first").find("td:first").html();
					si = false; su = false;
					$("#symbol_details").html("");
					$("#symbol_edits").css('display','none');
					ni = false; n0u = false; n1u = false; n2u = false;
					$("#net_details").html("");
					$("#net_edits").css('display','none');
					qi = false; q0u = false; q1u = false;
					$("#quote_details").html("");
					$("#quote_edits").css('display','none');
					$("#editListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
					$.post("${context}/servlets/funds/funddetail", {"format":"json", "codeB":codeB}, function(data) {
						if (data.result) {
							var codeB = data.fund.code, base = "", extend = "", net = "", quote = "", bean;
							extend += (data.fund.m ? (extend ? ", " : "") + data.fund.m : "");
							extend += (data.fund.sd ? (extend ? ", " : "") + data.fund.sd : "");
							extend += (data.fund.ed ? (extend ? ", " : "") + data.fund.ed : "");
							onMediaDivChanged("mediaEdit");
							$("#edit_base").html(extend);
							$("#edit_codeB").html(codeB);
							for (var i = 0; i < 3; i++) {
								bean = data.fund["stat" + i];
								if (!bean) continue;
								base = (bean.code ? bean.code : "");
								base += (bean.n2 ? (base ? ", " : "") + bean.n2 : "");
								base += (bean.n ? (base ? ", " : "") + bean.n : "");
								base += (bean.e ? (base ? ", " : "") + bean.e : "");
								var $detailrow1 = $(detail_row.replace(/\{dtvalue\}/gi, base).replace(/\{stitle\}/gi, msg("label_base")));
								var $detailrow2 = $(detail_row.replace(/\{dtvalue\}/gi, (bean.q ? bean.q : "")).replace(/\{stitle\}/gi, msg("label_quote")));
								var $detailrow3 = $(detail_row.replace(/\{dtvalue\}/gi, (bean.net ?  bean.net : "")).replace(/\{stitle\}/gi, msg("label_net2")));
								if (i == 1) {
									$("#symbol_details").find("div:eq(0)").before($detailrow1);
									$("#quote_details").find("div:eq(0)").before($detailrow2);
									$("#net_details").find("div:eq(0)").before($detailrow3);
								} else {
									$("#symbol_details").append($detailrow1);
									$("#quote_details").append($detailrow2);
									$("#net_details").append($detailrow3);
								}
							}
							$("#editListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
						} else 
							alert(msg("detail_fail"));
					},"json");
				});							
			} else {
				search(false);
				var error = msg("search_fail");
				if (data.error && data.error.value) error += "\n" + msg("fail_reason") + data.error.value;
				alert(error);
			}
			$("#mediaListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
		},"json");
		
		onMediaDivChanged("mediaList");
	});

	$("#button-export-xml").click(function() {
		window.open("", 'window2');
		$("#symbol").val($("#search-symbol").val());
		$("#sortType").val($("#search-sorttype").val());
		$("#sort").val($("#search-sort").val());
		$("#searchForm").attr("action", "${context}/servlets/funds/funds").attr("target", "window2").submit().removeAttr("target");
	});

	$("#button-export-ebk").click(function() {
		$("#symbol").val($("#search-symbol").val());
		$("#sortType").val($("#search-sorttype").val());
		$("#sort").val($("#search-sort").val());
		$("#searchForm").attr("action", "${context}/servlets/funds/fundebk").removeAttr("target").submit();
	});
				
	$("#funds_symbol_edit").click(function() {
		if (readonly() || su) return;
		if ($("#symbol_edits").css('display') == 'none') {
			var pairs = {"format":"json"};
			var codeB = $("#edit_codeB").html();
			if (isEmpty(codeB)) alert(msg("funds_codeB_tip"));
			else pairs.codeB = codeB;
			$("#symbol_edits").css('display','');
			if (!si) {
				si = true;
				$("#symbol_edits_content").html("");
				$.post("${context}/servlets/funds/fundsymboldetail", pairs, function(data) {
					if (!data.result) {
						var error = msg("symbol_fail");
						if (data.error) error += "\n" + msg("fail_reason") + data.error;
						alert(error);
						return;
					}
					for (var i = 0; i < 3; i++) {
						var ss = data.symbols["symbol" + i];
						if (!ss) ss= {};
						if (!ss.market) ss.market = "sz";
						var $symbolrow = $(symbol_row.replace(/\{type\}/gi, "" + i).replace(/\{scode\}/gi, unull(ss.code)).replace(/\{sname\}/gi, unull(ss.name)).replace(/\{snameS\}/gi, unull(ss.nameS)).replace(/\{smarket\}/gi, unull(ss.market)).replace(/\{sequity\}/gi, unull(ss.equity)).replace(/\{smanager\}/gi, unull(ss.manager)).replace(/\{sstart\}/gi, unull(ss.startDate)).replace(/\{send\}/gi, unull(ss.endDate)));
						if (i == 1) $("#symbol_edits_content").find("div:eq(0)").before($symbolrow);
						else $("#symbol_edits_content").append($symbolrow);
					}
					$("#editListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
				},"json");
			}
		} else {
			$("#symbol_edits").css('display','none');
		}
	}); 

	$("#funds_symbol_update").click(function() {
		var pairs = {"format":"json"}, pf = "symbol_edit_", tip = "symbol_edit_";
		for (var i = 0; i < 3; i++) {
			if (!fill2("code" + i, pf, tip, pairs) || !fill2("name" + i, pf, tip, pairs) || !fill2("market" + i, pf, tip, pairs)) return;
			pairs["name_s" + i] = $("#" + pf + "name_s" + i).val();
			pairs["equity" + i] = $("#" + pf + "equity" + i).val();
			pairs["manager" + i] = $("#" + pf + "manager" + i).val();
			pairs["start_date" + i] = $("#" + pf + "start_date" + i).val();
			pairs["end_date" + i] = $("#" + pf + "end_date" + i).val();
		}
		if (!confirm(msgconfirm(pairs["code1"] + ", " + pairs["code0"] + ", " + pairs["code2"], "update_label"))) return;
		$.post("${context}/servlets/funds/fundsymbolupdate", pairs, function(data) {
			if (data.result) {
				su = true;
				$("#symbol_edits").css('display','none');
			} else {
				$("symbol_edit_code1").focus();
				var error = msg("update_fail");
				if (data.error) error += "\n" + msg("fail_reason") + data.error;
				alert(error);
			}
		},"json");
	});
				
	$("#funds_quote_edit").click(function() {
		if (readonly()) return;
		if ($("#quote_edits").css('display') == 'none') {
			var pairs = {"format":"json"}, pf = "funds_quote_", tip = "funds_quote_";
			if (!fill2("date", pf, tip, pairs)) return;
			var codeB = $("#edit_codeB").html();
			if (isEmpty(codeB)) alert(msg("funds_codeB_tip"));
			else pairs.codeB = codeB;
			$("#quote_edits").css('display','');
			if (pairs.date != qd) qi = false;
			if (!qi) {
				qi = true;
				qd = pairs.date;
				$("#quote_edits_content").html("");
				$.post("${context}/servlets/funds/fundquotedetail", pairs, function(data) {
					if (!data.result) {
						qi = false;
						$("#quote_edits").css('display','none');
						var error = msg("quote_fail");
						if (data.error) error += "\n" + msg("fail_reason") + data.error;
						alert(error);
						return;
					}
					for (var i = 0; i < 2; i++) {
						var ss = data.quotes["quote" + i], st = msg("quote_edit_" + i + "_update");
						if (!ss) ss= {};
						if (!ss.date) ss.date = qd;
						var $quoterow = $(quote_row.replace(/\{type\}/gi, "" + i).replace(/\{scode\}/gi, unull(ss.code)).replace(/\{sdate\}/gi, unull(ss.date)).replace(/\{stime\}/gi, unull(ss.time)).replace(/\{sprice\}/gi, unull(ss.price)).replace(/\{sclosePrev\}/gi, unull(ss.closePrev)).replace(/\{sopen\}/gi, unull(ss.open)).replace(/\{shigh\}/gi, unull(ss.high)).replace(/\{slow\}/gi, unull(ss.low)).replace(/\{svolume\}/gi, unull(ss.volume)).replace(/\{samount\}/gi, unull(ss.amount)).replace(/\{stitle\}/gi, st));
						if (i == 1) $("#quote_edits_content").find("div:eq(0)").before($quoterow);
						else $("#quote_edits_content").append($quoterow);
					}
					$(".quote-row").click(function() {
						var type = $(this).attr("data-link");  // var type = $(this).parents(".quote-row").find("div:first").html();
						var pairs = {"format":"json"}, pf = "quote_edit_" + type + "_", tip = "quote_edit_";
						if (!fill2("code", pf, tip, pairs) || !fill2("date", pf, tip, pairs) || !fill2("time", pf, tip, pairs) || !fill2("price", pf, tip, pairs) || !fill2("close_prev", pf, tip, pairs) || !fill2("open", pf, tip, pairs) || !fill2("high", pf, tip, pairs) || !fill2("low", pf, tip, pairs) || !fill2("volume", pf, tip, pairs) || !fill2("amount", pf, tip, pairs)) return;
						if (!confirm(msgconfirm(pairs["code"] + ", " + pairs["date"], "update_label"))) return;
						$.post("${context}/servlets/funds/fundquoteupdate", pairs, function(data) {
							if (data.result) {
								$("#quote_edit_" + type + "_update").css('display','none');
							} else {
								$("quote_edit_" + type + "_code").focus();
								var error = msg("update_fail");
								if (data.error) error += "\n" + msg("fail_reason") + data.error;
								alert(error);
							}
						},"json");
					});
					$("#editListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
				},"json");
			}
		} else {
			$("#quote_edits").css('display','none');
		}
	}); 
				
	$("#funds_net_edit").click(function() {
		if (readonly()) return;
		if ($("#net_edits").css('display') == 'none') {
			var pairs = {"format":"json"}, pf = "funds_net_", tip = "funds_net_";
			if (!fill2("date", pf, tip, pairs)) return;
			var codeB = $("#edit_codeB").html();
			if (isEmpty(codeB)) alert(msg("funds_codeB_tip"));
			else pairs.codeB = codeB;
			$("#net_edits").css('display','');
			if (pairs.date != nd) ni = false;
			if (!ni) {
				ni = true;
				nd = pairs.date;
				$("#net_edits_content").html("");
				$.post("${context}/servlets/funds/fundnetdetail", pairs, function(data) {
					if (!data.result) {
						ni = false;
						$("#net_edits").css('display','none');
						var error = msg("net_fail");
						if (data.error) error += "\n" + msg("fail_reason") + data.error;
						alert(error);
						return;
					}
					for (var i = 0; i < 3; i++) {
						var ss = data.nets["net" + i], st = msg("net_edit_" + i + "_update");
						if (!ss) ss= {};
						if (!ss.date) ss.date = nd;
						var $netrow = $(net_row.replace(/\{type\}/gi, "" + i).replace(/\{scode\}/gi, unull(ss.code)).replace(/\{sdate\}/gi, unull(ss.date)).replace(/\{snet\}/gi, unull(ss.net)).replace(/\{snetTotal\}/gi, unull(ss.netTotal)).replace(/\{sgrowth\}/gi, unull(ss.growth)).replace(/\{stitle\}/gi, st));
						if (i == 1) $("#net_edits_content").find("div:eq(0)").before($netrow);
						else $("#net_edits_content").append($netrow);
					}
					$(".net-row").click(function() {
						var type = $(this).attr("data-link");  // var type = $(this).parents(".net-row").find("div:first").html();
						var pairs = {"format":"json"}, pf = "net_edit_" + type + "_", tip = "net_edit_";
						if (!fill2("code", pf, tip, pairs) || !fill2("date", pf, tip, pairs) || !fill2("net", pf, tip, pairs) || !fill2("net_total", pf, tip, pairs) || !fill2("growth", pf, tip, pairs)) return;
						if (!confirm(msgconfirm(pairs["code"] + ", " + pairs["date"], "update_label"))) return;
						$.post("${context}/servlets/funds/fundnetupdate", pairs, function(data) {
							if (data.result) {
								$("#net_edit_" + type + "_update").css('display','none');
							} else {
								$("net_edit_" + type + "_code").focus();
								var error = msg("update_fail");
								if (data.error) error += "\n" + msg("fail_reason") + data.error;
								alert(error);
							}
						},"json");
					});
					$("#editListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
				},"json");
			}
		} else {
			$("#net_edits").css('display','none');
		}
	}); 

	$("#funds_quote_date").val("${result.last_trade}"); $("#funds_net_date").val("${result.last_trade}");
	$("#mediaListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
	$("#detailListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
	$("#editListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
	list();
}); 

var detid = "${detid}";
function list() {
	$("#button-search").trigger("click");
}
function detail() {
	if (!curdetid) return;
	$("#chart_base").html("");
	$("#chart_details").html("");
	$("#chart_imgs").html("");
	$("#detailListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
	$.post("${context}/servlets/funds/funddetail", {"format":"json", "codeB":curdetid}, function(data) {
		if (data.result) {
			clearInterval(chartInterval);
			var codeB = data.fund.code, base = "", extend = "", net = "", quote = "", bean;
			extend += (data.fund.m ? (extend ? ", " : "") + data.fund.m : "");
			extend += (data.fund.sd ? (extend ? ", " : "") + data.fund.sd : "");
			extend += (data.fund.ed ? (extend ? ", " : "") + data.fund.ed : "");
			onMediaDivChanged("mediaDetail", codeB);
			$("#chart_base").html(extend);
			for (var i = 0; i < 3; i++) {
				bean = data.fund["stat" + i];
				if (!bean) continue;
				base = (bean.code ? bean.code : "");
				base += (bean.n2 ? (base ? ", " : "") + bean.n2 : "");
				base += (bean.n ? (base ? ", " : "") + bean.n : "");
				base += (bean.e ? (base ? ", " : "") + bean.e : "");
				var $detailrow1 = $(detail_row.replace(/\{dtvalue\}/gi, base).replace(/\{stitle\}/gi, msg("label_base")));
				var $detailrow2 = $(detail_row.replace(/\{dtvalue\}/gi, (bean.q ? bean.q : "")).replace(/\{stitle\}/gi, msg("label_quote")));
				var $detailrow3 = $(detail_row.replace(/\{dtvalue\}/gi, (bean.net ?  bean.net : "")).replace(/\{stitle\}/gi, msg("label_net2")));
				if (i == 1) $("#chart_details").find("div:eq(0)").before($detailrow1).before($detailrow2).before($detailrow3);
				else $("#chart_details").append($detailrow1).append($detailrow2).append($detailrow3);
				if (i < 2) {
					var $img1 = $(img_row.replace(/\{src\}/gi, (img_base + "min/n/" + bean.code2 + ".gif?rd=" + Math.random())));
					var $img2 = $(img_row.replace(/\{src\}/gi, (img_base + "daily/n/" + bean.code2 + ".gif?rd=" + Math.random())));
					if (i == 0) $('#chart_imgs').append($img1).append($img2);
					else $('#chart_imgs').find("div:eq(0)").before($img1).after($img2);
				}
			}
			chartDataAppend(data.fund.net1.net, data.fund.net2.net, data.fund.netm1.net, data.fund.netm2.net);
			$("#detailListTable").mCustomScrollbar("vertical",300,"easeOutCirc",1.05,"auto","yes","yes",15);
			chartInterval = setInterval(detail, 60000);		
		} else 
			alert(msg("detail_fail"));
	},"json");
}

function chartInit() {
	// highchart options
	Highcharts.setOptions({global: {useUTC: false}});
	chartColors = Highcharts.getOptions().colors;
	var cht, sers;
	for (var i = 0; i < charts.length; i++) {
		cht = charts[i];
		sers = cht.series;
		cht.chart = chartNew(cht.id, true, msg(cht.ytitle));
		for (var j = 0; j < sers.length; j++) cht.chart.addSeries({name: msg("serries_" + sers[j]), color: chartColors[j]}, false);
	}
}

function chartNew(id, bchart, chtitle, ytitle) {
	return $("#chart_" + id).css('display', (bchart ? '' : 'none')).empty().highcharts({
		chart: {type: 'spline', animation: Highcharts.svg, marginRight: 10},
		title: {text: msg("label_" + id)},
		xAxis: {type: 'datetime', tickPixelInterval: 100},
		yAxis: {
			title: {text: ytitle},
			plotLines: [{value: 0, width: 1, color: '#808080'}]
		},
		tooltip: {
			formatter: function() {
				var s = '<b><span style="color:' + chartColors[1] + '">'+ Highcharts.dateFormat('%Y-%m-%d %H:%M', this.x) +'</span></b>';
				$.each(this.points, function(i, point) {
					s += '<br/><span style="color:' + point.series.color + '">' + point.series.name + ': ' + point.y + '</span>';
				});
				return s;
			},
			shared: true
		},
		plotOptions: {spline: {lineWidth: 2, marker: {enabled: false}}},
		legend: {enabled: true},
		credits: {enabled: false},
		exporting: {enabled: false}
	}).highcharts();
}

var chartInterval, chartColors;
var charts = [{"id":"net", "ytitle":"label_y_rmb", "series":["net", "net_m"]}, 
                {"id":"net_ratio", "ytitle":"label_y_ratio", "series":["net_premium", "net_premium2", "net_premiumHigh5", "net_premiumLow5", "net_growth", "net_premium_m", "net_growth_m"]}, 
                {"id":"net_min", "ytitle":"label_y_ratio", "series":["net_premium", "net_premium2", "net_premiumHigh5", "net_premiumLow5", "net_premium_m", "net_premium2_m"]}];
function chartDataAppend(arr1, arr2, arr3, arr4) {
	var nets1 = ($.isArray(arr1) ? arr1 : [arr1]), nets2 = ($.isArray(arr2) ? arr2 : [arr2]), netmins1 =  ($.isArray(arr3) ? arr3 : [arr3]), netmins2 =  ($.isArray(arr4) ? arr4 : [arr4]);
	var chartData = {}, net, time, cht, sers;
	for (var i = 0; i < charts.length; i++) {
		sers = charts[i].series;
		for (var j = 0; j < sers.length; j++) chartData["ch" + i + "_" + sers[j]] = [];
	}
	for (var i = 0; i < nets1.length; i++) {
		net = nets1[i], time = parseInt(net.t);
		chartData["ch0_net"].push({x: time, y: parseFloat(net.net)});
		chartData["ch1_net_premium"].push({x: time, y: parseFloat(net.p)});
		chartData["ch1_net_premium2"].push({x: time, y: parseFloat(net.p2)});
		chartData["ch1_net_premiumHigh5"].push({x: time, y: parseFloat(net.ph5)});
		chartData["ch1_net_premiumLow5"].push({x: time, y: parseFloat(net.pl5)});
		chartData["ch1_net_growth"].push({x: time, y: parseFloat(net.g)});
	}
	for (var i = 0; i < nets2.length; i++) {
		net = nets2[i], time = parseInt(net.t);
		chartData["ch0_net_m"].push({x: time, y: parseFloat(net.net)});
		chartData["ch1_net_premium_m"].push({x: time, y: parseFloat(net.p)});
		chartData["ch1_net_growth_m"].push({x: time, y: parseFloat(net.g)});
	}
	for (var i = 0; i < netmins1.length; i++) {
		net = netmins1[i], time = parseInt(net.t);
		chartData["ch2_net_premium"].push({x: time, y: parseFloat(net.p)});
		chartData["ch2_net_premium2"].push({x: time, y: parseFloat(net.p2)});
		chartData["ch2_net_premiumHigh5"].push({x: time, y: parseFloat(net.ph5)});
		chartData["ch2_net_premiumLow5"].push({x: time, y: parseFloat(net.pl5)});
	}
	for (var i = 0; i < netmins2.length; i++) {
		net = netmins2[i], time = parseInt(net.t);
		chartData["ch2_net_premium_m"].push({x: time, y: parseFloat(net.p)});
		chartData["ch2_net_premium2_m"].push({x: time, y: parseFloat(net.p2)});
	}
	for (var i = 0; i < charts.length; i++) {
		cht = charts[i], sers = cht.series;
		for (var j = 0; j < sers.length; j++) cht.chart.series[j].setData(chartData["ch" + i + "_" + sers[j]]);
		cht.chart.redraw();
	}
}

var img_base = "http://image.sinajs.cn/newchart/";
var img_row = '<div style="margin-left:0px;margin-top:10px;"><img src="{src}" width="418" height="230" /></div>';

var detail_row = '';
detail_row += '<div style="margin-left:0px;margin-top:4px;">';
detail_row += '    <div style="float:left;" title="{stitle}">{dtvalue}</div><div class="clear">';
detail_row += '</div>';

var fund_row =  '';
fund_row +=  '<tr class="media_row">';
fund_row += '    <td style="display:none">{codeB}</td>';
fund_row += '    <td style="padding:6px;" width="195" align="center" valign="middle"><span title="' + msg("label_base") + '">{base}</span></td>';
fund_row += '    <td width="90" align="center" valign="middle"><span title="' + msg("label_extend") + '">{extend}</span></td>';
fund_row += '    <td style="padding-left:6px;" width="450" align="left" valign="middle"><span title="' + msg("label_net2") + '">{net}</span></td>';
fund_row += '    <td style="padding-top:6px;padding-bottom:6px;" width="260" align="left" valign="middle"><span title="' + msg("label_quote") + '">{quote}</span></td>';
fund_row += '    <td>&nbsp;&nbsp;</td>';
fund_row += '</tr>';

var symbol_row = '';
symbol_row += '<div style="margin-top:10px;">';
symbol_row += '    <div style="float:left;"><input id="symbol_edit_code{type}" value="{scode}" style="width:60px;"/></div>';
symbol_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
symbol_row += '    <div style="float:left;"><input id="symbol_edit_name{type}" value="{sname}" style="width:200px;"/></div>';
symbol_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
symbol_row += '    <div style="float:left;"><input id="symbol_edit_name_s{type}" value="{snameS}" style="width:60px;"/></div>';
symbol_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
symbol_row += '    <div style="float:left;"><input id="symbol_edit_market{type}" value="{smarket}" style="width:50px;"/></div>';
symbol_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
symbol_row += '    <div style="float:left;"><input id="symbol_edit_equity{type}" value="{sequity}" style="width:60px;"/></div>';
symbol_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
symbol_row += '    <div style="float:left;"><input id="symbol_edit_manager{type}" value="{smanager}" style="width:100px;"/></div>';
symbol_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
symbol_row += '    <div style="float:left;"><input id="symbol_edit_start_date{type}" value="{sstart}" style="width:80px;"/></div>';
symbol_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
symbol_row += '    <div style="float:left;"><input id="symbol_edit_end_date{type}" value="{send}" style="width:80px;"/></div>';
symbol_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
symbol_row += '    <div class="clear"></div>';
symbol_row += '</div>';

var quote_row = '';
quote_row += '<div style="margin-top:10px;">';
quote_row += '    <div style="float:left;"><input id="quote_edit_{type}_code" value="{scode}" style="width:60px;"/></div>';
quote_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
quote_row += '    <div style="float:left;"><input id="quote_edit_{type}_date" value="{sdate}" style="width:80px;"/></div>';
quote_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
quote_row += '    <div style="float:left;"><input id="quote_edit_{type}_time" value="{stime}" style="width:80px;"/></div>';
quote_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
quote_row += '    <div style="float:left;"><input id="quote_edit_{type}_price" value="{sprice}" style="width:60px;"/></div>';
quote_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
quote_row += '    <div style="float:left;"><input id="quote_edit_{type}_close_prev" value="{sclosePrev}" style="width:60px;"/></div>';
quote_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
quote_row += '    <div style="float:left;"><input id="quote_edit_{type}_open" value="{sopen}" style="width:60px;"/></div>';
quote_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
quote_row += '    <div style="float:left;"><input id="quote_edit_{type}_high" value="{shigh}" style="width:60px;"/></div>';
quote_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
quote_row += '    <div style="float:left;"><input id="quote_edit_{type}_low" value="{slow}" style="width:60px;"/></div>';
quote_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
quote_row += '    <div style="float:left;"><input id="quote_edit_{type}_volume" value="{svolume}" style="width:100px;"/></div>';
quote_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
quote_row += '    <div style="float:left;"><input id="quote_edit_{type}_amount" value="{samount}" style="width:100px;"/></div>';
quote_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
quote_row += '    <div id="quote_edit_{type}_update" class="quote-row" data-link="{type}" style="float:left;"><a href="javascript:void(0)" style="color:#c42525;margin-left:0px;" title="{stitle}">&nbsp;&nbsp;&nbsp;&nbsp;<i class="icon-plus icon-2x"></i>&nbsp;&nbsp;</a></div>';
quote_row += '    <div class="clear"></div>';
quote_row += '</div>';

var net_row = '';
net_row += '<div style="margin-top:10px;">';
net_row += '    <div style="float:left;"><input id="net_edit_{type}_code" value="{scode}" style="width:60px;"/></div>';
net_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
net_row += '    <div style="float:left;"><input id="net_edit_{type}_date" value="{sdate}" style="width:80px;"/></div>';
net_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
net_row += '    <div style="float:left;"><input id="net_edit_{type}_net" value="{snet}" style="width:80px;"/></div>';
net_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
net_row += '    <div style="float:left;"><input id="net_edit_{type}_net_total" value="{snetTotal}" style="width:80px;"/></div>';
net_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
net_row += '    <div style="float:left;"><input id="net_edit_{type}_growth" value="{sgrowth}" style="width:80px;"/></div>';
net_row += '    <div style="width:12px;float:left;">&nbsp;</div>';
net_row += '    <div id="net_edit_{type}_update" class="net-row" data-link="{type}" style="float:left;"><a href="javascript:void(0)" style="color:#c42525;margin-left:0px;" title="{stitle}">&nbsp;&nbsp;&nbsp;&nbsp;<i class="icon-plus icon-2x"></i>&nbsp;&nbsp;</a></div>';
net_row += '    <div class="clear"></div>';
net_row += '</div>';

var si = false, su = false, qi = false, qd = "", ni = false, nd = "";

var searchTimer, scheduleTime, searching = false;
function search(flag) {
	searching = flag;
	$("#button-search").css("cursor", (flag ? "default" : "pointer"));
	if (!flag) clearTimeout(searchTimer);
}
function scheduling(obj) {
	$("#button-manual-ok").css("cursor", "default");
	$("#button-manual-cancel").css("cursor", "default");
	$("#manual-scheduling").css('display','');
	if (obj) $(obj).css("cursor", "default");
	clearTimeout(scheduleTime);
	scheduleTime = setTimeout(function() {
		list();
	},3000);
}
</script>
  
<div class="main ui-widget-content">
    <div style="margin-left:0px;height:40px;margin-bottom:0px;margin-top:0px;background-color:#A3D4F2">
        <div class="thumb_button" style="background-color:#7bbfe6;" title="<fmt:message key="label_funds" />"><fmt:message key="label_funds" /></div>
        <div class="button" id="button-refresh" style="margin-top:8px;margin-right:20px;background-color:#7bbfe6;color:#000;height:24px;line-height:24px;float:right;" title="<fmt:message key="button_refresh" />"><i class="icon-refresh icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_refresh" /></div>
        <div class="clear"></div>
    </div>
    <div id="mediaList" class="media_info" style="display:none;">
        <div style="width:100%;height:48px;background-color:#e6e6e6;">
	    	<div style="margin-left:20px;">
	    		<div class="search-icon" style="margin-top:7px;">&nbsp;</div>
	    		<div style="margin-left:88px;margin-top:13px;float:left;">
	    			<div>
			    		<div style="width:60px;float:left;"><fmt:message key="funds_symbol" /></div>
			    		<div style="width:210px;float:left;"><input id="search-symbol" style="width:170px;" value="" onkeydown='if(event.keyCode==13) $("#button-search").trigger("click");'/></div>
			    		<div style="width:40px;float:left;"><fmt:message key="funds_sort" /></div>
			    		<div style="width:290px;float:left;">
							<select id="search-sorttype" style="width:280px;">
								<option value="115"><fmt:message key="funds_sort_115" /></option>
								<option value="111"><fmt:message key="funds_sort_111" /></option>
								<option value="112"><fmt:message key="funds_sort_112" /></option>
								<option value="113"><fmt:message key="funds_sort_113" /></option>
								<option value="114"><fmt:message key="funds_sort_114" /></option>
								<option value="424"><fmt:message key="funds_sort_424" /></option>
								<option value="425"><fmt:message key="funds_sort_425" /></option>
								<option value="426"><fmt:message key="funds_sort_426" /></option>
								<option value="423"><fmt:message key="funds_sort_423" /></option>
								<option value="421"><fmt:message key="funds_sort_421" /></option>
								<option value="422"><fmt:message key="funds_sort_422" /></option>
								<option value="452"><fmt:message key="funds_sort_452" /></option>
								<option value="451"><fmt:message key="funds_sort_451" /></option>
								<option value="414"><fmt:message key="funds_sort_414" /></option>
								<option value="415"><fmt:message key="funds_sort_415" /></option>
								<option value="416"><fmt:message key="funds_sort_416" /></option>
								<option value="413"><fmt:message key="funds_sort_413" /></option>
								<option value="411"><fmt:message key="funds_sort_411" /></option>
								<option value="412"><fmt:message key="funds_sort_412" /></option>
								<option value="442"><fmt:message key="funds_sort_442" /></option>
								<option value="441"><fmt:message key="funds_sort_441" /></option>
								<option value="341"><fmt:message key="funds_sort_341" /></option>
								<option value="211"><fmt:message key="funds_sort_211" /></option>
								<option value="212"><fmt:message key="funds_sort_212" /></option>
								<option value="213"><fmt:message key="funds_sort_213" /></option>
								<option value="241"><fmt:message key="funds_sort_241" /></option>
								<option value="242"><fmt:message key="funds_sort_242" /></option>
								<option value="243"><fmt:message key="funds_sort_243" /></option>
								<option value="244"><fmt:message key="funds_sort_244" /></option>
								<option value="245"><fmt:message key="funds_sort_245" /></option>
								<option value="406"><fmt:message key="funds_sort_406" /></option>
								<option value="331"><fmt:message key="funds_sort_331" /></option>
								<option value="201"><fmt:message key="funds_sort_201" /></option>
								<option value="202"><fmt:message key="funds_sort_202" /></option>
								<option value="203"><fmt:message key="funds_sort_203" /></option>
							</select>
			    		</div>
			    		<div style="width:10px;float:left;">&nbsp;&nbsp;</div>
			    		<div style="width:90px;float:left;">
							<select id="search-sort" style="width:80px;">
								<option value="desc"><fmt:message key="funds_sort_desc" /></option>
								<option value="asc"><fmt:message key="funds_sort_asc" /></option>
							</select>
			    		</div>
			    		<div class="clear"></div>	
		    		</div> 		
	    		</div>
            	<div id="button-search" style="margin-top:10px;cursor:pointer;margin-right:20px;text-align:center;background-color:#7BBFE6;height:24px;line-height:24px;width:100px;float:right;"><i class="icon-search icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_search" /><c:if test="${sessionScope.locale == 'en_US'}">&nbsp;</c:if></div>
	    		<div class="clear"></div>
	    	</div>
        </div>
        <div style="width:100%;margin-top:1px;height:35px;background-color:#e0eecb;">
            <div id="thead" style="margin-top:5px;color:#859b52;font-weight:bold;margin-left:20px;height:24px;line-height:24px;width:400px;float:left;"><fmt:message key="label_total" />: <span id="search-count"></span>&nbsp;&nbsp;&nbsp;&nbsp;<fmt:message key="label_records" /> ${result.maxRecords} <fmt:message key="label_records2" /></div>
            <div id="button-export-xml" class="button" style="margin-top:5px;margin-right:20px;margin-left:0px;background-color:#7BBFE6;height:24px;line-height:24px;width:100px;float:right;"><i class="icon-file icon-large"></i>&nbsp;&nbsp;<fmt:message key="funds_export_xml" />&nbsp;&nbsp;</div>
            <div id="button-export-ebk" class="button" style="margin-top:5px;margin-right:20px;margin-left:0px;background-color:#7BBFE6;height:24px;line-height:24px;width:100px;float:right;"><i class="icon-file-text icon-large"></i>&nbsp;&nbsp;<fmt:message key="funds_export_ebk" /></div>
            <div class="clear"></div>
        </div>
        <div style="position:relative;margin:0px;margin-top:1px;width:100%">
            <div id="mediaListTable" class="scroller_table" style="width:100%;">
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
                    <div class="dragger_container" style="height:460px;">
                        <div class="dragger"></div>
                    </div>
                </div>
            </div>
            <div class="clear"></div>
        </div>
	    <form id="searchForm" method="post">
			<input id="symbol" name="symbol" type="hidden"/>
			<input id="sortType" name="sortType" type="hidden"/>
			<input id="sort" name="sort" type="hidden"/>
		</form>
    </div>
    <div id="mediaDetail" class="media_info" style="display:none;width:945px;margin-left:50px;">
        <div class="button-return button" style="margin-top:12px;margin-left:828px;height:24px;line-height:24px;"><i class="icon-reply icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_return" /></div>
    	<div style="margin-left:320px;margin-top:-8px;font-weight:bold;font-size:18px;"><fmt:message key="funds_chart_info" /></div>    
    	<div style="margin-left:320px;margin-top:4px;">
    		<div id="chart_base" style="float:left;"></div><div class="clear"></div>
    	</div>
        <div id="detailListTable" class="scroller_table" style="position:relative;margin:0px;margin-top:10px;width:945px;height:460px;">
            <div class="customScrollBox" style="width:100%;height:100%;position:relative;">
                <div class="container" style="width:100%;">
                    <div id="detail-content" class="content" style="width:100%;">
			    		<div style="margin-left:0px;margin-top:8px;width:480px;float:left;">
					    	<div id="chart_details" style="margin-left:0px;margin-top:12px;"></div>
							<div style="margin-left:0px;margin-top:30px;">
								<div id="chart_net" style="width:468px;height:240px;margin-top:10px;"></div> 
								<div id="chart_net_ratio" style="width:468px;height:240px;margin-top:10px;margin-left:20px;"></div> 
								<div id="chart_net_min" style="width:468px;height:240px;margin-top:10px;margin-left:20px;"></div>
							</div>
			    		</div>
			    		<div id="chart_imgs" style="margin-left:10px;margin-top:0px;width:428px;float:left;"></div>
			    		<div class="clear"></div>
                    </div>
                </div>
                <div class="dragger_container" style="height:460px;">
                    <div class="dragger"></div>
                </div>
            </div>
        </div>
    </div>
    <div id="mediaEdit" class="media_info" style="display:none;width:945px;margin-left:50px;">
        <div class="button-return button" style="margin-top:12px;margin-left:828px;height:24px;line-height:24px;"><i class="icon-reply icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_return" /></div>
    	<div style="margin-left:320px;margin-top:-8px;font-weight:bold;font-size:18px;"><fmt:message key="funds_edit_info" /></div>    
    	<div style="margin-left:320px;margin-top:4px;">
    		<div id="edit_base" style="float:left;"></div><div id="edit_codeB" style="float:left;display:none;"></div><div class="clear"></div>
    	</div>
        <div id="editListTable" class="scroller_table" style="position:relative;margin:0px;margin-top:10px;width:945px;height:460px;">
            <div class="customScrollBox" style="width:100%;height:100%;position:relative;">
                <div class="container" style="width:100%;">
                    <div id="edit-content" class="content" style="width:100%;">
				    	<div id="symbol_details" style="margin-left:0px;margin-top:12px;"></div>
				    	<div style="margin-left:0px;margin-top:12px;">
				    		<div id="funds_symbol_edit" class="button" style="margin-top:-2px;margin-left:0px;background-color:#7BBFE6;width:100px;height:24px;line-height:24px;float:left;"><i class="icon-edit-sign icon-large"></i>&nbsp;&nbsp;&nbsp;&nbsp;<fmt:message key="funds_edit" /></div>
            				<div class="clear"></div>
				    	</div>
				    	<div id="symbol_edits" style="margin-left:0px;margin-top:12px;display:none">
							<div style="margin-top:10px;">
								<div style="width:78px;float:left;font-size:12px;"><fmt:message key="funds_symbol_code" /></div>
								<div style="width:215px;float:left;font-size:12px;"><fmt:message key="funds_symbol_name" /></div>
								<div style="width:76px;float:left;font-size:12px;"><fmt:message key="funds_symbol_name_s" /></div>
								<div style="width:66px;float:left;font-size:12px;"><fmt:message key="funds_symbol_market" /></div>
								<div style="width:76px;float:left;font-size:12px;"><fmt:message key="funds_symbol_equity" /></div>
								<div style="width:116px;float:left;font-size:12px;"><fmt:message key="funds_symbol_manager" /></div>
								<div style="width:96px;float:left;font-size:12px;"><fmt:message key="funds_symbol_start" /></div>
								<div style="width:96px;float:left;font-size:12px;"><fmt:message key="funds_symbol_end" /></div>
								<div id="funds_symbol_update" class="button" style="margin-top:-2px;margin-left:8px;width:100px;height:24px;line-height:24px;float:left;"><i class="icon-plus icon-large"></i>&nbsp;&nbsp;<fmt:message key="funds_symbol_update" /></div>
    							<div style="float:left;">&nbsp;&nbsp;</div>
								<div class="clear"></div>
							</div>
							<div id="symbol_edits_content"></div>
							<div style="margin-top:15px;width:100%;height:0px;border:1px solid #e3e3ef;"></div>
						</div>
				    	<div id="quote_details" style="margin-left:0px;margin-top:12px;"></div>
				    	<div style="margin-left:0px;margin-top:12px;">
				    		<div style="width:90px;float:left;"><input id="funds_quote_date" style="width:72px;"/></div>
				    		<div id="funds_quote_edit" class="button" style="margin-top:-2px;margin-left:0px;background-color:#7BBFE6;width:100px;height:24px;line-height:24px;float:left;"><i class="icon-edit-sign icon-large"></i>&nbsp;&nbsp;&nbsp;&nbsp;<fmt:message key="funds_edit" /></div>
            				<div class="clear"></div>
				    	</div>
				    	<div id="quote_edits" style="margin-left:0px;margin-top:12px;display:none">
							<div style="margin-top:10px;">
								<div style="width:78px;float:left;font-size:12px;"><fmt:message key="funds_quote_code" /></div>
								<div style="width:95px;float:left;font-size:12px;"><fmt:message key="funds_quote_date" /></div>
								<div style="width:96px;float:left;font-size:12px;"><fmt:message key="funds_quote_time" /></div>
								<div style="width:76px;float:left;font-size:12px;"><fmt:message key="funds_quote_price" /></div>
								<div style="width:76px;float:left;font-size:12px;"><fmt:message key="funds_quote_close_prev" /></div>
								<div style="width:76px;float:left;font-size:12px;"><fmt:message key="funds_quote_open" /></div>
								<div style="width:76px;float:left;font-size:12px;"><fmt:message key="funds_quote_high" /></div>
								<div style="width:76px;float:left;font-size:12px;"><fmt:message key="funds_quote_low" /></div>
								<div style="width:116px;float:left;font-size:12px;"><fmt:message key="funds_quote_volume" /></div>
								<div style="width:116px;float:left;font-size:12px;"><fmt:message key="funds_quote_amount" /></div>
								<div style="float:left;">&nbsp;&nbsp;</div>
								<div class="clear"></div>
							</div>
							<div id="quote_edits_content"></div>
							<div style="margin-top:15px;width:100%;height:0px;border:1px solid #e3e3ef;"></div>
						</div>
				    	<div id="net_details" style="margin-left:0px;margin-top:12px;"></div>
				    	<div style="margin-left:0px;margin-top:12px;">
				    		<div style="width:90px;float:left;"><input id="funds_net_date" style="width:72px;"/></div>
				    		<div id="funds_net_edit" class="button" style="margin-top:-2px;margin-left:0px;background-color:#7BBFE6;width:100px;height:24px;line-height:24px;float:left;"><i class="icon-edit-sign icon-large"></i>&nbsp;&nbsp;&nbsp;&nbsp;<fmt:message key="funds_edit" /></div>
            				<div class="clear"></div>
				    	</div>
				    	<div id="net_edits" style="margin-left:0px;margin-top:12px;display:none">
							<div style="margin-top:10px;">
								<div style="width:78px;float:left;font-size:12px;"><fmt:message key="funds_net_code" /></div>
								<div style="width:95px;float:left;font-size:12px;"><fmt:message key="funds_net_date" /></div>
								<div style="width:96px;float:left;font-size:12px;"><fmt:message key="funds_net_net" /></div>
								<div style="width:96px;float:left;font-size:12px;"><fmt:message key="funds_net_net_total" /></div>
								<div style="width:96px;float:left;font-size:12px;"><fmt:message key="funds_net_growth" /></div>
								<div style="float:left;">&nbsp;&nbsp;</div>
								<div class="clear"></div>
							</div>
							<div id="net_edits_content"></div>
							<div style="margin-top:15px;width:100%;height:0px;border:1px solid #e3e3ef;"></div>
						</div>
                    </div>
                </div>
                <div class="dragger_container" style="height:460px;">
                    <div class="dragger"></div>
                </div>
            </div>
        </div>
    </div>    
</div>
<div class="clear"></div>
</tags:template>
