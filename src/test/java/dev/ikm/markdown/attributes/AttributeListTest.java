/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
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
package dev.ikm.markdown.attributes;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttributeListTest {

    @Test
    void emptyBodyReturnsEmpty() {
        assertSame(AttributeList.EMPTY, AttributeList.parse(""));
        assertSame(AttributeList.EMPTY, AttributeList.parse("   "));
    }

    @Test
    void nullBodyReturnsNull() {
        assertNull(AttributeList.parse(null));
    }

    @Test
    void singleClass() {
        AttributeList a = AttributeList.parse(".warning");
        assertNotNull(a);
        assertEquals(Set.of("warning"), a.classes());
        assertTrue(a.id().isEmpty());
        assertTrue(a.attributes().isEmpty());
    }

    @Test
    void multipleClasses() {
        AttributeList a = AttributeList.parse(".warning .normative");
        assertNotNull(a);
        assertEquals(Set.of("warning", "normative"), a.classes());
    }

    @Test
    void idOnly() {
        AttributeList a = AttributeList.parse("#intro");
        assertNotNull(a);
        assertEquals("intro", a.id().orElse(null));
        assertTrue(a.classes().isEmpty());
    }

    @Test
    void classAndId() {
        AttributeList a = AttributeList.parse(".warning #intro");
        assertNotNull(a);
        assertEquals(Set.of("warning"), a.classes());
        assertEquals("intro", a.id().orElse(null));
    }

    @Test
    void classIdInverseOrder() {
        AttributeList a = AttributeList.parse("#anchor .note");
        assertNotNull(a);
        assertEquals(Set.of("note"), a.classes());
        assertEquals("anchor", a.id().orElse(null));
    }

    @Test
    void unquotedKeyValue() {
        AttributeList a = AttributeList.parse("key=value");
        assertNotNull(a);
        assertEquals("value", a.attributes().get("key"));
    }

    @Test
    void quotedKeyValue() {
        AttributeList a = AttributeList.parse("title=\"two words\"");
        assertNotNull(a);
        assertEquals("two words", a.attributes().get("title"));
    }

    @Test
    void mixedTokens() {
        AttributeList a = AttributeList.parse(".warning #intro lang=en title=\"Mixed bag\"");
        assertNotNull(a);
        assertEquals(Set.of("warning"), a.classes());
        assertEquals("intro", a.id().orElse(null));
        assertEquals("en", a.attributes().get("lang"));
        assertEquals("Mixed bag", a.attributes().get("title"));
    }

    @Test
    void barewordIsTreatedAsClass() {
        AttributeList a = AttributeList.parse("warning");
        assertNotNull(a);
        assertEquals(Set.of("warning"), a.classes());
    }

    @Test
    void roundTripToString() {
        AttributeList a = AttributeList.parse(".x .y #z key=v");
        AttributeList b = AttributeList.parse(a.toString().substring(1, a.toString().length() - 1));
        assertEquals(a, b);
    }

    @Test
    void rejectsNestedBraces() {
        assertNull(AttributeList.parse(".warning {.nested}"));
    }
}
