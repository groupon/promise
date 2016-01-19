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

import javax.annotation.Nonnull;

/**
 * Interface which must be implemented by all promises.  The contract is based on the Promises/A+ specification:
 *
 * http://promises-aplus.github.io/promises-spec/
 *
 * Example:
 *
 * <pre>
 * {@code
 * Promise<Order> getOrder = new Promise<>();
 * Promise<Deal> getDeal = getOrder.then(new PromiseHandler<Order,Deal>() {
 *      public PromiseFuture<Deal> handle(Order order) {
 *          return dealClient.getDeal(order.getDealUuid());
 *      }
 * });
 *
 * Promise<Option> getOption = getDeal.map(new PromiseHandler<Deal,List<Option>>() {
 *      public PromiseFuture<List<Option>> handle(Deal deal) {
 *          return new PromiseFuture(deal.getOptions());
 *      }
 * });
 *
 * Promise<Price> getPrice = getOption.then(new PromiseHandler<Deal,Price>() {
 *      public PromiseFuture<Price> handle(Option option) {
 *          return voucherInventoryClient.getProduct(option.getInventoryProductId());
 *      }
 * });
 *
 * PromiseFuture<Void> promiseFinished = getOrder.fulfill(orderClient.getOrder(userUuid, orderId));
 * }
 * </pre>
 *
 * @author Stuart Siegrist (fsiegrist at groupon dot com)
 * @since 0.1
 */
public interface Promise<T> {

    /**
     * When the Promise is fulfilled the value has been set and cannot be changed.
     *
     * @return - Whether this promise was fulfilled
     */
    boolean fulfilled();

    /**
     * When the Promise is rejected the reason has been set and cannot be changed.
     *
     * @return - Whether this promise was rejected
     */
    boolean rejected();

    /**
     * When the Promise is pending it neither has a value or reason.
     *
     * @return - Whether this promise is still pending
     */
    boolean pending();

    /**
     * When the Promise is rejected the future will be successful when optional is set.
     *
     * @return - Whether this promise is set to be optional
     */
    boolean optional();

    /**
     * When the Promise is fulfilled it will only execute the onFulfilled and onRejected if
     * the equivalent promise function and fulfilled value have not already been processed.  Equivalence
     * is determined using the equals and hashCode on the promise functions.
     *
     * @return - Whether this promise is set always call the onFulfilled and onRejected.
     */
    boolean nonduplicating();

    /**
     * The value fulfilled by this Promise.
     *
     * @return - The fulfilled value of this promise
     */
    T value();

    /**
     * The reason for the Promise being rejected.
     *
     * @return The reason this promise was rejected
     */
    Throwable reason();

    /**
     * Add an existing Promise which will be executed when the current Promise is fulfilled.
     *
     * @param promise the Promise to be added to the chain.
     * @return A Promise that will act on the fulfilled value.
     */
    Promise<T> then(@Nonnull Promise<T> promise);

    /**
     * Create a new Promise which will be executed when the onFulfilled function is complete based on the semantics of
     * the passed in function.  For more information see {@link SyncPromiseFunction} and {@link AsyncPromiseFunction}.
     *
     * @param <O> Return type of the onFulfill function
     * @param <V> Return type of the onFulfill function which extends O
     * @param onFulfilled Prepares the value to be processed by the Promise returned.
     * @return A Promise that will act on the value returned by onFulfilled
     */
    <O, V extends O> Promise<O> then(@Nonnull PromiseFunction<T, V> onFulfilled);

    /**
     * Create a new Promise which will be executed when the onFulfilled function returns a value.
     *
     * @param <O> Return type of the onFulfill function
     * @param <V> Return type of the onFulfill function which extends O
     * @param onFulfilled Prepares the value to be processed by the Promise returned.
     * @return A Promise that will act on the value returned by onFulfilled
     */
    <O, V extends O> Promise<O> thenSync(@Nonnull SyncPromiseFunction<T, V> onFulfilled);

    /**
     * Create a new Promise which will be executed when the PromiseFuture returned by the onFulfilled is complete.
     *
     * @param <O> Return type of the onFulfill function
     * @param <V> Return type of the onFulfill function which extends O
     * @param onFulfilled Prepares the value to be processed by the Promise returned.
     * @return A Promise that will act on the value returned by onFulfilled
     */
    <O, V extends O> Promise<O> thenAsync(@Nonnull AsyncPromiseFunction<T, V> onFulfilled);

    /**
     * Create a new Promise which will be executed when the onFulfilled or onRejected functions are complete based on
     * the semantics of the passed in functions.  For more information see {@link SyncPromiseFunction} and
     * {@link AsyncPromiseFunction}.
     *
     * @param <O> Return type of the onFulfill function
     * @param <V> Return type of the onFulfill function which extends O
     * @param onFulfilled Prepares the value to be processed by the Promise returned.
     * @param onRejected Performs any logic prior to passing the reason to the Promise returned.
     * @return A Promise that will act on the value returned by onFulfilled
     */
    <O, V extends O> Promise<O> then(@Nonnull PromiseFunction<T, V> onFulfilled, @Nonnull PromiseFunction<Throwable, V> onRejected);

    /**
     * Create a new Promise which will be executed when the onFulfilled or onRejected functions return a value.
     *
     * @param <O> Return type of the onFulfill function
     * @param <V> Return type of the onFulfill function which extends O
     * @param onFulfilled Prepares the value to be processed by the Promise returned.
     * @param onRejected Performs any logic prior to passing the reason to the Promise returned.
     * @return A Promise that will act on the value returned by onFulfilled
     */
    <O, V extends O> Promise<O> thenSync(@Nonnull SyncPromiseFunction<T, V> onFulfilled, @Nonnull SyncPromiseFunction<Throwable, V> onRejected);

    /**
     * Create a new Promise which will be executed when the PromiseFuture returned by the onFulfilled or onRejected is
     * complete.
     *
     * @param <O> Return type of the onFulfill function
     * @param <V> Return type of the onFulfill function which extends O
     * @param onFulfilled Prepares the value to be processed by the Promise returned.
     * @param onRejected Performs any logic prior to passing the reason to the Promise returned.
     * @return A Promise that will act on the value returned by onFulfilled
     */
    <O, V extends O> Promise<O> thenAsync(@Nonnull AsyncPromiseFunction<T, V> onFulfilled, @Nonnull AsyncPromiseFunction<Throwable, V> onRejected);

    /**
     * Create a new PromiseList which will be executed when the onFulfilled function is complete based on the semantics
     * of the passed in function.  For more information see {@link SyncPromiseListFunction} and
     * {@link AsyncPromiseListFunction}.
     *
     * @param <O> Return type of the onFulfill function
     * @param <V> Return type of the onFulfill function which extends O
     * @param onFulfilled Prepares the value to be processed by the Promise returned.
     * @return A Promise that will act on the value returned by onFulfilled
     */
    <O, V extends O> PromiseList<O> thenList(@Nonnull PromiseListFunction<T, V> onFulfilled);

    /**
     * Create a new PromiseList which will be executed when the onFulfilled function returns a value.
     *
     * @param <O> Return type of the onFulfill function
     * @param <V> Return type of the onFulfill function which extends O
     * @param onFulfilled Prepares the value to be processed by the Promise returned.
     * @return A Promise that will act on the value returned by onFulfilled
     */
    <O, V extends O> PromiseList<O> thenListSync(@Nonnull SyncPromiseListFunction<T, V> onFulfilled);

    /**
     * Create a new PromiseList which will be executed when the PromiseFuture returned by the onFulfilled is complete.
     *
     * @param <O> Return type of the onFulfill function
     * @param <V> Return type of the onFulfill function which extends O
     * @param onFulfilled Prepares the value to be processed by the Promise returned.
     * @return A Promise that will act on the value returned by onFulfilled
     */
    <O, V extends O> PromiseList<O> thenListAsync(@Nonnull AsyncPromiseListFunction<T, V> onFulfilled);

    /**
     * Create a new PromiseList which will be executed when the onFulfilled or onRejected functions are complete based
     * on the semantics of the passed in functions.  For more information see {@link SyncPromiseListFunction} and
     * {@link AsyncPromiseListFunction}.
     *
     * @param <O> Return type of the onFulfill function
     * @param <V> Return type of the onFulfill function which extends O
     * @param onFulfilled Prepares the value to be processed by the Promise returned.
     * @param onRejected Performs any logic prior to passing the reason to the Promise returned.
     * @return A Promise that will act on the value returned by onFulfilled
     */
    <O, V extends O> PromiseList<O> thenList(@Nonnull PromiseListFunction<T, V> onFulfilled, @Nonnull PromiseListFunction<Throwable, V> onRejected);

    /**
     * Create a new PromiseList which will be executed when the onFulfilled or onRejected functions return a value.
     *
     * @param <O> Return type of the onFulfill function
     * @param <V> Return type of the onFulfill function which extends O
     * @param onFulfilled Prepares the value to be processed by the Promise returned.
     * @param onRejected Performs any logic prior to passing the reason to the Promise returned.
     * @return A Promise that will act on the value returned by onFulfilled
     */
    <O, V extends O> PromiseList<O> thenListSync(@Nonnull SyncPromiseListFunction<T, V> onFulfilled, @Nonnull SyncPromiseListFunction<Throwable, V> onRejected);

    /**
     * Create a new PromiseList which will be executed when the PromiseFuture returned by the onFulfilled or onRejected
     * is complete.
     *
     * @param <O> Return type of the onFulfill function
     * @param <V> Return type of the onFulfill function which extends O
     * @param onFulfilled Prepares the value to be processed by the Promise returned.
     * @param onRejected Performs any logic prior to passing the reason to the Promise returned.
     * @return A Promise that will act on the value returned by onFulfilled
     */
    <O, V extends O> PromiseList<O> thenListAsync(@Nonnull AsyncPromiseListFunction<T, V> onFulfilled, @Nonnull AsyncPromiseListFunction<Throwable, V> onRejected);

    /**
     * Update the Promise with the specified optional status.
     *
     * @param optional - Sets the optional state of the Promise.
     * @return - The current Promise
     */
    Promise<T> optional(boolean optional);

    /**
     * Update the Promise with the specified nonduplicating status.
     *
     * @param nonduplicating - Sets the nonduplicating state of the Promise.
     * @return - The current Promise
     */
    Promise<T> nonduplicating(boolean nonduplicating);

    /**
     * Return a Promise which will be executed when the current Promise and all of it's children are
     * fulfilled.  Multiple calls to this method will always return a reference to the same Promise.
     *
     * @return A Promise that will act on the value of the current Promise
     */
    Promise<Void> after();

    /**
     * Start processing the current Promise chain with the specified value.
     *
     * @param value The value necessary to complete this promise.
     */
    void fulfill(T value);

    /**
     * Start processing the current Promise chain with the specified reason.
     *
     * @param reason The reason for rejecting this promise.
     */
    void reject(Throwable reason);
}
