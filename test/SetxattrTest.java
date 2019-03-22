package test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

import net.trineo.xattr4j.XAttr4J;

import com.google.common.base.Preconditions;

class SetxattrTest {
    public static void main(String[] args) throws IOException {
        test1();

        System.out.println("\nPass!");
    }

    private static void assertExMsgContains(Exception e, String str) {
        Preconditions.checkState(e.getMessage().contains(str),
                "Unexpected exception message: %s", e);
        e.printStackTrace();
    }

    private static void noFail(boolean expr) {
        Preconditions.checkState(expr, "Operation failed  cannot proceed");
    }

    private static void test1() throws IOException {
        File f = new File(String.format("/tmp/uuid_%s", UUID.randomUUID().toString()));

        try {
            XAttr4J.getxattr(f, "", 0);
        } catch (IOException e) {
            assertExMsgContains(e, " errno: 2 ");
        }

        try {
            XAttr4J.setxattr(f, "", "", 0);
        } catch (IOException e) {
            assertExMsgContains(e, " errno: 2 ");
        }

        noFail(f.createNewFile());

        try {
            XAttr4J.getxattr(f, "", 0);
        } catch (IOException e) {
            assertExMsgContains(e, " errno: 22 ");
        }

        try {
            XAttr4J.setxattr(f, "", "", 0);
        } catch (IOException e) {
            assertExMsgContains(e, " errno: 22 ");
        }

        try {
            XAttr4J.setxattr(f, "attr1", "", XAttr4J.XATTR_REPLACE);
        } catch (IOException e) {
            assertExMsgContains(e, " errno: 93 ");  // ENOATTR
        }

        XAttr4J.setxattr(f, "attr1", "", 0);

        byte[] val;
        val = XAttr4J.getxattr(f, "attr1", 0);
        Preconditions.checkState(val.length == 0, "Expect zero  got %s", val.length);

        try {
            XAttr4J.setxattr(f, "attr1", "", XAttr4J.XATTR_CREATE);
        } catch (IOException e) {
            assertExMsgContains(e, " errno: 17 ");  // EEXIST
        }

        XAttr4J.removexattr(f, "attr1", 0);

        try {
            XAttr4J.getxattr(f, "attr1", 0);
        } catch (IOException e) {
            assertExMsgContains(e, " errno: 93 ");  // ENOATTR
        }

        XAttr4J.setxattr(f, "xattr1", "1", 0);
        XAttr4J.setxattr(f, "xattr2", "0123456789", 0);
        XAttr4J.setxattr(f, "xattr3", "abcdef", XAttr4J.XATTR_CREATE);
        XAttr4J.setxattr(f, "xattr4", "你好！", XAttr4J.XATTR_CREATE);
        XAttr4J.setxattr(f, "xattr5", "", XAttr4J.XATTR_NOFOLLOW);

        String[] ls;
        ls = XAttr4J.listxattr(f, 0);
        Preconditions.checkState(ls.length == 5, "Expect 5  got %s", ls.length);

        noFail(XAttr4J.existxattr(f, "xattr1", XAttr4J.XATTR_SHOWCOMPRESSION | XAttr4J.XATTR_NOFOLLOW));
        noFail(XAttr4J.existxattr(f, "xattr2", XAttr4J.XATTR_SHOWCOMPRESSION | XAttr4J.XATTR_NOFOLLOW));
        noFail(XAttr4J.existxattr(f, "xattr3", XAttr4J.XATTR_SHOWCOMPRESSION | XAttr4J.XATTR_NOFOLLOW));
        noFail(XAttr4J.existxattr(f, "xattr4", XAttr4J.XATTR_SHOWCOMPRESSION | XAttr4J.XATTR_NOFOLLOW));
        noFail(XAttr4J.existxattr(f, "xattr5", XAttr4J.XATTR_SHOWCOMPRESSION | XAttr4J.XATTR_NOFOLLOW));

        long sz;
        sz = XAttr4J.sizexattr(f, "xattr1", XAttr4J.XATTR_NOFOLLOW);
        Preconditions.checkState(sz == 1, "Expect 1  got %s", sz);
        sz = XAttr4J.sizexattr(f, "xattr2", 0);
        Preconditions.checkState(sz == 10, "Expect 10  got %s", sz);
        sz = XAttr4J.sizexattr(f, "xattr3", 0);
        Preconditions.checkState(sz == 6, "Expect 6  got %s", sz);
        sz = XAttr4J.sizexattr(f, "xattr4", 0);
        Preconditions.checkState(sz == 9, "Expect 6  got %s", sz);
        sz = XAttr4J.sizexattr(f, "xattr5", XAttr4J.XATTR_SHOWCOMPRESSION);
        Preconditions.checkState(sz == 0, "Expect 5  got %s", sz);

        File link = new File(String.format("%s.symlink", f.getAbsolutePath()));
        Files.createSymbolicLink(link.toPath(), f.toPath());

        // Can get with default follow symlink option
        val = XAttr4J.getxattr(link, "xattr2", 0);
        Preconditions.checkState(val.length == 10, "Expect 10  got %s", val.length);
        sz = XAttr4J.sizexattr(link, "xattr2", 0);
        Preconditions.checkState(sz == 10, "Expect 10  got %s", sz);
        // Expect cannot get with XATTR_NOFOLLOW options
        try {
            XAttr4J.getxattr(link, "xattr2", XAttr4J.XATTR_NOFOLLOW);
        } catch (IOException e) {
            assertExMsgContains(e, " errno: 93 ");  // ENOATTR
        }

        XAttr4J.removexattr(f, "xattr1", XAttr4J.XATTR_NOFOLLOW);
        XAttr4J.removexattr(f, "xattr2", XAttr4J.XATTR_NOFOLLOW);
        XAttr4J.removexattr(f, "xattr3", XAttr4J.XATTR_NOFOLLOW);
        XAttr4J.removexattr(f, "xattr4", XAttr4J.XATTR_NOFOLLOW);

        noFail(f.setReadable(false, true));

        try {
            XAttr4J.getxattr(f, "xattr5", 0);
        } catch (IOException e) {
            assertExMsgContains(e, " errno: 13 ");  // EACCESS
        }

        noFail(f.setReadable(true, true));
        noFail(f.setReadable(false, false));

        try {
            XAttr4J.getxattr(f, "xattr5", 0);
        } catch (IOException e) {
            assertExMsgContains(e, " errno: 13 ");
        }

        noFail(f.setReadable(true, false));

        noFail(f.setWritable(false, true));
        try {
            XAttr4J.getxattr(f, "xattr5", 0);
        } catch (IOException e) {
            assertExMsgContains(e, " errno: 13 ");
        }

        noFail(f.setWritable(true, true));
        noFail(f.setWritable(false, false));

        try {
            XAttr4J.getxattr(f, "xattr5", 0);
        } catch (IOException e) {
            assertExMsgContains(e, " errno: 13 ");
        }

        noFail(f.setWritable(true, false));
        XAttr4J.removexattr(f, "xattr5", XAttr4J.XATTR_NOFOLLOW);

        noFail(f.delete());
        noFail(link.delete());
    }
}
