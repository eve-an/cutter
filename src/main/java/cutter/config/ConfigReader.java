package cutter.config;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ConfigReader {

    private static Config config;

    public static Config getConfig() {
        if (config == null) {
            try {
                config = readConfig();
            } catch (IOException e) {
                throw new RuntimeException("Could not read config file!", e);
            }
        }

        return config;
    }

    private static Config readConfig() throws IOException {
        Gson gson = new Gson();
        Reader reader = Files.newBufferedReader(Paths.get("/home/ivan/git/Cutter/config.json"));

        Config config = gson.fromJson(reader, Config.class);
        reader.close();
        return config;
    }
}
