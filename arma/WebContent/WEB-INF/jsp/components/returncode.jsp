<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %><%@ page language="java" contentType="text/xml" pageEncoding="UTF-8" %><?xml version="1.0" encoding="UTF-8"?>
<result>
	<c:if test="${requestScope.returnCode!=null}">
		<code>${requestScope.returnCode}</code>
	</c:if>
	<c:if test="${requestScope.returnData!=null}">
		<data>
			<c:forEach var="entry" items="${requestScope.returnData}">
			<${entry.key}><![CDATA[<c:out value="${entry.value}" escapeXml="false" />]]></${entry.key}>
			</c:forEach>
		</data>
	</c:if>
</result>
