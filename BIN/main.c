#include <stdio.h>

void run();

void cPrint(int x, int y) {
    printf("FerricOxide says: %d, %d\n", x, y);
}

void cPrintStr(char *str) {
    printf("FerricOxide says: %s\n", str);
}

int main() {
    printf("Hello World!\n");
	run();
	return 0;
}