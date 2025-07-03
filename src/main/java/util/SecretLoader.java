package util;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;

import dto.Secret;

public class SecretLoader {
    public static Secret load() {
        try (FileReader reader = new FileReader("secret.json")) {
            Gson gson = new Gson();
            return gson.fromJson(reader, Secret.class);
        } catch (IOException e) {
            throw new RuntimeException("secret.json 읽기 실패", e);
        }
    }
}
