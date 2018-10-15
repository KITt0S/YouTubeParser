import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.concurrent.Callable;

public class UrlParser implements Callable<String> {

    private String url;

    UrlParser( String url ) {

        this.url = url;
    }


    public String getUrl() {

        return url;
    }

    @Override
    public String call() throws Exception {

        try {

            Document doc = Jsoup.connect( url ).userAgent( "Chrome/61.0.3163.100" ).
                    header( "Accept-Language", "ru" ).get();
            Element viewCount = doc.select( ".watch-view-count" ).first();
            if( viewCount != null ) {

                return viewCount.ownText();
            } else {

                return "Видео не существует.";
            }
        } catch ( Exception e ) {

            return "Ошибка.";
        }
    }
}
