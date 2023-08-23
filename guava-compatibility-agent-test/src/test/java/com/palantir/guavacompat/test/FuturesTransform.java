/*
 * (c) Copyright 2023 Palantir Technologies Inc. All rights reserved.
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

package com.palantir.guavacompat.test;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Function;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class FuturesTransform {

    @Test
    void transformNoExecutorArg() throws ExecutionException, InterruptedException {
        ListenableFuture<String> future = Futures.immediateFuture("Hello");
        ListenableFuture<String> transformed = Futures.transform(future, new Function<String, String>() {
            @Override
            public String apply(String input) {
                return Thread.currentThread().getName() + "-" + input;
            }
        });
        assertThat(transformed.get()).isEqualTo(Thread.currentThread().getName() + "-Hello");
    }

    @Test
    void transformWithExecutorArg() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setNameFormat("futures-test-%d").build());
        try {
            ListenableFuture<String> future = Futures.immediateFuture("Hello");
            ListenableFuture<String> transformed = Futures.transform(
                    future,
                    new Function<String, String>() {
                        @Override
                        public String apply(String input) {
                            return Thread.currentThread().getName() + "-" + input;
                        }
                    },
                    executor);
            assertThat(transformed.get()).matches("futures-test-\\d+-Hello");
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(3, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void transformAsyncNoExecutorArg() throws ExecutionException, InterruptedException {
        ListenableFuture<String> future = Futures.immediateFuture("Hello");
        ListenableFuture<String> transformed = Futures.transform(future, new AsyncFunction<String, String>() {
            @Override
            public ListenableFuture<String> apply(String input) {
                return Futures.immediateFuture(Thread.currentThread().getName() + "-" + input);
            }
        });
        assertThat(transformed.get()).isEqualTo(Thread.currentThread().getName() + "-Hello");
    }

    @Test
    void transformAsyncWithExecutorArg() throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setNameFormat("futures-test-%d").build());
        try {
            ListenableFuture<String> future = Futures.immediateFuture("Hello");
            ListenableFuture<String> transformed = Futures.transform(
                    future,
                    new AsyncFunction<String, String>() {
                        @Override
                        public ListenableFuture<String> apply(String input) {
                            return Futures.immediateFuture(
                                    Thread.currentThread().getName() + "-" + input);
                        }
                    },
                    executor);
            assertThat(transformed.get()).matches("futures-test-\\d+-Hello");
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(3, TimeUnit.SECONDS)).isTrue();
        }
    }
}
