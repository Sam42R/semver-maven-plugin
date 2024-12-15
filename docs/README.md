- [Getting started](#getting-started)
- [Semantic versioning](#semantic-versioning)
- [Commit message format](#commit-message-format)
  - [Conventional](#conventional-commit)
  - [Gitmoji](#gitmoji)
- [Changelog](#changelog) 
- [Usage and configuration](#usage-and-configuration)

# Getting started
The `semver-maven-plugin` will automatically create [semantic version](#semantic-versioning) numbers based on your
[commit messages](#commit-message-format). In addition, a [changelog](#changelog) containing all commits since last release will be
created.

## Semantic versioning
_"[Semantic versioning] ... proposes a simple set of rules and requirements that dictate how version numbers
are assigned and incremented."_<br>
([semver.org](https://semver.org/spec/v2.0.0.html) "Semantic Versioning 2.0.0")

Given a version number `MAJOR.MINOR.PATCH`, increment the:
1. MAJOR version when you make incompatible API changes
2. MINOR version when you add functionality in a backward compatible manner
3. PATCH version when you make backward compatible bug fixes

## Commit message format
_"High-quality [...] commits are the key to a maintainable and collaborative [...] project."_<br>
([github.blog](https://github.blog/developer-skills/github/write-better-commits-build-better-projects/) "Write Better Commits, Build Better Projects")

Commit messages are used to explain why the change was made and what it does.

### Conventional commit
_"The Conventional Commits specification is a lightweight convention on top of commit messages. It provides an easy set
of rules for creating an explicit commit history; which makes it easier to write automated tools on top of._<br>
([conventionalcommits.org](https://www.conventionalcommits.org/en/v1.0.0/)
"A specification for adding human and machine readable meaning to commit messages")

#### Format
The commit message should be structured as follows:
> &lt;type&gt;[optional scope]: &lt;description&gt;
>
> [optional body]
>
> [optional footer(s)]

#### Example

> fix(database): Load objects by id
> 
> * change load strategy to use id
>
> refs #42

#### Semantics
The following table contains all elements** which lead to an updated version:

| element              | Description                                                                                                                    | Major              | Minor              | Patch              |
|----------------------|--------------------------------------------------------------------------------------------------------------------------------|--------------------|--------------------|--------------------|
| **BREAKING CHANGE:** | a commit that has a _footer_ `BREAKING CHANGE:`, or appends a `!` after<br> the _type/scope_, introduces a breaking API change | :heavy_check_mark: |                    |                    |
| **feat:**            | a commit of the _type_ `feat` introduces a new feature to the codebase                                                         |                    | :heavy_check_mark: |                    |
| **fix:**             | a commit of the _type_ `fix` patches a bug in your codebase                                                                    |                    |                    | :heavy_check_mark: |

** a complete list of all elements can be found [here](https://www.conventionalcommits.org/en/v1.0.0/)

### Gitmoji
_"Gitmoji is an emoji guide for [...] commit messages. [...] Using emojis on commit messages provides an easy way of identifying
the purpose or intention of a commit with only looking at the emojis used."_<br>
([gitmoji.dev](https://gitmoji.dev/about) "An emoji guide for your commit messages")

#### Format
A gitmoji commit message has the following structure:
> &lt;intention&gt; [scope?][:?] &lt;message&gt;

- **intention:** The intention you want to express with the commit, using an emoji from the list. Either in the :shortcode: or unicode format.
- **scope:** An optional string that adds contextual information for the scope of the change.
- **message:** A brief explanation of the change

#### Example
> :bug: (database): Load objects by id #42

#### Semantics
The following table contains all intentions** which lead to an updated version:

| Intention   | Description                    | Major              | Minor              | Patch              |
|-------------|--------------------------------|--------------------|--------------------|--------------------|
| :boom:      | Introduce breaking changes     | :heavy_check_mark: |                    |                    |
| :sparkles:  | Introduce new features         |                    | :heavy_check_mark: |                    |
| :bug:       | Fix a bug                      |                    |                    | :heavy_check_mark: |
| :ambulance: | Critical hotfix                |                    |                    | :heavy_check_mark: |
| :lock:      | Fix security or privacy issues |                    |                    | :heavy_check_mark: |

** a complete list of all intentions can be found [here](https://gitmoji.dev/)

## Changelog
_"The reason for creating and keeping a changelog is simple; when a contributor or end-user wants to see if any changes
have been made to a software program, they can do that easily and precisely by reading the changelog."_<br>
([changelog.md](https://changelog.md/) "Changelogs are Vital for Debugging Software and Error Control")

The `semver-maven-plugin` follows the types of changes as defined by [keepachangelog.com](https://keepachangelog.com/en/1.0.0/):
- `Added` for new features
- `Changed` for changes in existing functionality
- `Deprecated` for soon-to-be removed features
- `Removed` for now removed features
- `Fixed` for any bug fixes
- `Security` in case of vulnerabilities
- `Other` in all other cases

## Usage and configuration
The `semver-maven-plugin` does only define a single maven goal:
```bash
$ mvn semver:semenatic-release
```
All plugin configuration may be set in plugin config of `pom.xml`
or by setting corresponding system property.
```xml
<project>
    <!-- ... -->
    <build>
        <plugins>
            <plugin>
                <groupId>io.github.sam42r</groupId>
                <artifactId>semver-maven-plugin</artifactId>
                <version>v1.3.3</version>
                <configuration>
                    <!--tag-format>v${version}</tag-format-->
                    <scm>
                        <provider-name>Git</provider-name>
                        <push>true</push>
                        <username></username>
                        <password>${env.SSH_PASSPHRASE}</password>
                    </scm>
                    <analyzer>
                        <specification-name>Conventional</specification-name>
                    </analyzer>
                    <changelog>
                        <renderer-name>Markup</renderer-name>
                    </changelog>
                    <release>
                        <publisher-name>GitHub</publisher-name>
                        <publish>true</publish>
                        <username></username>
                        <password>${env.GITHUB_TOKEN}</password>
                    </release>
              </configuration>
              <dependencies>
                    <dependency>
                        <groupId>io.github.sam42r</groupId>
                        <artifactId>semver-scm-[git|subversion|mercurial]</artifactId>
                        <version>v1.3.3</version>
                    </dependency>
                    <dependency>
                        <groupId>io.github.sam42r</groupId>
                        <artifactId>semver-analyzer-[conventional|gitmoji]</artifactId>
                        <version>v1.3.3</version>
                    </dependency>
                    <dependency>
                        <groupId>io.github.sam42r</groupId>
                        <artifactId>semver-changelog-[markup|html]</artifactId>
                        <version>v1.3.3</version>
                    </dependency>
                    <dependency>
                        <groupId>io.github.sam42r</groupId>
                        <artifactId>semver-release-[github|gitlab]</artifactId>
                        <version>v1.3.3</version>
                    </dependency>
                </dependencies>
            </plugin>
        </plugins>
    </build>
    <!-- ... -->
</project>
```

| Module    | Parameter           | Property | Default        | Values                               | Description                           |
|-----------|---------------------|----------|----------------|--------------------------------------|---------------------------------------|
| scm       | provider-name       |          | `Git`          | [Git&vert;Subversion&vert;Mercurial] | SCM provider                          |             |
|           | push                |          | `true`         | [true&vert;false]                    | push changes to remote                |
|           | username            |          |                |                                      |                                       |
|           | password            |          |                |                                      |                                       |
| analyzer  | specification-name  |          | `Conventional` | [Conventional&vert;Gitmoji]          | Commit message analyzer specification |
| changelog | renderer-name       |          | `Markup`       | [Markup&vert;Html]                   | Changelog renderer                    |
| release   | publisher-name      |          | `GitHub`       | [Github&vert;Gitlab]                 | Release publisher                     |
|           | publish             |          | `false`        | [true&vert;false]                    | publish release to remote             |
|           | username            |          |                |                                      |                                       |
|           | password            |          |                |                                      |                                       |

If you use a none default SCM-Provider, Commit-Message-Analyzer, Changelog-Renderer or Release-Publisher your have to add
the corresponding dependency to the plugin dependencies.