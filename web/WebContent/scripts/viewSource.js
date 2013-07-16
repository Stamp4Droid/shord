var ViewSource = (function ()  
{
    var module = {}

    function hashtocolor(num)
    {
        num = ((num >> 16) ^ num) * 0x45d9f3b;
        num = ((num >> 16) ^ num) * 0x45d9f3b;
        num = ((num >> 16) ^ num);
        return Math.abs(num % 16777215).toString(16);
    }
    
    function prettyprintParam(param, isPopover)
    {
        if(isPopover) {
            var l = param.split(".");
            param = l[l.length-1];
        }
        l = param.split(":");
        if(l.length == 1) {
            if(param == "NULL_TYPE") {
                param = "null";
            } else if(param.substr(param.length-1,param.length) == "\"" &&
                      param.substr(0,1) != "\"") {
                param = "\"" + param;
            }
            return param;
        }
        if(l[1] == 0) return "null"
        color = hashtocolor(l[1]);
        var span = "<span style=\"color:#" + color + ";\">" + l[0] + "<span>";
        if(isPopover) {
            return span;
        } else {
            return "<a class=\"droidrecord-obj-param-link\" title=\"Object ID: "+l[1]+"\" " +
                   "style=\"color:#" + color + ";\">" + span + "</a>";
        }
    }
    
    // 'Public' function
    module.droidrecordDataToTable = function(data, isPopover)
    {
        if(data.parameterValues.length == 0) return "";
        else {
            numArgs = data.parameterValues[0].length;
        }
        var html = "<table class=\"droidrecord-parameter-info-table ";
        if(isPopover) html += "droidrecord-parameter-info-table-popover";
        else html += "droidrecord-parameter-info-table-rightbar";
        html += "\">";
        html += "<tr>";
        columnCounter = 0;
        if(numArgs == (data.parameterTypes.length + 1)) {
            if(numArgs == 1) {
                html += "<th class=\"table-leftmost table-rightmost\">this</th>";
            } else {
                html += "<th class=\"table-leftmost\">this</th>";
            }
            columnCounter++;
        }
        for(ptype in data.parameterTypes) {
            htmlClass = ""
            if(columnCounter == 0) {
                htmlClass += "table-leftmost ";
            } 
            if(columnCounter == (numArgs-1)) {
                htmlClass += "table-rightmost ";
            }
            if(htmlClass != "") {
                html += "<th class=\""+htmlClass+"\">";
            } else {
                html += "<th>";
            }
            columnCounter++;
            html += prettyprintParam(data.parameterTypes[ptype], isPopover) + "</th>";
        }
        html += "</tr>";
        for(pvals in data.parameterValues) {
            html += "<tr>";
            columnCounter = 0;
            for(pval in data.parameterValues[pvals]) {
                htmlClass = "";
                if(columnCounter == 0) {
                    htmlClass += "table-leftmost ";
                } 
                if(columnCounter == (numArgs-1)) {
                    htmlClass += "table-rightmost ";
                }
                if(htmlClass != "") {
                    html += "<td class=\""+htmlClass+"\">";
                } else {
                    html += "<td>";
                }
                columnCounter++;
                html += prettyprintParam(data.parameterValues[pvals][pval], isPopover) + "</td>";
            }
            html += "</tr>";
        }
        html += "</table>";
        //console.log(html)
        return html;
    }
    
    return module;
}());  
