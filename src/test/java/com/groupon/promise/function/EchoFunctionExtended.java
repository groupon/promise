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

/**
 * This is designed to test the equality check for the EchoFunction.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public class EchoFunctionExtended<T> extends EchoFunction<T> {
    private String someValue;

    public EchoFunctionExtended(String someValue) {
        this.someValue = someValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o || (o != null && EchoFunctionExtended.class.equals(o.getClass()))) {
            return true;
        }

        EchoFunctionExtended<?> that = (EchoFunctionExtended<?>) o;

        return someValue.equals(that.someValue);
    }

    @Override
    @SuppressWarnings("magicnumber")
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + someValue.hashCode();
        return result;
    }
}
