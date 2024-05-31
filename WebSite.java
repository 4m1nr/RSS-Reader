import java.io.Serializable;
import java.net.URL;

public class WebSite implements Serializable {
    private URL websiteUrl;
    private String name;
    private URL rssUrl;
    public WebSite(String url){
        try {
            this.name = RSS.extractPageTitle(url);
            this.websiteUrl = new URL(url);
            this.rssUrl = new URL(RSS.extractRssUrl(url));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public URL getRssUrl() {
        return rssUrl;
    }

    public URL getWebsiteUrl() {
        return websiteUrl;
    }

    public boolean equals(Object other){
        if (other.getClass() == WebSite.class)
            return this.getRssUrl().equals(((WebSite) other).getRssUrl());
        return false;
    }
    public String toString(){
        return this.name;
    }
}
