$(function ()  
{ 
    prettyprintParam = function(param) {
        l = param.split(".")
        param = l[l.length-1]
        l = param.split(":")
        if(l.length == 1) {
            return param;
        }
        colornum = l[1] % 50;
        return "<span class=\"popover-param-obj-color-" + colornum + "\">" + l[0] + "<span>";
    }    
    
    $(".invocationExpression").popover({ 
        trigger : "hover",
        position: "bottom", 
        html : true,
        title : function () {
            return jQuery.parseJSON(atob($(this).attr("data-droidrecord-params"))).methodName;
        }, 
        content : function() {
            data = jQuery.parseJSON(atob($(this).attr("data-droidrecord-params")))
            if(data.parameterValues.length === 0) return ""
            else {
                numArgs = data.parameterValues[0].length;
            }
            html = "<table class=\"droidrecord-parameter-info-table\">"
            html += "<tr>"
            if(numArgs === (data.parameterTypes.length + 1)) {
                html += "<th>this</th>"
            }
            for(ptype in data.parameterTypes) {
                html += "<th>" + prettyprintParam(data.parameterTypes[ptype]) + "</th>"
            }
            html += "</tr>"
            for(pvals in data.parameterValues) {
                html += "<tr>"
                for(pval in data.parameterValues[pvals]) {
                    html += "<td>" + prettyprintParam(data.parameterValues[pvals][pval]) + "</td>"
                }
                html += "</tr>"
            }
            html += "</table>"
            return html
        }
    });
});  
