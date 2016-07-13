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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Test the EchoFunction.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public class EchoFunctionTest {

    @Test
    public void testHandle() throws Exception {
        final EchoFunction<Integer> echoFunction = new EchoFunction<>();
        final int arg = 2;
        final Integer future = echoFunction.handle(arg);
        assertThat(future, is(arg));
    }

    @Test
    public void testHandleNull() throws Exception {
        final EchoFunction<String> echoFunction = new EchoFunction<>();
        final String future = echoFunction.handle(null);
        assertNull(future);
    }
}
