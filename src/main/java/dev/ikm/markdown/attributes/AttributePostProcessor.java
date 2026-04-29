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

import org.commonmark.node.Code;
import org.commonmark.node.Emphasis;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;
import org.commonmark.parser.PostProcessor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Walks a parsed AST and resolves both block-level and inline-level
 * {@code {...}} attribute markers.
 *
 * <p><strong>Block-level pass.</strong> {@link AttributeBlockParserFactory}
 * has already emitted an {@link AttributedBlock} marker for each {@code {...}}
 * line. This pass takes each unbound marker, unlinks the immediately
 * following sibling block, and appends it as the marker's only child. Markers
 * with no following sibling are left as-is; the renderer is expected to skip
 * unbound markers.
 *
 * <p><strong>Inline-level pass.</strong> Walks the inline children of every
 * {@link Paragraph} and {@link Heading} (and recurses into nested containers).
 * When it finds a {@link Text} node whose contents start with {@code {...}}
 * immediately following one of the attributable inline types — emphasis,
 * strong emphasis, code, link, image — it parses the attributes, replaces the
 * preceding inline with an {@link AttributedInline} that wraps it, and trims
 * the consumed prefix from the text node (deleting the node entirely if the
 * prefix was its full content).
 */
final class AttributePostProcessor implements PostProcessor {

    /** Brace expression at the very start of a Text node. */
    private static final Pattern INLINE = Pattern.compile("^\\{([^{}]*)}");

    @Override
    public Node process(Node document) {
        bindBlockMarkers(document);
        // A single full-tree walk binds inline markers wherever they appear:
        // inside paragraphs, headings, table cells, list items, and nested
        // attributed inlines. The walker is idempotent on containers without
        // attributable inline pairs, so it's safe to apply uniformly.
        scanInlineSiblings(document);
        return document;
    }

    // -- Block pass ---------------------------------------------------------

    private static void bindBlockMarkers(Node parent) {
        Node child = parent.getFirstChild();
        while (child != null) {
            // Capture next BEFORE we mutate, since rewiring may move siblings around.
            Node next = child.getNext();
            if (child instanceof AttributedBlock marker && !marker.isBound() && next != null) {
                next.unlink();
                marker.appendChild(next);
                // Recurse into the now-wrapped block (it may itself contain markers).
                bindBlockMarkers(next);
                child = marker.getNext();
                continue;
            }
            // Not a marker (or already bound, or no next): recurse normally.
            bindBlockMarkers(child);
            child = next;
        }
    }

    // -- Inline pass --------------------------------------------------------

    private static void scanInlineSiblings(Node container) {
        Node child = container.getFirstChild();
        while (child != null) {
            Node next = child.getNext();
            if (isAttributable(child) && next instanceof Text textNext) {
                Matcher m = INLINE.matcher(textNext.getLiteral());
                if (m.find()) {
                    AttributeList attrs = AttributeList.parse(m.group(1));
                    if (attrs != null) {
                        wrap(child, attrs);
                        consumePrefix(textNext, m.end());
                    }
                }
            }
            // Also descend into the child to handle nested attributable spans
            // inside emphasis/strong/link payloads.
            scanInlineSiblings(child);
            child = next;
        }
    }

    /** True if {@code n} is an inline that can carry a trailing attribute marker. */
    private static boolean isAttributable(Node n) {
        return n instanceof Emphasis
                || n instanceof StrongEmphasis
                || n instanceof Code
                || n instanceof Link
                || n instanceof Image;
    }

    /** Replace {@code original} with an {@link AttributedInline} that wraps it. */
    private static void wrap(Node original, AttributeList attrs) {
        AttributedInline wrapper = new AttributedInline(attrs);
        original.insertAfter(wrapper);
        original.unlink();
        wrapper.appendChild(original);
    }

    /**
     * Trim the first {@code endIndex} characters off {@code text}; unlink the
     * node entirely if nothing remains.
     */
    private static void consumePrefix(Text text, int endIndex) {
        String literal = text.getLiteral();
        if (endIndex >= literal.length()) {
            text.unlink();
        } else {
            text.setLiteral(literal.substring(endIndex));
        }
    }

}
