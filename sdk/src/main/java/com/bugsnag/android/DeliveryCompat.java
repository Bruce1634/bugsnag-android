package com.bugsnag.android;

import static com.bugsnag.android.DeliveryFailureException.Reason.CONNECTIVITY;
import static com.bugsnag.android.DeliveryFailureException.Reason.REQUEST_FAILURE;

/**
 * A compatibility implementation of {@link Delivery} which wraps {@link ErrorReportApiClient} and
 * {@link SessionTrackingApiClient}. This class allows for backwards compatibility for users still
 * utilising the old API, and should be removed in the next major version.
 */
class DeliveryCompat implements Delivery {

    volatile ErrorReportApiClient errorReportApiClient;
    volatile SessionTrackingApiClient sessionTrackingApiClient;

    @Override
    public void deliver(SessionTrackingPayload payload,
                        Configuration config) throws DeliveryFailureException {
        if (sessionTrackingApiClient != null) {

            try {
                sessionTrackingApiClient.postSessionTrackingPayload(config.getSessionEndpoint(),
                    payload, config.getSessionApiHeaders());
            } catch (NetworkException | BadResponseException exception) {
                throw convertException(exception);
            }
        }
    }

    @Override
    public void deliver(Report report, Configuration config) throws DeliveryFailureException {
        if (errorReportApiClient != null) {
            try {
                errorReportApiClient.postReport(config.getEndpoint(),
                    report, config.getErrorApiHeaders());
            } catch (NetworkException | BadResponseException exception) {
                throw convertException(exception);
            }
        }
    }

    DeliveryFailureException convertException(Exception exception) {
        if (exception instanceof NetworkException) {
            return new DeliveryFailureException(CONNECTIVITY, exception.getMessage(), exception);
        } else if (exception instanceof BadResponseException) {
            return new DeliveryFailureException(REQUEST_FAILURE, exception.getMessage(), exception);
        } else {
            return null;
        }
    }
}
