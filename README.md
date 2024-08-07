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