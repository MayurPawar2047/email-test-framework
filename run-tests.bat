@echo off
setlocal
cd /d "%~dp0"
title Email Test Automation

REM ============================================================
REM  Email Test Automation - test runner
REM  Usage:  run-tests.bat [1-4]     (no argument = show menu)
REM    1 = Smoke tests          (fast gate)
REM    2 = Unit tests           (offline, no credentials needed)
REM    3 = Full suite           (QA environment)
REM    4 = Full suite           (STAGING environment)
REM  Credentials are read from local-env.bat (git-ignored).
REM ============================================================

where mvn >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven ^(mvn^) was not found on PATH. Install Maven or open a terminal where mvn works.
    pause
    exit /b 1
)

if exist "local-env.bat" call "local-env.bat"

set CHOICE=%~1
if not defined CHOICE (
    echo ==========================================
    echo   Email Test Automation
    echo ==========================================
    echo   1. Smoke tests
    echo   2. Unit tests  ^(offline^)
    echo   3. Full suite  ^(QA^)
    echo   4. Full suite  ^(STAGING^)
    echo.
    set /p CHOICE=Choose 1-4 [default 3]:
)

set SUITE=testng.xml
set TESTENV=qa
if "%CHOICE%"=="1" set SUITE=smoke.xml
if "%CHOICE%"=="2" set SUITE=unit.xml
if "%CHOICE%"=="4" set TESTENV=staging

REM Unit tests need no credentials
if "%CHOICE%"=="2" goto :run

if "%MAILOSAUR_API_KEY%"=="PASTE_YOUR_NEW_KEY_HERE" set MAILOSAUR_API_KEY=
if not defined MAILOSAUR_API_KEY (
    echo.
    echo No API key found. Put it in local-env.bat, or type it now ^(not saved^).
    set /p MAILOSAUR_API_KEY=Mailosaur API key:
)
if not defined MAILOSAUR_SERVER_ID set /p MAILOSAUR_SERVER_ID=Mailosaur server id:

:run
echo.
echo Suite: %SUITE%   Environment: %TESTENV%
echo.
call mvn clean test "-Dsuite=%SUITE%" "-Denv=%TESTENV%"
set RESULT=%ERRORLEVEL%

echo.
if exist "test-results\latest\email-test-report.html" (
    echo Opening report: test-results\latest\email-test-report.html
    start "" "test-results\latest\email-test-report.html"
) else (
    echo No report was generated - check the console output above.
)

echo.
if "%RESULT%"=="0" (
    echo RESULT: BUILD SUCCESS
) else (
    echo RESULT: BUILD FAILED, exit code %RESULT%
)
pause
exit /b %RESULT%
