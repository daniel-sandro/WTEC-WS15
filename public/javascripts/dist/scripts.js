$(function() {
    $('.playground .field').click(function(e) {
        console.log($(this).data('col'));
        //console.log('col: ' + e);
    })
});