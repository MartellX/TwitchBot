package api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;


public class ComicBot extends SimpleApi {
    private String RESOURCE_POINT = "http://anecdotica.ru/api";
    private String pid = "z2njof3gz598spk3hunv";
    private String skey = "dbc506036a28b8e4ebb6ad60654cfca8d1e8a4c0c089de9792e198b30c77cc58";

    public ComicBot(){
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
