<%-- 
    Document   : edit
    Created on : 23-Jul-2018, 11:09:43
    Author     : hhiden
--%>

<%@page contentType="text/html" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>JSP Page</title>
    <!--script type="text/javascript" src="blocks/jquery.js"></script-->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.min.js"></script>
    <script src="http://code.jquery.com/jquery-migrate-1.4.1.js"></script>
    <script type="text/javascript" src="blocks/jquery.json.min.js"></script>
    <script type="text/javascript" src="blocks/jquery.mousewheel.min.js"></script>
    <script type="text/javascript" src="blocks/jquery.svg.min.js"></script>
    <script type="text/javascript" src="blocks/jquery.formserialize.min.js"></script>
    <script type="text/javascript" src="blocks/jquery.fancybox.min.js"></script>
    <link rel="stylesheet" type="text/css" href="blocks/fancybox/jquery.fancybox.css"/>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.3.7/js/bootstrap.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/patternfly/3.24.0/js/patternfly.min.js"></script>

    <!-- blocks.js -->
    <script type="text/javascript" src="blocks/blocks.js"></script>
    <link rel="stylesheet" type="text/css"
          href="https://cdnjs.cloudflare.com/ajax/libs/patternfly/3.24.0/css/patternfly.min.css">
    <link rel="stylesheet" type="text/css"
          href="https://cdnjs.cloudflare.com/ajax/libs/patternfly/3.24.0/css/patternfly-additions.min.css">

    <link rel="stylesheet" type="text/css" href="blocks/blocks.css"/>
    <link rel="stylesheet" type="text/css" href="css/style.css"/>

    <script type="text/javascript" src="js/yaml.js"></script>
    <script type="text/javascript" src="js/ui.js"></script>

</head>
<body>
<div>
    <label for="deploymentName">Flow Name</label>
    <input type="text" size="30" id="deploymentName"/>
    <button onclick="submit()" id="submitFlow">Submit</button>
    <p id="submit-response"></p>
</div>
<div id="blocks">

</div>

</body>

<script type="text/javascript">
    function submit() {
        var flowName = document.getElementById("deploymentName").value;
        var json = exportJson(flowName);

        var promise = $.ajax({
            url: "rest/api/flows",
            type: 'POST',
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            data: json
        }).then(function () {
            var elem = $("#submit-response");
            elem.show();
            elem.html("Submitted dataflow: " + flowName);
            elem.fadeOut(5000);
        });

    }
</script>
</html>
