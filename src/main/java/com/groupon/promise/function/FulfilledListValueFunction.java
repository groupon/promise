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

import com.groupon.promise.ComparablePromiseFunction;
import com.groupon.promise.PromiseList;
import com.groupon.promise.SyncPromiseListFunction;
import com.groupon.promise.exception.IllegalPromiseException;

/**
 * A FulfilledListValueFunction that takes the value from the fulfilled promise list and translates it as an output
 *
 * @author Gil Markham (gil at groupon dot com)
 * @since 0.10
 */
public class FulfilledListValueFunction<T, O> implements SyncPromiseListFunction<T, O>, ComparablePromiseFunction {

    private PromiseList<O> promise;

    public FulfilledListValueFunction(PromiseList<O> promise) {
        this.promise = promise;
    }

    @Override
    public Collection<O> handle(T t) throws Throwable {
        if (promise == null) {
            throw new IllegalPromiseException();
        } else if (promise.fulfilled()) {
            return promise.value();
        } else if (promise.rejected()) {
            throw promise.reason();
        } else {
            throw new IllegalStateException("Trying to use the promise value before the promise state is fulfilled");
        }
    }

    @Override
    public boolean equivalent(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof FulfilledListValueFunction)) {
            return false;
        }

        FulfilledListValueFunction<?, ?> that = (FulfilledListValueFunction<?, ?>) o;

        return promise != null ? promise.equals(that.promise) : that.promise == null;
    }
}
