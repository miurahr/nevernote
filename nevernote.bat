@echo off
rem #####################
rem # Install variables #
rem #####################
set NEVERNOTE=%~dp0


rem ########################################
rem # Memory settings.  These can be tuned #
rem # to your specific needs.  The greater #
rem # the memory allocated the better      #
rem # your response may be, but the more   #
rem # resources the program will consume.  #
rem # Lower numbers may hurt performance   #
rem # but will reduce resource held by     #
rem # the program.  If you get errors      #
rem # that say "out of memory" you need    #
rem # to increase these values.            #
rem ########################################
rem # Initial heap size
set NN_XMS=256M
rem # Maximum heap size
set NN_XMX=512M

rem ## The young generation
rem # the young generation will occupy 1/2 of total heap
set NN_NEW_RATIO=1

rem ## GC option
rem ## recommend Incremental Low Pause GC for desktop apps 
set NN_GC_OPT=-Xincgc
rem ## recent multi-core CPU may show good performance
rem set NN_GC_OPT=-XX:+UseParNewGC
rem set NN_GC_OPT=-XX:+UseConcMarkSweepGC
rem ## same as default
rem set NN_GC_OPT=-XX:+UseParallelGC

rem ## debug
rem set NN_DEBUG=-agentlib:hprof=format=b
rem set NN_DEBUG=-agentlib:hprof=cpu=samples,format=a
rem set NN_DEBUG=-verbose:gc 

rem ########################################
rem # This next variable is optional. It   #
rem # is only needed if you want to run    #
rem # multiple copies of NeverNote under   #
rem # the same Linux user id.  Each        #
rem # additional copy (after the first)    #
rem # should have a unique name.  This     #
rem # permits the settings to be saved     #
rem # properly.  If you only want to run   #
rem # one copy under a single userid, this #
rem # can be commented out.                #
rem ########################################
set NN_NAME=
rem set NN_NAME="production"  



rem #################################################################
rem #################################################################
rem ## You probably don't need to change anything below this line. ##
rem #################################################################
rem #################################################################

:Loop
IF "%1"=="" GOTO Continue
if "%1" == "NN_NAME" set NN_NAME=%2
echo %NN_NAME%
shift 
shift
GOTO Loop
:Continue

rem #####################
rem # Setup environment #
rem #####################
set NN_CLASSPATH=%NEVERNOTE%nevernote.jar
set NN_CLASSPATH=%NN_CLASSPATH%;%NEVERNOTE%lib\evernote.jar
set NN_CLASSPATH=%NN_CLASSPATH%;%NEVERNOTE%lib\libthrift.jar
set NN_CLASSPATH=%NN_CLASSPATH%;%NEVERNOTE%lib\log4j-1.2.14.jar
set NN_CLASSPATH=%NN_CLASSPATH%;%NEVERNOTE%lib\h2-1.2.136.jar
set NN_CLASSPATH=%NN_CLASSPATH%;%NEVERNOTE%lib\PDFRenderer.jar
set NN_CLASSPATH=%NN_CLASSPATH%;%NEVERNOTE%lib\commons-lang-2.4.jar
set NN_CLASSPATH=%NN_CLASSPATH%;%NEVERNOTE%lib\jtidy-r938.jar
if exist "%NEVERNOTE%lib\qtjambi-win32-4.5.2_01.jar" set NN_CLASSPATH=%NN_CLASSPATH%;%NEVERNOTE%lib\qtjambi-win32-4.5.2_01.jar
if exist "%NEVERNOTE%lib\qtjambi-win32-msvc2005-4.5.2_01.jar" set NN_CLASSPATH=%NN_CLASSPATH%;%NEVERNOTE%lib\qtjambi-win32-msvc2005-4.5.2_01.jar
if exist "%NEVERNOTE%lib\qtjambi-win64-4.5.2_01.jar" set NN_CLASSPATH=%NN_CLASSPATH%;%NEVERNOTE%lib\qtjambi-win64-4.5.2_01.jar
if exist "%NEVERNOTE%lib\qtjambi-win64-msvc2005x64-4.5.2_01.jar" set NN_CLASSPATH=%NN_CLASSPATH%;%NEVERNOTE%lib\qtjambi-win64-msvc2005x64-4.5.2_01.jar

rem set NN_CLASSPATH="%NN_CLASSPATH%"

@echo on

rem java -Xmx%NN_XMX% -Xms%NN_XMS%  -XX:NewRatio=%NN_NEW_RATIO% %NN_GC_OPT% %NN_DEBUG%  -classpath "%NN_CLASSPATH%" cx.fbn.nevernote.NeverNote --name=%NN_NAME%

start /B javaw -Xmx%NN_XMX% -Xms%NN_XMS%  -XX:NewRatio=%NN_NEW_RATIO% %NN_GC_OPT% %NN_DEBUG%  -classpath "%NN_CLASSPATH%" cx.fbn.nevernote.NeverNote --name=%NN_NAME%
exit
