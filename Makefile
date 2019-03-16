#
# Created 190311 lynnl
#
# XXX: rewrite this Makefile devil
#

JAVA_VERSION?=1.7
JAVA_HOME?=$(shell /usr/libexec/java_home -v $(JAVA_VERSION))
JAVAH?=$(JAVA_HOME)/bin/javah
MAIN_CLASS:=net.trineo.xattr4j.XAttr4J

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

all: jni

javah:
	$(JAVAH) $(MAIN_CLASS)
	mv net_trineo_xattr4j_XAttr4J.h net/trineo/xattr4j/xattr4j_jni.h

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

clean:
	rm -f $(LIBNAME)
	rm -rf $(LIBNAME).dSYM

