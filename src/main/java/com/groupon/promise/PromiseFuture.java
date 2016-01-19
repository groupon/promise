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
 * A PromiseFuture contract.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public interface PromiseFuture<T> {
    /**
     * The result if successful.
     *
     * @return T The result
     */
    T result();

    /**
     * An exception if failed.
     *
     * @return Throwable The exception
     */
    Throwable cause();

    /**
     * Returns true if the future is completed without a failure.
     *
     * @return boolean Whether the future succeeded
     */
    boolean succeeded();

    /**
     * Returns true if a failure was set.
     *
     * @return boolean Whether the future failed
     */
    boolean failed();

    /**
     * Whether the future is complete.
     *
     * @return boolean Whether the future is complete
     */
    boolean complete();

    /**
     * Add a handler to be called on complete.
     *
     * @param aHandler The handler to be called on completion
     * @return PromiseFuture The current future
     */
    PromiseFuture<T> setHandler(PromiseHandler<PromiseFuture<T>> aHandler);

    /**
     * Set the result and execute the handler.
     *
     * @param result The result on success
     * @return PromiseFuture The current future
     */
    PromiseFuture<T> setResult(T result);

    /**
     * Set the failure and execute any handlers.
     *
     * @param throwable The cause on failure
     * @return PromiseFuture The current future
     */
    PromiseFuture<T> setFailure(Throwable throwable);
}
