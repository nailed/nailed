Nailed 3
=============
Version 3 of our minecraft gameserver modification. It is licensed under the [MIT License]. 

* [Homepage]
* [Source]
* [Issues]
* [Nailed API Wiki]
* [Nailed Wiki]

## Prerequisites
* [Java] 6

## Clone
The following steps will ensure your project is cloned properly.  
  1. `git clone git@github.com:nailed/nailed.git`  
  2. `cd nailed`  
  3. `git submodule update --init --recursive`  

## Setup
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems instead of any 'gradle' command.

__For [IntelliJ]__  
  1. Run `gradle setupNailed`  
  2. Run `gradle idea`  
  3. Open the generated nailed.ipr file in IntelliJ  
  4. When IntelliJ asks you to import the gradle project, click on `Import gradle project`
  
__For [Eclipse]__  
* Currently we do not support Eclipse. [IntelliJ] is our only supported IDE

## Running
__Note:__ The following is aimed to help you setup run configurations for IntelliJ, if you do not want to be able to run Nailed directly from your IDE then you can skip this.  

__For [IntelliJ]__  
  1. Go to **Run > Edit Configurations**.  
  2. Click the green + button and select **Application**.  
  3. Set the name as `Nailed` and apply the information below.  
  5. When launching the server for the first time, it will shutdown by itself. You will need to modify the settings.conf to set onlinemode=false and modify the eula.txt to set eula=true (this means you agree to the Mojang EULA, if you do not wish to do this then you cannot run the server).

__Server__

|     Property      | Value                              |
|:-----------------:|:-----------------------------------|
|    Main class     | net.minecraft.launchwrapper.Launch |
|  Program Options  | --tweakClass=jk_5.nailed.server.tweaker.NailedTweaker |
| Working directory | ./runtime)                         |
| Module classpath  | nailed                             |


## Building
__Note:__ If you do not have [Gradle] installed then use ./gradlew for Unix systems or Git Bash and gradlew.bat for Windows systems instead of any 'gradle' command.

In order to build Nailed you simply need to run the `gradle buildPackages` command. You can find the compiled JAR files in `./build/libs` labeled similarly to 'nailed-x.x.x-SNAPSHOT.jar'.

## Contributing
Are you a talented programmer looking to contribute some code? We'd love the help!
* Open a pull request with your changes, following our [guidelines](CONTRIBUTING.md).
* Please follow the above guidelines for your pull request(s) to be accepted.

[Eclipse]: http://www.eclipse.org/
[Gradle]: http://www.gradle.org/
[Homepage]: http://nailed.jk-5.tk/
[IntelliJ]: http://www.jetbrains.com/idea/
[Issues]: https://github.com/nailed/nailed/issues
[Nailed API Wiki]: https://github.com/nailed/nailed-api/wiki/
[Nailed Wiki]: https://github.com/nailed/nailed/wiki/
[Java]: http://java.oracle.com/
[Source]: https://github.com/nailed/nailed/
[MIT License]: http://www.tldrlegal.com/license/mit-license
