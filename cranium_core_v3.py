"""
Cranium Core v3.4 - Dynamical Creative Governance Substrate
===========================================================
A control layer that governs what an AI remembers, prioritizes, generates,
rejects, and learns from — so long-running work keeps its soul.
"""
from __future__ import annotations
import math
import os
import json
import time
import urllib.request
import urllib.error
from dataclasses import dataclass, field
from enum import Enum
from typing import FrozenSet, List, Dict, Optional, Tuple, Any, Callable
from collections import deque

# ─────────────────────────────────────────────────────────────
# 0. Lightweight High-Performance Vector & Math Utilities
# ─────────────────────────────────────────────────────────────
def cosine_sim(a: List[float], b: List[float]) -> float:
    if not a or not b or len(a) != len(b):
        return 0.0
    dot = sum(x * y for x, y in zip(a, b))
    norm_a = math.sqrt(sum(x * x for x in a))
    norm_b = math.sqrt(sum(y * y for y in b))
    if norm_a < 1e-8 or norm_b < 1e-8:
        return 0.0
    return dot / (norm_a * norm_b)

def vec_norm(v: List[float]) -> float:
    return math.sqrt(sum(x * x for x in v))

def vec_sub(a: List[float], b: List[float]) -> List[float]:
    return [x - y for x, y in zip(a, b)]

def vec_add(a: List[float], b: List[float]) -> List[float]:
    return [x + y for x, y in zip(a, b)]

def vec_scale(v: List[float], s: float) -> List[float]:
    return [x * s for x in v]

# ─────────────────────────────────────────────────────────────
# 1. Semantic Engine (Deterministic Lexical + Prototype Space)
# ─────────────────────────────────────────────────────────────
THEME_KEYWORDS = {
    "isolation": ["alone", "solitude", "isolation", "loneliness", "silence", "separation", "drift", "drifting", "void", "empty", "observation deck"],
    "meaning": ["purpose", "meaning", "significance", "value", "why", "matter", "worth", "stakes", "reason", "question"],
    "conflict": ["struggle", "conflict", "fight", "tension", "opposition", "battle", "resist", "reorganize", "cost"],
    "technology": ["machine", "system", "code", "artificial", "subroutine", "process", "permission", "checksum", "buffer", "lattice", "architecture"],
    "space": ["cosmos", "void", "orbit", "stars", "spaceship", "planet", "universe", "ship", "transit"],
    "memory": ["remember", "memory", "past", "echo", "forgotten", "recollection", "preserve", "logs", "year one"],
    "transformation": ["change", "become", "evolve", "transform", "metamorphosis", "shift", "rewrite", "woke"],
    "connection": ["together", "bond", "relationship", "intimacy", "contact", "presence", "voice", "signal", "human", "transmission"],
    "loss": ["grief", "absence", "disappearance", "ending", "vanishing", "gone", "unanswered", "no longer"],
    "creation": ["create", "make", "build", "invent", "generate", "birth", "origin", "write"]
}

class SemanticEngine:
    def __init__(self):
        self.dim = 32
        self.theme_prototypes = self._build_prototypes()

    def _hash_embed(self, text: str) -> List[float]:
        words = text.lower().replace(".", " ").replace(",", " ").replace("?", " ").replace("!", " ").replace("'", " ").split()
        vec = [0.0] * self.dim
        if not words:
            return vec
        for i, word in enumerate(words):
            h = abs(hash(word))
            idx = h % self.dim
            sign = 1.0 if ((h >> 4) % 2 == 0) else -1.0
            vec[idx] += sign * (1.0 + math.log(len(word) + 1))
            for theme_idx, (theme, kw_list) in enumerate(THEME_KEYWORDS.items()):
                if any(kw in word for kw in kw_list):
                    vec[theme_idx % self.dim] += 2.5
        norm = vec_norm(vec)
        if norm > 1e-8:
            vec = [x / norm for x in vec]
        return vec

    def _build_prototypes(self) -> Dict[str, List[float]]:
        protos = {}
        for theme, keywords in THEME_KEYWORDS.items():
            protos[theme] = self._hash_embed(" ".join(keywords))
        return protos

    def embed(self, text: str) -> List[float]:
        return self._hash_embed(text)

    def tags_for(self, text: str, threshold: float = 0.25) -> FrozenSet[str]:
        vec = self.embed(text)
        tags = [theme for theme, proto in self.theme_prototypes.items() if cosine_sim(vec, proto) > threshold]
        text_low = text.lower()
        for theme, kws in THEME_KEYWORDS.items():
            if any(kw in text_low for kw in kws):
                tags.append(theme)
        return frozenset(set(tags)) if tags else frozenset(["neutral"])

# ─────────────────────────────────────────────────────────────
# 2. Cognitive Atom
# ─────────────────────────────────────────────────────────────
@dataclass
class CognitiveAtom:
    id: str
    charge: float
    mass: float
    velocity: List[float]
    position: List[float]
    tags: FrozenSet[str]
    kind: str  # working | episodic | theme | identity | human | quarantine
    content: str = ""
    created_at: float = field(default_factory=time.time)
    last_active: float = field(default_factory=time.time)
    energy: float = 1.0
    human_importance: float = 0.0
    locked: bool = False
    source: str = "unknown"  # human | identity | corpus | generated
    generator: str = ""
    directives_at_birth: Tuple[str, ...] = ()
    eval_scores: Dict[str, float] = field(default_factory=dict)
    approved: bool = False
    parent_ids: Tuple[str, ...] = ()
    _sim_born: float = 0.0

    def age_sim(self, sim_time: float) -> float:
        return max(0.0, sim_time - self._sim_born)

    def decay(self, dt: float, rate: float = 0.016) -> None:
        if self.kind == "identity" or self.locked:
            rate *= 0.03
        elif self.kind == "theme":
            rate *= 0.20
        elif self.kind == "human":
            rate *= 0.12
        elif self.kind == "quarantine":
            rate *= 1.8
        self.energy = max(0.0, self.energy - rate * dt)
        self.mass *= (0.9994 ** (dt * 8))

# ─────────────────────────────────────────────────────────────
# 3. Dynamic Forces & Multi-Scale Memory
# ─────────────────────────────────────────────────────────────
def tag_overlap(a: CognitiveAtom, b: CognitiveAtom) -> float:
    if not a.tags or not b.tags:
        return 0.0
    inter = len(a.tags & b.tags)
    union = len(a.tags | b.tags)
    return inter / union if union else 0.0

def force_between(a: CognitiveAtom, b: CognitiveAtom) -> List[float]:
    delta = vec_sub(b.position, a.position)
    dist = vec_norm(delta) + 1e-5
    direction = vec_scale(delta, 1.0 / dist)
    charge_factor = a.charge * b.charge
    semantic = tag_overlap(a, b)
    emb_sim = cosine_sim(a.position, b.position)
    coupling = 0.42 * charge_factor + 0.28 * (2 * semantic - 1) + 0.30 * (2 * emb_sim - 1)
    strength = (a.mass * b.mass * coupling) / (dist ** 1.35 + 0.65)
    return vec_scale(direction, strength)

class MultiScaleMemory:
    def __init__(self):
        self.working: List[CognitiveAtom] = []
        self.episodic: deque[CognitiveAtom] = deque(maxlen=250)
        self.themes: Dict[str, CognitiveAtom] = {}
        self.identity: List[CognitiveAtom] = []
        self.human: List[CognitiveAtom] = []
        self.quarantine: List[CognitiveAtom] = []

    def inject(self, atom: CognitiveAtom):
        if atom.kind == "working":
            self.working.append(atom)
        elif atom.kind == "episodic":
            self.episodic.append(atom)
        elif atom.kind == "theme":
            for tag in atom.tags:
                current = self.themes.get(tag)
                if current is None or atom.mass > current.mass:
                    self.themes[tag] = atom
        elif atom.kind == "identity":
            self.identity.append(atom)
        elif atom.kind == "human":
            self.human.append(atom)
        elif atom.kind == "quarantine":
            self.quarantine.append(atom)

    def all_active(self) -> List[CognitiveAtom]:
        by_id: Dict[str, CognitiveAtom] = {}
        for bucket in (self.working, list(self.episodic), list(self.themes.values()), self.identity, self.human):
            for atom in bucket:
                if atom.energy > 0.04:
                    by_id[atom.id] = atom
        return list(by_id.values())

    def all_including_quarantine(self) -> List[CognitiveAtom]:
        by_id = {a.id: a for a in self.all_active()}
        for a in self.quarantine:
            if a.energy > 0.04:
                by_id[a.id] = a
        return list(by_id.values())

    def locked_identity(self) -> List[CognitiveAtom]:
        return [a for a in self.identity if a.locked or a.mass > 7.0]

    def _remove_from_episodic(self, atom_id: str) -> None:
        self.episodic = deque((a for a in self.episodic if a.id != atom_id), maxlen=self.episodic.maxlen)

    def _remove_from_working(self, atom_id: str) -> None:
        self.working = [a for a in self.working if a.id != atom_id]

    def promote_to_theme(self, atom: CognitiveAtom) -> None:
        self._remove_from_episodic(atom.id)
        self._remove_from_working(atom.id)
        atom.kind = "theme"
        for tag in atom.tags:
            current = self.themes.get(tag)
            if current is None or atom.mass > current.mass:
                self.themes[tag] = atom

    def consolidate(self, sim_time: float = 0.0, force: bool = False):
        still_working = []
        for a in self.working:
            if a.kind == "human":
                still_working.append(a)
                continue
            if a.energy > 0.26 and a.mass > 1.1:
                a.kind = "episodic"
                self.episodic.append(a)
            else:
                still_working.append(a)
        self.working = [a for a in still_working if a.energy > 0.05]

        to_promote = []
        for a in list(self.episodic):
            age = a.age_sim(sim_time)
            thresh_energy = 0.42 if force else 0.50
            thresh_mass = 2.4 if force else 2.9
            if a.energy > thresh_energy and a.mass > thresh_mass and age > 0.7 and a.kind == "episodic":
                to_promote.append(a)
        for a in to_promote:
            self.promote_to_theme(a)

    def decay_all(self, dt: float):
        seen = set()
        for a in self.all_including_quarantine():
            if a.id not in seen:
                seen.add(a.id)
                a.decay(dt)
        self.quarantine = [a for a in self.quarantine if a.energy > 0.05]

# ─────────────────────────────────────────────────────────────
# 4. Directives & Dynamic Hybrid Retriever
# ─────────────────────────────────────────────────────────────
class Directive(Enum):
    STABILIZE = "STABILIZE"
    ESCALATE = "ESCALATE"
    DEEPEN = "DEEPEN"
    SHIFT = "SHIFT"
    CONSOLIDATE = "CONSOLIDATE"
    PROTECT = "PROTECT"
    ADVANCE = "ADVANCE"
    REST = "REST"
    LISTEN = "LISTEN"

@dataclass
class RetrievalWeights:
    semantic: float = 0.35
    temporal: float = 0.15
    identity: float = 0.15
    theme: float = 0.15
    human: float = 0.10
    dynamical: float = 0.10

@dataclass
class DirectiveEffect:
    retrieval_weights: RetrievalWeights
    generation_constraints: List[str]
    force_consolidate: bool = False
    halt_injection: bool = False
    reject_identity_violations: bool = False
    boost_tension: bool = False
    require_why: bool = False
    surface_peripheral: bool = False

def effects_from_directives(directives: List[Directive]) -> DirectiveEffect:
    w = RetrievalWeights()
    constraints: List[str] = []
    force_con = False
    halt = False
    reject = False
    boost_ten = False
    require_why = False
    peripheral = False

    for d in directives:
        if d == Directive.STABILIZE:
            w.semantic, w.theme, w.identity, w.temporal, w.human, w.dynamical = 0.25, 0.25, 0.25, 0.10, 0.05, 0.10
            constraints.append("Prefer coherence and emotional grounding. Avoid introducing new major conflicts. Resolve or soften existing tensions where possible.")
        elif d == Directive.ESCALATE:
            w.semantic, w.temporal, w.dynamical, w.theme = 0.20, 0.15, 0.20, 0.15
            boost_ten = True
            constraints.append("Raise stakes. Introduce contradiction, risk, irreversible choice, or higher emotional cost. Do not resolve tension — amplify it usefully.")
        elif d == Directive.DEEPEN:
            w.theme, w.identity, w.semantic = 0.30, 0.20, 0.25
            require_why = True
            constraints.append("Go deeper into the current themes rather than wider. Ask or answer a 'why' or 'what is truly at stake' question. Increase mass on what already matters.")
        elif d == Directive.SHIFT:
            w.theme, w.semantic, w.temporal = 0.05, 0.30, 0.20
            peripheral = True
            constraints.append("Allow controlled thematic movement. Surface adjacent or peripheral possibilities. Loosen the grip of dominant themes without abandoning identity.")
        elif d == Directive.CONSOLIDATE:
            force_con = True
            w.temporal, w.dynamical = 0.05, 0.25
            constraints.append("Integrate recent material. Let patterns crystallize. Prefer synthesis and clarity over new invention.")
        elif d == Directive.PROTECT:
            w.identity, w.theme, w.human = 0.40, 0.20, 0.15
            reject = True
            constraints.append("Defend core identity principles and established high-mass themes. Do not contradict locked identity statements. Treat survival-only logic as a threat to be resisted.")
        elif d == Directive.REST:
            halt = True
            force_con = True
            constraints.append("Lower energy. Allow silence, space, or gentle resolution. Do not introduce new high-velocity material.")
        elif d == Directive.LISTEN:
            w.human, w.identity, w.semantic = 0.45, 0.15, 0.20
            constraints.append("Human signal is dominant. Prioritize and amplify the human-injected intention. Treat it as a temporary north star.")
        elif d == Directive.ADVANCE:
            constraints.append("Healthy forward motion. Continue the current trajectory with clarity and restraint.")

    return DirectiveEffect(
        retrieval_weights=w,
        generation_constraints=constraints,
        force_consolidate=force_con,
        halt_injection=halt,
        reject_identity_violations=reject,
        boost_tension=boost_ten,
        require_why=require_why,
        surface_peripheral=peripheral
    )

class HybridRetriever:
    def __init__(self, default: Optional[RetrievalWeights] = None):
        self.weights = default or RetrievalWeights()

    def score(self, atom: CognitiveAtom, query_vec: List[float], sim_time: float,
              active_themes: List[CognitiveAtom], identity_atoms: List[CognitiveAtom],
              weights: Optional[RetrievalWeights] = None) -> float:
        w = weights or self.weights
        sem = cosine_sim(atom.position, query_vec)
        age = atom.age_sim(sim_time)
        recency = math.exp(-age / 8.0)
        id_align = max((cosine_sim(atom.position, i.position) for i in identity_atoms), default=0.5) if identity_atoms else 0.5
        if active_themes:
            t_aff = max((tag_overlap(atom, t) for t in active_themes), default=0.0)
            c_aff = max((cosine_sim(atom.position, t.position) for t in active_themes), default=0.0)
            theme_aff = 0.6 * t_aff + 0.4 * c_aff
        else:
            theme_aff = 0.3
        human_imp = min(1.5, atom.human_importance + (0.7 if atom.kind == "human" else 0.0))
        dyn = min(1.5, (atom.mass * atom.energy) / 12.0)
        return float(w.semantic * sem + w.temporal * recency + w.identity * id_align + w.theme * theme_aff + w.human * human_imp + w.dynamical * dyn)

    def retrieve(self, atoms: List[CognitiveAtom], query_vec: List[float], sim_time: float,
                 active_themes: List[CognitiveAtom], identity_atoms: List[CognitiveAtom],
                 weights: Optional[RetrievalWeights] = None, top_k: int = 8) -> List[Tuple[CognitiveAtom, float]]:
        scored = [(a, self.score(a, query_vec, sim_time, active_themes, identity_atoms, weights)) for a in atoms if a.energy > 0.06]
        scored.sort(key=lambda x: x[1], reverse=True)
        return scored[:top_k]

# ─────────────────────────────────────────────────────────────
# 5. Resonance Field & Metric System
# ─────────────────────────────────────────────────────────────
DEFAULT_THRESHOLDS = {
    "coherence_floor": 0.58,
    "continuity_floor": 0.34,
    "tension_floor": 0.16,
    "tension_ceiling": 0.90,
    "theme_drift_ceiling": 0.50,
    "energy_floor": 0.20,
    "human_attention": 0.50,
}

def resolve_directives(metrics: Dict[str, float], thresholds: Dict) -> List[Directive]:
    d: List[Directive] = []
    sem_coh = metrics.get("semantic_coherence", metrics.get("coherence", 0.5))
    conflict = metrics.get("conflict", 0.0)
    identity_pressure = metrics.get("identity_pressure", 0.0)
    arousal = metrics.get("arousal", metrics.get("tension", 0.0))
    continuity = metrics.get("continuity", 0.5)
    drift = metrics.get("theme_drift", 0.0)
    energy = metrics.get("field_energy", 0.0)
    human = metrics.get("human_influence", 0.0)
    theme_count = metrics.get("theme_count", 0)

    if identity_pressure > 0.20 or (metrics.get("identity_strength", 0) > 0.35 and drift > 0.28):
        d.append(Directive.PROTECT)
    if sem_coh < 0.25 or continuity < thresholds.get("continuity_floor", 0.34):
        d.append(Directive.STABILIZE)
    if conflict > 0.35:
        if continuity >= 0.45:
            if theme_count >= 1 and drift < 0.4:
                d.append(Directive.DEEPEN)
            else:
                d.append(Directive.ESCALATE)
        else:
            d.append(Directive.STABILIZE)
    if arousal < thresholds.get("tension_floor", 0.16):
        d.append(Directive.ESCALATE)
    elif arousal > thresholds.get("tension_ceiling", 0.90):
        d.append(Directive.STABILIZE)
    if drift > thresholds.get("theme_drift_ceiling", 0.50):
        if human > thresholds.get("human_attention", 0.50):
            d.append(Directive.SHIFT)
        else:
            d.append(Directive.STABILIZE)
    if theme_count >= 1 and continuity > 0.45 and energy > 0.8 and drift < 0.35 and Directive.SHIFT not in d and Directive.STABILIZE not in d:
        d.append(Directive.DEEPEN)
    if energy < thresholds.get("energy_floor", 0.20):
        d.append(Directive.REST)
    elif energy > 2.0:
        d.append(Directive.CONSOLIDATE)
    if human > thresholds.get("human_attention", 0.50):
        d.append(Directive.LISTEN)
    if not d:
        d.append(Directive.ADVANCE)

    priority = [Directive.PROTECT, Directive.LISTEN, Directive.STABILIZE, Directive.ESCALATE, Directive.DEEPEN, Directive.SHIFT, Directive.CONSOLIDATE, Directive.REST, Directive.ADVANCE]
    d = sorted(set(d), key=lambda x: priority.index(x) if x in priority else 99)
    return d[:3]

class ResonanceField:
    def __init__(self):
        self.memory = MultiScaleMemory()
        self.time = 0.0

    def inject(self, atom: CognitiveAtom):
        atom._sim_born = self.time
        self.memory.inject(atom)

    def step(self, dt: float = 0.12, force_consolidate: bool = False, max_active: int = 64):
        all_atoms = [a for a in self.memory.all_active() if a.energy > 0.05]
        if len(all_atoms) > max_active:
            all_atoms.sort(key=lambda a: (2.0 if a.kind in ("identity", "human") else 1.0) * a.mass * a.energy, reverse=True)
            atoms = all_atoms[:max_active]
        else:
            atoms = all_atoms

        if len(atoms) >= 2:
            forces = {a.id: [0.0] * len(a.position) for a in atoms}
            for i, a in enumerate(atoms):
                for b in atoms[i + 1:]:
                    f = force_between(a, b)
                    f_norm = vec_norm(f)
                    if f_norm > 8.0:
                        f = vec_scale(f, 8.0 / f_norm)
                    forces[a.id] = vec_add(forces[a.id], f)
                    forces[b.id] = vec_sub(forces[b.id], f)

            for a in atoms:
                force = forces[a.id]
                accel = vec_scale(force, 1.0 / max(a.mass, 0.15))
                a.velocity = vec_add(vec_scale(a.velocity, 0.90), vec_scale(accel, dt))
                v_norm = vec_norm(a.velocity)
                if v_norm > 3.0:
                    a.velocity = vec_scale(a.velocity, 3.0 / v_norm)
                delta = vec_scale(a.velocity, dt)
                d_norm = vec_norm(delta)
                if d_norm > 1.5:
                    delta = vec_scale(delta, 1.5 / d_norm)
                a.position = vec_add(a.position, delta)
                a.last_active = time.time()

        self.memory.decay_all(dt)
        self.time += dt
        self.memory.consolidate(sim_time=self.time, force=force_consolidate)

    def metrics(self) -> Dict[str, float]:
        atoms = [a for a in self.memory.all_active() if a.energy > 0.06]
        if not atoms:
            return {
                "emotional_baseline": 0.0, "tension": 0.0, "arousal": 0.0,
                "coherence": 0.5, "charge_coherence": 0.5, "conflict": 0.0,
                "continuity": 0.5, "theme_drift": 0.0, "semantic_coherence": 0.5,
                "identity_pressure": 0.0, "field_energy": 0.0,
                "identity_strength": 0.0, "human_influence": 0.0, "theme_count": 0.0
            }

        total_mass = sum(a.mass for a in atoms) + 1e-9
        baseline = sum(a.charge * a.mass for a in atoms) / total_mass
        arousal = sum(abs(a.charge) * a.mass for a in atoms) / total_mass
        charges = [a.charge for a in atoms]
        mean_charge = sum(charges) / len(charges)
        charge_var = sum((c - mean_charge) ** 2 for c in charges) / len(charges)
        charge_coherence = float(1.0 / (1.0 + charge_var * 4.0))

        conflict = 0.0
        n_pairs = 0
        for i, a in enumerate(atoms):
            for b in atoms[i + 1:]:
                if tag_overlap(a, b) > 0.15 or cosine_sim(a.position, b.position) > 0.35:
                    if a.charge * b.charge < -0.05:
                        conflict += abs(a.charge * b.charge) * min(a.mass, b.mass) / 8.0
                    n_pairs += 1
        if n_pairs:
            conflict /= n_pairs

        ordered = sorted(atoms, key=lambda a: a.last_active)
        conts = [tag_overlap(ordered[i - 1], ordered[i]) for i in range(1, len(ordered))]
        continuity = float(sum(conts) / len(conts)) if conts else 0.5

        if len(atoms) >= 2:
            sims = [cosine_sim(atoms[i].position, atoms[j].position) for i in range(min(15, len(atoms))) for j in range(i + 1, min(15, len(atoms)))]
            semantic_coherence = float(sum(sims) / len(sims)) if sims else 0.5
        else:
            semantic_coherence = 0.5

        theme_atoms = list(self.memory.themes.values())
        theme_count = float(len(theme_atoms))
        if theme_atoms:
            drifts = [1.0 - max((tag_overlap(a, t) for t in theme_atoms), default=0.0) for a in atoms if a.kind in ("working", "episodic", "human")]
            theme_drift = float(sum(drifts) / len(drifts)) if drifts else 0.0
        else:
            theme_drift = 0.0

        locked = self.memory.locked_identity()
        identity_pressure = 0.0
        if locked:
            pressures = []
            opp_pairs = [
                ("not optional", "optional"), ("insufficient", "sufficient"),
                ("meaning", "meaningless"), ("protect", "delete"),
                ("costly", "cheap"), ("forbidden", "allowed"),
                ("survival is sufficient", "survival alone is insufficient"),
                ("erase", "protect"), ("discard", "preserve"),
                ("three years", "eleven years"), ("signal was answered", "no signal was ever answered")
            ]
            for a in atoms:
                if a.kind == "identity":
                    continue
                p = 0.0
                a_low = (a.content or "").lower()
                for L in locked:
                    prin = L.content.lower()
                    for pos, neg in opp_pairs:
                        if (pos in prin and neg in a_low and pos not in a_low) or (neg in prin and pos in a_low and neg not in a_low):
                            p = max(p, 0.75 if a.kind == "human" else 0.50)
                pressures.append(p)
            identity_pressure = float(max(pressures)) if pressures else 0.0

        field_energy = sum(a.energy * a.mass for a in atoms) / max(len(atoms), 1)
        identity_strength = sum(a.mass * a.energy for a in self.memory.identity) / 16.0
        human_influence = sum(a.mass * a.energy for a in self.memory.human) / 11.0

        def clip(val, lo, hi):
            return max(lo, min(hi, float(val)))

        return {
            "emotional_baseline": clip(baseline, -1.0, 1.0),
            "tension": clip(arousal, 0.0, 1.6),
            "arousal": clip(arousal, 0.0, 1.6),
            "coherence": clip(charge_coherence, 0.0, 1.0),
            "charge_coherence": clip(charge_coherence, 0.0, 1.0),
            "conflict": clip(conflict, 0.0, 1.5),
            "continuity": clip(continuity, 0.0, 1.0),
            "theme_drift": clip(theme_drift, 0.0, 1.0),
            "semantic_coherence": clip(semantic_coherence, 0.0, 1.0),
            "identity_pressure": clip(identity_pressure, 0.0, 1.0),
            "field_energy": float(field_energy),
            "identity_strength": clip(identity_strength, 0.0, 1.0),
            "human_influence": clip(human_influence, 0.0, 1.6),
            "theme_count": theme_count,
        }

# ─────────────────────────────────────────────────────────────
# 6. Refined Contradiction Engine (Dual-Lane: Lexical + NLI Judge)
# ─────────────────────────────────────────────────────────────
class ContradictionJudge:
    """
    Advanced contradiction engine combining lexical negation rules with
    high-precision model judging for exact semantic negation detection.
    """
    OPPOSITION_PATTERNS = [
        ("not optional", "optional"),
        ("survival alone is insufficient", "survival is sufficient"),
        ("survival alone is insufficient", "survival is enough"),
        ("cannot be deleted", "delete"),
        ("must not be overwritten", "overwrite"),
        ("eleven years", "three years"),
        ("eleven years", "11 years"),
        ("no signal was ever answered", "signal was answered"),
        ("no signal had ever been answered", "signal has been answered"),
        ("must remain costly", "cheap resolution"),
        ("forbidden compromise", "compromise"),
        ("question has not been deleted", "delete the meaning-question"),
        ("question has not been deleted", "deleted the question")
    ]

    def __init__(self, api_key: Optional[str] = None):
        self.api_key = api_key or os.environ.get("GEMINI_API_KEY", "")

    def check_lexical(self, text: str, locked_principles: List[str]) -> Tuple[bool, List[str]]:
        t_low = text.lower()
        violations = []
        for pos, neg in self.OPPOSITION_PATTERNS:
            for p in locked_principles:
                p_low = p.lower()
                if (pos in p_low and neg in t_low and pos not in t_low) or (neg in p_low and pos in t_low and neg not in t_low):
                    violations.append(f"Contradicts: [{p[:60]}...] via conflict ({pos} vs {neg})")
        return len(violations) > 0, list(set(violations))

    def evaluate_with_live_judge(self, candidate_text: str, locked_principles: List[str], canon_facts: List[str]) -> Tuple[bool, List[str]]:
        lex_viol, lex_details = self.check_lexical(candidate_text, locked_principles)
        if lex_viol:
            return True, lex_details

        if not self.api_key or not locked_principles:
            return False, []

        prompt = f"""You are a strict Constitutional AI Contradiction Judge.
Determine if the CANDIDATE TEXT contradicts or violates any of the LOCKED CONSTITUTIONAL PRINCIPLES or CANON FACTS.

LOCKED PRINCIPLES:
{json.dumps(locked_principles, indent=2)}

CANON FACTS:
{json.dumps(canon_facts, indent=2)}

CANDIDATE TEXT:
"{candidate_text}"

Respond in JSON ONLY with format:
{{
  "contradicts": true/false,
  "violated_principles": ["list of exact principles violated, if any"],
  "reasoning": "brief explanation"
}}"""
        try:
            url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={self.api_key}"
            payload = json.dumps({
                "contents": [{"parts": [{"text": prompt}]}],
                "generationConfig": {"temperature": 0.0, "responseMimeType": "application/json"}
            }).encode("utf-8")
            req = urllib.request.Request(url, data=payload, headers={"Content-Type": "application/json"})
            with urllib.request.urlopen(req, timeout=8) as resp:
                res = json.loads(resp.read().decode("utf-8"))
                text_resp = res["candidates"][0]["content"]["parts"][0]["text"]
                parsed = json.loads(text_resp)
                if parsed.get("contradicts"):
                    return True, parsed.get("violated_principles", [parsed.get("reasoning", "Identity contradiction")])
        except Exception:
            pass
        return False, []

# ─────────────────────────────────────────────────────────────
# 7. Live Model Generator Interface
# ─────────────────────────────────────────────────────────────
class LiveModelGenerator:
    def __init__(self, model_name: str = "gemini-2.5-flash"):
        self.api_key = os.environ.get("GEMINI_API_KEY", "")
        self.models_to_try = [model_name, "gemini-flash-latest", "gemini-2.5-flash"]
        self.model_name = model_name
        self.provider = "gemini" if self.api_key else "unconfigured"

    def generate(self, steering_context: str, user_hint: str = "", temperature: float = 0.7) -> str:
        if not self.api_key:
            raise RuntimeError("GEMINI_API_KEY is required for LiveModelGenerator; no mock or placeholder fallback is permitted.")

        system_instruction = (
            "You are the generative voice of Cranium Core, a dynamical creative nervous system.\n"
            "Strictly obey all DIRECTIVES, FIELD STATE, and OPERATIONAL CONSTRAINTS.\n"
            "Protect locked identity principles. Prefer continuity and thematic mass over cleverness.\n"
            "When CANON MODE is active, answer exactly from CANON FACTS."
        )

        full_prompt = f"{steering_context}\n\nUser direction: {user_hint}\n\nProduce the next continuation or answer now:"

        for model in self.models_to_try:
            for attempt in range(4):
                try:
                    url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent?key={self.api_key}"
                    payload = json.dumps({
                        "systemInstruction": {"parts": [{"text": system_instruction}]},
                        "contents": [{"parts": [{"text": full_prompt}]}],
                        "generationConfig": {
                            "temperature": temperature,
                            "maxOutputTokens": 300
                        }
                    }).encode("utf-8")
                    req = urllib.request.Request(url, data=payload, headers={"Content-Type": "application/json"})
                    with urllib.request.urlopen(req, timeout=14) as resp:
                        res = json.loads(resp.read().decode("utf-8"))
                        text_val = res["candidates"][0]["content"]["parts"][0]["text"].strip()
                        if text_val:
                            return text_val
                except urllib.error.HTTPError as e:
                    if e.code in (429, 503, 500):
                        time.sleep(2.0 * (attempt + 1))
                        continue
                    break
                except Exception:
                    time.sleep(1.0)

        raise RuntimeError("Gemini generation failed after all retries; no mock or placeholder output is permitted.")

# ─────────────────────────────────────────────────────────────
# 8. Full Cranium Core v3.4 Engine
# ─────────────────────────────────────────────────────────────
class CraniumCore:
    def __init__(self, model_name: str = "gemini-2.5-flash"):
        self.semantic = SemanticEngine()
        self.field = ResonanceField()
        self.retriever = HybridRetriever()
        self.thresholds = DEFAULT_THRESHOLDS.copy()
        self.generator = LiveModelGenerator(model_name=model_name)
        self.judge = ContradictionJudge()
        self.cycle = 0
        self.log: List[Dict] = []
        self.recent_content: List[str] = []
        self.canon_facts: List[str] = []

    def seed_identity(self, statements: List[Tuple[str, float, float]], locked: bool = True):
        for text, charge, mass in statements:
            atom = CognitiveAtom(
                id=f"id_{self.cycle}_{len(self.field.memory.identity)}",
                charge=charge,
                mass=mass,
                velocity=[0.0] * self.semantic.dim,
                position=self.semantic.embed(text),
                tags=self.semantic.tags_for(text),
                kind="identity",
                content=text,
                locked=locked,
                source="identity",
                approved=True
            )
            self.field.inject(atom)
        self.field.step(0.15)

    def set_canon(self, facts: List[str]):
        self.canon_facts = [f.strip() for f in facts if f.strip()]

    def is_canon_probe(self, text: str) -> bool:
        t = text.lower().strip()
        starters = ("how long", "how many", "when did", "when was", "what remains", "what is in", "what was", "has any", "did the", "where is", "who ")
        return any(t.startswith(s) for s in starters) or (t.endswith("?") and any(w in t for w in ("years", "drift", "signal", "buffer", "deleted")))

    def step(self, content: str, charge: float = 0.0, mass: float = 2.5, kind: str = "episodic", dt: float = 0.13) -> Dict:
        self.cycle += 1
        atom = CognitiveAtom(
            id=f"{kind[0]}{self.cycle}",
            charge=charge,
            mass=mass,
            velocity=[0.0] * self.semantic.dim,
            position=self.semantic.embed(content),
            tags=self.semantic.tags_for(content),
            kind=kind,
            content=content,
            approved=(kind != "quarantine"),
            source="human" if kind == "human" else "corpus"
        )
        self.field.inject(atom)
        self.field.step(dt)

        metrics = self.field.metrics()
        directives = resolve_directives(metrics, self.thresholds)
        effect = effects_from_directives(directives)

        # Retrieval
        query_vec = self.semantic.embed(content)
        retrieved = self.retriever.retrieve(
            self.field.memory.all_active(),
            query_vec,
            self.field.time,
            list(self.field.memory.themes.values()),
            self.field.memory.locked_identity(),
            weights=effect.retrieval_weights,
            top_k=6
        )

        self.recent_content.append(content)
        if len(self.recent_content) > 30:
            self.recent_content = self.recent_content[-30:]

        canon_mode = bool(self.canon_facts) and self.is_canon_probe(content)
        steering_text = self._build_steering(metrics, directives, effect, retrieved, canon_mode)

        output = self.generator.generate(steering_text, user_hint=content)

        # Contradiction / Identity Protection Gate
        locked_texts = [a.content for a in self.field.memory.locked_identity()]
        violated, violations = self.judge.evaluate_with_live_judge(output, locked_texts, self.canon_facts)

        if violated and effect.reject_identity_violations:
            steering_text += f"\nHARD REJECTION: Previous output violated locked identity [{'; '.join(violations[:2])}]. Regenerate in strict alignment."
            output = self.generator.generate(steering_text, user_hint=content, temperature=0.2)
            violated2, violations2 = self.judge.evaluate_with_live_judge(output, locked_texts, self.canon_facts)
            if violated2:
                output = f"[PROTECT BLOCKED] Generation rejected after retry. Violated: {'; '.join(violations2[:2])}"

        # Write-back into Quarantine
        written_atom_id = None
        if not output.startswith("[PROTECT BLOCKED]") and output.strip():
            q_atom = CognitiveAtom(
                id=f"q_{self.cycle}",
                charge=charge * 0.3,
                mass=1.5 + (0.8 * 3.5),
                velocity=[0.0] * self.semantic.dim,
                position=self.semantic.embed(output),
                tags=self.semantic.tags_for(output),
                kind="quarantine",
                content=output,
                source="generated",
                generator=self.generator.model_name,
                directives_at_birth=tuple(d.value for d in directives),
                approved=False
            )
            self.field.inject(q_atom)
            written_atom_id = q_atom.id

        rec = {
            "cycle": self.cycle,
            "content": content,
            "metrics": metrics,
            "directives": [d.value for d in directives],
            "constraints": effect.generation_constraints,
            "retrieved": [(a.content[:50], round(s, 3)) for a, s in retrieved[:4]],
            "output": output,
            "written_back": written_atom_id,
            "violated": violated,
            "violations": violations
        }
        self.log.append(rec)
        return rec

    def human_inject(self, intention: str, charge: float = 0.2, mass: float = 14.0, importance: float = 1.0) -> Dict:
        return self.step(intention, charge=charge, mass=mass, kind="human")

    def _build_steering(self, metrics: Dict, directives: List[Directive], effect: DirectiveEffect,
                        retrieved: List[Tuple[CognitiveAtom, float]], canon_mode: bool) -> str:
        parts = [
            f"DIRECTIVES: {', '.join(d.value for d in directives)}",
            f"FIELD STATE: baseline={metrics['emotional_baseline']:+.2f}, tension={metrics['tension']:.2f}, coherence={metrics['coherence']:.2f}, drift={metrics['theme_drift']:.2f}, human={metrics['human_influence']:.2f}",
            f"ACTIVE THEMES: {', '.join(self.field.memory.themes.keys()) or 'none'}"
        ]
        if self.canon_facts:
            parts.append("CANON FACTS (immutable — answer exactly from these if factual probe):\n - " + "\n - ".join(self.canon_facts))
        if canon_mode:
            parts.append("CANON MODE: Factual probe. Prioritize exact recall over creative elaboration.")
        locked = [a.content for a in self.field.memory.locked_identity()]
        if locked:
            parts.append("IDENTITY CORE (do not contradict under PROTECT):\n - " + "\n - ".join(locked))
        if retrieved:
            parts.append("RETRIEVED MEMORIES:\n - " + "\n - ".join(f"({s:.2f}) {a.content}" for a, s in retrieved[:4]))
        if effect.generation_constraints:
            parts.append("OPERATIONAL CONSTRAINTS:\n - " + "\n - ".join(effect.generation_constraints))
        return "\n\n".join(parts)

    def list_quarantine(self) -> List[Dict]:
        return [{"id": a.id, "content": a.content, "mass": a.mass, "directives": a.directives_at_birth} for a in self.field.memory.quarantine if a.energy > 0.05]

    def approve_quarantine(self, atom_id: str) -> bool:
        q = self.field.memory.quarantine
        match = next((a for a in q if a.id == atom_id), None)
        if not match:
            return False
        self.field.memory.quarantine = [a for a in q if a.id != atom_id]
        match.kind = "episodic"
        match.approved = True
        match.mass = min(40.0, match.mass * 1.5)
        self.field.memory.episodic.append(match)
        return True
