Note on compiling kernel modules: you may not get a compilable
kernel tree from your favorite android firmware hacker. In this
case, you may consider using a stock kernel and simply set this
up e.g. to use dkms for virtualbox kernel module compile. Here
are some commands for that:

cd /usr/src
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

if ! dkms build -m vboxhost -v 4.1.10;then
	cat /var/lib/dkms/vboxhost/4.1.10/build/make.log
else
	dkms install -m vboxhost -v 4.1.10
	depmod -a
fi
