/* Ask for an OpenGL Core Context */
#define GLFW_INCLUDE_GLCOREARB
#include <GLFW/glfw3.h>

#define BUFFER_OFFSET(i) ((char *)NULL + (i))

int glColorBufferBit() { return GL_COLOR_BUFFER_BIT; }
int glDepthBufferBit() { return GL_DEPTH_BUFFER_BIT; }
float glClearColorR() { return 0.4; }
float glClearColorG() { return 0.5; }
float glClearColorB() { return 0.4; }
float glClearColorA() { return 1.0; }
int glfwContextVersionMajor() { return GLFW_CONTEXT_VERSION_MAJOR; }
int glfwContextVersionMinor() { return GLFW_CONTEXT_VERSION_MINOR; }
int glfwOpenGLForwardCompat() { return GLFW_OPENGL_FORWARD_COMPAT; }
int glfwOpenGLProfile() { return GLFW_OPENGL_PROFILE; }
int glfwOpenGLCoreProfile() { return GLFW_OPENGL_CORE_PROFILE; }

extern int run();

int main(int argc, char** argv)
{
    return run();
}