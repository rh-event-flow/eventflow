<%-- 
    Document   : uploadyaml
    Created on : 06-Jul-2018, 12:08:43
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
        <h1>Upload a new YAML Definition</h1>
        <h2>YAML Data:</h2>

        <textarea id="yamlEditor" style="width:100%;height:200px;"></textarea>
        <button onclick="sendYaml()">Upload</button>

        
    </body>
    
    <script type="text/javascript">
        function sendYaml(){
            var yaml = document.getElementById("yamlEditor").value;
            var promise = $.ajax({
                url: "rest/api/processors",
                type: 'POST',
                dataType: "json",
                contentType: "application/json; charset=utf-8",
                data: yaml
            }).then(function(){
                alert("Done");
            });



        }
        
        
    </script>
</html>
