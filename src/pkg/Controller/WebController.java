package pkg.Controller;

import okhttp3.*;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;
import pkg.model.EncryptedFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class WebController {

    public Response uploadFileToServer(EncryptedFile encryptedFile) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\n    \"user_id\": \"kevin@kevin.com\"" +
                ",\n    \"file_name\": \"" + encryptedFile.getName() + "\"" +
                ",\n    \"file_extension\": \"" + FilenameUtils.getExtension(encryptedFile.getFilePath()) + "\"" +
                ",\n    \"file\": \"" + encryptedFile.getFileString() + "\"" +
                ",\n    \"salt\": \"" + encryptedFile.getSaltString() + "\"\n}", JSON);

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
        RequestBody body = RequestBody.create("{\n    \"user_id\": \"kevin@kevin.com\"" +
                ",\n    \"files\": [" +
                stringBuilder.toString() +
                "\n    ]\n}", JSON);

        Request request = new Request.Builder()
                .url("http://3.15.177.232/api/upload_bulk")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        System.out.println(body);

        Response response = client.newCall(request).execute();
        response.close();
        return response;
    }


    public Response downloadFromServer(String fileToDownloadId) throws IOException {
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\n    \"user_id\": \"kevin@kevin.com\",\n    \"file_id\":"
                + "\"" + fileToDownloadId + "\"" + "\n}", JSON);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://3.15.177.232/api/download")
                .method("POST", body)
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    public Response deleteToServer(String fileID) throws IOException {
        System.out.println(fileID);

        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        RequestBody body = RequestBody.create("{\n    \"user_id\": \"kevin@kevin.com\",\n    \"file_id\":"
                + "\"" + fileID + "\"\n}", JSON);
        Request request = new Request.Builder()
                .url("http://3.15.177.232/api/delete")
                .method("POST", body)
                .addHeader("Content-Type", "application/json")
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }
}
