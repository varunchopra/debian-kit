#!/bin/sh

set -e

if [ ! -f linux-3.0.8.tar.xz ];then
	wget -c http://www.kernel.org/pub/linux/kernel/v3.0/linux-3.0.8.tar.xz
fi

if [ ! -d linux-$(uname -r) ];then
	tar xvJf linux-3.0.8.tar.xz
	mv linux-3.0.8 linux-$(uname -r)
fi

if [ ! -f linux-$(uname -r)/.config ];then
	zcat /proc/config.gz | sed 's/-CorvusMod/&+/p' > linux-$(uname -r)/.config
	cd linux-$(uname -r) && make menuconfig
	cd linux-$(uname -r) && make prepare
	cd linux-$(uname -r) && make scripts
	rm /lib/modules/$(uname -r)/build
	ln -s /usr/src/linux-$(uname -r) /lib/modules/$(uname -r)/build
fi

if [ ! -f virtualbox-ose-dkms_4.0.4-dfsg-1ubuntu4.1_all.deb ];then
	wget -c http://security.ubuntu.com/ubuntu/pool/universe/v/virtualbox-ose/virtualbox-ose-dkms_4.0.4-dfsg-1ubuntu4.1_all.deb
fi

if [ ! -d virtualbox-ose-4.0.4 ];then
	ar x virtualbox-ose-dkms_4.0.4-dfsg-1ubuntu4.1_all.deb data.tar.gz
	tar --wildcards -xvzf data.tar.gz "./usr/src/virtualbox-ose-4.0.4/*"
	mv usr/src/virtualbox-ose-4.0.4 .
	rm data.tar.gz
	rm -rvf usr
fi

if ! grep -q generated/autoconf.h virtualbox-ose-3.2.10/include/internal/iprt.h;then
	patch -p1 -d virtualbox-ose-3.2.10 < mk-vbox.patch
fi

if ! dkms build -m virtualbox-ose -v 3.2.10;then
	cat /var/lib/dkms/virtualbox-ose/3.2.10/build/make.log
else
	dkms install -m virtualbox-ose -v 3.2.10
fi
