@echo off
@setlocal

if [%BEES_HOME%] == [] set BEES_HOME=%STAX_HOME%
if [%BEES_HOME%] == [] goto err_nobeeshome
set STAX_HOME=%BEES_HOME%

REM remove surrounding quotes
SET BEES_HOME=%BEES_HOME:"=%
SET BEES_HOME=%BEES_HOME:"=%

set JAVA_OPTS=-Dbees.home="%BEES_HOME%" -Xmx256m %BEES_OPTS%

java %JAVA_OPTS% -cp "%BEES_HOME%/lib/cloudbees-boot.jar" com.cloudbees.sdk.boot.Launcher %*
goto end

:err_nobeeshome
echo "BEES_HOME is not set"
goto end

:end
@endlocal