This project is a kind of boilerplate to be used to get acquainted
with a process of TensorFlow Serving client development
using Kotlin as a programming language and Gradle as a build system.

Before a compilation make sure that you have provided some host
with a running TensorFlow Serving model in a `main.kt` file.

To build the project execute from a root of this repo:

`./gradlew build`

Then to launch a compiled executable you can use 
a generated jar-file, don't forget that you should
provide a path to some image to be processed by the server:

`java -jar build/libs/tf-serving-client-1.0.0.jar test.jpg`