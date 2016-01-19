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
 * An optional future which only sets a failure when optional false.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
class OptionalFuture<T> extends DefaultPromiseFuture<T> {
    private boolean optional = false;

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    @Override
    public PromiseFuture<T> setFailure(Throwable throwable) {
        if (optional) {
            return super.setResult(null);
        } else {
            return super.setFailure(throwable);
        }
    }
}
