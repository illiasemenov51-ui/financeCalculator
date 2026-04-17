# 🇵🇱 Poland Tax Calculator

<div align="center">

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![SQLite](https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green?style=for-the-badge)

**Terminal-based Polish income tax calculator with multi-user support, persistent storage and full audit logging.**

*Supports Skala podatkowa · Podatek liniowy · Ryczałt — rates for 2024*

</div>

---

## ✨ Features

| Feature | Details |
|---|---|
| 🔐 **Secure auth** | SHA-256 password hashing, per-user sessions |
| 💰 **Income tracking** | Add, view, filter and delete income by category and year |
| 🧮 **Tax calculation** | PIT + ZUS + health insurance — all three Polish tax forms |
| 🗃️ **Persistent storage** | SQLite database — data survives restarts |
| 📋 **Audit logging** | Every action logged to terminal + `tax_calculator.log` |
| 👥 **Multi-user** | Unlimited accounts, each with their own data |
| 🔄 **Switch navigation** | Fluid terminal menu — move between sections instantly |
| 📊 **Annual reports** | Tax summary per year, saved to history automatically |

---

## 🧮 Tax Rates 2024

```
┌─────────────────────┬────────────────────────────────────┐
│ Skala podatkowa     │ 12% up to 120 000 PLN              │
│                     │ 32% above 120 000 PLN              │
│                     │ Tax-free allowance: 30 000 PLN     │
├─────────────────────┼────────────────────────────────────┤
│ Podatek liniowy     │ Flat 19% on income                 │
├─────────────────────┼────────────────────────────────────┤
│ Ryczałt             │ 8.5% on revenue (IT / services)    │
├─────────────────────┼────────────────────────────────────┤
│ ZUS 2024            │ Base: 4 694.40 PLN/month           │
│                     │ Emerytalne 19.52% + Rentowe 8%     │
│                     │ Chorobowe 2.45% + Wypadkowe 1.67%  │
├─────────────────────┼────────────────────────────────────┤
│ Health insurance    │ 9% of income (skala / ryczałt)     │
│                     │ 4.9% of income (liniowy)           │
└─────────────────────┴────────────────────────────────────┘
```

---

## 🚀 Quick Start

### Option 1 — Run with Maven

```bash
git clone https://github.com/your-username/poland-tax-calculator.git
cd poland-tax-calculator

mvn clean package
java -jar target/poland-tax-calculator.jar
```

### Option 2 — Run without Maven (manual JARs)

```bash
# Download dependencies
curl -L -o sqlite-jdbc.jar \
  https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar

curl -L -o slf4j-api.jar \
  https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar

curl -L -o slf4j-nop.jar \
  https://repo1.maven.org/maven2/org/slf4j/slf4j-nop/1.7.36/slf4j-nop-1.7.36.jar

# Compile
javac -cp sqlite-jdbc.jar:slf4j-api.jar \
  src/main/java/org/example/files/*.java \
  -d target/classes

# Run
java -cp target/classes:sqlite-jdbc.jar:slf4j-api.jar:slf4j-nop.jar \
  org.example.files.Main
```

---

## 🐳 Docker

### Run with Docker (no Java or Maven needed)

```bash
# Build the image
docker build -t poland-tax-calculator .

# Run interactively (required for terminal input)
docker run -it --rm \
  -v $(pwd)/data:/app/data \
  poland-tax-calculator
```

The `-v` flag mounts a local `data/` folder so your SQLite database **persists between container restarts**.

### Docker Compose

```bash
docker compose up
```

To reset all data:
```bash
docker compose down -v
```

---

## 📁 Project Structure

```
poland-tax-calculator/
├── src/main/java/org/example/files/
│   ├── Main.java                  # Entry point
│   ├── AuthService.java           # Login · Register · SHA-256
│   ├── MenuController.java        # Switch-based terminal navigation
│   ├── DatabaseManager.java       # SQLite via JDBC · Singleton
│   ├── IncomeService.java         # Add · View · Delete income
│   ├── TaxCalculatorService.java  # PIT + ZUS logic 2024
│   ├── TaxResult.java             # Result model + pretty print
│   ├── Income.java                # Income model
│   ├── User.java                  # User model
│   └── Logger.java                # File + terminal logging
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 🗄️ Database Schema

```sql
-- User accounts
CREATE TABLE users (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    login         TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,        -- SHA-256
    full_name     TEXT,
    tax_type      TEXT DEFAULT 'skala', -- skala | liniowy | ryczalt
    created_at    TEXT DEFAULT (datetime('now'))
);

-- Income records
CREATE TABLE income (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL REFERENCES users(id),
    amount      REAL NOT NULL,
    description TEXT,
    category    TEXT,   -- umowa_o_prace | b2b | zlecenie | inne
    date        TEXT NOT NULL
);

-- Tax calculation history
CREATE TABLE tax_records (
    id           INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id      INTEGER NOT NULL REFERENCES users(id),
    year         INTEGER NOT NULL,
    gross_income REAL,
    zus          REAL,
    health_ins   REAL,
    taxable_base REAL,
    income_tax   REAL,
    net_income   REAL,
    calculated_at TEXT DEFAULT (datetime('now'))
);
```

---

## 📋 Menu Overview

```
╔══════════════════════════════════════════╗
║  Zalogowany: Jan Kowalski                ║
║  Forma:      skala                       ║
╠══════════════════════════════════════════╣
║  DOCHODY                                 ║
║    1. Dodaj dochód                       ║
║    2. Pokaż wszystkie dochody            ║
║    3. Dochody za rok                     ║
║    4. Usuń wpis                          ║
╠══════════════════════════════════════════╣
║  PODATKI                                 ║
║    5. Oblicz podatek za rok              ║
║    6. Szybki kalkulator (własna kwota)   ║
║    7. Informacje o stawkach              ║
╠══════════════════════════════════════════╣
║  KONTO                                   ║
║    8. Zmień formę opodatkowania          ║
║    9. Wyloguj się                        ║
║    0. Wyjście                            ║
╚══════════════════════════════════════════╝
```

---

## 📦 Adding New Features

The project is structured for easy extension:

- **New tax type** → `TaxCalculatorService.java` — add a new `case` in `calculate()`
- **New menu item** → `MenuController.java` — add to `handleChoice()` switch
- **New DB table** → `DatabaseManager.java` — add to `createTables()`
- **New service** → create in `org.example.files`, inject `DatabaseManager` via constructor

---

## 📄 License

MIT © 2024 — feel free to use, modify and distribute.
