# netlib
## How to use library with Maven
Copy and paste the following content in the file `<USER_HOME>/.m2/settings.xml`:
```
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">

  <activeProfiles>
    <activeProfile>github-cifpfbmoll</activeProfile>
  </activeProfiles>

  <profiles>
    <profile>
      <id>github-cifpfbmoll</id>
      <repositories>
        <repository>
          <id>central</id>
          <url>https://repo1.maven.org/maven2</url>
        </repository>
        <repository>
          <id>github-cifpfbmoll</id>
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
      <id>github-cifpfbmoll</id>
      <username>snebotcifpfbmoll</username>
      <password>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</password>
    </server>
  </servers>
</settings>
```
