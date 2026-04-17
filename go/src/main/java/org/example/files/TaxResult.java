package org.example.files;

public class TaxResult {
    private double grossIncome;
    private double zusContribution;
    private double healthInsurance;
    private double taxableBase;
    private double incomeTax;
    private double netIncome;
    private String taxType;
    private int year;

    public TaxResult(double grossIncome, double zusContribution, double healthInsurance,
                     double taxableBase, double incomeTax, double netIncome,
                     String taxType, int year) {
        this.grossIncome = grossIncome;
        this.zusContribution = zusContribution;
        this.healthInsurance = healthInsurance;
        this.taxableBase = taxableBase;
        this.incomeTax = incomeTax;
        this.netIncome = netIncome;
        this.taxType = taxType;
        this.year = year;
    }

    public double getGrossIncome()      { return grossIncome; }
    public double getZusContribution()  { return zusContribution; }
    public double getHealthInsurance()  { return healthInsurance; }
    public double getTaxableBase()      { return taxableBase; }
    public double getIncomeTax()        { return incomeTax; }
    public double getNetIncome()        { return netIncome; }
    public String getTaxType()          { return taxType; }
    public int    getYear()             { return year; }

    public void printSummary() {
        System.out.println("\n╔══════════════════════════════════════════╗");
        System.out.printf ("║  WYNIKI PODATKOWE %d %-20s ║%n", year, "");
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.printf ("║  Forma opodatkowania: %-19s ║%n", taxType);
        System.out.printf ("║  Przychód brutto:     %12.2f PLN   ║%n", grossIncome);
        System.out.printf ("║  ZUS (składki):       %12.2f PLN   ║%n", zusContribution);
        System.out.printf ("║  Ubezp. zdrowotne:    %12.2f PLN   ║%n", healthInsurance);
        System.out.printf ("║  Podstawa opodatkow.: %12.2f PLN   ║%n", taxableBase);
        System.out.printf ("║  Podatek dochodowy:   %12.2f PLN   ║%n", incomeTax);
        System.out.println("╠══════════════════════════════════════════╣");
        System.out.printf ("║  Dochód NETTO:        %12.2f PLN   ║%n", netIncome);
        System.out.println("╚══════════════════════════════════════════╝");
    }
}
