# ColonyLink — Translation Contribution Guide

Thank you for helping translate ColonyLink!

---

## How to contribute a translation

1. **Fork** the repository on GitHub: https://github.com/am0rphe/ColonyLink
2. In your fork, navigate to:
   `src/main/resources/assets/colonylink/lang/`
3. Find the file for your language (e.g. `zh_cn.json`, `fr_fr.json`, `es_es.json`, `ru_ru.json`) or create a new one following the same format as `en_us.json`.
4. Edit the values (the part after the `:`) — **do not change the keys** (the part before the `:`).
5. Submit a **Pull Request** to the `main` branch.

---

## File format

Each line follows this pattern:
```json
"key.name": "Translated text here"
```

- The **key** (left side) must never be modified.
- The **value** (right side) is what you translate.
- Keep the JSON valid: no trailing commas on the last line, all strings in double quotes.

---

## Notes

- These translation files are community-maintained. The mod author does not speak all languages and will merge Pull Requests without reviewing the linguistic accuracy — the community is trusted on that.
- If a translation already exists and you spot an error, feel free to open a PR with a correction.
- New keys may be added in future versions. If your language file is missing a key, the game will fall back to English for that string.

---

## Available language files

| Language | File | Status |
|---|---|---|
| English | `en_us.json` | ✅ Reference |
| Chinese Simplified | `zh_cn.json` | Draft — community review welcome |
| French | `fr_fr.json` | Draft — community review welcome |
| Spanish | `es_es.json` | Draft — community review welcome |
| Russian | `ru_ru.json` | Draft — community review welcome |

---

## Pull Request link

https://github.com/am0rphe/ColonyLink/pulls
