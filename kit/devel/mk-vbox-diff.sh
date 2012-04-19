#!/bin/sh -x

cp -a virtualbox-ose-3.2.10 vbox
rm $(find vbox -type l)
cp -a virtualbox-ose-3.2.10.orig vbox.orig
rm $(find vbox.orig -type l)

diff --exclude=*~ -Nur vbox.orig/ vbox

rm -rf vbox vbox.orig
