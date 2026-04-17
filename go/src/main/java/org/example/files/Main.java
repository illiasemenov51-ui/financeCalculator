package org.example.files;

public class Main {
    public static void main(String[] args) {
        Logger.info("=== Poland Tax Calculator started ===");

        DatabaseManager db = DatabaseManager.getInstance();
        db.initialize();

        AuthService auth = new AuthService(db);
        MenuController menu = new MenuController(auth, db);

        menu.start();

        db.close();
        Logger.info("=== Application closed ===");
    }
}
