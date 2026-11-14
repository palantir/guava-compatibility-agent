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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.junit.jupiter.api.Test;

class EmptyIteratorTest {

    @Test
    void emptyIteratorIsExhausted() {
        Iterator<String> empty = Iterators.emptyIterator();
        assertThat(empty).isExhausted();
    }

    @Test
    void emptyIteratorIsUnmodifiable() {
        Iterator<String> empty = Iterators.emptyIterator();
        assertThatThrownBy(empty::remove).isInstanceOf(UnsupportedOperationException.class);
    }
}
