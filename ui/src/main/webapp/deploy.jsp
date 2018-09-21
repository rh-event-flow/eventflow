<%-- 
    Document   : deploy
    Created on : 09-Jul-2018, 14:22:40
    Author     : hhiden
--%>

<%@page import="io.streamzi.eventflow.ProcessorFlowManager"%>
<%@page import="javax.naming.InitialContext"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <%
            String filename = request.getParameter("name");
            InitialContext ic = new InitialContext();
            ProcessorFlowManager mgr =(ProcessorFlowManager) ic.lookup("java:module/ProcessorFlowManagerBean");            
        %>
    </head>
    <body>
        <h1>Deploying</h1>
        <%=filename%>
        <%mgr.deployProcessor(filename);%>
    </body>
</html>
