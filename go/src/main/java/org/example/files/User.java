package org.example.files;

public class User {
    private int id;
    private String login;
    private String passwordHash;
    private String fullName;
    private String taxType; // "liniowy" (19%), "skala" (12/32%), "ryczalt"

    public User() {}

    public User(int id, String login, String fullName, String taxType) {
        this.id = id;
        this.login = login;
        this.fullName = fullName;
        this.taxType = taxType;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getTaxType() { return taxType; }
    public void setTaxType(String taxType) { this.taxType = taxType; }

    @Override
    public String toString() {
        return String.format("User{id=%d, login='%s', fullName='%s', taxType='%s'}",
                id, login, fullName, taxType);
    }
}
