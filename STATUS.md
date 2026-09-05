# Repository Status

## Role

`Cranium-Core-` is a historical research and evidence repository. It preserves earlier Kotlin/Python prototypes, receipt experiments, runners, stress benchmarks, and audit materials.

## Canonical implementation boundary

The canonical authority-kernel implementation is maintained in [`cranium-kernel`](https://github.com/worthwyl2022-cloud/cranium-kernel). The authority decision and governed state-transition boundary belong there.

This repository must not be presented as the current production or canonical authority kernel. Its Python material is useful for research, reproducibility experiments, receipt/report generation, and historical provenance.

## Claim boundary

Artifacts in this repository are historical or research evidence unless a specific artifact identifies its source commit, execution command, environment, and retained result bundle. A report alone is not a substitute for a reproducible execution.

## Maintenance policy

- Preserve meaningful historical code and documentation.
- Do not commit generated Python caches, local environments, secrets, or build output.
- Keep reports dated and scoped to the code and corpus they actually evaluate.
- Place new canonical Kotlin kernel behavior and automated tests in `cranium-kernel`.
