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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.groupon.promise.exception.PromiseException;

/**
 * This handler tracks the number of pending promises to be executed.  When all promises have been completed it will
 * execute the success/failure conditions as determined by the value passed in.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
class PromiseCountdownHandler {
    private static final Logger LOG = LoggerFactory.getLogger(PromiseCountdownHandler.class);

    private AtomicInteger latch;
    private PromiseHandler<Void> handleSuccess;
    private PromiseHandler<Throwable> handleFailure;
    private List<Throwable> failures = new ArrayList<>();

    PromiseCountdownHandler(AtomicInteger count, PromiseHandler<Void> handleSuccess, PromiseHandler<Throwable> handleFailure) {
        latch = count;
        this.handleSuccess = handleSuccess;
        this.handleFailure = handleFailure;
    }

    public void handleSuccess() {
        int latchValue = latch.decrementAndGet();
        if (latchValue == 0) {
            if (failures.isEmpty()) {
                handleSuccess.handle(null);
            } else {
                Exception e = new PromiseException();
                for (Throwable failure : failures) {
                    addSuppressed(e, failure);
                }
                handleFailure.handle(e);
            }
        } else if (latchValue == -1) {
            // Anything below negative one would be a duplicate of this message so stop logging.
            LOG.error("executedTooManyTimes", new Exception("Exceeded countdown"));
        }
    }

    public void handleFailure(Throwable throwable) {
        int latchValue = latch.decrementAndGet();
        failures.add(throwable);

        if (latchValue == 0) {
            Exception e = new PromiseException();
            for (Throwable failure : failures) {
                addSuppressed(e, failure);
            }
            handleFailure.handle(e);
        } else if (latchValue == -1) {
            // Anything below negative one would be a duplicate of this message so stop logging.
            LOG.error("executedTooManyTimes", new Exception("Exceeded countdown"));
        }
    }

    private void addSuppressed(Exception e, Throwable failure) {
        if (!(failure instanceof PromiseException)) {
            e.addSuppressed(failure);
        } else if (failure.getSuppressed() != null) {
            for (Throwable nestedFailure : failure.getSuppressed()) {
                addSuppressed(e, nestedFailure);
            }
        }
    }
}
