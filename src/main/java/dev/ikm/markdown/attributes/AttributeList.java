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

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsed contents of a Pandoc-style {@code {...}} attribute marker.
 *
 * <p>Supported tokens, separated by whitespace:
 * <ul>
 *   <li>{@code .name} — a class (also called a "role" in AsciiDoc)</li>
 *   <li>{@code #name} — the element id (last one wins)</li>
 *   <li>{@code key=value} — a free attribute with an unquoted value</li>
 *   <li>{@code key="value with spaces"} — a free attribute with a quoted value</li>
 * </ul>
 *
 * <p>Unrecognized tokens are tolerated as bare classes (e.g., {@code warning}
 * is treated like {@code .warning}) only when they are alphanumeric and don't
 * contain reserved prefixes — keeping bareword roles consistent with how
 * AsciiDoc treats positional roles in some contexts.
 *
 * <p>Instances are immutable and safe to share across threads. Equality is
 * based on the full set of fields, including class iteration order, so two
 * lists declaring the same classes in the same order are equal.
 */
public final class AttributeList {

    /** Pattern for one {@code key=value} or {@code key="quoted value"} pair. */
    private static final Pattern KV = Pattern.compile(
            "([A-Za-z_][A-Za-z0-9_-]*)=(?:\"([^\"]*)\"|([^\\s\"]*))");

    /** Token splitter — whitespace, but preserving quoted strings. */
    private static final Pattern TOKEN = Pattern.compile(
            "[.][A-Za-z_][A-Za-z0-9_-]*"          // .class
                    + "|#[A-Za-z_][A-Za-z0-9_-]*"          // #id
                    + "|[A-Za-z_][A-Za-z0-9_-]*=\"[^\"]*\""// key="quoted value"
                    + "|[A-Za-z_][A-Za-z0-9_-]*=[^\\s]+"   // key=value
                    + "|[A-Za-z_][A-Za-z0-9_-]*");         // bareword (treated as class)

    public static final AttributeList EMPTY =
            new AttributeList(Set.of(), null, Map.of());

    private final Set<String> classes;
    private final String id;
    private final Map<String, String> attrs;

    public AttributeList(Set<String> classes, String id, Map<String, String> attrs) {
        this.classes = Set.copyOf(Objects.requireNonNull(classes, "classes"));
        this.id = id;
        this.attrs = Map.copyOf(Objects.requireNonNull(attrs, "attrs"));
    }

    /** Class names, in declaration order. */
    public Set<String> classes() { return classes; }

    /** The {@code #id} value, or empty if no id was supplied. */
    public Optional<String> id() { return Optional.ofNullable(id); }

    /** Free attributes excluding classes and id. */
    public Map<String, String> attributes() { return attrs; }

    /**
     * True if no class, id, or free attribute was supplied — useful when
     * deciding whether a downstream renderer should bother to emit a wrapper.
     */
    public boolean isEmpty() {
        return classes.isEmpty() && id == null && attrs.isEmpty();
    }

    /** Class names as a flat space-separated string for HTML {@code class=}-style emission. */
    public String classesAsString() {
        return String.join(" ", classes);
    }

    /**
     * Parse the body of a {@code {...}} marker (the content between the braces,
     * with the braces already stripped). Returns {@link #EMPTY} for a blank body
     * and {@code null} if the body is malformed badly enough that the marker
     * should not be applied at all.
     *
     * <p>This method is intentionally permissive: unknown tokens are tolerated
     * as bare classes, and trailing or repeated whitespace is ignored. Any
     * token containing the literal characters {@code {}} is rejected outright,
     * since those would never appear in a real attribute marker.
     */
    public static AttributeList parse(String body) {
        if (body == null) return null;
        String trimmed = body.strip();
        if (trimmed.isEmpty()) return EMPTY;
        if (trimmed.indexOf('{') >= 0 || trimmed.indexOf('}') >= 0) return null;

        Set<String> classes = new LinkedHashSet<>();
        String id = null;
        Map<String, String> attrs = new LinkedHashMap<>();

        Matcher m = TOKEN.matcher(trimmed);
        int consumed = 0;
        while (m.find()) {
            // Bail out if the matcher skipped over non-whitespace garbage between tokens.
            String between = trimmed.substring(consumed, m.start());
            if (!between.isBlank()) return null;
            consumed = m.end();

            String token = m.group();
            if (token.startsWith(".")) {
                classes.add(token.substring(1));
            } else if (token.startsWith("#")) {
                id = token.substring(1);
            } else {
                Matcher kv = KV.matcher(token);
                if (kv.matches()) {
                    String key = kv.group(1);
                    String value = kv.group(2) != null ? kv.group(2) : kv.group(3);
                    attrs.put(key, value);
                } else {
                    // Bareword — treat as class for AsciiDoc-flavored compatibility.
                    classes.add(token);
                }
            }
        }
        if (!trimmed.substring(consumed).isBlank()) return null;
        return new AttributeList(classes, id, attrs);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AttributeList other)) return false;
        return classes.equals(other.classes)
                && Objects.equals(id, other.id)
                && attrs.equals(other.attrs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classes, id, attrs);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (String c : classes) {
            if (!first) sb.append(' ');
            sb.append('.').append(c);
            first = false;
        }
        if (id != null) {
            if (!first) sb.append(' ');
            sb.append('#').append(id);
            first = false;
        }
        for (Map.Entry<String, String> e : attrs.entrySet()) {
            if (!first) sb.append(' ');
            sb.append(e.getKey()).append('=');
            String v = e.getValue();
            if (v.indexOf(' ') >= 0 || v.indexOf('"') >= 0) {
                sb.append('"').append(v.replace("\"", "\\\"")).append('"');
            } else {
                sb.append(v);
            }
            first = false;
        }
        sb.append('}');
        return sb.toString();
    }
}
