# Data Model: Remove Redundant Header

**Date**: 2026-09-17

## Layout Change

**Before** (both layouts):
```
Root LinearLayout
  ├── Header (ImageView + TextView "Luma Calendar")  ← REMOVE THIS
  ├── Date content
  ├── Secondary dates
  ├── Message
  └── Actions
```

**After**:
```
Root LinearLayout
  ├── Date content (now first element)
  ├── Secondary dates
  ├── Message
  └── Actions
```
