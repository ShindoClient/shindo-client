package me.miki.shindo.libs.spotify;

import com.google.common.collect.Lists;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.concurrent.CancellableDependency;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Args;
import org.apache.hc.core5.util.TimeValue;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.*;
@Contract(threading = ThreadingBehavior.STATELESS)
public class SpotifyHttpRequestRetryStrategy implements HttpRequestRetryStrategy {
    private final int maxRetries;
    private final TimeValue defaultRetryInterval;
    private final Set<Class<? extends IOException>> nonRetriableIOExceptionClasses;
    private final Set<Integer> retriableCodes;

    protected SpotifyHttpRequestRetryStrategy(
            final int maxRetries,
            final TimeValue defaultRetryInterval,
            final Collection<Class<? extends IOException>> clazzes,
            final Collection<Integer> codes) {
        Args.notNegative(maxRetries, "maxRetries");
        Args.notNegative(defaultRetryInterval.getDuration(), "defaultRetryInterval");
        this.maxRetries = maxRetries;
        this.defaultRetryInterval = defaultRetryInterval;
        this.nonRetriableIOExceptionClasses = new HashSet<>(clazzes);
        this.retriableCodes = new HashSet<>(codes);
    }
    public SpotifyHttpRequestRetryStrategy(
            final int maxRetries,
            final TimeValue defaultRetryInterval) {
        this(maxRetries, defaultRetryInterval,
                Arrays.asList(
                        InterruptedIOException.class,
                        UnknownHostException.class,
                        ConnectException.class,
                        ConnectionClosedException.class,
                        NoRouteToHostException.class,
                        SSLException.class),
                Lists.newArrayList(HttpStatus.SC_SERVICE_UNAVAILABLE));
    }
    public SpotifyHttpRequestRetryStrategy() {
        this(1, TimeValue.ofSeconds(1L));
    }

    @Override
    public boolean retryRequest(
            final HttpRequest request,
            final IOException exception,
            final int execCount,
            final HttpContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(exception, "exception");

        if (execCount > this.maxRetries) {

            return false;
        }
        if (this.nonRetriableIOExceptionClasses.contains(exception.getClass())) {
            return false;
        } else {
            for (final Class<? extends IOException> rejectException : this.nonRetriableIOExceptionClasses) {
                if (rejectException.isInstance(exception)) {
                    return false;
                }
            }
        }
        if (request instanceof CancellableDependency && ((CancellableDependency) request).isCancelled()) {
            return false;
        }

        return handleAsIdempotent(request);
    }

    @Override
    public boolean retryRequest(
            final HttpResponse response,
            final int execCount,
            final HttpContext context) {
        Objects.requireNonNull(response, "response");

        return execCount <= this.maxRetries && retriableCodes.contains(response.getCode());
    }

    @Override
    public TimeValue getRetryInterval(
            final HttpResponse response,
            final int execCount,
            final HttpContext context) {
        Objects.requireNonNull(response, "response");

        return this.defaultRetryInterval;
    }

    protected boolean handleAsIdempotent(final HttpRequest request) {
        return Method.isIdempotent(request.getMethod());
    }

}
