# Contributing to Cranium Core

## Repository role

`Cranium-Core-` preserves historical research, prototypes, receipt experiments, benchmarks, reports, and provenance materials. It is not the canonical current authority-kernel repository. The canonical authority kernel is maintained in `cranium-kernel`.

## Where changes belong

| Change type | Preferred repository |
| --- | --- |
| Canonical authority decisions or governed state transitions | `cranium-kernel` |
| Production-grade kernel tests and release validation | `cranium-kernel` |
| Historical prototype preservation | `Cranium-Core-` |
| Research tooling, receipt/export experiments, and benchmark runners | `Cranium-Core-` |
| Evidence, audit materials, and reproducibility records | `Cranium-Core-` |

Do not represent a change to this repository as a replacement for the canonical kernel boundary. If work spans both repositories, explain the relationship and keep the authoritative implementation change in `cranium-kernel`.

## Contribution expectations

- Keep changes narrowly scoped and explain their historical, research, or provenance value.
- Do not commit generated caches, local environments, build output, credentials, or private input data.
- Update `REPRODUCIBILITY.md`-relevant provenance when adding or revising reported evidence.
- Date and scope reports to the exact code, corpus, command, and environment they evaluate.
- Avoid changing historical artifacts solely to make them appear current; add a dated clarification or supersession note instead.

## Pull requests

A pull request or change description should identify the files changed, the intended repository role, any commands used to generate artifacts, and whether outputs or reports were regenerated. For a documentation-only change, state that no source or runtime behavior was validated unless validation was actually performed.

## Code of conduct

Participate respectfully, protect sensitive information, and prioritize accurate technical claims over promotional language.
