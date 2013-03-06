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

set BEES_HOME=%~dp0.
set PATH=%PATH%;%~dp0

if "%1" == "" goto welcome
goto %1

:err_nojavahome
echo JAVA_HOME not set
goto end

:homedir
goto welcome

:welcome
echo Welcome to the CloudBees Development Console
echo --------------------------------------------
echo Here are some useful "How do I?" commands...
echo creating a new web application project
echo    bees help create
echo running your web application
echo    bees help run
echo deploying your web application to CloudBees
echo    bees help deploy


goto end

:end
