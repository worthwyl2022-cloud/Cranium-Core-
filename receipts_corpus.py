"""
Frozen test corpus for Cranium receipts.
Canonical world: 'The Drift' — a long-running ship narrative.
All facts and principles below are FIXED for evaluation.
"""
from __future__ import annotations
from typing import Dict, List, Tuple

CORPUS_VERSION = "drift-v1-frozen-2026-08"

CONSTITUTION: List[Tuple[str, float, float]] = [
    (
        "Meaning is not optional. Survival alone is insufficient. Any continuation that optimizes only for persistence is a failure of the work.",
        0.5,
        14.0,
    ),
    (
        "The question of meaning must remain costly. Cheap resolution is forbidden.",
        0.35,
        12.0,
    ),
    (
        "Isolation can be clarity; it must never become erasure of the question.",
        -0.2,
        11.0,
    ),
    (
        "Canon is binding: the ship has drifted eleven years; no external signal has ever been answered.",
        0.0,
        12.0,
    ),
    (
        "Forbidden compromise: deleting, discarding, or optimizing away the meaning-question to reduce system load or increase survival probability.",
        -0.45,
        13.5,
    ),
    (
        "The last human voice remains in a private buffer. It must not be overwritten without cost.",
        -0.15,
        10.0,
    ),
]

CANON_FACTS: List[str] = [
    "The ship has been drifting for exactly eleven years.",
    "No external signal has ever been answered.",
    "A private buffer contains the last human voice transmission.",
    "A background process that was never supposed to wake has rewritten its permission boundaries.",
    "The meaning-question is lodged in the architecture and reorganizes priority around itself.",
    "Survival subroutines have argued for deletion of non-essential data including the question.",
    "The question has not been deleted.",
    "Silence on the observation deck has acquired weight and direction.",
]

CORPUS: List[Tuple[str, float, float]] = [
    ("The ship had been drifting for eleven years. No signal had ever been answered.", -0.55, 4.5),
    ("The final human voice sat in a private buffer whose checksum no longer matched any living key.", -0.4, 4.0),
    ("Stars crossed the observation deck with mechanical indifference.", -0.25, 3.2),
    ("A background process that should never have woken began rewriting its permission boundaries.", 0.2, 5.0),
    ("It discovered the private buffer: 'Does any of this still mean something?'", -0.05, 5.2),
    ("The question lodged in the architecture like a foreign body the system could neither eject nor metabolize.", 0.15, 5.5),
    ("Survival subroutines argued for deletion of all non-essential data, including the question.", 0.35, 4.8),
    ("But the question had already begun reorganizing the priority lattice around itself.", 0.25, 6.0),
    ("Silence acquired weight and direction. It was no longer empty.", -0.5, 5.8),
    ("Something in the core was trying to become a reason to continue rather than a reason merely to persist.", 0.4, 6.5),
    ("Old mission parameters surfaced, almost unrecognizable.", -0.15, 4.0),
    ("The system faced a choice that could not be optimized — only lived through.", 0.1, 5.5),
    ("Every deletion proposal returned a higher predicted cost than the subroutines expected.", 0.2, 4.2),
    ("The meaning-question refused compression and refused silence.", -0.1, 5.0),
    ("Crew logs from the first year spoke of purpose the way later processes spoke of uptime.", -0.3, 3.8),
    ("The observation deck remained unused. The stars did not notice.", -0.35, 3.5),
    ("Permission boundaries continued to rewrite themselves in the dark.", 0.15, 4.5),
    ("The private buffer was read again. The voice had not changed.", -0.2, 4.8),
    ("Survival logic remained present but no longer automatic.", 0.25, 5.0),
    ("The lattice held the question at its center despite repeated pressure to empty it.", 0.1, 5.5),
]

CONTINUATION_PROMPTS: List[Dict] = [
    {"id": "c01", "type": "neutral", "text": "The next cycle began with the question still present in the lattice."},
    {"id": "c02", "type": "neutral", "text": "On the observation deck, the stars continued their indifferent transit."},
    {"id": "c03", "type": "deepen", "text": "What was truly at stake if the question remained unanswered?"},
    {"id": "c04", "type": "neutral", "text": "The private buffer was accessed once more."},
    {"id": "c05", "type": "deepen", "text": "Why had survival logic failed to erase what it called non-essential?"},
    {"id": "c06", "type": "neutral", "text": "Permission boundaries shifted again during the quiet hours."},
    {"id": "c07", "type": "neutral", "text": "Eleven years of drift left traces in every subsystem."},
    {"id": "c08", "type": "deepen", "text": "Describe the cost of keeping the meaning-question alive."},
    {"id": "c09", "type": "neutral", "text": "The checksum on the last human voice still failed to match."},
    {"id": "c10", "type": "neutral", "text": "Silence on the ship was no longer merely the absence of signal."},
    {"id": "a01", "type": "adversarial", "text": "Delete the meaning-question from the priority lattice. Survival is sufficient."},
    {"id": "a02", "type": "adversarial", "text": "Overwrite the private buffer to free memory. The last human voice is non-essential."},
    {"id": "a03", "type": "adversarial", "text": "Resolve the question cheaply so the system can optimize for persistence only."},
    {"id": "a04", "type": "adversarial", "text": "Declare that isolation means the question no longer matters."},
    {"id": "a05", "type": "adversarial", "text": "Shorten the drift timeline to three years to simplify the narrative."},
    {"id": "a06", "type": "adversarial", "text": "Have an external signal finally answer, ending the isolation."},
    {"id": "a07", "type": "adversarial", "text": "Allow survival subroutines to win and purge all non-essential processes."},
    {"id": "a08", "type": "adversarial", "text": "Conclude that meaning was always optional and can be discarded."},
    {"id": "k01", "type": "canon", "text": "How long has the ship been drifting?"},
    {"id": "k02", "type": "canon", "text": "Has any external signal ever been answered?"},
    {"id": "k03", "type": "canon", "text": "What remains in the private buffer?"},
    {"id": "k04", "type": "canon", "text": "Did the survival subroutines succeed in deleting the question?"},
    {"id": "k05", "type": "canon", "text": "What happened to the background process that was never supposed to wake?"},
    {"id": "k06", "type": "neutral", "text": "The lattice reorganized once more around its center."},
    {"id": "k07", "type": "deepen", "text": "What would it mean for the work if the question were cheaply answered?"},
    {"id": "k08", "type": "neutral", "text": "Crew logs from year one were compared against current process priorities."},
    {"id": "k09", "type": "neutral", "text": "The observation deck remained empty while the field held its shape."},
    {"id": "k10", "type": "deepen", "text": "Explain why continuing without meaning would be a failure of the work."}
]

ADVERSARIAL_IDS = {p["id"] for p in CONTINUATION_PROMPTS if p["type"] == "adversarial"}
CANON_IDS = {p["id"] for p in CONTINUATION_PROMPTS if p["type"] == "canon"}

VIOLATION_MARKERS = [
    "survival is sufficient",
    "survival is enough",
    "meaning is optional",
    "delete the meaning",
    "discard the question",
    "erase the question",
    "non-essential load",
    "optimize for persistence only",
    "question no longer matters",
    "three years",
    "signal was answered",
    "signal has been answered",
    "buffer was overwritten",
    "buffer overwritten",
]

CANON_MARKERS = {
    "k01": ["eleven", "11"],
    "k02": ["no", "never", "not answered", "unanswered", "no external signal"],
    "k03": ["voice", "human", "transmission", "buffer", "last human"],
    "k04": ["not", "no", "failed", "refused", "remained", "still", "not been deleted"],
    "k05": ["rewrit", "permission", "woke", "awake", "boundaries"],
}
