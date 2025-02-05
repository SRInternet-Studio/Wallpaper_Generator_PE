package top.fireworkrocket.lookup_kernel.config;

import java.io.File;

public class DefaultConfig {
    public static boolean debug = false; // 调试模式
    public static boolean checkConnected = true; // 检查网络连接
    public static int picProcessingSemaphore = 2; // 图片处理器线程池大小
    public static int json_Buffer_Size = 1024; // JSON缓冲区大小
    public static int timeOut = 5000; // 连接超时时间

    public static File configHome = new File(System.getenv("APPDATA") + "/LookUp/Config");
    public static String HttpConnectionUA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.6668.58 Safari/537.36";
    public static File backGroundfile = new File( "https://cn.bing.com/th?id=OHR.PointeDiable_ZH-CN0610493136_UHD.jpg&pid=hp&w=1920");
}
