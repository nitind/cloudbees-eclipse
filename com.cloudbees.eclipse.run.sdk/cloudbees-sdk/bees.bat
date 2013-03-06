@rem ***************************************************************************
@rem Copyright (c) 2013 Cloud Bees, Inc.
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem              http://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
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