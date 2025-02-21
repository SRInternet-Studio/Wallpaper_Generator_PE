package top.srintelligence.wallpaper_generator.api.acg;

public enum MirlKoiAPISort {
    TOP("top"),
    SILVERHAIR("yin"),
    CATGIRL("cat"),
    STARRYSKY("xing");

    public final String SORT;

    MirlKoiAPISort(String sort) {
        this.SORT = sort;
    }

    public String getSort() {
        return SORT;
    }
}
