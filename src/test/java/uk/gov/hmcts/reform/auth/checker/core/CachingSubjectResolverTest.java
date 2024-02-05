package uk.gov.hmcts.reform.auth.checker.core;

import com.google.common.base.Ticker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class CachingSubjectResolverTest {


    private static final Subject SOME_SUBJECT = mock(Subject.class);
    private static final int MAXIMUM_SIZE = 2;

    private final SubjectResolver delegate = mock(SubjectResolver.class);
    private final FakeTicker ticker = new FakeTicker();

    @BeforeEach
    void setUp() {
        when(delegate.getTokenDetails(anyString())).thenReturn(SOME_SUBJECT);
    }

    @Test
    void shouldNotCallDelegateAgainIfTtlHasNotPassed() {
        CachingSubjectResolver<Subject> cachingSubjectResolver = cachingSubjectResolverWithTtl(10);

        cachingSubjectResolver.getTokenDetails("token");
        verify(delegate, times(1)).getTokenDetails("token");

        cachingSubjectResolver.getTokenDetails("token");
        verifyNoMoreInteractions(delegate);
    }

    @Test
    void shouldCallDelegateAgainIfTtlHasNotPassed() {
        CachingSubjectResolver<Subject> cachingSubjectResolver = cachingSubjectResolverWithTtl(10);

        cachingSubjectResolver.getTokenDetails("token");
        verify(delegate, times(1)).getTokenDetails("token");

        ticker.time = SECONDS.toNanos(10);

        cachingSubjectResolver.getTokenDetails("token");
        verify(delegate, times(2)).getTokenDetails("token");
    }

    @Test
    void maximumCacheSizeEnforcementShouldWork() {
        CachingSubjectResolver<Subject> cachingSubjectResolver = cachingSubjectResolverWithTtl(10);

        cachingSubjectResolver.getTokenDetails("tokenA");   // A in
        cachingSubjectResolver.getTokenDetails("tokenB");   // B in
        cachingSubjectResolver.getTokenDetails("tokenC");   // A out, C in
        cachingSubjectResolver.getTokenDetails("tokenD");   // B out, D in
        cachingSubjectResolver.getTokenDetails("tokenC");
        cachingSubjectResolver.getTokenDetails("tokenD");
        cachingSubjectResolver.getTokenDetails("tokenA");   // C out, A in
        cachingSubjectResolver.getTokenDetails("tokenB");   // D out, B in1

        verify(delegate, times(2)).getTokenDetails("tokenA");
        verify(delegate, times(2)).getTokenDetails("tokenB");
        verify(delegate, times(1)).getTokenDetails("tokenC");
        verify(delegate, times(1)).getTokenDetails("tokenD");
    }

    @Test
    void settingTtlToZeroShouldDisableCaching() {
        CachingSubjectResolver<Subject> cachingSubjectResolver = cachingSubjectResolverWithTtl(0);

        cachingSubjectResolver.getTokenDetails("token");
        verify(delegate, times(1)).getTokenDetails("token");

        cachingSubjectResolver.getTokenDetails("token");
        verify(delegate, times(2)).getTokenDetails("token");
    }

    @Test
    void shouldNotWrapExceptionIfOneOccurs() {
        CachingSubjectResolver<Subject> cachingSubjectResolver = cachingSubjectResolverWithTtl(0);
        when(delegate.getTokenDetails(anyString())).thenThrow(new IllegalArgumentException());

        assertThrows(IllegalArgumentException.class, () -> cachingSubjectResolver.getTokenDetails("token"));
    }

    private CachingSubjectResolver<Subject> cachingSubjectResolverWithTtl(int ttlInSeconds) {
        return new CachingSubjectResolver<Subject>(delegate, ttlInSeconds, MAXIMUM_SIZE, ticker);
    }

    private static class FakeTicker extends Ticker {
        private long time;

        @Override
        public long read() {
            return time;
        }
    }
}
