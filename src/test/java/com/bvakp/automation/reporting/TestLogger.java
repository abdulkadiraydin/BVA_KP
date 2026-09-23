package com.bvakp.automation.reporting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TestLogger {

    private TestLogger() {
    }

    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
}