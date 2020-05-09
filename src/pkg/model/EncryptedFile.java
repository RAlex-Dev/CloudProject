package pkg.model;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.net.URI;

public class EncryptedFile extends File {

    private byte[] salt = new byte[8];
    private String fileString;
    private String saltString;
    private String fileName;
    private String filePath;

    public EncryptedFile(String pathname) {
        super(pathname);
    }

    public EncryptedFile(String parent, String child) {
        super(parent, child);
    }

    public EncryptedFile(File parent, String child) {
        super(parent, child);
    }

    public EncryptedFile(URI uri) {
        super(uri);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public String getFileString() {
        return fileString;
    }

    public void setFileString(String fileString) {
        this.fileString = fileString;
    }

    public String getSaltString() {
        return saltString;
    }

    public void setSaltString(byte[] salt) {
        saltString = Base64.encodeBase64String(salt);
    }
}
