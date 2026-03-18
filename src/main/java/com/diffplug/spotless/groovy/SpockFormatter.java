/*
 * Copyright 2024 DiffPlug
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
package com.diffplug.spotless.groovy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Pure algorithm implementation for Spock block formatting.
 *
 * <p>This class contains zero external dependencies and can be compiled and
 * run standalone. {@link SpockStep} wraps this logic as a Spotless
 * {@code FormatterStep}.
 *
 * <p>See {@link SpockStep} for full Javadoc.
 */
public final class SpockFormatter {

	private static final int DEFAULT_INDENT_SIZE = 4;

	private static final Pattern SPOCK_METHOD_PATTERN = Pattern.compile(
			"^([ \\t]*)def\\s+(['\"]).*\\2\\s*\\(.*\\).*\\{\\s*$");

	private static final Pattern BLOCK_LABEL_PATTERN = Pattern.compile(
			"^([ \\t]*)(given|when|then|and|where|expect|cleanup|setup)\\s*:(.*)$");

	private SpockFormatter() {}

	/** Formats using the default 4-space indent. */
	public static String format(String rawUnix) {
		return format(rawUnix, DEFAULT_INDENT_SIZE);
	}

	/** Formats using the specified indent size. */
	public static String format(String rawUnix, int indentSize) {
		if (indentSize < 1) throw new IllegalArgumentException("indentSize must be >= 1, was: " + indentSize);

		String[] lines = rawUnix.split("\n", -1);
		List<String> result = new ArrayList<>(lines.length);

		int i = 0;
		while (i < lines.length) {
			String line = lines[i];
			Matcher methodMatcher = SPOCK_METHOD_PATTERN.matcher(line);

			if (methodMatcher.matches()) {
				String defIndent = methodMatcher.group(1);
				result.add(line);
				i++;

				List<String> bodyLines = new ArrayList<>();
				String closingBrace = null;
				int depth = 1;

				while (i < lines.length && depth > 0) {
					String bodyLine = lines[i];
					int depthChange = countBraceDepthChange(bodyLine);

					if (depth + depthChange <= 0) {
						closingBrace = bodyLine;
						i++;
						break;
					}

					depth += depthChange;
					bodyLines.add(bodyLine);
					i++;
				}

				String labelIndent = defIndent + spaces(indentSize);
				result.addAll(reformatSpockBody(bodyLines, labelIndent, indentSize));

				if (closingBrace != null) {
					result.add(closingBrace);
				}

			} else {
				result.add(line);
				i++;
			}
		}

		return String.join("\n", result);
	}

	private static List<String> reformatSpockBody(
			List<String> bodyLines,
			String labelIndent,
			int indentSize) {

		List<BodySection> sections = new ArrayList<>();
		BodySection current = new BodySection(null);

		for (String line : bodyLines) {
			Matcher labelMatcher = BLOCK_LABEL_PATTERN.matcher(line);
			if (labelMatcher.matches() && labelMatcher.group(1).equals(labelIndent)) {
				if (current.labelLine != null || !current.contentLines.isEmpty()) {
					sections.add(current);
				}
				current = new BodySection(line);
			} else {
				current.contentLines.add(line);
			}
		}
		if (current.labelLine != null || !current.contentLines.isEmpty()) {
			sections.add(current);
		}

		int targetContentIndent = labelIndent.length() + indentSize;
		List<String> result = new ArrayList<>();

		for (BodySection section : sections) {
			if (section.labelLine != null) {
				result.add(section.labelLine);
			}
			result.addAll(reindentContent(section.contentLines, targetContentIndent));
		}

		return result;
	}

	static List<String> reindentContent(List<String> contentLines, int targetMinIndent) {
		if (contentLines.isEmpty()) return contentLines;

		int minIndent = Integer.MAX_VALUE;
		for (String line : contentLines) {
			if (!line.trim().isEmpty()) {
				minIndent = Math.min(minIndent, countLeadingSpaces(line));
			}
		}

		if (minIndent == Integer.MAX_VALUE) return contentLines;

		int delta = targetMinIndent - minIndent;
		if (delta == 0) return contentLines;

		List<String> result = new ArrayList<>(contentLines.size());
		for (String line : contentLines) {
			if (line.trim().isEmpty()) {
				result.add(line);
			} else {
				int currentIndent = countLeadingSpaces(line);
				int newIndent = Math.max(0, currentIndent + delta);
				result.add(spaces(newIndent) + line.substring(currentIndent));
			}
		}
		return result;
	}

	static int countBraceDepthChange(String line) {
		int depth = 0;
		boolean inSingleQuote = false;
		boolean inDoubleQuote = false;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			char prev = i > 0 ? line.charAt(i - 1) : 0;
			char next = i + 1 < line.length() ? line.charAt(i + 1) : 0;

			if (!inSingleQuote && !inDoubleQuote && c == '/' && next == '/') break;

			if (c == '\'' && prev != '\\' && !inDoubleQuote) {
				inSingleQuote = !inSingleQuote;
			} else if (c == '"' && prev != '\\' && !inSingleQuote) {
				inDoubleQuote = !inDoubleQuote;
			} else if (!inSingleQuote && !inDoubleQuote) {
				if (c == '{') depth++;
				else if (c == '}') depth--;
			}
		}
		return depth;
	}

	static int countLeadingSpaces(String line) {
		int count = 0;
		for (char c : line.toCharArray()) {
			if (c == ' ') count++;
			else break;
		}
		return count;
	}

	private static String spaces(int count) {
		if (count <= 0) return "";
		char[] buf = new char[count];
		Arrays.fill(buf, ' ');
		return new String(buf);
	}

	private static class BodySection {
		final String labelLine;
		final List<String> contentLines = new ArrayList<>();

		BodySection(String labelLine) {
			this.labelLine = labelLine;
		}
	}
}
