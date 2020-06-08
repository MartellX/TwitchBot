package api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class EmotesGetter {
    private String BTTV_RESOURCE_POINT = "https://api.betterttv.net";
    private String FFZ_RESOURCE_POINT = "https://api.frankerfacez.com";
    private String TWITCH_RESOURCE_POINT = "";

    HttpClient client = null;

    public EmotesGetter() {
        client = HttpClient.newBuilder().build();

    }

    public Map<String, String> getGlobalBTTVEmotes() throws IOException, InterruptedException {
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

    public Map<String, String> getBTTVEmotes(String nick) throws IOException, InterruptedException {
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

    public Map<String, String> getGlobalFFZEmotes() throws IOException, InterruptedException {
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

    public Map<String, String> getFFZEmotes(String nick) throws IOException, InterruptedException {
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

    public Map<String, String> getGlobalTwitchEmotes() throws IOException, InterruptedException {
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

    public Map<String, String> getTwitchEmotes(String nick) throws IOException, InterruptedException {
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
