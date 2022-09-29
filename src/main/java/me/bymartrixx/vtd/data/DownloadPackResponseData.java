package me.bymartrixx.vtd.data;

public class DownloadPackResponseData {
    private final String link;

    public DownloadPackResponseData(String link) {
        this.link = link;
    }

    public String getLink() {
        return this.link;
    }

    public String getFileName() {
        return this.link.substring(this.link.lastIndexOf("/") + 1);
    }
}
