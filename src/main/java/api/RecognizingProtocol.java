package api;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import okhttp3.*;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class RecognizingProtocol {
    String host = "***REMOVED***";
    String accessKey = "***REMOVED***";
    String secretKey = "***REMOVED***";

    public RecognizingProtocol() {

    }

    private String encodeBase64(byte[] bstr) {
        Base64 base64 = new Base64();
        return new String(base64.encode(bstr));
    }

    private String encryptByHMACSHA1(byte[] data, byte[] key) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(key, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data);
            return encodeBase64(rawHmac);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getUTCTimeSeconds() {
        Calendar cal = Calendar.getInstance();
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        return cal.getTimeInMillis()/1000 + "";
    }


    private String postOkHTTP(String posturl, Map<String, Object> params){
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        for (var e : params.entrySet()) {
            Object value = e.getValue();
            String key = e.getKey();
            if (!key.equals("sample")) {
                bodyBuilder.addFormDataPart(key, (String) value);
            } else {
                bodyBuilder.addFormDataPart(key, key,
                        RequestBody.create((byte[]) value));
            }
        }

        RequestBody body = bodyBuilder.build();
        Request request = new Request.Builder()
                .url(posturl)
                .post(body)
                .build();
        String res = "";
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                res = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;


    }

    public Map<String, String> recognize(byte[] queryData, String queryType)
    {
        String method = "POST";
        String httpURL = "/v1/identify";
        String dataType = queryType;
        String sigVersion = "1";
        String timestamp = getUTCTimeSeconds();

        String reqURL = "http://" + host + httpURL;

        String sigStr = method + "\n" + httpURL + "\n" + accessKey + "\n" + dataType + "\n" + sigVersion + "\n" + timestamp;
        String signature = encryptByHMACSHA1(sigStr.getBytes(), secretKey.getBytes());

        Map<String, Object> postParams = new HashMap<String, Object>();
        postParams.put("access_key", accessKey);
        postParams.put("sample_bytes", queryData.length + "");
        postParams.put("sample", queryData);
        postParams.put("timestamp", timestamp);
        postParams.put("signature", signature);
        postParams.put("data_type", queryType);
        postParams.put("signature_version", sigVersion);

        String res = postOkHTTP(reqURL, postParams);
        Map<String, String> resMap = new HashMap<>();

        JsonObject resObject = JsonParser.parseString(res).getAsJsonObject();
        int statusCode = resObject.getAsJsonObject("status").get("code").getAsInt();
        String statusMessage = resObject.getAsJsonObject("status").get("msg").getAsString();
        resMap.put("status", statusMessage);
        if (statusCode == 0) {
            List<String> releaseDays = new ArrayList<>();
            StringBuilder songsInfo = new StringBuilder();
            JsonArray music = resObject.getAsJsonObject("metadata").getAsJsonArray("music");
            for (JsonElement e:music
                 ) {

                JsonObject track = e.getAsJsonObject();
                String releaseDay = track.get("release_date").getAsString();

                if (releaseDays.contains(releaseDay)) {
                    continue;
                } else {
                    releaseDays.add(releaseDay);
                }

                JsonArray artistsJsonArray = track.get("artists").getAsJsonArray();
                for (var art:artistsJsonArray
                     ) {
                    String artist = art.getAsJsonObject().get("name").getAsString();
                    songsInfo.append(artist + ", ");
                }
                songsInfo.deleteCharAt(songsInfo.lastIndexOf(","));
                String title = track.get("title").getAsString();
                songsInfo.append("- " + title + "; ");
            }
            songsInfo.deleteCharAt(songsInfo.lastIndexOf(";"));
            resMap.put("result", songsInfo.toString());
        }

        return resMap;
    }


}
