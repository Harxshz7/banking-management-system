@echo off
echo ============================================
echo   BankPro - Compiling...
echo ============================================

:: Create output directory
if not exist "out" mkdir out

:: Collect all .java files
set SRC=
for /r "src" %%f in (*.java) do set SRC=!SRC! "%%f"

:: Compile using javac
setlocal enabledelayedexpansion
set FILES=
for /r "src" %%f in (*.java) do (
    set FILES=!FILES! "%%f"
)

javac --release 25 -encoding UTF-8 -d out !FILES!

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [OK] Compilation successful!
    echo Run 'run.bat' to launch BankPro.
) else (
    echo.
    echo [ERROR] Compilation failed. Check errors above.
)
pause
