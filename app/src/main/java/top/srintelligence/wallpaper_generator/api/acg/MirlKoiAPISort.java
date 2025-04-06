package top.srintelligence.wallpaper_generator.api.acg;

public enum MirlKoiAPISort {
    TOP("top"),
    SILVERHAIR("yin"),
    CATGIRL("cat"),
    STARRYSKY("xing"),
    RANDOM("random"),
    PURE("iw233"),
    PHONE("mp"),
    PC("pc");

    public final String SORT;

    MirlKoiAPISort(String sort) {
        this.SORT = sort;
    }

    public String getSort() {
        return SORT;
    }
}
