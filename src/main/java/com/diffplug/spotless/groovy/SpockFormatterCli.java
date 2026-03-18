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

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Command-line interface for the Spock block formatter.
 *
 * <pre>
 * Usage:
 *   # Check mode (exit 1 if any file would change)
 *   java -jar spock-formatter.jar --check  src/test/groovy/&#42;&#42;/&#42;Spec.groovy
 *
 *   # Format in-place
 *   java -jar spock-formatter.jar          src/test/groovy/&#42;&#42;/&#42;Spec.groovy
 *
 *   # Custom indent size
 *   java -jar spock-formatter.jar --indent 2  src/test/groovy/MySpec.groovy
 *
 *   # Print formatted output to stdout (single file only)
 *   java -jar spock-formatter.jar --stdout  src/test/groovy/MySpec.groovy
 * </pre>
 */
public final class SpockFormatterCli {

	private SpockFormatterCli() {}

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			printUsage(System.err);
			System.exit(1);
		}

		boolean checkMode = false;
		boolean stdoutMode = false;
		int indentSize = 4;
		List<Path> files = new ArrayList<>();

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
				case "--check":
					checkMode = true;
					break;
				case "--stdout":
					stdoutMode = true;
					break;
				case "--indent":
					if (i + 1 >= args.length) {
						System.err.println("--indent requires a value");
						System.exit(1);
					}
					indentSize = Integer.parseInt(args[++i]);
					break;
				case "--help":
				case "-h":
					printUsage(System.out);
					System.exit(0);
					break;
				default:
					// Treat as a file path
					files.add(Paths.get(args[i]));
			}
		}

		if (files.isEmpty()) {
			System.err.println("No files specified.");
			printUsage(System.err);
			System.exit(1);
		}

		if (stdoutMode && files.size() > 1) {
			System.err.println("--stdout only works with a single file.");
			System.exit(1);
		}

		int changed = 0;

		for (Path file : files) {
			if (!Files.exists(file)) {
				System.err.println("File not found: " + file);
				System.exit(1);
			}

			byte[] original = Files.readAllBytes(file);
			String content = new String(original, StandardCharsets.UTF_8);

			// Normalise to Unix line endings before formatting, restore original endings after
			boolean hasCrlf = content.contains("\r\n");
			String unix = content.replace("\r\n", "\n");
			String formatted = SpockFormatter.format(unix, indentSize);
			if (hasCrlf) {
				formatted = formatted.replace("\n", "\r\n");
			}

			if (stdoutMode) {
				System.out.print(formatted);
				return;
			}

			if (!formatted.equals(content)) {
				changed++;
				if (checkMode) {
					System.out.println("  WOULD CHANGE: " + file);
				} else {
					Files.write(file, formatted.getBytes(StandardCharsets.UTF_8));
					System.out.println("  FORMATTED:    " + file);
				}
			} else {
				System.out.println("  OK:           " + file);
			}
		}

		if (checkMode && changed > 0) {
			System.out.println("\n" + changed + " file(s) would be reformatted. Run without --check to fix.");
			System.exit(1);
		}

		System.out.println("\nDone. " + (checkMode ? "Checked" : "Formatted") + " " + files.size()
				+ " file(s), " + changed + " changed.");
	}

	private static void printUsage(PrintStream out) {
		out.println("Usage: java -jar spock-formatter.jar [options] <file> [<file> ...]");
		out.println();
		out.println("Options:");
		out.println("  --check        Check only — exit 1 if any file would change");
		out.println("  --stdout       Print formatted output to stdout (single file)");
		out.println("  --indent <n>   Spaces per indent level (default: 4)");
		out.println("  --help / -h    Show this message");
		out.println();
		out.println("Examples:");
		out.println("  java -jar spock-formatter.jar src/test/groovy/MySpec.groovy");
		out.println("  java -jar spock-formatter.jar --check src/test/groovy/**/*Spec.groovy");
		out.println("  java -jar spock-formatter.jar --indent 2 src/test/groovy/*.groovy");
	}
}
