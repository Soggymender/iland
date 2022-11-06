#version 330

layout (location =0) in vec3 position;
layout (location =1) in vec4 color;
layout (location =2) in vec2 texCoord;
layout (location =3) in vec3 vertexNormal;

out vec2 outTexCoord;
out vec3 mvVertexNormal;
out vec3 mvVertexPos;
out vec3 mwVertexPos;

uniform bool outline;
uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;

void main()
{
    vec4 mvPos;

    if (outline) {
        vec3 blah = position + (color.xyz * 0.01);
        mvPos = modelViewMatrix * vec4(blah, 1.0);
    } else {
        mvPos = modelViewMatrix * position;
    }

    gl_Position = projectionMatrix * mvPos;
 
    outTexCoord = texCoord;
    mvVertexPos = mvPos.xyz;
    mvVertexNormal = normalize(modelViewMatrix * vec4(vertexNormal, 0.0)).xyz;
    
    mwVertexPos = position.xyz;

  
}