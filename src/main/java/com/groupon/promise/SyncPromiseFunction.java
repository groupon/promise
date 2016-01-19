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

/**
 * An interface to specify the required contract for executing a function on the result of a Promise.  Implementations
 * of this interface will be assumed complete when the handle() function returns.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.4
 */
@FunctionalInterface
public interface SyncPromiseFunction<T, O> extends PromiseFunction<T, O> {
    /**
     * Perform actions on the specified data and return with the expected result.
     *
     * @param data Data fulfilled on the Promise this function was applied to
     * @return The resulting data that will fulfill the promise associated with this function
     * @throws Exception An error can be thrown if unable to process the data
     */
    O handle(T data) throws Throwable;
}
