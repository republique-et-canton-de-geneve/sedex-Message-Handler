# Open e-Gov - MessageHandler for the SeDEx Network

MessageHandler is a SeDEx component used as a platform to ensure a better delivery of messages between members. It has 
been developed by a third party contracted by the Swiss Confederation. It is mainly used in the state of Geneva.

# Table of contents
- [Overview](#overview)
    - [Context](#context)
    - [Content](#content)
- [Compiling and running](#compiling-and-running)
    - [Preconditions](#preconditions)
    - [Compiling](#compiling)
    - [Compiled Version](#compiled-version)
- [Contributing](#contributing)
- [Licence](#licence)

# Overview

## Context
This project was originally developed by a third party. It is
used by different entities in Switzerland, such as the
Federal Statistical Office or external companies to exchange data securely and reliably. For further information about the
SeDEx network, please go to [this website](http://www.e-service.admin.ch/wiki/display/openegovdocfr/MessageHandler).

This version has been modified by the State of Geneva, following a need for new features that were previously not included. 

## Content

The project contains a working copy of the MessageHandler used in the SeDeX network.
The working copy contains :
* The project itself
* A java wrapper, which gives the ability for the service to run as a service.

# Compiling and running

Several steps need to be done in order to make the software work correctly.

## Preconditions

In order to make this software run and compile, you will need the following :
* Maven 3
* Oracle JDK 7
* [JCE Unlimited Strength Jurisdiction Policy Files for Java 7](http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html)
    (see the readme.txt file in the downloaded file for installation instructions into your JDK instance).
* A working internet connection
    

## Compiling

While most dependencies should be automatically be met with Maven (as long as you have it configured properly),
certain dependencies cannot be distributed automatically for legal reasons.
Therefore, in order to make it work, you will have to download a few files manually.

## Compiled version

You can also get the binary version of the project [by going here](https://github.com/republique-et-canton-de-geneve/sedex-Message-Handler-bin).

### Getting the non-distributed dependencies
You need files contained in the binary version of the MessageHandler.
The easiest way to get the missing files is to download the latest binary version of the application.
You can find this file [here](www.e-service.admin.ch/wiki/display/openegovdocfr/MessageHandler+Download).
You will find all the missing files in the lib/ folder.
Here are the files you need to extract :

- file-encryptor-1.0.1-SNAPSHOT.jar (you will need to replace SNAPSHOT by RC1)
- groovy-all-1.6.0.jar
- itext-2.1.7.jar
- suis-security-tools-2.1.20.jar
- wrapper-3.5.2.6.jar
- wrapper-windows-x86-32-3.5.26.zip

Then, you will need to download the BatchSigner library, available [here](https://www.e-service.admin.ch/wiki/display/openegovdoc/BatchSigner+Download)
You will to extract the lib/suis-batchsigner-1.6.2.jar file, and copy it with the other JAR files.

To install these files, you will need to run the following commands :

```Shell
cd <PATH_TO_DEPENDENCIES>
mvn install:install-file -Dfile=file-encryptor-1.0.1-RC1.jar -DgroupId=ch.glue -DartifactId=file-encryptor -Dversion=1.0.1-RC1
mvn install:install-file -Dfile=groovy-all-1.6.0.jar -DgroupId=groovy -DartifactId=groovy-all -Dversion=1.6.0
mvn install:install-file -Dfile=itext-2.1.7.jar -DgroupId=itext -DartifactId=itext -Dversion=2.1.7
mvn install:install-file -Dfile=suis-batchsigner-1.5.3.jar -DgroupId=ch.admin.suis -DartifactId=suis-batchsigner -Dversion=1.5.3
mvn install:install-file -Dfile=suis-security-tools-2.1.20.jar -DgroupId=ch.admin.suis.security.tools -DartifactId=suis-security-tools -Dversion=2.1.20
mvn install:install-file -Dfile=wrapper-3.5.26.jar -DgroupId=tanukisoft -DartifactId=wrapper -Dversion=3.5.26
mvn install:install-file -Dfile=wrapper-windows-x86-32-3.5.26.zip -DgroupId=tanukisoft -DartifactId=wrapper-windows-x86-32 -Dversion=3.5.26
```

The missing dependencies should have been installed into your maven repository. You will be able to build the project automatically from now on.

### Getting the distributed dependencies

```Shell
cd <ROOT_OF_PROJECT>
mvn clean install
```
If successful, you will now have a working binary version of the MessageHandler.

## Running the application
* Get to the root of the project (where the bin, conf, log folder are).
* Open a command prompt.
* Type ```bin\run.bat``` or ```bin\run.sh```, depending on your OS.

This will allow you of running the service without installing anything.
The STDOUT and STDERR pipes are redirected to the log file.

In order to make the application work correctly, you will need a working configuration file. All files are in the conf folder.

The builded project is in src/distribution folder, zipped.
# Contributing

Contributions are welcome ! You can either make a pull request or submit an issue on github.
* Please use the development branch for pull requests modifying the source code.
* You can however use the master branch for pull requests concerning the documentation only.

# Licence

MessageHandler components are released under the [GNU GPL v2.](https://www.gnu.org/licenses/old-licenses/gpl-2.0.html)

# Documentation

All manuals, documentation can be found on the following websites :
* [MessageHandler (v3.3.1)](http://www.e-service.admin.ch/wiki/display/openegovdocfr/MessageHandler+Download)
* [Sedex manual (French)](https://www.bfs.admin.ch/bfs/fr/home/registres/registre-personnes/sedex/downloads.assetdetail.315872.html)
