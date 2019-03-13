/*
 * Created 190312 lynnl
 *
 * Extended attribute syscalls support
 *
 * see:
 *  https://www.ibm.com/developerworks/java/tutorials/j-jni/j-jni.html
 *  http://journals.ecs.soton.ac.uk/java/tutorial/native1.1/implementing/sync.html
 *  https://github.com/IsNull/xattrj
 *  https://docs.oracle.com/javase/7/docs/api/java/nio/file/attribute/UserDefinedFileAttributeView.html
 */

package net.trineo.xattr4j;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public final class XAttr4J {
    static {
        try {
            LibLoader.loadlib("xattr4j");
            init();
        } catch (Exception e) {
            throw new RuntimeException("ERROR: Cannot load library", e);
        }
    }

    /*
     * Those flags can be found at:
     *  xnu/bsd/sys/xattr.h
     *
     * BSD internal require that xattr name should be valid UTF8 sequences(not empty)
     * see: xnu/bsd/vfs/vfs_xattr.c#xattr_validatename
     */

    /* Don't follow symbolic links */
    public static final int XATTR_NOFOLLOW = 0x0001;
    /* Set the value, fail if attr already exists */
    public static final int XATTR_CREATE   = 0x0002;
    /* Set the value, fail if attr does not exist */
    public static final int XATTR_REPLACE  = 0x0004;
    /*
     * option for f/getxattr() and f/listxattr()
     * to expose the HFS Compression extended attributes
     */
    public static final int XATTR_SHOWCOMPRESSION = 0x0020;

    private static final String UTF_8 = "UTF-8";

    private static void checkNotNull(Object ref) {
        if (ref == null) throw new NullPointerException();
    }

    private static byte[] getBytes(String str, String charsetName) throws UnsupportedEncodingException {
        checkNotNull(str);
        checkNotNull(charsetName);
        return str.getBytes(charsetName);
    }

    /**
     * @param str   string to be encoded
     * @return      array of UTF-8 bytes
     * @throws      UnsupportedEncodingException if encode unsupported
     */
    private static byte[] stringToUTF8Bytes(String str) throws UnsupportedEncodingException {
        return getBytes(str, UTF_8);
    }

    /**
     * Get an extended attribute value
     *
     * @param path      -
     * @param name      xattr name to retrieve
     * @param flags     retrieve options
     * @return          a byte array contains xattr value
     */
    public static byte[] getxattr(String path, String name, int flags) throws IOException {
        byte[] path1 = stringToUTF8Bytes(path);
        byte[] name1 = stringToUTF8Bytes(name);
        return _getxattr(path1, name1, flags);
    }

    public static byte[] getxattr(File file, String name, int flags) throws IOException {
        checkNotNull(file);
        return getxattr(file.getAbsolutePath(), name, flags);
    }

    /**
     * XXX: Assume value encoded in UTF-8(System.getProperty("file.encoding"))
     */
    public static void setxattr(String path, String name, byte[] value, int flags) throws IOException {
        checkNotNull(value);
        byte[] path1 = stringToUTF8Bytes(path);
        byte[] name1 = stringToUTF8Bytes(name);
        _setxattr(path1, name1, value, flags);
    }

    public static void setxattr(File file, String name, byte[] value, int flags) throws IOException {
        checkNotNull(file);
        setxattr(file.getAbsolutePath(), name, value, flags);
    }

    public static void setxattr(String path, String name, String value, int flags) throws IOException {
        checkNotNull(path);
        checkNotNull(name);
        byte[] value1 = stringToUTF8Bytes(value);
        setxattr(path, name, value1, flags);
    }

    public static void setxattr(File file, String name, String value, int flags) throws IOException {
        checkNotNull(file);
        setxattr(file.getAbsolutePath(), name, value, flags);
    }

    public static void removexattr(String path, String name, int flags) throws IOException {
        byte[] path1 = stringToUTF8Bytes(path);
        byte[] name1 = stringToUTF8Bytes(name);
        _removexattr(path1, name1, flags);
    }

    public static void removexattr(File file, String name, int flags) throws IOException {
        checkNotNull(file);
        removexattr(file.getAbsolutePath(), name, flags);
    }

    /**
     * List extended attribute names
     *
     * @param path      -
     * @param flags     listing options
     * Only XATTR_NOFOLLOW and XATTR_SHOWCOMPRESSION are supported
     *
     * @return          Array of exattr names(in arbitrary order)
     * if have no exattr                an empty array thus returned
     * if failed to retrieve exattr     IOException will throw
     */
    public static String[] listxattr(String path, int flags) throws IOException {
        byte[] path1 = stringToUTF8Bytes(path);
        return _listxattr(path1, flags);
    }

    public static String[] listxattr(File file, int flags) throws IOException {
        checkNotNull(file);
        return listxattr(file.getAbsolutePath(), flags);
    }

    /**
     * Fast wrapper of getxattr(2)
     * @return      size of the xattr value     possibly zero
     * @throws      IOException if I/O error or denoted xattr name do not exist
     *
     * This fast wrapper can be used to probe existence of a specific xattr
     */
    public static long sizexattr(String path, String name, int flags) throws IOException {
        byte[] path1 = stringToUTF8Bytes(path);
        byte[] name1 = stringToUTF8Bytes(name);
        return _sizexattr(path1, name1, flags);
    }

    public static long sizexattr(File file, String name, int flags) throws IOException {
        checkNotNull(file);
        return sizexattr(file.getAbsolutePath(), name, flags);
    }

    /* Should call in static block and call once */
    private static native void init();

    private static native byte[] _getxattr(byte[] path, byte[] name, int flags) throws IOException;

    private static native void _setxattr(byte[] path, byte[] name, byte[] value, int flags) throws IOException;

    private static native void _removexattr(byte[] path, byte[] name, int flags) throws IOException;

    private static native String[] _listxattr(byte[] path, int flags) throws IOException;

    /* Fast version of getxattr(2) */
    private static native long _sizexattr(byte[] path, byte[] name, int flags) throws IOException;
}
