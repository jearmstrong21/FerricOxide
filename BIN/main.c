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
    fo_global_init();
    return fo_run(argc, argv);
}