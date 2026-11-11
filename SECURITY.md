# Sicherheit / Security

## Sicherheitslücke melden

Bitte melde Sicherheitslücken **vertraulich** und **nicht** über öffentliche Issues, Pull Requests oder Diskussionen:

1. **Bevorzugt:** über GitHub unter [„Report a vulnerability“](https://github.com/FuelStat/fuelstat-android/security/advisories/new)
2. Alternativ per E-Mail an mario@caraggiu.eu

Hilfreich sind:

- App-Version bzw. Commit, Android-Version und Gerät
- betroffener Bildschirm bzw. API-Aufruf der App
- Schritte zum Nachstellen
- mögliche Auswirkung, z. B. Zugriff auf fremde Daten, abgreifbare Anmeldedaten oder Tokens

Den Eingang bestätige ich innerhalb von **3 Werktagen**. Ich halte dich über den Stand auf dem Laufenden und nenne dich nach der Behebung gern als Finder, wenn du das möchtest.

Betrifft die Lücke die API oder das Web-Frontend und nicht die App selbst, melde sie bitte im Repository [FuelStat/fuelstat](https://github.com/FuelStat/fuelstat/security/advisories/new). Bist du unsicher, reicht eine Meldung hier; ich leite sie weiter.

## Geltungsbereich

- die Android-App aus diesem Repository, jeweils der aktuelle Stand von `main` und die zuletzt veröffentlichte Version
- insbesondere: Speicherung von Anmeldedaten und Tokens (Android Keystore), Kommunikation mit der API, lokal gespeicherte Daten und zwischengespeicherte Bilder, exportierte Komponenten (Activities, Intents, Deep Links, Widgets)

Nicht im Geltungsbereich:

- die API und das Web-Frontend (siehe [FuelStat/fuelstat](https://github.com/FuelStat/fuelstat/blob/main/SECURITY.md))
- die iOS-App (siehe [FuelStat/fuelstat-ios](https://github.com/FuelStat/fuelstat-ios/blob/main/SECURITY.md))
- selbst betriebene Fuelstat-Server, die nicht unter tanken.1emc.de laufen
- Angriffe, die ein entsperrtes oder gerootetes Gerät oder eine bereits installierte Schadsoftware voraussetzen
- Plattform-Fehler von Android oder Google-Diensten
- Meldungen ohne nachvollziehbare Auswirkung, z. B. Ergebnisse automatischer Scanner ohne Prüfung

## Regeln für Tests

- Teste nur mit **eigenen Konten** und eigenen Daten. Rufe keine Daten anderer Nutzer ab, ändere oder lösche sie nicht. Sollte das versehentlich passieren, brich ab und erwähne es in der Meldung.
- Keine Last- oder Denial-of-Service-Tests gegen tanken.1emc.de, kein Social Engineering.
- Gib Details erst weiter, wenn die Lücke behoben ist oder wir uns abgestimmt haben (Coordinated Disclosure, in der Regel innerhalb von 90 Tagen).

Wer sich an diese Regeln hält und in gutem Glauben handelt, muss keine rechtlichen Schritte befürchten.

## Unterstützte Versionen

Noch gibt es keine veröffentlichte Version. Danach gibt es Sicherheitskorrekturen nur für den aktuellen Stand von `main` und die jeweils neueste veröffentlichte Version der App. Bitte aktualisiere die App, bevor du eine Lücke meldest.

---

**English summary:** Please report vulnerabilities in the Android app privately via [GitHub security advisories](https://github.com/FuelStat/fuelstat-android/security/advisories/new) or mario@caraggiu.eu, never in public issues. Issues in the API or web frontend belong in [FuelStat/fuelstat](https://github.com/FuelStat/fuelstat/security/advisories/new). Acknowledgement within 3 business days. Scope: this repository's `main` branch and the latest released app version; attacks requiring an unlocked or rooted device are out of scope. Only test with your own accounts, no DoS, no social engineering; good-faith research following these rules will not face legal action.
