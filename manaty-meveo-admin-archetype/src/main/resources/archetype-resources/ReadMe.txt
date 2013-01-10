
BUILDING

1 Prerequisites

To build a project you should have the following installed:

Java v1.6 
Maven v3.0.3+ 

To make a release you should also have the following installed:

SVN client

2 Building a project

2.1 To buid a project and deploy to local repository

Run the following command from the command line in project's root directory.
A packaged ".jar" file will be deployed to your local Maven repository (~/.m2/...). It will not be accessible to anybody else.

mvn clean install -P<your profile id>

2.2 To build a RELEASE project and deploy to Manaty repository

Run the following command from the command line in project's root directory.
A packaged ".ear" file will be deployed to your local Maven repository (~/.m2/...) and Manaty Maven repository. It will be accessible to everybody else with access to Manty Maven repository.

As part of release project will be taged in SVN with a release version number. You will be asked to provide a release version number, a new snapshot version number and a tag for SVN repository (you will be suggested default values) 

Note: Make sure you have the everything commited to SVN

mvn release:prepare -Prelease -Dusername=<SVN UserName> -Dpassword=<SVN Password>
mvn release:perform -Prelease -Dusername=<SVN UserName> -Dpassword=<SVN Password>

