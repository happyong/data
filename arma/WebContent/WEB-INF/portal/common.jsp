<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%><%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%><%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%><%@ taglib prefix="tags" tagdir="/WEB-INF/tags"%><%
   request.setAttribute("static",request.getContextPath());
   request.setAttribute("context",request.getContextPath());
%>
<fmt:setLocale value="${sessionScope.locale}" scope="session" />
<fmt:setBundle basename="i18n.portal" />