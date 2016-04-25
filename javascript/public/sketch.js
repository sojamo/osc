
var osc;
var remote;

function setup() {
  // osc = new OscP5({app:this,wsPort:8081});
  osc = new OscP5(this);
  remote = new NetAddress(9000);
}

function draw() {
  background(255,0,0);
}

function oscEvent(theMessage) {
    console.log("tested");
    console.log(theMessage);
}

function mousePressed() {
  // osc.send({ip:"127.0.0.1", port:12000,address:"/test",args:[1,2,3,4]});
  // osc.send(remote, "/test", 1, 2, mouseX, mouseY);
  var m1 = new OscMessage("/live/master/pan");
  m1.add(0);
  //m1.add(1,2,3);
  // m1.add(100,[55,44,33],300);
  console.log(m1);
  osc.send(remote,m1);
}
