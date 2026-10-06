package ra.tor;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class ControlWaiterTest {

    /** A reply delivered before the caller reaches the wait must not be lost (it used to sleep the full minute). */
    @Test
    void responseDeliveredBeforeGetResponseIsNotLost() {
        TORControlConnection.Waiter waiter = new TORControlConnection.Waiter();
        List<TORControlConnection.ReplyLine> reply = Collections.singletonList(
                new TORControlConnection.ReplyLine("250", "OK", null));
        waiter.setResponse(reply);

        assertTimeoutPreemptively(java.time.Duration.ofSeconds(5), () ->
                assertSame(reply, waiter.getResponse()));
    }

    @Test
    void responseDeliveredWhileWaitingIsReturned() throws Exception {
        TORControlConnection.Waiter waiter = new TORControlConnection.Waiter();
        List<TORControlConnection.ReplyLine> reply = Collections.singletonList(
                new TORControlConnection.ReplyLine("250", "OK", null));
        CountDownLatch started = new CountDownLatch(1);
        Thread deliverer = new Thread(() -> {
            started.countDown();
            try { Thread.sleep(100); } catch (InterruptedException ignored) { }
            waiter.setResponse(reply);
        });
        deliverer.start();
        started.await(5, TimeUnit.SECONDS);

        assertTimeoutPreemptively(java.time.Duration.ofSeconds(5), () ->
                assertSame(reply, waiter.getResponse()));
    }
}
