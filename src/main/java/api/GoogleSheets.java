package api;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchGetValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.*;

public class GoogleSheets {
    private final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private final NetHttpTransport HTTP_TRANSPORT;
    private final String hpgSpreadsheetId = "1Ag5JOC1tEC7EgMtAB8XJQw-pGc0Xm-s-gh1-_IGh-Ag";
    private final String pastesSpreadsheetId = "1WWBl4hgaFOgvJZvNrG3vwMoerAxMhcwRO-lcYVEu__M";

    private Sheets sheetsService;

    public GoogleSheets() throws GeneralSecurityException, IOException {
        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    }

    public static void main(String[] args) throws GeneralSecurityException, IOException {
        String creds = System.getenv("GOOGLE_CREDS");
        GoogleSheets googleSheets = new GoogleSheets(creds);
    }

    public GoogleSheets(String creds) throws GeneralSecurityException, IOException {
        this();

        GoogleCredential credentials;
        try (InputStream serviceAccountStream = new ByteArrayInputStream(creds.getBytes())) {
            credentials = GoogleCredential.fromStream(serviceAccountStream)
                    .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));
        }


        this.sheetsService = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
                .setApplicationName("hpg_bot")
                .build();
    }

    public List<List<Object>> getTop () {
        try{
            String range = "Таблица лидеров!A3:I8";
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(hpgSpreadsheetId, range)
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
            ranges.add(nick + "!F12:F10000"); // Статус
            ranges.add(nick + "!D12:D10000"); // Отрезок
            ranges.add(nick + "!E12:E10000"); // Игра
            ranges.add(nick + "!G12:G10000"); // Номинальное GGP
            ranges.add(nick + "!I12:I10000"); // Комментарий
            ranges.add(nick + "!J12:J10000");
            Sheets.Spreadsheets.Values.BatchGet request = sheetsService
                    .spreadsheets()
                    .values()
                    .batchGet(hpgSpreadsheetId);
            request.setRanges(ranges);
            BatchGetValuesResponse response = request.execute();
            int lastRow = 0;
            for(ValueRange column : response.getValueRanges()) {
                List<Object> columnList = column.getValues().get(0);
                if (columnList.get(0).equals("Результат")) {
                    lastRow = column.getValues().size();
                    infoMap.put("Status", column.getValues().get(lastRow - 1).toString());
                }
                if (columnList.get(0).equals("Отрезок")) {
                    infoMap.put("Range", column.getValues().get(lastRow - 1).toString());
                } else if (columnList.get(0).equals("Игра")) {
                    infoMap.put("Game", column.getValues().get(lastRow - 1).toString());
                } else if (columnList.get(0).equals("Номинальное GGP")) {
                    infoMap.put("GGP", column.getValues().get(lastRow - 1).toString());
                } else if (columnList.get(0).equals("Комментарий")) {
                    if (lastRow <= column.getValues().size()) {
                        if (!column.getValues().get(lastRow - 1).toString().equals("")) {
                            infoMap.put("Comment", column.getValues().get(lastRow - 1).toString());
                        }
                    }
                } else if (columnList.get(0).equals("События")) {
                    if (lastRow <= column.getValues().size()) {
                        if (!column.getValues().get(lastRow - 1).toString().equals("")) {
                            infoMap.put("Events", column.getValues().get(lastRow - 1).toString());
                        }
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
                    .get(hpgSpreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();

            int i = 0;
            boolean isFinds = true;
            while ((values.get(values.size() - 1 - i)).size() != 2) {
                i++;
                if (values.size() - 1 - i < 0) {
                    isFinds = false;
                    break;
                }
            }
            if (isFinds) {
                String event = values.get(values.size() - 1 - i).get(0).toString();
                if (event.equals("")) {
                    int j = i + 1;
                    while(event.equals("")) {
                        if (values.get(values.size() - 1 - j).size() > 0) {
                            event = values.get(values.size() - 1 - j).get(0).toString();
                        }
                        j++;
                    }
                }
                lastEvent.add(event);
                lastEvent.add(values.get(values.size() - 1 - i).get(1).toString());
            }
            return lastEvent;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<List<Object>> getPastes() {
        try {
            String range = "A1:C1000";
            ValueRange response = sheetsService.spreadsheets().values()
                    .get(pastesSpreadsheetId, range)
                    .setMajorDimension("Columns")
                    .execute();
            return response.getValues();
        } catch (IOException e) {
            System.out.println("Непредвиденная ошибка в [getPastes()]: " + e.getMessage());
        }
        return null;
    }
}
