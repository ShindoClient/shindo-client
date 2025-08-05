package me.miki.shindo.management.remote.news;

public class News {

    private final String title;
    private final String subTitle;
    private final String body;


    public News(String title, String subTitle, String body) {
        this.title = title;
        this.subTitle = subTitle;
        this.body = body;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getBody() {
        return body;
    }

}
