package top.srintelligence.wallpaper_generator.lookup_kernel.json_configuration;

import com.google.gson.Gson;
import top.srintelligence.wallpaper_generator.lookup_kernel.exception.ExceptionHandler;
import top.srintelligence.wallpaper_generator.lookup_kernel.json_configuration.image.JsonData;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JSON 数据处理器，用于从 URL 获取 JSON 数据并进行处理。
 */
public class JSON_Data_Processor {

    @FunctionalInterface
    public interface MyFunction<T, R> {
        R apply(T t);
    }

    @FunctionalInterface
    public interface MyBiFunction<T, U, R> {
        R apply(T t, U u);
    }

    private static final Gson GSON = new Gson();
    private static final int BUFFER_SIZE = 8192;
    private static JsonDataFactory jsonDataFactory = new DefaultJsonDataFactory();
    private static MyFunction<JsonData, Map<String, Object>> customProcessJsonData;
    private static MyBiFunction<JsonData.Data, Integer, Map<String, Object>> customPrintData;

    public static Map<String, Object> getUrl(String getUrl) {
        Map<String, Object> resultMap = new HashMap<>();
        StringBuilder response = new StringBuilder(BUFFER_SIZE);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(openConnection(getUrl).getInputStream()), BUFFER_SIZE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
                ExceptionHandler.handleDebug("JSON Line: \n"+line);
            }

            if (response.toString().trim().startsWith("{")) { // 如果返回的数据是 JSON 格式
                JsonData jsonData = jsonDataFactory.createJsonData();
                jsonData = GSON.fromJson(response.toString(), jsonData.getClass()); // 将 JSON 数据转换为 JsonData 对象
                resultMap.putAll(processJsonData(jsonData)); // 处理 JsonData 对象
                ExceptionHandler.handleDebug("Result Map: \n" + resultMap);
            }
        } catch (Exception ex) {
            ExceptionHandler.handleException("Error processing URL", ex);
        }
        return resultMap;
    }

    public static HttpURLConnection openConnection(String getUrl) throws Exception {
        URI uri = new URI(getUrl);
        URL url = uri.toURL();
        return (HttpURLConnection) url.openConnection();
    }

    private static Map<String, Object> processJsonData(JsonData jsonData) {
        if (customProcessJsonData != null) {
            return customProcessJsonData.apply(jsonData);
        }

        Map<String, Object> resultMap = new HashMap<>(); // 用于存储处理后的数据

        if (jsonData.getStatus() != null) { // 如果状态字段不为空
            resultMap.put("Status", jsonData.getStatus()); // 将状态字段存入结果
        }

        List<JsonData.Data> dataList = jsonData.getData(); // 获取数据列表（可能为空）

        if (dataList != null) {
            int count = 0; // 计数器
            for (JsonData.Data data : dataList) {
                resultMap.putAll(putData(data, count)); // 将数据存入结果
                count++;
            }
        } else {
            resultMap.put("URL", jsonData.getUrl()); // 将 URL 存入结果
        }

        return resultMap; // 返回结果
    }

    private static Map<String, Object> putData(JsonData.Data data, int count) {
        if (customPrintData != null) {
            return customPrintData.apply(data, count);
        }
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("$Data" + count, data);
        dataMap.put("URL", data.getOriginalUrl() == null ? data.getUrl() : data.getOriginalUrl());
        dataMap.put("PID", data.getPid());
        dataMap.put("Page", data.getPage());
        dataMap.put("Author", data.getAuthor());
        dataMap.put("Title", data.getTitle());
        dataMap.put("R18", data.getR18Vel());
        dataMap.put("Upload Date", data.getUploadDate());
        dataMap.put("Tags", data.getTags());
        dataMap.put("Ext", data.getExt());
        dataMap.put("Resolution", data.getRes());
        ExceptionHandler.handleDebug("Data: " + dataMap);
        return dataMap;
    }

    public static void setJsonDataFactory(JsonDataFactory factory) {
        jsonDataFactory = factory;
    }

    public static void setCustomProcessJsonData(MyFunction<JsonData, Map<String, Object>> customProcessJsonData) {
        JSON_Data_Processor.customProcessJsonData = customProcessJsonData;
    }

    public static void setCustomPrintData(MyBiFunction<JsonData.Data, Integer, Map<String, Object>> customPrintData) {
        JSON_Data_Processor.customPrintData = customPrintData;
    }
}