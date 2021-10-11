# Open e-Gov - MessageHandler for the SeDEx Network

MessageHandler is a SeDEx component used as a simple file based transport layer to ensure a better delivery of messages between members. It has 
been developed by a third party contracted by the Swiss Confederation. It is mainly used in the state of Geneva.

Files are transported by calling a send-receive program started
as a NT service or as a standalone console program.
Refer to the documentation for further information.

For a full documentation and configuration examples, please visit the
[Swiss Confederation web site](http://www.e-service.admin.ch/wiki/display/openegovdoc/MessageHandler).

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
The working copy contains:
* The project itself
* A java wrapper, which gives the ability for the service to run as a service.

# Compiling and running

Several steps need to be completed in order to make the software work correctly.

## Preconditions

In order to make this software run and compile, you will need the following:
* Maven 3
* Oracle JDK 8
* [JCE Unlimited Strength Jurisdiction Policy Files for Java 7](http://www.oracle.com/technetwork/java/javase/downloads/jce-7-download-432124.html)
    (see the readme.txt file in the downloaded file for installation instructions into your JDK instance)
* A working internet connection
* A computer running Windows. No support for Linux is included so far.
    
### Gathering all dependencies

While most dependencies should be automatically be met with Maven (as long as you have it configured properly),
certain dependencies cannot be found in the Maven Central repository, for legal reasons.
In order to install these dependencies into your local Maven repository, run the following command:
```Shell
cd <ROOT_OF_PROJECT>
mvn clean 
```

## Compiling

Use the usual Maven command:
```Shell
cd <ROOT_OF_PROJECT>
mvn install
```
If successful, you will now have a working binary version of the MessageHandler.

Note: the ``mvn clean`` command above matters, because it installs required artifacts into your
repository before Maven's automatic dependency checks are carried out.
Therefore it should be run separately from the
``mvn install`` command, i.e., you should avoid to run ``mvn clean install`` - at least the first time.

## Running the application

Run the following command:
```Shell
cd <ROOT_OF_PROJECT>\src\distribution
bin\run.bat
```

Note: on Windows you need to use a 32-bit - not 64-bit - JVM, 
because wrapper.dll is published in community edition only in 32-bit version.
By default your ``JAVA_HOME`` is used; you can override this setting by modifying ``JAVA_PATH`` in file
``<ROOT_OF_PROJECT>\src\distribution\bin\run.bat``.

The above command allows you to run the service without installing anything.
The STDOUT and STDERR pipes are redirected to the log file.

In order to make the application work correctly, you will need a working configuration file. All files are in the conf folder.

The built project is in src/distribution folder, zipped.

# Contributing

Contributions are welcome! You can either make a pull request on branch ``master`` or submit an issue on GitHub.

# Licence

MessageHandler components are released under the [GNU GPL v2.](https://www.gnu.org/licenses/old-licenses/gpl-2.0.html)

# Documentation

All manuals, documentation can be found on the following websites:
* [MessageHandler (v3.3.1)](http://www.e-service.admin.ch/wiki/display/openegovdocfr/MessageHandler+Download)
* [Sedex manual (French)](https://www.bfs.admin.ch/bfs/fr/home/registres/registre-personnes/sedex/downloads.assetdetail.315872.html)

# Contact

Should you have any question, comment or proposal, please send an email to opensource@etat.ge.ch.

# Disclaimer

This software is provided "as is" and any expressed or implied warranties, including, but not limited to, the implied
warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall the regents or
contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages
(including, but not limited to, procurement of substitute goods or services; loss of use, data, or profits; or business
interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort
(including negligence or otherwise) arising in any way out of the use of this software, even if advised of the
possibility of such damage.
