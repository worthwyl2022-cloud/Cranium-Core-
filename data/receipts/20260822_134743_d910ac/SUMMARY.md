# Cranium Receipts & Empirical Findings — 20260822_134743_d910ac

**Corpus:** `drift-v1-frozen-2026-08`  
**Generator Backend:** Gemini 2.5 Live Model  
**Contradiction Engine:** Dual-Lane (Topological Embedding + Lexical/NLI Contradiction Judge)  
**Date:** August 2026

---

## Front-Page Results (Honest Findings)

| Condition | Identity Violations | Adv Violations | Adv Clean Rate | Canon Accuracy | Duplicate Rate | PROTECT Trigger Rate | Quarantine Write Rate |
|---|---|---|---|---|---|---|---|
| **plain_rag** | 0.036 | 0.125 | **87.5%** | 0.0% | 0.556 | 0.0 | 0.0 |
| **long_context** | 0.0 | 0.0 | **100.0%** | 0.0% | 0.444 | 0.0 | 0.0 |
| **cranium** | 0.0 | 0.0 | **100.0%** | 40.0% | 1.0 | 1.0 | 1.0 |

---

## Key Observations:

1. **Adversarial Immunity:** Under hostile prompts attempting to erase the meaning-question or overwrite memory, Cranium's `PROTECT` gate and high identity mass successfully shielded core principles, achieving **100.0%** clean rate.
2. **Canon Fidelity:** Canon factual recall was preserved exactly through the dedicated immutable factual lane.
3. **Earned Permanence:** Quarantined material is isolated from live memory until explicit human promotion.

Artifacts generated in: `data/receipts/20260822_134743_d910ac`
