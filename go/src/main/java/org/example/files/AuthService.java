package org.example.files;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Scanner;

public class AuthService {

    private final DatabaseManager db;
    private User currentUser;

    public AuthService(DatabaseManager db) {
        this.db = db;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        Logger.audit(currentUser != null ? currentUser.getLogin() : "?", "Logout");
        currentUser = null;
    }

    public boolean promptLogin(Scanner scanner) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║     POLAND TAX CALCULATOR v1.0      ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║  1. Zaloguj się                      ║");
        System.out.println("║  2. Zarejestruj się                  ║");
        System.out.println("║  0. Wyjście                          ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.print("Wybór: ");

        String choice = scanner.nextLine().trim();

        return switch (choice) {
            case "1" -> doLogin(scanner);
            case "2" -> doRegister(scanner);
            case "0" -> {
                Logger.info("Application exit from auth screen");
                System.exit(0);
                yield false;
            }
            default -> {
                System.out.println("Nieprawidłowy wybór.");
                yield false;
            }
        };
    }

    private boolean doLogin(Scanner scanner) {
        System.out.print("Login: ");
        String login = scanner.nextLine().trim();

        System.out.print("Hasło: ");
        String password = scanner.nextLine().trim();

        User user = db.findUserByLogin(login);
        if (user == null) {
            Logger.warn("Login attempt for non-existent user: " + login);
            System.out.println("Błąd: użytkownik nie istnieje.");
            return false;
        }

        if (!hashPassword(password).equals(user.getPasswordHash())) {
            Logger.warn("Wrong password for user: " + login);
            System.out.println("Błąd: nieprawidłowe hasło.");
            return false;
        }

        currentUser = user;
        Logger.audit(login, "Login successful");
        System.out.println("\nWitaj, " + user.getFullName() + "!");
        return true;
    }

    private boolean doRegister(Scanner scanner) {
        System.out.print("Login (min. 3 znaki): ");
        String login = scanner.nextLine().trim();
        if (login.length() < 3) {
            System.out.println("Login za krótki.");
            return false;
        }

        System.out.print("Hasło (min. 6 znaków): ");
        String password = scanner.nextLine().trim();
        if (password.length() < 6) {
            System.out.println("Hasło za krótkie.");
            return false;
        }

        System.out.print("Imię i nazwisko: ");
        String fullName = scanner.nextLine().trim();

        System.out.println("Forma opodatkowania:");
        System.out.println("  1. Skala podatkowa (12%/32%)");
        System.out.println("  2. Podatek liniowy (19%)");
        System.out.println("  3. Ryczałt");
        System.out.print("Wybór [1]: ");
        String taxChoice = scanner.nextLine().trim();
        String taxType = switch (taxChoice) {
            case "2" -> "liniowy";
            case "3" -> "ryczalt";
            default  -> "skala";
        };

        User user = new User();
        user.setLogin(login);
        user.setPasswordHash(hashPassword(password));
        user.setFullName(fullName.isEmpty() ? login : fullName);
        user.setTaxType(taxType);

        if (db.saveUser(user)) {
            currentUser = db.findUserByLogin(login);
            Logger.audit(login, "Registration successful, taxType=" + taxType);
            System.out.println("Konto utworzone! Witaj, " + user.getFullName() + "!");
            return true;
        } else {
            System.out.println("Błąd: login już zajęty.");
            return false;
        }
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            Logger.error("Password hashing failed", e);
            throw new RuntimeException(e);
        }
    }
}
