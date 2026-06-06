package service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ChangeLoggerTest {

    @BeforeEach
    void setUp() {
        File logFile = new File("news_history.log");
        if (logFile.exists()) {
            logFile.delete();
        }
    }

    @AfterEach
    void tearDown() {
        File logFile = new File("news_history.log");
        if (logFile.exists()) {
            logFile.delete();
        }
    }

    @Test
    void logAddShouldWriteCorrectFormat() throws IOException {
        ChangeLogger.logAdd("RBC", 10);

        File logFile = new File("news_history.log");
        assertTrue(logFile.exists(), "Log file should exist");

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line = reader.readLine();
            assertNotNull(line);
            assertTrue(line.contains("ДОБАВЛЕНО"));
            assertTrue(line.contains("RBC"));
            assertTrue(line.contains("10"));
        }
    }

    @Test
    void logDeleteShouldWriteCorrectFormat() throws IOException {
        ChangeLogger.logDelete(5, "2025-01-01");

        File logFile = new File("news_history.log");
        assertTrue(logFile.exists(), "Log file should exist");

        try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
            String line = reader.readLine();
            assertNotNull(line);
            assertTrue(line.contains("УДАЛЕНО"));
            assertTrue(line.contains("5"));
            assertTrue(line.contains("2025-01-01"));
        }
    }

    @Test
    void showHistoryShouldNotThrowException() {
        ChangeLogger.logAdd("Test", 1);
        assertDoesNotThrow(() -> ChangeLogger.showHistory());
    }
}