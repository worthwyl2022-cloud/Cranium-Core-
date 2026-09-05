# Cranium Portfolio Map

## Role

`Cranium-Core-` is the application and cognitive-layer repository. It contains the Android application, Core runtime behavior, reproducibility guidance, and stress verification paths.

## Canonical repository relationships

- **Authority boundary:** [cranium-kernel](https://github.com/worthwyl2022-cloud/cranium-kernel) — canonical transition, replay, evidence, and authority-state boundary.
- **Conceptual substrate:** [Cranium-Substrate-](https://github.com/worthwyl2022-cloud/Cranium-Substrate-) — structured contradiction and substrate reference layer.
- **Diligence workbench:** [Substrate-Workbench-Diligence-Proof-](https://github.com/worthwyl2022-cloud/Substrate-Workbench-Diligence-Proof-) — acquisition and diligence artifacts, proofs, and review navigation.

## Boundary guidance

Application features may consume kernel decisions and substrate analyses, but they must not bypass the kernel authority boundary or treat conceptual substrate output as independently granted authority. Changes affecting authority, replay, evidence provenance, or canonical state should begin in `cranium-kernel` and be reflected here through reviewed integration.
