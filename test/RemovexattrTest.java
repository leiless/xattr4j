package test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

import net.trineo.xattr4j.XAttr4J;

import com.google.common.base.Preconditions;

class RemovexattrTest {
    private static String stringFromUTF8Bytes(byte[] bytes) {
        Preconditions.checkNotNull(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static byte[] stringToUTF8Bytes(String str) {
        Preconditions.checkNotNull(str);
        return str.getBytes(StandardCharsets.UTF_8);
    }

    private static void noFail(boolean expr) {
        Preconditions.checkState(expr, "Operation failed  cannot proceed");
    }

    private static void assertExMsgContains(Exception e, String str) {
        Preconditions.checkState(e.getMessage().contains(str),
                "Unexpected exception message: %s", e);
        e.printStackTrace();
    }

    private static void assertIOExErrno(Exception e, int errno) {
        assertExMsgContains(e, String.format(" errno: %d ", errno));
    }

    private static void assertXattrSize(File f, String name, int options, long expectSize) throws IOException {
        long got = XAttr4J.sizexattr(f, name, options);
        Preconditions.checkState(got == expectSize, "Expect %s size %s  got %s", name, expectSize, got);
    }

    public static void main(String[] args) throws IOException {
        test1();

        System.out.println("\nPass!");
    }

    private static void test1() throws IOException {
        File f = new File(String.format("/tmp/uuid_%s", UUID.randomUUID().toString()));

        try {
            XAttr4J.removexattr(f, "xattr", 0);
        } catch (IOException e) {
            assertIOExErrno(e, 2);
        }

        try {
            XAttr4J.removexattr(f, "xattr", XAttr4J.XATTR_NOFOLLOW);
        } catch (IOException e) {
            assertIOExErrno(e, 2);
        }

        try {
            // Actually XATTR_CREATE is an insane option to removexattr(2)
            XAttr4J.removexattr(f, "xattr", XAttr4J.XATTR_CREATE);
        } catch (IOException e) {
            assertIOExErrno(e, 2);
        }

        try {
            // Full mask contains some bits which used in XNU kernel yet doesn't exported in user space
            XAttr4J.removexattr(f, "xattr", Integer.MAX_VALUE);
        } catch (IOException e) {
            assertIOExErrno(e, 22);
        }

        noFail(f.createNewFile());

        try {
            XAttr4J.removexattr(f, "xattr", 0);
        } catch (IOException e) {
            assertIOExErrno(e, 93);
        }

        try {
            XAttr4J.setxattr(f, "", "some values", 0);
        } catch (IOException e) {
            assertIOExErrno(e, 22);
        }

        XAttr4J.setxattr(f, "xattr1", "", 0);
        XAttr4J.setxattr(f, "xattr2", UUID.randomUUID().toString(), 0);
        XAttr4J.setxattr(f, "xattr3", stringToUTF8Bytes(UUID.randomUUID().toString()), XAttr4J.XATTR_NOFOLLOW);
        XAttr4J.setxattr(f, "xattr4", stringToUTF8Bytes("祝你好运！"), XAttr4J.XATTR_CREATE);
        XAttr4J.setxattr(f, "xattr5", "こんにちは。", 0);
        XAttr4J.setxattr(f, "xattr6", "I got a 😊", 0);
        XAttr4J.setxattr(f, "xattr7", "", XAttr4J.XATTR_CREATE);
        XAttr4J.setxattr(f, "xattr7", "你好 👋", XAttr4J.XATTR_REPLACE);
        XAttr4J.setxattr(f, "xattr😋", "Wünsche schönes ß", 0);

        noFail(XAttr4J.existxattr(f, "xattr1", 0));
        noFail(XAttr4J.existxattr(f, "xattr2", 0));
        noFail(XAttr4J.existxattr(f, "xattr3", 0));
        noFail(XAttr4J.existxattr(f, "xattr4", 0));
        noFail(XAttr4J.existxattr(f, "xattr5", 0));
        noFail(XAttr4J.existxattr(f, "xattr6", 0));
        noFail(XAttr4J.existxattr(f, "xattr7", 0));
        noFail(XAttr4J.existxattr(f, "xattr😋", 0));

        assertXattrSize(f, "xattr1", 0, 0);
        assertXattrSize(f, "xattr2", 0, 36);
        assertXattrSize(f, "xattr3", 0, 36);
        assertXattrSize(f, "xattr4", 0, 15);
        assertXattrSize(f, "xattr5", 0, 18);
        assertXattrSize(f, "xattr6", 0, 12);
        assertXattrSize(f, "xattr7", XAttr4J.XATTR_NOFOLLOW, 11);
        assertXattrSize(f, "xattr😋", 0, 20);

        String[] ls;
        ls = XAttr4J.listxattr(f, 0);
        Preconditions.checkState(ls.length == 8, "Expect size 8  got %s", ls.length);

        byte[] arr = XAttr4J.getxattr(f, "xattr😋", 0);
        Preconditions.checkState(arr.length == 20, "Expect size 20  got %s", arr.length);

        XAttr4J.removexattr(f, "xattr😋", 0);

        ls = XAttr4J.listxattr(f, 0);
        Preconditions.checkState(ls.length == 7, "Expect size 7  got %s", ls.length);

        noFail(f.delete());
    }
}
