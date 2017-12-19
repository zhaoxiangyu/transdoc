@echo off

set TRANSDOC_HOME=%~dp0
cd /d %TRANSDOC_HOME%

set TRANSDOC_CLASSPATH=%TRANSDOC_HOME%bin

setlocal enabledelayedexpansion

for %%j in (%TRANSDOC_HOME%libs\*.jar) do (
  set TRANSDOC_CLASSPATH=!TRANSDOC_CLASSPATH!;%%j
)

set CMD_ARGS=

:getParameter
set PARAMETER=%1
if not defined PARAMETER goto runJava
set CMD_ARGS=%CMD_ARGS% %PARAMETER%
shift /0
goto getParameter

:runJava
java -classpath %TRANSDOC_CLASSPATH% com.transdoc.Transdoc %CMD_ARGS%

endlocal
echo;
pause