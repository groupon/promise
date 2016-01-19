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
import com.groupon.promise.SyncPromiseFunction;

/**
 * Helper function that outputs the value given in the constructor to a function.
 * This helps to translate and expects two parallels calls to take different arguments of the same type.
 *
 * @author Alex Campelo (acampelo at groupon dot com)
 * @since 0.1
 */
public class FixedValueFunction<T, O> implements SyncPromiseFunction<T, O>, ComparablePromiseFunction {
    private O value;

    public FixedValueFunction(O value) {
        this.value = value;
    }

    @Override
    public O handle(T arg) {
        return value;
    }

    @Override
    public boolean equivalent(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof FixedValueFunction)) {
            return false;
        }

        FixedValueFunction<?, ?> that = (FixedValueFunction<?, ?>) o;

        return value != null ? value.equals(that.value) : that.value == null;
    }
}
