#include "glad.h"
#include <GLFW/glfw3.h>
#include <stdio.h>
#include <stdlib.h>

void c_fwrite_str(FILE* ptr, char* str) {
    fprintf(ptr, "%s", str);
}
void c_fwrite_i32(FILE* ptr, int x) {
    fprintf(ptr, "%d", x);
}

extern void fo_global_init();
extern int fo_run(int argc, char** argv);

void printInt(int x) {
    printf("FO says: %d\n", x);
}

void print_f32(float f) {
    printf("FO says: %f\n", f);
}

void printPtr(void*x){
    printf("FO says ptr: %p\n", x);
}

float c_randFloat() {
    return (float)((double)rand()/(double)(RAND_MAX));
}

int main(int argc, char** argv)
{
    fo_global_init();
    return fo_run(argc, argv);
}