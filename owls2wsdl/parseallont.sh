#!/bin/bash

#
# Shell script for DFKI linux environment only.
#

# read ont directory to define which files should be requested from webserver
ontdir=/opt/local/www/ontology/
kbdir=/home/ofourman/owls2wsdl_kbs
cmd="/home/ofourman/jre1.5.0_11/bin/java -Xms256m -Xmx512m -jar /home/ofourman/bin/owls2wsdl/OWLS2WSDL.jar -kbdir $kbdir"
www="http://localhost/ontology/"

cd $ontdir

for file in $(ls *.owl) ; do
  if test -f $file; then
    kb=KB_${file%.owl}-MAP.xml
    if test -e "$kbdir/$kb"; then
      echo "File $file already exists in KB DIR $kbdir"
    else
      echo "Process file: $file"
      $cmd  $www$file
    fi
  fi
done
