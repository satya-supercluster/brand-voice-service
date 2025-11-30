"""
NLP Analyzer Service
Provides advanced text analysis for brand voice detection
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
import uvicorn
from typing import Dict, Any
import re
from collections import Counter
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="NLP Analyzer Service", version="1.0.0")


class AnalyzeRequest(BaseModel):
    text: str


class AnalyzeResponse(BaseModel):
    tone: str
    formality: float
    vocabulary_complexity: float
    sentence_length: str
    avg_sentence_length: float
    word_count: int
    unique_words: int
    key_phrases: list


@app.get("/health")
async def health_check():
    """Health check endpoint"""
    return {"status": "healthy", "service": "nlp-analyzer"}


@app.post("/analyze", response_model=AnalyzeResponse)
async def analyze_text(request: AnalyzeRequest):
    """
    Analyze text to extract voice characteristics
    """
    try:
        text = request.text

        if not text or len(text.strip()) < 10:
            raise HTTPException(status_code=400, detail="Text is too short for analysis")

        logger.info(f"Analyzing text of length: {len(text)}")

        # Perform analysis
        analysis = {
            "tone": detect_tone(text),
            "formality": calculate_formality(text),
            "vocabulary_complexity": calculate_vocabulary_complexity(text),
            "sentence_length": categorize_sentence_length(text),
            "avg_sentence_length": calculate_avg_sentence_length(text),
            "word_count": count_words(text),
            "unique_words": count_unique_words(text),
            "key_phrases": extract_key_phrases(text)
        }

        logger.info(f"Analysis completed: tone={analysis['tone']}, formality={analysis['formality']:.2f}")

        return analysis

    except Exception as e:
        logger.error(f"Error analyzing text: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Analysis failed: {str(e)}")


def detect_tone(text: str) -> str:
    """Detect the overall tone of the text"""
    text_lower = text.lower()

    # Tone indicators
    enthusiastic_words = ['exciting', 'amazing', 'wonderful', 'fantastic', 'incredible',
                          'awesome', 'great', 'excellent']
    professional_words = ['professional', 'enterprise', 'strategic', 'innovative',
                          'efficient', 'solution', 'optimize']
    casual_words = ['hey', 'cool', 'yeah', 'awesome', 'buddy', 'folks', 'guys']
    formal_words = ['hereby', 'pursuant', 'therefore', 'consequently', 'accordingly']

    # Count occurrences
    enthusiastic_count = sum(1 for word in enthusiastic_words if word in text_lower)
    professional_count = sum(1 for word in professional_words if word in text_lower)
    casual_count = sum(1 for word in casual_words if word in text_lower)
    formal_count = sum(1 for word in formal_words if word in text_lower)

    # Determine dominant tone
    scores = {
        'enthusiastic': enthusiastic_count,
        'professional': professional_count,
        'casual': casual_count,
        'formal': formal_count
    }

    max_score = max(scores.values())
    if max_score == 0:
        return 'neutral'

    return max(scores, key=scores.get)


def calculate_formality(text: str) -> float:
    """Calculate formality score (0-1)"""
    text_lower = text.lower()

    formal_indicators = 0
    informal_indicators = 0

    # Formal indicators
    formal_words = ['therefore', 'consequently', 'furthermore', 'moreover',
                   'nevertheless', 'accordingly', 'thus', 'hence']
    for word in formal_words:
        if word in text_lower:
            formal_indicators += 1

    # Informal indicators
    informal_words = ['hey', 'yeah', 'cool', 'awesome', 'gonna', 'wanna',
                     'kinda', 'sorta', 'literally']
    for word in informal_words:
        if word in text_lower:
            informal_indicators += 1

    # Contractions are informal
    contractions = re.findall(r"\w+\'[a-z]+", text)
    informal_indicators += len(contractions)

    # Exclamation marks are somewhat informal
    informal_indicators += text.count('!')

    total = formal_indicators + informal_indicators
    if total == 0:
        return 0.5  # Neutral

    return formal_indicators / total


def calculate_vocabulary_complexity(text: str) -> float:
    """Calculate vocabulary complexity (0-1)"""
    words = re.findall(r'\b\w+\b', text.lower())

    if not words:
        return 0.0

    # Count long words (>8 characters)
    long_words = [w for w in words if len(w) > 8]

    # Count syllables (rough approximation)
    complex_words = 0
    for word in words:
        if count_syllables(word) >= 3:
            complex_words += 1

    complexity_score = (len(long_words) + complex_words) / (len(words) * 2)
    return min(1.0, complexity_score)


def count_syllables(word: str) -> int:
    """Rough syllable counter"""
    word = word.lower()
    vowels = 'aeiouy'
    syllables = 0
    previous_was_vowel = False

    for char in word:
        is_vowel = char in vowels
        if is_vowel and not previous_was_vowel:
            syllables += 1
        previous_was_vowel = is_vowel

    # Adjust for silent 'e'
    if word.endswith('e'):
        syllables -= 1

    return max(1, syllables)


def categorize_sentence_length(text: str) -> str:
    """Categorize average sentence length"""
    avg_length = calculate_avg_sentence_length(text)

    if avg_length < 10:
        return "short"
    elif avg_length < 20:
        return "medium"
    else:
        return "long"


def calculate_avg_sentence_length(text: str) -> float:
    """Calculate average sentence length in words"""
    sentences = re.split(r'[.!?]+', text)
    sentences = [s.strip() for s in sentences if s.strip()]

    if not sentences:
        return 0.0

    total_words = 0
    for sentence in sentences:
        words = re.findall(r'\b\w+\b', sentence)
        total_words += len(words)

    return total_words / len(sentences)


def count_words(text: str) -> int:
    """Count total words"""
    words = re.findall(r'\b\w+\b', text)
    return len(words)


def count_unique_words(text: str) -> int:
    """Count unique words"""
    words = re.findall(r'\b\w+\b', text.lower())
    return len(set(words))


def extract_key_phrases(text: str, top_n: int = 5) -> list:
    """Extract key phrases (simple implementation)"""
    # Remove common stop words
    stop_words = {'the', 'a', 'an', 'and', 'or', 'but', 'in', 'on', 'at',
                  'to', 'for', 'of', 'with', 'by', 'is', 'are', 'was', 'were'}

    words = re.findall(r'\b\w+\b', text.lower())
    filtered_words = [w for w in words if w not in stop_words and len(w) > 3]

    # Get most common words
    word_counts = Counter(filtered_words)
    key_phrases = [word for word, count in word_counts.most_common(top_n)]

    return key_phrases


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8001)