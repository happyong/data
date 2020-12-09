<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java"%>
<%@ include file="common.jsp"%>
<tags:template className="mediaPage">
<script type="text/javascript" src="${static}/static/highcharts.js" charset="utf-8"></script>
<script type="text/javascript" src="${static}/static/datepicker/WdatePicker.js"></script>
<div style="display:none">${result.jscharts}</div>
<script>
var curdetid = "", curpage = "-1", portals='${result.portals}';

var taskChart, scheduleChart, mediaChart, colors;
var canTimer = Boolean(${result.canTimer});

$(function() {	
	menus('${result.menus}');
	var admin = Boolean(${login_user.admin}), schedule = true, task = true;
	var bassettools = ((admin || task) && show("AssetTool")), bshelltools = (admin && show("ShellTool")), buploaders = (admin && show("UploadServer")), bencoders = ((admin || schedule) && show("NeuEncoder")), bdemuxers = ((admin || schedule) && show("StreamDemuxer")), bservers = ((admin || schedule) && show("StreamServer"));
	var btask = ((admin || task) && show("tasks")), bschedule = ((admin || schedule) && show("lives")), bmedia = ((admin || schedule || task) && (bassettools || bshelltools || buploaders || bencoders || bdemuxers || bservers));
	var w1 = "432px", w2 = "912px", h1 = "220px", h2 = "450px";
	var wtask = (bschedule && bmedia ? w1 : w2), wschedule = (bmedia ? w1 : w2), wmedia = (bschedule ? w1 : w2);
	var htask = (bschedule || bmedia ? h1 : h2), hschedule = (bmedia || btask ? h1 : h2), hmedia = (bschedule || btask ? h1 : h2);
	// highchart options
	Highcharts.setOptions({global: {useUTC: false}});
	colors = Highcharts.getOptions().colors;
	// task chart
	taskChart = chartNew("task", btask, wtask, htask, msg("label_chart_task"));
	taskChart.addSeries({name: msg("label_chart_tasknews"), color: colors[0]}, false);
	taskChart.addSeries({name: msg("label_chart_taskredos"), color: colors[6]}, false);
	taskChart.addSeries({name: msg("label_chart_taskdones"), color: colors[2]}, false);
	taskChart.addSeries({name: msg("label_chart_taskfails"), color: colors[3]}, false);
	taskChart.addSeries({name: msg("label_chart_taskcancels"), color: colors[1]}, false);
	// schedule chart
	scheduleChart = chartNew("schedule", bschedule, wschedule, hschedule, msg("label_chart_live"));
	scheduleChart.addSeries({name: msg("label_chart_livedones"), color: colors[2]}, false);
	scheduleChart.addSeries({name: msg("label_chart_livefails"), color: colors[3]}, false);
	scheduleChart.addSeries({name: msg("label_chart_liveresets"), color: colors[1]}, false);
	// media chart
	mediaChart = chartNew("media", bmedia, wmedia, hmedia, msg("label_chart_media"));
	mediaChart.addSeries({showInLegend: bassettools, visible: bassettools, name: msg("label_chart_assettools"), color: colors[3]}, false);
	mediaChart.addSeries({showInLegend: bshelltools, visible: bshelltools, name: msg("label_chart_shelltools"), color: colors[1]}, false);
	mediaChart.addSeries({showInLegend: buploaders, visible: buploaders, name: msg("label_chart_uploaders"), color: colors[6]}, false);
	mediaChart.addSeries({showInLegend: bencoders, visible: bencoders, name: msg("label_chart_encoders"), color: colors[7]}, false);
	mediaChart.addSeries({showInLegend: bdemuxers, visible: bdemuxers, name: msg("label_chart_demuxers"), color: colors[2]}, false);
	mediaChart.addSeries({showInLegend: bservers, visible: bservers, name: msg("label_chart_servers"), color: colors[0]}, false);
	
	$("#button-search").click(function() {
		dataInit();
	});
	$("#button-now").click(function() {
		$("#search-endtime").val(new Date().Format("yyyy-MM-dd hh:mm"));
		dataInit();
	});
	if (canTimer) $("#button-now").trigger("click");
});

function chartNew(id, bchart, wchart, hchart, title) {
	return $("#container_" + id).css('display', (bchart ? '' : 'none')).css('width', wchart).css('height', hchart).empty().highcharts({
		chart: {type: 'spline', animation: Highcharts.svg, marginRight: 10},
		title: {text: title},
		xAxis: {type: 'datetime', tickPixelInterval: 100},
		yAxis: {
			title: {text: msg("label_chart_count")},
			plotLines: [{value: 0, width: 1, color: '#808080'}]
		},
		tooltip: {
			formatter: function() {
				var s = '<b><span style="color:' + colors[1] + '">'+ Highcharts.dateFormat('%Y-%m-%d %H:%M', this.x) +'</span></b>';
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

var chartEnd, chartInterval, chartData, maxSize;
function dataInit() {	
	var end = endtime();
	if (!end) {
		alert(msg("search_endtime_tip"));
		return;
	}
	if (searching) {
	   	alert(msg("search_time_tip"));
		return;
	}
	
	search(true);
	searchTimer = setTimeout("search(false)",1000);
	var duration = $("#search-duration").val();
	var end_time = new Date(parse(0, 4, end), (parse(4, 6, end) -1), parse(6, 8, end), parse(8, 10, end), parse(10, 12, end), 59).getTime();
	$.post("${context}/servlets/activity/chart", {"endtime":end_time,"duration":duration,"format":"json"}, function(data) {
		if (data.result) {
			var shots = data.shots;
			if (!shots || shots.length == 0) {
				var info = "no data\nclient time: " + new Date().Format("yyyy-MM-dd hh:mm:ss.S");
				if (data.now) info += "\nserver time: " + data.now;
				alert(info);
				return;
			}
			
			chartData = [];
			maxSize = duration;
			for (var i = 0; i < 14; i++) chartData.push(new Array());
			clearInterval(chartInterval);
			
			updateEndTime(shots[shots.length - 1].shotDate);
			var time = shots[0].shotDate, size = maxSize - shots.length;
			add(time - 60000, size, shots[0], shots);
			for (var i = 0; i < shots.length; i++) push(shots[i]);
			onChartPageChanged("detail");
			redraw();
			
			chartEnd = end_time;
			chartInterval = setInterval(dataAppend, 60000);			
		} else {
			search(false);
			var error = msg("search_fail");
			if (data.error && data.error.value) error += "\n" + msg("fail_reason") + data.error.value;
			alert(error);
		}
	},"json");
}

function dataAppend() {
	var date = new Date();
	if (chartEnd && chartEnd > 1) {
		chartEnd += 60000;
		date.setTime(chartEnd);
	}
	$.post("${context}/servlets/activity/chart", {"endtime":date.getTime(),"duration":"3","format":"json"}, function(data) {
		if (data.result) {
			var shots = data.shots;
			if (!shots || shots.length == 0) {
				var info = "no data\nclient time: " + new Date().Format("yyyy-MM-dd hh:mm:ss.S");
				if (data.now) info += "\nserver time: " + data.now;
				alert(info);
				return;
			}
			if (!chartData) return;
			
			updateEndTime(shots[shots.length - 1].shotDate);
			var time = shots[0].shotDate, len = chartData[8].length - 1, c8 = chartData[8][len], c9 = chartData[9][len], c10 = chartData[10][len], c11 = chartData[11][len], c12 = chartData[12][len], c13 = chartData[13][len];
			if (c8.x + 60000 <= time) {
				var size = ((time - c8.x) / 60000 - 1);
				var shot = {"assettools":c8.y,"shelltools":c9.y,"uploaders":c10.y,"encoders":c11.y,"demuxers":c12.y,"servers":c13.y};
				add(time - 60000, size, shot, shots);
			} else {
				var deta = ((c8.x - time) / 60000 + 1);
				for (var i = 0; i < 14; i++) chartData[i].splice(len + 1 - deta, deta);
			}
			for (var i = 0; i < shots.length; i++) push(shots[i]);
			redraw();
		} else {
			var error = msg("search_fail");
			if (data.error && data.error.value) error += "\n" + msg("fail_reason") + data.error.value;
			alert(error);
		}
	},"json");
}

function updateEndTime(time) {
	var date = new Date();
	date.setTime(time);
	var nt = date.Format("yyyy-MM-dd hh:mm"), ot = $("#search-endtime").val();
	if (!ot || nt.localeCompare(ot) > 0) $("#search-endtime").val(nt);
}

function add(time, size, shot, shots) {
	for (var i = 0; i < size; i++) {
		shots.unshift({"shotDate":(time - i * 60000),"tasknews":0,"taskredos":0,"taskdones":0,"taskfails":0,"taskcancels":0,
			"livedones":0,"livefails":0,"liveresets":0,"assettools":shot.assettools,"shelltools":shot.shelltools,
			"uploaders":shot.uploaders,"encoders":shot.encoders,"demuxers":shot.demuxers,"servers":shot.servers});
	}
}

function push(shot) {
	time = shot.shotDate;
	chartData[0].push({x:time, y:shot.tasknews});
	chartData[1].push({x:time, y:shot.taskredos});
	chartData[2].push({x:time, y:shot.taskdones});
	chartData[3].push({x:time, y:shot.taskfails});
	chartData[4].push({x:time, y:shot.taskcancels});
	chartData[5].push({x:time, y:shot.livedones});
	chartData[6].push({x:time, y:shot.livefails});
	chartData[7].push({x:time, y:shot.liveresets});
	chartData[8].push({x:time, y:shot.assettools});
	chartData[9].push({x:time, y:shot.shelltools});
	chartData[10].push({x:time, y:shot.uploaders});
	chartData[11].push({x:time, y:shot.encoders});
	chartData[12].push({x:time, y:shot.demuxers});
	chartData[13].push({x:time, y:shot.servers});
}

function redraw() {
	var len = chartData[0].length, deta = len - maxSize;
	if (deta > 0)
		for (var i = 0; i < 14; i++) 
			chartData[i] = chartData[i].splice(deta, len);
	for (var i = 0; i < 5; i++) taskChart.series[i].setData(chartData[i]);
	for (var i = 0; i < 3; i++) scheduleChart.series[i].setData(chartData[i + 5]);
	for (var i = 0; i < 6; i++) mediaChart.series[i].setData(chartData[i + 8]);
	taskChart.redraw();
	scheduleChart.redraw();
	mediaChart.redraw();
}

function parse(start, end, str) {
	return parseInt(str.substring(start, end), 10);
}

function endtime() {
	var end_time = $("#search-endtime").val();
	end_time = end_time.replace(/-/g, "");
	end_time = end_time.replace(/ /g, "");
	end_time = end_time.replace(/:/g, "");
	if (!end_time || end_time.length != 12) return "";
	var now = new Date();
	var end = now.Format("yyyyMMddhhmm");
	now.setTime(now.getTime() - 259200000);
	var start = now.Format("yyyyMMddhhmm");
	if (end_time.localeCompare(end) <= 0 && end_time.localeCompare(start) > 0) return end_time;
	return "";
}

function onChartPageChanged(page) {
	var id = "chart-" + page;
	$(".chart-detail").each(function(index, element) {
		$(this).css('display',(id==$(this).attr("id") ? '' : 'none'));
	});
}

var searchTimer, searching = false;
function search(flag) {
	searching = flag;
	$("#button-search").css("cursor", (flag ? "default" : "pointer"));
	$("#button-now").css("cursor", (flag ? "default" : "pointer"));
	if (!flag) clearTimeout(searchTimer);
}
</script>
  
<div class="main ui-widget-content">
    <div style="margin-left:0px;height:40px;margin-bottom:0px;margin-top:0px;background-color:#A3D4F2">
        <div class="thumb_button" style="background-color:#7bbfe6;" title="<fmt:message key="label_charts" />"><fmt:message key="label_charts" /></div>
        <div class="button" id="button-refresh" style="margin-top:8px;margin-right:20px;background-color:#7bbfe6;height:24px;line-height:24px;float:right;display:none;" title="<fmt:message key="button_refresh" />"><i class="icon-refresh icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_refresh" /></div>
        <div class="clear"></div>
    </div>
    <div id="chartInfo" class="media_info">
        <div style="width:100%;height:48px;background-color:#e6e6e6;">
	    	<div style="margin-left:20px;">
	    		<div class="search-icon" style="margin-left:164px;margin-top:10px;">&nbsp;</div>
    			<div style="margin-left:40px;margin-top:10px;float:left;">
					<div style="width:68px;margin-top:3px;float:left;"><fmt:message key="charts_endtime" /></div>
					<div style="width:186px;float:left;"><input id="search-endtime" type="text" class="Wdate" style="width:136px;" onClick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm'})"/></div>
					<div style="width:68px;margin-top:3px;float:left;"><fmt:message key="charts_duration" /></div>
					<div style="width:170px;float:left;">
						<select id="search-duration" style="width:120px;">
							<option value="5"><fmt:message key="charts_duration_5" /></option>
							<option value="10"><fmt:message key="charts_duration_10" /></option>
							<option value="15" selected="selected"><fmt:message key="charts_duration_15" /></option>
							<option value="30"><fmt:message key="charts_duration_30" /></option>
							<option value="60"><fmt:message key="charts_duration_60" /></option>
							<option value="90"><fmt:message key="charts_duration_90" /></option>
							<option value="120"><fmt:message key="charts_duration_120" /></option>
						</select>
					</div>
		    		<div class="clear"></div>
	    		</div>  
	    		<div id="button-now" style="margin-top:10px;cursor:pointer;margin-right:20px;text-align:center;background-color:#7BBFE6;height:24px;line-height:24px;width:100px;float:right;"><i class="icon-search icon-large"></i>&nbsp;&nbsp;<fmt:message key="charts_button_now" /><c:if test="${sessionScope.locale == 'en_US'}">&nbsp;&nbsp;</c:if></div>
	    		<div id="button-search" style="margin-top:10px;cursor:pointer;margin-right:20px;text-align:center;background-color:#7BBFE6;height:24px;line-height:24px;width:100px;float:right;"><i class="icon-search icon-large"></i>&nbsp;&nbsp;<fmt:message key="button_search" /></div>
	    		<div class="clear"></div>
	    	</div>
        </div>
        <div id="chart-detail" class="chart-detail" style="margin-top:0px;margin-left:40px;width:100%;display:none;">
	    	<div style="margin-top:10px;">
	    		<div id="container_media" style="width:432px;height:220px;margin-top:0px;float:left;"></div> 
	    		<div id="container_schedule" style="width:432px;height:220px;margin-top:0px;float:left;margin-left:40px;"></div> 
	    		<div class="clear"></div>
	    	</div>
	    	<div id="container_task" style="width:432px;height:220px;margin-top:30px;"></div>
        </div>	 
        <div id="chart-loading" class="chart-detail" style="margin-top:20px;margin-left:40px;">
	    	<div style="margin-top:60px;margin-left:40px;"><fmt:message key="charts_loading_tip" /></div>
        </div>
    </div>
</div>
<div class="clear"></div>
</tags:template>
