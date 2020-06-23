# sojamo.osc

An Open Sound Control (OSC) implementation for java (and [processing.org](http://processing.org)). This is work in progress and mostly used in personal projects.

### Build

Uses the [gradle](https://gradle.org) build tool to build the library. To build, use `./gradlew build` from inside the project's root directory, the osc.jar then sits inside _build → libs_.

To automatically copy the jar'ed library to your [Processing](https://processing.org) libraries folder, use `./gradlew build -q copyJar`. Test results can be found in _build → reports → tests → test_, then open the _index.html_ file to view them.

2020
