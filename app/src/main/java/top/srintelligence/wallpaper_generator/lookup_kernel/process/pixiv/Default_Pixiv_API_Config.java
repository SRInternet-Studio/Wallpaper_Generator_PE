package top.srintelligence.wallpaper_generator.lookup_kernel.process.pixiv;

public enum Default_Pixiv_API_Config {
    LOLICON("https://api.lolicon.app/setu/v2"),
    ANOSU("https://image.anosu.top/");

    private final String url;

    Default_Pixiv_API_Config(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}


