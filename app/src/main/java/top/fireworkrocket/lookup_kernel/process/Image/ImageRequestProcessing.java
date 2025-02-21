package top.fireworkrocket.lookup_kernel.process.Image;

import top.fireworkrocket.lookup_kernel.config.DefaultConfig;
import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;
import top.fireworkrocket.lookup_kernel.process.json.image.common.Return;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static top.fireworkrocket.lookup_kernel.exception.ExceptionHandler.handleDebug;
import static top.fireworkrocket.lookup_kernel.exception.ExceptionHandler.handleException;

public class ImageRequestProcessing {

    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);
    private static final ForkJoinPool forkJoinPool = new ForkJoinPool(3);

    public static String[] apiList;
    public static long lastCallTime = 0;
    static Semaphore semaphore = new Semaphore(DefaultConfig.picProcessingSemaphore);

    private static final Map<String, Integer> apiFailureCount = new ConcurrentHashMap<>();
    private static final Map<String, Long> apiLastFailureTime = new ConcurrentHashMap<>();
    private static final int MAX_CALLS = 10;
    private static final long MIN_COOLDOWN = 500;
    private static final long MAX_COOLDOWN = 5000;

    private static int callCount = 0;
    private static long cooldownTime = MIN_COOLDOWN;

    @Deprecated
    public static List<String> getPic(int picNum) {
        try {
            List<Future<String>> futures = new ArrayList<>();
            Random random = new Random();
            for (int i = 0; i < picNum; i++) {
                int apiIndex = random.nextInt(apiList.length);
                futures.add(getPicUrlAsync(apiList[apiIndex]));
            }
            return getUrlsFromFutures(futures);
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    public static Future<List<String>> getPicAtNow(int picNum) {
        try {
            List<Future<List<String>>> futures = new ArrayList<>();
            Random random = new Random();
            int requestCount = 0;
            for (int i = 0; i < picNum; i++) {
                if (requestCount >= picNum) break;
                int apiIndex = random.nextInt(apiList.length);
                String api = apiList[apiIndex];
                if (shouldSkipApi(api)) continue;
                futures.add(forkJoinPool.submit(() -> getPicUrlWithSemaphore(api)));
                requestCount++;
            }
            return forkJoinPool.submit(() -> collectUrlsFromFutures(futures));
        } catch (Exception e) {
            handleException(e);
            return null;
        }
    }

    private static Future<String> getPicUrlAsync(String api) {
        return forkJoinPool.submit(() -> {
            List<String> urls = getPicUrlWithRetry(api);
            ExceptionHandler.handleDebug("获取到的URL: " + urls);
            return urls != null && !urls.isEmpty() ? urls.get(0) : null;
        });
    }

    private static List<String> collectUrlsFromFutures(List<Future<List<String>>> futures) throws Exception {
        List<String> allUrls = new ArrayList<>();
        for (Future<List<String>> future : futures) {
            List<String> urls = future.get();
            if (urls != null && !urls.isEmpty()) {
                allUrls.addAll(urls);
            }
        }
        return allUrls;
    }

    private static boolean checkCallFrequency() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCallTime < cooldownTime) {
            handleException(new Exception("调用 GetPic() 不能超过每 " + (cooldownTime / 1000) + " 秒一次"));
            return true;
        } else {
            updateCooldownTime();
            lastCallTime = currentTime;
            return false;
        }
    }

    private static List<String> getPicUrl(String api) throws Exception {
        Map<String, Object> resultMap = Return.getReturn(api);
        handleDebug("API: " + api + " API 响应: " + resultMap);
        List<String> urls = extractUrls(resultMap);
        if (!urls.isEmpty()) {
            return urls;
        }
        return null;
    }

    public static List<String> extractUrls(Map<String, Object> jsonData) {
        List<String> returnUrls = new ArrayList<>();

        if (jsonData.containsKey("pic")) {
            Object picData = jsonData.get("pic");
            if (picData instanceof List<?>) {
                List<?> picList = (List<?>) picData;
                for (Object pic : picList) {
                    if (pic instanceof String) {
                        returnUrls.add((String) pic);
                        ExceptionHandler.handleDebug("提取到URL: " + pic);
                    }
                }
            }
        }

        ExceptionHandler.handleDebug("提取的URL列表: " + returnUrls);
        return returnUrls;
    }


    public static void picProcessingShutdown() {
        shutdownExecutorService(executorService);
        shutdownExecutorService(forkJoinPool);
    }

    public static void checkApiAvailability() {
        executorService.scheduleWithFixedDelay(() -> {
            for (String api : apiList) {
                if (apiFailureCount.getOrDefault(api, 0) >= 3) {
                    try {
                        getPicUrl(api);
                        apiFailureCount.put(api, 0);
                        handleDebug("API " + api + " 可用，重置失败计数器");
                    } catch (Exception e) {
                        handleDebug("API " + api + " 仍不可用");
                    }
                }
            }
        }, 0, 10, TimeUnit.MINUTES);
    }

    public static List<String> getDisabledApis() {
        List<String> disabledApis = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (String api : apiList) {
            if (apiFailureCount.getOrDefault(api, 0) >= 3 &&
                    currentTime - apiLastFailureTime.getOrDefault(api, 0L) < 5 * 60 * 1000) {
                disabledApis.add(api);
            }
        }
        return disabledApis;
    }

    public static List<String> getUrlsFromFutures(List<Future<String>> futures) {
        List<String> urls = new ArrayList<>();
        for (Future<String> future : futures) {
            try {
                String url = future.get();
                ExceptionHandler.handleDebug("bbbbbb获取到的URL: " + url); // 添加调试日志
                if (url != null && !url.isEmpty()) {
                    urls.add(url);
                }
            } catch (Exception e) {
                handleException(e);
                ExceptionHandler.handleDebug("bbbbbb获取URL时发生异常: " + e.getMessage()); // 添加异常日志
            }
        }
        ExceptionHandler.handleDebug("bbbbbb最终URL列表: " + urls);
        return urls;
    }

    private static boolean shouldSkipApi(String api) {
        return apiFailureCount.getOrDefault(api, 0) >= 3 &&
                System.currentTimeMillis() - apiLastFailureTime.getOrDefault(api, 0L) < 5 * 60 * 1000;
    }

    private static List<String> getPicUrlWithSemaphore(String api) throws Exception {
        try {
            semaphore.acquire();
            return getPicUrl(api);
        } finally {
            semaphore.release();
        }
    }

    private static String getFirstNonNullUrl(List<Future<String>> futures) throws Exception {
        for (Future<String> future : futures) {
            String result = future.get();
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static List<String> getPicUrlWithRetry(String api) throws Exception {
        int totalRetryCount = 0;
        while (totalRetryCount <= 3) {
            try {
                return getPicUrl(api);
            } catch (Exception e) {
                handleException(e);
                totalRetryCount++;
                if (totalRetryCount > 3) {
                    throw new RuntimeException("重试 3 次后获取图片 URL 失败: " + e.getMessage(), e);
                }
            }
        }
        return new ArrayList<>();
    }

    private static void updateCooldownTime() {
        callCount++;
        if (callCount > MAX_CALLS) {
            cooldownTime = Math.min(cooldownTime * 2, MAX_COOLDOWN);
            callCount = 0;
        } else {
            cooldownTime = MIN_COOLDOWN;
        }
        handleDebug("设置 " + (cooldownTime / 1000) + " 秒冷却期...");
    }

    private static void shutdownExecutorService(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}