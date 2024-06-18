#!/bin/bash

VERSION="$1"
MODULES="semver-analyzer-api semver-analyzer-conventional semver-analyzer-gitmoji semver-changelog-api semver-changelog-mustache semver-maven-plugin semver-scm-api semver-scm-git-provider semver-scm-mecurial-provider semver-scm-subversion-provider"

rm -rf io
mkdir -p io/github/sam42r

mkdir -p io/github/sam42r/semver-parent/$VERSION
cp ../target/*.pom* io/github/sam42r/semver-parent/$VERSION
md5sum io/github/sam42r/semver-parent/$VERSION/semver-parent-$VERSION.pom | cut -d ' ' -f 1 > io/github/sam42r/semver-parent/$VERSION/semver-parent-$VERSION.pom.md5
sha1sum io/github/sam42r/semver-parent/$VERSION/semver-parent-$VERSION.pom | cut -d ' ' -f 1 > io/github/sam42r/semver-parent/$VERSION/semver-parent-$VERSION.pom.sha1

for MODULE in $MODULES
do
  echo "Bundling '$MODULE'"
  DIRECTORY="io/github/sam42r/$MODULE/$VERSION"
  mkdir -p ${DIRECTORY}
  cp ../$MODULE/target/*.jar* io/github/sam42r/$MODULE/$VERSION
  cp ../$MODULE/target/*.pom* io/github/sam42r/$MODULE/$VERSION
  for FILE in `find $DIRECTORY -name "*.jar" -o -name "*.pom"`
  do
    md5sum $FILE | cut -d ' ' -f 1 > $FILE.md5
    sha1sum $FILE | cut -d ' ' -f 1 > $FILE.sha1
  done
done

tar cvfz bundle.tar.gz io
rm -r io