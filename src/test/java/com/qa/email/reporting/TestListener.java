package com.qa.email.reporting;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/** Logs every test outcome and feeds the Extent HTML report. Registered in the suite XML. */
public class TestListener implements ITestListener {

    private static final Logger LOG = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        String name = result.getMethod().getMethodName();
        LOG.info("START  {}", name);
        ExtentManager.startTest(name, result.getMethod().getDescription());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        LOG.info("PASS   {}", result.getMethod().getMethodName());
        if (ExtentManager.current() != null) {
            ExtentManager.current().pass("Passed");
        }
        ExtentManager.endTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        LOG.error("FAIL   {}", result.getMethod().getMethodName(), result.getThrowable());
        if (ExtentManager.current() != null) {
            ExtentManager.current().fail(result.getThrowable());
        }
        ExtentManager.endTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        LOG.warn("SKIP   {} ({})", result.getMethod().getMethodName(),
                result.getThrowable() == null ? "" : result.getThrowable().getMessage());
        if (ExtentManager.current() != null) {
            ExtentManager.current().skip(result.getThrowable() == null ? "Skipped"
                    : result.getThrowable().getMessage());
        }
        ExtentManager.endTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flush();
    }
}
