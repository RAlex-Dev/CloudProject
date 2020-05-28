package pkg.Tools;

import pkg.Constants;
import pkg.model.EncryptedFile;

import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class EDTool {

    public EncryptedFile encryptFile(File file, char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException,
            NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[8];
        secureRandom.nextBytes(salt);

        if (file == null) {
            return null;
        } else {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec keySpec = new PBEKeySpec(password, salt, 65535, 256);
            SecretKey tempKey = secretKeyFactory.generateSecret(keySpec);
            SecretKey secretKey = new SecretKeySpec(tempKey.getEncoded(), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            int partSize = 1024 * 1024;
            byte[] buffer = new byte[partSize];

            EncryptedFile encryptedFile = new EncryptedFile(Constants.tempDir, file.getName());
            encryptedFile.setSaltString(salt); // create salt string for transmission
            encryptedFile.setFileName(file.getName());
            encryptedFile.setFilePath(file.getPath());

            try (
                    FileOutputStream fileOutputStream = new FileOutputStream(encryptedFile);
                    FileInputStream fileInputStream = new FileInputStream(file);
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {

                int bytesLeft = 0;

                while ((bytesLeft = bufferedInputStream.read(buffer)) > -1) { // read until there is no data left from the input stream
                    byte[] cipherData = cipher.update(buffer, 0, bytesLeft);
                    fileOutputStream.write(cipherData);
                }

                byte[] outputArr = cipher.doFinal();
                if (outputArr != null) {
                    fileOutputStream.write(outputArr);
                }

                fileInputStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return encryptedFile;
        }
    }

    public void decryptFile(File encryptedFile, byte[] salt, String fileExt, String fileName, char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException,
            IOException, BadPaddingException, IllegalBlockSizeException {
        byte[] buffer = new byte[1024 * 1024];
        byte[] decipherData;

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

        KeySpec keySpec = new PBEKeySpec(password, salt, 65535, 256);
        SecretKey tempKey = secretKeyFactory.generateSecret(keySpec);
        SecretKey secretKey = new SecretKeySpec(tempKey.getEncoded(), "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);

        // insert (try... catch) later
        FileInputStream fileInputStream = new FileInputStream(encryptedFile);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        File outputFile = new File(Constants.incDir, fileName + "." + fileExt);

        FileOutputStream fileOutputStream = new FileOutputStream(outputFile); // fix for multiple files
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);

        int bytesRead;

        while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
            byte[] output = cipher.update(buffer, 0, bytesRead);

            if (output != null) {
                bufferedOutputStream.write(output);
            }
        }
        byte[] output = cipher.doFinal();
        if (output != null) {
            bufferedOutputStream.write(output);
        }
        fileInputStream.close();
        fileOutputStream.flush();
        fileOutputStream.close();
    }
}
