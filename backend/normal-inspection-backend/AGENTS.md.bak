# Project conventions — normal-inspection-backend

## Tooling rules

Кодовая база — Kotlin / Gradle multi-module. Инструменты выбирать по задаче:

| Задача                                                           | Инструмент                   |
|------------------------------------------------------------------|------------------------------|
| Структура файла / директории (символы, сигнатуры)                | `aft_outline`                |
| Прочитать конкретный символ (функция/класс) с телом              | `aft_zoom`                   |
| Health / диагностики / dead code / TODO после правок             | `aft_inspect`                |
| Cross-file поиск: «кто вызывает X», «где используется Y», usages | `grep` (инструмент, НЕ bash) |
| Поиск файлов по имени/паттерну                                   | `glob`                       |
| Чтение конкретного файла / секции строк                          | `read`                       |

### Запрещено

- `bash` для поиска/чтения кода (`grep`, `rg`, `find`, `cat`, `sed`, `awk`). Только `git diff`/`git log`/`git show` и
  запуски сборки/тестов.
- `aft_outline` / `aft_zoom` для cross-file поиска usages — они не для этого.
- `grep` для чтения структуры одного файла — бери `aft_outline`.

### After edits

- Пакет правок → `aft_inspect` проверить E/W (диагностики).
- Перед коммитом → реальный gate: `./gradlew compileKotlin` (AFT-Kotlin-LSP может давать ложные ошибки на
  metadata-несовместимости, см. ниже).

## Known issue: AFT Kotlin LSP metadata mismatch

AFT's bundled Kotlin language server uses Kotlin compiler 2.1.0. Project compiles with Kotlin 2.3.21.
Result: `aft_inspect` reports false "incompatible metadata version 2.3.0" errors on cross-module symbols.
These are **false positives** — real build via `./gradlew` succeeds. Ignore AFT Kotlin diagnostics for version-mismatch;
trust Gradle.

## Module layout

- `normal-inspection-backend-common` — shared contracts (`IFileStorage`, `IFileReceiver`, `FileResult`).
- `normal-inspection-backend-file-storage` — disk `FileStorage` impl.
- `normal-inspection-backend-file-receiver` — `FileReceiver` orchestration.
- `normal-inspection-backend-ktor-app` — Ktor routes (`FileRouter`).

## Conventions

- Errors: domain `sealed class` result types (`FileResult.Success`/`Failure`/`FileNotProvided`). Encode failures in the
  type, don't throw across module boundary.
- Coroutines: blocking I/O inside `withContext(Dispatchers.IO)`. Always rethrow `CancellationException` first in any
  `catch`; never log it. Catch narrow types (`IOException`), not `Exception`/`Throwable`.
- Logging: SLF4J (`LoggerFactory.getLogger`), logback-classic already in deps. No new logging lib unless justified.
- Resource ownership: who opens, closes — wrap in `.use {}` at the creation site (e.g. `InputStream` born in
  `FileRouter`).
- Config: versions centralized in `gradle/libs.versions.toml`.
