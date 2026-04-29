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

import org.commonmark.node.CustomBlock;

/**
 * A block AST node that carries an {@link AttributeList} and (after
 * post-processing) wraps the immediately-following sibling block.
 *
 * <p>During parsing, an {@code AttributedBlock} marker has no children — it
 * represents the consumed {@code {...}} line. The {@link AttributePostProcessor}
 * walks the AST after parsing and binds each marker to the next sibling block
 * by unlinking that sibling and appending it as the marker's only child.
 *
 * <p>Renderers should look at {@link #getAttributes()} for the
 * classes/id/attributes and recurse into {@link #getFirstChild()} for the
 * wrapped block. A bound {@code AttributedBlock} always has exactly one child;
 * an unbound one (the marker survived post-processing because nothing followed)
 * has zero children and should be rendered as nothing.
 */
public final class AttributedBlock extends CustomBlock {

    private final AttributeList attributes;

    public AttributedBlock(AttributeList attributes) {
        this.attributes = attributes == null ? AttributeList.EMPTY : attributes;
    }

    public AttributeList getAttributes() {
        return attributes;
    }

    /** True once the post-processor has wrapped a real block under this marker. */
    public boolean isBound() {
        return getFirstChild() != null;
    }

    @Override
    protected String toStringAttributes() {
        return "attributes=" + attributes;
    }
}
