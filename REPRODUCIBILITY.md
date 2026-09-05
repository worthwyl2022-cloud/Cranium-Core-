# Reproducibility and Evidence Policy

## Purpose

This repository preserves historical research, prototypes, receipt experiments, benchmarks, and audit materials. It is not the canonical authority-kernel implementation; that role belongs to `cranium-kernel`.

A report, receipt, benchmark result, or generated artifact in this repository is evidence only for the specific execution it documents. It is not, by itself, evidence of current production behavior, independent certification, or a generalized safety claim.

## Minimum provenance record

When producing or updating a research result, retain a provenance record containing:

- Source repository and immutable commit SHA.
- Command or script entry point, including all material arguments.
- Interpreter, runtime, dependency versions, and operating-system/environment details.
- Input corpus, fixture, configuration, and seed identifiers or hashes.
- Execution timestamp and timezone.
- Generated outputs, logs, receipts, and any validation results needed to inspect the claim.
- Known limitations, exclusions, and any manual steps.

## Reporting requirements

- Date reports and state the exact scope of the artifact they evaluate.
- Label generated reports as historical/research output unless they are independently reproducible from retained inputs and commands.
- Do not call an artifact “latest,” “current,” or “production” unless it identifies the source revision and execution context that justify that label.
- Do not overwrite prior evidence in a way that obscures provenance; preserve prior artifacts or clearly record their supersession.

## Reproduction checklist

Before relying on a result, verify that another reviewer can identify the source commit, obtain the declared inputs, run the stated command in a comparable environment, and compare retained outputs with the reported claim. If any element is unavailable, state the limitation explicitly.

## Canonical implementation boundary

New canonical authority-kernel behavior, governed state transitions, and production-grade kernel validation belong in `cranium-kernel`. Work in this repository should preserve research provenance and should not be represented as replacing that implementation boundary.
