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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A PromiseFuture which supports multiple handlers.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 1.11
 */
public class PromiseMultiFuture<T> extends DefaultPromiseFuture<T> {
    private boolean failed;
    private boolean succeeded;

    private List<PromiseHandler<PromiseFuture<T>>> handlers = new ArrayList<>();

    private T result;
    private Throwable throwable;

    /**
     * Create a PromiseMultiFuture
     */
    public PromiseMultiFuture() {
    }

    /**
     * Create a failed PromiseMultiFuture.  If no throwable is provided it will be considered a success.
     *
     * @param t The Throwable or null if succeeded
     */
    public PromiseMultiFuture(Throwable t) {
        if (t == null) {
            setResult(null);
        } else {
            setFailure(t);
        }
    }

    /**
     * Create a successful PromiseMultiFuture.
     *
     * @param result The result
     */
    public PromiseMultiFuture(T result) {
        setResult(result);
    }

    /**
     * The result if successful.
     *
     * @return T The result on success
     */
    public T result() {
        return result;
    }

    /**
     * An exception if failed.
     *
     * @return Throwable The cause on failure
     */
    public Throwable cause() {
        return throwable;
    }

    /**
     * Returns true if the future is completed without a failure.
     *
     * @return boolean Whether the future succeeded
     */
    public boolean succeeded() {
        return succeeded;
    }

    /**
     * Returns true if a failure was set.
     *
     * @return boolean Whether the future failed
     */
    public boolean failed() {
        return failed;
    }

    /**
     * Whether the future is complete.
     *
     * @return boolean Whether the future is complete
     */
    public boolean complete() {
        return failed || succeeded;
    }

    /**
     * Add a handler to be called on complete.
     *
     * @param handler A handler to be called on completion
     * @return PromiseMultiFuture The current future
     */
    public PromiseMultiFuture<T> addHandler(PromiseHandler<PromiseFuture<T>> handler) {
        if (handler != null) {
            this.handlers.add(handler);
            checkCallHandler(Collections.singletonList(handler));
        }
        return this;
    }

    /**
     * Set the result and execute any handlers.
     *
     * @param result The result on success
     * @return PromiseMultiFuture The current future
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public PromiseMultiFuture<T> setResult(T result) {
        this.result = result;
        succeeded = true;
        checkCallHandler(handlers);
        return this;
    }

    /**
     * Set the failure and execute any handlers.
     *
     * @param throwable The cause on failure
     * @return PromiseMultiFuture The current future
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public PromiseMultiFuture<T> setFailure(Throwable throwable) {
        this.throwable = throwable;
        failed = true;
        checkCallHandler(handlers);
        return this;
    }

    private void checkCallHandler(List<PromiseHandler<PromiseFuture<T>>> checkHandlers) {
        if (!checkHandlers.isEmpty() && complete()) {
            for (PromiseHandler<PromiseFuture<T>> handler : checkHandlers) {
                handler.handle(this);
            }
        }
    }
}
