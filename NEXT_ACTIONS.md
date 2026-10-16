# Immediate Actions - High-Value Files

Based on AST analysis, here are the concrete next steps.

## Summary

- **Files Present:** 4/4 (100.0%)
- **Function parity:** 48/49 matched (target 62) — 98.0%
- **Class/type parity:** 7/7 matched (target 10) — 100.0%
- **Combined symbol parity:** 55/56 matched (target 72) — 98.2%
- **Average inline-code cosine:** 0.47 (function body across 4 matched files)
- **Average documentation cosine:** 0.48 (doc text across 4 matched files)
- **Cheat-zeroed Files:** 0
- **Critical Issues:** 3 files with <0.60 function similarity
- **Needs Review:** 0 files with 0.60-0.84 function similarity
- **Excellent:** 1 files with >=0.85 function similarity

## Priority 1: Fix Incomplete High-Dependency Files

No incomplete high-dependency files detected.

## Priority 2: Port Missing High-Value Files

Critical missing files (>10 dependencies):

No missing high-value files detected.

## Detailed Work Items

Every matched file is listed below with function and type symbol parity.

### 1. lib

- **Target:** `mimeguess.Lib`
- **Similarity:** 0.23
- **Dependents:** 0
- **Priority Score:** 14807.7
- **Functions:** 42/43 matched (target 54)
- **Missing functions:** `into_iter`
- **Types:** 5/5 matched (target 6)
- **Missing types:** _none_
- **Tests:** 8/8 matched

### 2. impl_bin_search

- **Target:** `mimeguess.ImplBinSearch`
- **Similarity:** 0.30
- **Dependents:** 0
- **Priority Score:** 407.0
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 3. impl_phf

- **Target:** `mimeguess.ImplPhf`
- **Similarity:** 0.34
- **Dependents:** 0
- **Priority Score:** 406.6
- **Functions:** 3/3 matched (target 4)
- **Missing functions:** _none_
- **Types:** 1/1 matched (target 2)
- **Missing types:** _none_

### 4. mime_types

- **Target:** `mimeguess.MimeTypes`
- **Similarity:** 1.00
- **Dependents:** 0
- **Priority Score:** 0.0
- **Functions:** 0/0 matched
- **Missing functions:** _none_
- **Types:** 0/0 matched
- **Missing types:** _none_

## Success Criteria

For each file to be considered "complete":
- **Similarity ≥ 0.85** (Excellent threshold)
- All public APIs ported
- All tests ported
- Documentation ported
- port-lint header present

