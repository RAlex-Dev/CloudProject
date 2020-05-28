package pkg.Controller;

import okhttp3.*;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import pkg.model.EncryptedFile;

import java.io.IOException;
import java.util.ArrayList;

public class WebController {

    public Response uploadFileToServer(EncryptedFile encryptedFile) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\n    \"user_id\": \"ACCloud@ACCloud.com\"" +
                ",\n    \"file_name\": \"" + encryptedFile.getName() + "\"" +
                ",\n    \"file_extension\": \"" + FilenameUtils.getExtension(encryptedFile.getFilePath()) + "\"" +
                ",\n    \"file\": \"" + encryptedFile.getFileString() + "\"" +
                ",\n    \"salt\": \"" + encryptedFile.getSaltString() + "\"" +
                ",\n    \"tier\": \"" + encryptedFile.getAccessLevel() + "\"" +
                " \n}", JSON);

        String requestString = "{\n    \"user_id\": \"ACCloud@ACCloud.com\"" +
                ",\n    \"file_name\": \"" + encryptedFile.getName() + "\"" +
                ",\n    \"file_extension\": \"" + FilenameUtils.getExtension(encryptedFile.getFilePath()) + "\"" +
                ",\n    \"file\": \"" + encryptedFile.getFileString() + "\"" +
                ",\n    \"salt\": \"" + encryptedFile.getSaltString() + "\"" +
                ",\n    \"tier\": \"" + encryptedFile.getAccessLevel() + "\"" +
                " \n}";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://3.15.177.232/api/upload")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        response.close();
        return response;
    }
    // upload entire directory to server

    public Response uploadDirToServer(ArrayList<EncryptedFile> encryptedFileList) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();

        // build a response body for multiple files if necessary
        for (int i = 0; i < encryptedFileList.size(); i++) {
            stringBuilder.append("{\n    \"file_name\": \"" + encryptedFileList.get(i).getName() + "\"" +
                    ",\n    \"file_extension\": \"" + FilenameUtils.getExtension(encryptedFileList.get(i).getFilePath()) + "\"" +
                    ",\n    \"file\": \"" + encryptedFileList.get(i).getFileString() + "\"" +
                    ",\n    \"salt\": \"" + encryptedFileList.get(i).getSaltString() + "\"\n}");
            if (encryptedFileList.size() > 1 && encryptedFileList.size() - 1 != i) {
                stringBuilder.append(",\n ");
            }
        }

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\n    \"user_id\": \"ACCloud@ACCloud.com\"" +
                ",\n    \"files\": [" +
                stringBuilder.toString() +
                "\n    ]\n}", JSON);

        Request request = new Request.Builder()
                .url("http://3.15.177.232/api/upload_bulk")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();

        Response response = client.newCall(request).execute();
        response.close();
        return response;
    }

    public Response downloadFromServer(String fileToDownloadId) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\n    \"user_id\": \"ACCloud@ACCloud.com\",\n    \"file_id\":"
                + "\"" + fileToDownloadId + "\"" + "\n}", JSON);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://3.15.177.232/api/download")
                .method("POST", body)
                .build();
        Response response = client.newCall(request).execute();
        System.out.println(response);
        return response;
    }

    public Response deleteToServer(String fileID) throws IOException {
        System.out.println(fileID);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\n    \"user_id\": \"ACCloud@ACCloud.com\",\n    \"file_id\":"
                + "\"" + fileID + "\"\n}", JSON);
        Request request = new Request.Builder()
                .url("http://3.15.177.232/api/delete")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    public Response getBlockchain() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("", JSON);
        Request request = new Request.Builder()
                .url("http://3.15.177.232/api/get_chain")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    public Response getWork() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("", JSON);
        Request request = new Request.Builder()
                .url("http://3.15.177.232/api/get_work")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    public Response submitWork(JSONObject jsonObject) throws IOException {
        String stringBlock = jsonObject.toString();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        String previousHash = jsonObject.get("previousHash").toString();
        int index = (int) jsonObject.get("index");
        String merkleRoot = jsonObject.get("merkleRoot").toString();

        JSONArray transactions = (JSONArray) jsonObject.get("transactions");
        JSONObject transaction = (JSONObject) transactions.get(0);
        JSONObject value = transaction.getJSONObject("value");
        String payload = value.getString("payload");
        long time = value.getLong("time");
        String event = value.getString("event");
        String transHash = transaction.getString("hash");
        int nonce = jsonObject.getInt("nonce");
        String blockHash = jsonObject.getString("hash");
        long timeStamp = jsonObject.getLong("timestamp");

        RequestBody body = RequestBody.create("{\n    \"user_id\": \"ACCloud@ACCloud.com\",\n    " +
                "\"block\": {\n       " +
                "\"previousHash\": \"" + previousHash + "\",\n        " +
                "\"index\":" + index + ",\n        " +
                "\"merkleRoot\": \"" + merkleRoot + "\",\n        " +
                "\"transactions\": [\n            {\n                " +
                "\"value\": {\n                    " +
                "\"payload\": \"" + payload + "\",\n                    " +
                "\"time\":" + time + ",\n                    " +
                "\"event\": \"" + event + "\"\n                },\n                " +
                "\"hash\": \"" + transHash + "\"\n            }\n        ],\n        " +
                "\"nonce\":" + nonce + " ,\n        " +
                "\"hash\": \"" + blockHash + "\",\n        " +
                "\"timestamp\":" + timeStamp + "\n    }\n}", JSON);

        Request request = new Request.Builder()
                .url("http://3.15.177.232/api/submit_work")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    public Response getInfo() throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\"user_id\":\"ACCloud@ACCloud.com\"}", JSON);
        Request request = new Request.Builder()
                .url("http://3.15.177.232/api/user_info")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    public Response getHistory(String fileId) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\n    \"user_id\": \"ACCloud@ACCloud.com\",\n    \"file_id\":"
                + "\"" + fileId + "\"" + "\n}", JSON);
        Request request = new Request.Builder()
                .url("http://3.15.177.232/api/history")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }
}
