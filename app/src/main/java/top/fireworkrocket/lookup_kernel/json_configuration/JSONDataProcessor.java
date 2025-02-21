// Java
package top.fireworkrocket.lookup_kernel.json_configuration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static top.fireworkrocket.lookup_kernel.config.DefaultConfig.HttpConnectionUA;
import static top.fireworkrocket.lookup_kernel.config.DefaultConfig.json_Buffer_Size;
import static top.fireworkrocket.lookup_kernel.config.DefaultConfig.timeOut;

public class JSONDataProcessor {
    private final Gson gson = new Gson();
    private final int bufferSize = json_Buffer_Size;

    private Class<?> structure;
    private Object parser;

    public JSONDataProcessor build() {
        return this;
    }

    public JSONDataProcessor webJsonDataProcess(String webSite) throws IOException {
        URL url = new URL(webSite);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", HttpConnectionUA);
        connection.setConnectTimeout(timeOut);
        connection.setReadTimeout(timeOut);
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new MalformedURLException("HTTP response code: " + responseCode);
        }

        StringBuilder json = new StringBuilder();
        try (InputStream inputStream = connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream), bufferSize)) {
            String line;
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        }

        JsonElement element = gson.fromJson(json.toString(), JsonElement.class);
        if (element == null || !element.isJsonObject()) {
            ExceptionHandler.handleException(new IllegalStateException("Expected JSON object but got: " + element));
        } else {
            JsonObject jsonObj = element.getAsJsonObject();
            if (parser != null) {
                parser = gson.fromJson(jsonObj, structure);
            } else {
                ExceptionHandler.handleException(new Throwable("Parser is null"));
            }
        }

        connection.disconnect();
        return this;
    }

    public JSONDataProcessor localJsonDataProcess(String json) {
        if (parser != null) {
            parser = gson.fromJson(json, structure);
        }
        return this;
    }

    public JSONDataProcessor setStructure(Class<?> structure) {
        this.structure = structure;
        return this;
    }

    public JSONDataProcessor setParser(Object parser) {
        this.parser = parser;
        return this;
    }

    public Class<?> getStructure() {
        return structure;
    }

    public Object getParser() {
        return parser;
    }
}