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
 * A PromiseFuture which supports a handler.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public class DefaultPromiseFuture<T> implements PromiseFuture<T> {
    private boolean failed;
    private boolean succeeded;

    private PromiseHandler<PromiseFuture<T>> handler = null;

    private T result;
    private Throwable throwable;

    /**
     * Create a PromiseFuture
     */
    public DefaultPromiseFuture() {
    }

    /**
     * Create a failed PromiseFuture.  If no throwable is provided it will be considered a success.
     *
     * @param t The Throwable or null if succeeded
     */
    public DefaultPromiseFuture(Throwable t) {
        if (t == null) {
            setResult(null);
        } else {
            setFailure(t);
        }
    }

    /**
     * Create a successful PromiseFuture.
     *
     * @param result The result
     */
    public DefaultPromiseFuture(T result) {
        setResult(result);
    }

    /**
     * The result if successful.
     */
    public T result() {
        return result;
    }

    /**
     * An exception if failed.
     */
    public Throwable cause() {
        return throwable;
    }

    /**
     * Returns true if the future is completed without a failure.
     */
    public boolean succeeded() {
        return succeeded;
    }

    /**
     * Returns true if a failure was set.
     */
    public boolean failed() {
        return failed;
    }

    /**
     * Whether the future is complete.
     */
    public boolean complete() {
        return failed || succeeded;
    }

    /**
     * Add a handler to be called on complete.
     */
    public PromiseFuture<T> setHandler(PromiseHandler<PromiseFuture<T>> aHandler) {
        this.handler = aHandler;
        checkCallHandler();
        return this;
    }

    /**
     * Set the result and execute the handler.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public PromiseFuture<T> setResult(T result) {
        this.result = result;
        succeeded = true;
        checkCallHandler();
        return this;
    }

    /**
     * Set the failure and execute any handlers.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public PromiseFuture<T> setFailure(Throwable throwable) {
        this.throwable = throwable;
        failed = true;
        checkCallHandler();
        return this;
    }

    private void checkCallHandler() {
        if (handler != null && complete()) {
            handler.handle(this);
        }
    }
}
