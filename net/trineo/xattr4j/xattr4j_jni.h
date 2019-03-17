/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class net_trineo_xattr4j_XAttr4J */

#ifndef _Included_net_trineo_xattr4j_XAttr4J
#define _Included_net_trineo_xattr4j_XAttr4J
#ifdef __cplusplus
extern "C" {
#endif
#undef net_trineo_xattr4j_XAttr4J_XATTR_NOFOLLOW
#define net_trineo_xattr4j_XAttr4J_XATTR_NOFOLLOW 1L
#undef net_trineo_xattr4j_XAttr4J_XATTR_CREATE
#define net_trineo_xattr4j_XAttr4J_XATTR_CREATE 2L
#undef net_trineo_xattr4j_XAttr4J_XATTR_REPLACE
#define net_trineo_xattr4j_XAttr4J_XATTR_REPLACE 4L
#undef net_trineo_xattr4j_XAttr4J_XATTR_SHOWCOMPRESSION
#define net_trineo_xattr4j_XAttr4J_XATTR_SHOWCOMPRESSION 32L
/*
 * Class:     net_trineo_xattr4j_XAttr4J
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_trineo_xattr4j_XAttr4J_init
  (JNIEnv *, jclass);

/*
 * Class:     net_trineo_xattr4j_XAttr4J
 * Method:    _getxattr
 * Signature: ([B[BI)[B
 */
JNIEXPORT jbyteArray JNICALL Java_net_trineo_xattr4j_XAttr4J__1getxattr
  (JNIEnv *, jclass, jbyteArray, jbyteArray, jint);

/*
 * Class:     net_trineo_xattr4j_XAttr4J
 * Method:    _setxattr
 * Signature: ([B[B[BI)V
 */
JNIEXPORT void JNICALL Java_net_trineo_xattr4j_XAttr4J__1setxattr
  (JNIEnv *, jclass, jbyteArray, jbyteArray, jbyteArray, jint);

/*
 * Class:     net_trineo_xattr4j_XAttr4J
 * Method:    _removexattr
 * Signature: ([B[BIZ)V
 */
JNIEXPORT void JNICALL Java_net_trineo_xattr4j_XAttr4J__1removexattr
  (JNIEnv *, jclass, jbyteArray, jbyteArray, jint, jboolean);

/*
 * Class:     net_trineo_xattr4j_XAttr4J
 * Method:    _listxattr
 * Signature: ([BI)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_net_trineo_xattr4j_XAttr4J__1listxattr
  (JNIEnv *, jclass, jbyteArray, jint);

/*
 * Class:     net_trineo_xattr4j_XAttr4J
 * Method:    _sizexattr
 * Signature: ([B[BI)J
 */
JNIEXPORT jlong JNICALL Java_net_trineo_xattr4j_XAttr4J__1sizexattr
  (JNIEnv *, jclass, jbyteArray, jbyteArray, jint);

/*
 * Class:     net_trineo_xattr4j_XAttr4J
 * Method:    _existxattr
 * Signature: ([B[BI)Z
 */
JNIEXPORT jboolean JNICALL Java_net_trineo_xattr4j_XAttr4J__1existxattr
  (JNIEnv *, jclass, jbyteArray, jbyteArray, jint);

#ifdef __cplusplus
}
#endif
#endif
