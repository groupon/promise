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

import java.util.Arrays;
import java.util.List;

/**
 * Utility for extracting specific exceptions.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @author Alessandro Rossi (alessandro.rossi at groupon dot com)
 * @since 0.2
 */
public final class ExceptionUtils {
    private ExceptionUtils() {
        // Constructor to prevent creation of an instance.
    }

    /**
     * Fetches an expected Exception for the stack of suppressed exceptions of a Throwable.
     * <br>
     * Particularly useful when dealing with Promises, this method inspects the stack of suppressed exceptions and looks
     * for a cause assignable to one of the provided types.
     * <br>
     * If <pre>throwable</pre> doesn't hold any suppressed exception, <pre>throwable</pre> itself will be returned.
     * If expectedCauses is not passed, the first exception in the suppressed stack will be returned.
     * In any other case, the first exception in the suppressed stack matching any of the provided expected types will
     * be returned.
     *
     * @param throwable          The trapped exception
     * @param expectedCauses The array of expected exception classes
     * @return The expected Exception for the stack of suppressed exceptions of a Throwable.
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static Throwable getMostSignificantCause(Throwable throwable, Class<? extends Throwable>... expectedCauses) {
        Throwable mostSignificantCause;

        Throwable[] suppressedExceptions = throwable.getSuppressed();
        if (suppressedExceptions != null && suppressedExceptions.length > 0) {
            // Initialize with the first in the list.
            mostSignificantCause = suppressedExceptions[0];

            if (expectedCauses != null && expectedCauses.length > 0) {
                List<Class<? extends Throwable>> expectedCausesList = Arrays.asList(expectedCauses);
                for (Throwable suppressedException : suppressedExceptions) {
                    if (expectedCausesList.contains(suppressedException.getClass())) {
                        // Found a more specific cause so update most significant cause and break.
                        mostSignificantCause = suppressedException;
                        break;
                    }
                }
            }
        } else {
            mostSignificantCause = throwable;
        }

        return mostSignificantCause;
    }
}
