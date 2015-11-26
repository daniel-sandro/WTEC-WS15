pubsub.subscribe("socket/message/receive", function(msg) {
    var data = JSON.parse(msg.data);
    if (data.action === "newuser") {
        var newuser = JSON.parse(data.newuser);
        var ul = document.getElementById("onlineUsers");
        var li = document.createElement("li");
        li.setAttribute("id", newuser.id.toString());
        var a = document.createElement("a");
        a.setAttribute("href", "/playagainst/" + newuser.id);
        a.innerHTML = newuser.name;
        li.appendChild(a);
        ul.appendChild(li);
    } else if (data.action === "userleaves") {
        var leavinguser = JSON.parse(data.leavinguser);
        var elem = document.getElementById(leavinguser.id.toString());
        elem.parentNode.removeChild(elem);
    }
});
