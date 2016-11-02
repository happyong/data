<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

	<!-- top menu -->	
	<div class="container">
		<div class="blank"></div>
		<div id="topMenu">
			<c:forEach var="menu" items="${infos.menus}">
			<div class="fleft bold"><a href="${menu.href}">> ${menu.text}</a></div>
			<div class="fleft bold" style="width:40px;">&nbsp;</div>
			</c:forEach>
			<div class="btn fright bold small fred" style="padding:0px 20px 0px 20px;width:88px;" onclick="refresh();">Refresh<!-- &nbsp;&nbsp;(<span id="count">120</span>)--></div>
			<div class="clear"></div>
		</div>	
		<hr />
	</div>
	
	<!-- title -->
	<!-- 
	<div class="container">
		<div class="blank"></div>
		<div class="title header">${infos.title}</div>
		<div class="blank" style="height:5px;"></div>
	</div>
	 -->