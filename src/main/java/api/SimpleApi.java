package api;

import controllers.MainController;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
    protected OkHttpClient okclient;
    public SimpleApi() {

        okclient = new OkHttpClient.Builder().build();
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

    public static void main(String[] args) {
        SimpleApi simpleApi = new SimpleApi();
        for(int i = 0; i < 50; i++){
            System.out.println("--------------------------------------");
            System.out.println(simpleApi.getAnek());
            System.out.println("--------------------------------------");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
    public String[] getAnek() {
        Request request = new Request.Builder()
                .url("https://baneks.ru/random")
                .get()
                .build();

        String[] result = new String[2];

        try {
            int i = 0;
            String key = "-1";
            while (true) {
                Response response = okclient.newCall(request).execute();
                Document anekHTML = Jsoup.parse(response.body().string());
                key = anekHTML.selectFirst("body > main").attr("data-id");
                i++;
                if (MainController.getAneks().contains(key) && i < 10) {
                    continue;
                } else {
                    Element anekText = anekHTML.selectFirst("body > main > section > article > p");
                    result[0] = anekText.text();
                    result[1] = key;
                    break;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public String[] getAnekAlter() {
        Request request = new Request.Builder()
                .url("https://baneks.site/random")
                .get()
                .build();

        String[] result = new String[2];

        try {
            int i = 0;
            String key = "-1";
            while (true) {
                Response response = okclient.newCall(request).execute();
                Document anekHTML = Jsoup.parse(response.body().string());
                Element keyElement = anekHTML
                        .selectFirst("body > div.mdl-layout.mdl-js-layout.mdl-layout--fixed-header.mdl-layout--fixed-drawer > main > div > div.page-content.mdl-cell.mdl-cell--6-col > div.joke.mdl-shadow--6dp.block.mdl-card.mdl-card--border");
                key = keyElement
                        .attr("id");
                i++;
                if (MainController.getAneks().contains(key) && i < 10) {
                    continue;
                } else {

                    Element anekText = keyElement.selectFirst("div.block-content.mdl-card__supporting-text.mdl-color--grey-300.mdl-color-text--grey-900 > article > section > p");
                    result[0] = anekText.text();
                    result[1] = key;
                    break;
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }
}


