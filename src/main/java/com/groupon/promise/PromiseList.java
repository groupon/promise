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

import java.util.Collection;
import javax.annotation.Nonnull;

/**
 * Represents the interface for a Promise list.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public interface PromiseList<T> extends Promise<Collection<T>> {

    /**
     * Add an existing PromiseList which will be executed when the current Promise is fulfilled.
     *
     * @param promise the PromiseList to be added to the chain.
     * @return A Promise that will act on the fulfilled value.
     */
    PromiseList<T> thenList(@Nonnull PromiseList<T> promise);

    /**
     * Create a new Promise which expects to operate on a list of objects.  Any promises issued on the promise returned
     * by this method will be cloned and executed for each object in the list. This method does not limit concurrency for
     * for downstream asynchronous calls, all elements will be processed in parallel assuming the downstream functions
     * are asynchronous.
     *
     * @return - A Promise that will act on all elements in the list returned by onFulfilled
     */
    Promise<T> map();

    /**
     * Create a new Promise which expects to operate on a list of objects.  Any promises issued on the promise returned
     * by this method will be cloned and executed for each object in the list.
     *
     * @param concurrencyLimit The number of concurrent elements to handle in the list.
     * @return - A Promise that will act on all elements in the list returned by onFulfilled
     * @throws IllegalArgumentException if the concurrency limit is less then 1
     */
    Promise<T> map(int concurrencyLimit);

    @Override
    PromiseList<T> optional(boolean optional);

    @Override
    PromiseList<T> nonduplicating(boolean nonduplicating);
}
