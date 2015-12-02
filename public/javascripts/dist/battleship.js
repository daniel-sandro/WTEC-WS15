pubsub.subscribe("socket/message/receive", function(msg) {
    var data = JSON.parse(msg.data);
    if (data.action === "newgame") {
        var opponent = JSON.parse(data.opponent);
        var yn = confirm(opponent.name + " wants to play against you");
        localStorage.setItem("gameid", data.gameid);
        pubsub.publish("socket/message/send", JSON.stringify({
            action: "newgame",
            response: yn,
            gameid: data.gameid }));
        if (yn) {
            window.location.replace("/battleship/" + data.gameid);
        }
    } else if (data.action === "newgame_response") {
        var response = data.response;
        if (response) {
            // Game accepted
            localStorage.setItem("gameid", data.gameid);
            window.location.replace("/battleship/" + data.gameid);
        } else {
            // Game rejected
            window.location.replace("/");
        }
    } else if (data.action === "shoot_field") {
        if (localStorage.getItem("gameid") == data.gameid) {
            var position = JSON.parse(data.position);
            $('.playground .field').filter(function () {
                return $(this).data('row') === position.row && $(this).data('col') === position.col;
            }).css('background-color', '#ffffff');
        }
    }
});

$('.playground .field').click(function(e) {
    console.log($(this).data('col'));

    pubsub.publish("socket/message/send", JSON.stringify({
        action: "clickfield",
        gameid: localStorage.getItem("gameid"),
        row: $(this).data('row'),
        col: $(this).data('col') }));
});
