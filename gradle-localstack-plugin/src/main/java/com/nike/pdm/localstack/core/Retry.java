/**
 * Copyright 2019-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.core;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Wrapper function that retries, with exponential backoff, in the event of an exception.
 */
public final class Retry {
    public static final int DEFAULT_MAX_RETRIES = 10;

    private Retry() {
        // Noop
    }

    /**
     * Executes the wrapped function with default maximum number of retries in the event of an exception.
     *
     * @param fn wrapped function to execute
     * @param <T> return type of the wrapped function
     * @return result of the wrapped function
     */
    public static <T> T execute(RetryableFunction<T> fn) {
        return execute(fn, DEFAULT_MAX_RETRIES, new HashSet<>());
    }

    /**
     * Executes the wrapped function with the specified maximum number of retries in the event of an exception.
     *
     * @param fn wrapped function to execute
     * @param maxRetries maximum number of times to retry the wrapped function in the event of an exception
     * @param <T> return type of the wrapped function
     * @return result of the wrapped function
     */
    public static <T> T execute(RetryableFunction<T> fn, int maxRetries) {
        return execute(fn, maxRetries, new HashSet<>());
    }

    /**
     * Executes the wrapped function, terminating immediately if one of the expected errors is encountered.
     *
     * @param fn wrapped function to execute
     * @param expectedErrors a collection of errors that when encountered will cause the function to fail immediately
     *                       without exhausting the maximum number of retries
     * @param <T> return type of the wrapped function
     * @return result of the wrapped function
     */
    public static <T> T execute(RetryableFunction<T> fn, Collection<Class<? extends Throwable>> expectedErrors) {
        return execute(fn, DEFAULT_MAX_RETRIES, expectedErrors);
    }

    /**
     * Executes the wrapped function, terminating immediately if one of the expected errors is encountered, otherwise
     * the function will be retried up to the specified maximum number of retries.
     *
     * @param fn wrapped function to execute
     * @param maxRetries maximum number of times to retry the wrapped function in the event of an exception not
     *                   specified in the expectedErrors
     * @param expectedErrors a collection of errors that when encountered will cause the function to fail immediately
     *                       without exhausting the maximum number of retries
     * @param <T> return type of the wrapped function
     * @return result of the wrapped function
     */
    public static <T> T execute(RetryableFunction<T> fn, int maxRetries, Collection<Class<? extends Throwable>> expectedErrors) {
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                return fn.execute();
            } catch (Throwable t) {
                handleException(attempt, t, expectedErrors);
            }
        }

        throw new RuntimeException("Maximum retry attempts reached");
    }

    private static void handleException(int attempt, Throwable t, Collection<Class<? extends Throwable>> expectedErrors) {
        ConsoleLogger.log("Error: %s", t.getMessage());

        if (expectedErrors.contains(t.getClass()) || (t.getCause() != null && expectedErrors.contains(t.getCause().getClass()))) {
            throw new RuntimeException(t);
        }

        doWait(attempt);
        ConsoleLogger.log("Retrying...");
    }

    private static void doWait(int attempt) {
        try {
            Thread.sleep((long) (1_000 * Math.exp(attempt)) + ThreadLocalRandom.current().nextLong(-1_000, 1_000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Function that adds retries with exponential backoff.
     *
     * @param <T> return type of the wrapped function
     */
    @FunctionalInterface
    public interface RetryableFunction<T> {

        /**
         * Executes the wrapped function.
         *
         * @return result of the wrapped function
         * @throws Exception exception
         */
        T execute() throws Exception;
    }
}
