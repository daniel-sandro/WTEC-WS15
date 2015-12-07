pubsub.subscribe("socket/message/receive", function(msg) {
    var data = JSON.parse(msg.data);
    if (data.action === "newuser") {
        var newuser = JSON.parse(data.newuser);
        var table = document.getElementById("onlineUsers");
        var tr = document.createElement("tr");
        tr.setAttribute("id", newuser.id.toString());
        var tdID = document.createElement("td");
        tdID.innerHTML = newuser.id;
        var tdName = document.createElement("td");
        tdName.innerHTML = newuser.name;
        var tdActive = document.createElement("td");
        var play = document.createElement("a");
        play.setAttribute("href", "/playagainst/" + newuser.id);
        play.className = "btn btn-primary pull-right";
        play.innerHTML = "Play!";
        tdActive.appendChild(play);
        tr.appendChild(tdID);
        tr.appendChild(tdName);
        tr.appendChild(tdActive);
        table.appendChild(tr);
    } else if (data.action === "userleaves") {
        var leavinguser = JSON.parse(data.leavinguser);
        var elem = document.getElementById(leavinguser.id.toString());
        elem.parentNode.removeChild(elem);
    } else if (data.action === "currently_playing") {
        var user1 = JSON.parse(data.user1);
        var user2 = JSON.parse(data.user2);
        var elem = document.getElementById("btn_" + user.id.toString());
        // TODO: replace "Play!" anchor with "Currently playing" button
    } else if (data.action === "not_playing_anymore") {
        var user1 = JSON.parse(data.user1);
        var user2 = JSON.parse(data.user2);
        var elem = document.getElementById("btn_" + user.id.toString());
        // TODO: replace "Currently playing" button with "Play!" anchor
    }
});
