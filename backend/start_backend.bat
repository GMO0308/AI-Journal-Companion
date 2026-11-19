@echo off
REM ============================================================
REM  AI Companion - Backend Launcher
REM  Put this file in the "backend" folder (next to main.py)
REM  and double-click it to start the FastAPI server.
REM ============================================================

REM --- Go to the folder this script is in (the backend folder)
cd /d "%~dp0"

REM --- Path to the virtual environment's Python
set "PY=venv\Scripts\python.exe"

echo ============================================
echo   Starting AI Companion Backend
echo ============================================
echo.

REM --- Step 1: Make sure the virtual environment exists
if not exist "%PY%" (
    echo [ERROR] Could not find %PY%
    echo Make sure this file is in the backend folder
    echo and that the "venv" folder exists.
    echo.
    pause
    exit /b 1
)

REM --- Step 2: Install the required packages into venv
REM     Uses requirements.txt if there is one, otherwise
REM     installs the packages main.py needs directly.
echo [1/2] Checking required packages...
if exist "requirements.txt" (
    "%PY%" -m pip install -q -r requirements.txt
) else (
    "%PY%" -m pip install -q fastapi uvicorn requests
)
echo       Packages are ready.
echo.

REM --- Step 3: Start the server
REM     --host 0.0.0.0  lets the Android emulator reach it (via 10.0.2.2)
REM     --port 8000     must match the port the app uses
REM     --reload        restarts the server when you edit main.py
echo [2/2] Starting server on http://localhost:8000
echo       Check it in a browser: http://localhost:8000/docs
echo       Press CTRL+C to stop.
echo.
"%PY%" -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload

REM --- Keep the window open so you can read any errors
pause
