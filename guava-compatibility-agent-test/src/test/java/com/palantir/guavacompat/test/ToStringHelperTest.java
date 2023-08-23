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

import com.google.common.base.Objects;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Objects.ToStringHelper backcompat has not been implemented yet")
class ToStringHelperTest {

    @Test
    void toStringHelper_this() {
        Objects.ToStringHelper toStringHelper = Objects.toStringHelper(this);
        String stringValue = toStringHelper.add("foo", "bar").toString();
        assertThat(stringValue).isEqualTo("ToStringHelperTest{foo=bar}");
    }

    @Test
    void toStringHelper_className() {
        Objects.ToStringHelper toStringHelper = Objects.toStringHelper("ToStringHelperTest");
        String stringValue = toStringHelper.add("foo", "bar").toString();
        assertThat(stringValue).isEqualTo("ToStringHelperTest{foo=bar}");
    }

    @Test
    void toStringHelper_clazz() {
        Objects.ToStringHelper toStringHelper = Objects.toStringHelper(ToStringHelperTest.class);
        String stringValue = toStringHelper.add("foo", "bar").toString();
        assertThat(stringValue).isEqualTo("ToStringHelperTest{foo=bar}");
    }
}
