package com.example.common.utils;

import org.jetbrains.annotations.NotNull;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtility {
	private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
	private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
	private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

	private TextUtility() {
	}

	public static String removeSpecialCharacters(String str) {
		if (str == null) return null;
		return str.replaceAll("[^A-Za-z0-9]", "");
	}

	public static boolean containsSpecialCharacter(String str) {
		if (str == null) return false;
		Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(str);
		return m.find();
	}

	public static String slugify(@NotNull String input) {
		// Replace whitespace with dashes
		String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");

		// Normalize the string to ensure consistent encoding (UTF-8 safe)
		String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);

		// Remove all non-alphanumeric characters except dashes, allowing all UTF-8 letters and numbers
		String slug = normalized.replaceAll("[^\\p{L}\\p{N}\\-]", "");

		// Remove dashes from the edges
		slug = EDGESDHASHES.matcher(slug).replaceAll("");

		return slug.toLowerCase();
	}


	public static boolean isValidDomain(String domain) {
		return Pattern
				.compile("^(?!://)([a-zA-Z0-9-_]+\\.)*[a-zA-Z0-9][a-zA-Z0-9-_]+\\.[a-zA-Z]{2,11}?$")
				.matcher(domain).matches();
	}
}
