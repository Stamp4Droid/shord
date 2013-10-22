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

function reportFormError() {
    $('#centerpane').prepend([
        '<div class="alert alert-error">',
        '<a href="#" class="close" data-dismiss="alert">x</a>',
        'Error Messages.',
        '</div>',
        '</div>'
    ].join('\n'));
}