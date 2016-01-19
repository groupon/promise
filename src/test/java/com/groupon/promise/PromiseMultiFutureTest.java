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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test the PromiseMultiFuture.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public class PromiseMultiFutureTest {

    @Mock
    private PromiseHandler<PromiseFuture<String>> handler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void constructWithSuccessTest() {
        PromiseMultiFuture<String> future = new PromiseMultiFuture<>("success");

        assertTrue(future.complete());
        assertTrue(future.succeeded());
        assertFalse(future.failed());
        assertEquals("success", future.result());
        assertNull(future.cause());
    }

    @Test
    public void constructWithFailureTest() {
        Exception exception = new Exception("failed");
        PromiseMultiFuture<String> future = new PromiseMultiFuture<>(exception);

        assertTrue(future.complete());
        assertFalse(future.succeeded());
        assertTrue(future.failed());
        assertNull(future.result());
        assertEquals(exception, future.cause());
    }

    @Test
    public void setResultTest() {
        PromiseMultiFuture<String> future = new PromiseMultiFuture<>();

        assertFalse(future.complete());
        assertFalse(future.succeeded());
        assertFalse(future.failed());
        assertNull(future.result());
        assertNull(future.cause());

        future.setResult("success");

        assertTrue(future.complete());
        assertTrue(future.succeeded());
        assertFalse(future.failed());
        assertEquals("success", future.result());
        assertNull(future.cause());
    }

    @Test
    public void setResultWithHandlerTest() {
        PromiseMultiFuture<String> future = new PromiseMultiFuture<>();
        future.addHandler(handler);

        assertFalse(future.complete());
        assertFalse(future.succeeded());
        assertFalse(future.failed());
        assertNull(future.result());
        assertNull(future.cause());

        future.setResult("success");

        assertTrue(future.complete());
        assertTrue(future.succeeded());
        assertFalse(future.failed());
        assertEquals("success", future.result());
        assertNull(future.cause());

        verify(handler, times(1)).handle(future);
    }

    @Test
    public void setResultWithMultipleHandlersTest() {
        PromiseMultiFuture<String> future = new PromiseMultiFuture<>();
        future.addHandler(handler);
        future.addHandler(handler);

        assertFalse(future.complete());
        assertFalse(future.succeeded());
        assertFalse(future.failed());
        assertNull(future.result());
        assertNull(future.cause());

        future.setResult("success");

        assertTrue(future.complete());
        assertTrue(future.succeeded());
        assertFalse(future.failed());
        assertEquals("success", future.result());
        assertNull(future.cause());

        verify(handler, times(2)).handle(future);
    }

    @Test
    public void setResultWithPreAndPostHandlersTest() {
        PromiseMultiFuture<String> future = new PromiseMultiFuture<>();
        future.addHandler(handler);

        assertFalse(future.complete());
        assertFalse(future.succeeded());
        assertFalse(future.failed());
        assertNull(future.result());
        assertNull(future.cause());

        future.setResult("success");

        assertTrue(future.complete());
        assertTrue(future.succeeded());
        assertFalse(future.failed());
        assertEquals("success", future.result());
        assertNull(future.cause());

        future.addHandler(handler);

        verify(handler, times(2)).handle(future);
    }

    @Test
    public void setFailureTest() {
        Exception exception = new Exception("failed");
        PromiseMultiFuture<String> future = new PromiseMultiFuture<>();

        assertFalse(future.complete());
        assertFalse(future.succeeded());
        assertFalse(future.failed());
        assertNull(future.result());
        assertNull(future.cause());

        future.setFailure(exception);

        assertTrue(future.complete());
        assertFalse(future.succeeded());
        assertTrue(future.failed());
        assertNull(future.result());
        assertEquals(exception, future.cause());
    }

    @Test
    public void setFailureWithHandlerTest() {
        Exception exception = new Exception("failed");
        PromiseMultiFuture<String> future = new PromiseMultiFuture<>();
        future.addHandler(handler);

        assertFalse(future.complete());
        assertFalse(future.succeeded());
        assertFalse(future.failed());
        assertNull(future.result());
        assertNull(future.cause());

        future.setFailure(exception);

        assertTrue(future.complete());
        assertFalse(future.succeeded());
        assertTrue(future.failed());
        assertNull(future.result());
        assertEquals(exception, future.cause());

        verify(handler, times(1)).handle(future);
    }

    @Test
    public void setFailureWithMultipleHandlersTest() {
        Exception exception = new Exception("failed");
        PromiseMultiFuture<String> future = new PromiseMultiFuture<>();
        future.addHandler(handler);
        future.addHandler(handler);

        assertFalse(future.complete());
        assertFalse(future.succeeded());
        assertFalse(future.failed());
        assertNull(future.result());
        assertNull(future.cause());

        future.setFailure(exception);

        assertTrue(future.complete());
        assertFalse(future.succeeded());
        assertTrue(future.failed());
        assertNull(future.result());
        assertEquals(exception, future.cause());

        verify(handler, times(2)).handle(future);
    }

    @Test
    public void setFailureWithPreAndPostHandlersTest() {
        Exception exception = new Exception("failed");
        PromiseMultiFuture<String> future = new PromiseMultiFuture<>();
        future.addHandler(handler);

        assertFalse(future.complete());
        assertFalse(future.succeeded());
        assertFalse(future.failed());
        assertNull(future.result());
        assertNull(future.cause());

        future.setFailure(exception);

        assertTrue(future.complete());
        assertFalse(future.succeeded());
        assertTrue(future.failed());
        assertNull(future.result());
        assertEquals(exception, future.cause());

        future.addHandler(handler);

        verify(handler, times(2)).handle(future);
    }
}
