package cutter.frame;

import com.google.gson.Gson;
import cutter.Video;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;

public class FrameReader {

    public static Video readJson(String jsonFile) {
        File file = new File(jsonFile);

        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(file.toPath())) {
            return gson.fromJson(reader, Video.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
