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

import org.commonmark.node.CustomNode;

/**
 * An inline AST node that carries an {@link AttributeList} and wraps the
 * inline span that the {@code {...}} marker decorated.
 *
 * <p>For input like {@code *highlighted text*{.role}}, the post-processor
 * consumes the {@code Text} node containing {@code {.role}}, replaces the
 * preceding {@code Emphasis} with an {@code AttributedInline} that has the
 * original emphasis as its only child, and trims the brace text from any
 * remainder.
 *
 * <p>Renderers should merge {@link #getAttributes()} into the active
 * {@link jfx.incubator.scene.control.richtext.model.StyleAttributeMap StyleAttributeMap}
 * (or the equivalent in their target) and recurse into the children.
 */
public final class AttributedInline extends CustomNode {

    private final AttributeList attributes;

    public AttributedInline(AttributeList attributes) {
        this.attributes = attributes == null ? AttributeList.EMPTY : attributes;
    }

    public AttributeList getAttributes() {
        return attributes;
    }

    @Override
    protected String toStringAttributes() {
        return "attributes=" + attributes;
    }
}
