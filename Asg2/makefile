.SUFFIXES: .java .class 
CLASSES = $(wildcard *.java)
JVC = javac
JVM = java
EXEC = Central
 
.java.class:
	$(JVC) $(JCFLAGS) $*.java
 
all: classes

classes: $(CLASSES:.java=.class)
 
run: classes
	$(JVM) $(EXEC)
	
clean:
	$(RM) *.class
