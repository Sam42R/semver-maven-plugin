# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

<!-- DO NOT REMOVE - c871f32ed1b7a85b24a0f22e8e7d9e3ee285742c - DO NOT REMOVE -->

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
