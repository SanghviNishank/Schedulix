<div align="center">

<img src="https://img.shields.io/badge/Schedulix-Smart%20Timetable%20Generator-4A90E2?style=for-the-badge&logo=java&logoColor=white" alt="Schedulix"/>

# 📅 Schedulix

### *Smart Timetable Generator — Built for Students, Teachers & Institutions*

**Schedulix** is a powerful, open-source desktop application that automatically generates optimized **daily class timetables** and **exam schedules** — eliminating conflicts, saving hours, and making academic planning effortless.

<br/>

[![Java](https://img.shields.io/badge/Java-JDK%2017+-ED8B00?style=flat-square&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17+-007396?style=flat-square&logo=java&logoColor=white)](https://gluonhq.com/products/javafx/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey?style=flat-square)]()
[![Status](https://img.shields.io/badge/Status-Active%20Development-brightgreen?style=flat-square)]()
[![PRs Welcome](https://img.shields.io/badge/PRs-Welcome-ff69b4?style=flat-square)](CONTRIBUTING.md)

<br/>

[🚀 Get Started](#%EF%B8%8F-installation--setup) · [✨ Features](#-features) · [🤝 Contribute](#-contributing) · [📬 Contact](#-author--contact)

---

</div>

## 📌 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Screenshots](#-screenshots)
- [Tech Stack](#%EF%B8%8F-tech-stack)
- [Project Structure](#-project-structure)
- [Installation & Setup](#%EF%B8%8F-installation--setup)
- [How to Run](#%EF%B8%8F-how-to-run)
- [How It Works](#-how-it-works)
- [Future Improvements](#-future-improvements)
- [Contributing](#-contributing)
- [License](#-license)
- [Author & Contact](#-author--contact)

---

## 🧠 Overview

Scheduling timetables manually is slow, error-prone, and frustrating — especially when you're juggling dozens of subjects, multiple teachers, shared classrooms, and strict constraints.

**Schedulix** solves this completely.

It is a **Java + JavaFX desktop application** that takes your inputs — subjects, time slots, teachers, rooms, and constraints — and generates a fully optimized, conflict-free timetable in seconds. Whether you need a weekly class schedule or a full exam timetable for an entire department, Schedulix handles it cleanly and efficiently.

> Built as a desktop-first application, Schedulix runs entirely **offline**. No accounts. No internet. No servers.

**Who is this for?**

| 🎓 Students | 👩‍🏫 Teachers | 🏫 Institutions | 📋 Coordinators |
|:---:|:---:|:---:|:---:|
| Plan personal study schedules | Manage class and lecture slots | Schools, colleges & coaching centers | Automate full-semester exam planning |

---

## ✨ Features

### 🗓️ Daily Timetable Generator
- Add subjects, teachers, and available time slots via a clean input form
- Set hard constraints — no back-to-back lectures, preferred slots, teacher availability windows
- Instantly generates a balanced, conflict-free weekly class timetable
- Handles multiple classes and sections simultaneously
- Visual timetable grid view for easy review

### 📝 Exam Timetable Generator
- Input exam subjects, durations, student groups, and available exam dates
- Automatically avoids exam clashes for students sharing subjects
- Distributes exams evenly — no three exams crammed into a single day
- Supports gap-day constraints between consecutive heavy exams
- Produces a clean, ready-to-share exam schedule

### 🖥️ Modern Desktop UI (JavaFX)
- Clean, professional interface with intuitive navigation
- Organized input forms — no technical knowledge required
- Timetable output displayed in a color-coded, structured grid
- Fully offline — runs on Windows, macOS, and Linux without any setup beyond Java

### ⚙️ Smart Scheduling Engine
- Constraint-satisfaction based scheduling algorithm
- Detects and resolves conflicts automatically
- Handles edge cases — overloaded days, missing slots, double-booked teachers
- Fast: even complex, multi-section schedules generate in under a second

---

## 📸 Screenshots

> UI screenshots will be added with the first stable release. Stay tuned!

| 🏠 Dashboard / Home | 📋 choice screen |
|:---:|:---:|
| ![Dashboard](Screenshot%20(632).png) | ![choice screen](Screenshot%20(633).png) |

| 🗓️ Daily Timetable Output | 📝 Exam Schedule Output |
|:---:|:---:|
| ![Daily Timetable](Screenshot%20(634).png) | ![Exam Schedule](Screenshot%20(635).png) |

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| **Language** | Java (JDK 17+) |
| **UI Framework** | JavaFX 17+ |
| **UI Markup** | FXML |
| **Styling** | JavaFX CSS |
| **Build Tool** | Maven / Gradle |
| **Architecture** | MVC (Model-View-Controller) |
| **IDE Recommended** | IntelliJ IDEA / Eclipse |
| **Platform** | Desktop — Windows, macOS, Linux |

---

## 📁 Project Structure

```
Schedulix/
│
├── src/
│   └── main/
│       ├── java/
│       │   └── com/schedulix/
│       │       ├── Main.java                      # Application entry point
│       │       │
│       │       ├── controllers/                   # JavaFX UI Controllers
│       │       │   ├── HomeController.java
│       │       │   ├── DailyTimetableController.java
│       │       │   └── ExamTimetableController.java
│       │       │
│       │       ├── models/                        # Data Models
│       │       │   ├── Subject.java
│       │       │   ├── TimeSlot.java
│       │       │   ├── Teacher.java
│       │       │   └── Constraint.java
│       │       │
│       │       └── services/                      # Core Scheduling Logic
│       │           ├── ScheduleGenerator.java
│       │           ├── ConflictResolver.java
│       │           └── ExamScheduler.java
│       │
│       └── resources/
│           ├── fxml/                              # FXML Layout Files
│           │   ├── home.fxml
│           │   ├── daily-timetable.fxml
│           │   └── exam-timetable.fxml
│           │
│           ├── styles/                            # CSS Stylesheets
│           │   └── main.css
│           │
│           └── assets/                            # Icons and Images
│               └── logo.png
│
├── pom.xml                                        # Maven Configuration
└── README.md
```

---

## ⚙️ Installation & Setup

### ✅ Prerequisites

| Requirement | Minimum Version | Download Link |
|---|---|---|
| Java JDK | 17+ | [Adoptium OpenJDK](https://adoptium.net/) |
| JavaFX SDK | 17+ | [GluonHQ JavaFX](https://gluonhq.com/products/javafx/) |
| Maven *(optional)* | 3.8+ | [maven.apache.org](https://maven.apache.org/) |

---

> ⚠️ **Important — JavaFX is NOT bundled in this repository.**
>
> JavaFX must be downloaded separately from [gluonhq.com](https://gluonhq.com/products/javafx/). It is platform-specific and too large to include in the repo. Follow Step 2 carefully.

---

### 📥 Step 1 — Clone the Repository

```bash
git clone https://github.com/your-username/Schedulix.git
cd Schedulix
```

### 📦 Step 2 — Download JavaFX SDK

1. Visit 👉 [https://gluonhq.com/products/javafx/](https://gluonhq.com/products/javafx/)
2. Select your **OS** and **Java version** (e.g., JavaFX 17, Windows x64)
3. Download and extract the SDK to a memorable path:

```
Windows  →  C:\javafx-sdk-17\
macOS    →  /Users/yourname/javafx-sdk-17/
Linux    →  /usr/local/javafx-sdk-17/
```

### 🔧 Step 3 — Configure JavaFX in Your IDE

<details>
<summary><b>▶ IntelliJ IDEA (Recommended)</b></summary>
<br/>

1. Go to `File` → `Project Structure` → `Libraries`
2. Click `+` → `Java` → Navigate to your JavaFX SDK's `lib/` folder
3. Select the folder and click OK → Apply
4. Go to `Run` → `Edit Configurations` → add **VM options**:

```
--module-path /path/to/javafx-sdk-17/lib --add-modules javafx.controls,javafx.fxml
```

</details>

<details>
<summary><b>▶ Eclipse IDE</b></summary>
<br/>

1. Right-click project → `Build Path` → `Configure Build Path`
2. Under `Libraries` → `Add External JARs`
3. Select all `.jar` files inside your JavaFX SDK `lib/` folder
4. In `Run Configurations`, under `Arguments` → `VM Arguments`, add:

```
--module-path /path/to/javafx-sdk-17/lib --add-modules javafx.controls,javafx.fxml
```

</details>

### 🏗️ Step 4 — Build the Project

**Maven:**
```bash
mvn clean install
```

**Gradle:**
```bash
gradle build
```

---

## ▶️ How to Run

### Option A — From Your IDE

Add VM arguments in run configuration:
```
--module-path /path/to/javafx-sdk-17/lib --add-modules javafx.controls,javafx.fxml
```
Run `Main.java` as the entry point.

### Option B — Maven (Terminal)

```bash
mvn javafx:run
```

### Option C — Gradle (Terminal)

```bash
gradle run
```

> 💡 Always replace `/path/to/javafx-sdk-17/lib` with the actual absolute path to your JavaFX `lib` folder.

---

## 💡 How It Works

```
┌──────────────────────────────────────┐
│         User Input (UI Form)         │
│  Subjects · Slots · Teachers ·       │
│  Constraints · Exam Dates            │
└─────────────────┬────────────────────┘
                  │
                  ▼
┌──────────────────────────────────────┐
│         Scheduling Engine            │
│  ✔ Constraint Validation             │
│  ✔ Conflict Detection                │
│  ✔ Slot Assignment Algorithm         │
│  ✔ Backtracking on Conflicts         │
└─────────────────┬────────────────────┘
                  │
                  ▼
┌──────────────────────────────────────┐
│       Optimized Timetable Output     │
│   Grid View · Export · Print Ready  │
└──────────────────────────────────────┘
```

The scheduling engine uses a **constraint-satisfaction approach** — it assigns subjects to time slots while validating all rules at each step. If a conflict is found, it backtracks and tries the next valid slot until a complete, clean schedule is produced.

---

## 🔮 Future Improvements

Planned features for upcoming releases:

- [ ] 📄 Export timetable to **PDF** and **Excel**
- [ ] 🏫 Multi-room and multi-venue support
- [ ] 🖱️ Drag-and-drop manual adjustments post-generation
- [ ] 💾 Save and reload timetable profiles locally
- [ ] 🌙 Dark mode / theme switcher
- [ ] 📊 Import subjects and constraints from **CSV / Excel**
- [ ] 🖨️ Print-ready timetable formatting
- [ ] 🔔 Conflict warnings with smart suggestions
- [ ] 🌍 Multi-language UI support

---

## 🤝 Contributing

Contributions are what make open-source worth building. Whether it's a bug fix, a UI improvement, a new feature, or better documentation — all help is genuinely valued here.

### Steps to Contribute

```bash
# 1. Fork this repository on GitHub

# 2. Clone your fork
git clone https://github.com/your-username/Schedulix.git

# 3. Create a feature branch
git checkout -b feature/your-feature-name

# 4. Make your changes and commit with a clear message
git commit -m "Add: description of your change"

# 5. Push to your fork
git push origin feature/your-feature-name

# 6. Open a Pull Request on the main repo
```



### 💬 Want to Contribute but Not Sure Where to Start?

**Just DM me — I'll personally help you get onboarded!**

I actively respond to everyone, whether you're a beginner making your first open-source contribution or an experienced developer with a feature idea. Don't hesitate.

📧 **Email:** nishanksanghvi4@gmail.com

> Open source is better when people work together. Let's build something great. 🙌

---

## 📄 License

This project is licensed under the **MIT License** — free to use, modify, and distribute with proper attribution.

See the [`LICENSE`](LICENSE) file for full terms.

---



> Have a bug to report, an idea to share, or just want to say hi?
> **DM me on Instagram or LinkedIn — I respond to every message.**

---

<div align="center">

### 🔍 SEO Keywords

`timetable generator` · `smart scheduler` · `Java desktop app` · `JavaFX timetable` · `exam schedule generator` · `class timetable software` · `academic scheduler` · `school timetable app` · `college timetable generator` · `open source timetable` · `constraint-based scheduling` · `Java scheduling algorithm` · `JavaFX project` · `student timetable tool` · `automated timetable` · `conflict-free schedule generator`

---

**If Schedulix saved you time, please ⭐ star the repo — it genuinely helps!**

<br/>

*Made with ☕, Java, and way too many debugging sessions*

</div>
