var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
var websocket = new WS("ws://141.37.192.2:9000/socket");
var pubsub = amplify;

websocket.onopen = function() {
    pubsub.publish("socket/open");
};

websocket.onclose = function() {
    pubsub.publish("socket/close");
};

websocket.onerror = function() {
    pubsub.publish("socket/error");
};

websocket.onmessage = function(data) {
    pubsub.publish("socket/message/receive", data);
};

pubsub.subscribe("socket/message/send", function(data) {
    console.log(data);
    websocket.send(data);
});

pubsub.subscribe("socket/message/receive", function(data) {
    console.log(data);
});

pubsub.subscribe("socket/open", function() {
    console.log("Socket opened");
});
