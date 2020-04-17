package com.bugsnag.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class HandledStateTest {

    @Test
    public void testHandled() {
        HandledState handled = HandledState.newInstance(
            HandledState.REASON_HANDLED_EXCEPTION);
        assertNotNull(handled);
        assertFalse(handled.isUnhandled());
        assertEquals(Severity.WARNING, handled.getCurrentSeverity());
    }

    @Test
    public void testUnhandled() {
        HandledState unhandled = HandledState.newInstance(
            HandledState.REASON_UNHANDLED_EXCEPTION);
        assertNotNull(unhandled);
        assertTrue(unhandled.isUnhandled());
        assertEquals(Severity.ERROR, unhandled.getCurrentSeverity());
    }

    @Test
    public void testUserSpecified() {
        HandledState userSpecified = HandledState.newInstance(
            HandledState.REASON_USER_SPECIFIED, Severity.INFO, null);
        assertNotNull(userSpecified);
        assertFalse(userSpecified.isUnhandled());
        assertEquals(Severity.INFO, userSpecified.getCurrentSeverity());
    }

    @Test
    public void testStrictMode() {
        HandledState strictMode = HandledState.newInstance(
            HandledState.REASON_STRICT_MODE, null, "Test");
        assertNotNull(strictMode);
        assertTrue(strictMode.isUnhandled());
        assertEquals(Severity.WARNING, strictMode.getCurrentSeverity());
        assertEquals("Test", strictMode.getAttributeValue());
    }

    @Test
    public void testPromiseRejection() { // invoked via react native
        HandledState unhandled = HandledState.newInstance(
            HandledState.REASON_PROMISE_REJECTION);
        assertNotNull(unhandled);
        assertTrue(unhandled.isUnhandled());
        assertEquals(Severity.ERROR, unhandled.getCurrentSeverity());
    }

    @Test
    public void testLog() { // invoked via Unity
        HandledState unhandled = HandledState.newInstance(
            HandledState.REASON_LOG, Severity.WARNING, null);
        assertNotNull(unhandled);
        assertFalse(unhandled.isUnhandled());
        assertEquals(Severity.WARNING, unhandled.getCurrentSeverity());
    }

    @Test
    public void testCallbackSpecified() {
        HandledState handled = HandledState.newInstance(HandledState.REASON_HANDLED_EXCEPTION);
        assertEquals(HandledState.REASON_HANDLED_EXCEPTION,
            handled.calculateSeverityReasonType());

        handled.setCurrentSeverity(Severity.INFO);
        assertEquals(HandledState.REASON_CALLBACK_SPECIFIED,
            handled.calculateSeverityReasonType());
    }

    @Test
    public void testInvalidUserSpecified() {
        HandledState handled = HandledState.newInstance(HandledState.REASON_CALLBACK_SPECIFIED);
        assertEquals(HandledState.REASON_CALLBACK_SPECIFIED,
            handled.calculateSeverityReasonType());

        handled.setCurrentSeverity(Severity.INFO);
        assertEquals(HandledState.REASON_CALLBACK_SPECIFIED,
            handled.calculateSeverityReasonType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidStrictmodeVal() {
        HandledState.newInstance(HandledState.REASON_STRICT_MODE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidHandledVal() {
        HandledState.newInstance(HandledState.REASON_HANDLED_EXCEPTION, Severity.ERROR, "Whoops");
    }

}
