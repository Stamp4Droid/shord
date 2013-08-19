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

function processFlow(flow) {
    alert("HERE");
    alert(flow.flowClass);
    alert(flow.srcName);
    alert(flow.sinkName);

    //for (flow in flows) {
        newentry = '<tr><td>"+flow.srcLabel+"</td><td><i class="icon-arrow-right"></i></td><td>"+flow.sinkLabel+"</td><td><span class="label label-important">"+flow.modifier+"</span></td> \
                    <td><i class="icon-search"></i></td> \
                    <td><i class="icon-ok"></i></td> \
                    <td><i class="icon-ban-circle"></i></td> \
                    </tr> ';

        flowC = flow.flowClass;

        if (flowC === "privacy") {
            $("#privacy-rpt").append(newentry);

        } else if (flowC === "integrity") {
            $("integrity-rpt").append(newentry);

        } else if (flowC === "other") {
            // handle other

        } else if (flowC === "") {
            // handle null 

        } else if (flowC === "location") {

            //what is location anyway?? Placeholder
            $("#privacy-rpt").append(newentry);

        } else {
            // handle something weird 
            console.log("unknown flow class" + flowC);
        }

        /*  
            // other reports in overview.html that may need adding
            } else if (flowC === "lowrisk") {

            } else if (flowC === "warn") {

            } else if (flowC === "coverage") {

            }   
        */  
    //}   
}   

// Process messages from server
function processMessage(message){
    if(message.data == "Hello!")
    	return;

    alert(message.data);

    console.log(message.data);

    // Parse message 
    var tokens = message.data.split("::");
    var action = tokens[0];
    var tkns = tokens[1].split('.apk');
    var apkName = tkns[0] + ".apk";
    var apkId = tokens[1];
    var rowElem, labElem, flowXML;

    alert(action);


    if(action == "Flow") {
        flowXML = tokens[2];
    } else {
        if (action != "WARN") {
            if (action == "BEGIN") {
                $('#accordionFlows').append("<div class=\"accordion-group\"><div class=\"accordion-heading\"><a class=\"accordion-toggle\" data-toggle=\"collapse\" data-parent=\"#accordionFlows\" href=\"#collapse" + apkName + "\">" + apkName + " Flows</a></div><div id=\"collapse" + apkName + "\" class=\"accordion-body collapse in\"><div class=\"accordion-inner\"><table class=\"table table-condensed table-hover \" id=\"flows" + apkName + "\"></table></div></div></div>");

		$('#warnings').append("<tr><td colspan=\"2\"><h5>" + apkName + " Warnings</h5></td></tr>");
            }
        }

        rowElem = apkIdToRow[apkId];
        labElem = rowElem.find('.label');
    }

    // Process based on message type
    if(action == "BEGIN")
        labElem.addClass('label-info').text("Analyzing");
    else if(action == "END") {
        labElem.removeClass('label-info').addClass('label-success').text("Finished");
        alert("WHADUUP");
        // is it JSON? probably need action flag for this; we lost the flow one
        flow = $.parseJSON(message.data);
        processFlow(flow);
    } else if(action == "ERROR")
        labElem.removeClass('label-info').addClass('label-important').text("Error");
    else if(action == "WARN") 
        processWarnings(message);
    else if (action === "Flow") {
        alert("WHADUUP");
        // is it JSON? probably need action flag for this; we lost the flow one
        flow = $.parseJSON(flowXML);
        processFlow(flow);
    }
}

function closeConnect(){
    ws.close();
}

