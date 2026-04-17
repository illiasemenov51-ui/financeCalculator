package org.example.files;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class MenuController {

    private final AuthService auth;
    private final DatabaseManager db;
    private final IncomeService incomeService;
    private final TaxCalculatorService taxService;
    private final Scanner scanner;

    public MenuController(AuthService auth, DatabaseManager db) {
        this.auth          = auth;
        this.db            = db;
        this.incomeService = new IncomeService(db);
        this.taxService    = new TaxCalculatorService();
        this.scanner       = new Scanner(System.in);
    }

    public void start() {
        // Auth loop — keep asking until logged in
        while (!auth.isLoggedIn()) {
            auth.promptLogin(scanner);
        }

        // Main menu loop
        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            Logger.info("Menu choice: '" + choice + "' by " + auth.getCurrentUser().getLogin());
            running = handleChoice(choice);
        }
    }

    private void printMainMenu() {
        String user    = auth.getCurrentUser().getFullName();
        String taxType = auth.getCurrentUser().getTaxType();

        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.printf ("║  Zalogowany: %-28s║%n", user);
        System.out.printf ("║  Forma:      %-28s║%n", taxType);
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║  DOCHODY                                 ║");
        System.out.println("║    1. Dodaj dochód                       ║");
        System.out.println("║    2. Pokaż wszystkie dochody            ║");
        System.out.println("║    3. Dochody za rok                     ║");
        System.out.println("║    4. Usuń wpis                          ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║  PODATKI                                 ║");
        System.out.println("║    5. Oblicz podatek za rok              ║");
        System.out.println("║    6. Szybki kalkulator (własna kwota)   ║");
        System.out.println("║    7. Informacje o stawkach              ║");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.println("║  KONTO                                   ║");
        System.out.println("║    8. Zmień formę opodatkowania          ║");
        System.out.println("║    9. Wyloguj się                        ║");
        System.out.println("║    0. Wyjście                            ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.print("Wybór: ");
    }

    private boolean handleChoice(String choice) {
        switch (choice) {
            case "1" -> incomeService.addIncome(auth.getCurrentUser(), scanner);
            case "2" -> incomeService.listAllIncome(auth.getCurrentUser());
            case "3" -> incomeService.listIncomeByYear(auth.getCurrentUser(), scanner);
            case "4" -> incomeService.deleteIncome(auth.getCurrentUser(), scanner);
            case "5" -> calculateTaxForYear();
            case "6" -> quickCalculator();
            case "7" -> taxService.printTaxRatesInfo();
            case "8" -> changeTaxType();
            case "9" -> {
                auth.logout();
                System.out.println("Do widzenia!");
                while (!auth.isLoggedIn()) {
                    auth.promptLogin(scanner);
                }
            }
            case "0" -> {
                Logger.audit(auth.getCurrentUser().getLogin(), "Application exit");
                System.out.println("Do widzenia!");
                return false;
            }
            default -> System.out.println("Nieznana opcja. Spróbuj ponownie.");
        }
        return true;
    }

    private void calculateTaxForYear() {
        System.out.print("Rok podatkowy: ");
        int year;
        try {
            year = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Nieprawidłowy rok.");
            return;
        }

        List<Income> incomes = db.getIncomeByUserAndYear(auth.getCurrentUser().getId(), year);
        if (incomes.isEmpty()) {
            System.out.println("Brak dochodów w roku " + year + ". Dodaj najpierw dochody.");
            return;
        }

        double totalGross = incomes.stream().mapToDouble(Income::getAmount).sum();
        long months = incomes.stream().map(i -> i.getDate().getMonth()).distinct().count();
        int activeMonths = (int) Math.max(1, months);

        String taxType = auth.getCurrentUser().getTaxType();
        TaxResult result = taxService.calculate(totalGross, taxType, year, activeMonths);
        result.printSummary();

        db.saveTaxRecord(auth.getCurrentUser().getId(), year,
                result.getGrossIncome(), result.getZusContribution(),
                result.getHealthInsurance(), result.getTaxableBase(),
                result.getIncomeTax(), result.getNetIncome());

        System.out.println("✓ Wyniki zapisane w historii.");
    }

    private void quickCalculator() {
        System.out.println("\n── Szybki kalkulator ────────────────────────");
        System.out.print("Kwota brutto (PLN): ");
        double gross;
        try {
            gross = Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            System.out.println("Nieprawidłowa kwota.");
            return;
        }

        System.out.print("Liczba miesięcy [12]: ");
        String mStr = scanner.nextLine().trim();
        int months = mStr.isEmpty() ? 12 : Integer.parseInt(mStr);

        String taxType = auth.getCurrentUser().getTaxType();
        int year = LocalDate.now().getYear();

        TaxResult result = taxService.calculate(gross, taxType, year, months);
        result.printSummary();
    }

    private void changeTaxType() {
        System.out.println("\nForma opodatkowania:");
        System.out.println("  1. Skala podatkowa (12%/32%)");
        System.out.println("  2. Podatek liniowy (19%)");
        System.out.println("  3. Ryczałt");
        System.out.print("Nowa forma: ");
        String c = scanner.nextLine().trim();
        String newType = switch (c) {
            case "2" -> "liniowy";
            case "3" -> "ryczalt";
            default  -> "skala";
        };
        if (db.updateTaxType(auth.getCurrentUser().getId(), newType)) {
            auth.getCurrentUser().setTaxType(newType);
            System.out.println("✓ Zmieniono formę na: " + newType);
            Logger.audit(auth.getCurrentUser().getLogin(), "Tax type changed to: " + newType);
        }
    }
}
