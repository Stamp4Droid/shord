// Author Jason Franklin jfrankli@cs.stanford.edu


//Iterate over source sink flows and add data to DOM
function processFlows(flowXML, apkName){
    var xml = flowXML;
    xmlDoc = $.parseXML(xml),
    $xml = $(xmlDoc);
    var id = "flows" + apkName; 

    var highSeverityFlows = new Array();
    var lowSeverityFlows = new Array();

    $('#accordionFlows').find('table[id="'+id+'"]').append("<thead><tr><th>Source</th><th>Source Class</th><th>Sink</th><th>Sink Class</th></tr></thead>");


    $xml.find("tuple").each(function (highSeverityFlows, lowSeverityFlows) {
        var text = $(this).text();
        var sourceText = $(this).find("source").text().replace(/\$/g,'').toLowerCase();
        var sourceClass = $(this).find("source").attr('class');
        var sinkText = $(this).find("sink").text().replace(/\!/g,'').toLowerCase();
        var sinkClass = $(this).find("sink").attr('class');

	// Rank flows based on severity 
        if (text) {

            if (sinkClass == "offdevice" && (sourceClass == "personal data" || sourceClass == "location")) {
		//highSeverityFlows[0] = sourceText;

                $('#accordionFlows').find('table[id="'+id+'"]').prepend("<tr class=\"error\"><td>" + sourceText + "</td><td>" + sourceClass + "</td><td>" + sinkText + "</td><td>" + sinkClass + "</td></tr>"); 
            } else {
                $('#accordionFlows').find('table[id="'+id+'"]').append("<tr><td>" + sourceText +  "</td><td>" + sourceClass + "</td><td>" + sinkText + "</td><td>" + sinkClass + "</td></tr>");
            }
        }
    });

}


//Categorize and display warnings
function processWarnings(message){
    var t = message.data.split("::");
    var warnings = t[2].split("DANGER_METHOD:");
    var numOther = 0;
    var warnHash = {};

    for (var i = 0; i < warnings.length; i++) {
	if (warnings[i] && warnings[i].trim() != '')  {

            var warnTuple = warnings[i].split(";;");

            if (warnHash[warnTuple[1]]) {
		warnHash[warnTuple[1]]++;
            } else {
		warnHash[warnTuple[1]] = 1;
            }
        }
    }
    
    for (var i in warnHash) {
        $('#warnings').append("<tr><td><span class=\"label label-warning\">" 
			      + i + "</span></td><td><span class=\"badge\">" 
			      + warnHash[i] + "</span></td></tr>");		

    }
}

// Process JSON flows
function processFlowJSON(flow) {

    function newPrivTableEntry(entry) {
        return "<tr><td>"+entry.sourceLabel+"</td><td><i class=\"icon-arrow-right\"></i></td><td>"+entry.sinkLabel+"</td> <td><i  onClick=\"function(e) {debugger;}\" class=\"icon-ok\"></i></td> \ <td><i  onClick=\"function(e) {alert('hi');}\" class=\"icon-ban-circle\"></i></td> \ </tr> ";
    }

    function newTableEntry(entry) {
        return "<tr><td>"+entry.sourceLabel+"</td><td><i class=\"icon-arrow-right\"></i></td><td>"+entry.sinkLabel+"</td><td><span class=\"label label-success\">"+entry.modifier+"</span></td> \ <td><i  onClick=\"function(e) {debugger;}\" class=\"icon-ok\"></i></td> \ <td><i  onClick=\"function(e) {debugger;}\" class=\"icon-ban-circle\"></i></td> \ </tr> ";
    }

    function newTableEntryUnencrypted(entry) {
        return "<tr><td>"+entry.sourceLabel+"</td><td><i class=\"icon-arrow-right\"></i></td><td>"+entry.sinkLabel+"</td><td><span class=\"label label-important\">"+entry.modifier+"</span></td> \ <td><i  onClick=\"function(e) {debugger;}\" class=\"icon-ok\"></i></td> \ <td><i  onClick=\"function(e) {debugger;}\" class=\"icon-ban-circle\"></i></td> \ </tr> ";
    }

    console.log("begin processFlow");

    var maxC = -1;
    var apkName = "";
    var privacyCount = -1;
    var lowRiskCount = -1;
    var confCount = -1;
    $.each(flow, function(i, item) {
	if ('lowRiskCount' in item) {
	    lowRiskCount = item.lowRiskCount;
	}

	if ('privacyCount' in item) {
	    privacyCount = item.privacyCount;
	}

	if (parseInt(item.analysisCounter) > maxC) {
	    maxC = item.analysisCounter;
	    apkName = item.appName;
	}
    });

    // Report header
    var headerRow = "<th>" + apkName +  " Risk Report &nbsp <a href=\"\"><i class=\"icon-download\"></a></i></th>";
    $("#reportheader").append(headerRow);

    console.log("Max analysisCounter:" + maxC);

    // Incident counts
    $("#incident-summary").append("<th>Incidents</th><th>Risk Type</th>");
    $("#incident-summary").append("<tr><td>Privacy Risks</td></td><td>" + privacyCount + "</td></tr>");
    $("#incident-summary").append("<tr><td>Confidentiality Risks</td></td><td>" + privacyCount + "</td></tr>");
    $("#incident-summary").append("<tr><td>Low Risk</td></td><td>" + lowRiskCount + "</td></tr>");
    $("#incident-summary").append("<tr><td>Warnings</td></td><td>" + "X" + "</td></tr>");
    

    // Section headers
    $("#privacy-rpt").append("<th colspan=\"5\">Privacy - Data sent off device </th>");
    $("#conf-rpt").append("<th colspan=\"7\">Confidentiality - Encryption status of data sent off device</th>");


    if (privacyCount == 0) {
	$("#privacy-rpt").append("<th colspan=\"5\">No Privacy Risks Detected! </th>");
	$("#conf-rpt").append("<th colspan=\"5\">No Confidentiality Risks Detected! </th>");
    }

    if (lowRiskCount != 0) {
	$("#lowrisk-rpt").append("<th colspan=\"7\">Low Risk - Data accessed and remaining on device</th>")
    }
			  
    $.each(flow, function(i, item) {
        if (item.analysisCounter === maxC) {

	    var newentry;
	    if (item.modifier === "encrypted") {
		newentry = newTableEntry(item); 
	    } else {
		newentry = newTableEntryUnencrypted(item); 
	    }

            var flowC = item.flowClass;

	    if (flowC === "privacy") {
                $("#privacy-rpt").append(newPrivTableEntry(item));
                $("#conf-rpt").append(newentry);
            } else if (flowC === "integrity") {
                $("integrity-rpt").append(newentry);
            } else if (flowC === "other") {
                $("#lowrisk-rpt").append(newentry);
            } else if (flowC === "NoClass" || flowC === "") {
                // explicit no class. Treat as low-risk.
                $("#lowrisk-rpt").append(newentry);

            } else {
                // unknown flowClass. Treat as low-risk.
                $("#lowrisk-rpt").append(newentry);
                console.log("unknown flow class" + flowC);
            }

        }
    });
}   


// Process messages from server
function processMessage(message){

    function newAccordionGroup(apkName) {
        return "<div class=\"accordion-group\"><div class=\"accordion-heading\"><a class=\"accordion-toggle\" data-toggle=\"collapse\" data-parent=\"#accordionFlows\" href=\"#collapse" + apkName + "\">" + apkName + " Flows</a></div><div id=\"collapse" + apkName + "\" class=\"accordion-body collapse in\"><div class=\"accordion-inner\"><table class=\"table table-condensed table-hover \" id=\"flows" + apkName + "\"></table></div></div></div>";
    }


    if(message.data == "Hello!")
    	return;

    console.log(message.data);

    // Parse message 
    var tokens = message.data.split("::");
    var action = tokens[0];
    var tkns = tokens[1].split('.apk');
    var apkName = tkns[0] + ".apk";
    var apkId = tokens[1];
    var rowElem, labElem, flowJSON;

    // get JSON data
    if(action == "Flow") {
        flowJSON = tokens[2];

    } else {

        // Create new apk status box
        if (action == "BEGIN") {
            $('#accordionFlows').append(newAccordionGroup(apkName));
            $('#warnings').append("<tr><td colspan=\"2\"><h5>" + apkName + " Warnings</h5></td></tr>");
        }

        rowElem = apkIdToRow[apkId];
        labElem = rowElem.find('.label');
    }

    // Process based on message type. The first three simply 
    // adjust the analysis status box
    // WARN and Flow are handled more specifically 
    if(action === "BEGIN")
        labElem.addClass('label-info').text("Analyzing");
    else if(action === "END") {
        labElem.removeClass('label-info').addClass('label-success').text("Finished");
    } else if(action === "ERROR")
        labElem.removeClass('label-info').addClass('label-important').text("Error");

    else if(action === "WARN") 
        processWarnings(message);
    else if (action === "Flow") {
        // is it JSON? probably need action flag for this; we lost the flow one
        flow = $.parseJSON(flowJSON);
        processFlowJSON(flow);
    }
}

function closeConnect(){
    ws.close();
}

