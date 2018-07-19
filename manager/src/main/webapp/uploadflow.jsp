<%-- 
    Document   : uploadflow
    Created on : 10-Jul-2018, 10:11:03
    Author     : hhiden
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <script type="text/javascript" src="js/jquery.js"></script>
    </head>
    <body>
        <h1>Upload flow JSON</h1>
        <textarea id="flowEditor" style="width:100%;height:200px;"></textarea>
        <button onclick="sendFlow()">Upload</button>        
    </body>
    
    <script type="text/javascript">
        function sendFlow(){
            var json = document.getElementById("flowEditor").value;
            var promise = $.ajax({
                url: "rest/api/flows",
                type: 'POST',
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                data: json
            }).then(function(){
                alert("Done");
            });



        }        
    </script>
</html>
