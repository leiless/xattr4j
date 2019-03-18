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

        try {
            XAttr4J.setxattr(f, "foobar", "deadbeef", 0);
        } catch (IOException e) {
            /* Expect errno 22(EINVAL)  empty attribute name is impossible in macOS */
            Preconditions.checkState(e.getMessage().contains(" errno: 22 "), "Unexpected exception message: %s", e.getMessage());
            e.printStackTrace();
        }

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

        ok = f.delete();
        Preconditions.checkState(ok, "%s cannot be deleted", f);

        System.out.println("Pass!");
    }
}
