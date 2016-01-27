var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
var websocket = new WS("ws://" + window.location.host + "/socket");
// var websocket = new WS("wss://" + window.location.host + "/socket");
var pubsub = amplify;
var that = this;

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
    that.send(data);
});

pubsub.subscribe("socket/message/receive", function(data) {
    console.log(data);
});

pubsub.subscribe("socket/open", function() {
    console.log("Socket opened");
});

// Wait for established connection
send = function (message, callback) {
    this.waitForConnection(function () {
        websocket.send(message);
        if (typeof callback !== 'undefined') {
            callback();
        }
    }, 1000);
};

waitForConnection = function (callback, interval) {
    if (websocket.readyState === 1) {
        callback();
    } else {
        var that = this;
        // optional: implement backoff for interval here
        setTimeout(function () {
            that.waitForConnection(callback, interval);
        }, interval);
    }
};
