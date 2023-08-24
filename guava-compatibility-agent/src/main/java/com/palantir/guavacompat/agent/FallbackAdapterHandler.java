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

package com.palantir.guavacompat.agent;

import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executor;

public final class FallbackAdapterHandler {
    public static <V> ListenableFuture<V> withFallback(
            ListenableFuture<? extends V> input, FutureFallback<? extends V> fallback) {
        return Futures.catchingAsync(
                input, Throwable.class, new FallbackAdapter<>(fallback), MoreExecutors.directExecutor());
    }

    public static <V> ListenableFuture<V> withFallback(
            ListenableFuture<? extends V> input, FutureFallback<? extends V> fallback, Executor executor) {
        return Futures.catchingAsync(input, Throwable.class, new FallbackAdapter<>(fallback), executor);
    }

    private FallbackAdapterHandler() {}
}
