package other;

import java.util.HashMap;
import java.util.Map;

public class LatecyFunctions {
    static private Map<String, Integer> allPastes;
    static public String handleMessage(String message) {
        if (allPastes == null) {
            allPastes = new HashMap<>();
        }



        if (allPastes.size() > 1000) {
            allPastes.clear();
            /*
            for (Map.Entry<String, Integer> e: allPastes.entrySet()
                 ) {
                if (e.getValue() < 3) {
                    allPastes.remove(e);
                }
            }

             */
        }

        int count = 0;
        String checkedOnCopies = getSubStringIfContains(message);
        if (checkedOnCopies != null) {
            message = checkedOnCopies;
        }

        if (allPastes.containsKey(message)) {
            count = allPastes.get(message);
        }
        allPastes.put(message, ++count);

        if (count > 5) {
            allPastes.put(message, 0);
            return message;
        } else {
            return null;
        }
    }

    public static String getSubStringIfContains(String string) {
        if (string.length() < 2) {
            return null;
        }
        String specSymbols = "!$()*+.<>?[\\]^{|}";
        StringBuilder substr = new StringBuilder();
        for (int i = 0; i < string.length() / 2; i++) {
            Character chr = string.charAt(i);
            if (specSymbols.contains(chr.toString())) {
                substr.append("\\");
                substr.append(chr);
            } else {
                substr.append(chr);
            }
            String clearedFromSubstrings
                    = string.replaceAll(substr.toString(), "");

            if (clearedFromSubstrings.length() == 0 || clearedFromSubstrings.matches("\\s+")) {
                String returnedString = substr.toString().replaceAll("\\\\?", "");
                return returnedString;
            }
        }

        return null;
    }
}
