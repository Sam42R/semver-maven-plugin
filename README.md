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

# GPG signing

## Generate key
```bash
$ gpg --gen-key
```

## List keys
```bash
$ gpg --list-keys
```

## Export public key to keyserver
```bash
$ gpg --keyserver keyserver.ubuntu.com --send-keys D826D9A9F4001BFDA16BD20C4A3649F0FDDA2A52
```

##
```bash
$ export MAVEN_GPG_PASSPHRASE=<PASSPHRASE>
```


# Links and Resources

See [documentation](docs/resources.md)