# LauncherLogic

This project executes the installer logic from the LCLPLauncher from javascript.

## Building
This project uses Java 13, make sure you run it with version >= 13.<br>
If you are using a lower version than Java 13 (`java --version` < 13), make sure to follow the next section:
<hr>

#### If you are using a java version below 13 by default
In the root of this project, `gradle.properties` can be created to explicitly tell gradle which java to use.
Add an entry to it like this:
```
org.gradle.java.home=<path to jdk home>
```
Make sure, the path does not contain backslashes ('/') and do not put it in quotes, for gradle to understand.
<hr>

## Java jlink runtime
The project uses the [Badass JLink Plugin](https://github.com/beryx/badass-jlink-plugin) gradle plugin to automatically create custom jlink runtimes.<br>
To build it, simply type:<br>
<br>
`gradlew jlink`<br>
<br>
Any output will be in the `/build` directory.
The useable runtime is located under `/build/image`.
