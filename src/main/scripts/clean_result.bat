@echo off

set cmd_dir=%~dp0
set doc_dir=%cmd_dir%docs

rem delete all result directories and files of .\docs
for /d %%d in (%doc_dir%\*) do rd /s/q %%d

echo 已清理完毕所有转换生成的结果目录
pause