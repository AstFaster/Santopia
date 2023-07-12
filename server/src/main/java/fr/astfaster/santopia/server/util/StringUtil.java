package fr.astfaster.santopia.server.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

    public static List<String> splitInLines(String input, int maxLineLength) {
        final List<String> lines = new ArrayList<>();
        final String[] words = input.split(" ");

        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxLineLength) {
                lines.add("§7" + currentLine);

                currentLine = new StringBuilder();
            }

            currentLine.append(word).append(" ");
        }

        lines.add("§7" + currentLine);

        return lines;
    }


}
