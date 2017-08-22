# Security Overview

This project provides the following capabilities for a GemFire/Apache Geode cluster.
See [http://geode.apache.org/](http://geode.apache.org/) for more information on Apache Geode.


*  Security Manager - clients must present credentials to connect to a Geode cluster


Users, their encrypted passwords and their privileges are stored within the GemFire
cluster in a region called "users".

# Building Notes

## Install GPG Tools

Install the GPG at the following URL

	https://gpgtools.org/

# Installer Notes

**Building the Java Artifacts**

You will need a maven installation to build the java artifacts.  Directions
are available on the [maven website](http://maven.apache.org/download.cgi). After that, 
follow these instructions to build all of the java artifacts.


## Setting `SECURITY_ENCRYPTION_KEY` configuration

The encryption and decryption of user password is based on a encryption key.

You must set the SECURITY_ENCRYPTION_KEY environment variable or as a JVM system property on each started Geode member (locators and cache server).
   
	export SECURITY_ENCRYPTION_KEY=<MY.ENCRYPTION.KEY.HERE>
	
Example

	export SECURITY_ENCRYPTION_KEY=PIVOTAL
   
## Starting the Locator

The following is an example gfsh command to start the locator
 
	start locator --name=locator  --J="-Dgemfire.security-users.admin={cryption}114 119 103 -118 -77 -24 43 -30 -34 112 -109 -100 90 25 -41 -102,ALL"  --J=-Dgemfire.security-manager=io.pivotal.gemfire.security.UserSecurityManager --classpath=/Projects/Pivotal/dataEng/dev/gemfire-security-extensions/target/gemfire-extensions-security-0.0.1-SNAPSHOT.jar:/Projects/Pivotal/dataEng/dev/gemfire-security-extensions/lib/nyla.solutions.core-1.0.0.jar --enable-cluster-configuration --locators=localhost[10334]
	
	start server --name=server1 --locators=localhost[10334] --server-port=10201 --J="-Dgemfire.security-user=admin" --J="-Dgemfire.security-password=admin" --user=admin --password="{cryption}114 119 103 -118 -77 -24 43 -30 -34 112 -109 -100 90 25 -41 -102" --J="-Dgemfire.security-users.admin={cryption}114 119 103 -118 -77 -24 43 -30 -34 112 -109 -100 90 25 -41 -102,ALL" --J=-Dgemfire.security-manager=io.pivotal.gemfire.security.UserSecurityManager  --classpath=/Projects/Pivotal/dataEng/dev/gemfire-security-extensions/target/gemfire-extensions-security-0.0.1-SNAPSHOT.jar:/Projects/Pivotal/dataEng/dev/gemfire-security-extensions/lib/nyla.solutions.core-1.0.0.jar
	
	
	start server --name=server2 --locators=localhost[10334] --server-port=10202 --J="-Dgemfire.security-user=admin" --J="-Dgemfire.security-password=admin" --user=admin  --password="{cryption}114 119 103 -118 -77 -24 43 -30 -34 112 -109 -100 90 25 -41 -102" --J="-Dgemfire.security-users.admin={cryption}114 119 103 -118 -77 -24 43 -30 -34 112 -109 -100 90 25 -41 -102,ALL"  --J=-Dgemfire.security-manager=io.pivotal.gemfire.security.UserSecurityManager  --classpath=/Projects/Pivotal/dataEng/dev/gemfire-security-extensions/target/gemfire-extensions-security-0.0.1-SNAPSHOT.jar:/Projects/Pivotal/dataEng/dev/gemfire-security-extensions/lib/nyla.solutions.core-1.0.0.jar

# User Security Manager

Set the GemFire security property **security-manager**=*io.pivotal.gemfire.security.UserSecurityManager* 
		
## Configured Users

Add the following GemFire security property

	-Dgemfire.security-users.<userName1>=userEncryptedPassword,[privilege] [,privilege]* 
	
	-Dgemfire.security-users.<userName2>=userEncryptedPassword,[privilege] [,privilege]* 
	
* Example System Property:*

	 -Dgemfire.security-users.admin={cryption}114 119 103 -118 -77 -24 43 -30 -34 112 -109 -100 90 25 -41 -102,ALL
	 
	 

### Privilege

The User privilege are based on the GemFire ResourePermission (Resource:Operation).

- ALL - admin level user access with no restrictions
- CLUSTER - all cluster read, write and manage permissions
- CLUSTER:READ - cluster read permission
- CLUSTER:WRITE - cluster write permission
- CLUSTER:MANAGE - cluster management permissions such as shutdown cluster and members
- DATA - all data read, write and manage permissions
- DATA:READ - data read permission
- DATA:WRITE - data write permission
- DATA:MANAGE - data managed permissions such as creating regions


## Encryption Password

Use the following sample command to encrypt a password. NOTE: SECURITY_ENCRYPTION_KEY variable must match the value set on the server.

	java -classpath target/gemfire-extensions-security-0.0.1-SNAPSHOT.jar:lib/nyla.solutions.core-1.0.0.jar io.pivotal.gemfire.security.SecurityCryption <PASSWORD>
	
	