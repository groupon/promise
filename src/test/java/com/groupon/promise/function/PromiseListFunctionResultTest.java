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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.groupon.promise.ComparablePromiseFunction;
import com.groupon.promise.PromiseFuture;
import com.groupon.promise.SyncPromiseListFunction;

/**
 * Test the PromiseListFunctionResult.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public class PromiseListFunctionResultTest {

    private List<Integer> promiseListResult = Arrays.asList(2);
    private TestPromiseListFunction promiseListFunction = new TestPromiseListFunction(promiseListResult);

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHandle() throws Exception {
        PromiseListFunctionResult<String, Integer> function = new PromiseListFunctionResult<>(promiseListFunction);
        final PromiseFuture<Collection<Integer>> future = function.handle("any");
        assertTrue(future.complete());
        assertThat(future.result(), is(promiseListResult));
    }

    @Test
    public void testHandleNull() throws Exception {
        PromiseListFunctionResult<Integer, String> function = new PromiseListFunctionResult<>(null);
        final PromiseFuture<Collection<String>> future = function.handle(1);
        assertTrue(future.failed());
        assertNull(future.result());
    }

    @Test
    public void testEquivalence() {
        PromiseListFunctionResult<String, Integer> functionA = new PromiseListFunctionResult<>(promiseListFunction);
        PromiseListFunctionResult<String, Integer> functionB = new PromiseListFunctionResult<>(promiseListFunction);

        assertTrue(functionA.equivalent(functionB));
        assertTrue(functionB.equivalent(functionA));
        assertTrue(functionA.equivalent(promiseListFunction));
        assertFalse(functionA.equivalent(null));
    }

    class TestPromiseListFunction implements SyncPromiseListFunction<String, Integer>, ComparablePromiseFunction {
        private List<Integer> list;

        TestPromiseListFunction(List<Integer> list) {
            this.list = list;
        }

        @Override
        public List<Integer> handle(String data) {
            return list;
        }

        @Override
        public boolean equivalent(Object o) {
            return this == o;
        }
    }
}
