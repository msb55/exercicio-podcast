package br.ufpe.cin.if710.podcast.domain;

public class ItemFeed {
    private final String title;
    private final String link;
    private final String pubDate;
    private final String description;
    private final String downloadLink;


    public ItemFeed(String title, String link, String pubDate, String description, String downloadLink) {
        this.title = title;
        this.link = link;
        this.pubDate = pubDate;
        this.description = description;
        this.downloadLink = downloadLink;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getPubDate() {
        return pubDate;
    }

    public String getDescription() {
        return description;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    @Override
    public String toString() {
        return title;
    }

    @Override
    public boolean equals(Object obj) {
        return  this.getTitle().equals(((ItemFeed) obj).getTitle()) &&
                this.getLink().equals(((ItemFeed) obj).getLink()) &&
                this.getDescription().equals(((ItemFeed) obj).getDescription()) &&
                this.getPubDate().equals(((ItemFeed) obj).getPubDate()) &&
                this.getDownloadLink().equals(((ItemFeed) obj).getDownloadLink());
    }
}