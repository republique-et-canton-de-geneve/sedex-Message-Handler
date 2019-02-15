@echo off

rem
rem The script to run the MessageHandler as a standalone Java application
rem
rem Run this from the install directory!
REM
rem $Revision: 348 $
rem $Author: sasha $

setlocal

rem The path of the java runtime to use (change this if needed!)
set JAVA_PATH=%JAVA_HOME%

rem The path of the configuration directory (absolute or relative to the
rem installation directory)
set CONFIG_PATH=conf

rem The path and name of the sender's log4j configuration file (absolute or
rem relative to the installation directory)
set LOG4J_CONFIG=conf\log4j.properties

rem --------- DO NOT CHANGE BELOW! ----------

rem Set the classpath
for /F %%G in (bin\classpath_windows) do set LIBPATH=%%G 
set CLASSPATH=lib\${pom.build.finalName}.jar;%CONFIG_PATH%;%LIBPATH%

rem Set VM arguments
set VM_ARGS=-Dlog4j.configuration=file:%LOG4J_CONFIG% -Djava.library.path=lib\native

rem Execute the sender-receiver
%JAVA_PATH%\bin\java %VM_ARGS% -cp %CLASSPATH% ch.admin.suis.msghandler.common.MessageHandlerService conf\config.xml
