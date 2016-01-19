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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Test the PromiseListImpl.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public class PromiseListImplTest {

    @Mock
    private SyncPromiseFunction<Collection<String>, Integer> onFulfilled;

    @Mock
    private SyncPromiseListFunction<Collection<String>, Integer> onFulfilledMap;

    @Mock
    private SyncPromiseFunction<Void, Void> onFulfilledAfter;

    @Mock
    private PromiseHandler<PromiseFuture<Void>> handler;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDispatchListWithNoHandler() {
        List<String> results = Arrays.asList("one", "two");

        PromiseListImpl<String> future = new PromiseListImpl<>();
        Promise<Void> dispatches = future.after();
        future.fulfill(results);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(results.get(0), future.value().iterator().next());
        assertNull(future.reason());

        assertTrue(dispatches.fulfilled());
        assertFalse(dispatches.rejected());
        assertNull(dispatches.reason());
    }

    @Test
    public void testDispatchListWithHandler() throws Throwable {
        List<String> results = Arrays.asList("one", "two");

        PromiseListImpl<String> future = new PromiseListImpl<>();
        Promise<Void> dispatches = future.after();
        dispatches.then(onFulfilledAfter);
        future.fulfill(results);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(results.get(0), future.value().iterator().next());
        assertNull(future.reason());

        assertTrue(dispatches.fulfilled());
        assertFalse(dispatches.rejected());
        assertNull(dispatches.reason());

        verify(onFulfilledAfter, times(1)).handle(null);
    }

    @Test
    public void testDispatchNullWithHandler() throws Throwable {
        PromiseListImpl<String> future = new PromiseListImpl<>();
        Promise<Void> dispatches = future.after();
        dispatches.then(onFulfilledAfter);
        future.fulfill(null);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertNull(future.value());
        assertNull(future.reason());

        assertTrue(dispatches.fulfilled());
        assertFalse(dispatches.rejected());
        assertNull(dispatches.reason());

        verify(onFulfilledAfter, times(1)).handle(null);
    }

    @Test
    public void testDispatchListWithHandlers() throws Throwable {
        List<String> results = Arrays.asList("one", "two");
        Integer output = 1;
        Integer output2 = 2;

        when(onFulfilled.handle(results)).thenReturn(output);

        when(onFulfilledMap.handle(results)).thenReturn(Arrays.asList(output, output2));

        PromiseListImpl<String> future = new PromiseListImpl<>();
        future.thenList(onFulfilledMap).map();
        Promise<Integer> thenPromise = future.then(onFulfilled);

        Promise<Void> dispatches = future.after();
        dispatches.then(onFulfilledAfter);

        future.fulfill(results);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(results.get(0), future.value().iterator().next());
        assertNull(future.reason());

        assertFalse(thenPromise.pending());
        assertTrue(thenPromise.fulfilled());
        assertFalse(thenPromise.rejected());
        assertEquals(output, thenPromise.value());
        assertNull(thenPromise.reason());

        assertTrue(dispatches.fulfilled());
        assertFalse(dispatches.rejected());
        assertNull(dispatches.reason());

        verify(onFulfilledAfter, times(1)).handle(null);
    }

    @Test
    public void testConcurrencyLimitingWithBadInput() {
        PromiseListImpl<String> promiseList = new PromiseListImpl<>();

        try {
            promiseList.map(0);
            fail("Didn't throw exception");
        } catch (IllegalArgumentException iae) {
            assertNotNull(iae);
        }
    }

    @Test
    public void testConcurrencyLimitingWithFailure() {
        List<String> data = Arrays.asList("one", "two", "three", "four", "five");
        final Map<String, PromiseFuture<Void>> futures = new HashMap<>();

        PromiseListImpl<String> promiseList = new PromiseListImpl<>();
        promiseList.map(2).then(new AsyncPromiseFunction<String, Void>() {
            @Override
            public PromiseFuture<Void> handle(String data) {
                PromiseFuture<Void> rval = new DefaultPromiseFuture<>();
                futures.put(data, rval);
                return rval;
            }
        });
        Promise<Void> dispatches = promiseList.after();
        promiseList.fulfill(data);

        assertTrue(dispatches.pending());
        assertEquals(2, futures.size());
        assertTrue(futures.containsKey("one"));
        assertTrue(futures.containsKey("two"));

        futures.get("two").setResult(null);

        assertEquals(3, futures.size());
        assertTrue(futures.containsKey("three"));
        assertTrue(dispatches.pending());

        futures.get("one").setFailure(new Exception("Exception"));

        assertEquals(4, futures.size());
        assertTrue(futures.containsKey("four"));
        assertTrue(dispatches.pending());

        futures.get("four").setResult(null);

        assertEquals(5, futures.size());
        assertTrue(futures.containsKey("five"));
        assertTrue(dispatches.pending());

        futures.get("three").setResult(null);

        assertEquals(5, futures.size());
        assertTrue(dispatches.pending());

        futures.get("five").setResult(null);

        assertEquals(5, futures.size());
        assertFalse(dispatches.pending());
    }

    @Test
    public void testConcurrencyLimitingWithFewerElements() {
        List<String> data = Arrays.asList("one", "two");
        final Map<String, PromiseFuture<Void>> futures = new HashMap<>();

        PromiseListImpl<String> promiseList = new PromiseListImpl<>();
        promiseList.map(10).then(new AsyncPromiseFunction<String, Void>() {
            @Override
            public PromiseFuture<Void> handle(String data) {
                PromiseFuture<Void> rval = new DefaultPromiseFuture<>();
                futures.put(data, rval);
                return rval;
            }
        });
        Promise<Void> dispatches = promiseList.after();
        promiseList.fulfill(data);

        assertTrue(dispatches.pending());
        assertEquals(2, futures.size());
        assertTrue(futures.containsKey("one"));
        assertTrue(futures.containsKey("two"));

        futures.get("two").setResult(null);

        assertEquals(2, futures.size());
        assertTrue(dispatches.pending());

        futures.get("one").setResult(null);

        assertEquals(2, futures.size());
        assertFalse(dispatches.pending());
    }

    @Test
    public void testConcurrencyLimitingWithEqualElements() {
        List<String> data = Arrays.asList("one", "two", "three");
        final Map<String, PromiseFuture<Void>> futures = new HashMap<>();

        PromiseListImpl<String> promiseList = new PromiseListImpl<>();
        promiseList.map(3).then(new AsyncPromiseFunction<String, Void>() {
            @Override
            public PromiseFuture<Void> handle(String data) {
                PromiseFuture<Void> rval = new DefaultPromiseFuture<>();
                futures.put(data, rval);
                return rval;
            }
        });
        Promise<Void> dispatches = promiseList.after();
        promiseList.fulfill(data);

        assertTrue(dispatches.pending());
        assertEquals(3, futures.size());
        assertTrue(futures.containsKey("one"));
        assertTrue(futures.containsKey("two"));
        assertTrue(futures.containsKey("three"));

        futures.get("two").setResult(null);

        assertEquals(3, futures.size());
        assertTrue(dispatches.pending());

        futures.get("one").setResult(null);
        futures.get("three").setResult(null);

        assertEquals(3, futures.size());
        assertFalse(dispatches.pending());
    }

    @Test
    public void testConcurrencyLimitingSynchronousWithFailure() {
        List<String> data = Arrays.asList("one", "two", "three", "four", "five");
        final Map<String, Boolean> futures = new HashMap<>();

        PromiseListImpl<String> promiseList = new PromiseListImpl<>();
        promiseList.map(2).then(new SyncPromiseFunction<String, Boolean>() {
            public Boolean handle(String data) {
                futures.put(data, Boolean.TRUE);

                if ("one".equals(data)) {
                    throw new RuntimeException("one failed");
                }
                return Boolean.TRUE;
            }
        });
        Promise<Void> dispatches = promiseList.after();
        promiseList.fulfill(data);

        assertEquals(5, futures.size());
        assertTrue(futures.containsKey("one"));
        assertTrue(futures.containsKey("two"));
        assertTrue(futures.containsKey("three"));
        assertTrue(futures.containsKey("four"));
        assertTrue(futures.containsKey("five"));
        assertFalse(dispatches.pending());
    }

    @Test
    public void testConcurrencyLimitingSynchronousWithFewerElements() {
        List<String> data = Arrays.asList("one", "two");
        final Map<String, Boolean> futures = new HashMap<>();

        PromiseListImpl<String> promiseList = new PromiseListImpl<>();
        promiseList.map(10).then(new SyncPromiseFunction<String, Boolean>() {
            public Boolean handle(String data) {
                futures.put(data, Boolean.TRUE);
                return Boolean.TRUE;
            }
        });
        Promise<Void> dispatches = promiseList.after();
        promiseList.fulfill(data);

        assertEquals(2, futures.size());
        assertTrue(futures.containsKey("one"));
        assertTrue(futures.containsKey("two"));
        assertFalse(dispatches.pending());
    }

    @Test
    public void testConcurrencyLimitingSynchronousWithEqualElements() {
        List<String> data = Arrays.asList("one", "two", "three");
        final Map<String, Boolean> futures = new HashMap<>();

        PromiseListImpl<String> promiseList = new PromiseListImpl<>();
        promiseList.map(3).then(new SyncPromiseFunction<String, Boolean>() {
            public Boolean handle(String data) {
                futures.put(data, Boolean.TRUE);
                return Boolean.TRUE;
            }
        });
        Promise<Void> dispatches = promiseList.after();
        promiseList.fulfill(data);

        assertEquals(3, futures.size());
        assertTrue(futures.containsKey("one"));
        assertTrue(futures.containsKey("two"));
        assertTrue(futures.containsKey("three"));
        assertFalse(dispatches.pending());
    }

    @Test
    public void testThenListSuccessWithPromise() {
        List<String> results = Arrays.asList("one", "two");

        PromiseListImpl<String> parent = new PromiseListImpl<>();
        PromiseListImpl<String> future = new PromiseListImpl<>();
        parent.thenList(future);
        parent.fulfill(results);

        assertFalse(parent.pending());
        assertTrue(parent.fulfilled());
        assertFalse(parent.rejected());
        assertEquals(results.get(0), parent.value().iterator().next());
        assertNull(parent.reason());

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(results.get(0), future.value().iterator().next());
        assertNull(future.reason());
    }

    @Test
    public void testThenListFailureWithPromise() {
        Exception exception = new Exception("error");

        PromiseListImpl<String> parent = new PromiseListImpl<>();
        PromiseListImpl<String> future = new PromiseListImpl<>();
        parent.thenList(future);
        parent.reject(exception);

        assertFalse(parent.pending());
        assertFalse(parent.fulfilled());
        assertTrue(parent.rejected());

        assertFalse(future.pending());
        assertFalse(future.fulfilled());
        assertTrue(future.rejected());
        assertNull(future.value());
        assertEquals(exception, future.reason());
    }
}
