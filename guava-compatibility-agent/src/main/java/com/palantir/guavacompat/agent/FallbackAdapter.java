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

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureFallback;
import com.google.common.util.concurrent.ListenableFuture;

public final class FallbackAdapter<T> implements AsyncFunction<Throwable, T> {

    private final FutureFallback<T> delegate;

    public FallbackAdapter(FutureFallback<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public ListenableFuture<T> apply(Throwable input) throws Exception {
        return delegate.create(input);
    }
}
