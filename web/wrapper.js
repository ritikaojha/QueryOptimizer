
window.onload = init;
var wsUri = "ws://" + document.location.host + document.location.pathname + "queryoptimizer";
var websocket = new WebSocket(wsUri);

websocket.onmessage = function(evt) { onMessage(evt) };
websocket.onerror = function(evt) { onError(evt) };

function onError(evt) {
    writeToScreen('<span style="color: red;">ERROR:</span> ' + evt.data);
}

var output = document.getElementById("output");
websocket.onopen = function(evt) { onOpen(evt) };

function writeToScreen(message) {
    output.innerHTML += message + "<br>";
}

function onOpen() {
    writeToScreen("Connected to " + wsUri);
}

function onMessage(event) {
    jsonObject = JSON.parse(event.data);
    chart_query_visualizer_config = (jsonObject);
    new Treant(chart_query_visualizer_config);
}

function formSubmit() {
    var form = document.getElementById("sendQueryForm");
    var query = form.elements["query"].value;
    websocket.send(query);
}

function init() {
    hideForm();
}