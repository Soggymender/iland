#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec4 color;
layout (location=2) in vec2 texCoord;
layout (location=3) in vec3 vertexNormal;

out vec2 outTexCoord;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

void main()
{
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    outTexCoord = texCoord;
}