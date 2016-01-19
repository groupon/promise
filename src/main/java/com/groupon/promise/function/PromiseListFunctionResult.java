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

import java.util.Collection;

import com.groupon.promise.AsyncPromiseListFunction;
import com.groupon.promise.ComparablePromiseFunction;
import com.groupon.promise.DefaultPromiseFuture;
import com.groupon.promise.PromiseFuture;
import com.groupon.promise.SyncPromiseListFunction;

/**
 * Wrapper for getting the future result from a synchronous promise function.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public class PromiseListFunctionResult<T, O> implements AsyncPromiseListFunction<T, O>, ComparablePromiseFunction {
    protected SyncPromiseListFunction<T, ? extends O> promiseFunction;

    public PromiseListFunctionResult(SyncPromiseListFunction<T, ? extends O> promiseFunction) {
        this.promiseFunction = promiseFunction;
    }

    @Override
    @SuppressWarnings("unchecked")
    public PromiseFuture<Collection<O>> handle(T data) {
        PromiseFuture<Collection<O>> future = new DefaultPromiseFuture<>();
        try {
            future.setResult((Collection<O>) promiseFunction.handle(data));
        } catch (Throwable t) {
            future.setFailure(t);
        }
        return future;
    }

    @Override
    public boolean equivalent(Object o) {
        return this == o || (o != null && o instanceof ComparablePromiseFunction &&
                ((ComparablePromiseFunction) o).equivalent(promiseFunction));
    }
}
