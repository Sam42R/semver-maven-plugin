[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/sam42r/semver-maven-plugin/maven.yml?label=Build)](https://github.com/Sam42R/semver-maven-plugin/actions?query=branch%3Amain)
[![Sonar Quality Gate](https://img.shields.io/sonar/quality_gate/Sam42R_semver-maven-plugin?server=https%3A%2F%2Fsonarcloud.io&label=Quality%20Gate)](https://sonarcloud.io/project/overview?id=Sam42R_semver-maven-plugin)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.sam42r/semver-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/artifact/io.github.sam42r/semver-maven-plugin)
[![GNU GPL, Version 3.0, June 2007](https://img.shields.io/github/license/sam42r/semver-maven-plugin.svg?label=License)](https://www.gnu.org/licenses/gpl-3.0.txt)

# semver-maven-plugin

The `semver-maven-plugin` aims to transfer the functionality of the
[semantic-release](https://www.npmjs.com/package/semantic-release) package for
[node.js](https://nodejs.org/en) into the maven universe.

# Release steps
(as defined by [semantic-release](https://www.npmjs.com/package/semantic-release))

| Step              | Description                                                                      |
|-------------------|----------------------------------------------------------------------------------|
| Verify Conditions | Verify all the conditions to proceed with the release.                           |
| Get last release  | Obtain the commit corresponding to the last release by analyzing Git tags.       |
| Analyze commits   | Determine the type of release based on the commits added since the last release. |
| Verify release    | Verify the release conformity.                                                   |
| Generate notes    | Generate release notes for the commits added since the last release.             |
| Create Git tag    | Create a Git tag corresponding to the new release version.                       |
| Publish           | Publish the release.                                                             |
| Notify            | Notify of new releases or errors.                                                |

# Usage

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <!-- ... -->
    <build>
        <plugins>
            <plugin>
                <groupId>io.github.sam42r</groupId>
                <artifactId>semver-maven-plugin</artifactId>
                <version>v1.3.3</version>
            </plugin>
        </plugins>
    </build>
    <!-- ... -->
</project>
```
```bash
$ mvn semver:semantic-release
```
For a complete documentation how to use and configure `semver-maven-plugin` check the [documentation](docs/README.md#usage-and-configuration)

# Links and Resources

- [Getting started](docs/README.md)
- [External resources](docs/external-resources.md)
- [Build plugin](docs/build-plugin.md)