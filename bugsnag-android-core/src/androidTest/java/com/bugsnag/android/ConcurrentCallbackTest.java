package com.bugsnag.android;

import androidx.annotation.NonNull;
import androidx.test.filters.SmallTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

/**
 * Ensures that if a callback is added or removed during iteration, a
 * {@link java.util.ConcurrentModificationException} is not thrown
 */
@SmallTest
public class ConcurrentCallbackTest {

    private Client client;

    @Before
    public void setUp() throws Exception {
        client = BugsnagTestUtils.generateClient();
    }

    @After
    public void tearDown() throws Exception {
        client.close();
    }

    @Test
    public void testClientNotifyModification() throws Exception {
        Configuration config = (Configuration) client.getConfiguration();
        final Collection<BeforeNotify> beforeNotifyTasks = config.getBeforeNotifyTasks();
        client.addBeforeNotify(new BeforeNotify() {
            @Override
            public boolean run(@NonNull Error error) {
                beforeNotifyTasks.add(new BeforeNotifySkeleton());
                // modify the Set, when iterating to the next callback this should not crash
                return true;
            }
        });
        client.addBeforeNotify(new BeforeNotifySkeleton());
        client.notify(new RuntimeException());
    }

    @Test
    public void testClientBreadcrumbModification() throws Exception {
        Configuration config = (Configuration) client.getConfiguration();
        final Collection<OnBreadcrumb> breadcrumbTasks =
                config.getBreadcrumbCallbacks();

        client.addOnBreadcrumb(new OnBreadcrumb() {
            @Override
            public boolean run(@NonNull Breadcrumb breadcrumb) {
                breadcrumbTasks.add(new OnBreadcrumbSkeleton());
                // modify the Set, when iterating to the next callback this should not crash
                return true;
            }
        });
        client.addOnBreadcrumb(new OnBreadcrumbSkeleton());
        client.leaveBreadcrumb("Whoops");
        client.notify(new RuntimeException());
    }

    static class BeforeNotifySkeleton implements BeforeNotify {
        @Override
        public boolean run(@NonNull Error error) {
            return true;
        }
    }

    static class OnBreadcrumbSkeleton implements OnBreadcrumb {
        @Override
        public boolean run(@NonNull Breadcrumb breadcrumb) {
            return true;
        }
    }

}