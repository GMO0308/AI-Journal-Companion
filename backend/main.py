"""
AI Journal Companion - FastAPI backend.

Receives journal entries from the Android app (POST /analyze), asks the
local Ollama gemma:2b model for the dominant emotion and one line of advice,
and returns them as JSON: {"emotion": "JOY", "advice": "..."}
"""

import json
import re

import requests
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel

app = FastAPI(title="AI Journal Companion Backend")

OLLAMA_URL = "http://localhost:11434/api/generate"
MODEL_NAME = "gemma:2b"
VALID_EMOTIONS = {"JOY", "SADNESS", "ANGER", "FEAR", "SURPRISE"}
DEFAULT_ADVICE = "Take a moment for yourself today."


class JournalRequest(BaseModel):
    """Body sent by the app: {"text": "..."}"""
    text: str


@app.post("/analyze")
def analyze_entry(request: JournalRequest):
    """Analyse a journal entry and return its dominant emotion and advice."""
    if not request.text.strip():
        raise HTTPException(status_code=400, detail="Journal text is empty.")

    prompt = f"""
    Analyze the following journal entry.
    Return ONLY a JSON object with two fields:
    "emotion": (one of: JOY, SADNESS, ANGER, FEAR, SURPRISE)
    "advice": (a one-line supportive sentence)

    Journal Entry: "{request.text}"
    """

    try:
        # Plain "def" endpoint: FastAPI runs it in a worker thread, so this
        # blocking call does not freeze the server. Timeout allows for model loading.
        response = requests.post(
            OLLAMA_URL,
            json={
                "model": MODEL_NAME,
                "prompt": prompt,
                "stream": False,
                "format": "json",
            },
            timeout=120,
        )
    except requests.exceptions.ConnectionError:
        raise HTTPException(
            status_code=503,
            detail="Ollama is not running. Start Ollama and run: ollama pull gemma:2b",
        )
    except requests.exceptions.Timeout:
        raise HTTPException(status_code=504, detail="Ollama took too long to respond.")

    if response.status_code != 200:
        raise HTTPException(status_code=502, detail=f"Ollama error: {response.text}")

    raw_content = response.json().get("response", "")

    # Clean up potential markdown formatting (```json ... ```)
    clean_json = re.sub(r"```json\s?|\s?```", "", raw_content).strip()

    try:
        result = json.loads(clean_json)
    except json.JSONDecodeError:
        print(f"Could not parse model output: {raw_content}")
        raise HTTPException(status_code=502, detail="The AI returned an invalid response.")

    # Make sure the app always receives the two expected fields
    emotion = str(result.get("emotion", "")).strip().upper()
    if emotion not in VALID_EMOTIONS:
        emotion = "NEUTRAL"
    advice = str(result.get("advice", "")).strip() or DEFAULT_ADVICE

    return {"emotion": emotion, "advice": advice}


@app.get("/health")
def health_check():
    """Simple check that the backend is running."""
    return {"status": "ok", "service": "AI Journal Companion Backend"}
