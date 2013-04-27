@echo off

rem Note: You need to run from this directory!
set libDir=lib
set CP=
set classDir=bin\classes

for %%i in (%libDir%\*.jar) do call cpappend.bat %%i

set CP=%CP%;%classDir%

java -Dprotege.dir="c:\daele\Protege21b181" owlstab.Owlstab 

