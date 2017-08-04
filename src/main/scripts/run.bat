@echo off

rem Init args string for execute transdoc.jar
set cmd_dir=%~dp0
set empty_str=
set param_str=

rem get each param then append to "param_str"
:get_param
set param=%1
if not defined param goto execute
set param_str=%param_str% %param%
shift /0
goto get_param

rem when param is empty, end the cmd
:execute
java -jar %cmd_dir%bin\transdoc.jar %param_str%
echo;
pause