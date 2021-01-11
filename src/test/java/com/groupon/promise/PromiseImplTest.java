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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.groupon.promise.exception.PromiseException;
import com.groupon.promise.function.EchoFunction;
import com.groupon.promise.function.FixedValueFunction;
import com.groupon.promise.function.PromiseFunctionResult;
import com.groupon.promise.function.PromiseListFunctionResult;

/**
 * Test the PromiseImpl.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public class PromiseImplTest {
    @Mock
    private PromiseHandler<PromiseFuture<Void>> handler;

    @Mock
    private PromiseHandler<PromiseFuture<Throwable>> errorHandler;

    @Mock
    private PromiseFunctionResult<String, Integer> onFulfilled;

    @Mock
    private PromiseFunctionResult<Integer, String> onFulfilledReversed;

    @Mock
    private PromiseListFunctionResult<String, Integer> onFulfilledMap;

    @Mock
    private PromiseListFunctionResult<Integer, String> onFulfilledMapReversed;

    @Mock
    private PromiseFunctionResult<Void, Void> onFulfilledAfter;

    @Mock
    private PromiseFunctionResult<Throwable, String> onRejectedString;

    @Mock
    private PromiseFunctionResult<Throwable, Integer> onRejectedInteger;

    @Mock
    private PromiseListFunctionResult<Throwable, String> onRejectedStringList;

    @Mock
    private PromiseListFunctionResult<Throwable, Integer> onRejectedIntegerList;

    @Mock
    private PromiseFunctionResult<Throwable, Void> onRejectedAfter;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testEmptyCreate() {
        PromiseImpl<String> future = new PromiseImpl<>();

        assertTrue(future.pending());
        assertFalse(future.fulfilled());
        assertFalse(future.rejected());
        assertFalse(future.optional());
        assertNull(future.value());
        assertNull(future.reason());
    }

    @Test
    public void testDispatchWithNoHandler() {
        String result = "success";

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Void> dispatch = future.after();
        future.fulfill(result);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertFalse(future.optional());
        assertEquals(result, future.value());
        assertNull(future.reason());

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());
    }

    @Test
    public void testDispatchWithHandler() {
        String result = "success";

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Void> dispatch = future.after();
        dispatch.then(onFulfilledAfter);
        future.fulfill(result);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertFalse(future.optional());
        assertEquals(result, future.value());
        assertNull(future.reason());

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());

        verify(onFulfilledAfter, times(1)).handle(null);
    }

    @Test
    public void testDispatchFailureWithNoHandler() {
        RuntimeException exception = new RuntimeException();

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Void> dispatch = future.after();
        future.reject(exception);

        assertFalse(future.pending());
        assertFalse(future.fulfilled());
        assertTrue(future.rejected());
        assertFalse(future.optional());
        assertNull(future.value());
        assertEquals(exception, future.reason());

        assertFalse(dispatch.fulfilled());
        assertTrue(dispatch.rejected());
        assertEquals(exception, dispatch.reason().getSuppressed()[0]);
    }

    @Test
    public void testDispatchFailureWithNoHandlerAndOptional() {
        RuntimeException exception = new RuntimeException();

        Promise<String> future = new PromiseImpl<String>().optional(true);
        Promise<Void> dispatch = future.after();
        future.reject(exception);

        assertFalse(future.pending());
        assertFalse(future.fulfilled());
        assertTrue(future.rejected());
        assertTrue(future.optional());
        assertNull(future.value());
        assertEquals(exception, future.reason());

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());
    }

    @Test
    public void testDispatchFailureWithHandler() {
        RuntimeException exception = new RuntimeException();

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Void> dispatch = future.after();
        dispatch.then(onFulfilledAfter, onRejectedAfter);
        future.reject(exception);

        assertFalse(future.pending());
        assertFalse(future.fulfilled());
        assertTrue(future.rejected());
        assertNull(future.value());
        assertEquals(exception, future.reason());

        assertFalse(dispatch.fulfilled());
        assertTrue(dispatch.rejected());
        assertEquals(exception, dispatch.reason().getSuppressed()[0]);

        verify(onFulfilledAfter, never()).handle(null);
        verify(onRejectedAfter, times(1)).handle(any(PromiseException.class));
    }

    @Test
    public void testDispatchFailureWithHandlerAndOptional() {
        RuntimeException exception = new RuntimeException();

        Promise<String> future = new PromiseImpl<String>().optional(true);
        Promise<Void> dispatch = future.after();
        dispatch.then(onFulfilledAfter);
        future.reject(exception);

        assertFalse(future.pending());
        assertFalse(future.fulfilled());
        assertTrue(future.rejected());
        assertTrue(future.optional());
        assertNull(future.value());
        assertEquals(exception, future.reason());

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());

        verify(onFulfilledAfter, times(1)).handle(null);
    }

    @Test
    public void testThenSuccessWithPromise() {
        String result = "success";

        PromiseImpl<String> parent = new PromiseImpl<>();
        PromiseImpl<String> future = new PromiseImpl<>();
        parent.then(future);

        Promise<Void> dispatch = parent.after();
        parent.fulfill(result);

        assertFalse(parent.pending());
        assertTrue(parent.fulfilled());
        assertFalse(parent.rejected());
        assertFalse(parent.optional());
        assertEquals(result, parent.value());
        assertNull(parent.reason());

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertFalse(future.optional());
        assertEquals(result, future.value());
        assertNull(future.reason());

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());
    }

    @Test
    public void testThenFailureWithPromise() {
        Exception exception = new Exception("error");

        PromiseImpl<String> parent = new PromiseImpl<>();
        PromiseImpl<String> future = new PromiseImpl<>();
        parent.then(future);

        Promise<Void> dispatch = parent.after();
        parent.reject(exception);

        assertFalse(parent.pending());
        assertFalse(parent.fulfilled());
        assertTrue(parent.rejected());
        assertFalse(parent.optional());
        assertNull(parent.value());
        assertEquals(exception, parent.reason());

        assertFalse(future.pending());
        assertFalse(future.fulfilled());
        assertTrue(future.rejected());
        assertFalse(future.optional());
        assertNull(future.value());
        assertEquals(exception, future.reason());

        assertFalse(dispatch.fulfilled());
        assertTrue(dispatch.rejected());
        assertNull(dispatch.value());
    }

    @Test
    public void testThenCreation() {
        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> then = future.then(onFulfilled);

        assertNotNull(then);
        assertTrue(then.pending());
        assertFalse(then.fulfilled());
        assertFalse(then.rejected());
        assertNull(then.value());
        assertNull(then.reason());

        verify(onFulfilled, never()).handle(any(String.class));
    }

    @Test
    public void testMultipleThenCreation() {
        PromiseImpl<String> future = new PromiseImpl<>();

        List<Promise<Integer>> thenList = new ArrayList<>();
        thenList.add(future.then(onFulfilled));
        thenList.add(future.then(onFulfilled));

        for (Promise<Integer> then : thenList) {
            assertNotNull(then);
            assertTrue(then.pending());
            assertFalse(then.fulfilled());
            assertFalse(then.rejected());
            assertNull(then.value());
            assertNull(then.reason());
        }

        verify(onFulfilled, never()).handle(any(String.class));
    }

    @Test
    public void testDispatchThen() {
        String input = "result";
        Integer output = 1;

        when(onFulfilled.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> then = future.then(onFulfilled);

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        assertNotNull(then);
        assertFalse(then.pending());
        assertTrue(then.fulfilled());
        assertFalse(then.rejected());
        assertEquals(output, then.value());
        assertNull(then.reason());

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());

        verify(onFulfilled, times(1)).handle(input);
    }

    @Test
    public void testDispatchThenWithNullOnFulfilled() {
        PromiseImpl<String> future = new PromiseImpl<>();

        try {
            assertNull("Should have thrown an exception", future.then((AsyncPromiseFunction<String, String>) null));
        } catch (IllegalArgumentException iae) {
            assertNotNull(iae);
        }
    }

    @Test
    public void testDispatchMultipleThen() {
        String input = "result";
        Integer output = 1;

        when(onFulfilled.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));
        when(onFulfilled.equivalent(onFulfilled)).thenReturn(true);

        PromiseImpl<String> future = new PromiseImpl<>();

        List<Promise<Integer>> thenList = new ArrayList<>();
        thenList.add(future.then(onFulfilled));
        thenList.add(future.then(onFulfilled));

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        for (Promise<Integer> then : thenList) {
            assertNotNull(then);
            assertFalse(then.pending());
            assertTrue(then.fulfilled());
            assertFalse(then.rejected());
            assertEquals(output, then.value());
            assertNull(then.reason());
        }

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());

        verify(onFulfilled, times(1)).handle(input);
    }

    @Test
    public void testDispatchMultipleThenWithDuplication() {
        String input = "result";
        Integer output = 1;

        when(onFulfilled.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));

        PromiseImpl<String> future = new PromiseImpl<>();
        future.nonduplicating(false);

        List<Promise<Integer>> thenList = new ArrayList<>();
        thenList.add(future.then(onFulfilled));
        thenList.add(future.then(onFulfilled));

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        for (Promise<Integer> then : thenList) {
            assertNotNull(then);
            assertFalse(then.pending());
            assertTrue(then.fulfilled());
            assertFalse(then.rejected());
            assertEquals(output, then.value());
            assertNull(then.reason());
        }

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());

        verify(onFulfilled, times(2)).handle(input);
    }

    @Test
    public void testDispatchNestedThen() {
        String input = "success";
        Integer output = 1;

        when(onFulfilled.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));
        when(onFulfilledReversed.handle(output)).thenReturn(new DefaultPromiseFuture<>(input));

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> then = future.then(onFulfilled);
        Promise<String> nestedThen = then.then(onFulfilledReversed);

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        assertNotNull(then);
        assertFalse(then.pending());
        assertTrue(then.fulfilled());
        assertFalse(then.rejected());
        assertEquals(output, then.value());
        assertNull(then.reason());

        assertNotNull(nestedThen);
        assertFalse(nestedThen.pending());
        assertTrue(nestedThen.fulfilled());
        assertFalse(nestedThen.rejected());
        assertEquals(input, nestedThen.value());
        assertNull(nestedThen.reason());

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());

        verify(onFulfilled, times(1)).handle(input);
        verify(onFulfilledReversed, times(1)).handle(output);
    }

    @Test
    public void testDispatchNestedThenAndOptional() {
        String input = "success";
        RuntimeException error = new RuntimeException("error");

        when(onFulfilled.handle(input)).thenReturn(new DefaultPromiseFuture<Integer>(error));

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> then = future.then(onFulfilled).optional(true);
        Promise<String> nestedThen = then.then(onFulfilledReversed);

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        assertNotNull(then);
        assertFalse(then.pending());
        assertFalse(then.fulfilled());
        assertTrue(then.rejected());
        assertNull(then.value());
        assertEquals(error, then.reason());

        assertNotNull(nestedThen);
        assertFalse(nestedThen.pending());
        assertFalse(nestedThen.fulfilled());
        assertTrue(nestedThen.rejected());
        assertNull(input, nestedThen.value());
        assertEquals(error, nestedThen.reason());

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());

        verify(onFulfilled, times(1)).handle(input);
        verifyNoMoreInteractions(onFulfilled);
        verifyNoMoreInteractions(onFulfilledReversed);
    }

    @Test
    public void testDispatchNestedThenWithError() {
        String input = "success";
        RuntimeException error = new RuntimeException("error");

        when(onFulfilled.handle(input)).thenReturn(new DefaultPromiseFuture<Integer>(error));

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> then = future.then(onFulfilled);
        Promise<String> nestedThen = then.then(onFulfilledReversed);

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        assertNotNull(then);
        assertFalse(then.pending());
        assertFalse(then.fulfilled());
        assertTrue(then.rejected());
        assertNull(then.value());
        assertEquals(error, then.reason());

        assertNotNull(nestedThen);
        assertFalse(nestedThen.pending());
        assertFalse(nestedThen.fulfilled());
        assertTrue(nestedThen.rejected());
        assertNull(nestedThen.value());
        assertEquals(error, nestedThen.reason());

        assertFalse(dispatch.fulfilled());
        assertTrue(dispatch.rejected());
        assertEquals(error, dispatch.reason().getSuppressed()[0]);

        verify(onFulfilled, times(1)).handle(input);
        verify(onFulfilledReversed, never()).handle(any(Integer.class));
    }

    @Test
    public void testOptionalWithAfter() {
        String input = "success";
        Exception error = new Exception("error");

        Promise<String> a = new PromiseImpl<>();
        Promise<String> b = a.then(new EchoFunction<>());
        Promise<String> c = b.then(new EchoFunction<>()).optional(true);
        Promise<Void> d = b.after();

        a.reject(error);

        assertTrue(a.rejected());
        assertTrue(b.rejected());
        assertTrue(c.rejected());
        assertTrue(d.rejected());
    }

    @Test
    public void testDedupeAfterMap() {
        final AtomicInteger counter = new AtomicInteger(0);
        CounterFunction countFunction = new CounterFunction(counter);

        Promise<Void> root = new PromiseImpl<>();
        root.then(new FixedValueFunction<Void, String>("a")).then(countFunction);
        root.then(new FixedValueFunction<Void, String>("b")).then(countFunction);
        root.then(new FixedValueFunction<Void, String>("c")).then(countFunction);

        root.thenList(new SyncPromiseListFunction<Void, String>() {
            public List<String> handle(Void data) {
                return Arrays.asList("a", "b", "c", "a", "b", "c");
            }
        }).map().then(countFunction);
        root.fulfill(null);

        assertEquals(3, counter.get());
    }

    @Test
    public void testDispatchNestedThenWithLeafError() {
        String input = "success";
        Integer output = 1;
        RuntimeException error = new RuntimeException("error");

        when(onFulfilled.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));
        when(onFulfilledReversed.handle(output)).thenReturn(new DefaultPromiseFuture<String>(error));

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> then = future.then(onFulfilled);
        Promise<String> nestedThen = then.then(onFulfilledReversed);

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        assertNotNull(then);
        assertFalse(then.pending());
        assertTrue(then.fulfilled());
        assertFalse(then.rejected());
        assertEquals(output, then.value());
        assertNull(then.reason());

        assertNotNull(nestedThen);
        assertFalse(nestedThen.pending());
        assertFalse(nestedThen.fulfilled());
        assertTrue(nestedThen.rejected());
        assertNull(nestedThen.value());
        assertEquals(error, nestedThen.reason());

        assertFalse(dispatch.fulfilled());
        assertTrue(dispatch.rejected());
        assertEquals(error, dispatch.reason().getSuppressed()[0]);

        verify(onFulfilled, times(1)).handle(input);
        verify(onFulfilledReversed, times(1)).handle(output);
    }

    @Test
    public void testDispatchThrowableWithNestedThen() {
        RuntimeException error = new RuntimeException("failure");

        when(onRejectedInteger.handle(error)).thenReturn(new DefaultPromiseFuture<Integer>(error));
        when(onRejectedString.handle(error)).thenReturn(new DefaultPromiseFuture<String>(error));

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> then = future.then(onFulfilled, onRejectedInteger);
        Promise<String> nestedThen = then.then(onFulfilledReversed, onRejectedString);

        Promise<Void> dispatch = future.after();
        future.reject(error);

        assertFalse(future.pending());
        assertFalse(future.fulfilled());
        assertTrue(future.rejected());
        assertNull(future.value());
        assertEquals(error, future.reason());

        assertFalse(then.pending());
        assertFalse(then.fulfilled());
        assertTrue(then.rejected());
        assertNull(then.value());
        assertEquals(error, then.reason());

        assertFalse(nestedThen.pending());
        assertFalse(nestedThen.fulfilled());
        assertTrue(nestedThen.rejected());
        assertNull(nestedThen.value());
        assertEquals(error, nestedThen.reason());

        assertFalse(dispatch.fulfilled());
        assertTrue(dispatch.rejected());
        assertEquals(error, dispatch.reason().getSuppressed()[0]);

        verify(onFulfilled, never()).handle(any(String.class));
        verify(onFulfilledReversed, never()).handle(any(Integer.class));
        verify(onRejectedInteger, times(1)).handle(error);
        verify(onRejectedString, times(1)).handle(error);
    }

    @Test
    public void testDispatchThrowableWithOnRejectSuccessAndNestedThen() {
        RuntimeException error = new RuntimeException("failure");

        when(onRejectedInteger.handle(error)).thenReturn(new DefaultPromiseFuture<>(1));
        when(onFulfilledReversed.handle(1)).thenReturn(new DefaultPromiseFuture<>("success"));

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> then = future.then(onFulfilled, onRejectedInteger);
        Promise<String> nestedThen = then.then(onFulfilledReversed, onRejectedString);

        Promise<Void> dispatch = future.after();
        future.reject(error);

        assertFalse(future.pending());
        assertFalse(future.fulfilled());
        assertTrue(future.rejected());
        assertNull(future.value());
        assertEquals(error, future.reason());

        assertFalse(then.pending());
        assertTrue(then.fulfilled());
        assertFalse(then.rejected());
        assertEquals(Integer.valueOf(1), then.value());
        assertNull(then.reason());

        assertFalse(nestedThen.pending());
        assertTrue(nestedThen.fulfilled());
        assertFalse(nestedThen.rejected());
        assertEquals("success", nestedThen.value());
        assertNull(nestedThen.reason());

        assertFalse(dispatch.fulfilled());
        assertTrue(dispatch.rejected());
        assertNotNull(dispatch.reason());

        verify(onFulfilled, never()).handle(any(String.class));
        verify(onFulfilledReversed, times(1)).handle(any(Integer.class));
        verify(onRejectedInteger, times(1)).handle(error);
    }

    @Test
    public void testDispatchMultipleThenWithAfter() {
        String input = "result";
        Integer output = 1;

        when(onFulfilled.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));
        when(onFulfilled.equivalent(onFulfilled)).thenReturn(true);

        PromiseImpl<String> future = new PromiseImpl<>();

        List<Promise<Integer>> thenList = new ArrayList<>();
        thenList.add(future.then(onFulfilled));
        thenList.add(future.then(onFulfilled));

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        for (Promise<Integer> then : thenList) {
            assertNotNull(then);
            assertFalse(then.pending());
            assertTrue(then.fulfilled());
            assertFalse(then.rejected());
            assertEquals(output, then.value());
            assertNull(then.reason());
        }

        assertFalse(dispatch.pending());
        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.value());
        assertNull(dispatch.reason());

        verify(onFulfilled, times(1)).handle(input);
    }

    @Test
    public void testDispatchMultipleThenWithAfterAndDuplicates() {
        String input = "result";
        Integer output = 1;

        when(onFulfilled.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));

        PromiseImpl<String> future = new PromiseImpl<>();
        future.nonduplicating(false);

        List<Promise<Integer>> thenList = new ArrayList<>();
        thenList.add(future.then(onFulfilled));
        thenList.add(future.then(onFulfilled));

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        for (Promise<Integer> then : thenList) {
            assertNotNull(then);
            assertFalse(then.pending());
            assertTrue(then.fulfilled());
            assertFalse(then.rejected());
            assertEquals(output, then.value());
            assertNull(then.reason());
        }

        assertFalse(dispatch.pending());
        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.value());
        assertNull(dispatch.reason());

        verify(onFulfilled, times(2)).handle(input);
    }

    @Test
    public void testDispatchNestedThenWithAfter() {
        String input = "result";
        Integer output = 1;

        when(onFulfilled.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));
        when(onFulfilledReversed.handle(output)).thenReturn(new DefaultPromiseFuture<>(input));

        PromiseImpl<String> future = new PromiseImpl<>();

        Promise<Integer> firstThen = future.then(onFulfilled);
        Promise<String> secondThen = firstThen.then(onFulfilledReversed);

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        assertNotNull(firstThen);
        assertFalse(firstThen.pending());
        assertTrue(firstThen.fulfilled());
        assertFalse(firstThen.rejected());
        assertEquals(output, firstThen.value());
        assertNull(firstThen.reason());

        assertNotNull(secondThen);
        assertFalse(secondThen.pending());
        assertTrue(secondThen.fulfilled());
        assertFalse(secondThen.rejected());
        assertEquals(input, secondThen.value());
        assertNull(secondThen.reason());

        assertFalse(dispatch.pending());
        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.value());
        assertNull(dispatch.reason());

        verify(onFulfilled, times(1)).handle(input);
        verify(onFulfilledReversed, times(1)).handle(output);
    }

    @Test
    public void testDispatchNestedThenUnfinishedWithAfter() {
        String input = "result";
        Integer output = 1;

        when(onFulfilled.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));
        when(onFulfilledReversed.handle(output)).thenReturn(new DefaultPromiseFuture<String>());

        PromiseImpl<String> future = new PromiseImpl<>();

        Promise<Integer> firstThen = future.then(onFulfilled);
        Promise<String> secondThen = firstThen.then(onFulfilledReversed);

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        assertNotNull(firstThen);
        assertFalse(firstThen.pending());
        assertTrue(firstThen.fulfilled());
        assertFalse(firstThen.rejected());
        assertEquals(output, firstThen.value());
        assertNull(firstThen.reason());

        assertNotNull(secondThen);
        assertTrue(secondThen.pending());
        assertFalse(secondThen.fulfilled());
        assertFalse(secondThen.rejected());
        assertNull(secondThen.value());
        assertNull(secondThen.reason());

        assertTrue(dispatch.pending());
        assertFalse(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.value());
        assertNull(dispatch.reason());

        verify(onFulfilled, times(1)).handle(input);
        verify(onFulfilledReversed, times(1)).handle(output);
    }

    @Test
    public void testMapCreation() {
        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> map = future.thenList(onFulfilledMap).map();

        assertNotNull(map);
        assertTrue(map.pending());
        assertFalse(map.fulfilled());
        assertFalse(map.rejected());
        assertNull(map.value());
        assertNull(map.reason());

        verify(onFulfilledMap, never()).handle(any(String.class));
    }

    @Test
    public void testMultipleMapCreation() {
        PromiseImpl<String> future = new PromiseImpl<>();

        List<Promise<Integer>> mapList = new ArrayList<>();
        mapList.add(future.thenList(onFulfilledMap).map());
        mapList.add(future.thenList(onFulfilledMap).map());

        for (Promise<Integer> map : mapList) {
            assertNotNull(map);
            assertTrue(map.pending());
            assertFalse(map.fulfilled());
            assertFalse(map.rejected());
            assertNull(map.value());
            assertNull(map.reason());
        }

        verify(onFulfilled, never()).handle(any(String.class));
    }

    @Test
    public void testDispatchMap() {
        String input = "result";
        List<Integer> output = Arrays.asList(1, 2);

        when(onFulfilledMap.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));

        PromiseImpl<String> future = new PromiseImpl<>();
        future.thenList(onFulfilledMap).map();

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());

        verify(onFulfilledMap, times(1)).handle(input);
    }

    @Test
    public void testDispatchMapWithNullOnFulfilled() {
        PromiseImpl<String> future = new PromiseImpl<>();

        try {
            assertNull("Should have thrown an exception", future.thenList((PromiseListFunction<String, Void>) null).map());
        } catch (IllegalArgumentException iae) {
            assertNotNull(iae);
        }
    }

    @Test
    public void testEmptyList() {
        String result = "success";

        PromiseImpl<String> future = new PromiseImpl<>();
        future.thenList(new SyncPromiseListFunction<String, String>() {
            public List<String> handle(String data) {
                return Collections.emptyList();
            }
        }).map().then(new SyncPromiseFunction<String, Object>() {
            public Object handle(String data) {
                return data;
            }
        });

        Promise<Void> dispatch = future.after();
        future.fulfill(result);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(result, future.value());
        assertNull(future.reason());

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());
    }

    @Test
    public void testDispatchMultipleMap() {
        String input = "result";
        List<Integer> output = Arrays.asList(1, 2);

        when(onFulfilledMap.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));
        when(onFulfilledMap.equivalent(onFulfilledMap)).thenReturn(true);

        PromiseImpl<String> future = new PromiseImpl<>();

        future.thenList(onFulfilledMap).map();
        future.thenList(onFulfilledMap).map();

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());

        verify(onFulfilledMap, times(1)).handle(input);
    }

    @Test
    public void testDispatchMultipleMapWithDuplicates() {
        String input = "result";
        List<Integer> output = Arrays.asList(1, 2);

        when(onFulfilledMap.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));

        PromiseImpl<String> future = new PromiseImpl<>();
        future.nonduplicating(false);

        future.thenList(onFulfilledMap).map();
        future.thenList(onFulfilledMap).map();

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());

        verify(onFulfilledMap, times(2)).handle(input);
    }

    @Test
    public void testDispatchMapWithNestedThen() {
        String input = "result";
        List<Integer> output = Arrays.asList(1, 2);

        when(onFulfilledMap.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));

        for (Integer intOut : output) {
            when(onFulfilledReversed.handle(intOut)).thenReturn(new DefaultPromiseFuture<>(input));
        }

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> map = future.thenList(onFulfilledMap).map();
        map.then(onFulfilledReversed);

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(future.pending());
        assertTrue(future.fulfilled());
        assertFalse(future.rejected());
        assertEquals(input, future.value());
        assertNull(future.reason());

        assertTrue(dispatch.fulfilled());
        assertFalse(dispatch.rejected());
        assertNull(dispatch.reason());

        verify(onFulfilledMap, times(1)).handle(input);
        verify(onFulfilledReversed, times(1)).handle(1);
        verify(onFulfilledReversed, times(1)).handle(2);
    }

    @Test
    public void testDispatchMapWithNestedMap() {
        String input = "result";
        RuntimeException error = new RuntimeException("error");

        when(onFulfilledMap.handle(input)).thenReturn(new DefaultPromiseFuture<Collection<Integer>>(error));

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> map = future.thenList(onFulfilledMap).map();
        map.thenList(onFulfilledMapReversed).map();

        Promise<Void> dispatch = future.after();
        future.fulfill(input);

        assertFalse(dispatch.fulfilled());
        assertTrue(dispatch.rejected());
        assertEquals(error, dispatch.reason().getSuppressed()[0]);

        verify(onFulfilledMap, times(1)).handle(input);
        verify(onFulfilledMapReversed, never()).handle(any(Integer.class));
    }

    @Test
    public void testDispatchMapWithNestedFailure() {
        RuntimeException error = new RuntimeException("1");

        when(onRejectedIntegerList.handle(error)).thenReturn(new DefaultPromiseFuture<Collection<Integer>>(error));
        when(onRejectedString.handle(error)).thenReturn(new DefaultPromiseFuture<>(error));

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> map = future.thenList(onFulfilledMap, onRejectedIntegerList).map();
        map.then(onFulfilledReversed, onRejectedString);

        Promise<Void> dispatch = future.after();
        future.reject(error);

        assertFalse(dispatch.fulfilled());
        assertTrue(dispatch.rejected());
        assertEquals(error, dispatch.reason().getSuppressed()[0]);

        verify(onFulfilledMap, never()).handle(any(String.class));
        verify(onFulfilledReversed, never()).handle(any(Integer.class));
        verify(onRejectedIntegerList, times(1)).handle(error);
        verify(onRejectedString, times(1)).handle(error);
    }

    @Test
    public void testDispatchMapFailureWithOnRejectSuccessOnNested() {
        String success = "success";
        RuntimeException error = new RuntimeException("1");

        when(onRejectedIntegerList.handle(error)).thenReturn(new DefaultPromiseFuture<>(Collections.singletonList(1)));
        when(onFulfilledReversed.handle(1)).thenReturn(new DefaultPromiseFuture<>(success));

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> map = future.thenList(onFulfilledMap, onRejectedIntegerList).map();
        map.then(onFulfilledReversed, onRejectedString);

        Promise<Void> dispatch = future.after();
        future.reject(error);

        assertFalse(dispatch.fulfilled());
        assertTrue(dispatch.rejected());
        assertNotNull(dispatch.reason());

        verify(onFulfilledMap, never()).handle(any(String.class));
        verify(onRejectedIntegerList, times(1)).handle(error);
        verify(onFulfilledReversed, times(1)).handle(any(Integer.class));
        verify(onRejectedString, never()).handle(any(Throwable.class));
    }

    @Test
    public void testDispatchMapWithNestedFailureAlt() {
        RuntimeException error = new RuntimeException("1");

        when(onRejectedIntegerList.handle(error)).thenReturn(new DefaultPromiseFuture<Collection<Integer>>(error));
        when(onRejectedString.handle(error)).thenReturn(new DefaultPromiseFuture<>(error));

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> map = future.thenList(onFulfilledMap, onRejectedIntegerList).map();
        map.then(onFulfilledReversed, onRejectedString);

        Promise<Void> dispatch = future.after();
        future.reject(error);

        assertFalse(dispatch.fulfilled());
        assertTrue(dispatch.rejected());
        assertEquals(error, dispatch.reason().getSuppressed()[0]);

        verify(onFulfilledMap, never()).handle(any(String.class));
        verify(onFulfilledReversed, never()).handle(any(Integer.class));
        verify(onRejectedIntegerList, times(1)).handle(error);
        verify(onRejectedString, times(1)).handle(error);
    }

    @Test
    public void testMultipleDispatchFails() {
        String input = "result";
        List<Integer> output = Arrays.asList(1, 2);

        when(onFulfilledMap.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));

        for (Integer intOut : output) {
            when(onFulfilledReversed.handle(intOut)).thenReturn(new DefaultPromiseFuture<>(input));
        }

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> map = future.thenList(onFulfilledMap).map();
        map.then(onFulfilledReversed);

        future.fulfill(input);

        try {
            future.fulfill(input);
            assertFalse("Failed to throw error", true);
        } catch (IllegalStateException ise) {
            assertNotNull(ise);
        }
    }

    @Test
    public void testMultipleNestedDispatchFails() {
        String input = "result";
        List<Integer> output = Arrays.asList(1, 2);

        when(onFulfilledMap.handle(input)).thenReturn(new DefaultPromiseFuture<>(output));

        for (Integer intOut : output) {
            when(onFulfilledReversed.handle(intOut)).thenReturn(new DefaultPromiseFuture<>(input));
        }

        PromiseImpl<String> future = new PromiseImpl<>();
        Promise<Integer> map = future.thenList(onFulfilledMap).map();
        Promise<String> then = map.then(onFulfilledReversed);

        future.fulfill(input);

        try {
            then.fulfill(input);
            assertFalse("Failed to throw error", true);
        } catch (IllegalStateException ise) {
            assertNotNull(ise);
        }
    }

    @Test
    public void testDeferredAfter() {
        Map<String, PromiseFuture<String>> results = new HashMap<>();

        Promise<String> future = new PromiseImpl<>();
        future.then((AsyncPromiseFunction<String, String>) data -> {
            PromiseFuture<String> current = new DefaultPromiseFuture<>();
            results.put("one", current);
            return current;
        });
        future.then((AsyncPromiseFunction<String, String>) data -> {
            PromiseFuture<String> current = new DefaultPromiseFuture<>();
            results.put("two", current);
            return current;
        });
        future.then((AsyncPromiseFunction<String, String>) data -> {
            PromiseFuture<String> current = new DefaultPromiseFuture<>();
            results.put("three", current);
            return current;
        });

        Map<String, PromiseFuture<Void>> afterResults = new HashMap<>();

        Promise<Void> after = future.after();
        after.then((AsyncPromiseFunction<Void, Void>) data -> {
            PromiseFuture<Void> current = new DefaultPromiseFuture<>();
            afterResults.put("four", current);
            return current;
        }, (AsyncPromiseFunction<Throwable, Void>) data -> {
            PromiseFuture<Void> current = new DefaultPromiseFuture<>();
            afterResults.put("five", current);
            return current;

        });

        assertEquals(0, results.size());
        assertEquals(0, afterResults.size());
        assertTrue(after.pending());

        future.fulfill("result");

        assertEquals(3, results.size());
        assertEquals(0, afterResults.size());
        assertTrue(after.pending());

        results.get("one").setResult("success");
        results.get("two").setResult("success");

        assertEquals(3, results.size());
        assertEquals(0, afterResults.size());
        assertTrue(after.pending());

        results.get("three").setResult("success");

        assertEquals(3, results.size());
        assertEquals(1, afterResults.size());
        assertTrue(after.fulfilled());
    }

    @Test
    public void testThenWithLambdaSuccess() {
        String input = "result";

        List<String> results = new ArrayList<>();

        PromiseImpl<String> future = new PromiseImpl<>();
        future.thenSync(data -> {
            results.add(input);
            return input;
        });
        future.thenSync(new EchoFunction<>(), data -> {
            results.add(input);
            return input;
        });
        future.thenSync(data -> {
            results.add(input);
            return input;
        }, data -> {
            results.add(input);
            return input;
        });
        future.thenAsync(data -> {
            results.add(input);
            return new DefaultPromiseFuture<>(input);
        });
        future.thenAsync(data -> {
            results.add(input);
            return new DefaultPromiseFuture<>(input);
        }, onRejectedString);

        future.fulfill(input);

        assertEquals(4, results.size());
    }

    @Test
    public void testThenWithLambdaRejected() {
        Throwable error = new Exception("result");

        List<String> results = new ArrayList<>();

        PromiseImpl<String> future = new PromiseImpl<>();
        future.thenSync(data -> {
            results.add("result");
            return "result";
        }, data -> {
            results.add("result");
            return "result";
        });
        future.thenSync(new EchoFunction<>(), data -> {
            results.add("result");
            return "result";
        });
        future.thenSync(data -> {
            results.add("result");
            return "result";
        }, data -> {
            results.add("result");
            return "result";
        });
        future.thenAsync(data -> {
            results.add("result");
            return new DefaultPromiseFuture<>("result");
        });
        future.thenAsync(data -> {
            results.add("result");
            return new DefaultPromiseFuture<>("result");
        }, data -> {
            results.add("result");
            return new DefaultPromiseFuture<>("result");
        });

        future.reject(error);

        assertEquals(4, results.size());
    }

    private class CounterFunction implements SyncPromiseFunction<String, Integer>, ComparablePromiseFunction {
        private AtomicInteger counter;

        CounterFunction(AtomicInteger counter) {
            this.counter = counter;
        }

        public Integer handle(String value) {
            return counter.incrementAndGet();
        }

        @Override
        public boolean equivalent(Object o) {
            return this == o || (o != null && CounterFunction.class.equals(o.getClass()));
        }
    }
}
