#include "glad.h"
#include <GLFW/glfw3.h>

int glfwContextVersionMajor() { return GLFW_CONTEXT_VERSION_MAJOR; }
int glfwContextVersionMinor() { return GLFW_CONTEXT_VERSION_MINOR; }
int glfwOpenGLForwardCompat() { return GLFW_OPENGL_FORWARD_COMPAT; }
int glfwOpenGLProfile() { return GLFW_OPENGL_PROFILE; }
int glfwOpenGLCoreProfile() { return GLFW_OPENGL_CORE_PROFILE; }

float zero() { return 0.0; }
float one() { return 1.0; }

extern int run(int argc, char** argv);

int loadGLAD() {
    return gladLoadGLLoader((GLADloadproc)glfwGetProcAddress);
}

int main(int argc, char** argv)
{
    return run(argc, argv);
}