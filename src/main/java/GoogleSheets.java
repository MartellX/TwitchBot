import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Stream;

public class GoogleSheets {
    private JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final NetHttpTransport HTTP_TRANSPORT;
    private final String spreadsheetId = "1Ag5JOC1tEC7EgMtAB8XJQw-pGc0Xm-s-gh1-_IGh-Ag";

    private Credential credentials;
    private Sheets sheetsService;

    public GoogleSheets() throws GeneralSecurityException, IOException {
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    }

    public GoogleSheets(String credsPath) throws GeneralSecurityException, IOException {
        this();

        File credentialsPath = new File(credsPath);
        try (FileInputStream serviceAccountStream = new FileInputStream(credentialsPath)) {
            this.credentials = GoogleCredential.fromStream(serviceAccountStream)
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        }

        this.sheetsService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                .setApplicationName("hpg_bot")
                .build();
    }

    public List<List<Object>> getTop () {
        try{
            String range = "Таблица лидеров!A2:H6";
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            return response.getValues();
        } catch (IOException e) {
            System.out.println("Непредвиденная ошибка в [getTop()]: " + e.getMessage());
        }
        return null;
    }

    public Map<String, String> getInfoAbout (String nick) {
        try {
            Map<String, String> infoMap= new HashMap<>();
            List<String> ranges = new ArrayList<>();
            ranges.add(nick + "!D12:D10000"); // Отрезок
            ranges.add(nick + "!E12:E10000"); // Игра
            ranges.add(nick + "!G12:G10000"); // Номинальное GGP
            ranges.add(nick + "!I12:I10000"); // Комментарий
            Sheets.Spreadsheets.Values.BatchGet request = sheetsService
                    .spreadsheets()
                    .values()
                    .batchGet(spreadsheetId);
            request.setRanges(ranges);
            BatchGetValuesResponse response = request.execute();
            int lastRow = 0;
            for(var column : response.getValueRanges()) {
                List<Object> columnList = column.getValues().get(0);
                if (columnList.get(0).equals("Отрезок")) {
                    lastRow = column.getValues().size();
                    infoMap.put("Range", column.getValues().get(lastRow - 1).toString());
                } else if (columnList.get(0).equals("Игра")) {

                    infoMap.put("Game", column.getValues().get(column.getValues().size() - 1).toString());
                } else if (columnList.get(0).equals("Номинальное GGP")) {
                    infoMap.put("GGP", column.getValues().get(column.getValues().size() - 1).toString());
                } else if (columnList.get(0).equals("Комментарий")) {
                    if (lastRow == column.getValues().size()) {
                        infoMap.put("Comment", column.getValues().get(column.getValues().size() - 1).toString());
                    }
                }
            }

            return infoMap;
        } catch (IOException e) {
            System.out.println("Непредвиденная ошибка в [getInfoAbout (" + nick + ")]: " + e.getMessage());
        }
        return null;
    }

    public List<String> getLastEvent(String nick) {
        String range = nick + "!L12:M10000";
        List<String> lastEvent = new ArrayList<>();
        try {
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();

            int i = 0;
            boolean isFinds = true;
            while (values.get(values.size() - 1 - i).size() != 2) {
                i++;
                if (values.size() - 1 - i < 0) {
                    isFinds = false;
                    break;
                }
            }
            if (isFinds) {
                lastEvent.add(values.get(values.size() - 1).get(0).toString());
                lastEvent.add(values.get(values.size() - 1).get(1).toString());
            }
            return lastEvent;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
