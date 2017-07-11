var express = require("express");
var app = express();
var server = require("http").createServer(app);
var io = require("socket.io").listen(server);
server.listen(process.env.PORT || 3000);

var users = [];

io.sockets.on('connection', function (socket) {

    console.log("connected");
  
    socket.emit('conn', { content: "connected" });

  
    socket.on('register', function (data) {
        if (users.indexOf(data) > -1) {
            socket.emit('register', {content: data + " is exist"});
        } else {
            socket.name = data;
            users.push(data);

            socket.emit('register', { current: data });

            // emit toi tat ca moi nguoi
            io.sockets.emit('register', { content: users });

            // clients = io.sockets.clients();

            // console.log(clients);
        }
    });

    socket.on('message', function (data) {
        // emit toi tat ca moi nguoi
        io.sockets.emit('message', { content: data });
    
        // emit tới máy nguoi vừa gửi
        // socket.emit('message', { content: data });
    });

    socket.on('disconnect', function () {
        console.log("disconnected");
    });
  
    socket.on('disconn', function (data) {
        var index = users.indexOf(data);
        if (index > -1) {
            users.splice(index, 1);
        }
    });

});
