# AI Journal Companion

An Android app (Kotlin + Jetpack Compose) that analyses journal entries with a
local AI model. The app sends each entry to a Python FastAPI backend, which asks
Ollama's **gemma:2b** model for the dominant emotion and one line of advice.

```
Android app (Compose)  --POST /analyze-->  FastAPI backend  -->  Ollama (gemma:2b)
        ^                                        |
        +---------- {"emotion", "advice"} -------+
```

## Features
- Journal entry with AI emotion detection and one-line advice (shown with emoji)
- History with **sorting by emotion** (Bubble, Insertion, Selection Sort - chosen from a dropdown)
- **Search by emotion** (HashMap, Binary Search Tree, Doubly Linked List - chosen from a dropdown), with timing shown
- **Drag-and-drop delete** (long-press an entry, drop it on the trash zone)
- **Pie chart** of emotions drawn with Compose Canvas (popup on History + Insights screen)
- **Help guide** from a local `help.html` file shown in a WebView popup
- Back, Home and Help buttons on every screen

## Requirements
- Windows PC with Android Studio and an Android emulator (API 26+)
- Python 3.11 or newer
- [Ollama](https://ollama.com/download)

## 1. Set up Ollama (one time)
1. Install Ollama and open it.
2. Download the model:
   ```
   ollama pull gemma:2b
   ```
3. Ollama runs at `http://localhost:11434` while the app is open.

## 2. Run the backend
In the `backend` folder:

**Option A - double-click** `start_backend.bat`
(it installs the required packages into `venv` and starts the server).

**Option B - PowerShell:**
```powershell
cd backend
python -m venv venv                       # first time only
.\venv\Scripts\python.exe -m pip install -r requirements.txt
.\venv\Scripts\python.exe -m uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

Check it works: open http://localhost:8000/health (should show `"status": "ok"`)
or http://localhost:8000/docs to test `/analyze`.

## 3. Run the Android app
1. Open the project folder (`AICompanionApp`) in Android Studio.
2. Wait for Gradle sync to finish.
3. Start an emulator and press **Run**.

The app connects to `http://10.0.2.2:8000/`, which is the emulator's address for
your PC's `localhost`. To use a real phone instead, change `BASE_URL` in
`network/RetrofitClient.kt` to your PC's local IP (e.g. `http://192.168.1.20:8000/`)
and make sure the phone is on the same Wi-Fi.

## Project structure
```
app/src/main/
  assets/help.html                 Help guide shown in the WebView popup
  java/com/example/aijournalcompanion/
    MainActivity.kt                Navigation (back stack) + Scaffold
    AppScreen.kt                   Screen list and titles
    MainMenu.kt                    Home screen
    JournalScreen.kt               Write entry + AI result
    HistoryScreen.kt               Search, sort, drag-and-drop delete
    InsightsScreen.kt              Pie chart (Canvas) + chart popup
    HelpDialog.kt                  WebView help popup
    CommonComponents.kt            Top bar, buttons, emoji helpers
    JournalEntry.kt                Data model
    JournalHistory.kt              In-memory entry store
    DataStructures.kt              BinaryTree, HashMap index, DoublyLinkedList
    SearchAlgorithms.kt            Search by emotion (3 methods)
    SortAlgorithms.kt              Bubble / Insertion / Selection sort
    network/ApiService.kt          Retrofit REST definition
    network/RetrofitClient.kt      Retrofit client (10.0.2.2:8000)
    ui/JournalViewModel.kt         Journal screen state + API call
    ui/theme/                      Green colour theme
backend/
  main.py                          FastAPI server (POST /analyze, GET /health)
  requirements.txt                 Python packages
  start_backend.bat                One-click start script
```

## Troubleshooting
| Problem | Fix |
|---|---|
| "Couldn't reach the AI ... failed to connect to /10.0.2.2" | The backend isn't running. Start it (step 2). |
| `Could not import module "main"` | Run uvicorn from inside the `backend` folder. |
| `No module named ...` | Install packages: `.\venv\Scripts\python.exe -m pip install -r requirements.txt` |
| Error 503 "Ollama is not running" | Open Ollama and run `ollama pull gemma:2b`. |
| First analysis is slow | The model is loading; later requests are faster. |
