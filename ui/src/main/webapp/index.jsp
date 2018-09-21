<%-- 
    Document   : index
    Created on : 29-Aug-2018, 10:04:19
    Author     : hhiden
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
        <script src="http://code.jquery.com/jquery-migrate-1.4.1.js"></script>        
        <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/patternfly/3.24.0/css/patternfly.min.css">
        <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/patternfly/3.24.0/css/patternfly-additions.min.css">        
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <a href="editor.jsp">Create new flow</a>
        
        <h1>Flows</h1>
        <div id="flows"></div>
        

    </body>
    
    <script type="text/javascript">            
        $( document ).ready(function() {

            var promise = $.ajax({
                url: "rest/api/dataflows",
                type: 'GET',
                dataType: "json",
                contentType: "application/json; charset=utf-8"
            }).then(function (data) {
                var html = "<ul>";
                for(var i=0;i<data.length;i++){
                    html+='<li><a href="editor.jsp?flow=' + data[i] + '">' + data[i] + '</a></li>';
                }
                html+="</ul>";
                document.getElementById("flows").innerHTML = html;
            });            
        });        

    </script>
        
</html>
