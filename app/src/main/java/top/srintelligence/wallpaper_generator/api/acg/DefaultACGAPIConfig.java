package top.srintelligence.wallpaper_generator.api.acg;

public enum DefaultACGAPIConfig {
    loliAPI("https://www.loliapi.com/acg/pc/?id=23&type=json"),
    mirlKoiAPI("https://iw233.cn/api.php");

    public final String url;

    DefaultACGAPIConfig(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
