#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <errno.h>
#include <unistd.h>
#include <string.h>
               
/* Create string that can be used by shell-echo to output a binary */

int escape(char* fname)
{
	int fd = 0;
	size_t rd;
	int digit = 0;
	unsigned char buf[1024];
	
	if (0 != strcmp("-", fname)) {
		if (0 >= (fd = open(fname, O_RDONLY))) {
			perror(fname);
			return 0;
		}
	}
	printf("'");
	while(0 < (rd = read(fd, buf, sizeof(buf)))) {
		size_t i = 0;
		while(i < rd) {
			switch(buf[i]) {
			case 0x00:
				/* 
				 * Note: busybox-1.15 shell interprets
				 * \0[char x00-x0f,x18-x2f] as backslash
				 */
				if (i + 1 < rd && ((0x00 <= buf[i + 1] && buf[i + 1] <= 0x0f) || (0x18 <= buf[i + 1] && buf[i + 1] <= 0x2f)))
				{
					printf("\\00");
				}
				else {
					printf("\\0");
				}
				digit = 1;
				break;
			case '\'':
				printf("\\0%o", buf[i]);
				digit = 1;
				break;
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			/*
			 * Note: all busyboxe shells interpret
			 * \00[char x10..char x17] as \00..\07. 
			 */
			case 0x10:
			case 0x11:
			case 0x12:
			case 0x13:
			case 0x14:
			case 0x15:
			case 0x16:
			case 0x17:
				if (digit) {
					printf("\\0%o", buf[i]);
				}
				else {
					printf("%c", buf[i]);
				}
				break;
			case '\\':
				printf("\\");
			default:
				digit = 0;
				printf("%c", buf[i]);
			} /* switch */
			i++;
		}
	}
	printf("'");
	close(fd);
	return 1;
}

int main(int argc, char* argv[])
{
	if (1 >= argc) {
		fprintf(stderr, "File name required.\n");
		return 1;
	}
	if (!escape(argv[1])) return 1;
	return 0;
}
