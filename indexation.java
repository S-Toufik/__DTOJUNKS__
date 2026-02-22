cache.names=users,products,orders
field.names=id,name,email

import java.util.List;

public record MyConfig(
        List<String> cacheNames,
        List<String> fieldNames
) {}

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ConfigLoader {

    public static MyConfig loadConfig() throws IOException {
        Properties props = new Properties();

        // Load properties from classpath
        try (InputStream input = ConfigLoader.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) throw new IOException("application.properties not found");
            props.load(input);
        }

        // Convert comma-separated strings to List<String> safely
        List<String> cacheNames = csvToList(props.getProperty("cache.names"));
        List<String> fieldNames = csvToList(props.getProperty("field.names"));

        return new MyConfig(cacheNames, fieldNames);
    }

    // Utility method to handle null and trimming
    private static List<String> csvToList(String value) {
        if (value == null || value.isBlank()) return List.of();
        return Arrays.stream(value.split(","))
                     .map(String::trim)
                     .toList();
    }

    public static void main(String[] args) throws IOException {
        MyConfig config = loadConfig();
        System.out.println("Cache Names: " + config.cacheNames());
        System.out.println("Field Names: " + config.fieldNames());
    }
}