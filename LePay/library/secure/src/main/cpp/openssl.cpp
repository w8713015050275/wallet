#include <jni.h>
#include <openssl/hmac.h>
#include <openssl/pem.h>
#include<openssl/err.h>
#include <openssl/rsa.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <malloc.h>

#include <android/log.h>


#ifdef __cplusplus
extern "C" {
#endif

#define LOG_TAG "LOG_TEST"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

//本地测试用公钥,服务器用来加密,
char publicKey[] = "-----BEGIN PUBLIC KEY-----\n"\
    "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC1St/YDeJKlf9+EaEwao1uC6IZ\n"\
    "zD5sub+4ASEQQlhefVTVbI825gLFS00YBRqrY0vJAlbtrqOrUMIpI5aBZkAity4m\n"\
    "x68Lx5quoDRjU2yUOm4fcM3HGBmUvfvHot4R7KFGq+GfaOviJkuwbKZ0ApbMl/Np\n"\
    "kC/qf9fBhSKTM7tytwIDAQAB\n"\
    "-----END PUBLIC KEY-----\n";


//服务器端公钥,作用是:客户端发送给服务器的数据加密
/**
char publicKey[] = "-----BEGIN PUBLIC KEY-----\n"\
    "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDNrMS1/XHrMeMdw2bL8oV4Wj88\n"\
    "IuAlMjSx/X9Mamz0UJu3P06JbXUQlb/eAUNd+yevC4cxww3U7kIyBvs3Wcjk4bd4\n"\
    "O3Ox1IcEaj8k6sA3kV6ziJX+A+i2WSM1e1kuw89TzagrSJ4tYNGTk1Z30Bq8QEyr\n"\
    "VAJy3h5MtWRCi0zRcwIDAQAB\n"\
    "-----END PUBLIC KEY-----\n";
    **/

//客户端私钥,本地用私钥来解密服务器发送过来的数据
char privateKey[] = "-----BEGIN RSA PRIVATE KEY-----\n"\
    "MIICXQIBAAKBgQC1St/YDeJKlf9+EaEwao1uC6IZzD5sub+4ASEQQlhefVTVbI82\n"\
    "5gLFS00YBRqrY0vJAlbtrqOrUMIpI5aBZkAity4mx68Lx5quoDRjU2yUOm4fcM3H\n"\
    "GBmUvfvHot4R7KFGq+GfaOviJkuwbKZ0ApbMl/NpkC/qf9fBhSKTM7tytwIDAQAB\n"\
    "AoGAVc6BkjZIE/tY+SeI/myAUkSpTcKRDU8Bgirupk5wmxCBFZ8TTe2iBBW3AJZx\n"\
    "4ArYdORplofhKZXIwEX613Y5BilbApD/wm31snXWF4828XZSbf7J6nOEtg803pbs\n"\
    "BvgVnZXFihLBQ/r6njZocG8kh2dV8aDgergJlOVwrP3SuckCQQDeRq4BRUfRsDHa\n"\
    "zSaNfwSURX1GNd1OVc0X0XNONMP5NNQ+eu0cYVg4vrCae1V7osFA94fU22igNvGl\n"\
    "MnB2Q8xNAkEA0Mxems0z0OAcIdttnsNoTgaYby03ZVUkwH4286RlFUfnyCqsdBQa\n"\
    "IXUORZK2WDu70J9CMcKF8j6V1MiTUtDtEwJAUj//RZMYZ1x2pkuBt5xH2oH4QFHQ\n"\
    "SZtwYB7uNwNJfV7yJwif0v+mgKD3gsKeuDhaoKq4BakHBzotov6OJQUjhQJBAMS/\n"\
    "v2nJcUki1P+GYD5ZTeEwT9V7WxbOa2pDxI5DW8j4z80LgSOEibb7MW6Xt+FesDT+\n"\
    "zQZMkaFaHh+6vTAtlScCQQDXOF/foObLtZk7oNU87MCtJkUIx9JIglI3EgAufX6t\n"\
    "a8J4NR4w3QcMZbLfFRFA3JBVbcopwQPBjMmf0CJ/Oc+5\n"\
    "-----END RSA PRIVATE KEY-----\n";

RSA *createRSA(unsigned char *key, int type) {
    RSA *rsa = NULL;
    BIO *keybio;
    keybio = BIO_new_mem_buf(key, -1);
    if (keybio == NULL) {
        return 0;
    }
    if (type) {
        rsa = PEM_read_bio_RSA_PUBKEY(keybio, &rsa, NULL, NULL);
    }
    else {
        rsa = PEM_read_bio_RSAPrivateKey(keybio, &rsa, NULL, NULL);
    }
    if (rsa == NULL) {
        printf("Failed to create RSA");
    }

    return rsa;
}

int public_encrypt(unsigned char *data, int data_len, unsigned char *key,
                   unsigned char *encrypted) {
    RSA *rsa = createRSA(key, 1);
    int result = RSA_public_encrypt(data_len, data, encrypted, rsa, RSA_PKCS1_PADDING);
    RSA_free(rsa);
    return result;
}
int private_decrypt(unsigned char *enc_data, int data_len, unsigned char *key,
                    unsigned char *decrypted) {
    RSA *rsa = createRSA(key, 0);
    int result = RSA_private_decrypt(data_len, enc_data, decrypted, rsa, RSA_PKCS1_PADDING);
    RSA_free(rsa);
    return result;
}


JNIEXPORT jint JNICALL Java_com_letv_wallet_utils_SslUtil_opensslRsaSize
        (JNIEnv *env, jclass) {

    RSA *rsa = createRSA((unsigned char *) publicKey, 1);
    int size = RSA_size(rsa);
    RSA_free(rsa);
    return size;
}

JNIEXPORT jbyteArray JNICALL Java_com_letv_wallet_utils_SslUtil_opensslEncrypt
        (JNIEnv *env, jclass, jbyteArray data) {
    jbyte *bytedata = env->GetByteArrayElements(data, 0);
    int text_length = env->GetArrayLength(data);


    unsigned char encrypted[128] = {};
    char *plainText = (char *) bytedata;
    int encrypted_length = public_encrypt((unsigned char *) plainText, text_length,
                                          (unsigned char *) publicKey, encrypted);
    //LOGI("aa测试之后加密的长度是%d", encrypted_length);
    int len = encrypted_length;
    jbyteArray jarrRV = env->NewByteArray(len);
    env->SetByteArrayRegion(jarrRV, 0, len, reinterpret_cast<jbyte *>(encrypted));
    env->ReleaseByteArrayElements(data, bytedata, 0);
    return jarrRV;

}

JNIEXPORT jbyteArray JNICALL Java_com_letv_wallet_utils_SslUtil_opensslDecrypt
        (JNIEnv *env, jclass, jbyteArray data) {
    //LOGI("解密过程开始------------------------------------------------------");
    jbyte *bytedata = env->GetByteArrayElements(data, 0);
    int encrypted_length = env->GetArrayLength(data);
    //LOGI("aa测试之后解密的之前密文的长度是%d", encrypted_length);
    unsigned char decrypted[128] = {};
    unsigned char *encrypted = (unsigned char *) bytedata;
    int decrypted_length = private_decrypt(encrypted, encrypted_length,
                                           (unsigned char *) privateKey, decrypted);
    //LOGI("aa测试之后解密的长度是%d,文本是%s", decrypted_length, decrypted);
    int len = decrypted_length;
    jbyteArray jarrRV = env->NewByteArray(len);
    env->SetByteArrayRegion(jarrRV, 0, len, reinterpret_cast<jbyte *>(decrypted));
    env->ReleaseByteArrayElements(data, bytedata, 0);
    return jarrRV;

}

#ifdef __cplusplus
}
#endif