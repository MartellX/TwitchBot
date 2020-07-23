package api;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SimpleApi {
    private String RESOURCE_POINT;
    protected HttpClient client = null;
    public SimpleApi() {

        client = HttpClient.newBuilder().build();
    }

    HttpRequest buildRequestGameHLTB(String game) {
        String resource = "https://howlongtobeat.com/search_results?page=1";
        String body = "queryString="+ URLEncoder.encode(game) +
                "&t=games" +
                "&sorthead%3D=popular" +
                "&sortd=Normal Order" +
                "&plat=" +
                "&length_type=main" +
                "&length_min=" +
                "&length_max=" +
                "&detail=";
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(resource))
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return httpRequest;
    }

    public String getGameTimeFromHLTB(String game) {
        game = game
                .replaceAll("®", "")
                .replaceFirst("\\[", "")
                .replaceAll("]$", "");
        HttpRequest request = buildRequestGameHLTB(game);
        String answer = "???";
        try {
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Document html = Jsoup.parse(response.body().toString());
            Elements times = html.getElementsByAttributeValueStarting("class", "search_list_tidbit");
            if (times.size() > 0) {
                Element firstElement = times.get(1);
                answer = firstElement.text();
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return answer;
    }

    public String getTimeFromGamefaqs(String game) {
        game = game
                .replaceAll("®", "")
                .replaceFirst("\\[", "")
                .replaceAll("]$", "");
        String resource = "https://gamefaqs.gamespot.com";
        String query = "/search?game=" + URLEncoder.encode(game);
        String url = resource + query;
        String answer = "???";
        int i = 0;
        while (i < 5) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36")
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                Document findedHTML = Jsoup.parse(response.body().toString());
                Element ref = findedHTML.selectFirst("#content > div.main_content.row > div > div.search_results_title " +
                        "> div:nth-child(1) > div.sr_right_block > div.sr_header > div > div.sr_name > a");

                if (ref == null) {
                    return answer;
                }
                url = resource + ref.attr("href");

                request = HttpRequest.newBuilder()
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .uri(URI.create(url))
                        .GET()
                        .build();

                response = client.send(request, HttpResponse.BodyHandlers.ofString());

                Document gameHTML = Jsoup.parse(response.body().toString());
                Element time = gameHTML.selectFirst("#js_mygames_time > div.pod_split.gamerater_label > div > div > a");
                answer = time.text();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                i++;
            }
        }

        return answer;
    }

}


