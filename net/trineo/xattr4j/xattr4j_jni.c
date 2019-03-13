/*
 * Created: 170721
 *
 * C code implementation of extended attribute syscalls
 *
 * see:
 *  xdprof.sourceforge.net/doxygen/jni_8h-source.html
 *  xdprof.sourceforge.net/doxygen/jni__md_8h-source.html
 *  docs.oracle.com/javase/8/docs/technotes/guides/jni/spec/functions.html
 *  ptolemy.berkeley.edu/~johnr/tutorials/assertions.html
 */

#include <jni.h>
#include <stdio.h>
#include <assert.h>         /* -DNDEBUG to disable runtime assertion */
#include <errno.h>          /* for DEBUG */
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/xattr.h>
#include "xattr4j_jni.h"

/* Used to indicate unused function parameters */
#define UNUSED(arg, ...)        (void) ((void) (arg), ##__VA_ARGS__)

/**
 * Compile-time assertion  see: linux/arch/x86/boot/boot.h
 */
#ifdef DEBUG
#define BUILD_BUG_ON(cond)      UNUSED(sizeof(char[-!(cond)]))
#else
#define BUILD_BUG_ON(cond)      UNUSED(cond)
#endif

#ifdef DEBUG
#define LOG(fmt, ...) fprintf(stderr, "[xattr4j]: " fmt "\n", ##__VA_ARGS__)
#else
#define LOG(fmt, ...) UNUSED(fmt, ##__VA_ARGS__)
#endif

/**
 * Get a NUL-terminated native C string from Java byte[]
 * @return      C char array    NULL if OOM(errno will set)
 *              Must be free via free(3)
 */
static char *get_cstr_bytes(JNIEnv *env, jbyteArray jbarr)
{
    char *buff = NULL;
    jsize sz;
    jbyte *arr;

    arr = (*env)->GetByteArrayElements(env, jbarr, NULL);
    if (arr == NULL) goto get1;

    sz = (*env)->GetArrayLength(env, jbarr);
    /* ENOMEM is the only possible errno from malloc(2) */
    buff = (char *) malloc(sz + 1);
    if (buff != NULL) {
        memcpy(buff, arr, sz);
        buff[sz] = '\0';
    }

    (*env)->ReleaseByteArrayElements(env, jbarr, arr, JNI_ABORT);
get1:
    if (buff == NULL) errno = ENOMEM;
    return buff;
}

static jclass java_lang_String;
static jclass java_io_IOException;

/**
 * Initialize non-direct JNI functionalities
 *
 * see: javap -s java.lang.String
 * exception will be throw if any error
 */
JNIEXPORT void JNICALL
Java_net_trineo_xattr4j_XAttr4J_init(
        JNIEnv *env,
        jclass cls)
{
    BUILD_BUG_ON(sizeof(char) == sizeof(jbyte));

    java_lang_String = (*env)->FindClass(env, "java/lang/String");
    if (java_lang_String == NULL) return;
    java_lang_String = (*env)->NewGlobalRef(env, java_lang_String);
    assert(java_lang_String != NULL);

    java_io_IOException = (*env)->FindClass(env, "java/io/IOException");
    if (java_io_IOException == NULL) return;
    java_io_IOException = (*env)->NewGlobalRef(env, java_io_IOException);
    assert(java_io_IOException != NULL);
}

/**
 * Strong version of atomic compare-and-swap
 * @p           pointer to cas with
 * @o           old value
 * @n           new value
 * @return      true if success  false o.w.
 * see: http://donghao.org/2015/01/30/128bit-atomic-operation-in-arm64
 *
 * NOTE: GCC-compatible available only
 */
#define atomic_cas(p, o, n) ({  \
    __typeof(*(p)) t = (o);     \
    __atomic_compare_exchange_n(p, &t, n, 0, __ATOMIC_SEQ_CST, __ATOMIC_SEQ_CST); \
})

static int __k = 0;
#define spin_lock()     while (!atomic_cas(&__k, 0, 1)) continue
#define spin_unlock()   do {            \
    int ok = atomic_cas(&__k, 1, 0);    \
    assert(ok);                         \
} while (0)

#define EXC_BUFSZ 2048
static char exc_buff[EXC_BUFSZ];

/**
 * __FILE__ info is ignored since it's trivial
 *
 * __func__ info not attach to the static buffer
 *  since the stack trace already done that for us
 *
 * XXX: errno must be set properly before call
 */
static void throw_exc(JNIEnv *env, jclass cls, const char *msg, int line)
{
    int sz;
    jint e;

    spin_lock();
    /*
     * We use static buffer simply :. the exception itself may caused by OOM
     *  in such case further malloc(3) will fail again
     */
    sz = snprintf(exc_buff, EXC_BUFSZ,
                "(native) %s: line: %d errno: %d", msg, line, errno);

    e = (*env)->ThrowNew(env, cls, sz > 0 ? exc_buff : msg);
    spin_unlock();

    /* We should fail if exception cannot be throw */
    assert(e == 0);
}

static void throw_ioexc0(JNIEnv *env, const char *msg, int line)
{
    assert(java_io_IOException != NULL);
    throw_exc(env, java_io_IOException, msg, line);
}

#define throw_ioexc(env, msg) throw_ioexc0(env, msg, __LINE__)

/*
 * XXX: When xattr data sized zero  we should return new byte[0] instead of null
 * @throws      IOException if internal failure
 */
JNIEXPORT jbyteArray JNICALL
Java_net_trineo_xattr4j_XAttr4J__1getxattr(
        JNIEnv *env,
        jclass cls,
        jbyteArray jbpath,
        jbyteArray jbname,
        jint flags)
{
    jbyteArray out = NULL;
    char *path;
    char *name;
    jbyte *buff;
    ssize_t len;
    ssize_t len2;

    path = get_cstr_bytes(env, jbpath);
    if (path == NULL) goto get1;

    name = get_cstr_bytes(env, jbname);
    if (name == NULL) goto get2;

out_replay:
    len = getxattr(path, name, NULL, 0, 0, flags);
    if (len < 0) goto get3;

    /*
     * malloc(0) have implementation-defined behaviour
     * BSD malloc(3) won't fail in such case
     * see: Open Group Base Specifications - malloc(3)
     */
    if (len != 0) {
        buff = (jbyte *) malloc(len);
        if (buff == NULL) goto get3;
    } else {
        buff = NULL;
    }

    len2 = getxattr(path, name, buff, len, 0, flags);
    if (len2 < 0) {
        /* [NS-5301] potential TOCTTOU BUG */
        if (errno == ERANGE) {
            free(buff);
            LOG("TOCTTOU BUG in getxattr()  old size: %zd errno: %d", len, errno);
            goto out_replay;
        }

        goto get4;
    }

    len = len2;     /* NOTE: 0 <= len2 <= len */

    /* zero is a valid size in JVM allocator */
    out = (*env)->NewByteArray(env, len);
    if (out != NULL) {
        (*env)->SetByteArrayRegion(env, out, 0, len, buff);
    } else {
        errno = ENOMEM;
    }

get4:
    free(buff);
get3:
    free(name);
get2:
    free(path);
get1:
    if (out == NULL) throw_ioexc(env, "getxattr failure");
    return out;
}

JNIEXPORT void JNICALL
Java_net_trineo_xattr4j_XAttr4J__1setxattr(
        JNIEnv *env,
        jclass cls,
        jbyteArray jbpath,
        jbyteArray jbname,
        jbyteArray jbvalue,
        jint flags)
{
    int ok = 0;
    char *path;
    char *name;
    jbyte *value;
    jsize sz;

    path = get_cstr_bytes(env, jbpath);
    if (path == NULL) goto get1;
    name = get_cstr_bytes(env, jbname);
    if (name == NULL) goto get2;
    value = (*env)->GetByteArrayElements(env, jbvalue, NULL);
    if (value == NULL) {
        errno = EINVAL;
        goto get3;
    }
    sz = (*env)->GetArrayLength(env, jbvalue);

    ok = !setxattr(path, name, value, sz, 0, flags);

    (*env)->ReleaseByteArrayElements(env, jbvalue, value, JNI_ABORT);
get3:
    free(name);
get2:
    free(path);
get1:
    if (!ok) throw_ioexc(env, "setxattr failure");
}

JNIEXPORT void JNICALL
Java_net_trineo_xattr4j_XAttr4J__1removexattr(
        JNIEnv *env,
        jclass cls,
        jbyteArray jbpath,
        jbyteArray jbname,
        jint flags)
{
    int ok = 0;
    char *path;
    char *name;

    path = get_cstr_bytes(env, jbpath);
    if (path == NULL) goto get1;
    name = get_cstr_bytes(env, jbname);
    if (name == NULL) goto get2;

    ok = !removexattr(path, name, flags);

    free(name);
get2:
    free(path);
get1:
    if (!ok) throw_ioexc(env, "removexattr failure");
}

JNIEXPORT jobjectArray JNICALL
Java_net_trineo_xattr4j_XAttr4J__1listxattr(
        JNIEnv *env,
        jclass cls,
        jbyteArray jbpath,
        jint flags)
{
    jobjectArray arr;
    char *path;
    ssize_t sz;
    ssize_t sz2;
    char *namebuf;
    char *cursor;
    size_t cnt, i;
    jstring *jnamebuf;

    assert(java_lang_String != NULL);

    arr = NULL;

    path = get_cstr_bytes(env, jbpath);
    if (path == NULL) goto out1;

out_replay:
    sz = listxattr(path, NULL, 0, flags);
    if (sz < 0) goto out2;
    if (sz == 0) {
        arr = (*env)->NewObjectArray(env, 0, java_lang_String, NULL);
        if (arr == NULL) errno = ENOMEM;
        goto out2;
    }

    namebuf = (char *) malloc(sz);
    if (namebuf == NULL) goto out2;

    sz2 = listxattr(path, namebuf, sz, flags);
    if (sz2 < 0) {
        /* [NS-5301] potential TOCTTOU BUG */
        if (errno == ERANGE) {
            free(namebuf);
            LOG("TOCTTOU BUG in listxattr()  old size: %zd errno: %d", sz, errno);
            goto out_replay;
        }

        goto out3;
    }

    sz = sz2;       /* NOTE: 0 <= sz2 <= sz */
    if (sz == 0) {
        arr = (*env)->NewObjectArray(env, 0, java_lang_String, NULL);
        if (arr == NULL) errno = ENOMEM;
        goto out3;
    }

    cnt = 0;
    cursor = namebuf;
    while (cursor - namebuf < sz) {
        cnt++;
        cursor += strlen(cursor) + 1;
    }

    jnamebuf = (jstring *) malloc(sizeof(jstring *) * cnt);
    if (jnamebuf == NULL) goto out3;

    cursor = namebuf;
    for (i = 0; i < cnt; i++) {
        jnamebuf[i] = (*env)->NewStringUTF(env, cursor);
        if (jnamebuf[i] == NULL) {
            errno = ENOMEM;
            goto out4;
        }
        cursor += strlen(cursor) + 1;
    }

    arr = (*env)->NewObjectArray(env, cnt, java_lang_String, NULL);
    if (arr == NULL) {
        errno = ENOMEM;
        goto out4;
    }
    for (i = 0; i < cnt; i++) {
        (*env)->SetObjectArrayElement(env, arr, i, jnamebuf[i]);
        (*env)->DeleteLocalRef(env, jnamebuf[i]);
    }

out4:
    free(jnamebuf);
out3:
    free(namebuf);
out2:
    free(path);
out1:
    if (arr == NULL) throw_ioexc(env, "listxattr failure");
    return arr;
}

/**
 * Fast wrapper of getxattr(2)
 * @return      size of the xattr value     possibly zero
 * @throws      IOException if I/O error or denoted xattr name do not exist
 *
 * This fast wrapper can be used to probe existence of a specific xattr
 */
JNIEXPORT jlong JNICALL
Java_net_trineo_xattr4j_XAttr4J__1sizexattr(
        JNIEnv *env,
        jclass cls,
        jbyteArray jbpath,
        jbyteArray jbname,
        jint flags)
{
    jlong sz = -1;  /* -1 isn't a valid size */
    char *path;
    char *name;

    path = get_cstr_bytes(env, jbpath);
    if (path == NULL) goto get1;
    name = get_cstr_bytes(env, jbname);
    if (name == NULL) goto get2;

    sz = (jlong) getxattr(path, name, NULL, 0, 0, flags);

    free(name);
get2:
    free(path);
get1:
    if (sz < 0) throw_ioexc(env, "sizexattr(getxattr) failure");
    return sz;
}

