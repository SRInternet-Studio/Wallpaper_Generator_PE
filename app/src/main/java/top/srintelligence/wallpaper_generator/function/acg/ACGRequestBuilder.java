package top.srintelligence.wallpaper_generator.function.acg;

import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;
import top.fireworkrocket.lookup_kernel.process.ApiParamHandler;
import top.fireworkrocket.lookup_kernel.process.Image.ImageRequestProcessing;
import top.srintelligence.wallpaper_generator.api.acg.DefaultACGAPIConfig;
import top.srintelligence.wallpaper_generator.api.acg.MirlKoiAPISort;

import java.util.List;

import java.util.HashMap;
import java.util.Map;

public class ACGRequestBuilder {

    private static DefaultACGAPIConfig api;
    private static MirlKoiAPISort sort;
    private static int num;

    public static List<String> build() {
        String url = api.getUrl();
        Map<String, String> params = new HashMap<>();
        params.put("sort", sort.getSort());
        params.put("type", "json");
        params.put("num", String.valueOf(num));

        try {
            url = ApiParamHandler.addParamsWithoutObservable(params, url);
        } catch (Exception e) {
            ExceptionHandler.handleDebug(e.getMessage());
        }
        ExceptionHandler.handleDebug("Request URL: " + url);
        ImageRequestProcessing.apiList = new String[]{url};
        return ImageRequestProcessing.getPic(1);
    }

    public ACGRequestBuilder setApi(DefaultACGAPIConfig api) {
        ACGRequestBuilder.api = api;
        return this;
    }

    public ACGRequestBuilder setSort(MirlKoiAPISort sort) {
        ACGRequestBuilder.sort = sort;
        return this;
    }

    public ACGRequestBuilder setNum(int num) {
        ACGRequestBuilder.num = num;
        return this;
    }
}