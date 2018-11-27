# Overview

The [gedi-geode-extensions-core](README.md) module contains a LDAP based implementation of the GemFire/Geode 9.x [integrated security manager](https://gemfire.docs.pivotal.io/geode/managing/security/implementing_authentication.html).

Note this package supports [GemFire](https://gemfire.docs.pivotal.io/gemfire/about_gemfire.html) version 9.0.2 or higher and [nyla.solutions.core](https://github.com/nyla-solutions/nyla/tree/master/nyla.solutions.core) version 1.1.0 or higher.


## Setup LDAP 

For local testing, it is recommended to use [ApacheDS](http://directory.apache.org/apacheds/).

For an easy install on a Mac, it is also recommended to use the [h3nrik/apacheds](https://hub.docker.com/r/h3nrik/apacheds) docker image.

Use the following to build the image

	docker build -t h3nrik/apacheds .

Run the container using the following command

	docker run --name ldap -d -p 389:10389 h3nrik/apacheds


The Apache DS will now be available on port 389. 
The default user/password is admin/secret.

You can use  [ApacheDS Studio](http://directory.apache.org/studio/) the add users for testing.

Also see the following the scripts build the docker image, start Apache DS and add test LDAP users

- src/test/resources/ldap/build.sh 
- src/test/resources/ldap/start.sh  
- src/test/resources/ldap/addusers.sh

## Cluster Startup

1) Set the CRYPTION_KEY environment variable used for encrypting/decrypting passwords prior to starting the cluster
	
		export CRYPTION_KEY=GEDI-GEODI-CRYPTION-KEY
		
You should assert that the CRYPTION_KEY value is a minimum of 16 characters.

2) Setup GemFire Security Property File

The following is an example security property file content (ex: gfldapsecurity.properties)

		# LDAP PROXY user DN used to for all authentication LDAP request
		security-ldap-proxy-dn=uid=admin,ou=system
		
		# LDAP PROXY user password (encrypted or un-encrypted passwords supported) 
		security-ldap-proxy-password=secret
		
		# LDAP server URL
		security-ldap-server-url=ldap://localhost:389
		
		# LDAP base dn to search for user for authentication reques
		security-ldap-base-dn=ou=system
		
		# LDAP attribute that will match the user ID
		security-ldap-uid-attribute=uid
		
		# The LDAP  attribute the indicates the users' group associations
		security-ldap-memberOf-attribute=memberOf
		
		# The LDAP GROUP attribute that will match the security-ldap-acl-group-${??} property
		security-ldap-group-attribute=CN
		
		# Example Access Control Lists
		# user nyla has permission to read data
		
		security-ldap-acl-user-nyla=DATA:READ
		
		# user cluster has permission to performance any cluster operation
		security-ldap-acl-user-cluster=CLUSTER
		
		# user admin ALL permissions
		security-ldap-acl-user-admin=ALL
		security-ldap-acl-group-administrator=ALL
		
		
		# User credentials used to join the GemFire cluster
		security-username=cluster
		security-password={cryption}6rvSAHPquoSszq1SVlbnrw==


**ACL Permissions**

The Access Control List (ACL) permissions the property file are based on the GemFire ResourePermission (Resource:Operation). The format of the property are **security-ldap-acl-user-${UID}** or  **security-ldap-acl-group-${groupID}**.

The following are example ACLs permissions

- ALL - admin level user access with no restrictions
- CLUSTER - all cluster read, write and manage permissions
- CLUSTER:READ - cluster read permission
- CLUSTER:WRITE - cluster write permission
- CLUSTER:MANAGE - cluster management permissions such as shutdown cluster and members
- DATA - all data read, write and manage permissions
- DATA:READ - data read permission
- DATA:WRITE - data write permission
- DATA:MANAGE - data managed permissions such as creating regions
- READ - cluster or data read permissions
- WRITE - cluster or data write permissions

*Password Encryption Support*

You can use the [nyla solution core](https://github.com/nyla-solutions/nyla/tree/master/nyla.solutions.core) [Cryption](https://github.com/nyla-solutions/nyla/blob/master/nyla.solutions.core/src/main/java/nyla/solutions/core/util/Cryption.java) object to generate an encrypted password. 

Usage java nyla.solutions.core.util.Cryption <pass>

Example:

	java -classpath lib/nyla.solutions.core-1.1.0.jar nyla.solutions.core.util.Cryption password
	{cryption}Hepk7h7LmK3WO+dQlGQB0A==

The encrypted password is always prefixed with {cryption}. This prefixed should be included in the property passwords.

3) **Start the Locators**

The following are example gfsh commands to start a single locator

		start locator --name=local --http-service-bind-address=localhost --classpath=/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/lib/nyla.solutions.core-1.1.0.jar:/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/target/gedi-geode-extensions-core-<VERSION>.jar --enable-cluster-configuration  --http-service-port=7070 --security-properties-file=/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/src/test/resources/ldap/gfldapsecurity.properties --J=-Dgemfire.security-manager=gedi.solutions.geode.security.ldap.LdapSecurityMgr 
		
		
		Example

	start locator --name=local --http-service-bind-address=localhost --classpath=/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/lib/nyla.solutions.core-1.1.0.jar:/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/target/gedi-geode-extensions-core-1.1.3-SNAPSHOT.jar --enable-cluster-configuration  --http-service-port=7070 --security-properties-file=/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/src/test/resources/ldap/gfldapsecurity.properties --J=-Dgemfire.security-manager=gedi.solutions.geode.security.ldap.LdapSecurityMgr 


		
	
4) **Start Servers**

The following are example gfsh commands to start two data node cache servers

		start server --name=server1 --server-port=9001 --locators=localhost[10334] --security-properties-file=/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/src/test/resources/ldap/gfldapsecurity.properties --J=-Dgemfire.security-manager=gedi.solutions.geode.security.ldap.LdapSecurityMgr --classpath=/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/target/gedi-geode-extensions-core-<VERSION>.jar:/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/lib/nyla.solutions.core-1.1.0.jar
		
		start server --name=server2 --server-port=9002 --locators=localhost[10334] --security-properties-file=/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/src/test/resources/ldap/gfldapsecurity.properties --J=-Dgemfire.security-manager=gedi.solutions.geode.security.ldap.LdapSecurityMgr --classpath=/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/target/gedi-geode-extensions-core-<VERSION>.jar:/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/lib/nyla.solutions.core-1.1.0.jar
	
	
	
example

	

	start server --name=server1 --server-port=9001 --locators=localhost[10334] --security-properties-file=/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/src/test/resources/ldap/gfldapsecurity.properties --J=-Dgemfire.security-manager=gedi.solutions.geode.security.ldap.LdapSecurityMgr --classpath=/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/target/gedi-geode-extensions-core-1.1.3-SNAPSHOT.jar:/Projects/solutions/gedi/dev/gedi-geode/gedi-geode-extensions-core/lib/nyla.solutions.core-1.1.0.jar

After startup, gfsh and pulse will require a username/password to connect.
  