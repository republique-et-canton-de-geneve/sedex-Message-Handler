#!/bin/sh
#
# The script to run the MessageHandler as a standalone Java application.
#
# Run this from the install directory!
#
# $Revision: 51 $
# $Author: sasha $
#

# The path of the java runtime to use
JAVA_PATH=$JAVA_HOME

# The path of the configuration directory (absolute or relative to the
# installation directory)
CONFIG_PATH=conf

# The path and name of the sender's log4j configuration file (absolute or
# relative to the installation directory)
LOG4J_CONFIG=conf/log4j.properties

# --------- DO NOT CHANGE BELOW! ----------

# Set the classpath
CLASSPATH=$CONFIG_PATH:lib/${pom.build.finalName}.jar:`cat bin/classpath_unix`

# Set VM arguments
VM_ARGS=-Dlog4j.configuration=file:$LOG4J_CONFIG

# Execute the sender
$JAVA_PATH/bin/java $VM_ARGS -cp $CLASSPATH ch.admin.suis.msghandler.common.MessageHandlerService conf/config.xml
