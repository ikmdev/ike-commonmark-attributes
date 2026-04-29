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

/**
 * Pandoc-style attribute extension for commonmark-java, aligned with AsciiDoc
 * role/ID/option syntax.
 *
 * <p>Block-level: a line containing only {@code {.class1 .class2 #id key=value}}
 * immediately preceding a block attaches the attributes to that block. Inline:
 * an emphasis, strong, code, or link span immediately followed by {@code {.role}}
 * attaches the attributes to that span.
 *
 * <p>The renderer is responsible for translating the attribute set into the
 * target presentation — for the JavaFX RichTextArea pipeline, classes become
 * style names on {@code RichParagraph}; in HTML, they become {@code class=}
 * attributes; in AsciiDoc round-tripping, they become {@code [.role]} spans.
 *
 * <p>Wire it on a {@code Parser.Builder}:
 * <pre>{@code
 * Parser.builder()
 *       .extensions(List.of(AttributesExtension.create()))
 *       .build();
 * }</pre>
 */
module dev.ikm.markdown.attributes {
    requires org.commonmark;

    exports dev.ikm.markdown.attributes;
}
