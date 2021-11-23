#include <stdio.h>

int factorialIterative(int);
int factorialRecursive(int);
int printFactorials(int);

int cPrint(int x, int y) {
    printf("FerricOxide says: %d, %d\n", x, y);
    return 0;
}

int main() {
    printf("Hello World!\n");
	printFactorials(10);
	return 0;
}