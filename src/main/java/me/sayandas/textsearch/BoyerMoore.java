package me.sayandas.textsearch;

import lombok.ToString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
public class BoyerMoore implements TextSearch {

    private String pattern;
    private Map<Character, Integer> badCharMap;

    public BoyerMoore(String pattern){
        this.pattern = pattern;
        this.preprocessPattern();
    }

     private void preprocessPattern() {
        this.badCharMap = new HashMap<Character, Integer>();
        for (int i = 0; i < pattern.length(); i++) {
            badCharMap.put(pattern.charAt(i), i);
        }
    }

    @Override
    public List<Integer> searchQuery(String text, String query) {
        List<Integer> hitIndices = new ArrayList<>();
         System.out.println("text = " + text);
         System.out.println("pattern = " + pattern);
        // System.out.println("badCharMap = " + badCharMap);
        int n = text.length();
        int m = pattern.length();
        int shift = 0;

        while (shift <= n - m) {
            int j = m - 1;
//             System.out.println("shift = " + shift + " j = " + j);
//             System.out.println("Comparing characters: Pattern - " + pattern.charAt(j)
//                    + " and Text - " + text.charAt(shift + j));
//             Compare the pattern from the end
            while (j >= 0 && pattern.charAt(j) == text.charAt(shift + j)) {
//                System.out.println("Match Found for Char: " + pattern.charAt(j));
                j--;
            }

            // If the pattern matches
            if (j < 0) {
                hitIndices.add(shift);
//                System.out.println("Pattern found at index: " + shift);
                shift += (shift + m < n) ? m - badCharMap.getOrDefault(text.charAt(shift + m), -1) : 1;
            } else {
                // Shift the pattern to align with the last occurrence of the mismatched character
                shift += Math.max(1, j - badCharMap.getOrDefault(text.charAt(shift + j), -1));
            }
        }
//        System.out.println("hitIndices = " + hitIndices);
        return hitIndices;
    }
}
