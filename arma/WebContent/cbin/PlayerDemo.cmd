@echo off
if not "%JAVA_HOME%" == "" goto gotJdkHome
set JAVA_HOME=C:\jdk1.6.0_34
:gotJdkHome

set STITCH=""
set CONCURRENT=2000
set BATCHS=5
set INTERVALPERFETCH=10000
set TIMEOUTPERFETCH=4000

if ""%5""=="""" goto doneTimeoutPerFetch
if ""%5""=="""""" goto doneTimeoutPerFetch
set TIMEOUTPERFETCH=%5
:doneTimeoutPerFetch

if ""%4""=="""" goto doneIntervalPerFetch
if ""%4""=="""""" goto doneIntervalPerFetch
set INTERVALPERFETCH=%4
:doneIntervalPerFetch

if ""%3""=="""" goto doneBatchs
if ""%3""=="""""" goto doneBatchs
set BATCHS=%3
:doneBatchs

if ""%2""=="""" goto doneConcurrent
if ""%2""=="""""" goto doneConcurrent
set CONCURRENT=%2
:doneConcurrent

if ""%1""=="""" goto doneStitch
if ""%1""=="""""" goto doneStitch
set STITCH=%1
:doneStitch

echo JAVA_HOME=%JAVA_HOME%
echo STITCH=%STITCH%
echo CONCURRENT=%CONCURRENT%
echo BATCHS=%BATCHS%
echo INTERVALPERFETCH=%INTERVALPERFETCH%
echo TIMEOUTPERFETCH=%TIMEOUTPERFETCH%
"%JAVA_HOME%\bin\java" -classpath lib\stitch.jar test.PlayerDemo %STITCH% %CONCURRENT% %BATCHS% %INTERVALPERFETCH% %TIMEOUTPERFETCH%
