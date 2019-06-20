package test;

import java.io.File;
import java.io.IOException;

import net.trineo.xattr4j.XAttr4J;

public class XattrSizeBits {
    public static void main(String[] args) throws IOException {
        File f;
        long sz;

        f = new File("/tmp");
        sz = XAttr4J.xattrsizebits(f);
        System.out.println("size: " + sz);

        sz = XAttr4J.xattrsizebits("/");
        System.out.println("size: " + sz);

        sz = XAttr4J.xattrsizebits("/net");
        System.out.println("size: " + sz);

        sz = XAttr4J.xattrsizebits("/dev");
        System.out.println("size: " + sz);

        try {
            sz = XAttr4J.xattrsizebits("/foo/bar/gee");
            System.out.println("size: " + sz);
        } catch (IOException e) {
            if (!e.getMessage().contains("errno: 2")) {
                throw e;
            } else {
                e.printStackTrace();
            }
        }

        System.out.println("\nPass!");
    }
}
