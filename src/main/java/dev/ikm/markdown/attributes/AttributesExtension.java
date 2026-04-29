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

import org.commonmark.Extension;
import org.commonmark.parser.Parser;

/**
 * Pandoc-style {@code {...}} attribute extension for commonmark-java.
 *
 * <p>Aligned with AsciiDoc's role/ID/options syntax — the same role
 * vocabulary ({@code .warning}, {@code .normative}, {@code .deprecated},
 * {@code .example}, …) and the same stylesheet entries can drive both
 * Markdown and AsciiDoc renderers in IKE.
 *
 * <p>Mapping summary:
 * <pre>
 *   AsciiDoc                      Markdown
 *   --------                      --------
 *   [.warning]                    {.warning}
 *   [.warning#intro]              {.warning #intro}
 *   [.role]#highlighted text#     *highlighted text*{.role}
 *   [#anchor.note]                {#anchor .note}
 * </pre>
 *
 * <p>Wire it on a {@code Parser.Builder}:
 * <pre>{@code
 * Parser parser = Parser.builder()
 *     .extensions(List.of(AttributesExtension.create()))
 *     .build();
 * }</pre>
 *
 * <p>See {@link AttributedBlock} and {@link AttributedInline} for the AST
 * node types that the renderer should handle.
 */
public final class AttributesExtension implements Parser.ParserExtension {

    private AttributesExtension() {}

    /** Build a fresh instance of the extension; safe to share across parsers. */
    public static Extension create() {
        return new AttributesExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new AttributeBlockParserFactory());
        parserBuilder.postProcessor(new AttributePostProcessor());
    }
}
