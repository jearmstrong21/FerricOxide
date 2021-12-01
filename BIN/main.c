#include "glad.h"
#include <GLFW/glfw3.h>

int glColorBufferBit() { return GL_COLOR_BUFFER_BIT; }
int glDepthBufferBit() { return GL_DEPTH_BUFFER_BIT; }
int glTriangles() { return GL_TRIANGLES; }
float glClearColorR() { return 0.4; }
float glClearColorG() { return 0.5; }
float glClearColorB() { return 0.4; }
float glClearColorA() { return 1.0; }
int glfwContextVersionMajor() { return GLFW_CONTEXT_VERSION_MAJOR; }
int glfwContextVersionMinor() { return GLFW_CONTEXT_VERSION_MINOR; }
int glfwOpenGLForwardCompat() { return GLFW_OPENGL_FORWARD_COMPAT; }
int glfwOpenGLProfile() { return GLFW_OPENGL_PROFILE; }
int glfwOpenGLCoreProfile() { return GLFW_OPENGL_CORE_PROFILE; }

float zero() { return 0.0; }
float one() { return 1.0; }

int glVersion() { return GL_VERSION; }

extern int run();

int loadGLAD() {
    return gladLoadGLLoader((GLADloadproc)glfwGetProcAddress);
}

void triangle() {
    glBegin(GL_TRIANGLES);

    glColor3f(1,0,0);
    glVertex2f(0,0);

    glColor3f(0,1,0);
    glVertex2f(1,0);

    glColor3f(0,0,1);
    glVertex2f(0,1);

    glEnd();
    /*
        glBegin(glTriangles());

        glColor3f(one(),zero(),zero());
        glVertex2f(zero(),zero());

        glColor3f(zero(),one(),zero());
        glVertex2f(one(),zero());

        glColor3f(zero(),zero(),one());
        glVertex2f(zero(),one());

        glEnd();
    */
}

int main(int argc, char** argv)
{
    return run();
}