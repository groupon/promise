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

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.groupon.promise.function.PromiseFunctionResult;

/**
 * Test the PromiseTuple.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public class PromiseTupleTest {

    @Mock
    private PromiseImpl<Integer> promise;

    @Mock
    private PromiseFunctionResult<String, Integer> onFulfilled;

    @Mock
    private PromiseFunctionResult<Throwable, Integer> onRejected;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPromiseTuple() {
        PromiseTuple<String, Integer> tuple = new PromiseTuple<>(promise, onFulfilled, onRejected);

        assertEquals(promise, tuple.promise());
        assertEquals(onFulfilled, tuple.onFulfilled());
        assertEquals(onRejected, tuple.onRejected());
    }
}
