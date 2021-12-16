#include "glad.h"
#include <GLFW/glfw3.h>
#include <stdio.h>
#include <stdlib.h>

int glfwContextVersionMajor() { return GLFW_CONTEXT_VERSION_MAJOR; }
int glfwContextVersionMinor() { return GLFW_CONTEXT_VERSION_MINOR; }
int glfwOpenGLForwardCompat() { return GLFW_OPENGL_FORWARD_COMPAT; }
int glfwOpenGLProfile() { return GLFW_OPENGL_PROFILE; }
int glfwOpenGLCoreProfile() { return GLFW_OPENGL_CORE_PROFILE; }

void c_fwrite_str(FILE* ptr, char* str) {
    fprintf(ptr, "%s", str);
}
void c_fwrite_i32(FILE* ptr, int x) {
    fprintf(ptr, "%d", x);
}

extern int fo_global_init();
extern int fo_run(int argc, char** argv);

void printInt(int x) {
    printf("FO says: %d\n", x);
}

void printPtr(void*x){
    printf("FO says ptr: %p\n", x);
}

void c_check_stack() {
    int x = 0;
    printf("FO stack check: %p\n", &x);
}

int loadGLAD() {
    return gladLoadGLLoader((GLADloadproc)glfwGetProcAddress);
}

float c_randFloat() {
    return (float)((double)rand()/(double)(RAND_MAX));
}

int main(int argc, char** argv)
{
    if(fo_global_init() != 0) {
        puts("FerricOxide runtime error: runtime global init failed");
        return 1;
    }
    return fo_run(argc, argv);
}