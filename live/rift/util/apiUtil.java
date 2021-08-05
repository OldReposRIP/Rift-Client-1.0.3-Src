package live.rift.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Consumer;

public class apiUtil {

    private static SimpleEntry entry = new SimpleEntry(Integer.valueOf(0), Long.valueOf(System.currentTimeMillis()));
    private static SimpleEntry entry2 = new SimpleEntry("0m", Long.valueOf(System.currentTimeMillis()));

    public static int getPrioQueueLength() {
        if (((Integer) apiUtil.entry.getKey()).intValue() != 0 && System.currentTimeMillis() - ((Long) apiUtil.entry.getValue()).longValue() <= 30000L) {
            return ((Integer) apiUtil.entry.getKey()).intValue();
        } else {
            JsonParser jsonParser = new JsonParser();
            String url = "https://api.2b2t.dev/prioq";

            try {
                HttpURLConnection e = (HttpURLConnection) (new URL("https://api.2b2t.dev/prioq")).openConnection();

                e.setRequestProperty("User-Agent", "Sanku-Bot/1.0.0");
                BufferedReader in = new BufferedReader(new InputStreamReader(e.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder result = new StringBuilder();

                in.lines().forEach(result::append);
                in.close();
                String response = result.toString();

                if (response.length() > 0) {
                    JsonArray jsonArray = (JsonArray) jsonParser.parse(response);

                    if (jsonArray != null) {
                        JsonPrimitive object = jsonArray.get(1).getAsJsonPrimitive();
                        int prioQLength = object.getAsInt();

                        apiUtil.entry = new SimpleEntry(Integer.valueOf(prioQLength), Long.valueOf(System.currentTimeMillis()));
                        return prioQLength;
                    }
                }
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            }

            return 0;
        }
    }

    public static String getPrioTime() {
        if (!((String) apiUtil.entry2.getKey()).equals("0m") && System.currentTimeMillis() - ((Long) apiUtil.entry2.getValue()).longValue() <= 30000L) {
            return (String) apiUtil.entry2.getKey();
        } else {
            JsonParser jsonParser = new JsonParser();
            String url = "https://api.2b2t.dev/prioq";

            try {
                HttpURLConnection e = (HttpURLConnection) (new URL("https://api.2b2t.dev/prioq")).openConnection();

                e.setRequestProperty("User-Agent", "Sanku-Bot/1.0.0");
                BufferedReader in = new BufferedReader(new InputStreamReader(e.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder result = new StringBuilder();

                in.lines().forEach(result::append);
                in.close();
                String response = result.toString();

                if (response.length() > 0) {
                    JsonArray jsonArray = (JsonArray) jsonParser.parse(response);

                    if (jsonArray != null) {
                        String object;

                        if (!jsonArray.get(2).isJsonNull()) {
                            object = jsonArray.get(2).getAsString();
                            apiUtil.entry2 = new SimpleEntry(object, Long.valueOf(System.currentTimeMillis()));
                            return object;
                        }

                        object = "0m";
                        apiUtil.entry2 = new SimpleEntry(object, Long.valueOf(System.currentTimeMillis()));
                        return object;
                    }
                }
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            }

            return "0m";
        }
    }
}
