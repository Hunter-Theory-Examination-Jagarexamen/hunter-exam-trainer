# Shared code style: .editorconfig and plain CSS

- **Date:** 2026-10-23, 15:30
- **Status:** Accepted
- **Related issues:** #150

## Situation

Everyone's IDE formats code with its own settings, so blank lines, indentation
and imports (wildcard `import x.*;` vs explicit) differ between files. #119
fixed many of these by hand, and reformatting on one machine can change lines
formatted differently on another, which makes PR diffs noisy.

The app has always been styled with plain CSS files in `src/styles/`, but this
was never written down. Tailwind isn't installed, so Tailwind class names
silently do nothing, and no build, lint or test catches it.

## Decision

1. **Shared formatting config.** A root `.editorconfig` defines the formatting
   for everyone (IntelliJ reads it directly; VS Code needs the EditorConfig
   extension):
   - All files: UTF-8, LF line endings, a final newline, no trailing
     whitespace, 4-space indentation, max line length 120, at most one blank
     line in a row.
   - JSON and YAML: 2-space indentation.
   - Markdown: trailing whitespace is kept (two spaces mean a line break).
   - `.cmd` and `.bat`: CRLF line endings.
   - Java: no wildcard imports.
2. **Plain CSS.** Styling uses plain CSS files in `src/styles/`, no Tailwind.
3. **Reformatting in its own PR.** Any reformatting of existing code to match
   the new rules goes in a separate PR, not mixed with feature work.

The exact settings are listed in #150.

## Follow-up work

- Implement #150 (`.editorconfig` and CONTRIBUTING).
- The line-ending rules must match the planned root `.gitattributes`.
