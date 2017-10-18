
window.onload = init;
var wsUri = "ws://" + document.location.host + document.location.pathname + "queryoptimizer";
var websocket = new WebSocket(wsUri);

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
    parse(jsonObject)
}

function formSubmit() {
    var form = document.getElementById("sendQueryForm");
    var query = form.elements["query"].value;
    document.getElementById("sendQueryForm").reset();
    chart_query_visualizer_config = websocket.send(query);
    alert(query);
    alert(chart_query_visualizer_config);
    new Treant(chart_query_visualizer_config);
}

function init() {
    hideForm();
}