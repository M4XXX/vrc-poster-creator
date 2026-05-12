# VRC Poster Creator

> A dark-themed desktop app for creating and managing VRChat DJ event posters.

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.5-orange)
![Maven](https://img.shields.io/badge/Built%20with-Maven-C71A36?logo=apachemaven)
![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-lightgrey)

---

## Table of Contents

- [About](#about)
- [Features](#features)
- [Requirements](#requirements)
- [For End Users](#for-end-users)
- [For Developers](#for-developers)
- [Building the Windows Installer](#building-the-windows-installer)
- [License](#license)

---

## About

VRC Poster Creator is a standalone Windows desktop application for VRChat event organizers. It lets you build professional DJ event posters — complete with lineup, timeslots, logos, and QR codes — through a guided UI, then export them as publication-ready images.

The app stores your DJ roster locally in SQLite, auto-saves your work between sessions, and runs entirely offline.

---

## Features

- **DJ Roster Management** — Store artist name, Discord/VRC names, genres, logo, and stream links in a local SQLite database
- **Poster Builder** — Set event title, subtitle, date, background image, club logo, and QR code
- **Timeslot Scheduler** — Assign DJs to time slots; sort by start time
- **Real-Time Preview** — See the poster update live as you edit
- **Export** — Save finished posters as JPEG or PNG
- **Session Auto-Save & Restore** — Your work is saved automatically and restored on next launch
- **Roster Import / Export** — Back up or share your DJ roster as a ZIP file
- **Dark Theme** — AtlantaFX Primer Dark throughout

---

## Requirements

| Requirement | Version |
|---|---|
| Java JDK | 21 or later |
| Maven | 3.6 or later (developers only) |
| JavaFX 21.0.5 jmods | Windows x64 (installer build only) |
| WiX Toolset | v3 (installer build only) |
| OS | Windows 10 / 11 (64-bit) |

---

## For End Users

1. Go to the [Releases](https://github.com/M4XXX/vrc-poster-creator/releases) page.
2. Download `VRCPosterCreator-1.0.0.exe` from the latest release.
3. Run the installer — it will guide you through installation and create a Start Menu entry and optional Desktop shortcut.
4. Launch **VRC Poster Creator** from the Start Menu or Desktop.

> No Java installation required — the app bundles its own JRE.

---

## For Developers

### Prerequisites

- [Java 21 JDK](https://adoptium.net/) (ensure `JAVA_HOME` is set)
- [Apache Maven 3.6+](https://maven.apache.org/download.cgi)
- Git

### Clone & Run

```bash
git clone https://github.com/M4XXX/vrc-poster-creator.git
cd vrc-poster-creator
mvn clean javafx:run
```

### Run Tests

```bash
mvn test
```

---

## Building the Windows Installer

The installer is produced by `jpackage` via the `package-win` Maven profile. It bundles a stripped JRE and produces a self-contained `.exe` installer.

### Step 1 — Install WiX Toolset v3

> **Important:** WiX v3 is required. WiX v4 has a different CLI and is not compatible.

```powershell
winget install WiXToolset.WiXToolset
```

Alternatively, download the v3 installer from the [WiX Toolset releases](https://github.com/wixtoolset/wix3/releases) and ensure `candle.exe` / `light.exe` are on your `PATH`.

### Step 2 — Download JavaFX 21.0.5 jmods

1. Go to [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/)
2. Select: **JavaFX 21.0.5 → Windows x64 → jmods**
3. Download and extract the archive (e.g., to `C:\javafx-jmods-21.0.5`)

### Step 3 — Set the `JAVAFX_JMODS` Environment Variable

**Command Prompt:**
```cmd
set JAVAFX_JMODS=C:\javafx-jmods-21.0.5
```

**PowerShell:**
```powershell
$env:JAVAFX_JMODS = "C:\javafx-jmods-21.0.5"
```

### Step 4 — Build

> `mvn clean` is required before `-Ppackage-win` to prevent stale JARs from being bundled.

```bash
mvn clean package -Ppackage-win
```

The finished installer is written to:

```
target/VRCPosterCreator-1.0.0.exe
```

---

## License

VRChat DJ Event Poster Creator — All rights reserved.
