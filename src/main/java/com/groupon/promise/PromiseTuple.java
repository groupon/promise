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
 * A storage container for tracking the Promise created by the then/map methods and the associated functions necessary
 * to complete them.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
class PromiseTuple<T, O> {
    protected PromiseImpl<O> promise;
    protected AsyncPromiseFunction<T, O> onFulfilled;
    protected AsyncPromiseFunction<Throwable, O> onRejected;

    PromiseTuple(PromiseImpl<O> promise, AsyncPromiseFunction<T, O> onFulfilled,
                 AsyncPromiseFunction<Throwable, O> onRejected) {
        this.promise = promise;
        this.onFulfilled = onFulfilled;
        this.onRejected = onRejected;
    }

    public PromiseImpl<O> promise() {
        return promise;
    }

    public AsyncPromiseFunction<T, ? extends O> onFulfilled() {
        return onFulfilled;
    }

    public AsyncPromiseFunction<Throwable, ? extends O> onRejected() {
        return onRejected;
    }

    protected PromiseTuple<T, O> copy() {
        return new PromiseTuple<>(promise.copy(), onFulfilled, onRejected);
    }
}
