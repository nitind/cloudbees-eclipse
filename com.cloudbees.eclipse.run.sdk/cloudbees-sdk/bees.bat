@rem ***************************************************************************
@rem Copyright (c) 2013 Cloud Bees, Inc.
@rem All rights reserved. 
@rem This program is made available under the terms of the 
@rem Eclipse Public License v1.0 which accompanies this distribution, 
@rem and is available at http://www.eclipse.org/legal/epl-v10.html
@rem
@rem Contributors:
@rem 	Cloud Bees, Inc. - initial API and implementation 
@rem ***************************************************************************
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