import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

import net.trineo.xattr4j.XAttr4J;

import com.google.common.base.Preconditions;

class GetxattrTest {
    private static String stringFromUTF8Bytes(byte[] bytes) {
        Preconditions.checkNotNull(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {
        File f = new File("/tmp/" + UUID.randomUUID().toString());

        try {
            XAttr4J.getxattr(f, "", 0);
        } catch (IOException e) {
            /* Expect errno 2(ENOENT) */
            Preconditions.checkState(e.getMessage().contains(" errno: 2 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        try {
            XAttr4J.getxattr(f, "foobar", 0);
        } catch (IOException e) {
            /* Expect errno 2(ENOENT) */
            Preconditions.checkState(e.getMessage().contains(" errno: 2 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        try {
            XAttr4J.getxattr(f, "foobar", XAttr4J.XATTR_NOFOLLOW);
        } catch (IOException e) {
            /* Expect errno 2(ENOENT) */
            Preconditions.checkState(e.getMessage().contains(" errno: 2 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        try {
            XAttr4J.getxattr(f, "foobar", XAttr4J.XATTR_CREATE);
        } catch (IOException e) {
            /* Expect errno 2(ENOENT) */
            Preconditions.checkState(e.getMessage().contains(" errno: 2 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        try {
            XAttr4J.getxattr(f, "foobar", XAttr4J.XATTR_REPLACE);
        } catch (IOException e) {
            /* Expect errno 2(ENOENT) */
            Preconditions.checkState(e.getMessage().contains(" errno: 2 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        try {
            XAttr4J.getxattr(f, "foobar", XAttr4J.XATTR_SHOWCOMPRESSION);
        } catch (IOException e) {
            /* Expect errno 2(ENOENT) */
            Preconditions.checkState(e.getMessage().contains(" errno: 2 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        try {
            /* Passing an invalid options to getxattr(2) */
            XAttr4J.getxattr(f, "foobar", Integer.MAX_VALUE);
        } catch (IOException e) {
            /* Expect errno 22(EINVAL) */
            Preconditions.checkState(e.getMessage().contains(" errno: 22 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        try {
            /* Passing an invalid options to getxattr(2) */
            XAttr4J.getxattr("/private/etc/security/audit_user", "foobar", 0);
        } catch (IOException e) {
            /* Expect errno 13(EACCESS) */
            Preconditions.checkState(e.getMessage().contains(" errno: 13 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        boolean ok;
        ok = f.createNewFile();
        Preconditions.checkState(ok, "%s cannot be created", f);

        try {
            XAttr4J.getxattr(f, "", 0);
        } catch (IOException e) {
            /* Expect errno 22(EINVAL)  empty attribute name is impossible in macOS */
            Preconditions.checkState(e.getMessage().contains(" errno: 22 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        try {
            XAttr4J.getxattr(f, "foobar", 0);
        } catch (IOException e) {
            /* Expect errno 93(ENOATTR) */
            Preconditions.checkState(e.getMessage().contains(" errno: 93 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        try {
            XAttr4J.setxattr(f, "", "deadbeef", 0);
        } catch (IOException e) {
            /* Expect errno 22(EINVAL)  empty attribute name is impossible in macOS */
            Preconditions.checkState(e.getMessage().contains(" errno: 22 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        String[] ls;
        ls = XAttr4J.listxattr(f, 0);
        Preconditions.checkState(ls.length == 0, "Expect zero array length  got %s", ls.length);

        try {
            XAttr4J.setxattr(f, "foobar", "deadbeef", 0);
        } catch (IOException e) {
            /* Expect errno 22(EINVAL)  empty attribute name is impossible in macOS */
            Preconditions.checkState(e.getMessage().contains(" errno: 22 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        long sz = XAttr4J.sizexattr(f, "foobar", 0);
        Preconditions.checkState(sz == 8, "Expect xattr size 8  got %s", sz);

        try {
            XAttr4J.getxattr(f, "", 0);
        } catch (IOException e) {
            /* Expect errno 22(EINVAL)  empty attribute name is impossible in macOS */
            Preconditions.checkState(e.getMessage().contains(" errno: 22 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        byte[] val;
        String str;
        val = XAttr4J.getxattr(f, "foobar", 0);
        str = stringFromUTF8Bytes(val);
        Preconditions.checkState(str.equals("deadbeef"), "Bad xattr value %s", str);

        ls = XAttr4J.listxattr(f, 0);
        Preconditions.checkState(ls.length == 1, "Expect array length 1  got %s", ls.length);
        Preconditions.checkState(ls[0].equals("foobar"));

        XAttr4J.removexattr(f, "foobar", 0);

        try {
            XAttr4J.getxattr(f, "foobar", 0);
        } catch (IOException e) {
            /* Expect errno 93(ENOATTR) */
            Preconditions.checkState(e.getMessage().contains(" errno: 93 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        // Force remove a doesn't exist xattr(shouldn't throw IOException)
        XAttr4J.removexattr(f, "foobar", 0, true);

        try {
            XAttr4J.removexattr(f, "", 0, true);
        } catch (IOException e) {
            /* Expect errno 22(EINVAL)  empty attribute name isn't tolerant even if you turned on force flag */
            Preconditions.checkState(e.getMessage().contains(" errno: 22 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        ls = XAttr4J.listxattr(f, 0);
        Preconditions.checkState(ls.length == 0, "Expect zero array length  got %s", ls.length);

        boolean exists = XAttr4J.existxattr(f, "foobar", 0);
        Preconditions.checkState(!exists, "Why xattr foobar still exists?!");

        ok = f.delete();
        Preconditions.checkState(ok, "%s cannot be deleted", f);

        // Shouldn't throw IOException even if backing file doesn't exist
        //  this works somewhat like rm(1) -f
        XAttr4J.removexattr(f, "foobar", 0, true);

        try {
            XAttr4J.getxattr(f, "foobar", 0);
        } catch (IOException e) {
            /* Expect errno 2(ENOENT) */
            Preconditions.checkState(e.getMessage().contains(" errno: 2 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        try {
            ls = XAttr4J.listxattr(f, 0);
        } catch (IOException e) {
            /* Expect errno 2(ENOENT) */
            Preconditions.checkState(e.getMessage().contains(" errno: 2 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\nXXX: Pass!");
    }
}
