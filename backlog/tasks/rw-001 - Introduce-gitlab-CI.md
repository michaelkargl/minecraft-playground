---
id: RW-001
title: Introduce gitlab CI
status: To Do
assignee: []
created_date: '2026-01-28 10:17'
updated_date: '2026-01-31 08:53'
labels: []
milestone: m-0
dependencies: []
ordinal: 1000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Add gitlab CI/CD that builds and runs the tests on every peer review update. Merging is only possible if both are successful and artifact is downloadable
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 PR merges are only accepted if game tests are successfully run (run_tests.sh)
- [ ] #2 Mod is built and artifact is downloadable
<!-- AC:END -->
