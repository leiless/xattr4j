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
     * Those options can be found at:
     *  xnu/bsd/sys/xattr.h
     *
     * BSD internal require that xattr name should be valid UTF8 sequences(not empty)
     * see: xnu/bsd/vfs/vfs_xattr.c#xattr_validatename
     */

    /** Don't follow symbolic links */
    public static final int XATTR_NOFOLLOW = 0x0001;
    /** Set the value, fail if attr already exists */
    public static final int XATTR_CREATE   = 0x0002;
    /** Set the value, fail if attr does not exist */
    public static final int XATTR_REPLACE  = 0x0004;
    /** Expose HFS+ Compression extended attributes */
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
     * @param path          File path
     * @param name          Extended attribute name
     * @param options       getxattr(2) options
     * @return              A byte array contains extended attribute value
     * @throws IOException  If getxattr(2) operation failed
     */
    public static byte[] getxattr(String path, String name, int options) throws IOException {
        byte[] path1 = stringToUTF8Bytes(path);
        byte[] name1 = stringToUTF8Bytes(name);
        return _getxattr(path1, name1, options);
    }

    /**
     * @see XAttr4J#getxattr(String, String, int)
     */
    public static byte[] getxattr(File file, String name, int options) throws IOException {
        checkNotNull(file);
        return getxattr(file.getAbsolutePath(), name, options);
    }

    /**
     * Get an extended attribute value(fd-oriented)
     * @see XAttr4J#getxattr(String, String, int)
     */
    public static byte[] fgetxattr(int fd, String name, int options) throws IOException {
        return _fgetxattr(fd, stringToUTF8Bytes(name), options);
    }

    /**
     * Set an extended attribute value
     *
     * @param path          File path
     * @param name          Extended attribute name
     * @param value         Extended attribute value to set
     * @param options       setxattr(2) options
     * @throws IOException  If setxattr(2) operation failed
     *
     * XXX: Assume value encoded in UTF-8(System.getProperty("file.encoding"))
     */
    public static void setxattr(String path, String name, byte[] value, int options) throws IOException {
        checkNotNull(value);
        byte[] path1 = stringToUTF8Bytes(path);
        byte[] name1 = stringToUTF8Bytes(name);
        _setxattr(path1, name1, value, options);
    }

    /**
     * @see XAttr4J#setxattr(String, String, byte[], int)
     */
    public static void setxattr(File file, String name, byte[] value, int options) throws IOException {
        checkNotNull(file);
        setxattr(file.getAbsolutePath(), name, value, options);
    }

    /**
     * @see XAttr4J#setxattr(String, String, byte[], int)
     */
    public static void setxattr(String path, String name, String value, int options) throws IOException {
        checkNotNull(path);
        checkNotNull(name);
        byte[] value1 = stringToUTF8Bytes(value);
        setxattr(path, name, value1, options);
    }

    /**
     * @see XAttr4J#setxattr(String, String, byte[], int)
     */
    public static void setxattr(File file, String name, String value, int options) throws IOException {
        checkNotNull(file);
        setxattr(file.getAbsolutePath(), name, value, options);
    }

    /**
     * Set an extended attribute value(fd-oriented)
     */
    public static void fsetxattr(int fd, String name, byte[] value, int options) throws IOException {
        checkNotNull(value);
        _fsetxattr(fd, stringToUTF8Bytes(name), value, options);
    }

    /**
     * @see XAttr4J#fsetxattr(int, String, byte[], int)
     */
    public static void fsetxattr(int fd, String name, String value, int options) throws IOException {
        fsetxattr(fd, name, stringToUTF8Bytes(value), options);
    }

    /**
     * Remove an extended attribute value
     *
     * @param path          File path
     * @param name          Extended attribute name
     * @param options       removexattr(2) options
     * @param force         Flag to indicate don't throw IOException when path don't exist or name don't exist
     * @throws IOException  If removexattr(2) operation failed
     */
    public static void removexattr(String path, String name, int options, boolean force) throws IOException {
        byte[] path1 = stringToUTF8Bytes(path);
        byte[] name1 = stringToUTF8Bytes(name);
        _removexattr(path1, name1, options, force);
    }

    /**
     * @see XAttr4J#removexattr(String, String, int, boolean)
     */
    public static void removexattr(File file, String name, int options, boolean force) throws IOException {
        checkNotNull(file);
        removexattr(file.getAbsolutePath(), name, options, force);
    }

    /**
     * @see XAttr4J#removexattr(String, String, int, boolean)
     */
    public static void removexattr(String path, String name, int options) throws IOException {
        removexattr(path, name, options, false);
    }

    /**
     * @see XAttr4J#removexattr(String, String, int, boolean)
     */
    public static void removexattr(File file, String name, int options) throws IOException {
        removexattr(file, name, options, false);
    }

    /**
     * Remove an extended attribute(fd-oriented)
     * @see XAttr4J#removexattr(String, String, int, boolean)
     */
    public static void fremovexattr(int fd, String name, int options, boolean force) throws IOException {
        _fremovexattr(fd, stringToUTF8Bytes(name), options, force);
    }

    /**
     * @see XAttr4J#fremovexattr(int, String, int, boolean)
     */
    public static void fremovexattr(int fd, String name, int options) throws IOException {
        fremovexattr(fd, name, options, false);
    }

    /**
     * List extended attribute names
     *
     * @param path          File path
     * @param options       listxattr(2) options(Only XATTR_NOFOLLOW, XATTR_SHOWCOMPRESSION supported)
     * @return              An array of extended attributes(in arbitrary order, empty array is possible)
     * @throws IOException  If listxattr(2) operation failed
     */
    public static String[] listxattr(String path, int options) throws IOException {
        byte[] path1 = stringToUTF8Bytes(path);
        return _listxattr(path1, options);
    }

    /**
     * @see XAttr4J#listxattr(String, int)
     */
    public static String[] listxattr(File file, int options) throws IOException {
        checkNotNull(file);
        return listxattr(file.getAbsolutePath(), options);
    }

    /**
     * List extended attribute names(fd-oriented)
     * @see XAttr4J#listxattr(String, int)
     */
    public static String[] flistxattr(int fd, int options) throws IOException {
        return _flistxattr(fd, options);
    }

    /**
     * Get an extended attribute value size(fast wrapper of getxattr(2))
     *
     * @param path          File path
     * @param name          Extended attribute name
     * @param options       getxattr(2) options
     * @return              Size of the extended attribute value(possibly zero)
     * @throws IOException  If getxattr(2) operation failed
     */
    public static long sizexattr(String path, String name, int options) throws IOException {
        byte[] path1 = stringToUTF8Bytes(path);
        byte[] name1 = stringToUTF8Bytes(name);
        return _sizexattr(path1, name1, options);
    }

    /**
     * @see XAttr4J#sizexattr(String, String, int)
     */
    public static long sizexattr(File file, String name, int options) throws IOException {
        checkNotNull(file);
        return sizexattr(file.getAbsolutePath(), name, options);
    }

    /**
     * Get an extended attribute value size(fast wrapper of fgetxattr(2))
     * @see XAttr4J#sizexattr(String, String, int)
     */
    public static long fsizexattr(int fd, String name, int options) throws IOException {
        return _fsizexattr(fd, stringToUTF8Bytes(name), options);
    }

    /**
     * Check if an extended attribute exists
     *
     * @param path          File path
     * @param name          Extended attribute name
     * @param options       getxattr(2) options
     * @return              true if given extended attribute exists  false o.w.
     * @throws IOException  If getxattr(2) operation failed
     *                      If given path doesn't exist  IOException will throw
     */
    public static boolean existxattr(String path, String name, int options) throws IOException {
        byte[] path1 = stringToUTF8Bytes(path);
        byte[] name1 = stringToUTF8Bytes(name);
        return _existxattr(path1, name1, options);
    }

    /**
     * @see XAttr4J#existxattr(String, String, int)
     */
    public static boolean existxattr(File file, String name, int options) throws IOException {
        checkNotNull(file);
        return existxattr(file.getAbsolutePath(), name, options);
    }

    /**
     * Check if an extended attribute exists(fd-oriented)
     * @see XAttr4J#existxattr(String, String, int)
     */
    public static boolean fexistxattr(int fd, String name, int options) throws IOException {
        return _fexistxattr(fd, stringToUTF8Bytes(name), options);
    }

    /* Should call in static block and call once */
    private static native void init();

    private static native byte[] _getxattr(byte[] path, byte[] name, int options) throws IOException;
    private static native byte[] _fgetxattr(int fd, byte[] name, int options) throws IOException;

    private static native void _setxattr(byte[] path, byte[] name, byte[] value, int options) throws IOException;
    private static native void _fsetxattr(int fd, byte[] name, byte[] value, int options) throws IOException;

    private static native void _removexattr(byte[] path, byte[] name, int options, boolean force) throws IOException;
    private static native void _fremovexattr(int fd, byte[] name, int options, boolean force) throws IOException;

    private static native String[] _listxattr(byte[] path, int options) throws IOException;
    private static native String[] _flistxattr(int fd, int options) throws IOException;

    /* Fast version of getxattr(2) */
    private static native long _sizexattr(byte[] path, byte[] name, int options) throws IOException;
    /* Fast version of fgetxattr(2) */
    private static native long _fsizexattr(int fd, byte[] name, int options) throws IOException;

    private static native boolean _existxattr(byte[] path, byte[] name, int options) throws IOException;
    private static native boolean _fexistxattr(int fd, byte[] name, int options) throws IOException;
}
