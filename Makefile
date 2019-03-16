#
# Created 190311 lynnl
#
# XXX: rewrite this Makefile devil
#

# Used to denote XAttr4J implementation version
VERSION:=0.1

JAVA_VERSION?=1.7
JAVA_HOME?=$(shell /usr/libexec/java_home -v $(JAVA_VERSION))
JAVAH:=$(JAVA_HOME)/bin/javah
MAIN_CLASS:=net.trineo.xattr4j.XAttr4J
JAVA_VERSION:=$(shell $(JAVA_HOME)/bin/javap -version)

CC?=gcc

TIME_STAMP:=$(shell date +'%Y/%m/%d\ %H:%M:%S%z')
CPPFLAGS+=-D__TS__=\"$(TIME_STAMP)\"

#ARCHS?=-shared -arch i386
#ARCHS?=-arch x86_64
ARCHS?=-shared -arch i386 -arch x86_64
MACOSX_VERSION_MIN=10.6
CFLAGS+=-Wall -Wextra -Wno-unused-parameter -std=c99 \
	$(ARCHS) -mmacosx-version-min=$(MACOSX_VERSION_MIN)

SOURCE:=$(wildcard net/trineo/xattr4j/*.c)
LIBNAME:=net/trineo/xattr4j/libxattr4j.dylib

all: jar

javac:
	$(JAVAH) $(MAIN_CLASS)
	mv net_trineo_xattr4j_XAttr4J.h net/trineo/xattr4j/xattr4j_jni.h
	$(JAVA_HOME)/bin/javac -Xlint net/trineo/xattr4j/XAttr4J.java net/trineo/xattr4j/LibLoader.java

jni-core: I1 += $(JAVA_HOME)/include
jni-core: I2 += $(JAVA_HOME)/include/darwin
jni-core:
	$(CC) $(CPPFLAGS) $(CFLAGS) -I$(I1) -I$(I2) -o $(LIBNAME) -lc $(SOURCE)

jni-debug: CPPFLAGS += -DDEBUG
jni-debug: CFLAGS += -O0 -g
jni-debug: jni-core

jni-release: CFLAGS += -O2
jni-release: jni-core

jni: jni-debug

manifest.txt: manifest.txt.in
	sed \
		-e 's/__JAVA_VERSION__/$(JAVA_VERSION)/g' \
		-e 's/__VERSION__/$(VERSION)/g' \
	$^ > $@

jar: javac jni manifest.txt
	$(JAVA_HOME)/bin/jar cvfm xattr4j-$(VERSION).jar \
		manifest.txt net/trineo/xattr4j/*.class net/trineo/xattr4j/*.dylib

clean:
	rm -f $(LIBNAME) manifest.txt net/trineo/xattr4j/*.class
	rm -rf $(LIBNAME).dSYM

