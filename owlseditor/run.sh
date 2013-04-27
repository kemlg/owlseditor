#! /bin/sh

simDir=$HOME/Projekt
libDir=${simDir}/lib
classDir=${simDir}/bin/classes

# Add jars in lib dir to classpath
for i in ${libDir}/*.jar ; do
    if [ "$CP" != "" ]; then
	CP=${CP}:$i
    else
	CP=$i
    fi
done

# Add our own classes to classpath
CP=${CP}:${classDir}

java -classpath $CP wizard.Host
