package top.fireworkrocket.lookup_kernel.process.json.image.common;

import java.util.List;

public class CommonData {
    public String status;
    public String code;
    public String error;
    public String success;
    public List<Data> data;
    public String url;
    public List<String> pics;
    public List<String> pic;
    public String imgurl;
    public String imageUrl;
    public String acgurl;
    public String file_url;

    public static class Data {
        public String pid;
        public String page;
        public String author;
        public String title;
        public boolean r18;
        public String uploadDate;
        public List<String> tags;
        public Urls urls;
        public String url;
        public int width;
        public int height;
        public String ext;

        public static class Urls {
            public String original;
        }
    }

    public String getJsonStatus() {
        if (status != null && !status.isEmpty()) {
            return status;
        } else if (code != null && !code.isEmpty()) {
            return code;
        } else if (error != null && !error.isEmpty()) {
            return error;
        } else if (success != null && !success.isEmpty()) {
            return success;
        } else {
            return null;
        }
    }

    public String getJsonUrl() {
        if (url != null && !url.isEmpty()) {
            return url;
        } else if (pics != null && !pics.isEmpty()) {
            return String.join(" $n ", pics);
        } else if (imgurl != null && !imgurl.isEmpty()) {
            return imgurl;
        } else if (pic != null && !pic.isEmpty()) {
            return String.join(" $n ", pic);
        } else if (imageUrl != null && !imageUrl.isEmpty()) {
            return imageUrl;
        } else if (acgurl != null && !acgurl.isEmpty()) {
            return acgurl;
        } else if (file_url != null && !file_url.isEmpty()) {
            return file_url;
        } else {
            return null;
        }
    }
}
