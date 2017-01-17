package com.letv.wallet.utils;

import android.util.Base64;

import java.util.Arrays;

/**
 * Created by zhuchuntao on 17-1-9.
 */

public class SslUtil {


    private static SslUtil instance;

    private int rsaSize;

    /**
     * 线程安全的单例模式
     *
     * @return SecurityUtil
     */
    public static SslUtil getInstance() {
        if (null == instance) {
            synchronized (SslUtil.class) {
                if (instance == null) {//二次检查
                    instance = new SslUtil();
                }
            }
        }
        return instance;
    }

    public SslUtil() {
        setSizeOfRSA();
    }

    /**
     * 设置公钥长度，决定分块的大小
     */
    private void setSizeOfRSA() {
        rsaSize = opensslRsaSize();
    }

    /**
     * 将一段文本转换成密文
     *
     * @param needEncryptData 待加密的文本
     * @return 加密后的密文
     */
    public String encryptData(String needEncryptData) {
        try {


            int dataMaxLength = rsaSize - 11;
            // 待加密的数据
            byte[] beforeEncrypt = needEncryptData.getBytes();
            byte[] encryp = new byte[0];
            if (beforeEncrypt.length > dataMaxLength) {
                //分块加密
                int byteListNumber = beforeEncrypt.length % dataMaxLength > 0 ? beforeEncrypt.length / dataMaxLength + 1 : beforeEncrypt.length / dataMaxLength;
                for (int i = 0; i < byteListNumber; i++) {
                    int start = i * dataMaxLength;
                    int end = i == byteListNumber - 1 ? beforeEncrypt.length : i * dataMaxLength + dataMaxLength;
                    byte[] block = Arrays.copyOfRange(beforeEncrypt, start, end);
                    byte[] encryptByte = opensslEncrypt(block);// RSAUtils.encryptData(block, publicKey);

                    encryp = addByte(encryp, encryptByte);
                }
            } else {
                //直接加密
                byte[] encryptByte = opensslEncrypt(beforeEncrypt);//RSAUtils.encryptData(beforeEncrypt, publicKey);

                encryp = addByte(encryp, encryptByte);
            }
            // 为了方便观察吧加密后的数据用base64加密转一下，要不然看起来是乱码,所以解密是也是要用Base64先转换
            return Base64.encodeToString(encryp, Base64.DEFAULT);
        } catch (Exception e) {
            return "Exception";
        }
    }

    /**
     * 解密一段文本
     *
     * @param needDecryptContent 待解密的文本
     * @return 解密后的明文
     */
    public String decryptData(String needDecryptContent) {
        try {
            String decryptStr = "";
            // 先Base64解密回来再给RSA解密
            byte[] beforeDecrypt = Base64.decode(needDecryptContent, Base64.DEFAULT);

            // 从文件中得到私钥
            int maxLength = rsaSize;
            //下边是分块解密的逻辑
            int byteListNumber = beforeDecrypt.length / maxLength;
            for (int i = 0; i < byteListNumber; i++) {
                byte[] block = Arrays.copyOfRange(beforeDecrypt, i * maxLength, i * maxLength + maxLength);

                byte[] decryptByte = opensslDecrypt(block);//RSAUtils.decryptData(block, privateKey);
                decryptStr += new String(decryptByte, "UTF-8");
            }

            return decryptStr;
        } catch (Exception e) {
            return "Exception";
        }
    }

    private byte[] addByte(byte[] data1, byte[] data2) {
        if (null == data1) {
            return data2;
        }
        if (null == data2) {
            return data1;
        }
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    static {
        System.loadLibrary("openssl-jni");
    }

    private native byte[] opensslEncrypt(byte[] data);

    private native byte[] opensslDecrypt(byte[] data);

    private native int opensslRsaSize();

}
