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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.groupon.promise.PromiseList;
import com.groupon.promise.PromiseListImpl;
import com.groupon.promise.exception.IllegalPromiseException;

/**
 * Added a FulfilledListValueTest to test the generic promise list function FulFilledListValueFunction
 *
 * @author Gil Markham (gil at groupon dot com)
 * @since 0.10
 */
public class FulfilledListValueFunctionTest {
    @Mock
    private PromiseList<String> promise;

    private Collection<String> promiseVal = Collections.singletonList("foo");

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(promise.fulfilled()).thenReturn(true);
        when(promise.value()).thenReturn(promiseVal);
    }

    @Test
    public void testHandleWithSuccessfulPromise() throws Throwable {
        FulfilledListValueFunction<String, String> function = new FulfilledListValueFunction<>(promise);
        final Collection<String> result = function.handle("any_string");
        assertThat(result, is(promiseVal));
    }

    @Test
    public void testHandleWithRejectedPromise() throws Throwable {
        when(promise.fulfilled()).thenReturn(false);
        when(promise.rejected()).thenReturn(true);
        Exception exception = new Exception("Promise did not complete");
        when(promise.reason()).thenReturn(exception);
        FulfilledListValueFunction<String, String> function = new FulfilledListValueFunction<>(promise);
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
        FulfilledListValueFunction<String, String> function = new FulfilledListValueFunction<>(promise);
        function.handle("any_string");
    }

    @Test(expected = IllegalStateException.class)
    public void testHandleWithIncompletePromise() throws Throwable {
        PromiseList<String> localPromise = new PromiseListImpl<>();
        FulfilledListValueFunction<String, String> function = new FulfilledListValueFunction<>(localPromise);
        function.handle("any_string");
        fail();
    }
}
