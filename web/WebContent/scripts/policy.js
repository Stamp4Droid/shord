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


    $('#add_policy_btn').click(function () {
        var src = $('#srcSelect').select('selectedItem').text;
        var srcparam = $('#src_param').val();
        var sink = $('#sinkSelect').select('selectedItem').text;
        var sinkparam = $('#sink_param').val();
        $('#src-sink-table tr:last').after('<tr><td><input type="checkbox"><i class="checkbox"></i></td><td><i class="icon-remove"></i></td><td>' + src + '</td><td>' + srcparam + '</td><td>' + sink + '</td><td>' + sinkparam + '</td></tr>');

        $('#src-sink-table .icon-remove').click(function () {
            $(this).parent().parent().remove();

        });
    });

    $('#save_policy_btn').click(function () {
        var $table = $('#src-sink-table');
        var rules = [];
        $table.find('tr').each(function () {
            var $tds = $(this).find('td');
            if ($tds.length > 0) {
                var rule = {};
                rule.src = $tds.eq(2).text();
                rule.srcparam = $tds.eq(3).text();
                rule.sink = $tds.eq(4).text();
                rule.sinkparam = $tds.eq(5).text();
                rules.push(rule);
            }
        });
        console.log(rules);
    });

    $('#src-sink-table .icon-remove').click(function () {
        $(this).parent().parent().remove();

    });
});