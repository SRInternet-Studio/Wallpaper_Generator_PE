package top.fireworkrocket.lookup_kernel.process.net.util;

import top.fireworkrocket.lookup_kernel.config.DefaultConfig;
import top.fireworkrocket.lookup_kernel.exception.ExceptionHandler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class NetworkUtil {

    private static final String[] TEST_URLS = {
            "http://www.bing.com",
            "https://www.iana.org",
            "https://www.example.com"
    };

    /**
     * 检测是否有网络连接。
     *
     * @return 如果有网络连接则返回 true，否则返回 false
     */
    public static boolean isConnected() {
        if (!DefaultConfig.checkConnected) {
            return true;
        }
        for (String testUrl : TEST_URLS) {
            try {
                URL url = new URL(testUrl);
                HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
                urlConnect.setConnectTimeout(3000);
                urlConnect.connect();
                if (urlConnect.getResponseCode() == 200) {
                    return true;
                }
            } catch (IOException e) {
                ExceptionHandler.handleWarning(testUrl+"连接失败！");
            }
        }
        return false;
    }

    /**
     * 读取系统代理设置。
     *
     * @return 系统代理设置的 Properties 对象
     */
    public static Properties getSystemProxySettings() {
        Properties systemProperties = System.getProperties();
        Properties proxySettings = new Properties();
        String[] proxyTypes = {"http", "https", "ftp", "socks"};

        for (String type : proxyTypes) {
            String hostKey = type + ".proxyHost";
            String portKey = type + ".proxyPort";
            if (systemProperties.getProperty(hostKey) != null) {
                proxySettings.put(hostKey, systemProperties.getProperty(hostKey));
            }
            if (systemProperties.getProperty(portKey) != null) {
                proxySettings.put(portKey, systemProperties.getProperty(portKey));
            }
        }

        return proxySettings;
    }
}