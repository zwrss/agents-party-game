var roomId = "0";
var role = "Regular";

function getDiv() {
    return document.getElementById("gameboard");
}

function renderRoomSelection() {
    console.log('Rendering room selection!');
    $("#gameboard").load("public/form_room.html", function () {
        $("#joinRoomButton").click(function () {
            roomId = $("#roomIdInput").val();
            document.title = roomId;
            websocket.send('{"action":"Join", "roomId":"' + roomId + '"}')
        });
        $("#createRoomButton").click(function () {
            websocket.send('{"action":"Create"}');
        });
    });
}

function renderRoleSelection() {
    console.log('Rendering role selection!');
    $("#gameboard").load("public/form_role.html", function () {
        $("#adminRoleButton").click(function () {
            role = "Admin";
            websocket.send('{"role":"Admin"}')
        });
        $("#regularRoleButton").click(function () {
            websocket.send('{"role":"Regular"}');
        });
    });
}

function renderGameState(state) {
    console.log('Rendering game state for ' + JSON.stringify(state));
    roomId = state.roomId;
    document.title = roomId;
    $("#gameboard").empty();
    var inRow = 0;
    var row = $('<div class="Row"></div>');
    var cell = $('<div class="Cell"><p>Room: ' + roomId + '</p></div>');
    row.append(cell)
    if (role == "Admin") {
        cell = $('<div class="Cell"><p>RESET</p></div>');
        cell.click(function () {
            websocket.send('{"action":"Reset"}');
        });
    }
    row.append(cell)
    $("#gameboard").append(row);
    row = $('<div class="Row"></div>');
    state.words.forEach(function (element) {
        inRow += 1;
        console.log(element);
        cell = $('<div class="Cell ' + element.state + '"><p>' + element.word + '</p></div>');
        if (role == "Admin") {
            cell.click(function () {
                websocket.send('{"word":"' + element.word + '"}');
            });
        }
        row.append(cell);
        if (inRow == 5) {
            inRow = 0;
            $("#gameboard").append(row);
            row = $('<div class="Row"></div>');
        }
    });
}

function init() {

// TODO Learn damn https!
//    if (window.location.protocol != 'https:' && window.location.hostname != 'localhost') {
//        location.href = 'https:' + window.location.href.substring(window.location.protocol.length);
//    }

    if (window.location.protocol == 'https:') {
        websocket = new WebSocket('wss://' + window.location.host);
    } else {
        websocket = new WebSocket('ws://' + window.location.host);
    }

    websocket.onmessage = onRoomMessage;

    renderRoomSelection();

}

function onRoomMessage(evt) {
    var message = JSON.parse(evt.data);
    console.log(message);
    roomId = message.roomId;
    websocket.onmessage = onGameStateMessage;
    renderRoleSelection();
}

function onGameStateMessage(evt) {
    var message = JSON.parse(evt.data);
    console.log(message);
    renderGameState(message);
}


window.addEventListener("load", init, false);