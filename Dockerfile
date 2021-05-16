FROM maven:3.6.3 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
RUN mvn -B -f pom.xml dependency:resolve-plugins dependency:resolve clean package > /dev/null
COPY src ./src
ENTRYPOINT ["mvn", "-Dtest=DockerTest", "test"]
