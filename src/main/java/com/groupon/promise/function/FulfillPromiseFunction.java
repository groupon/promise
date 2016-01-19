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

import com.groupon.promise.ComparablePromiseFunction;
import com.groupon.promise.Promise;
import com.groupon.promise.SyncPromiseFunction;
import com.groupon.promise.exception.IllegalPromiseException;

/**
 * A FulfillPromiseFunction that takes the value and fulfills the promise.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.11
 */
public class FulfillPromiseFunction<O> implements SyncPromiseFunction<O, O>, ComparablePromiseFunction {

    private Promise<O> promise;

    public FulfillPromiseFunction(Promise<O> promise) {
        this.promise = promise;
    }

    @Override
    public O handle(O t) throws Throwable {
        if (promise == null) {
            throw new IllegalPromiseException();
        } else {
            promise.fulfill(t);
        }
        return t;
    }

    @Override
    public boolean equivalent(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof FulfillPromiseFunction)) {
            return false;
        }

        FulfillPromiseFunction<?> that = (FulfillPromiseFunction<?>) o;

        return promise != null ? promise.equals(that.promise) : that.promise == null;
    }
}
