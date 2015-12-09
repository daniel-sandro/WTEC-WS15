
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
            $('#own-playground .field').filter(function () {
                return $(this).data('row') === position.row && $(this).data('col') === position.col;
            }).css('background-color', '#ffffff');
        }
    } else if (data.action === "setrowboat") {
        localStorage.setItem("action", data.action);
    } else if (data.action === "setdestructor") {
        localStorage.setItem("action", data.action);
    } else if (data.action === "setflattop") {
        localStorage.setItem("action", data.action);
    } else if (data.action === "shoot") {
        localStorage.setItem("action", data.action);
    } else if (data.action === "setstatus") {
        var status = data.status;
        $('#status-panel').text(status);
    } else if (data.action === "gameover") {
        // TODO: implement
    } else if (data.action === "youwon") {
        // TODO: implement
    } else if (data.action === "repaint") {
        // TODO: implement
    } else if (data.action === "opponentleft") {
        // TODO: executes twice (?)
        alert("Opponent left!");
        localStorage.clear();
        window.location.replace("/");
    }
});

$('#opponents-playground .field').click(function(e) {
    var row = $(this).data('row');
    var col = $(this).data('col');

    var action = localStorage.getItem("action");
    var gameid = localStorage.getItem("gameid");
    if (action === "shoot") {
        // TODO: implement
    }
});

$('#own-playground .field').click(function(e) {
    var row = $(this).data('row');
    var col = $(this).data('col');

    // TODO: take into account playboard's boundaries!
    var action = localStorage.getItem("action");
    var gameid = localStorage.getItem("gameid");
    if (action === "setrowboat") {
        $(this).css('background-color', '#606060');
        pubsub.publish("socket/message/send", JSON.stringify({
            action: "setrowboat",
            gameid: gameid,
            row: row,
            col: col,
            horizontal: true}));
    } else if (action === "setdestructor") {
        var horiz = confirm("Place horizontally?");
        $(this).css('background-color', '#606060');
        $('#own-playground .field[data-row="' + (horiz ? row : row + 1) + '"][data-col="' + (horiz ? col + 1 : col) + '"]').css('background-color', '#606060');
        $('#own-playground .field[data-row="' + (horiz ? row : row + 2) + '"][data-col="' + (horiz ? col + 2 : col) + '"]').css('background-color', '#606060');
        pubsub.publish("socket/message/send", JSON.stringify({
            action: "setdestructor",
            gameid: gameid,
            row: row,
            col: col,
            horizontal: horiz}));
    } else if (action === "setflattop") {
        var horiz = confirm("Place horizontally?");
        $(this).css('background-color', '#606060');
        $('#own-playground .field[data-row="' + (horiz ? row : row + 1) + '"][data-col="' + (horiz ? col + 1 : col) + '"]').css('background-color', '#606060');
        $('#own-playground .field[data-row="' + (horiz ? row : row + 2) + '"][data-col="' + (horiz ? col + 2 : col) + '"]').css('background-color', '#606060');
        $('#own-playground .field[data-row="' + (horiz ? row : row + 3) + '"][data-col="' + (horiz ? col + 3 : col) + '"]').css('background-color', '#606060');
        $('#own-playground .field[data-row="' + (horiz ? row : row + 4) + '"][data-col="' + (horiz ? col + 4 : col) + '"]').css('background-color', '#606060');
        pubsub.publish("socket/message/send", JSON.stringify({
            action: "setflattop",
            gameid: gameid,
            row: row,
            col: col,
            horizontal: horiz}));
    }
    // Remove action to wait for next one
    localStorage.removeItem("action");
});
