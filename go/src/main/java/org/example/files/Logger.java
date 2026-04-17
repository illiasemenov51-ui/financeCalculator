package org.example.files;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final String LOG_FILE = "tax_calculator.log";
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void info(String message) {
        log("INFO ", message);
    }

    public static void warn(String message) {
        log("WARN ", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }

    public static void error(String message, Exception e) {
        log("ERROR", message + " | " + e.getMessage());
    }

    public static void audit(String user, String action) {
        log("AUDIT", "[" + user + "] " + action);
    }

    private static void log(String level, String message) {
        String timestamp = LocalDateTime.now().format(FMT);
        String line = "[" + timestamp + "] [" + level + "] " + message;

        System.err.println(line);

        try (PrintWriter pw = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            pw.println(line);
        } catch (IOException e) {
            System.err.println("Cannot write to log file: " + e.getMessage());
        }
    }
}
