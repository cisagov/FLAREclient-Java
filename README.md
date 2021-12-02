# FLAREclient
This application uses Spring Boot and MongoDB.  
You can find Spring Boot documentation here: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/
MongoDB documentation can be found here: https://docs.mongodb.com/

## Getting Started
Before you begin **development** on this project you will need to meet these prerequisites in your development environment.

- You must have the latest JDK 1.8 installed (both at your command line as well as with your favorite Integrated Development Environment
  such as `Eclipse`, `IntelliJ`.

- make sure to download this file `mongodb-linux-x86_64-3.5.5.tgz` from here `http://dl.mongodb.org/dl/linux/x86_64`. Store this JAR file under `src/test/resources`.

- You will need these repositories from `https://github.com/cisagov`:
	- javaTAXII2: `https://github.com/cisagov/javaTAXII2`
	- FLAREutils: `https://github.com/cisagov/FLAREutils`
	- FLAREclient-UI: `https://github.com/cisagov/FLAREclient-UI`
	- FLAREclient-mongo: `https://github.com/cisagov/FLAREclient-mongo`
- This is project is under `FLAREclient-Java` repository @ `https://github.com/cisagov`.

- To build this project, `FLAREclient-Java`, you will need to run these in sequence:
	- `javaTAXII2`: `mvn clean install` or `mvn clean install -DskipTests` to save some time.
	- `FLAREutils`: `mvn clean install` or `mvn clean install -DskipTests` to save some time.
	- `FLAREclient-Java`: `mvn clean package`

## Running FLAREclient-Java Locally
After installing all of the prerequisites, you should be able to now run FLAREclient locally. To begin you will need to run the MongoDB instance in docker and expose port `27017`. If this is your first time setting up the client, run

- Run mongo database
```
  docker run -p 127.0.0.1:27017:27017 -d mongo
  ```

If this in not your first time running the above command, you should run check for existing mongo containers using

    docker ps -a

Get the ID of an existing mongo instance in order to prevent creating unnecessary containers / using unnecessary resources on your computer. With the id, run

    docker start #### 

where `####` is the first four characters of your container's ID.
- Run FLAREclient-Java project:


    java -jar target/flareclient-0.0.2-SNAPSHOT.jar

- Output should be like this

```
2021-08-08 06:39:33.246  INFO 23039 --- [           main] g.d.c.c.flare.client.api.FlareclientApp  : 
----------------------------------------------------------
   Application 'flareclient' is running! Access URLs:
   Local:        https://localhost:8443
   External:  https://127.0.1.1:8443
   Profile(s):    []  
   Application Name:  FLAREclient  
   Application Version:   4.0  
----------------------------------------------------------

```
## Docker
The containerization of FLAREclient-UI requires that you have the FLAREclient-Java
repository locally.
1. Make sure the `FLARECLIENT_JAVA_LOCATION` value is correct in `FLAREclient-UI/build-intergrated-image.sh`.
2. Make sure `FLAREclient-mongo` docker is build and running.
3. Build the docker image `./build-intergrated-image.sh`. This will build the `FLAREclient-Java` `jar` file,
   as well as the `FLAREclient-UI` front end.
4. Run the docker image with `./run.sh`.

## OLD:
- Install the listed FLARE projects as they are dependencies of the FLAREclient (Branches listed are as of December 17 2018. This will updated as branches are merged, and the application is closer to delivery.)
	- [javaTAXII2](https://github.com/cisagov/javaTAXII2/tree/develop/) - `develop` branch
		- To make sure this is properly installed as a dependency for the client run a `mvn install` or `mvn install -DskipTests` to save some time.
	- [FLAREutils](https://git.ecicd.dso.ncps.us-cert.gov/fireteam/bcmc/FLAREutils) - `master` branch
		- To make sure this is properly installed as a dependency for the client run a `mvn install` or `mvn install -DskipTests` to save some time.

- Install the following development tools in order to run the client locally for development.
	- [Docker](https://www.docker.com/get-started) to house the Mongo instance that interacts with the client during development.

- You will need our admin certificate to be able to hit the api endpoints within the client found [here](https://cybershare.atlassian.net/wiki/spaces/devspace/pages/172949587/FLAREcloud+-+Client+Certificate). Get the password from a teammate to unzip it.

**Building**

To build the FLAREclient application run:

    mvn clean package

To ensure everything worked, run:

    java -jar target/*.jar

Then navigate to [http://localhost:8081](http://localhost:8081) in your browser.


**Testing**

To launch your application's tests, run:

    mvn clean test

**Using Docker to simplify development (optional)**

You can use Docker to improve your FLAREclient development experience. A number of docker-compose configuration are available in the [src/main/docker](src/main/docker) folder to launch required third party services.

For example, to start a mongodb database in a docker container, run:

    docker-compose -f docker/mongodb.yml up -d

To stop it and remove the container, run:

    docker-compose -f docker/mongodb.yml down

You can also fully dockerize your application and all the services that it depends on.
To achieve this, first build a docker image of your app by running:

    mvn clean package docker:build

Then run:

    docker-compose -f docker/app.yml up -d

For more information about Docker refer to https://docs.docker.com/
For more information about Docker Compose refer to https://docs.docker.com/compose/
