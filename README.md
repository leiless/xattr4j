# xattr4j - macOS native xattr syscall wrappers(JNI) for Java

xattr4j is a JNI wrapper which enables Java to access/modify extended attributes over native syscalls.

# Availability

Currently, only macOS is guaranteed to be supported(Other Unix-like systems which have `<sys/xattr.h>` header may require slight changes).

> [[sic]](https://github.com/IsNull/xattrj#scope) On Windows and Linux you can access the xattr by using Javas UserDefinedFileAttributeView which seems to work nativly too. (UserDefinedFileAttributeView is not supported on HFS+ with Java 7 or anything before.)

# Build

This project use `Makefile` for build, the following targets are avaiable:

* `javac` - Generate JNI C header and compile Java classes

* `jni-debug` - Build JNI C code with debug info

* `jni-release` - Build JNI C code for release

* `jar-debug` - Build `jni-debug` and pack into jar

* `jar-release` - Build `jni-release` and pack into jar

* `release` - Alias of `jar-release`

Default target is `jar-debug`.

<br>

You can override the following variables before make:

* `JAVA_VERSION` - JDK version, format 1.x

* `JAVA_HOME` - JDK home path

* `CC` - Which compiler

* `ARCHS` - Build architecture(s)

* `CPPFLAGS` - C preprocessor flags

* `CFLAGS` - C flags

* (macOS) `MACOSX_VERSION_MIN` - Minimal macOS version supported(default 10.6)

# Install

Please specify `PREFIX` variable before make, default installation is `~/Library/Java/Extensions`.

```shell
# Install xattr4j to default location
make install
# Uninstall xatt4j from default localtion
make uninstall

# Install xattr4j to given localtion
sudo PREFIX=/Library/Java/Extensions make install
# Uninstall xatt4j from given localtion
sudo PREFIX=/Library/Java/Extensions make uninstall
```

# Use of `xattr4j`

### Get an extended attribute value

```java
XAttr4J.getxattr(path, xattr_name, options);
```

### Set an extended attribute value

```java
XAttr4J.setxattr(path, xattr_name, xattr_value, options);
```

### Remove an extended attribute value

```java
// Unforce remove a xattr
XAttr4J.removexattr(path, xattr_name, options);

// Explicitly provide the force flag
XAttr4J.removexattr(path, xattr_name, options, is_force);
```

### List extended attribute names

```java
XAttr4J.listxattr(path, options);
```

### Get size of an extended attribute value

```java
XAttr4J.sizexattr(path, xattr_name, options);
```

### Check existence of an extended attribute

```java
XAttr4J.existxattr(path, xattr_name, options);
```

# Test

All test cases require [Google's Guava](https://github.com/google/guava) library to compile.

```shell
make tests
```

Feel free to [submit an issue](issues/new)(with *bug* label) if any test failed.

# Caveats

* This xattr4j implementation claims to be compatible with [UserDefinedFileAttributeView](https://docs.oracle.com/javase/7/docs/api/java/nio/file/attribute/UserDefinedFileAttributeView.html)

* (macOS) Some file systems don't support xattr syscalls, Apple alternatively use a [AppleDouble format](https://en.wikipedia.org/wiki/AppleSingle_and_AppleDouble_formats) for remedy, when you use any xattr syscalls for AppleDouble format file, you'll got a errno 1(EPERM).

* Java uses a [Modified UTF-8](https://en.wikipedia.org/wiki/UTF-8#Modified_UTF-8) representation, when you pass a `java.lang.String` down to JNI and use `JNIEnv->GetStringUTFChars` to get its `char *` representation, and use it as first parameter xattr syscalls, you might got errno 2(ENOENT), since aforementioned `char *` is encoded in a modified UTF-8 form.

	xattr4j overcomed this flaw, we convert it into standard UTF-8 representation before call down to JNI.

* For macOS >= 10.14, please specify `ARCHS` to `-arch x86_64`, Apple droped `i386` library linkage starting from 10.14.

	```shell
	ARCHS='-arch x86_64' make
	```

# TODO

* Support `fd`-oriented `fgetxattr`/`fsetxattr`/`fremovexattr`/`flistxattr` syscalls?

* (macOS) Add a JNI function to retrieve result of `pathconf(path, _PC_XATTR_SIZE_BITS)`?

* Add a `javadoc` target into Makefile?

* Add more sane test cases

Feel free to contribute to this repository. :-)

# License

See [LICENSE](LICENSE)

# *References*

[UserDefinedFileAttributeView](https://docs.oracle.com/javase/7/docs/api/java/nio/file/attribute/UserDefinedFileAttributeView.html)

[IsNull/xattrj](https://github.com/IsNull/xattrj)

[12.108 Glibc `<sys/xattr.h>`](https://www.gnu.org/software/gnulib/manual/html_node/Glibc-sys_002fxattr_002eh.html#Glibc-sys_002fxattr_002eh)
