# Build plugin

## GPG signing

### Generate key
```bash
$ gpg --gen-key
```

### List keys
```bash
$ gpg --list-keys
```

### Export public key to keyserver
```bash
$ gpg --keyserver keyserver.ubuntu.com --send-keys D826D9A9F4001BFDA16BD20C4A3649F0FDDA2A52
```

### Export passphrase to environment
```bash
$ export MAVEN_GPG_PASSPHRASE=<PASSPHRASE>
```