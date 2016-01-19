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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

import com.groupon.promise.function.FulfillPromiseFunction;
import com.groupon.promise.function.PromiseFunctionResult;
import com.groupon.promise.function.PromiseListFunctionResult;
import com.groupon.promise.function.RejectPromiseFunction;

/**
 * This fulfills the contract required by the Promise interface.
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public class PromiseImpl<T> implements Promise<T> {
    private List<PromiseHandler<T>> performOnFulfilled = new ArrayList<>();
    private List<PromiseHandler<Throwable>> performOnRejected = new ArrayList<>();
    private volatile PromiseImpl<Void> performAfter;

    private OptionalFuture<Void> resultFuture = new OptionalFuture<>();

    protected List<PromiseTuple<T, ?>> children = new ArrayList<>();

    protected ConcurrentMap<PromiseFunctionKey<?>, PromiseMultiFuture<?>> pendingFutures = new ConcurrentHashMap<>();
    protected AtomicBoolean dispatched = new AtomicBoolean(false);
    protected AtomicInteger countdown = new AtomicInteger(1);
    protected PromiseCountdownHandler countdownHandler;

    private boolean fulfilled = false;
    private boolean rejected = false;
    private boolean optional = false;
    private boolean nonduplicating = true;

    private T value;
    private Throwable reason;

    public PromiseImpl() {
        PromiseHandler<Void> countdownSuccess = result -> {
            if (performAfter != null) {
                final PromiseFuture<Void> afterFuture = performAfter.internalFulfill(null);
                afterFuture.setHandler(event -> {
                    if (afterFuture.succeeded()) {
                        resultFuture.setResult(event.result());
                    } else {
                        resultFuture.setFailure(afterFuture.cause());
                    }
                });
            } else {
                resultFuture.setResult(null);
            }
        };

        PromiseHandler<Throwable> countdownFailure = result -> {
            if (performAfter != null) {
                final PromiseFuture<Void> afterFuture;
                if (optional) {
                    afterFuture = performAfter.internalFulfill(null);
                } else {
                    afterFuture = performAfter.internalReject(result);
                }
                afterFuture.setHandler(event -> {
                    if (afterFuture.failed()) {
                        result.addSuppressed(afterFuture.cause());
                    }
                    resultFuture.setFailure(result);
                });
            } else {
                resultFuture.setFailure(result);
            }
        };

        countdownHandler = new PromiseCountdownHandler(countdown, countdownSuccess, countdownFailure);
    }

    protected PromiseImpl(
            AtomicBoolean promiseDispatched,
            ConcurrentMap<PromiseFunctionKey<?>, PromiseMultiFuture<?>> pendingFutures) {
        this(promiseDispatched, pendingFutures, false, true);
    }

    protected PromiseImpl(
            AtomicBoolean promiseDispatched,
            ConcurrentMap<PromiseFunctionKey<?>, PromiseMultiFuture<?>> pendingFutures,
            boolean optional, boolean nonduplicating) {
        this();
        this.dispatched = promiseDispatched;
        this.pendingFutures = pendingFutures;
        this.optional = optional;
        this.nonduplicating = nonduplicating;
    }

    @Override
    public boolean fulfilled() {
        return fulfilled;
    }

    @Override
    public boolean rejected() {
        return rejected;
    }

    @Override
    public boolean pending() {
        return !(fulfilled || rejected);
    }

    @Override
    public boolean optional() {
        return optional;
    }

    @Override
    public boolean nonduplicating() {
        return nonduplicating;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public Throwable reason() {
        return reason;
    }

    @Override
    public Promise<T> then(@Nonnull Promise<T> promise) {
        if (promise == null) {
            throw new IllegalArgumentException("Promise cannot be null");
        }

        return asyncThen(new PromiseFunctionResult<>(new FulfillPromiseFunction<>(promise)),
                new PromiseFunctionResult<>(new RejectPromiseFunction<>(promise)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O, V extends O> Promise<O> then(@Nonnull PromiseFunction<T, V> onFulfilled) {
        if (onFulfilled == null) {
            throw new IllegalArgumentException("onFulfilled cannot be null");
        }

        AsyncPromiseFunction<T, O> asyncOnFulfilled;
        if (onFulfilled instanceof AsyncPromiseFunction) {
            asyncOnFulfilled = (AsyncPromiseFunction<T, O>) onFulfilled;
        } else if (onFulfilled instanceof SyncPromiseFunction) {
            asyncOnFulfilled = new PromiseFunctionResult<>((SyncPromiseFunction<T, O>) onFulfilled);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + onFulfilled.getClass());
        }

        return asyncThen(asyncOnFulfilled, null);
    }

    @Override
    public <O, V extends O> Promise<O> thenSync(@Nonnull SyncPromiseFunction<T, V> onFulfilled) {
        if (onFulfilled == null) {
            throw new IllegalArgumentException("onFulfilled cannot be null");
        }

        return asyncThen(new PromiseFunctionResult<>(onFulfilled), null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O, V extends O> Promise<O> thenAsync(@Nonnull AsyncPromiseFunction<T, V> onFulfilled) {
        if (onFulfilled == null) {
            throw new IllegalArgumentException("onFulfilled cannot be null");
        }

        return asyncThen((AsyncPromiseFunction<T, O>) onFulfilled, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O, V extends O> Promise<O> then(@Nonnull PromiseFunction<T, V> onFulfilled, @Nonnull PromiseFunction<Throwable, V> onRejected) {
        if (onFulfilled == null) {
            throw new IllegalArgumentException("onFulfilled cannot be null");
        } else if (onRejected == null) {
            throw new IllegalArgumentException("onRejected cannot be null");
        }

        AsyncPromiseFunction<T, O> asyncOnFulfilled;
        if (onFulfilled instanceof AsyncPromiseFunction) {
            asyncOnFulfilled = (AsyncPromiseFunction<T, O>) onFulfilled;
        } else if (onFulfilled instanceof SyncPromiseFunction) {
            asyncOnFulfilled = new PromiseFunctionResult<>((SyncPromiseFunction<T, O>) onFulfilled);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + onFulfilled.getClass());
        }

        AsyncPromiseFunction<Throwable, O> asyncOnRejected;
        if (onRejected instanceof AsyncPromiseFunction) {
            asyncOnRejected = (AsyncPromiseFunction<Throwable, O>) onRejected;
        } else if (onRejected instanceof SyncPromiseFunction) {
            asyncOnRejected = new PromiseFunctionResult<>((SyncPromiseFunction<Throwable, O>) onRejected);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + onFulfilled.getClass());
        }

        return asyncThen(asyncOnFulfilled, asyncOnRejected);
    }

    @Override
    public <O, V extends O> Promise<O> thenSync(@Nonnull SyncPromiseFunction<T, V> onFulfilled, @Nonnull SyncPromiseFunction<Throwable, V> onRejected) {
        if (onFulfilled == null) {
            throw new IllegalArgumentException("onFulfilled cannot be null");
        } else if (onRejected == null) {
            throw new IllegalArgumentException("onRejected cannot be null");
        }

        return asyncThen(new PromiseFunctionResult<>(onFulfilled), new PromiseFunctionResult<>(onRejected));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O, V extends O> Promise<O> thenAsync(@Nonnull AsyncPromiseFunction<T, V> onFulfilled, @Nonnull AsyncPromiseFunction<Throwable, V> onRejected) {
        if (onFulfilled == null) {
            throw new IllegalArgumentException("onFulfilled cannot be null");
        } else if (onRejected == null) {
            throw new IllegalArgumentException("onRejected cannot be null");
        }

        return asyncThen((AsyncPromiseFunction<T, O>) onFulfilled, (AsyncPromiseFunction<Throwable, O>) onRejected);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O, V extends O> PromiseList<O> thenList(@Nonnull PromiseListFunction<T, V> onFulfilled) {
        if (onFulfilled == null) {
            throw new IllegalArgumentException("onFulfilled cannot be null");
        }

        AsyncPromiseListFunction<T, O> asyncOnFulfilled;
        if (onFulfilled instanceof AsyncPromiseListFunction) {
            asyncOnFulfilled = (AsyncPromiseListFunction<T, O>) onFulfilled;
        } else if (onFulfilled instanceof SyncPromiseListFunction) {
            asyncOnFulfilled = new PromiseListFunctionResult<>((SyncPromiseListFunction<T, O>) onFulfilled);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + onFulfilled.getClass());
        }

        return asyncThenList(asyncOnFulfilled, null);
    }

    @Override
    public <O, V extends O> PromiseList<O> thenListSync(@Nonnull SyncPromiseListFunction<T, V> onFulfilled) {
        if (onFulfilled == null) {
            throw new IllegalArgumentException("onFulfilled cannot be null");
        }

        return asyncThenList(new PromiseListFunctionResult<>(onFulfilled), null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O, V extends O> PromiseList<O> thenListAsync(@Nonnull AsyncPromiseListFunction<T, V> onFulfilled) {
        if (onFulfilled == null) {
            throw new IllegalArgumentException("onFulfilled cannot be null");
        }

        return asyncThenList((AsyncPromiseListFunction<T, O>) onFulfilled, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O, V extends O> PromiseList<O> thenList(@Nonnull PromiseListFunction<T, V> onFulfilled, @Nonnull PromiseListFunction<Throwable, V> onRejected) {
        if (onFulfilled == null) {
            throw new IllegalArgumentException("onFulfilled cannot be null");
        } else if (onRejected == null) {
            throw new IllegalArgumentException("onRejected cannot be null");
        }

        AsyncPromiseListFunction<T, O> asyncOnFulfilled;
        if (onFulfilled instanceof AsyncPromiseListFunction) {
            asyncOnFulfilled = (AsyncPromiseListFunction<T, O>) onFulfilled;
        } else if (onFulfilled instanceof SyncPromiseFunction) {
            asyncOnFulfilled = new PromiseListFunctionResult<>((SyncPromiseListFunction<T, O>) onFulfilled);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + onFulfilled.getClass());
        }

        AsyncPromiseListFunction<Throwable, O> asyncOnRejected;
        if (onRejected instanceof AsyncPromiseListFunction) {
            asyncOnRejected = (AsyncPromiseListFunction<Throwable, O>) onRejected;
        } else if (onRejected instanceof SyncPromiseListFunction) {
            asyncOnRejected = new PromiseListFunctionResult<>((SyncPromiseListFunction<Throwable, O>) onRejected);
        } else {
            throw new IllegalArgumentException("Unsupported type: " + onFulfilled.getClass());
        }

        return asyncThenList(asyncOnFulfilled, asyncOnRejected);
    }

    @Override
    public <O, V extends O> PromiseList<O> thenListSync(@Nonnull SyncPromiseListFunction<T, V> onFulfilled, @Nonnull SyncPromiseListFunction<Throwable, V> onRejected) {
        if (onFulfilled == null) {
            throw new IllegalArgumentException("onFulfilled cannot be null");
        } else if (onRejected == null) {
            throw new IllegalArgumentException("onRejected cannot be null");
        }

        return asyncThenList(new PromiseListFunctionResult<>(onFulfilled),
                new PromiseListFunctionResult<>(onRejected));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O, V extends O> PromiseList<O> thenListAsync(@Nonnull AsyncPromiseListFunction<T, V> onFulfilled, @Nonnull AsyncPromiseListFunction<Throwable, V> onRejected) {
        if (onFulfilled == null) {
            throw new IllegalArgumentException("onFulfilled cannot be null");
        } else if (onRejected == null) {
            throw new IllegalArgumentException("onRejected cannot be null");
        }

        return asyncThenList((AsyncPromiseListFunction<T, O>) onFulfilled,
                (AsyncPromiseListFunction<Throwable, O>) onRejected);
    }

    @Override
    @SuppressWarnings("checkstyle:hiddenfield")
    public Promise<T> optional(boolean optional) {
        this.optional = optional;
        return this;
    }

    @Override
    @SuppressWarnings("checkstyle:hiddenfield")
    public Promise<T> nonduplicating(boolean nonduplicating) {
        this.nonduplicating = nonduplicating;
        return this;
    }

    @Override
    public Promise<Void> after() {
        if (performAfter == null) {
            synchronized (this) {
                if (performAfter == null) {
                    performAfter = new PromiseImpl<>(dispatched, pendingFutures);
                }
            }
        }
        return performAfter;
    }

    @Override
    public void fulfill(T result) {
        if (dispatched.compareAndSet(false, true)) {
            internalFulfill(result);
        } else {
            throw new IllegalStateException("Promise has already been dispatched.");
        }
    }

    @Override
    public void reject(Throwable rejectedReason) {
        if (dispatched.compareAndSet(false, true)) {
            internalReject(rejectedReason);
        } else {
            throw new IllegalStateException("Promise has already been dispatched.");
        }
    }

    protected PromiseFuture<Void> internalFulfill(T result) {
        setValue(result);

        for (PromiseHandler<T> handler : performOnFulfilled) {
            handler.handle(result);
        }

        countdownHandler.handleSuccess();

        return resultFuture;
    }

    protected PromiseFuture<Void> internalReject(Throwable rejectedReason) {
        setReason(rejectedReason);

        resultFuture.setOptional(optional);

        if (performOnRejected.size() > 0) {
            for (PromiseHandler<Throwable> handler : performOnRejected) {
                handler.handle(rejectedReason);
            }
        }

        countdownHandler.handleFailure(rejectedReason);

        return resultFuture;
    }

    protected <O> void internalThen(final PromiseTuple<T, O> tuple) {
        children.add(tuple);
        countdown.incrementAndGet();

        performOnFulfilled.add(createPerformOnFulfilled(tuple));
        performOnRejected.add(createPerformOnRejected(tuple));
    }

    private void setValue(T value) {
        this.value = value;
        this.fulfilled = true;
    }

    private void setReason(Throwable reason) {
        this.reason = reason;
        this.rejected = true;
    }

    protected PromiseImpl<T> copy() {
        PromiseImpl<T> newPromise = new PromiseImpl<>(dispatched, pendingFutures, optional, nonduplicating);
        for (PromiseTuple<T, ?> child : children) {
            newPromise.internalThen(child.copy());
        }

        return newPromise;
    }

    @SuppressWarnings("unchecked")
    private <O> PromiseMultiFuture<O> getNonDuplicatingFuture(
            AsyncPromiseFunction<?, ? extends O> method,
            Object result,
            PromiseMultiFuture<O> pendingFuture) {
        PromiseMultiFuture<O> existingFuture = null;
        if (nonduplicating && method instanceof ComparablePromiseFunction) {
            PromiseFunctionKey<?> key = new PromiseFunctionKey<>((ComparablePromiseFunction) method, result);
            existingFuture = (PromiseMultiFuture<O>) pendingFutures.putIfAbsent(key, pendingFuture);
        }

        if (existingFuture == null) {
            existingFuture = pendingFuture;
        }

        return existingFuture;
    }

    private <O> PromiseHandler<PromiseFuture<O>> buildPromiseHandler(final PromiseTuple<T, O> tuple,
                                                                     Throwable rejectedReason) {
        return newResult -> {
            if (newResult.succeeded()) {
                tuple.promise().internalFulfill(newResult.result()).setHandler(
                        finished -> {
                            if (finished.succeeded()) {
                                countdownHandler.handleSuccess();
                            } else {
                                countdownHandler.handleFailure(finished.cause());
                            }
                        });
            } else {
                tuple.promise().internalReject(newResult.cause()).setHandler(
                        finished -> {
                            if (finished.succeeded()) {
                                countdownHandler.handleSuccess();
                            } else if (rejectedReason != null) {
                                countdownHandler.handleFailure(rejectedReason);
                            } else {
                                countdownHandler.handleFailure(newResult.cause());
                            }
                        });
            }
        };
    }

    private <O> Promise<O> asyncThen(AsyncPromiseFunction<T, O> onFulfilled, AsyncPromiseFunction<Throwable, O> onRejected) {
        if (dispatched.get()) {
            throw new IllegalStateException("Promise has already been dispatched.");
        }

        PromiseImpl<O> child = new PromiseImpl<>(dispatched, pendingFutures);

        internalThen(new PromiseTuple<>(child, onFulfilled, onRejected));

        return child;
    }

    private <O> PromiseList<O> asyncThenList(AsyncPromiseListFunction<T, O> onFulfilled, AsyncPromiseListFunction<Throwable, O> onRejected) {
        if (dispatched.get()) {
            throw new IllegalStateException("Promise has already been dispatched.");
        }

        PromiseListImpl<O> child = new PromiseListImpl<>(dispatched, pendingFutures);

        internalThen(new PromiseTuple<>(child, onFulfilled, onRejected));

        return child;
    }

    private <O> PromiseHandler<Throwable> createPerformOnRejected(PromiseTuple<T, O> tuple) {
        return rejectedReason -> {
            if (tuple.onRejected() != null) {
                PromiseMultiFuture<O> pendingFuture = new PromiseMultiFuture<>();
                PromiseMultiFuture<O> existingFuture = getNonDuplicatingFuture(tuple.onRejected(), rejectedReason, pendingFuture);
                boolean initialFuture = pendingFuture == existingFuture;

                existingFuture.addHandler(buildPromiseHandler(tuple, rejectedReason));

                if (initialFuture) {
                    final PromiseMultiFuture<O> newPendingFuture = existingFuture;
                    try {
                        PromiseFuture<? extends O> future = tuple.onRejected().handle(rejectedReason);
                        if (future != null) {
                            future.setHandler(event -> {
                                if (event.succeeded()) {
                                    newPendingFuture.setResult(event.result());
                                } else {
                                    newPendingFuture.setFailure(event.cause());
                                }
                            });
                        } else {
                            newPendingFuture.setResult(null);
                        }
                    } catch (Throwable throwable) {
                        newPendingFuture.setFailure(throwable);
                    }
                }
            } else {
                tuple.promise().internalReject(rejectedReason).setHandler(finished -> {
                    if (finished.succeeded()) {
                        countdownHandler.handleSuccess();
                    } else {
                        countdownHandler.handleFailure(rejectedReason);
                    }
                });
            }
        };
    }

    private <O> PromiseHandler<T> createPerformOnFulfilled(PromiseTuple<T, O> tuple) {
        return result -> {
            PromiseMultiFuture<O> pendingFuture = new PromiseMultiFuture<>();
            PromiseMultiFuture<O> existingFuture = getNonDuplicatingFuture(tuple.onFulfilled(), result, pendingFuture);
            boolean initialFuture = pendingFuture == existingFuture;

            existingFuture.addHandler(buildPromiseHandler(tuple, null));

            if (initialFuture) {
                final PromiseMultiFuture<O> newPendingFuture = existingFuture;
                try {
                    PromiseFuture<? extends O> future = tuple.onFulfilled().handle(result);
                    if (future != null) {
                        future.setHandler(event -> {
                            if (event.succeeded()) {
                                newPendingFuture.setResult(event.result());
                            } else {
                                newPendingFuture.setFailure(event.cause());
                            }
                        });
                    } else {
                        newPendingFuture.setResult(null);
                    }
                } catch (Throwable throwable) {
                    newPendingFuture.setFailure(throwable);
                }
            }
        };
    }
}
