# This makefile also checks if app and kit are in sync

# Versioning rules: Publish kit with major.minor, publish
# the app with major.minor.sub, sub is the market version
# number incremented for each Google-play app-publishing.

VERSION=1.6
SHELL=/bin/bash

all: doc version
	(cd kit && ./shar.sh) > debian-kit-$(subst .,-,$(VERSION)).shar
	@echo Ok.

doc:
	diff kit/debian-kit-en.html app/assets/debian-kit-en.html
	diff -r kit/images/ app/assets/images/

version:
	grep -q '^# Version $(subst .,\.,$(VERSION)),' kit/bootdeb
	grep -q '^# Version $(subst .,\.,$(VERSION)),' kit/autorun
	grep -q '^# Version $(subst .,\.,$(VERSION)),' kit/initctl
	grep -q '^# Version $(subst .,\.,$(VERSION)),' kit/mk-debian
	grep -q '^# Version $(subst .,\.,$(VERSION)),' kit/shar.sh
	grep -q '^# Version $(subst .,\.,$(VERSION)),' kit/uninstall
	grep -q 'android:versionName="$(subst .,\.,$(VERSION))\.' app/AndroidManifest.xml

clean:
	rm -v $$(find -name "*~")
