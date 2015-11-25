var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
var websocket = new WS("ws://localhost:9000/socket");

websocket.onmessage = function(event) {
    switch (event.action) {
        case "newgame":
            // TODO: not showing?
            var r = confirm("Play against " + event.oponent.name + "?");
            if (r) {
                console.log("Game accepted");
            } else {
                console.log("Game rejected");
            }
            break;
    }
};
