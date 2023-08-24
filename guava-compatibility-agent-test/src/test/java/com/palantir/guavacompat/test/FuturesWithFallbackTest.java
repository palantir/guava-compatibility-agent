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

import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

class FuturesWithFallbackTest {

    @Test
    void addFallback() throws ExecutionException, InterruptedException {
        ListenableFuture<String> future = Futures.immediateFailedFuture(new RuntimeException());
        ListenableFuture<String> fallback = Futures.withFallback(future, new FutureFallback<String>() {
            @Override
            public ListenableFuture<String> create(Throwable _throwable) throws Exception {
                return Futures.immediateFuture("fallback");
            }
        });
        assertThat(fallback.get()).isEqualTo("fallback");
    }

    @Test
    void addFallbackWithExecutor() throws ExecutionException, InterruptedException {
        ListenableFuture<String> future = Futures.immediateFailedFuture(new RuntimeException());
        ListenableFuture<String> fallback = Futures.withFallback(
                future,
                new FutureFallback<String>() {
                    @Override
                    public ListenableFuture<String> create(Throwable _throwable) throws Exception {
                        return Futures.immediateFuture("fallback");
                    }
                },
                MoreExecutors.sameThreadExecutor());
        assertThat(fallback.get()).isEqualTo("fallback");
    }
}
