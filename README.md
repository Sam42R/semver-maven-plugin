# semver-maven-plugin

The `semver-maven-plugin` aims to transfer the functionality of the
[semantic-release](https://www.npmjs.com/package/semantic-release) package for
[node.js](https://nodejs.org/en) into the maven universe.

# Release steps
(as defined by [semantic-release](https://www.npmjs.com/package/semantic-release))

| Step              | Description                                                                      | MOJO                                                                                 |
|-------------------|----------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|
| Verify Conditions | Verify all the conditions to proceed with the release.                           |                                                                                      |
| Get last release  | Obtain the commit corresponding to the last release by analyzing Git tags.       | [GetLastReleaseMojo](src/main/java/com/github/sam42r/semver/GetLastReleaseMojo.java) |
| Analyze commits   | Determine the type of release based on the commits added since the last release. |                                                                                      |
| Verify release    | Verify the release conformity.                                                   |                                                                                      |
| Generate notes    | Generate release notes for the commits added since the last release.             |                                                                                      |
| Create Git tag    | Create a Git tag corresponding to the new release version.                       |                                                                                      |
| Prepare           | Prepare the release.                                                             |                                                                                      |
| Publish           | Publish the release.                                                             |                                                                                      |
| Notify            | Notify of new releases or errors.                                                |                                                                                      |

# Links and Resources

- [Semantic Versioning 2.0.0](https://semver.org/)
- [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/)
- [Git documentation](https://git-scm.com/doc)