How to compile binaries for Android phones

0) Install softs, e.g. apt-get install subversion build-essential

1) Grab openwrt/backfire. Download the current trunk with
   svn co svn://svn.openwrt.org/openwrt/branches/backfire

2) Apply patch: cd backfire && patch -p1 < .../backfire-android.patch

2a) For x86/i386, also apply patch -p1 < .../backfire-android-x86.patch

3) Do "make menuconfig" and save.

4) Do "make V=99"

Results are in bin/android/packages. Use "tar" to get the binaries,
e.g. something like "tar xvzf bla-*.ipk;tar xvzf data.tar.gz".

When checking against newer openwrt, check packages also:

cp backfire.orig backfire+packages
cd backfire+packages
./scripts/feeds update -a
./scripts/feeds install -a

# no diffs with lzo
diff -r --exclude=.svn package/feeds/packages/lzo ../backfire/package/lzo/

# 2.1.3 -> 2.2.1
diff -r --exclude=.svn package/feeds/packages/openvpn ../backfire/package/openvpn-static/

# zlib-dynamic->zlib, lots of "no-xxx" options (12+7)
diff -r --exclude=.svn package/openssl/ ../backfire/package/openssl/

# Renamed only BuildPackage(e2fsprogs) left - removed all other package rules, removed
# -= DEPENDS to removed rules and no libpthread, replaced e2fsprogs -> *-static, PGK_SOURCE
# reverted to e2fsprogs, CONFIGURE_ARGS-=shard -=static -= dynamic-e2fsck, /description adapted, 
# build+install rules adapated to misc/tune2fs.static, misc/mke2fs.static, e2fsck/e2fsck.static, 
# files/ deleted, adapted Build/Prepare rule to untar to *-static dir, 900-mtab patch added
diff -r --exclude=.svn ../backfire/package/e2fsprogs/ ../backfire/package/e2fsprogs-static/

# Note: package/pkgdetails-static added by me
