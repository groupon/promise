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
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import com.groupon.promise.function.EchoFunction;
import com.groupon.promise.function.EchoFunctionExtended;
import com.groupon.promise.function.FixedValueFunction;

/**
 * Test the PromiseFunctionKey.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public class PromiseFunctionKeyTest {

    @Test
    public void equalityTest() {
        PromiseFunctionKey<String> key1 = new PromiseFunctionKey<>(new EchoFunction<String>(), "a");
        PromiseFunctionKey<String> key2 = new PromiseFunctionKey<>(new EchoFunction<String>(), "a");

        assertEquals(key1, key2);
    }

    @Test
    public void differentValuesTest() {
        PromiseFunctionKey<String> key1 = new PromiseFunctionKey<>(new EchoFunction<String>(), "a");
        PromiseFunctionKey<String> key2 = new PromiseFunctionKey<>(new EchoFunction<String>(), "b");

        assertNotEquals(key1, key2);
    }

    @Test
    public void differentFunctionsTest() {
        PromiseFunctionKey<String> key1 = new PromiseFunctionKey<>(new EchoFunction<String>(), "a");
        PromiseFunctionKey<String> key2 = new PromiseFunctionKey<>(new FixedValueFunction<String, String>("a"), "a");

        assertNotEquals(key1, key2);
    }

    @Test
    public void differentFunctionsByExtensionTest() {
        PromiseFunctionKey<String> key1 = new PromiseFunctionKey<>(new EchoFunction<String>(), "a");
        PromiseFunctionKey<String> key2 = new PromiseFunctionKey<>(new EchoFunctionExtended<String>("a"), "a");

        assertNotEquals(key1, key2);
    }
}
