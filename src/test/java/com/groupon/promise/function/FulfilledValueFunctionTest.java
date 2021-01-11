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
package com.groupon.promise.function;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import com.groupon.promise.Promise;
import com.groupon.promise.PromiseImpl;
import com.groupon.promise.exception.IllegalPromiseException;

/**
 * Added a FulfilledValueTest to test the generic promise function FulFilledFunction
 *
 * @author Swati Kumar (swkumar at groupon dot com)
 * @since 0.1
 */
public class FulfilledValueFunctionTest {
    @Mock
    private Promise<Integer> promise;

    private Integer promiseVal = new Integer(200);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(promise.fulfilled()).thenReturn(true);
        when(promise.value()).thenReturn(promiseVal);
    }

    @Test
    public void testHandleWithSuccessfulPromise() throws Throwable {
        FulfilledValueFunction<String, Integer> function = new FulfilledValueFunction<>(promise);
        final Integer result = function.handle("any_string");
        assertThat(result, is(promiseVal));
    }

    @Test
    public void testHandleWithRejectedPromise() throws Throwable {
        when(promise.fulfilled()).thenReturn(false);
        when(promise.rejected()).thenReturn(true);
        Exception exception = new Exception("Promise did not complete");
        when(promise.reason()).thenReturn(exception);
        FulfilledValueFunction<String, Integer> function = new FulfilledValueFunction<>(promise);
        try {
            function.handle("any_string");
            fail();
        } catch (Exception e) {
            assertEquals(exception, e);
        }
    }

    @Test(expected = IllegalPromiseException.class)
    public void testHandleWithNullPromise() throws Throwable {
        promise = null;
        FulfilledValueFunction<String, Integer> function = new FulfilledValueFunction<>(promise);
        function.handle("any_string");
    }

    @Test(expected = IllegalStateException.class)
    public void testHandleWithIncompletePromise() throws Throwable {
        Promise<Integer> localPromise = new PromiseImpl<>();
        FulfilledValueFunction<String, Integer> function = new FulfilledValueFunction<>(localPromise);
        function.handle("any_string");
        fail();
    }
}
