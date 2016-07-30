/* UDP (OSC) to websocket interface */



/* read config file */
var config = require('./config.json');



/* init logger */
var logger = {};
logger.debugLevel = 'info';
logger.log = function(level, message) {
  var levels = ['debug','info', 'warn', 'error'];
  if (levels.indexOf(level) >= levels.indexOf(logger.debugLevel) ) {
    if (typeof message !== 'string') {
      message = JSON.stringify(message);
    };
    console.log(level+': '+(new Date().toString())+' '+message);
  }
}



/* init websocket and OSC related variables */
var wsPort, wss, WebSocketServer
wsListeningPort = config.wsListeningPort !== undefined ? config.wsListeningPort:8081;
WebSocketServer = require('ws').Server;
wss = new WebSocketServer({ port: wsListeningPort });

var osc, sock, udp;
var oscListeningPort;
oscListeningPort = config.oscListeningPort !== undefined ? config.oscListeningPort:3333;



/* setup websocket */
wss.on('connection', function connection(ws) {
  logger.log('info','new websocket connection from '+ws.upgradeReq.headers.origin);
  ws.on('message', function incoming(message) {
    logger.log('debug','websocket received: '+ message);
    var packet = JSON.parse(message);
    var buffer = osc.toBuffer(packet);
    var port = packet.port;
    var ip = packet.ip;
    sock.send(buffer, 0, buffer.length, port, ip);
  });

  ws.on('close', function close(ws) {
    logger.log('info','closing websocket connection ' + ws);
  });

  // TODO send connected message
  ws.send(JSON.stringify(process.memoryUsage()));

});
logger.log('info','Websocket running and bound to port '+wsListeningPort);



/* setup UDP socket to send and receive OSC packets */
osc = require('osc-min');
udp = require("dgram");

sock = udp.createSocket("udp4", function(thePacket, rinfo) {
  // receive an OSC message
  logger.log('debug','received an OSC packet at '+wsListeningPort);
  try {
    // send through WebSocketServer wss to all connected clients (browser)
    var packet = unpack(thePacket);
    console.log(packet);

    /* a packet here is converted into an object which is
     * identifiable by its oscType value (message or bundle).
     * at this point the packet, no matter of which type it is,
     * can be sent to the client, or can be processed and
     * scheduled (if of type bundle) for distribution considering
     * the timetag.
     * the packet is the sent to the Websocket client where it is
     * translated from the osc-min packet format into an
     * sojamo.osc.OscPacket format.
     */

    wss.clients.forEach(function each(client) {
      client.send(packet);
    });
    
  } catch (err) {
    logger.log('error',err);
    return logger.log('debug','invalid OSC packet.');
  }
});

var unpack = function(thePacket) {
    return JSON.stringify(osc.fromBuffer(thePacket));
}

sock.bind(oscListeningPort);
logger.log('info','UDP socket create and bound to port '+oscListeningPort);
