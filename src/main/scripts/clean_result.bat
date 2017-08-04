@echo off

set cmd_dir=%~dp0
set doc_dir=%cmd_dir%docs

rem delete all result directories and files of .\docs
for /d %%d in (%doc_dir%\*) do rd /s/q %%d

echo the result directories have already bean deleted
pause