LIBPATH=lib

default: BrilPRE

BrilPRE: 
	javac -cp .:${LIBPATH}/* BrilPRE.java

clean:
	rm -f *.class
