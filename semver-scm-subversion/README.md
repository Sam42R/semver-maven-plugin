# SCM Subversion Provider

TODO general notes and limitations

## Public SVN repositories
> Info: Seems that working copies checked out with HTTPS causing HTTP-400

### Apache-Software-Foundation (ASF)
The [Apache-Software-Foundation](https://www.apache.org/) serves [SVN](https://svn.apache.org/) repositories
for all their [projects](https://www.apache.org/index.html#projects-list).

```bash
$ svn checkout https://svn.apache.org/repos/asf/avro avro-trunk
```

### Source-Forge (SF)
[SourceForge](https://sourceforge.net/) serves [SVN](https://sourceforge.net/p/forge/documentation/SVN%20Overview/) repositories
for a lot of open source projects.

HTTPS
```bash
$ svn checkout https://svn.code.sf.net/p/keepass/code/trunk keepass-code
```
SVN
```bash
svn checkout svn://svn.code.sf.net/p/keepass/code/trunk keepass-code
```

## Local SVN repositories

[SVN Chaet Sheet](https://www.perforce.com/blog/vcs/svn-commands-cheat-sheet)

### Create local repository
```bash
$ svnadmin create sample
```

### Create working copy
```bash
$ svn checkout file:///$PWD/sample working
```

### Create test repository
```bash
$ svnadmin create junit
$ svn checkout file:///$PWD/junit junit-working
$ cd junit-working
$ mkdir trunk
$ mkdir tags
$ mkdir branches
$ echo "# JUnit" > trunk/README.md
$ svn add branches tags trunk
$ svn commit -m "chore: initial project setup"
$ svn copy trunk/ tags/v0.0.1
$ svn commit -m "release: v0.0.1"
$ cd ..
$ svnadmin dump junit > junit.dmp
```