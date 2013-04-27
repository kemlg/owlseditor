if "%CP%" == "" goto setInitial
set CP=%CP%;%1
goto finish
:setInitial
set CP=%1
:finish
