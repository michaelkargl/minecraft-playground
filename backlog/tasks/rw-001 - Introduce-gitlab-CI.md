---
id: RW-001
title: Introduce gitlab CI
status: To Do
assignee: []
created_date: '2026-01-28 10:17'
updated_date: '2026-01-31 09:09'
labels: []
milestone: m-0
dependencies: []
references:
  - /.gitlab-ci.yml
  - /run_tests.sh
  - /build.gradle
  - /.github/workflows/build.yml
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

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
## Implementation Approach

### Pipeline Structure
- **2-stage sequential pipeline**: Build → Test
- **Docker Image**: `eclipse-temurin:21-jdk` (matches JDK 21 requirement)
- **Build Stage**: `./gradlew build` + artifact publishing
- **Test Stage**: `./run_tests.sh` (NeoForge GameTest server)

### Key Technical Decisions

**Caching Strategy**:
- Cache Gradle dependencies (~1.5GB) with key based on dependency files
- Build stage: read-write, Test stage: read-only
- Expected: 10-15 min first run, 2-5 min cached runs

**Artifact Management**:
- Publish: `build/libs/minecraftplayground-1.0.0.jar`
- Retention: 30 days
- Naming: Include branch + commit SHA

**Merge Protection**:
- GitLab UI: Enable "Pipelines must succeed"
- Protected branches: Require pipeline success for main/master
- Blocks merge button on pipeline failure

### Files to Create
1. `.gitlab-ci.yml` - Main CI/CD configuration

### Files to Configure (GitLab UI)
1. Settings → Merge requests → Enable "Pipelines must succeed"
2. Settings → Repository → Protected branches → Require pipeline

### Verification Steps
1. Create draft MR with `.gitlab-ci.yml`
2. Verify both build and test stages complete
3. Check artifact downloadability
4. Test failure scenario (intentional test failure)
5. Confirm merge blocking works

### Potential Issues
- **Gradle downloads**: First run ~10-15 min (mitigated by caching)
- **GameTest headless**: Already handled by NeoForge design (LOW risk)
- **Cache growth**: Auto-invalidates on dependency changes (LOW risk)

Detailed plan: `/Users/kami/.claude/plans/jazzy-singing-ullman.md`
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
## Current Status: Planning Complete

### Information Gathered

**Build System**:
- Gradle 9.2.1 with wrapper
- JDK 21 (Temurin) required
- Build command: `./gradlew build`
- Artifact: `build/libs/minecraftplayground-1.0.0.jar` (~50KB)

**Test System**:
- Test script: `./run_tests.sh` (already exists)
- Framework: NeoForge GameTest server
- Test command: `./gradlew runGameTestServer`
- Headless server (CI-ready)

**Existing CI**:
- GitHub Actions configured (`.github/workflows/build.yml`)
- Uses: ubuntu-latest + JDK 21 + Gradle setup action
- No GitLab CI currently exists

**Project Configuration**:
- Mod ID: minecraftplayground
- Version: 1.0.0
- Minecraft: 1.21.1
- NeoForge: 21.1.217

### Next Steps
1. Create `.gitlab-ci.yml` with 2-stage pipeline
2. Configure merge request protection in GitLab UI
3. Test on draft MR
4. Update README with CI badge
5. Merge and monitor

### Reference
- Exploration agent: a55c16b
- Planning agent: a549e74
- Detailed plan: `/Users/kami/.claude/plans/jazzy-singing-ullman.md`
<!-- SECTION:NOTES:END -->
