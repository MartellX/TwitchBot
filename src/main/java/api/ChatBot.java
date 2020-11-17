package api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ChatBot extends SimpleApi {
    String userid = "cemka";
    String RESOURCE_POINT = "https://aiproject.ru/api/";

    public ChatBot() {
        super();
    }

    Request requestAnswer(String msg) {

        Data data = new Data(msg, userid);

        String query = new Gson().toJson(data);
        Request request = new Request.Builder()
                .post(new FormBody.Builder().addEncoded("query", query).build())
                .url(RESOURCE_POINT)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .header("Content-Type", "application/x-www-form-urlencoded")
                .uri(URI.create(RESOURCE_POINT))
                .POST(HttpRequest.BodyPublishers.ofString("query=" + query))
                .build();

        return request;
    }

    public String getAnswer(String msg) {
        Request request = requestAnswer(msg);

        String answer = null;
        try {
            //HttpResponse response = client.send(request, HttpResponse.BodyHandlers.ofString());
            Response response = okclient.newCall(request).execute();
            JsonObject jsonResponse = JsonParser.parseString(response.body().string()).getAsJsonObject();
            int status = jsonResponse.get("status").getAsInt();
            if (status == 1) {
                answer = jsonResponse.get("aiml").getAsString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return answer;
    }

    private class Data{
        String ask;
        String userid;
        String key="";

        Data(String ask, String userid) {
            this.ask = ask;
            this.userid = userid;

        }
    }
}
