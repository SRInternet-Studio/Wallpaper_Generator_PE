package top.fireworkrocket.lookup_kernel.process.pixiv;

import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;
import top.fireworkrocket.lookup_kernel.process.json.image.common.Return;
import top.fireworkrocket.lookup_kernel.process.net.util.URLUtil;
import top.fireworkrocket.lookup_kernel.config.api.Default_Pixiv_API_Config;

import java.io.IOException;
import java.util.*;

public class Pixiv_Request_Builder {

    String[] tag; // 标签
    String keyword; // 关键词
    String proxy; // 代理
    String aspectRatio; //长宽比
    static Set<String> blockedTags = new HashSet<>();
    static Set<String> hazardousTags = new HashSet<>();
    Boolean excludeAI = false; // 是否排除AI

    Default_Pixiv_API_Config api; // API

    int limit; // 限制数量
    int num; // 个数
    int r18; // 是否包含R18
    int width; // 宽度
    int height; // 高度

    public List<String> build() {
        if (api == null) {
            api = Default_Pixiv_API_Config.LOLICON;
        }

        Map<String, String> params = new HashMap<>();
        List<AbstractMap.SimpleEntry<String, String>> entries = Arrays.asList(
                new AbstractMap.SimpleEntry<>("tag", tag != null && tag.length > 0 ? String.join("&tag=", tag) : null),
                new AbstractMap.SimpleEntry<>("keyword", keyword != null && !keyword.isEmpty() ? keyword : null),
                new AbstractMap.SimpleEntry<>("num", limit > 0 ? String.valueOf(limit) : null),
                new AbstractMap.SimpleEntry<>("r18", r18 >= 0 ? String.valueOf(r18) : null),
                new AbstractMap.SimpleEntry<>("width", width > 0 ? String.valueOf(width) : null),
                new AbstractMap.SimpleEntry<>("height", height > 0 ? String.valueOf(height) : null),
                new AbstractMap.SimpleEntry<>("proxy", proxy != null && !proxy.isEmpty() ? proxy : null),
                new AbstractMap.SimpleEntry<>("aspectRatio", aspectRatio != null && !aspectRatio.isEmpty() ? aspectRatio : null),
                new AbstractMap.SimpleEntry<>("excludeAI", excludeAI != null ? (excludeAI ? "true" : "false") : null)
        );

        for (AbstractMap.SimpleEntry<String, String> entry : entries) {
            if (entry.getValue() != null) {
                params.put(entry.getKey(), entry.getValue());
            }
        }

        String apiURL = URLUtil.buildURLWithParams(api.getUrl(), params);
        ExceptionHandler.handleDebug("Pixiv_Request_URL：\n"+apiURL);

        HashMap<String, Object> jsonData;
        Set<String> blockedTags;
        try {
            jsonData = Return.getReturn(apiURL);
            for (Map.Entry<String, Object> entry : jsonData.entrySet()) {
                ExceptionHandler.handleDebug("------------------------------------------------");
                ExceptionHandler.handleDebug(entry.getKey() + "：" + entry.getValue()+"\n");
            }
            //blockedTags = top.fireworkrocket.lookup_kernel.process.json.image.pixiv.blocked_tags.Return.getReturn("");
            jsonData.get("data");
        } catch (IOException e){
            throw new RuntimeException(e);
        }

        // 抽取图片URL
        List<Map<String, Object>> dataList = (List<Map<String, Object>>) jsonData.get("data");
        for (Map<String, Object> data : dataList) {
            Map<String, String> urls = (Map<String, String>) data.get("urls");
            String originalUrl = urls.get("original");
            ExceptionHandler.handleDebug("Original URL: " + originalUrl);
        }

        return null;
    }

    public Pixiv_Request_Builder setTags(String[] tags) {
        this.tag = tags;
        return this;
    }

    public Pixiv_Request_Builder setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public Pixiv_Request_Builder setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Deprecated
    public Pixiv_Request_Builder setR18(int r18) {
        if (r18 > 0 && r18 <= 2)  {
            throw new IllegalArgumentException("R18 Parameter must be 0, 1 or 2!");
        }
        this.r18 = 0;
        return this;
    }

    public Pixiv_Request_Builder setNum(int num) {
        this.num = num;
        return this;
    }

    public Pixiv_Request_Builder setProxy(String proxy) {
        this.proxy = proxy;
        return this;
    }

    public Pixiv_Request_Builder excludeAI(Boolean excludeAI) {
        this.excludeAI = excludeAI;
        return this;
    }

    public Pixiv_Request_Builder setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
        return this;
    }

    public Pixiv_Request_Builder setAPI(Default_Pixiv_API_Config api) {
        this.api = api;
        return this;
    }

    public Pixiv_Request_Builder setResolution(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }
}