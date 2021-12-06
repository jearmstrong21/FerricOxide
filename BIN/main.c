#include "glad.h"
#include <GLFW/glfw3.h>
#include <stdio.h>

int glfwContextVersionMajor() { return GLFW_CONTEXT_VERSION_MAJOR; }
int glfwContextVersionMinor() { return GLFW_CONTEXT_VERSION_MINOR; }
int glfwOpenGLForwardCompat() { return GLFW_OPENGL_FORWARD_COMPAT; }
int glfwOpenGLProfile() { return GLFW_OPENGL_PROFILE; }
int glfwOpenGLCoreProfile() { return GLFW_OPENGL_CORE_PROFILE; }

float zero() { return 0.0; }
float one() { return 1.0; }

void c_fwrite_str(FILE* ptr, char* str) {
    fprintf(ptr, "%s", str);
}
void c_fwrite_i32(FILE* ptr, int x) {
    fprintf(ptr, "%d", x);
}

extern int fo__runtime_global_init();
extern int run(int argc, char** argv);

void printInt(int x) {
    printf("FO says: %d\n", x);
}

int loadGLAD() {
    return gladLoadGLLoader((GLADloadproc)glfwGetProcAddress);
}

int main(int argc, char** argv)
{
    if(fo__runtime_global_init() != 0) {
        puts("FerricOxide runtime error: runtime global init failed");
        return 1;
    }
    return run(argc, argv);
}