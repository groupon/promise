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
 * A RejectPromiseFunction that takes the exception and rejects the promise.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.11
 */
public class RejectPromiseFunction<O> implements SyncPromiseFunction<Throwable, O>, ComparablePromiseFunction {

    private Promise<O> promise;

    public RejectPromiseFunction(Promise<O> promise) {
        this.promise = promise;
    }

    @Override
    public O handle(Throwable t) throws Throwable {
        if (promise == null) {
            throw new IllegalPromiseException();
        } else {
            promise.reject(t);
        }
        throw t;
    }

    @Override
    public boolean equivalent(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof RejectPromiseFunction)) {
            return false;
        }

        RejectPromiseFunction<?> that = (RejectPromiseFunction<?>) o;

        return promise != null ? promise.equals(that.promise) : that.promise == null;
    }
}
