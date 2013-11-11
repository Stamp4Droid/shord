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

    var addEntries = function (str) {
        var line = str.split(' ');
        var src = line[1];
        var srcparam = line[2];
        var sink = line[3];
        var sinkparam = line[4];
        $('#src-sink-table tr:last').after('<tr><td><input type="checkbox"' + ((line[0] === "1") ? ' checked' : '') + '><i class="checkbox"></i></td><td><i class="icon-remove"></i></td><td>' + src + '</td><td>' + srcparam + '</td><td>' + sink + '</td><td>' + sinkparam + '</td></tr>');

        $('#src-sink-table .icon-remove').click(function () {
            $(this).parent().parent().remove();
        });
    }


    $.ajax({
        type: "GET",
        url: "/stamp/policyServlet",
        data: {
            policyName: "initPolicy"
        },
        dataType: "text",
        success: function (tex) {
            var lines = tex.split('\n');
            for (var i = 0; i < lines.length; i++) {
                if (lines[i].length > 0) {
                    addEntries(lines[i]);
                }
            }
        }
    });

    $.ajax({
        type: "GET",
        url: "/stamp/policyServlet",
        data: {
            policies: "all"
        },
        dataType: "text",
        success: function (tex) {
            alert(tex);
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

        $.post("/stamp/policyServlet", rules.toString(), function (data) {
            var date = new Date();
            var timestr = date.getHours() + ':' + date.getMinutes();
            $('#policy_save_status').html('<i>Policy Saved at ' + timestr + '</i>');
        });
    });

    $('#src-sink-table .icon-remove').click(function () {
        $(this).parent().parent().remove();

    });
});