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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.groupon.promise.Promise;
import com.groupon.promise.PromiseImpl;

/**
 * Test the FulfillPromiseFunction.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.11
 */
public class FulfillPromiseFunctionTest {
    private Promise<Integer> promise;

    private Integer promiseVal = new Integer(200);

    @Before
    public void setUp() {
        promise = new PromiseImpl<>();
    }

    @Test
    public void testHandle() throws Throwable {
        FulfillPromiseFunction<Integer> function = new FulfillPromiseFunction<>(promise);
        final Integer result = function.handle(promiseVal);
        assertEquals(result, promiseVal);

        assertFalse(promise.pending());
        assertTrue(promise.fulfilled());
        assertFalse(promise.rejected());
        assertEquals(result, promise.value());
    }
}
