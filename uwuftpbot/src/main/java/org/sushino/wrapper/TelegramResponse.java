package org.sushino.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.sushino.Parameters;

public class TelegramResponse {

    public final boolean ok;
    public final TelegramFileWrapper result;

    public TelegramResponse(@JsonProperty("ok") boolean ok, @JsonProperty("result") TelegramFileWrapper result) {
        this.ok = ok;
        this.result = result;
    }

    @Override
    public String toString() {
        return "org.sushino.wrapper.TelegramResponse{" +
                "ok=" + ok +
                ", result=" + result.toString() +
                '}';
    }

    public static class TelegramFileWrapper {
        public final String fileId;
        public final String fileUniqueId;
        public final Long fileSize;
        public final String filePath;

        public TelegramFileWrapper(@JsonProperty("file_id") String fileId,
                                   @JsonProperty("file_unique_id") String fileUniqueId,
                                   @JsonProperty("file_size") Long fileSize,
                                   @JsonProperty("file_path") String filePath) {
            this.fileId = fileId;
            this.fileUniqueId = fileUniqueId;
            this.fileSize = fileSize;
            this.filePath = filePath;
        }

        @Override
        public String toString() {
            return "JsonWrapper{" +
                    ", fileId='" + fileId + '\'' +
                    ", fileUniqueId='" + fileUniqueId + '\'' +
                    ", fileSize=" + fileSize +
                    ", filePath='" + filePath + '\'' +
                    '}';
        }
    }

    public static TelegramResponse getResponse(String fileId) {
        ObjectMapper mapper = new ObjectMapper();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String uri = "https://api.telegram.org/bot" + Parameters.BOT_TOKEN + "/getFile?file_id=" + fileId;
            HttpGet request = new HttpGet(uri);
            return client.execute(request, httpResponse -> mapper.readValue(httpResponse.getEntity().getContent(), TelegramResponse.class));
        } catch (Exception ignored) {
        }
        return null;
    }
}
