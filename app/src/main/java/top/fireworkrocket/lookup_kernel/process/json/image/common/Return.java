package top.fireworkrocket.lookup_kernel.process.json.image.common;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import top.fireworkrocket.lookup_kernel.json_configuration.JSONDataProcessor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Return {
    static JSONDataProcessor jsonDataProcessor = new JSONDataProcessor();

    public static HashMap<String, Object> getReturn(String url) throws IOException {
        CommonData commonData = (CommonData) jsonDataProcessor
                .setParser(new CommonData())
                .setStructure(CommonData.class)
                .webJsonDataProcess(url)
                .build()
                .getParser();

        // 将 CommonData 对象转换为 Map
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(commonData);
        Map<String, Object> resultMap = gson.fromJson(jsonElement, Map.class);
        return new HashMap<>(resultMap);
    }
}