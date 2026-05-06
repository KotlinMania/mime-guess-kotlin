# mime-guess-kotlin in Kotlin

[![GitHub link](https://img.shields.io/badge/GitHub-KotlinMania%2Fmime--guess--kotlin-blue.svg)](https://github.com/KotlinMania/mime-guess-kotlin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kotlinmania/mime-guess-kotlin)](https://central.sonatype.com/artifact/io.github.kotlinmania/mime-guess-kotlin)
[![Build status](https://img.shields.io/github/actions/workflow/status/KotlinMania/mime-guess-kotlin/ci.yml?branch=main)](https://github.com/KotlinMania/mime-guess-kotlin/actions)

This is a Kotlin Multiplatform line-by-line transliteration port of [`abonander/mime_guess`](https://github.com/abonander/mime_guess).

**Original Project:** This port is based on [`abonander/mime_guess`](https://github.com/abonander/mime_guess). All design credit and project intent belong to the upstream authors; this repository is a faithful port to Kotlin Multiplatform with no behavioural changes intended.

### Porting status

This is an **in-progress port**. The goal is feature parity with the upstream Rust crate while providing a native Kotlin Multiplatform API. Every Kotlin file carries a `// port-lint: source <path>` header naming its upstream Rust counterpart so the AST-distance tool can track provenance.

---

## Upstream README — `abonander/mime_guess`

> The text below is reproduced and lightly edited from [`https://github.com/abonander/mime_guess`](https://github.com/abonander/mime_guess). It is the upstream project's own description and remains under the upstream authors' authorship; links have been rewritten to absolute upstream URLs so they continue to resolve from this repository.

## mime_guess ![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/abonander/mime_guess/rust.yml?branch=master) [![Crates.io](https://img.shields.io/crates/v/mime_guess.svg)](https://crates.io/crates/mime_guess)

MIME/MediaType guessing by file extension. 
Uses a static map of known file extension -> MIME type mappings.

**Returning Contributors: New Requirements for Submissions Below**

##### Required Rust Version: 1.33

#### [Documentation](https://docs.rs/mime_guess/)

### Versioning

Due to a mistaken premature release, `mime_guess` currently publicly depends on a pre-1.0 `mime`,
which means `mime` upgrades are breaking changes and necessitate a major version bump. 
Refer to the following table to find a version of `mime_guess` which matches your version of `mime`:

| `mime` version | `mime_guess` version |
|----------------|----------------------|
| `0.1.x, 0.2.x` | `1.x.y` |
| `0.3.x`        | `2.x.y` |

#### Note: MIME Types Returned Are Not Stable/Guaranteed
The media types returned for a given extension are not considered to be part of the crate's
 stable API and are often updated in patch (`x.y.z + 1`) releases to be as correct as possible. MIME
 changes are backported to previous major releases on a best-effort basis.
 
Note that only the extensions of paths/filenames are inspected in order to guess the MIME type. The
file that may or may not reside at that path may or may not be a valid file of the returned MIME type.
Be wary of unsafe or un-validated assumptions about file structure or length.

An extension may also have multiple applicable MIME types. When more than one is returned, the first
is considered to be the most "correct"--see below for elaboration.

Contributing
-----------

#### Adding or correcting MIME types for extensions

Is the MIME type for a file extension wrong or missing? Great! 
Well, not great for us, but great for you if you'd like to open a pull request! 

The file extension -> MIME type mappings are listed in `src/mime_types.rs`. 
**The list is sorted lexicographically by file extension, and all extensions are lowercase (where applicable).** 
The former is necessary to support fallback to binary search when the 
`phf-map` feature is turned off, and for the maintainers' sanity.
The latter is only for consistency's sake; the search is case-insensitive.

Simply add or update the appropriate string pair(s) to make the correction(s) needed. 
Run `cargo test` to make sure the library continues to work correctly.

#### Important! Citing the corrected MIME type 

When opening a pull request, please include a link to an official document or RFC noting 
the correct MIME type for the file type in question **in the commit message** so
that the commit history can be used as an audit trail.

Though we're only guessing here, we like to be as correct as we can. 
It makes it much easier to vet your contribution if we don't have to search for corroborating material.

#### Multiple MIME types per extension
As of `2.0.0`, multiple MIME types per extension are supported. The first MIME type in the list for 
a given extension should be the most "correct" so users who only care about getting a single MIME 
type can use the `first*()` methods.

The definition of "correct" is open to debate, however. In the author's opinion this should be 
whatever is defined by the latest IETF RFC for the given file format, or otherwise explicitly 
supercedes all others.

If an official IANA registration replaces an older "experimental" style media type, please
place the new type before the old type in the list, but keep the old type for reference:

```
- ("md", &["text/x-markdown"]),
+ ("md", &["text/markdown", "text/x-markdown"]),
```

#### Changes to the API or operation of the crate

We're open to changes to the crate's API or its inner workings, breaking or not, if it improves the overall operation, efficiency, or ergonomics of the crate. However, it would be a good idea to open an issue on the repository so we can discuss your proposed changes and decide how best to approach them.


License
-------

MIT (See the `LICENSE` file in this repository for more information.)

---

## About this Kotlin port

### Installation

```kotlin
dependencies {
    implementation("io.github.kotlinmania:mime-guess-kotlin:0.1.0-SNAPSHOT")
}
```

### Building

```bash
./gradlew build
./gradlew test
```

### Targets

- macOS arm64
- Linux x64
- Windows mingw-x64
- iOS arm64 / simulator-arm64 (Swift export + XCFramework)
- JS (browser + Node.js)
- Wasm-JS (browser + Node.js)
- Android (API 24+)

### Porting guidelines

See [AGENTS.md](AGENTS.md) and [CLAUDE.md](CLAUDE.md) for translator discipline, port-lint header convention, and Rust → Kotlin idiom mapping.

### License

This Kotlin port is distributed under the same MIT license as the upstream [`abonander/mime_guess`](https://github.com/abonander/mime_guess). See [LICENSE](LICENSE) (and any sibling `LICENSE-*` / `NOTICE` files mirrored from upstream) for the full text.

Original work copyrighted by the mime_guess authors.  
Kotlin port: Copyright (c) 2026 Sydney Renee and The Solace Project.

### Acknowledgments

Thanks to the [`abonander/mime_guess`](https://github.com/abonander/mime_guess) maintainers and contributors for the original Rust implementation. This port reproduces their work in Kotlin Multiplatform; bug reports about upstream design or behavior should go to the upstream repository.
