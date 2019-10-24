package com.bugsnag.android.mazerunner.scenarios;

import com.bugsnag.android.BeforeSend;
import com.bugsnag.android.Bugsnag;
import com.bugsnag.android.Configuration;
import com.bugsnag.android.Report;
import com.bugsnag.android.Severity;

import android.content.Context;
import androidx.annotation.NonNull;

public class NotifyBeforeSendUnsetContextScenario extends Scenario {

    /**
     * Constructs a scenario where notify is called with a beforeSend block and the context is
     * unset
     */
    public NotifyBeforeSendUnsetContextScenario(@NonNull Configuration config,
                                                @NonNull Context context) {
        super(config, context);
        config.setAutoTrackSessions(false);
        config.addBeforeSend(new BeforeSend() {
            @Override
            public boolean run(Report report) {
                report.getEvent().setContext(null);
                report.getEvent().setSeverity(Severity.ERROR);

                return true;
            }
        });
    }

    @Override
    public void run() {
        super.run();
        Bugsnag.notify(new Exception("Registration failure"));
    }
}
