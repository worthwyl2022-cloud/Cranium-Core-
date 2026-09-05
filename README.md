# Cranium Core — Historical Research Repository

> **Status:** Historical research, evidence, and prototype repository. This is **not** the canonical current authority-kernel implementation.

The canonical Kotlin authority kernel is maintained in [`cranium-kernel`](https://github.com/worthwyl2022-cloud/cranium-kernel). Python tooling in this repository supports research, receipt/export experiments, stress benchmarks, and report generation; it does not replace the kernel's governed authority boundary.

See [STATUS.md](STATUS.md) for repository role, claim boundaries, and maintenance policy.

## Contents

This repository preserves earlier Cranium Core work, including Kotlin/Gradle structure, Python prototypes, receipt runners, benchmark scripts, audit reports, and related technical documentation. Preserve this material as provenance and research history.

## Evidence boundary

Treat any reported result as scoped to the named code revision, corpus, command, and environment. Reports and static artifacts do not establish production deployment, independent security certification, or generalized AI-safety claims by themselves.

## Local hygiene

Generated Python caches, virtual environments, Gradle output, local configuration, and secrets are excluded through `.gitignore`.
