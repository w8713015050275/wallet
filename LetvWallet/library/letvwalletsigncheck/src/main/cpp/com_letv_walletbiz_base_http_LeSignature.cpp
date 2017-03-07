#include "com_letv_walletbiz_base_http_LeSignature.h"
#include <openssl/evp.h>
#include <openssl/hmac.h>
#include <openssl/pem.h>
#include <openssl/rsa.h>
#include <openssl/err.h>
#include <stdio.h>
#include <string.h>
#include "Log.h"
#include <stdlib.h>

#define HAMC_EVP_MAX_SIZE 128


unsigned char *HMAC(const EVP_MD *evp_md, const void *key, int key_len,
                    const unsigned char *d, int n, unsigned char *md,
                    unsigned int *md_len) {
    HMAC_CTX c;


    if (md == NULL) {
        return NULL;
    }

    HMAC_CTX_init(&c);

    HMAC_Init(&c, key, key_len, evp_md);

    HMAC_Update(&c, d, n);

    HMAC_Final(&c, md, md_len);

    HMAC_CTX_cleanup(&c);

    return (md);
}

unsigned char *decrypt() {
    unsigned char *p_de = NULL;
    RSA *p_rsa = NULL;
    int rsa_len;
    BIO *bio = NULL;
    int i;

    const unsigned char en_in[] = {0xb0, 0x53, 0x28, 0x74, 0xa7, 0x07, 0x3f, 0x43,
                                   0x85, 0x31, 0x52, 0x71, 0x08, 0x7e, 0xab, 0xb8,
                                   0x17, 0x99, 0x1e, 0x9f, 0x42, 0x3d, 0xce, 0x44,
                                   0xaf, 0x59, 0x88, 0xc7, 0xba, 0xa3, 0xb0, 0x0d,
                                   0xc9, 0xe2, 0x1a, 0x09, 0x27, 0x15, 0x99, 0x1a,
                                   0x07, 0x04, 0x12, 0x41, 0x82, 0x21, 0x34, 0x31,
                                   0x40, 0x2e, 0xac, 0xc6, 0x65, 0xf6, 0x35, 0x63,
                                   0x6f, 0xd4, 0x28, 0xf6, 0x6e, 0x99, 0xee, 0x7b,
                                   0x9d, 0xbe, 0x22, 0x0f, 0xa1, 0x58, 0x24, 0x25,
                                   0x35, 0x2f, 0xac, 0x1f, 0xa9, 0x49, 0xd2, 0xcf,
                                   0xec, 0xa7, 0xea, 0x9a, 0x70, 0x3d, 0xec, 0xaa,
                                   0x65, 0xc1, 0x54, 0x3c, 0x4b, 0x8d, 0xa5, 0x60,
                                   0x4d, 0xf0, 0xa8, 0xcb, 0x4f, 0xe0, 0x7a, 0x07,
                                   0xe7, 0xcc, 0xd6, 0x46, 0xf0, 0x0e, 0x46, 0x0f,
                                   0x2c, 0xac, 0xeb, 0x3f, 0xca, 0xba, 0xe5, 0x1e,
                                   0x1b, 0xb6, 0x75, 0x35, 0xad, 0x0a, 0x9d, 0xbe};

    const char *pub = "-----BEGIN RSA PUBLIC KEY-----\n"\
    "MIGJAoGBAML4C+v9u5UezTTuFkqHGtQGnLkm9JJRrqxGkQ8zG8st3HIzloKR95Ur\n"\
    "+UBrokDYesgwEiWm15flfPMB5B74WVgPAeWOdTZFEeRDMGV/DuGcolOS8ZUFGpwe\n"\
    "fp23y3WqKe1AIp1E1Ock/bmnLj/rmrc9aL+6bN1b1p/qqTbMdd27AgMBAAE=\n"\
    "-----END RSA PUBLIC KEY-----";

    bio = BIO_new(BIO_s_mem());
    BIO_puts(bio, pub);
    p_rsa = PEM_read_bio_RSAPublicKey(bio, &p_rsa, NULL, NULL);
    BIO_free(bio);
    if (p_rsa == NULL) {
        ALOGI("LeSignature dec RSA == null\n");
        return NULL;
    }

    rsa_len = RSA_size(p_rsa);
    p_de = (unsigned char *) malloc(rsa_len + 1);
    memset(p_de, 0, rsa_len + 1);

    if (RSA_public_decrypt(rsa_len, en_in, (unsigned char *) p_de, p_rsa, RSA_NO_PADDING) < 0) {
        ALOGI("LeSignature dec fail\n");
        RSA_free(p_rsa);
        free(p_de);
        return NULL;
    }

    RSA_free(p_rsa);
    ALOGI("LeSignature dec de(%s)", (char *) p_de);

    return p_de;
}


static void logSign(const unsigned char *pszSrc, int nLen) {
#ifdef WDEBUG
    char m[EVP_MAX_MD_SIZE *2];
    for (int i = 0; i < nLen; i++){
        sprintf(m + i*2, "%02X", (unsigned char)pszSrc[i]);
    }
     ALOGI("LeSignature_jni sign (%s)\n",m);
#endif
}

/*
 * Class:     com_letv_wallet_base_http_LeSignature
 * Method:    jni_hmacsha1
 * Signature: (Ljava/lang/String;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_letv_walletbiz_base_http_LeSignature_jni_1hmacsha1
        (JNIEnv *env, jclass, jstring strToSign, jboolean isTest) {
    unsigned int mlen = 0;
    unsigned char *toSign;
    unsigned char *md;
    unsigned char *sk;
    unsigned char m[EVP_MAX_MD_SIZE];

    unsigned char b = isTest;

    if (b) {
        char *testsk = (char *) "4hkjwuWDNzRRZGcyC8uk";
        int len = strlen(testsk);
        sk = (unsigned char *) malloc(len + 1);
        memcpy(sk, testsk, len + 1);
        ALOGI("LeSignature testsk(%s)", sk);
    } else {
        sk = decrypt();
    }


    if (sk == NULL)
        return NULL;

    toSign = (unsigned char *) env->GetStringUTFChars(strToSign, 0);
    ALOGI("LeSignature_jni_hmacsha1 %s(%s)\n", toSign, sk);

    md = HMAC(EVP_sha1(), sk, strlen(reinterpret_cast<char *>(sk)),
              toSign, strlen(reinterpret_cast<char *>(toSign)), m, &mlen);
    ALOGI("LeSignature_jni_hmacsha1 md(%p, %d)\n", md, mlen);

    free(sk);
    if (mlen == 0) {
        return NULL;
    }

    logSign(md, mlen);

    jbyteArray byteArray = env->NewByteArray(mlen);
    if (byteArray == NULL) {
        return NULL;
    }
    env->SetByteArrayRegion(byteArray, 0, mlen, (jbyte *) md);

    return byteArray;
}

