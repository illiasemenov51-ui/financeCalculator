package org.example.files;

/**
 * Polish tax calculator 2024.
 *
 * Skala:   12% up to 120,000 PLN / 32% above. Tax-free: 30,000 PLN.
 * Liniowy: flat 19%.
 * Ryczalt: 8.5% on revenue.
 *
 * ZUS 2024 base: 4694.40 PLN/month (60% avg salary).
 */
public class TaxCalculatorService {

    private static final double ZUS_BASE_MONTHLY  = 4694.40;
    private static final double ZUS_EMERYTALNE    = 0.1952;
    private static final double ZUS_RENTOWE       = 0.0800;
    private static final double ZUS_CHOROBOWE     = 0.0245;
    private static final double ZUS_WYPADKOWE     = 0.0167;

    private static final double TAX_FREE_ALLOWANCE = 30_000.0;
    private static final double BRACKET_LIMIT      = 120_000.0;
    private static final double SKALA_LOWER        = 0.12;
    private static final double SKALA_UPPER        = 0.32;
    private static final double LINIOWY_RATE       = 0.19;
    private static final double RYCZALT_RATE       = 0.085;

    public TaxResult calculate(double grossIncome, String taxType, int year, int months) {
        Logger.info(String.format("Calculating tax: gross=%.2f, type=%s, year=%d, months=%d",
                grossIncome, taxType, year, months));

        double zusTotal = calcZusMonthly() * months;
        double taxableBase;
        double healthInsurance;
        double incomeTax;
        double netIncome;

        switch (taxType.toLowerCase()) {
            case "liniowy" -> {
                taxableBase     = Math.max(0, grossIncome - zusTotal);
                healthInsurance = calcHealthInsurance(taxableBase, taxType);
                incomeTax       = taxableBase * LINIOWY_RATE;
                netIncome       = grossIncome - zusTotal - healthInsurance - incomeTax;
            }
            case "ryczalt" -> {
                taxableBase     = grossIncome;
                healthInsurance = calcHealthInsurance(grossIncome, taxType);
                incomeTax       = grossIncome * RYCZALT_RATE;
                netIncome       = grossIncome - zusTotal - healthInsurance - incomeTax;
            }
            default -> { // skala
                taxableBase     = Math.max(0, grossIncome - zusTotal);
                healthInsurance = calcHealthInsurance(taxableBase, taxType);
                incomeTax       = calcSkalaTax(taxableBase);
                netIncome       = grossIncome - zusTotal - healthInsurance - incomeTax;
            }
        }

        return new TaxResult(grossIncome, zusTotal, healthInsurance,
                taxableBase, incomeTax, netIncome, taxType, year);
    }

    private double calcZusMonthly() {
        return ZUS_BASE_MONTHLY * (ZUS_EMERYTALNE + ZUS_RENTOWE + ZUS_CHOROBOWE + ZUS_WYPADKOWE);
    }

    private double calcHealthInsurance(double income, String taxType) {
        double rate = taxType.equalsIgnoreCase("liniowy") ? 0.049 : 0.09;
        return income * rate;
    }

    private double calcSkalaTax(double base) {
        if (base <= TAX_FREE_ALLOWANCE) return 0;
        if (base <= BRACKET_LIMIT)
            return (base - TAX_FREE_ALLOWANCE) * SKALA_LOWER;
        double lower = (BRACKET_LIMIT - TAX_FREE_ALLOWANCE) * SKALA_LOWER;
        double upper = (base - BRACKET_LIMIT) * SKALA_UPPER;
        return lower + upper;
    }

    public void printTaxRatesInfo() {
        System.out.println("""
                
                ══ STAWKI PODATKOWE 2024 ══════════════════════════════
                 Skala podatkowa:
                   12% od nadwyżki ponad kwotę wolną (30 000 PLN)
                   32% od dochodu powyżej 120 000 PLN
                
                 Podatek liniowy:
                   19% od całego dochodu (bez kwoty wolnej)
                
                 Ryczałt ewidencjonowany:
                   8,5% od przychodu (stawka dla usług IT/wolnych zawodów)
                
                 ZUS 2024 (miesięcznie, podstawa 4694,40 PLN):
                   Emerytalne: 19,52%  |  Rentowe: 8%
                   Chorobowe:   2,45%  |  Wypadkowe: 1,67%
                
                 Ubezpieczenie zdrowotne:
                   9% dochodu (skala/ryczałt) | 4,9% dochodu (liniowy)
                ═══════════════════════════════════════════════════════
                """);
    }
}
