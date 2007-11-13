<<<<<<< .working
@echo off
rem -------------------------------------------------------------------------
rem JBoss Bootstrap Script for Win32
rem -------------------------------------------------------------------------

rem $Id$

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%
set PROGNAME=run.bat
if "%OS%" == "Windows_NT" set PROGNAME=%~nx0%

rem Read all command line arguments

REM
REM The %ARGS% env variable commented out in favor of using %* to include
REM all args in java command line. See bug #840239. [jpl]
REM
REM set ARGS=
REM :loop
REM if [%1] == [] goto endloop
REM         set ARGS=%ARGS% %1
REM         shift
REM         goto loop
REM :endloop

rem Find run.jar, or we can't continue

set RUNJAR=%DIRNAME%\run.jar
if exist "%RUNJAR%" goto FOUND_RUN_JAR
echo Could not locate %RUNJAR%. Please check that you are in the
echo bin directory when running this script.
goto END

:FOUND_RUN_JAR

if not "%JAVA_HOME%" == "" goto ADD_TOOLS

set JAVA=java

echo JAVA_HOME is not set.  Unexpected results may occur.
echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
goto SKIP_TOOLS

:ADD_TOOLS

set JAVA=%JAVA_HOME%\bin\java

if exist "%JAVA_HOME%\lib\tools.jar" goto SKIP_TOOLS
echo Could not locate %JAVA_HOME%\lib\tools.jar. Unexpected results may occur.
echo Make sure that JAVA_HOME points to a JDK and not a JRE.

:SKIP_TOOLS

rem Include the JDK javac compiler for JSP pages. The default is for a Sun JDK
rem compatible distribution to which JAVA_HOME points

set JAVAC_JAR=%JAVA_HOME%\lib\tools.jar

rem If JBOSS_CLASSPATH is empty, don't include it, as this will 
rem result in including the local directory, which makes error tracking
rem harder.
if "%JBOSS_CLASSPATH%" == "" (
	set JBOSS_CLASSPATH=%JAVAC_JAR%;%RUNJAR%
) else (
	set JBOSS_CLASSPATH=%JBOSS_CLASSPATH%;%JAVAC_JAR%;%RUNJAR%
)

rem Setup JBoss specific properties
set JAVA_OPTS=%JAVA_OPTS% -Dprogram.name=%PROGNAME%
set JBOSS_HOME=%DIRNAME%\..

rem Add -server to the JVM options, if supported
"%JAVA%" -version 2>&1 | findstr /I hotspot > nul
if not errorlevel == 1 (set JAVA_OPTS=%JAVA_OPTS% -server)

rem JVM memory allocation pool parameters. Modify as appropriate.
set JAVA_OPTS=%JAVA_OPTS% -Xms128m -Xmx512m

rem With Sun JVMs reduce the RMI GCs to once per hour
set JAVA_OPTS=%JAVA_OPTS% -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000

rem JPDA options. Uncomment and modify as appropriate to enable remote debugging.
rem set JAVA_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n %JAVA_OPTS%

rem Setup the java endorsed dirs
set JBOSS_ENDORSED_DIRS=%JBOSS_HOME%\lib\endorsed

echo ===============================================================================
echo.
echo   JBoss Bootstrap Environment
echo.
echo   JBOSS_HOME: %JBOSS_HOME%
echo.
echo   JAVA: %JAVA%
echo.
echo   JAVA_OPTS: %JAVA_OPTS%
echo.
echo   CLASSPATH: %JBOSS_CLASSPATH%
echo.
echo ===============================================================================
echo.

:RESTART
"%JAVA%" %JAVA_OPTS% "-Djava.endorsed.dirs=%JBOSS_ENDORSED_DIRS%" -classpath "%JBOSS_CLASSPATH%" org.jboss.Main %*
if ERRORLEVEL 10 goto RESTART

:END
if "%NOPAUSE%" == "" pause

:END_NO_PAUSE
=======
@echo off
rem -------------------------------------------------------------------------
rem JBoss Bootstrap Script for Win32
rem -------------------------------------------------------------------------

rem $Id$

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%
set PROGNAME=run.bat
if "%OS%" == "Windows_NT" set PROGNAME=%~nx0%

pushd %DIRNAME%..
set JBOSS_HOME=%CD%
popd

REM Add bin/native to the PATH if present
if exist "%JBOSS_HOME%\bin\native" set PATH=%JBOSS_HOME%\bin\native;%PATH%
if exist "%JBOSS_HOME%\bin\native" set JAVA_OPTS=%JAVA_OPTS% -Djava.library.path=%JBOSS_HOME%\bin\native

rem Read all command line arguments

REM
REM The %ARGS% env variable commented out in favor of using %* to include
REM all args in java command line. See bug #840239. [jpl]
REM
REM set ARGS=
REM :loop
REM if [%1] == [] goto endloop
REM         set ARGS=%ARGS% %1
REM         shift
REM         goto loop
REM :endloop

rem Find run.jar, or we can't continue

set RUNJAR=%JBOSS_HOME%\bin\run.jar
if exist "%RUNJAR%" goto FOUND_RUN_JAR
echo Could not locate %RUNJAR%. Please check that you are in the
echo bin directory when running this script.
goto END

:FOUND_RUN_JAR

if not "%JAVA_HOME%" == "" goto ADD_TOOLS

set JAVA=java

echo JAVA_HOME is not set.  Unexpected results may occur.
echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
goto SKIP_TOOLS

:ADD_TOOLS

set JAVA=%JAVA_HOME%\bin\java

rem A full JDK with toos.jar is not required anymore since jboss web packages
rem the eclipse jdt compiler and javassist has its own internal compiler.
if not exist "%JAVA_HOME%\lib\tools.jar" goto SKIP_TOOLS

rem If exists, point to the JDK javac compiler in case the user wants to
rem later override the eclipse jdt compiler for compiling JSP pages.
set JAVAC_JAR=%JAVA_HOME%\lib\tools.jar

:SKIP_TOOLS

rem If JBOSS_CLASSPATH or JAVAC_JAR is empty, don't include it, as this will 
rem result in including the local directory in the classpath, which makes
rem error tracking harder.
if not "%JAVAC_JAR%" == "" set RUNJAR=%JAVAC_JAR%;%RUNJAR%
if "%JBOSS_CLASSPATH%" == "" set RUN_CLASSPATH=%RUNJAR%
if "%RUN_CLASSPATH%" == "" set RUN_CLASSPATH=%JBOSS_CLASSPATH%;%RUNJAR%

set JBOSS_CLASSPATH=%RUN_CLASSPATH%

rem Setup JBoss specific properties
set JAVA_OPTS=%JAVA_OPTS% -Dprogram.name=%PROGNAME%

rem Add -server to the JVM options, if supported
"%JAVA%" -version 2>&1 | findstr /I hotspot > nul
if not errorlevel == 1 (set JAVA_OPTS=%JAVA_OPTS% -server)

rem JVM memory allocation pool parameters. Modify as appropriate.
set JAVA_OPTS=%JAVA_OPTS% -Xms128m -Xmx512m

rem With Sun JVMs reduce the RMI GCs to once per hour
set JAVA_OPTS=%JAVA_OPTS% -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000

rem JPDA options. Uncomment and modify as appropriate to enable remote debugging.
rem set JAVA_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y %JAVA_OPTS%

rem Setup the java endorsed dirs
set JBOSS_ENDORSED_DIRS=%JBOSS_HOME%\lib\endorsed

#SubEtha
rem set JAVA_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=n %JAVA_OPTS%
set JAVA_OPTS=-XX:MaxPermSize=128m %JAVA_OPTS%
set JAVA_OPTS=-Dfile.encoding=UTF-8 %JAVA_OPTS%
set JAVA_OPTS=-Djava.awt.headless=true %JAVA_OPTS%

echo ===============================================================================
echo.
echo   JBoss Bootstrap Environment
echo.
echo   JBOSS_HOME: %JBOSS_HOME%
echo.
echo   JAVA: %JAVA%
echo.
echo   JAVA_OPTS: %JAVA_OPTS%
echo.
echo   CLASSPATH: %JBOSS_CLASSPATH%
echo.
echo ===============================================================================
echo.

:RESTART
"%JAVA%" %JAVA_OPTS% "-Djava.endorsed.dirs=%JBOSS_ENDORSED_DIRS%" -classpath "%JBOSS_CLASSPATH%" org.jboss.Main %*
if ERRORLEVEL 10 goto RESTART

:END
if "%NOPAUSE%" == "" pause

:END_NO_PAUSE
>>>>>>> .merge-right.r960
