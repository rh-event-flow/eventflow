<%-- 
    Document   : listprocessors
    Created on : 09-Jul-2018, 10:55:32
    Author     : hhiden
--%>

<%@page import="java.util.List"%>
<%@page import="io.streamzi.eventflow.ProcessorFlowManager"%>
<%@page import="javax.naming.InitialContext"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <%
            
            InitialContext ic = new InitialContext();
            ProcessorFlowManager mgr =(ProcessorFlowManager) ic.lookup("java:module/ProcessorFlowManagerBean");
            List<String> names = mgr.listProcessors();
        %>
    </head>
    <body>
        <h1>Available processors</h1>
        <ul>
            <%for(String name : names){%>
            <li><%=name%><a href="deploy.jsp?name=<%=name%>">Deploy</a></li>
            <%}%>
        </ul>
    </body>
</html>
