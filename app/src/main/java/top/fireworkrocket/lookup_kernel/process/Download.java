package top.fireworkrocket.lookup_kernel.process;

import org.apache.commons.lang3.StringUtils;
import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static top.fireworkrocket.lookup_kernel.exception.ExceptionHandler.*;


/**
 * 文件下载工具类。
 *
 * <p>支持多线程下载与断点续传，并在出现异常时自动重试以确保文件完整性。</p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 文件下载示例：
 * String fileUrl = "http://example.com/file.zip";
 * String saveDir = "/path/to/save";
 *
 * // downLoadByUrl 方法中第三个参数为 true 时返回下载后文件的绝对路径
 * String downloadedFilePath = Download.downLoadByUrl(fileUrl, saveDir, true);
 * System.out.println("下载文件路径: " + downloadedFilePath);
 * }</pre>
 */
public class Download {
    public static String filePath = "";
    public static String savePath = "";
    private static final int THREAD_COUNT = 8;
    private static final AtomicLong downloadedBytes = new AtomicLong(0);
    private static final AtomicLong startTime = new AtomicLong(0);
    private static volatile boolean isPaused = false;
    private static volatile java.util.Map<String, String> customHeaders = new java.util.concurrent.ConcurrentHashMap<>();

    /**
     * 显示下载进度。
     *
     * @param totalSize 文件总大小
     */
    private static void showProgress(int totalSize) {
        long elapsedTime = System.currentTimeMillis() - startTime.get();
        double downloadSpeed = (downloadedBytes.get() / (1024.0 * 1024.0)) / (elapsedTime / 1000.0);
        double progress = (downloadedBytes.get() / (double) totalSize) * 100;
        int progressBarLength = 50;
        int filledLength = (int) (progressBarLength * progress / 100);

        StringBuilder progressBar = new StringBuilder("|");
        for (int i = 0; i < progressBarLength; i++) {
            progressBar.append(i < filledLength ? "█" : "-");
        }
        progressBar.append("|");

        String end = downloadedBytes.get() >= totalSize ? "\n" : "";
        System.out.printf("\rDownloaded: %s  %.2f MB/s%s", progressBar, downloadSpeed, end);
    }

    /**
     * 根据 URL 下载文件。
     *
     * @param urlStr 文件 URL
     * @param savePath 保存路径
     * @param Return 是否返回文件路径
     * @return 下载的文件路径
     */
    public static String downLoadByUrl(String urlStr, String savePath, boolean Return) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        AtomicInteger completedThreads = new AtomicInteger(0);
        AtomicBoolean retry = new AtomicBoolean(false);
        try {
            handleDebug("Downloading file from: " + urlStr);
            String fileName = getFileName(urlStr);
            String redirectedUrl = getRedirectedUrl(urlStr);
            URL url = new URL(redirectedUrl);
            int totalSize = getContentLength(url);

            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
                handleDebug("Directory created: " + saveDir);
            }
            File file = new File(saveDir, fileName);

            int partSize = totalSize / THREAD_COUNT;
            startTime.set(System.currentTimeMillis());
            for (int i = 0; i < THREAD_COUNT; i++) {
                int start = i * partSize;
                int end = (i == THREAD_COUNT - 1) ? totalSize : (start + partSize - 1);
                executor.execute(new DownloadTask(url, file, start, end, completedThreads, THREAD_COUNT, retry));
            }
            executor.shutdown();
            while (!executor.isTerminated()) {
                showProgress(totalSize);
                Thread.sleep(1000);
            }
            if (retry.get() || downloadedBytes.get() != totalSize) {
                handleWarning("Retrying download with single thread due to size mismatch.");
                downloadedBytes.set(0);
                startTime.set(System.currentTimeMillis());
                new DownloadTask(url, file, 0, totalSize - 1, completedThreads, 1, retry).run();
            }
            if (downloadedBytes.get() == totalSize) {
                System.out.print("\r\u001B[32m *Download completed \u001B[0m\n");
                handleInfo("File Download Success: " + file.getAbsolutePath());
            } else {
                System.err.println("*Download failed \n");
            }

            if (Return) {
                return file.getAbsolutePath();
            }
        } catch (Exception e) {
            handleException(e);
        }
        return null;
    }

    /**
     * 获取文件的总大小。
     *
     * @param url 文件 URL
     * @return 文件总大小
     * @throws IOException 如果获取文件大小失败
     */
    private static int getContentLength(URL url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.5410.0 Safari/537.36");
        applyCustomHeaders(conn);
        int totalSize = conn.getContentLength();
        conn.disconnect();
        return totalSize;
    }

    /**
     * 下载任务类，支持多线程下载。
     */
    private static class DownloadTask implements Runnable {
        private final URL url;
        private final File file;
        private final int start;
        private final int end;
        private final AtomicInteger completedThreads;
        private final int totalThreads;
        private final AtomicBoolean retry;

        public DownloadTask(URL url, File file, int start, int end, AtomicInteger completedThreads, int totalThreads, AtomicBoolean retry) {
            this.url = url;
            this.file = file;
            this.start = start;
            this.end = end;
            this.completedThreads = completedThreads;
            this.totalThreads = totalThreads;
            this.retry = retry;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            try (RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("Range", "bytes=" + start + "-" + end);
                applyCustomHeaders(conn);
                conn.connect();
                int responseCode = conn.getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_PARTIAL && responseCode != HttpURLConnection.HTTP_OK) {
                    handleWarning("Server did not return a valid response. Response Code: " + responseCode);
                    retry.set(true);
                    conn.disconnect();
                    throw new IOException("Server did not return a valid response. Response Code: " + responseCode);
                }
                InputStream inputStream = conn.getInputStream();
                raf.seek(start);
                byte[] buffer = new byte[4 * 1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    synchronized (Download.class) {
                        while (isPaused) {
                            Download.class.wait();
                        }
                    }
                    raf.write(buffer, 0, len);
                    downloadedBytes.addAndGet(len);
                    showProgress(end - start + 1); // 更新进度条
                }
                inputStream.close();
                conn.disconnect();
            } catch (IOException | InterruptedException e) {
                retry.set(true);
                throw new RuntimeException("Download failed", e);
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                completedThreads.incrementAndGet();
            }
        }

        public AtomicInteger getCompletedThreads() {
            return completedThreads;
        }

        public int getTotalThreads() {
            return totalThreads;
        }
    }

    /**
     * 获取文件名。
     *
     * @param srcRealPath 文件路径
     * @return 文件名
     */
    private static String getFileName(String srcRealPath) {
        String fileName = StringUtils.substringAfterLast(srcRealPath, "/");
        int queryIndex = fileName.indexOf("?");
        if (queryIndex != -1) {
            fileName = fileName.substring(0, queryIndex);
        }
        return fileName;
    }

    /**
     * 获取重定向后的 URL。
     *
     * @param urlStr 原始 URL
     * @return 重定向后的 URL
     */
    private static String getRedirectedUrl(String urlStr) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(urlStr).openConnection();
            connection.setInstanceFollowRedirects(false);
            String redirectedUrl = connection.getHeaderField("Location");
            connection.disconnect();
            if (redirectedUrl != null && !redirectedUrl.isEmpty()) {
                if (!redirectedUrl.startsWith("http://") && !redirectedUrl.startsWith("https://")) {
                    redirectedUrl = "http://" + redirectedUrl;
                }
                return redirectedUrl;
            } else {
                if (!urlStr.startsWith("http://") && !urlStr.startsWith("https://")) {
                    urlStr = "http://" + urlStr;
                }
                return urlStr;
            }
        } catch (IOException e) {
            handleException("Failed to get redirected URL", e);
        }
        return null;
    }

    public static String downLoadByUrlParallel(String urlStr, String savePath, boolean Return) {
        String fileName = getFileName(urlStr);
        String tmpFile = savePath + File.separator + fileName + ".tmp";
        String finalFile = savePath + File.separator + fileName;
        File file = null;

        try {
            handleDebug("Single-thread download from: " + urlStr);
            String redirectedUrl = getRedirectedUrl(urlStr);
            URL url = new URL(redirectedUrl);
            int totalSize = getContentLength(url);

            file = new File(tmpFile);
            File parentDir = file.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
                handleDebug("Directory created: " + parentDir);
            }

            downloadedBytes.set(0);
            startTime.set(System.currentTimeMillis());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            applyCustomHeaders(conn);

            try (RandomAccessFile raf = new RandomAccessFile(file, "rw");
                 InputStream inputStream = conn.getInputStream()) {

                byte[] buffer = new byte[4 * 1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    raf.write(buffer, 0, len);
                    downloadedBytes.addAndGet(len);
                    showProgress(totalSize);
                }
            }

            if (downloadedBytes.get() == totalSize) {
                // 重命名文件
                File target = new File(finalFile);
                if (target.exists()) {
                    target.delete();
                }
                if (file.renameTo(target)) {
                    handleInfo("Download completed: " + finalFile);
                    return Return ? finalFile : null;
                } else {
                    // 如果重命名失败，尝试复制文件
                    try (FileInputStream fis = new FileInputStream(file);
                         FileOutputStream fos = new FileOutputStream(target)) {
                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                        handleInfo("File copied: " + finalFile);
                        return Return ? finalFile : null;
                    }
                }
            } else {
                handleWarning("Download incomplete: " + downloadedBytes.get() + " / " + totalSize);
            }

        } catch (Exception e) {
            handleException(e);
        } finally {
            if (file != null && file.exists() && !file.getAbsolutePath().equals(finalFile)) {
                file.delete();
            }
        }
        return null;
    }

    /**
     * 暂停下载。
     */
    public static synchronized void pauseDownload() {
        isPaused = true;
    }

    /**
     * 恢复下载。
     */
    public static synchronized void resumeDownload() {
        isPaused = false;
        Download.class.notifyAll();
    }

    /**
     * 设置自定义请求头。
     *
     * @param headers 请求头键值对
     */
    public static void setCustomHeaders(java.util.Map<String, String> headers) {
        if (headers != null) {
            customHeaders.clear();
            customHeaders.putAll(headers);
        }
    }

    /**
     * 清除所有自定义请求头。
     */
    public static void clearCustomHeaders() {
        customHeaders.clear();
    }

    /**
     * 应用自定义请求头到连接。
     *
     * @param connection HTTP连接对象
     */
    private static void applyCustomHeaders(HttpURLConnection connection) {
        // 设置默认User-Agent
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.5410.0 Safari/537.36");

        // 应用自定义请求头
        for (Map.Entry<String, String> entry : customHeaders.entrySet()) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }
}

//    public static String downLoadByUrlParallel(String urlStr, String savePath, boolean Return) {
//        String fileName = getFileName(urlStr);
//        String multiThreadFile = savePath + File.separator + fileName + ".multi.tmp";
//        String singleThreadFile = savePath + File.separator + fileName + ".single.tmp";
//        String finalFile = savePath + File.separator + fileName;
//
//        ExecutorService executor = null;
//        try {
//            executor = Executors.newFixedThreadPool(2);
//            CompletableFuture<String> multiFuture = CompletableFuture.supplyAsync(() -> {
//                try {
//                    return downLoadByUrlMulti(urlStr, multiThreadFile, true);
//                } catch (Exception e) {
//                    return null;
//                }
//            }, executor);
//
//            CompletableFuture<String> singleFuture = CompletableFuture.supplyAsync(() -> {
//                try {
//                    return downLoadByUrlSingle(urlStr, singleThreadFile, true);
//                } catch (Exception e) {
//                    return null;
//                }
//            }, executor);
//
//            CompletableFuture<String> result = CompletableFuture.anyOf(multiFuture, singleFuture)
//                    .thenApply(path -> {
//                        if (path == null) return null;
//
//                        String resultPath = (String) path;
//                        File tmpFile = new File(resultPath);
//                        File targetFile = new File(finalFile);
//
//                        // 如果是多线程下载成功，取消单线程下载
//                        if (resultPath.equals(multiThreadFile)) {
//                            singleFuture.cancel(true);
//                            new File(singleThreadFile).delete();
//                        } else {
//                            multiFuture.cancel(true);
//                            new File(multiThreadFile).delete();
//                        }
//
//                        if (targetFile.exists()) targetFile.delete();
//                        if (tmpFile.renameTo(targetFile)) {
//                            return finalFile;
//                        }
//                        return null;
//                    });
//
//            String finalPath = result.get(30, java.util.concurrent.TimeUnit.SECONDS);
//            return Return ? finalPath : null;
//
//        } catch (Exception e) {
//            handleException("Parallel download failed", e);
//            return null;
//        } finally {
//            if (executor != null) {
//                executor.shutdownNow();
//            }
//            // 清理临时文件
//            new File(multiThreadFile).delete();
//            new File(singleThreadFile).delete();
//        }
//    }
//
//    /**
//     * 多线程下载方法。复制原有代码，但去掉自动重试单线程的逻辑，并在开始前重置计数变量。
//     */
//    private static String downLoadByUrlMulti(String urlStr, String filePath, boolean Return) {
//        downloadedBytes.set(0);
//        startTime.set(System.currentTimeMillis());
//        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
//        AtomicInteger completedThreads = new AtomicInteger(0);
//        AtomicBoolean retry = new AtomicBoolean(false);
//
//        try {
//            handleDebug("Multi-thread download from: " + urlStr);
//            String redirectedUrl = getRedirectedUrl(urlStr);
//            URL url = new URL(redirectedUrl);
//            int totalSize = getContentLength(url);
//
//            File file = new File(filePath);
//            File parentDir = file.getParentFile();
//            if (!parentDir.exists()) {
//                parentDir.mkdirs();
//                handleDebug("Directory created: " + parentDir);
//            }
//
//            int partSize = totalSize / THREAD_COUNT;
//            for (int i = 0; i < THREAD_COUNT; i++) {
//                int start = i * partSize;
//                int end = (i == THREAD_COUNT - 1) ? totalSize : (start + partSize - 1);
//                executor.execute(new DownloadTask(url, file, start, end, completedThreads, THREAD_COUNT, retry));
//            }
//            executor.shutdown();
//            while (!executor.isTerminated() && !Thread.currentThread().isInterrupted()) {
//                showProgress(totalSize);
//                Thread.sleep(1000);
//            }
//            if (downloadedBytes.get() == totalSize) {
//                handleInfo("Multi-thread File Download Success: " + file.getAbsolutePath());
//                if (Return) {
//                    return file.getAbsolutePath();
//                }
//            } else {
//                throw new RuntimeException("Multi-thread download failed, downloaded bytes: " + downloadedBytes.get());
//            }
//        } catch (Exception e) {
//            handleException(e);
//        }
//        return null;
//    }
//
//    /**
//     * 单线程下载方法。直接调用单线程任务，前提是重置计数变量以确保独立运行。
//     */
//    private static String downLoadByUrlSingle(String urlStr, String filePath, boolean Return) {
//        downloadedBytes.set(0);
//        startTime.set(System.currentTimeMillis());
//
//        try {
//            handleDebug("Single-thread download from: " + urlStr);
//            String redirectedUrl = getRedirectedUrl(urlStr);
//            URL url = new URL(redirectedUrl);
//            int totalSize = getContentLength(url);
//
//            File file = new File(filePath);
//            File parentDir = file.getParentFile();
//            if (!parentDir.exists()) {
//                parentDir.mkdirs();
//                handleDebug("Directory created: " + parentDir);
//            }
//
//            AtomicInteger completedThreads = new AtomicInteger(0);
//            AtomicBoolean retry = new AtomicBoolean(false);
//            new DownloadTask(url, file, 0, totalSize - 1, completedThreads, 1, retry).run();
//
//            if (downloadedBytes.get() == totalSize) {
//                handleInfo("Single-thread File Download Success: " + file.getAbsolutePath());
//                if (Return) {
//                    return file.getAbsolutePath();
//                }
//            } else {
//                ExceptionHandler.handleWarning("Single-thread download failed, downloaded bytes: " + downloadedBytes.get());
//            }
//        } catch (Exception e) {
//            handleException(e);
//            ExceptionHandler.handleWarning("Single-thread download failed" + e);
//        }
//        return null;
//    }