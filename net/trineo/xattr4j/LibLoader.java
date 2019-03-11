/*
 * Created 190311 lynnl
 *
 * Helper class for loading native libraries
 */

package net.trineo.xattr4j;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

final class LibLoader {
    private static <T> T checkNotNull(T ref) {
        if (ref != null) return ref;
        throw new NullPointerException();
    }

    public static void loadlib(String name) throws IOException {
        checkNotNull(name);

        /*
         * Try load shared library by System.loadLibrary()
         * If it's failed, fallback to search library from package root
         */

        try {
            System.loadLibrary(name);
        } catch (UnsatisfiedLinkError e) {
            String filename = System.mapLibraryName(name);

            /*
             * Assuming the jar followed the following path rule
             */
            Package pkg = LibLoader.class.getPackage();
            String prefix = "";
            if (pkg != null) {
                prefix = pkg.getName().replace('.', File.separatorChar) + File.separatorChar;
            }

            InputStream in = LibLoader.class.getClassLoader().getResourceAsStream(prefix + filename);
            if (in == null) {
                throw new IOException("Cannot found resource " + prefix + filename);
            }

            File file = File.createTempFile("library.", null);
            file.deleteOnExit();

            /* Fill up the temporary file with library file in jar */
            OutputStream out = null;

            try {
                byte[] buff = new byte[4096];
                out = new FileOutputStream(file);

                int len;
                while ((len = in.read(buff)) > 0)
                    out.write(buff, 0, len);
            } finally {
                in.close();
                if (out != null) out.close();
            }

            System.load(file.getAbsolutePath());
        }
    }
}

