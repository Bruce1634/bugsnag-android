package com.bugsnag.android;

import android.os.StrictMode;
import androidx.annotation.NonNull;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * Provides automatic notification hooks for unhandled exceptions.
 */
class ExceptionHandler implements UncaughtExceptionHandler {

    private static final String STRICT_MODE_TAB = "StrictMode";
    private static final String STRICT_MODE_KEY = "Violation";

    private final UncaughtExceptionHandler originalHandler;
    private final StrictModeHandler strictModeHandler = new StrictModeHandler();
    private final Client client;

    ExceptionHandler(Client client) {
        this.client = client;
        this.originalHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
        boolean strictModeThrowable = strictModeHandler.isStrictModeThrowable(throwable);

        // Notify any subscribed clients of the uncaught exception
        MetaData metaData = new MetaData();
        String violationDesc = null;

        if (strictModeThrowable) { // add strictmode policy violation to metadata
            violationDesc = strictModeHandler.getViolationDescription(throwable.getMessage());
            metaData = new MetaData();
            metaData.addMetadata(STRICT_MODE_TAB, STRICT_MODE_KEY, violationDesc);
        }

        String severityReason = strictModeThrowable
                ? HandledState.REASON_STRICT_MODE : HandledState.REASON_UNHANDLED_EXCEPTION;

        if (strictModeThrowable) { // writes to disk on main thread
            StrictMode.ThreadPolicy originalThreadPolicy = StrictMode.getThreadPolicy();
            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.LAX);

            client.cacheAndNotify(throwable, Severity.ERROR,
                    metaData, severityReason, violationDesc, thread);

            StrictMode.setThreadPolicy(originalThreadPolicy);
        } else {
            client.cacheAndNotify(throwable, Severity.ERROR,
                    metaData, severityReason, violationDesc, thread);
        }

        // Pass exception on to original exception handler
        if (originalHandler != null) {
            originalHandler.uncaughtException(thread, throwable);
        } else {
            System.err.printf("Exception in thread \"%s\" ", thread.getName());
            Logger.warn("Exception", throwable);
        }
    }
}