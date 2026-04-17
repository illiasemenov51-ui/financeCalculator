package org.example.files;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class IncomeService {

    private final DatabaseManager db;

    public IncomeService(DatabaseManager db) {
        this.db = db;
    }

    public void addIncome(User user, Scanner scanner) {
        System.out.println("\n── Dodaj dochód ─────────────────────────");

        System.out.print("Kwota (PLN): ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("Nieprawidłowa kwota.");
            return;
        }

        System.out.println("Kategoria:");
        System.out.println("  1. Umowa o pracę");
        System.out.println("  2. B2B / Działalność gospodarcza");
        System.out.println("  3. Umowa zlecenie");
        System.out.println("  4. Inne");
        System.out.print("Wybór [1]: ");
        String catChoice = scanner.nextLine().trim();
        String category = switch (catChoice) {
            case "2" -> "b2b";
            case "3" -> "zlecenie";
            case "4" -> "inne";
            default  -> "umowa_o_prace";
        };

        System.out.print("Opis (opcjonalnie): ");
        String desc = scanner.nextLine().trim();

        System.out.print("Data (RRRR-MM-DD) [Enter = dzisiaj]: ");
        String dateStr = scanner.nextLine().trim();
        LocalDate date;
        try {
            date = dateStr.isEmpty() ? LocalDate.now() : LocalDate.parse(dateStr);
        } catch (Exception e) {
            System.out.println("Nieprawidłowy format daty. Używam dzisiejszej daty.");
            date = LocalDate.now();
        }

        Income income = new Income(user.getId(), amount, desc, category, date);
        if (db.saveIncome(income)) {
            System.out.printf("✓ Dodano: %.2f PLN (%s)%n", amount, category);
            Logger.audit(user.getLogin(), "Income added: " + amount + " PLN, category=" + category);
        } else {
            System.out.println("Błąd zapisu dochodu.");
        }
    }

    public void listAllIncome(User user) {
        List<Income> incomes = db.getIncomeByUser(user.getId());
        if (incomes.isEmpty()) {
            System.out.println("\n  Brak zapisanych dochodów.");
            return;
        }

        System.out.println("\n── Historia dochodów ─────────────────────────────────");
        System.out.printf("  %-4s %-12s %-14s %-18s %s%n",
                "ID", "Data", "Kwota", "Kategoria", "Opis");
        System.out.println("  " + "─".repeat(72));

        double total = 0;
        for (Income inc : incomes) {
            System.out.printf("  %-4d %-12s %10.2f PLN  %-18s %s%n",
                    inc.getId(), inc.getDate(), inc.getAmount(),
                    inc.getCategory(),
                    inc.getDescription() != null ? inc.getDescription() : "");
            total += inc.getAmount();
        }
        System.out.println("  " + "─".repeat(72));
        System.out.printf("  RAZEM: %.2f PLN (%d wpisów)%n", total, incomes.size());
    }

    public void listIncomeByYear(User user, Scanner scanner) {
        System.out.print("Rok: ");
        int year;
        try {
            year = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Nieprawidłowy rok.");
            return;
        }

        List<Income> incomes = db.getIncomeByUserAndYear(user.getId(), year);
        if (incomes.isEmpty()) {
            System.out.println("Brak dochodów w roku " + year);
            return;
        }

        System.out.println("\n── Dochody za rok " + year + " ───────────────────────────");
        double total = 0;
        for (Income inc : incomes) {
            System.out.printf("  %s | %10.2f PLN | %-18s | %s%n",
                    inc.getDate(), inc.getAmount(), inc.getCategory(),
                    inc.getDescription() != null ? inc.getDescription() : "");
            total += inc.getAmount();
        }
        System.out.printf("%n  Łącznie za %d: %.2f PLN%n", year, total);
    }

    public void deleteIncome(User user, Scanner scanner) {
        listAllIncome(user);
        System.out.print("\nID wpisu do usunięcia (0 = anuluj): ");
        int id;
        try {
            id = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Nieprawidłowe ID.");
            return;
        }
        if (id == 0) return;
        if (db.deleteIncome(id, user.getId())) {
            System.out.println("✓ Usunięto wpis #" + id);
        } else {
            System.out.println("Błąd: wpis nie istnieje lub brak uprawnień.");
        }
    }
}
