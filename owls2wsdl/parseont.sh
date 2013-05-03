#!/bin/bash

#
# Shell script for DFKI linux environment only.
#

# java -Xms : minimmal zur Verfuegung stehender Speicherplatz
# java -Xmx : maximaal zur Verfuegung stehender Speicherplatz
/home/ofourman/jre1.5.0_11/bin/java -Xms256m -Xmx512m -jar OWLS2WSDL.jar -kbdir /home/ofourman/owls2wsdl_kbs $1 
