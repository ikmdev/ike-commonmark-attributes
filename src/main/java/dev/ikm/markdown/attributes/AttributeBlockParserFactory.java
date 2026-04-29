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

import org.commonmark.node.Block;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.block.AbstractBlockParser;
import org.commonmark.parser.block.AbstractBlockParserFactory;
import org.commonmark.parser.block.BlockContinue;
import org.commonmark.parser.block.BlockStart;
import org.commonmark.parser.block.MatchedBlockParser;
import org.commonmark.parser.block.ParserState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Recognizes a single line of the form {@code {.class #id key=value}} as a
 * standalone block marker. The marker emits an empty {@link AttributedBlock}
 * AST node; {@link AttributePostProcessor} runs after parsing and binds each
 * marker to the next sibling block.
 *
 * <p>The marker line must be at the start of a block (after any list/quote
 * indent) and must not be itself indented far enough to be a code block.
 * Lines that look like markers but contain attribute syntax this parser
 * doesn't understand are not consumed — the input falls through to the
 * default parser, which will treat them as a paragraph.
 */
final class AttributeBlockParserFactory extends AbstractBlockParserFactory {

    /** Outer match: a line containing only an attribute brace expression. */
    private static final Pattern LINE = Pattern.compile("^\\s*\\{([^{}]*)}\\s*$");

    @Override
    public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
        // 4+ spaces of indent makes this an indented code block in CommonMark.
        if (state.getIndent() >= 4) {
            return BlockStart.none();
        }
        SourceLine line = state.getLine();
        CharSequence content = line.getContent();
        Matcher m = LINE.matcher(content);
        if (!m.matches()) {
            return BlockStart.none();
        }
        AttributeList attrs = AttributeList.parse(m.group(1));
        if (attrs == null) {
            // Brace-shaped content that isn't valid attribute syntax — let
            // the default parser see it as plain text.
            return BlockStart.none();
        }
        return BlockStart.of(new SingleLineParser(new AttributedBlock(attrs)))
                .atIndex(content.length());
    }

    /**
     * Single-line block parser: emits the {@link AttributedBlock} marker on
     * its first call and returns {@link BlockContinue#none()} so the engine
     * closes the block at the next line.
     */
    private static final class SingleLineParser extends AbstractBlockParser {
        private final AttributedBlock block;

        SingleLineParser(AttributedBlock block) {
            this.block = block;
        }

        @Override
        public Block getBlock() {
            return block;
        }

        @Override
        public BlockContinue tryContinue(ParserState state) {
            return BlockContinue.none();
        }
    }
}
