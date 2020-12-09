<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ page language="java" contentType="text/xml" pageEncoding="UTF-8" %><?xml version="1.0" encoding="UTF-8"?>
<result>
	<code>${requestScope.returnData.result}</code>
	<c:choose><c:when test="${requestScope.returnData.bodyXml!=null}">${requestScope.returnData.bodyXml}</c:when><c:otherwise>
	<data><c:forEach var="entry" items="${requestScope.returnData}">
		<c:if test="${entry.key != 'result'}"><${entry.key}><![CDATA[<c:out value="${entry.value}" escapeXml="false" />]]></${entry.key}></c:if></c:forEach>
	</data></c:otherwise></c:choose>
</result>
