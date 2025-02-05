package top.fireworkrocket.lookup_kernel.process.json.image.pixiv.blocked_tags;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;
import top.fireworkrocket.lookup_kernel.json_configuration.JSONDataProcessor;

import java.io.IOException;
import java.util.HashSet;

public class Return {
    static JSONDataProcessor jsonDataProcessor = new JSONDataProcessor();

    public static HashSet<String> getReturn(String url) throws IOException {
        BlockedTagsData blockedTagsData= (BlockedTagsData) jsonDataProcessor
                .setParser(new BlockedTagsData())
                .setStructure(BlockedTagsData.class)
                .webJsonDataProcess(url)
                .build()
                .getParser();

        // 将 CommonData 对象转换为 Map
        Gson gson = new Gson();
        JsonElement jsonElement = gson.toJsonTree(blockedTagsData);
        HashSet<String> resultMap = gson.fromJson(jsonElement, HashSet.class);
        ExceptionHandler
                .handleDebug("Pixiv_Blocked_Tags：\n"+resultMap.toString());
        return new HashSet<>(resultMap);
    }
}