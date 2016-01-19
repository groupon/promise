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
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.groupon.promise.Promise;
import com.groupon.promise.PromiseImpl;

/**
 * Test the RejectPromiseFunction.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.11
 */
public class RejectPromiseFunctionTest {
    private Promise<Integer> promise;

    private Exception error = new Exception("error");

    @Before
    public void setUp() {
        promise = new PromiseImpl<>();
    }

    @Test
    public void testHandle() throws Throwable {
        RejectPromiseFunction<Integer> function = new RejectPromiseFunction<>(promise);

        try {
            function.handle(error);
            fail("Unexpected success");
        } catch (Exception ex) {
            assertEquals(error, ex);
        }

        assertFalse(promise.pending());
        assertFalse(promise.fulfilled());
        assertTrue(promise.rejected());
        assertEquals(error, promise.reason());
    }
}
