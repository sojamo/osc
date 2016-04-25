/* sojamo.osc
 *
 * An OSC implementation for javascript using websockets to communicated
 * with an UDP-Websocket bridge.
 *
 * Implements most of the java API of sojamo.osc. The bridge is written
 * for node.js and depends on osc-min and ws. OSC packets are formatted to
 * be osc-min conform when sent and received by OscP5 as a JSON object.
 *
 * At the application level an OSC message is treated as an OscMessage
 * object, see implementation below.
 *
 * Information about remote destinations is stored inside a NetAddress.
 * Packets can be sent as follows:
 *  - osc.send(NetAddress, OscMessage)
 *  - osc.send(NetAddress, OscBundle)
 *  - osc.send(NetAddress, AddressPattern, Arguments ...)
 *  - osc.send({ip:string,port:int,address:string,args:array})
 *
 * When a message is received, it gets forwarded to function
 * oscEvent(OscMessage) of the application.
 *
 */

"use strict";

var OscP5 = function() {
  // osc = new OscP5(this);
  // osc = new OscP5(this, 12000);
  // osc = new OscP5({app:this, oscPort:12000, wsPort:8081, wsHost:192.168.1.100});

  this.app = undefined;
  this.wsHost = '127.0.0.1';
  this.wsPort = 8081;
  this.oscPort = 12000;

  switch(arguments.length) {
    case(1):
    this.app = arguments[0].app !== undefined ? arguments[0].app:this.app;
    this.wsHost = arguments[0].wsHost !== undefined ? arguments[0].wsHost:this.wsHost;
    this.wsPort = arguments[0].wsPort !== undefined ? arguments[0].wsPort:this.wsPort;
    this.oscPort = arguments[0].oscPort !== undefined ? arguments[0].oscPort:this.oscPort;
    if(this.app === undefined) {
      this.app = arguments[0];
    }
    break;
    case(2):
    this.app = arguments[0];
    this.oscPort = arguments[1];
    break;
    default:
    console.log("OscP5 fail, wrong number of arguments.");
    break;
  }

  console.log(arguments.length+": app="+this.app+", oscPort="+this.oscPort+", wsHost="+this.wsHost+", wsPort="+this.wsPort);

  var self = this;

  /* TODO consider to use https://github.com/joewalnes/reconnecting-websocket instead,
   * or implement a reconnect-handler.
   */


  /* establish connection to websocke-server */
  this.ws = new WebSocket('ws://' + this.wsHost + ':' + this.wsPort);

  this.ws.onopen = function (theEvent) {
    console.log("websocket connection is open.");
  }

  this.ws.onmessage = function (theEvent) {
    // JSON.stringify(JSON.parse(event.data))
    console.log("got a message from websocket-server:");
    console.log(theEvent.data);
    self.app.oscEvent(theEvent);
  };

  this.ws.onerror = function (theEvent) {
    console.log("websocket error.");
  }

  this.ws.onclose = function (theEvent) {
    console.log("websocket connection is closed.");
    // TODO when connection is lost, try to reconnect
  }

}


OscP5.prototype.send = function() {
  if(arguments[0] instanceof NetAddress) {
    var ip = (arguments[0].ip === '127.0.0.1') ? this.wsHost:arguments[0].ip;
    var port = arguments[0].port;
    switch(arguments.length) {
      case(2):
      /* send(NetAddress, OscMessage | OscBundle) */
      if(arguments[1] instanceof OscMessage) {
        var address = arguments[1].getAddress();
        var args = arguments[1].getArguments();
        this.ws.send(JSON.stringify(this.pack(address, args, ip, port)));
      } else if (arguments[1] instanceof OscBundle) {
        /* TODO */
      }
      break;
      default:
      /* send(NetAddress, AddressPattern, Arguments ...) */
      var address = arguments[1];
      var args = Array.from(arguments).slice(2);
      this.ws.send(JSON.stringify(this.pack(address, args, ip, port)));
      break;
    }
  } else if (arguments.length == 1 && arguments[0] instanceof Object){
    /* send({ip:string,port:int,address:string,args:Array}) */
    /* TODO check if all pairs are properly set. keys required: ip, port, address, args */
    this.ws.send(JSON.stringify(arguments[0]));
  }
}

OscP5.prototype.pack = function(theAddress, theArgs, theRemote, thePort) {
      var packet = {};
      packet.address = theAddress;
      packet.args = theArgs;
      packet.ip = theRemote;
      packet.port = thePort;
      return packet;
}

OscP5.prototype.dispose = function() {
  /* TODO close websocket connection. */
}


var NetAddress = function() {
  switch(arguments.length) {
    case(1):
    this.ip = '127.0.0.1';
    this.port = arguments[0];
    break;
    case(2):
    this.ip = arguments[0];
    this.port = arguments[1];
    break;
    default:
    console.log("invalid NetAddress");
  }
}

NetAddress.prototype.toString = function() {
  var s = "NetAddress{";
  s += " ip="+this.ip;
  s += ", port="+this.port;
  s += " }";
  return s;
}

var OscMessage = function() {
  this.address = "";
  this.typetag = "";
  this.args = [];
  if(arguments.length == 1) {
    if(typeof arguments[0] ===  'string') {
      this.address = arguments[0];
    } else if (arguments[0] instanceof OscMessage){
      this.address = arguments[0].address;
      this.typetag = arguments[0].typetag;
      this.args = arguments[0].args.slice(0);
    }
  }
}

OscMessage.prototype.getAddress = function() {
  return this.address;
}

OscMessage.prototype.getArguments = function() {
  return this.args;
}

OscMessage.prototype.getTypetag = function() {
  return this.typetag;
}

OscMessage.prototype.add = function() {
  this.args = this.args.concat(Array.from(arguments));
}

OscMessage.prototype.toString = function() {
  var s = "OscMessage{";
  s += " address="+this.address;
  s += ", typetag="+this.typetag;
  s += ", arguments="+JSON.stringify(this.args);
  s += " }";
  return s;
}


var OscBundle = function() {

}

OscBundle.prototype.add = function() {

}
