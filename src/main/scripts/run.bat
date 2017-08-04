@echo off

rem Init args string for executing transdoc.jar
set cmd_dir=%~dp0
set param_str=

rem get each of params and append to %param_str%
:get_param
set param=%1
if not defined param goto execute
set param_str=%param_str% %param%
shift /0
goto get_param

rem execute the cmd
:execute
cd /d %cmd_dir%
java -jar %cmd_dir%bin\transdoc.jar %param_str%
echo;
pause