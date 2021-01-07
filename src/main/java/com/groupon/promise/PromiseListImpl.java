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

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

import com.groupon.promise.function.FulfillPromiseFunction;
import com.groupon.promise.function.PromiseFunctionResult;
import com.groupon.promise.function.RejectPromiseFunction;

/**
 * This fulfills the contract required by the Promise interface while under the covers maintaining a list of
 * Promises issued when being dispatched with a list.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @author Gil Markham (gil at groupon dot com)
 * @since 0.1
 */
public class PromiseListImpl<T> extends PromiseImpl<Collection<T>> implements PromiseList<T> {
    public PromiseListImpl() {
        super();
    }

    protected PromiseListImpl(AtomicBoolean promiseDispatched, ConcurrentMap<PromiseFunctionKey<?>, PromiseMultiFuture<?>> pendingFutures) {
        super(promiseDispatched, pendingFutures);
    }

    @Override
    public PromiseList<T> thenList(@Nonnull PromiseList<T> promise) {
        if (promise == null) {
            throw new IllegalArgumentException("Promise cannot be null");
        } else if (dispatched.get()) {
            throw new IllegalStateException("Promise has already been dispatched.");
        }

        PromiseListImpl<T> child = new PromiseListImpl<>(dispatched, pendingFutures);

        internalThen(new PromiseTuple<>(child, new PromiseFunctionResult<>(new FulfillPromiseFunction<>(promise)),
                new PromiseFunctionResult<>(new RejectPromiseFunction<>(promise))));

        return child;
    }

    @Override
    public Promise<T> map() {
        if (dispatched.get()) {
            throw new IllegalStateException("Promise has already been dispatched.");
        }

        final PromiseImpl<T> child = new PromiseImpl<>(dispatched, pendingFutures);

        AsyncPromiseFunction<Collection<T>, Void> onFulfilled = new PromiseOnFulfilledFunction<>(child);
        AsyncPromiseFunction<Throwable, Void> onReject = new PromiseOnRejectFunction(child);

        internalThen(new PromiseTuple<>(new PromiseImpl<>(), onFulfilled, onReject));

        return child;
    }

    @Override
    public Promise<T> map(int concurrencyLimit) {
        if (dispatched.get()) {
            throw new IllegalStateException("Promise has already been dispatched.");
        }

        if (concurrencyLimit <= 0) {
            throw new IllegalArgumentException("Concurrency limit must be greater than 0");
        }

        final PromiseImpl<T> child = new PromiseImpl<>(dispatched, pendingFutures);

        AsyncPromiseFunction<Collection<T>, Void> onFulfilled = new PromiseLimitedOnFulfilledFunction<>(child, concurrencyLimit);
        AsyncPromiseFunction<Throwable, Void> onReject = new PromiseOnRejectFunction(child);

        internalThen(new PromiseTuple<>(new PromiseImpl<>(), onFulfilled, onReject));

        return child;
    }

    @Override
    public PromiseList<T> optional(boolean value) {
        super.optional(value);
        return this;
    }

    @Override
    public PromiseList<T> nonduplicating(boolean value) {
        super.nonduplicating(value);
        return this;
    }

    /**
     * Non concurrency limited onfulfill function, walks over the list of elements firing off a child
     * promise for each element in the list.
     */
    private class PromiseOnFulfilledFunction<T> implements AsyncPromiseFunction<Collection<T>, Void> {
        private PromiseImpl<T> child;

        PromiseOnFulfilledFunction(PromiseImpl<T> child) {
            this.child = child;
        }

        @Override
        public PromiseFuture<Void> handle(Collection<T> data) {
            final PromiseFuture<Void> handleFuture = new DefaultPromiseFuture<>();
            if (data.size() == 0) {
                handleFuture.setResult(null);
            } else {
                final PromiseCountdownHandler countdownHandler = new PromiseCountdownHandler(new AtomicInteger(data.size()),
                        new PromiseHandler<Void>() {
                            @Override
                            public void handle(Void event) {
                                handleFuture.setResult(event);
                            }
                        },
                        new PromiseHandler<Throwable>() {
                            @Override
                            public void handle(Throwable event) {
                                handleFuture.setFailure(event);
                            }
                        }
                );
                for (T element : data) {
                    child.copy().internalFulfill(element).setHandler(new PromiseHandler<PromiseFuture<Void>>() {
                        @Override
                        public void handle(PromiseFuture<Void> event) {
                            if (event.succeeded()) {
                                countdownHandler.handleSuccess();
                            } else {
                                countdownHandler.handleFailure(event.cause());
                            }
                        }
                    });
                }
            }
            return handleFuture;
        }
    }

    /**
     * OnFulfill function that limits concurrency by using an iterator and the ConcurrencyLimitHandler to chain handlers.
     * Effectively this fires off the first 'concurrencyLimit' elements and then lets the handler handle the remaining
     * elements when an element is complete.
     */
    private class PromiseLimitedOnFulfilledFunction<T> implements AsyncPromiseFunction<Collection<T>, Void> {
        private PromiseImpl<T> child;
        private int concurrencyLimit;

        PromiseLimitedOnFulfilledFunction(PromiseImpl<T> child, int concurrencyLimit) {
            this.child = child;
            this.concurrencyLimit = concurrencyLimit;
        }

        @Override
        public PromiseFuture<Void> handle(Collection<T> data) {
            final PromiseFuture<Void> handleFuture = new DefaultPromiseFuture<>();
            if (data.size() == 0) {
                handleFuture.setResult(null);
            } else {
                // Create a concurrent linked queue for thread safety.
                final ConcurrentLinkedQueue<T> queueList = new ConcurrentLinkedQueue<>(data);

                final PromiseCountdownHandler countdownHandler = new PromiseCountdownHandler(new AtomicInteger(queueList.size()),
                        new PromiseHandler<Void>() {
                            @Override
                            public void handle(Void event) {
                                handleFuture.setResult(event);
                            }
                        },
                        new PromiseHandler<Throwable>() {
                            @Override
                            public void handle(Throwable event) {
                                handleFuture.setFailure(event);
                            }
                        }
                );

                for (int i = 0; i < concurrencyLimit; i++) {
                    T element = queueList.poll();
                    if (element != null) {
                        child.copy().internalFulfill(element).setHandler(
                                new ConcurrencyLimitHandler<>(queueList, countdownHandler, child));
                    } else {
                        // There are no more elements in the list.
                        break;
                    }
                }
            }
            return handleFuture;
        }
    }

    /**
     * onReject function that gets called if this PromiseListImpl is rejected, simply rejects a copy of the child promise.
     */
    private class PromiseOnRejectFunction implements AsyncPromiseFunction<Throwable, Void> {
        private PromiseImpl<T> child;

        PromiseOnRejectFunction(PromiseImpl<T> child) {
            this.child = child;
        }

        @Override
        public PromiseFuture<Void> handle(Throwable data) {
            final PromiseFuture<Void> handleFuture = new DefaultPromiseFuture<>();
            child.copy().internalReject(data).setHandler(new PromiseHandler<PromiseFuture<Void>>() {
                @Override
                public void handle(PromiseFuture<Void> event) {
                    if (event.succeeded()) {
                        handleFuture.setResult(event.result());
                    } else {
                        handleFuture.setFailure(event.cause());
                    }
                }
            });
            return handleFuture;
        }
    }

    /**
     * Handler that calls fulfill for the next element if one exists.
     */
    private class ConcurrencyLimitHandler<T> implements PromiseHandler<PromiseFuture<Void>> {
        private final ConcurrentLinkedQueue<T> queueList;
        private final PromiseCountdownHandler countdownHandler;
        private final PromiseImpl<T> childPromise;

        ConcurrencyLimitHandler(ConcurrentLinkedQueue<T> queueList, PromiseCountdownHandler countdownHandler,
                                       PromiseImpl<T> childPromise) {
            this.queueList = queueList;
            this.countdownHandler = countdownHandler;
            this.childPromise = childPromise;
        }

        @Override
        public void handle(PromiseFuture<Void> event) {
            T element = queueList.poll();
            if (element != null) {
                childPromise.copy().internalFulfill(element).setHandler(this);
            }

            if (event.succeeded()) {
                countdownHandler.handleSuccess();
            } else {
                countdownHandler.handleFailure(event.cause());
            }
        }
    }
}
