import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.digest.DigestUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class HttpClientController {
    private String RESOURCE_POINT;
    HttpClient client = null;
    HttpClientController() {

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
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
                Document findedHTML = Jsoup.parse(response.body().toString());
                Element ref = findedHTML.selectFirst("#content > div.main_content.row > div > div.search_results_title " +
                        "> div:nth-child(1) > div.sr_right_block > div.sr_header > div > div.sr_name > a");
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

class ChatBot extends HttpClientController {
    String userid = "***REMOVED***";
    String RESOURCE_POINT = "https://aiproject.ru/api/";

    ChatBot() {
        super();
    }

    HttpRequest requestAnswer(String msg) {
        String ask = "\"ask\":\"" + msg + "\",";
        String userid = "\"userid\":\"" + this.userid + "\",";
        String key = "\"key\":\"\"";
        String query = "{" + ask + userid + key + "}";

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(RESOURCE_POINT))
                .POST(HttpRequest.BodyPublishers.ofString("query=" + query))
                .build();

        return httpRequest;
    }

    public String getAnswer(String msg) {
        HttpRequest request = requestAnswer(msg);
        String answer = "Что-то пошло не так";
        try {
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body().toString()).getAsJsonObject();
            int status = jsonResponse.get("status").getAsInt();
            if (status == 1) {
                answer = jsonResponse.get("aiml").getAsString();
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        return answer;
    }
}

class ComicBot extends HttpClientController {
    private String RESOURCE_POINT = "http://anecdotica.ru/api";
    private String pid = "z2njof3gz598spk3hunv";
    private String skey = "dbc506036a28b8e4ebb6ad60654cfca8d1e8a4c0c089de9792e198b30c77cc58";

    ComicBot(){
        super();
    }

    HttpRequest requestAnswer() {
        String method = "getRandItemP";
        long timestamp = Instant.now().getEpochSecond();
        String query = "pid=" + pid + "&method=" + method + "&uts=" + timestamp + "&genre=1" + "&wlist=0";

        String hash = DigestUtils.md5Hex(query + skey);
        query += "&hash=" + hash;
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(RESOURCE_POINT + "?" + query))
                .GET()
                .build();

        return httpRequest;
    }

    public String getAnek(){
        HttpRequest request = requestAnswer();
        String answer = null;
        try {
            HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            JsonObject jsonResponse = JsonParser.parseString(response.body().toString()).getAsJsonObject();
            int status = jsonResponse.get("result").getAsJsonObject().get("error").getAsInt();
            if (status == 0) {
                answer = jsonResponse.get("item").getAsJsonObject().get("text").getAsString();
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return answer;
    }

}

class EmotesGetter {
    private String BTTV_RESOURCE_POINT = "https://api.betterttv.net";
    private String FFZ_RESOURCE_POINT = "https://api.frankerfacez.com";
    private String TWITCH_RESOURCE_POINT = "";

    HttpClient client = null;

    EmotesGetter() {
        client = HttpClient.newBuilder().build();

    }

    Map<String, String> getGlobalBTTVEmotes() throws IOException, InterruptedException {
        Map<String, String> emotesMap = new HashMap<>();
        String resource = BTTV_RESOURCE_POINT + "/3/cached/emotes/global";
        String urlTemplate = "//cdn.betterttv.net/emote/{{id}}/{{image}}";
        String image = "3x";

        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(resource))
                .GET()
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonArray emotesArray = JsonParser.parseString(response.body().toString()).getAsJsonArray();

        urlTemplate = urlTemplate.replace("//", "https://");
        for (JsonElement elem : emotesArray
        ) {
            JsonObject objElem = elem.getAsJsonObject();
            String id = objElem.get("id").getAsString();
            String code = objElem.get("code").getAsString();
            String url = urlTemplate.replace("{{id}}", id).replace("{{image}}", image);
            emotesMap.put(code, url);
        }

        return emotesMap;
    }

    Map<String, String> getBTTVEmotes(String nick) throws IOException, InterruptedException {
        Map<String, String> emotesMap = new HashMap<>();

        String resource = BTTV_RESOURCE_POINT + "/2/channels/" + nick;
        HttpRequest request = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(resource))
                .GET()
                .build();
        HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
        JsonObject JSONresponse = JsonParser.parseString(response.body().toString()).getAsJsonObject();
        String urlTemplate = JSONresponse.get("urlTemplate").getAsString();

        urlTemplate = urlTemplate.replace("//", "https://");
        JsonArray emotesArray = JSONresponse.getAsJsonArray("emotes");
        String image = "3x";
        for (JsonElement elem : emotesArray
             ) {
            JsonObject objElem = elem.getAsJsonObject();
            String id = objElem.get("id").getAsString();
            String code = objElem.get("code").getAsString();
            String url = urlTemplate.replace("{{id}}", id).replace("{{image}}", image);
            emotesMap.put(code, url);
        }


        return emotesMap;
    }

    Map<String, String> getGlobalFFZEmotes() throws IOException, InterruptedException {
        Map<String, String> emotesMap = new HashMap<>();
        String globalEmotesResource = FFZ_RESOURCE_POINT + "/v1/set/3";

        HttpRequest requestGlobalEmotes = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(globalEmotesResource))
                .GET()
                .build();
        HttpResponse globalResponse = client.send(requestGlobalEmotes, HttpResponse.BodyHandlers.ofString());
        JsonArray globalEmotes = JsonParser
                .parseString(globalResponse.body().toString())
                .getAsJsonObject()
                .getAsJsonObject("set")
                .getAsJsonArray("emoticons");
        for (var elem : globalEmotes
        ) {
            JsonObject emoteInfo = elem.getAsJsonObject();
            String name = emoteInfo.get("name").getAsString();
            JsonObject urls = emoteInfo.getAsJsonObject("urls");
            String url = "";
            if (urls.has("4")) {
                url = urls.get("4").getAsString();
            } else if (urls.has("2")) {
                url = urls.get("2").getAsString();
            } else {
                url = urls.get("1").getAsString();
            }

            url = url.replaceFirst("//", "https://");

            emotesMap.put(name, url);
        }

        return emotesMap;
    }

    Map<String, String> getFFZEmotes(String nick) throws IOException, InterruptedException {
        Map<String, String> emotesMap = new HashMap<>();


        String channelEmotresResource = FFZ_RESOURCE_POINT + "/v1/room/" + nick;


        HttpRequest requestChannelEmotes = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(channelEmotresResource))
                .GET()
                .build();


        HttpResponse channelResponse = client.send(requestChannelEmotes, HttpResponse.BodyHandlers.ofString());


        JsonObject channelInfoJson = JsonParser
                .parseString(channelResponse.body().toString())
                .getAsJsonObject();

        String channelSetId = channelInfoJson
                .getAsJsonObject("room")
                .get("set")
                .getAsString();

        JsonArray channelEmotes = channelInfoJson
                .getAsJsonObject("sets")
                .getAsJsonObject(channelSetId)
                .getAsJsonArray("emoticons");


        for (var elem : channelEmotes
        ) {
            JsonObject emoteInfo = elem.getAsJsonObject();
            String name = emoteInfo.get("name").getAsString();
            JsonObject urls = emoteInfo.getAsJsonObject("urls");
            String url = "";
            if (urls.has("4")) {
                url = urls.get("4").getAsString();
            } else if (urls.has("2")) {
                url = urls.get("2").getAsString();
            } else {
                url = urls.get("1").getAsString();
            }

            url = url.replaceFirst("//", "https://");

            emotesMap.put(name, url);
        }

        return emotesMap;
    }

    Map<String, String> getGlobalTwitchEmotes() throws IOException, InterruptedException {
        Map<String, String> emotesMap = new HashMap<>();

        String urlTemplate = "https://static-cdn.jtvnw.net/emoticons/v1/{{id}}/{{size}}";
        String globalEmotesResource = "https://api.twitchemotes.com/api/v4/channels/0";

        HttpRequest globalRequest = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(globalEmotesResource))
                .GET()
                .build();

        HttpResponse globalResponse = client.send(globalRequest, HttpResponse.BodyHandlers.ofString());
        JsonArray globalEmotes = JsonParser.parseString(globalResponse.body().toString())
                .getAsJsonObject()
                .getAsJsonArray("emotes");

        for (var elem : globalEmotes
        ) {
            JsonObject emoteInfo = elem.getAsJsonObject();
            String id = emoteInfo.get("id").getAsString();
            String size = "3.0";
            String code = emoteInfo.get("code").getAsString();
            String url = urlTemplate
                    .replace("{{id}}", id)
                    .replace("{{size}}", size);
            emotesMap.put(code, url);
        }

        return emotesMap;
    }

    Map<String, String> getTwitchEmotes(String nick) throws IOException, InterruptedException {
        Map<String, String> emotesMap = new HashMap<>();

        String urlTemplate = "https://static-cdn.jtvnw.net/emoticons/v1/{{id}}/{{size}}";
        String channelEmotesResource = "https://twitchemotes.com/search/channel?query=" + nick;

        HttpRequest channelIDRequest = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(channelEmotesResource))
                .GET()
                .build();

        HttpResponse channelIDResponse = client.send(channelIDRequest, HttpResponse.BodyHandlers.ofString());

        String channelid = channelIDResponse.headers()
                .firstValue("location")
                .get()
                .replace("/channels/", "");

        if (channelid.equals("")) {
            return null;
        }

        channelEmotesResource = "https://api.twitchemotes.com/api/v4/channels/" + channelid;
        HttpRequest channelRequest = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(channelEmotesResource))
                .GET()
                .build();
        HttpResponse channelResponse = client.send(channelRequest, HttpResponse.BodyHandlers.ofString());
        JsonArray channelEmotes = JsonParser.parseString(channelResponse.body().toString())
                .getAsJsonObject()
                .getAsJsonArray("emotes");

        if (channelEmotes == null) {
            return null;
        }

        for (var elem : channelEmotes
        ) {
            JsonObject emoteInfo = elem.getAsJsonObject();
            String id = emoteInfo.get("id").getAsString();
            String size = "3.0";
            String code = emoteInfo.get("code").getAsString();
            String url = urlTemplate
                    .replace("{{id}}", id)
                    .replace("{{size}}", size);
            emotesMap.put(code, url);
        }
        return emotesMap;
    }
}


