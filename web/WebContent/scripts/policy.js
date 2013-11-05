$(function () {
    
    'use strict';
    
    $.ajax({
        type: "GET",
        url: "/stamp/policyServlet",
        data: {
            annot: "Sources"
        },
        dataType: "xml",
        success: function (xml) {
            var seen = [];
            $(xml).find('src').each(function () {
                var d = $(this).attr("desc");
                if (jQuery.inArray(d, seen) < 0) {
                    seen.push(d);
                    $("#src-drop-down").append('<li><a href="#">' + d + '</a></li>');
                }
            });
        }
    });
    
    $.ajax({
        type: "GET",
        url: "/stamp/policyServlet",
        data: {
            annot: "Sinks"
        },
        dataType: "xml",
        success: function (xml) {
            var seen = [];
            $(xml).find('sink').each(function () {
                var d = $(this).attr("desc");
                if ($.inArray(d, seen) < 0) {
                    seen.push(d);
                    $("#sink-drop-down").append('<li><a href="#">' + d + '</a></li>');
                }
            });
        }
    });
    
    
    $('#addPolicyBtn').click(function () {
        var src = $('#srcSelect').select('selectedItem').text;
        var srcparam = $('#src_param').val();
        var sink = $('#sinkSelect').select('selectedItem').text;
        var sinkparam = $('#sink_param').val();
        $('#src-sink-table tr:last').after('<tr><td><input type="checkbox"><i class="checkbox"></i></td><td><i class="icon-remove"></i></td><td>' + src + '</td><td>' + srcparam + '</td><td>' + sink + '</td><td>' + sinkparam + '</td></tr>');
        
        $('#src-sink-table .icon-remove').click(function () {
            $(this).parent().parent().remove();
            
        });
    });
    
    $('#src-sink-table .icon-remove').click(function () {
        $(this).parent().parent().remove();
        
    });
});