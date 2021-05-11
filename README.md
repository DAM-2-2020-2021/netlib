# netlib
## How to use
This project uses [GitHub Packages](https://github.com/DAM-2-2020-2021/netlib/packages/) so that it can be added as a Maven dependency on your `pom.xml`:
```
<dependency>
  <groupId>eu.cifpfbmoll</groupId>
  <artifactId>netlib</artifactId>
  <version>VERSION_HERE</version>
</dependency>
```
Adding the dependency won't be enough to download it, you must tell Maven how to find this dependency. For that, you can add the following code inside your Maven settings file. Located by default: `<USER_HOME>/.m2/settings.xml`.

Note that you must replace `ACCESS_TOKEN` with a GitHub Personal Access Token. Creating one won't be necessary since you can use the administrator's token found [here](https://github.com/DAM-2-2020-2021/netlib/wiki/Dependency-Access-Token).
```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <activeProfiles>
    <activeProfile>github</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>github</id>
      <repositories>
        <repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
        </repository>
        <repository>
          <id>github</id>
          <url>https://maven.pkg.github.com/dam-2-2020-2021/*</url>
          <snapshots>
            <enabled>true</enabled>
          </snapshots>
        </repository>
      </repositories>
    </profile>
  </profiles>

  <servers>
    <server>
      <id>github</id>
      <username>snebotcifpfbmoll</username>
      <password>ACCESS_TOKEN</password>
    </server>
  </servers>
</settings>
```
For an Android project you can place the following code inside your gradle properties file, usually located at `<USER_HOME>/.gradle/gradle.properties`:
```
netlib_username=snebotcifpfbmoll
netlib_password=ACCESS_TOKEN
```
Inside your project's module `build.gradle` file:
```
repositories {
    mavenCentral()
    maven {
        credentials {
            username "$netlib_username"
            password "$netlib_password"
        }
        url    'https://maven.pkg.github.com/dam-2-2020-2021/*'
    }
}

dependencies {
    implementation group: 'eu.cifpfbmoll', name: 'netlib', version: 'VERSION_HERE'
}
```
