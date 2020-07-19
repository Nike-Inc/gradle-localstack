/**
 * Copyright 2020-present, Nike, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the Apache-2.0 license found in
 * the LICENSE file in the root directory of this source tree.
 */
package com.nike.pdm.localstack.core;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class RetryTest {

    @Test
    public void shouldReturnValue() {
        String val = Retry.execute(() -> "This is a test value");

        assertEquals("This is a test value", val);
    }

    @Test
    public void shouldNotRetryOnSuccess() {
        final AtomicInteger attempts = new AtomicInteger();

        Retry.execute(() -> {
            attempts.getAndIncrement();
            return null;
        });

        assertEquals(1, attempts.get());
    }

    @Test
    public void shouldRetryOnFailure() {
        final AtomicInteger attempts = new AtomicInteger();

        Retry.execute(() -> {
            attempts.getAndIncrement();

            if (attempts.get() == 1) {
                throw new RuntimeException();
            } else {
                return null;
            }
        });

        assertEquals(2, attempts.get());
    }

    @Test
    public void shouldTerminateOnExpectedException() {
        final AtomicInteger attempts = new AtomicInteger();
        final List<Class<? extends Throwable>> expectedExceptions = Collections.singletonList(IllegalArgumentException.class);

        try {
            Retry.execute(() -> {
                attempts.getAndIncrement();

                if (attempts.get() == 1) {
                    throw new IllegalArgumentException();
                } else {
                    return null;
                }
            }, expectedExceptions);
        } catch (Throwable ignored) {
            // Noop
        }

        assertEquals(1, attempts.get());
    }

    @Test
    public void shouldNotRetryMoreThanMaxRetries() {
        final AtomicInteger attempts = new AtomicInteger();

        try {
            Retry.execute(() -> {
                attempts.getAndIncrement();

                throw new IllegalArgumentException();
            }, 1);
        } catch (Throwable ignored) {
            // Noop
        }

        assertEquals(1, attempts.get());
    }
}
