# sojamo.osc

An Open Sound Control (OSC) implementation for java and processing.org. sojamo.osc succeeds the [oscP5](https://github.com/sojamo/oscP5) library.

## Contents

  * [Installation](#install)
  * [How does it work?](#how)
	* [How to use?](#use)
    * [Getting started](#started)
    * [Event Handling](#events)
  * [Android](#android)
  * [Javascript](#javascript)
  * [Help](#help)



## <a name"install"></a>Installation

### Processing

If you want to install sojamo.osc manually, download (the latest) version from the [releases](https://github.com/sojamo/osc/releases) directory. Inside the downloaded .zip file you will find [install_instructions](resources/install_instructions.txt) which will guide you through the installation details and tell you where to put the osc folder.

## <a name"how"></a>How does it work?



## <a name"use"></a>How to use?

sojamo.osc can be used with a Processing sketch as well as a Java application.

``` processing

import sojamo.osc.*;

OscP5 osc;
NetAddress remote;

void setup() {
    size(400,400);
    osc = new OscP5(this,12000);
    remote = new NetAddress("127.0.0.1",12000);
}

void draw() {

}

void keyPressed() {
    osc.send(remote, )
}

void oscEvent(OscMessage m) {
    println(m);
}

```

### <a name"started"></a>Getting started


### <a name"events"></a>Event handling
I