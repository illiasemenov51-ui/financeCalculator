package org.example.files;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:tax_calculator.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            Logger.info("Database connected: " + DB_URL);
            createTables();
        } catch (Exception e) {
            Logger.error("Database init failed", e);
            throw new RuntimeException("Cannot connect to database", e);
        }
    }

    private void createTables() throws SQLException {
        String usersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id            INTEGER PRIMARY KEY AUTOINCREMENT,
                login         TEXT UNIQUE NOT NULL,
                password_hash TEXT NOT NULL,
                full_name     TEXT,
                tax_type      TEXT DEFAULT 'skala',
                created_at    TEXT DEFAULT (datetime('now'))
            )
            """;

        String incomeTable = """
            CREATE TABLE IF NOT EXISTS income (
                id          INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id     INTEGER NOT NULL,
                amount      REAL NOT NULL,
                description TEXT,
                category    TEXT DEFAULT 'inne',
                date        TEXT NOT NULL,
                created_at  TEXT DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """;

        String taxRecordsTable = """
            CREATE TABLE IF NOT EXISTS tax_records (
                id              INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id         INTEGER NOT NULL,
                year            INTEGER NOT NULL,
                gross_income    REAL,
                zus             REAL,
                health_ins      REAL,
                taxable_base    REAL,
                income_tax      REAL,
                net_income      REAL,
                calculated_at   TEXT DEFAULT (datetime('now')),
                FOREIGN KEY (user_id) REFERENCES users(id)
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(usersTable);
            stmt.execute(incomeTable);
            stmt.execute(taxRecordsTable);
            Logger.info("Tables initialized");
        }
    }

    // ── USER OPERATIONS ──────────────────────────────────────────────

    public boolean saveUser(User user) {
        String sql = "INSERT INTO users (login, password_hash, full_name, tax_type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getLogin());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getTaxType());
            ps.executeUpdate();
            Logger.info("New user registered: " + user.getLogin());
            return true;
        } catch (SQLException e) {
            Logger.error("saveUser failed for: " + user.getLogin(), e);
            return false;
        }
    }

    public User findUserByLogin(String login) {
        String sql = "SELECT * FROM users WHERE login = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, login);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setLogin(rs.getString("login"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setFullName(rs.getString("full_name"));
                user.setTaxType(rs.getString("tax_type"));
                return user;
            }
        } catch (SQLException e) {
            Logger.error("findUserByLogin failed", e);
        }
        return null;
    }

    public boolean updateTaxType(int userId, String taxType) {
        String sql = "UPDATE users SET tax_type = ? WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, taxType);
            ps.setInt(2, userId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            Logger.error("updateTaxType failed", e);
            return false;
        }
    }

    // ── INCOME OPERATIONS ─────────────────────────────────────────────

    public boolean saveIncome(Income income) {
        String sql = "INSERT INTO income (user_id, amount, description, category, date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, income.getUserId());
            ps.setDouble(2, income.getAmount());
            ps.setString(3, income.getDescription());
            ps.setString(4, income.getCategory());
            ps.setString(5, income.getDate().toString());
            ps.executeUpdate();
            Logger.audit("user#" + income.getUserId(),
                    "Income added: " + income.getAmount() + " PLN");
            return true;
        } catch (SQLException e) {
            Logger.error("saveIncome failed", e);
            return false;
        }
    }

    public List<Income> getIncomeByUser(int userId) {
        List<Income> list = new ArrayList<>();
        String sql = "SELECT * FROM income WHERE user_id = ? ORDER BY date DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapIncome(rs));
            }
        } catch (SQLException e) {
            Logger.error("getIncomeByUser failed", e);
        }
        return list;
    }

    public List<Income> getIncomeByUserAndYear(int userId, int year) {
        List<Income> list = new ArrayList<>();
        String sql = "SELECT * FROM income WHERE user_id = ? AND strftime('%Y', date) = ? ORDER BY date";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, String.valueOf(year));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapIncome(rs));
            }
        } catch (SQLException e) {
            Logger.error("getIncomeByUserAndYear failed", e);
        }
        return list;
    }

    public boolean deleteIncome(int incomeId, int userId) {
        String sql = "DELETE FROM income WHERE id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, incomeId);
            ps.setInt(2, userId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                Logger.audit("user#" + userId, "Income deleted: id=" + incomeId);
                return true;
            }
        } catch (SQLException e) {
            Logger.error("deleteIncome failed", e);
        }
        return false;
    }

    private Income mapIncome(ResultSet rs) throws SQLException {
        Income inc = new Income();
        inc.setId(rs.getInt("id"));
        inc.setUserId(rs.getInt("user_id"));
        inc.setAmount(rs.getDouble("amount"));
        inc.setDescription(rs.getString("description"));
        inc.setCategory(rs.getString("category"));
        inc.setDate(LocalDate.parse(rs.getString("date")));
        return inc;
    }

    // ── TAX RECORDS ───────────────────────────────────────────────────

    public void saveTaxRecord(int userId, int year, double gross, double zus,
                               double health, double base, double tax, double net) {
        String sql = """
            INSERT INTO tax_records (user_id, year, gross_income, zus, health_ins,
                                     taxable_base, income_tax, net_income)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, year);
            ps.setDouble(3, gross);
            ps.setDouble(4, zus);
            ps.setDouble(5, health);
            ps.setDouble(6, base);
            ps.setDouble(7, tax);
            ps.setDouble(8, net);
            ps.executeUpdate();
            Logger.audit("user#" + userId, "Tax calculation saved for year " + year);
        } catch (SQLException e) {
            Logger.error("saveTaxRecord failed", e);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            Logger.error("Error closing DB", e);
        }
    }
}
