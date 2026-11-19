@echo off
REM ============================================================
REM  AI Journal Companion - Backend Launcher
REM  Keep this file in the "backend" folder (next to main.py)
REM  and double-click it to start the FastAPI server.
REM
REM  It will:
REM   1. Find Python on this PC
REM   2. Check the "venv" folder works (and rebuild it if
REM      Python was moved, updated or reinstalled)
REM   3. Install the required packages
REM   4. Start the server on port 8000
REM ============================================================

REM --- Go to the folder this script is in (the backend folder)
cd /d "%~dp0"

REM --- Path to the virtual environment's Python
set "PY=venv\Scripts\python.exe"

echo ============================================
echo   Starting AI Journal Companion Backend
echo ============================================
echo.

REM ------------------------------------------------------------
REM  Step 1: Check the existing venv works
REM ------------------------------------------------------------
echo [1/4] Checking the virtual environment...
if not exist "%PY%" goto :build_venv
"%PY%" -c "import sys" >nul 2>nul
if errorlevel 1 (
    echo       The venv points to a Python that no longer exists.
    echo       It will be rebuilt.
    goto :build_venv
)
echo       venv is OK.
goto :install_packages

REM ------------------------------------------------------------
REM  Step 2: Find Python and (re)build the venv
REM ------------------------------------------------------------
:build_venv
echo.
echo [2/4] Looking for Python on this PC...
set "SYS_PY="

REM Try the Python launcher first ("py"), then "python"
py -3 -c "import sys" >nul 2>nul
if not errorlevel 1 set "SYS_PY=py -3"

if not defined SYS_PY (
    python -c "import sys" >nul 2>nul
    if not errorlevel 1 set "SYS_PY=python"
)

if not defined SYS_PY (
    echo.
    echo [ERROR] Python was not found on this PC.
    echo.
    echo  Fix:
    echo   1. Install Python from https://www.python.org/downloads/
    echo   2. In the installer, tick "Add python.exe to PATH"
    echo   3. Close this window and double-click start_backend.bat again
    echo.
    pause
    exit /b 1
)

echo       Found Python:
%SYS_PY% --version

REM Remove the broken venv, then create a new one
if exist "venv" (
    echo       Removing old venv folder...
    rmdir /s /q "venv"
)
echo       Creating a new venv (this takes a moment)...
%SYS_PY% -m venv venv
if errorlevel 1 (
    echo.
    echo [ERROR] Could not create the virtual environment.
    pause
    exit /b 1
)
echo       venv created.

REM ------------------------------------------------------------
REM  Step 3: Install the required packages into venv
REM ------------------------------------------------------------
:install_packages
echo.
echo [3/4] Checking required packages...
"%PY%" -m pip install -q --disable-pip-version-check fastapi uvicorn requests pydantic
if errorlevel 1 (
    echo.
    echo [ERROR] Package install failed. Check your internet connection.
    pause
    exit /b 1
)
echo       Packages are ready.

REM ------------------------------------------------------------
REM  Step 4: Start the server
REM     --host 0.0.0.0  lets the Android emulator reach it (via 10.0.2.2)
REM     --port 8000     must match the port the app uses
REM     --reload        restarts the server when you edit main.py
REM ------------------------------------------------------------
echo.
echo [4/4] Starting server on http://localhost:8000
echo       Check it in a browser: http://localhost:8000/docs
echo       Remember: Ollama must also be running (ollama pull gemma:2b)
echo       Press CTRL+C to stop.
echo.
"%PY%" -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload

REM --- Keep the window open so you can read any errors
pause
