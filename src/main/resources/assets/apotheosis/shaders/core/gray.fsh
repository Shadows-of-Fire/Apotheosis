#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord1;
in vec4 normal;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) {
        discard;
    }
	color = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
	float gray = color.r * 0.35 + color.g * 0.54 + color.b * 0.11;
    fragColor = vec4(gray, gray, gray, color.a);
}
