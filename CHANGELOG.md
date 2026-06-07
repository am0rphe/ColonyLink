# Changelog

All notable changes to ColonyLink are documented in this file.

## [1.5.2] - 2026-06-07

### Added
- **Full localization (i18n) support.** Every in-game string - GUI labels, tooltips,
  chat/log messages and item descriptions - is now externalized into language files
  under `src/main/resources/assets/colonylink/lang/`.
- **Complete translations** for Simplified Chinese (`zh_cn`), French (`fr_fr`),
  Russian (`ru_ru`) and Spanish (`es_es`).

### Changed
- Server-side messages are now sent as translatable `Component`s instead of raw
  strings, so they are localized on each player's own client.

### Fixed
- Tooltip line breaks no longer render as a missing-glyph box; multi-line tooltips
  are now split correctly before rendering.

### Translations - help us make them better
The `zh_cn`, `fr_fr`, `ru_ru` and `es_es` translations were written entirely by
**Claude (Anthropic)**. They are a best-effort starting point and **are subject to
change** - some wording (especially MineColonies-specific vocabulary, which could not
be sourced automatically) may not match what native speakers expect.

If a translation reads wrong, please help fix it:

- **Join the Discord:** https://discord.gg/jjf2ZDQScC - tell us what is off, or
- **Open a Pull Request** on GitHub: edit the relevant
  `src/main/resources/assets/colonylink/lang/<lang>.json` file and, in your PR
  description, give the **line number** and the **corrected value** so it is quick
  to review and merge.

**About button labels:** UI buttons have very limited width, so several labels are
intentionally abbreviated (for example `Crear Alm.` / `Enviar Alm.` in Spanish,
`Созд. Скл.` / `Отпр. Скл.` in Russian). I tried to keep the wording consistent and
correct while still fitting the space available, so when proposing changes to button
text please keep your replacement roughly the same length so it still fits on screen.
