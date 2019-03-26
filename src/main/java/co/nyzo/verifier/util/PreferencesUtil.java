package co.nyzo.verifier.util;

import co.nyzo.verifier.Verifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PreferencesUtil {

    private static final Map<String, String> preferences = new ConcurrentHashMap<>();
    static {
        loadPreferences();
    }

    private static void loadPreferences() {

        Path path = Paths.get(Verifier.dataRootDirectory.getAbsolutePath() + "/preferences");
        if (path.toFile().exists()) {
            try {
                List<String> contentsOfFile = Files.readAllLines(path);
                for (String line : contentsOfFile) {
                    try {
                        line = line.trim();
                        int indexOfHash = line.indexOf("#");
                        if (indexOfHash >= 0) {
                            line = line.substring(0, indexOfHash).trim();
                        }
                        String[] split = line.split("=");
                        if (split.length == 2) {
                            preferences.put(split[0].trim().toLowerCase(), split[1].trim().toLowerCase());
                        }
                    } catch (Exception e) {
                        System.out.println("issue loading line from preferences: " + line);
                    }
                }
            } catch (Exception e) {
                System.out.println("issue getting preferences: " + PrintUtil.printException(e));
            }
        } else {
            System.out.println("skipping preferences loading; file not present");
        }
    }

    public static String get(String key) {

        return preferences.getOrDefault(key.toLowerCase(), "");
    }

    public static int getInt(String key, int defaultValue) {

        int result = defaultValue;
        try {
            String preference = preferences.get(key.toLowerCase());
            if (preference != null && !preference.isEmpty()) {
                result = Integer.parseInt(preference);
            }
        } catch (Exception ignored) { }

        return result;
    }
}
