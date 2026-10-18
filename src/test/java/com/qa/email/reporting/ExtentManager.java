package com.qa.email.reporting;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.qa.email.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Owns the single ExtentReports instance and a per-thread ExtentTest (safe for parallel runs).
 *
 * Every run is written to its own timestamped folder OUTSIDE target/, so "mvn clean" never
 * deletes history:
 *   test-results/2026-10-18_19-56-02/email-test-report.html   (+ email-tests.log)
 *   test-results/latest/email-test-report.html                (copy of the newest run)
 */
public final class ExtentManager {

    private static final Logger LOG = LogManager.getLogger(ExtentManager.class);

    private static final String REPORT_NAME = "email-test-report.html";
    private static final String LOG_SOURCE = "target/logs/email-tests.log";
    private static final Path RESULTS_ROOT = Paths.get(ConfigManager.get("report.dir", "test-results"));
    private static final Path RUN_DIR = RESULTS_ROOT.resolve(
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")));
    private static final ThreadLocal<ExtentTest> CURRENT = new ThreadLocal<>();
    private static ExtentReports extent;

    private ExtentManager() {
    }

    public static synchronized ExtentReports instance() {
        if (extent == null) {
            ExtentSparkReporter spark = new ExtentSparkReporter(RUN_DIR.resolve(REPORT_NAME).toString());
            spark.config().setDocumentTitle("Email Test Report");
            spark.config().setReportName("Email Automation - " + ConfigManager.env().toUpperCase());
            extent = new ExtentReports();
            extent.attachReporter(spark);
            extent.setSystemInfo("Environment", ConfigManager.env());
            extent.setSystemInfo("Java", System.getProperty("java.version"));
            extent.setSystemInfo("Run folder", RUN_DIR.toString());
        }
        return extent;
    }

    public static void startTest(String name, String description) {
        CURRENT.set(instance().createTest(name, description));
    }

    public static ExtentTest current() {
        return CURRENT.get();
    }

    public static void endTest() {
        CURRENT.remove();
    }

    /** Writes the report, then refreshes the "latest" copy and stores this run's log beside it. */
    public static synchronized void flush() {
        if (extent == null) {
            return;
        }
        extent.flush();
        try {
            Path report = RUN_DIR.resolve(REPORT_NAME);
            Path latest = RESULTS_ROOT.resolve("latest");
            Files.createDirectories(latest);
            Files.copy(report, latest.resolve(REPORT_NAME), StandardCopyOption.REPLACE_EXISTING);

            Path log = Paths.get(LOG_SOURCE);
            if (Files.exists(log)) {
                Files.copy(log, RUN_DIR.resolve("email-tests.log"), StandardCopyOption.REPLACE_EXISTING);
            }
            LOG.info("Report saved to {}", report.toAbsolutePath());
        } catch (IOException e) {
            LOG.warn("Could not archive report/log: {}", e.getMessage());
        }
    }
}
