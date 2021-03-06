# Security Overview

This project provides the following capabilities for a GemFire/Apache Geode cluster.
See [http://geode.apache.org/](http://geode.apache.org/) for more information on Apache Geode.


*  Security Manager - clients must present credentials to connect to a Geode cluster



# Building Notes

## Install GPG Tools

Install the GPG at the following URL

	https://gpgtools.org/

# Installer Notes

**Building the Java Artifacts**

You will need a maven installation to build the java artifacts.  Directions
are available on the [maven website](http://maven.apache.org/download.cgi). After that, 
follow these instructions to build all of the java artifacts.


## Setting `CRYPTION_KEY` configuration

The encryption and decryption of user password is based on a encryption key.

You must set the CRYPTION_KEY environment variable or as a JVM system property on each started Geode member (locators and cache server).
   
	export CRYPTION_KEY=<MY.ENCRYPTION.KEY.HERE>
	
Example

	export CRYPTION_KEY=PIVOTAL
  
# UserSecurityManager file based

The following explains how to deploy the file properties based user implementation of the
security manager.

 
## Starting the Locator

The following is an example gfsh command to start the locator


The key is setting the gemfire property `security-manager=io.pivotal.gedi.geode.security.UserSecurityManager`. Note the jar nyla.solutions.core-VERSION.jar and `gedi-geode-security-extensions-VERSION.jar` jars must be added to the CLASSPATH.
 
	start locator --name=locator  --J="-Dconfig.properties=/Projects/solutions/gedi/dev/gedi-geode-security-extensions/security-api/src/test/resources/geode_users.properties"  --J=-Dgemfire.security-manager=io.pivotal.gedi.geode.security.UserSecurityManager --J=-Dgemfire.jmx-manager-start=true --classpath=/Projects/solutions/gedi/dev/gedi-geode-security-extensions/security-api/target/gedi-geode-security-extensions-1.0.0.jar:/Projects/solutions/gedi/dev/gedi-geode-security-extensions/lib/nyla.solutions.core-1.1.9.jar --enable-cluster-configuration --locators=localhost[10334]  --connect=false
	
The following is an example gfsh command to start two servers where the JARS and properties must be set similar to the locator.
	
	start server --name=server1 --locators=localhost[10334] --server-port=10201  --use-cluster-configuration=true  --J="-Dconfig.properties=/Projects/solutions/gedi/dev/gedi-geode-security-extensions/security-api/src/test/resources/geode_users.properties" --user=admin --password="admin"  --J="-Dconfig.properties=/Projects/solutions/gedi/dev/gedi-geode-security-extensions/security-api/src/test/resources/geode_users.properties" --J=-Dgemfire.security-manager=io.pivotal.gedi.geode.security.UserSecurityManager  --classpath=/Projects/solutions/gedi/dev/gedi-geode-security-extensions/security-api/target/gedi-geode-security-extensions-1.0.0.jar:/Projects/solutions/gedi/dev/gedi-geode-security-extensions/lib/nyla.solutions.core-1.1.9.jar
	
	
	start server --name=server2 --locators=localhost[10334] --server-port=10202 --use-cluster-configuration=true --J="-Dgemfire.security-password=admin" --user=admin  --password="admin"  --J="-Dconfig.properties=/Projects/solutions/gedi/dev/gedi-geode-security-extensions/security-api/src/test/resources/geode_users.properties" --J=-Dgemfire.security-manager=io.pivotal.gedi.geode.security.UserSecurityManager  --classpath=/Projects/solutions/gedi/dev/gedi-geode-security-extensions/security-api/target/gedi-geode-security-extensions-1.0.0.jar:/Projects/solutions/gedi/dev/gedi-geode-security-extensions/lib/nyla.solutions.core-1.1.9.jar
	

# User Security Manager

Set the GemFire security property **security-manager**=*io.pivotal.gedi.geode.security.UserSecurityManager* 
		
## Configured Users


You can pass a **config.properties** JVM property to set a file that contains the security users passwords/privileges.

The following is an example to set this JVM property using gfsh --J option.


	--J="-Dconfig.properties=/Projects/solutions/gedi/dev/gedi-geode-security-extensions/security-api/src/test/resources/geode_users.properties"

The following is an example file content

	# First user
	gemfire.security-users.<userName1>=userEncryptedPassword,[privilege] [,privilege]* 
	
	# Second user
	gemfire.security-users.<userName2>=userEncryptedPassword,[privilege] [,privilege]* 


The following is an example default setting for an **admin** user with the **ALL** privilege and password:admin with in encrypted format when  `CRYPTION_KEY=PIVOTAL`

	gemfire.security-users.admin={cryption}.....,ALL


You can also add the following GemFire security property to configure users thru system properties

	-Dgemfire.security-users.<userName1>=userEncryptedPassword,[privilege] [,privilege]* 
	
	-Dgemfire.security-users.<userName2>=userEncryptedPassword,[privilege] [,privilege]* 
	
* Example System Property:*

	 -Dgemfire.security-users.admin={cryption}....,ALL
	 
	 

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

Use the following sample command to encrypt a password. NOTE: CRYPTION_KEY variable must match the value set on the server.

	java -classpath /Projects/solutions/gedi/dev/gedi-geode-security-extensions/security-api/target/gedi-geode-security-extensions-1.0.0.jar:/Projects/solutions/gedi/dev/gedi-geode-security-extensions/lib/nyla.solutions.core-1.1.9.jar io.pivotal.gedi.geode.security.SecurityCryption PASSWORD


**Start Cache Server --password encryption**

When starting a cache server the --user=... and ---password=... must be provided to authenticate to the locator. The password can be provided in encrypted or un-encrypted. 

*Note it is recommended to encrypt all passwords*.

**User passwords encryption** 

All user passwords in a property file or passed in as system properties must be encrypted.
		

# GemFire Client Connections

GemFire clients will provide an implementation of org.apache.geode.security.AuthInitialize.
The security-username and security-password must initialized as gemfire properties.

See the following link for details:

[https://gemfire.docs.pivotal.io/geode/managing/security/implementing_authentication.html](https://gemfire.docs.pivotal.io/geode/managing/security/implementing_authentication.html)


Note that the security-password can be encrypted or un-encrypted.


# LDAP Security Manager

See [LDAP Security manager](README_LDAP_SecurityMgr.md)

 
# GemFire Commercial Repository


See the following for instruction to down the GemFire artifacts.

	https://gemfire.docs.pivotal.io/gemfire/getting_started/installation/obtain_gemfire_maven.html
	
