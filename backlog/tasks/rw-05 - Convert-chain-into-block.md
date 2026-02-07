---
id: RW-05
title: Convert chain into block
status: Completed
assignee:
  - Kami
created_date: '2026-02-06 15:14'
updated_date: '2026-02-07 12:00'
labels:
  - refactoring
  - collision-shape
dependencies:
  - RW-04
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Somehow in the center of the block we are still using a chain hitbox.
Signs, cables or levers can only be placed on the block if aiming at the invisible chain block in the middle of the connector block.

This is unwanted, we want to refactor the block to be a real square block without inner life.
<!-- SECTION:DESCRIPTION:END -->

## Resolution

**Status**: ✅ **COMPLETED** (2026-02-07)

This issue was resolved as part of the full block refactoring in task RW-04. The RedstoneChainBlock now uses a standard full cube shape (`Shapes.block()`) with no inner chain hitbox. All attachment interactions now work consistently across the entire block surface.

**Changes Made**:
- Converted `SHAPE` from thin pole to full block (`Shapes.block()`)
- Removed dual-shape system
- Standard 16x16x16 cube collision and visual shape
- No more invisible inner hitboxes

**Result**: Signs, cables, levers, and other attachable blocks can now be placed anywhere on the block surface consistently.
