package red.fuyun.util;

import com.alibaba.fastjson.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

/**
 * 今日校园App Cpdaily-Extension字段加解密实现
 */
public class DES {

    private static final String ANDROID_DEFAULT_KEY = "ST83=@XV";
//    private static final String IPHONE_DEFAULT_KEY = "XCE927==";
    private static final String IPHONE_DEFAULT_KEY = "b3L26XNL";
    private static final byte[] IV = {1, 2, 3, 4, 5, 6, 7, 8};

    private static final String CHARSET_NAME = "utf-8";
    private static final String DES = "DES";
    private static final String CIPHER_NAME = "DES/CBC/PKCS5Padding";

    private static byte[] DESEncrypt(String data, String key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_NAME);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(CHARSET_NAME), DES);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        return cipher.doFinal(data.getBytes(CHARSET_NAME));
    }

    private static String DESDecrypt(byte[] data, String key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER_NAME);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(CHARSET_NAME), DES);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        return new String(cipher.doFinal(data), CHARSET_NAME);
    }

    /**
     * Base64编码
     *
     * @param data
     * @return
     */
    private static String Base64Encrypt(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    /**
     * Base64解码
     *
     * @param data
     * @return
     * @throws UnsupportedEncodingException
     */
    private static byte[] Base64Decrypt(String data) throws UnsupportedEncodingException {
        return Base64.getMimeDecoder().decode(data.getBytes(CHARSET_NAME));
    }

    public static String encryptDES(String data,String defaultKey) throws Exception {
        return Base64Encrypt(DESEncrypt(data, defaultKey, IV));
    }

    public static String decryptDES(String data,String defaultKey) throws Exception {
        return DESDecrypt(Base64Decrypt(data), defaultKey, IV);
    }
}
