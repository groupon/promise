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

import java.util.Objects;
import javax.annotation.Nonnull;

/**
 * Represents a unique combination of PromiseFunction and value.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
class PromiseFunctionKey<T> {
    private ComparablePromiseFunction promiseFunction;
    private T parameter;

    PromiseFunctionKey(@Nonnull ComparablePromiseFunction promiseFunction, T parameter) {
        this.promiseFunction = promiseFunction;
        this.parameter = parameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof PromiseFunctionKey)) {
            return false;
        }

        PromiseFunctionKey<?> that = (PromiseFunctionKey<?>) o;

        return Objects.equals(parameter, that.parameter) && promiseFunction.equivalent(that.promiseFunction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(promiseFunction.getClass(), parameter);
    }
}
