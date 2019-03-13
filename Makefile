#
# Created 190311 lynnl
#

JAVA_HOME?=$(shell /usr/libexec/java_home -v 1.7)
JAVAH?=$(JAVA_HOME)/bin/javah

MAIN_CLASS:=net.trineo.xattr4j.XAttr4J

javah:
	$(JAVAH) $(MAIN_CLASS)
	mv net_trineo_xattr4j_XAttr4J.h net/trineo/xattr4j/xattr4j_jni.h

