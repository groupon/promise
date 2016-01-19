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
package com.groupon.promise.exception;

import static org.junit.Assert.assertSame;

import java.net.MalformedURLException;

import org.junit.Test;

/**
 * Test the exception utils.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.2
 */
public class ExceptionUtilsTest {
    @Test
    public void testGetMostSignificantCause() throws Exception {
        Exception cause = new Exception();
        Throwable suppressed1 = new UnsupportedOperationException();
        Throwable suppressed2 = new MalformedURLException();
        cause.addSuppressed(suppressed1);
        cause.addSuppressed(suppressed2);

        // Single suppressed exception lookup
        assertSame(suppressed1, ExceptionUtils.getMostSignificantCause(cause, UnsupportedOperationException.class));
        assertSame(suppressed2, ExceptionUtils.getMostSignificantCause(cause, MalformedURLException.class));
        // Multiple suppressed exceptions lookup
        assertSame(suppressed1, ExceptionUtils.getMostSignificantCause(cause, UnsupportedOperationException.class, MalformedURLException.class));
        // Non-specific suppressed exception lookup
        assertSame(suppressed1, ExceptionUtils.getMostSignificantCause(cause));
        // Unknown suppressed exception lookup
        assertSame(suppressed1, ExceptionUtils.getMostSignificantCause(cause, IllegalArgumentException.class));
        // No suppressed exceptions in the stack
        Exception noSuppressedException = new Exception();
        assertSame(noSuppressedException, ExceptionUtils.getMostSignificantCause(noSuppressedException));
    }
}
