#!/bin/bash

# Script for creating an Android compatible shell archive
# Version 1.3, Copyright 2012 by Sven-Ola, License: GPLv3

if ! make -s -C shellescape >&2;then
	echo "To create a shell archive, you need 'make' and compiler on your PC." >&2
	exit 1
fi

for a in armel i386;do
	for i in ifconfig route;do
		if [ ! -h ${a}/${i} ];then
			rm -f ${a}/${i}
			ln -s busybox ${a}/${i}
		fi
	done
done

TAR_UID=2000
TAR_GID=2000
TAR_OPT=z

TAR="tar --numeric-owner \
	--owner ${TAR_UID} \
	--group ${TAR_GID} \
	--exclude=*~ \
	--exclude=.git* \
	--exclude=bla* \
	--exclude=*.orig \
	--exclude=Thumbs.db* \
	--exclude=nohup.out \
	--exclude=./deb \
	--exclude=./ash \
	--exclude=./busybox \
	--exclude=./openvpn \
	--exclude=./shellescape/*.o \
	--exclude=./shellescape/shellescape \
-c${TAR_OPT}"

case ${0%/*} in ${0}) DIR=${PWD};;*) case ${0} in /*) DIR=${0%/*};;*) DIR=${PWD}/${0%/*};DIR=${DIR%%/.};;esac;;esac

IMG=$(sed -n 's/^[[:space:]]*IMG[[:space:]]*=//p' ${DIR}/bootdeb|sed -n '$p')
MNT=$(sed -n 's/^[[:space:]]*MNT[[:space:]]*=//p' ${DIR}/bootdeb|sed -n '$p')

# Apply standard settings
sed -i "s,^[[:space:]]*IMG[[:space:]]*=.*,IMG=\${EXTERNAL_STORAGE:-/sdcard}/debian.img,;s,^[[:space:]]*MNT[[:space:]]*=.*,MNT=\${ANDROID_DATA:-/data}/local/mnt," ${DIR}/bootdeb

cat << EOF
# Self extracting shell archive

testx() {
	IFS=:
	for i in \${PATH};do
		case \$(ls -l -d \${i}/\${1}) in l*|-*)
			unset IFS
			return 0
		;;esac
	done
	unset IFS
	return 1
}

testd() {
	case \$(ls -l -d \${1} 2>&-) in d*)
		return 0
	;;esac
	return 1
}

case \${1} in '')
	DEST=\${ANDROID_DATA:-/data}/local/deb
	echo -n "Unpack to \${DEST} (Y/n) "
	read n
	case \${n} in n|N) exit 1;;esac
	if ! testd \${DEST};then
		mkdir \${DEST} || exit 1
	fi
;;*)
	DEST=\${1}
	if ! testd \${DEST};then
		echo "Destination \${DEST} is not a directory." >&2
		exit 1
	fi
	echo "Unpacking to \${DEST}..."
;;esac

for i in '' '-e' '-n' '-ne' '-n -e';do
	case \$(echo \${i} 'a\nb') in 'a
b')ECHO="echo \${i}"
	;;esac
done

BUSYBOX=
case \$(\${ECHO} 'a\0b') in a)
	echo "This shell does not echo the 0x00 character." >&2
;;*)
	if ! testd \${DEST}/armel;then mkdir \${DEST}/armel;fi
	\${ECHO} $(gzip -c -n -9 armel/busybox | ./shellescape/shellescape -) | gzip -d > \${DEST}/armel/busybox

	if ! testd \${DEST}/i386;then mkdir \${DEST}/i386;fi
	\${ECHO} $(gzip -c -n -9 i386/busybox | ./shellescape/shellescape -) | gzip -d > \${DEST}/i386/busybox

	chmod 755 \${DEST}/armel/busybox \${DEST}/i386/busybox

	BUSYBOX=\$( \
	\${DEST}/i386/busybox ash -c "echo \${DEST}/i386/busybox" 2>&- || \
	\${DEST}/armel/busybox ash -c "echo \${DEST}/armel/busybox" 2>&-)
	case \${BUSYBOX} in "")
		echo "Included busybox failed." >&2
	;;esac
;;esac

echo "Extracting with \${BUSYBOX:-system} tar..."

IMG=\$(\${BUSYBOX} sed -n 's/^[[:space:]]*IMG[[:space:]]*=//p' \${DEST}/bootdeb 2>&-|\${BUSYBOX} sed -n '\$p')
MNT=\$(\${BUSYBOX} sed -n 's/^[[:space:]]*MNT[[:space:]]*=//p' \${DEST}/bootdeb 2>&-|\${BUSYBOX} sed -n '\$p')

\${BUSYBOX} sed -e '1,/^### TAR ARCHIVE STARTS ###/d' \${0} | \${BUSYBOX} tar -C \${DEST} -xv${TAR_OPT}

echo "Checking md5sums..."
\${ECHO} "$(for i in $(${TAR} . | tar -t${TAR_OPT});do md5sum ${i} 2>&-;done | sed 's, .,&${DEST}/,')" | \${BUSYBOX} md5sum -c || exit 1

case \${IMG} in '');;*)
	\${BUSYBOX} sed -i "s,^[[:space:]]*IMG[[:space:]]*=.*,IMG=\${IMG}," \${DEST}/bootdeb
;;esac
case \${MNT} in '');;*)
	\${BUSYBOX} sed -i "s,^[[:space:]]*MNT[[:space:]]*=.*,MNT=\${MNT}," \${DEST}/bootdeb
;;esac

case \${BUSYBOX} in "");;*) case \${1} in "")
	echo
	exec \${BUSYBOX} ash -c "case \\\$(id -u) in 0) test -f \${DEST}/autorun && exec \${DEST}/autorun;;esac"
;;esac;;esac

exit 0

### TAR ARCHIVE STARTS ###
EOF

# Output main tgz here
${TAR} -v .

# Restore previous settings
sed -i "s,^[[:space:]]*IMG[[:space:]]*=.*,IMG=${IMG},;s,^[[:space:]]*MNT[[:space:]]*=.*,MNT=${MNT}," ${DIR}/bootdeb
