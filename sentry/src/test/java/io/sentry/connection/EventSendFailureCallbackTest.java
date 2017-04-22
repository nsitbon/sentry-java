package io.sentry.connection;

import io.sentry.DefaultSentryClientFactory;
import io.sentry.SentryClient;
import io.sentry.dsn.Dsn;
import io.sentry.event.Event;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class EventSendFailureCallbackTest {

    @Test
    public void testSimpleCallback() {
        final AtomicBoolean flag = new AtomicBoolean(false);

        DefaultSentryClientFactory factory = new DefaultSentryClientFactory() {
            @Override
            protected Connection createConnection(Dsn dsn) {
                Connection connection = super.createConnection(dsn);

                connection.addEventSendFailureCallback(new EventSendFailureCallback() {
                    @Override
                    public void onFailure(Event event, Exception exception) {
                        flag.set(true);
                    }
                });

                return connection;
            }
        };

        String dsn = "https://foo:bar@localhost:1234/1?async=false";
        SentryClient sentryClient = factory.createSentryClient(new Dsn(dsn));
        sentryClient.sendMessage("Message that will fail because DSN points to garbage.");

        assertThat(flag.get(), is(true));
    }

    @Test
    public void testExceptionInsideCallback() {
        DefaultSentryClientFactory factory = new DefaultSentryClientFactory() {
            @Override
            protected Connection createConnection(Dsn dsn) {
                Connection connection = super.createConnection(dsn);

                connection.addEventSendFailureCallback(new EventSendFailureCallback() {
                    @Override
                    public void onFailure(Event event, Exception exception) {
                        throw new RuntimeException("Error inside of EventSendFailureCallback");
                    }
                });

                return connection;
            }
        };

        String dsn = "https://foo:bar@localhost:1234/1?async=false";
        SentryClient sentryClient = factory.createSentryClient(new Dsn(dsn));
        sentryClient.sendMessage("Message that will fail because DSN points to garbage.");
    }

}
