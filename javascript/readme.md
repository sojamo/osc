# sojamo.osc

An Open Sound Control (OSC) implementation for javascript and p5js.org

Uses a node application to interface a Websocket-based browser application with a UDP socket.

**Currently work in progress.**

To run the node application from the command line, you need to have npm and node installed. cd into folder node `cd node` and install the required node modules with `npm install` if necessary and run the node application `node .`. Use config.json to configure the app.

By default the websocket server will run on port 8081, the UDP socket will listen for incoming messages on port 9001.

To run the p5js application, load index.html inside your preferred browser. There is currently no visual feedback, incoming and outgoing messages leave their traces inside the Developer Tools Console.


 
