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

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.atomic.AtomicBoolean;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class FuturesAddCallbackTest {

    @Test
    void addCallback() {
        ListenableFuture<String> future = Futures.immediateFuture("Hello");
        AtomicBoolean success = new AtomicBoolean();
        Futures.addCallback(future, new FutureCallback<String>() {
            @Override
            public void onSuccess(String _ignored) {
                success.set(true);
            }

            @Override
            public void onFailure(Throwable _ignored) {
                Assertions.fail("Should not be called");
            }
        });
        assertThat(success).isTrue();
    }

    @Test
    void addCallbackWithExecutor() {
        ListenableFuture<String> future = Futures.immediateFuture("Hello");
        AtomicBoolean success = new AtomicBoolean();
        Futures.addCallback(
                future,
                new FutureCallback<String>() {
                    @Override
                    public void onSuccess(String _ignored) {
                        success.set(true);
                    }

                    @Override
                    public void onFailure(Throwable _ignored) {
                        Assertions.fail("Should not be called");
                    }
                },
                MoreExecutors.sameThreadExecutor());
        assertThat(success).isTrue();
    }
}
