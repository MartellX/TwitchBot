package api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class UnofficialTwitchApi {

    private static volatile UnofficialTwitchApi instance;

    public static UnofficialTwitchApi getInstance() {
        UnofficialTwitchApi localInstance = instance;
        if (localInstance == null) {
            synchronized (UnofficialTwitchApi.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new UnofficialTwitchApi();
                }
            }
        }
        return localInstance;
    }

    OkHttpClient client;

    String clientid = "***REMOVED***";

    private UnofficialTwitchApi() {
        client = new OkHttpClient.Builder().callTimeout(Duration.ofSeconds(10)).build();
    }

    public static void main(String[] args) {
        UnofficialTwitchApi a = new UnofficialTwitchApi();
        Map<String, String> token = a.getAccessToken("bandsintown");
        try {
            String tokenString = URLEncoder.encode(token.get("token"), StandardCharsets.UTF_8.toString());
            System.out.println(tokenString);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        System.out.println(token.get("sig"));

    }

    Map<String, String> getAccessToken(String channel) {
        Map<String, String> resultMap = new HashMap<>();

        String url = "https://api.twitch.tv/api/channels/{{channel}}/access_token"
                .replace("{{channel}}", channel);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Client-ID", clientid)
                .build();

        try {
            Response response = client.newCall(request).execute();
            JsonObject jsonResponse = JsonParser.parseString(response.body().string()).getAsJsonObject();
            String token = jsonResponse.get("token").getAsString();
            String sig = jsonResponse.get("sig").getAsString();

            resultMap.put("token", token);
            resultMap.put("sig", sig);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultMap;
    }

    public InputStream getM3U8ofChannel(String channel) {
        Map<String, String> tokenMap = getAccessToken(channel);

        try {
            String token = URLEncoder.encode(tokenMap.get("token"), StandardCharsets.UTF_8.toString());
            String sig = tokenMap.get("sig");
            String url = HttpUrl.parse("https://usher.ttvnw.net/api/channel/hls/{{channel}}.m3u8"
                    .replace("{{channel}}", channel))
                    .newBuilder()
                    .addQueryParameter("sig", tokenMap.get("sig"))
                    .addQueryParameter("token", tokenMap.get("token"))
                    .addQueryParameter("allow_audio_only", "true")
                    .build()
                    .toString();

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().byteStream();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
