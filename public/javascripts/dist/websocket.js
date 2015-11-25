var WS = window['MozWebSocket'] ? MozWebSocket : WebSocket;
//var websocket = new WS("@routes.OnlineController.socket().webSocketURL()");
var websocket = new WS("ws://localhost:9000/socket");

