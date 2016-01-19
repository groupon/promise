/**
 * Copyright 2015 Groupon.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.groupon.promise;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.groupon.promise.exception.PromiseException;

/**
 * Test the PromiseCountdownHandler.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public class PromiseCountdownHandlerTest {
    @Mock
    private PromiseHandler<Void> success;

    @Mock
    private PromiseHandler<Throwable> failure;

    @Captor
    private ArgumentCaptor<PromiseException> failureCaptor;

    private AtomicInteger counter;
    private PromiseCountdownHandler handler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        counter = new AtomicInteger(1);
        handler = new PromiseCountdownHandler(counter, success, failure);
    }

    @Test
    public void countdownSuccessTest() {
        handler.handleSuccess();

        verify(success, times(1)).handle(null);
        verify(failure, never()).handle(any(Throwable.class));
        assertEquals(0, counter.get());
    }

    @Test
    public void countdownFailureTest() {
        Exception e = new Exception("failure");
        handler.handleFailure(e);

        verify(success, never()).handle(null);
        verify(failure, times(1)).handle(any(Throwable.class));
        assertEquals(0, counter.get());
    }

    @Test
    public void duplicateCountdownSuccessTest() {
        handler.handleSuccess();
        handler.handleSuccess();

        verify(success, times(1)).handle(null);
        verify(failure, never()).handle(any(Throwable.class));
        assertEquals(-1, counter.get());
    }

    @Test
    public void duplicateCountdownFailureTest() {
        handler.handleFailure(new Exception("failure"));
        handler.handleFailure(new Exception("failureDuplicate"));

        verify(success, never()).handle(null);
        verify(failure, times(1)).handle(any(Throwable.class));
        assertEquals(-1, counter.get());
    }

    @Test
    public void verifyNoSuppressedException() {
        PromiseException ignore = new PromiseException();

        handler.handleFailure(ignore);

        verify(success, never()).handle(null);
        verify(failure, times(1)).handle(failureCaptor.capture());

        PromiseException capturedError = failureCaptor.getValue();
        assertNotNull(capturedError.getSuppressed());
        assertEquals(0, capturedError.getSuppressed().length);

        assertEquals(0, counter.get());
    }

    @Test
    public void verifyFlattenedSuppressedException() {
        counter.incrementAndGet();

        PromiseException ignore = new PromiseException();
        ignore.addSuppressed(new Exception("failure"));

        Exception secondError = new Exception("secondError");
        handler.handleFailure(ignore);
        handler.handleFailure(secondError);

        verify(success, never()).handle(null);
        verify(failure, times(1)).handle(failureCaptor.capture());

        PromiseException capturedError = failureCaptor.getValue();
        assertNotNull(capturedError.getSuppressed());
        assertEquals(2, capturedError.getSuppressed().length);
        assertEquals(ignore.getSuppressed()[0], capturedError.getSuppressed()[0]);
        assertEquals(secondError, capturedError.getSuppressed()[1]);

        assertEquals(0, counter.get());
    }
}
