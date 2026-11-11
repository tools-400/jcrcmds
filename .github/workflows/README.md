# GitHub Actions Workflows

## release.yml — Release-Veröffentlichung

Veröffentlicht bei einem Release-Build die folgende Datei als Asset eines GitHub Release:

- `docs/files/rdi8.0/JCRCMDS for RDi <RDi-Version>+ (v<VERSION> Update Site).zip`

JCRCMDS hat kein Handbuch (`build.project.doc.enable=false`), deshalb wird — anders als bei den übrigen Produkten — keine PDF veröffentlicht.

### Trigger

- **Push auf `main`**, der `docs/files/rdi8.0/` ändert, also der Commit „Release v…“ mit dem neuen Update-Site-ZIP. Die Version stammt aus dem Namen des ZIPs (`(v<VERSION> Update Site).zip`). Fehlt der Tag `v<VERSION>`, legt der Workflow ihn auf dem Commit an, der das ZIP zuletzt geändert hat, und erstellt das Release.
- **Push eines Tags** im Release-Format `v<major>.<minor>.<patch>.r`, z. B. `v1.0.5.r`. Der Tag bestimmt die Version.
- **Manueller Start** (*Run workflow*) wie ein Push auf `main`. Mit `dry_run` (Voreinstellung) zeigt er nur an, welcher Commit getaggt würde, und prüft die Assets.

Beta-Versionen (`v1.0.5.b001` usw.) werden bewusst ignoriert. Ihre ZIPs liegen unter `docs/beta-version/` und lösen den Workflow nicht aus.

### Ablauf

1. **Checkout** des Repos mit allen Commits und Tags.
2. **Release bestimmen**: aus dem gepushten Tag oder aus dem Namen des ZIPs in `docs/files/rdi8.0/`. Existiert der Tag auf GitHub schon, endet der Workflow ohne Änderung.
3. **Tag anlegen** (nur beim Push auf `main` und beim manuellen Start ohne `dry_run`): annotiert, Nachricht `Tag v<VERSION>.`, Tagger `github-actions[bot]`. Tag und Release entstehen im selben Lauf, weil ein mit `GITHUB_TOKEN` angelegter Tag keinen weiteren Workflow auslöst.
4. **Assets validieren** auf dem Stand des Tags: Im Verzeichnis `docs/files/rdi8.0/` muss genau ein ZIP existieren, dessen Name auf `(v<VERSION> Update Site).zip` endet. Bei 0 oder mehreren Treffern bricht der Workflow mit klarer Fehlermeldung ab.
5. **GitHub Release erstellen** mit Titel `JCRCMDS v<VERSION>` und dem ZIP als Asset (`softprops/action-gh-release`).

### Veröffentlichen

Den Commit „Release v<VERSION>.“ auf `main` pushen. Tag und Release entstehen automatisch. Lokal holt `git fetch --tags` den Tag.

Ein Tag lässt sich weiterhin von Hand pushen, z. B. um ein Release neu zu erstellen:

```bash
git tag -a v1.0.5.r -m "Tag v1.0.5.r."
git push origin v1.0.5.r
```

### Tag-Schema

Bis Version 1.0.4 wurde im Quellcode-Repo nach dem Schema `v001.000.004` getaggt. Ab 1.0.5 gilt das Schema `v<major>.<minor>.<patch>.r`, auf das dieser Workflow reagiert. Die Alt-Tags liegen in [jcrcmds-plugin](https://github.com/tools-400/jcrcmds-plugin).

### Vorbedingung

Das Update-Site-ZIP muss unter dem Release-Namen in `docs/files/rdi8.0/` liegen. Heißt das ZIP noch `... (v1.0.5.b010 Update Site).zip`, muss es vorher in `... (v1.0.5.r Update Site).zip` umbenannt und committet werden.

### Berechtigungen

Der Job läuft mit `contents: write`, was zum Anlegen von Tag und Release nötig ist. Es werden keine zusätzlichen Secrets benötigt — `GITHUB_TOKEN` reicht.

### Beta-Builds

Aktuell nicht im Scope. Falls Beta-Tags (`v*.b*`) später ebenfalls automatisch als Pre-Release veröffentlicht werden sollen, lässt sich das über einen zweiten Tag-Filter und `prerelease: true` ergänzen.
