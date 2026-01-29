# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

<!-- DO NOT REMOVE - c871f32ed1b7a85b24a0f22e8e7d9e3ee285742c - DO NOT REMOVE -->

## 1.8.0 - 2026-01-29


### Added
- :sparkles: use hub4j to create GitHub release #52
### Removed
- :coffin: remove legacy github model #52
### Others
- :construction_worker: update coverage aggregation
- :arrow_up: Bump org.assertj:assertj-core from 3.24.2 to 3.27.7&#10;&#10;Bumps [org.assertj:assertj-core](https://github.com/assertj/assertj) from 3.24.2 to 3.27.7.&#10;- [Release notes](https://github.com/assertj/assertj/releases)&#10;- [Commits](https://github.com/assertj/assertj/compare/assertj-build-3.24.2...assertj-build-3.27.7)&#10;&#10;---&#10;updated-dependencies:&#10;- dependency-name: org.assertj:assertj-core&#10;  dependency-version: 3.27.7&#10;  dependency-type: direct:production&#10;...&#10;&#10;Signed-off-by: dependabot[bot] &lt;support@github.com&gt;
- :construction_worker: add project-name to all modules


## 1.7.0 - 2025-12-02


### Added
- :sparkles: switch from service-loader to jsr330 dependency injection #37
### Others
- :arrow_up: Bump org.apache.commons:commons-lang3 from 3.12.0 to 3.18.0&#10;&#10;Bumps org.apache.commons:commons-lang3 from 3.12.0 to 3.18.0.&#10;&#10;---&#10;updated-dependencies:&#10;- dependency-name: org.apache.commons:commons-lang3&#10;  dependency-version: 3.18.0&#10;  dependency-type: direct:production&#10;...&#10;&#10;Signed-off-by: dependabot[bot] &lt;support@github.com&gt;
- :arrow_up: bump org.eclipse.jgit&#10;&#10;Bumps org.eclipse.jgit:org.eclipse.jgit from 6.6.1.202309021850-r to 6.10.1.202505221210-r.&#10;&#10;---&#10;updated-dependencies:&#10;- dependency-name: org.eclipse.jgit:org.eclipse.jgit&#10;  dependency-version: 6.10.1.202505221210-r&#10;  dependency-type: direct:production&#10;...&#10;&#10;Signed-off-by: dependabot[bot] &lt;support@github.com&gt;


## 1.6.0 - 2025-04-06


### Added
- :sparkles: add analyzer configuration #46
### Fixed
- :ambulance: update is-breaking to match major changes #46


## 1.5.1 - 2025-02-17


### Fixed
- :bug: use &#39;@&#39; as tag format version property delimiter #45


## 1.5.0 - 2025-01-19


### Added
- :sparkles: add maven scm based git provider #34
- :sparkles: add abstract maven scm provider #34
### Changed
- :recycle: move remote URL parsing to util #34
### Deprecated
- :wastebasket: deprecate legacy git provider #34
### Others
- :white_check_mark: add local mojo test
- :white_check_mark: extract abstract test git provider class


## 1.4.0 - 2025-01-05


### Added
- :sparkles: add changelog HTML renderer #42
### Others
- :building_construction: use factory to intialize changelog renderer #42


## 1.3.6 - 2024-12-15


### Fixed
- :bug: use tag for release instead plain version number


## 1.3.5 - 2024-12-15


### Fixed
- :bug: always set plain version; use tag pattern for tag only #31


## v1.3.4 - 2024-11-28


### Fixed
- :ambulance: sort tags by semantic version
### Others
- :technologist: update link to maven central
- :green_heart: make sonarcloud find coverage report
- :memo: add minimal documentation
- :technologist: add badges to README.md
- :construction_worker: setup GitHub actions CI workflow


## v1.3.3 - 2024-10-11


### Fixed
- :bug: release from parent project #32


## v1.3.1 - 2024-08-29


### Fixed
- :bug: url encode project path (group inclusive project name) #29
- :bug: read url scheme from scm and use for release url #29


## v1.3.0 - 2024-07-31


### Added
- :sparkles: add release publisher modules #24
### Changed
- :recycle: (changelog): remove unused model
- :recycle: rename record holding latest release info
### Others
- :construction_worker: add aggregated coverage report


## v1.2.3 - 2024-07-02


### Changed
- :recycle: switch bundle creation from bash script to class #11
### Fixed
- :bug: (scm) push branches and tags #20
### Others
- :truck: rename mercurial module


## v1.2.2 - 2024-06-26


### Fixed
- :bug: (scm) add authentication to push #18


## v1.2.1 - 2024-06-25


### Fixed
- :bug: (mojo) replace simple parent check with maven module check #16


## v1.2.0 - 2024-06-23


### Added
- :sparkles: (changelog) load as plugin with service loader #9
- :sparkles: (mojo) publish to origin #10
- :sparkles: (scm) add push command #10
### Changed
- :recycle: rename scm modules #10
### Others
- :label: (analyzer) add commit analyzer config type
- :label: (scm) add SCM config type #10


## v1.1.1 - 2024-06-18


### Fixed
- :bug: (changelog) fix template package


## v1.1.0 - 2024-06-18


### Added
- :sparkles: (publish) add bundle script for maven central
### Others
- :memo: cleanup README.md
- :construction_worker: switch from com.github to io.github package
- :construction_worker: add source, javadoc and gpg signing


## v1.0.1 - 2024-06-17


### Fixed
- :bug: (git) replace platform file separator with slash
- :bug: (mojo) update parent version on modules


## v1.0.0 - 2024-06-16


### Added
- :sparkles: (changelog) introduce changelog categories
- :sparkles: (analyzer) add category in addition to type/intention
- :boom: split project into multiple modules #3
### Changed
- :recycle: (changelog) pass all analyzed commits to renderer
### Fixed
- :bug: (gitmoji) set analyzed commit header
- :bug: (mojo) support projects with parent
### Others
- :wrench: switch self release to gitmoji commit analyzer


## v0.1.0 - 2024-06-08


### Added
- feat: semantic release mojo

## Disclaimer

This changelog was created with [semver-maven-plugin](https://github.com/Sam42R/semver-maven-plugin). If you have any issue
with `semver-maven-plugin` or want to contribute please visit project site on [GitHub](https://github.com/Sam42R/semver-maven-plugin).
