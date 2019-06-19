#
# Created 190311 lynnl
#
# XXX: rewrite this Makefile devil
#

# Used to denote XAttr4J implementation version
VERSION:=0.5

JAVA_VERSION?=1.7
JAVA_HOME?=$(shell /usr/libexec/java_home -v $(JAVA_VERSION))
JAVAH:=$(JAVA_HOME)/bin/javah
JAVA_VERSION:=$(shell $(JAVA_HOME)/bin/javap -version)
PKG_DIR:=net/trineo/xattr4j

CC?=gcc
TIME_STAMP:=$(shell date +'%Y/%m/%d\ %H:%M:%S%z')
CPPFLAGS+=-D__TS__=\"$(TIME_STAMP)\"

#ARCHS?=-arch i386
#ARCHS?=-arch x86_64
ARCHS?=-arch i386 -arch x86_64 -arch x86_64h
MACOSX_VERSION_MIN?=10.6
CFLAGS+=-Wall -Wextra -Wno-unused-parameter -std=c99 \
	-shared $(ARCHS) -mmacosx-version-min=$(MACOSX_VERSION_MIN)

SOURCE:=$(wildcard $(PKG_DIR)/*.c)
LIBNAME:=$(PKG_DIR)/libxattr4j.dylib

PREFIX?=$(HOME)/Library/Java/Extensions

all: jar-debug

javac:
	$(JAVAH) net.trineo.xattr4j.XAttr4J
	mv net_trineo_xattr4j_XAttr4J.h $(PKG_DIR)/xattr4j_jni.h
	$(JAVA_HOME)/bin/javac -Xlint $(PKG_DIR)/XAttr4J.java $(PKG_DIR)/LibLoader.java

jni-core: I1 = $(JAVA_HOME)/include
jni-core: I2 = $(JAVA_HOME)/include/darwin
jni-core:
	$(CC) $(CPPFLAGS) $(CFLAGS) -I$(I1) -I$(I2) -o $(LIBNAME) -lc $(SOURCE)
	otool -l $(LIBNAME) | grep uuid

jni-debug: CPPFLAGS += -DDEBUG
jni-debug: CFLAGS += -O0 -g
jni-debug: jni-core

jni-release: CFLAGS += -O2
jni-release: jni-core

manifest.txt: manifest.txt.in
	sed \
		-e 's/__JAVA_VERSION__/$(JAVA_VERSION)/g' \
		-e 's/__VERSION__/$(VERSION)/g' \
	$^ > $@

jar-core: manifest.txt
	$(JAVA_HOME)/bin/jar cvfm xattr4j-$(VERSION).jar \
		manifest.txt $(PKG_DIR)/LibLoader.class $(PKG_DIR)/XAttr4J.class $(LIBNAME)

jar-debug: javac jni-debug jar-core

jar-release: javac jni-release jar-core

release: jar-release

tests:
	javac -Xlint test/GetxattrTest.java
	javac -Xlint test/SetxattrTest.java
	javac -Xlint test/RemovexattrTest.java
	java test/GetxattrTest
	java test/SetxattrTest
	java test/RemovexattrTest

install: jar-debug
	mkdir -p "$(PREFIX)"
	cp xattr4j-$(VERSION).jar "$(PREFIX)"

uninstall:
	rm -f "$(PREFIX)"/xattr4j-$(VERSION).jar

clean:
	rm -f manifest.txt $(PKG_DIR)/*.class $(LIBNAME) *.jar
	rm -rf $(LIBNAME).dSYM

