precision highp float; // default high precision for floating point ranges of the planets

uniform vec3 u_LightPos;        // light position in eye space
uniform vec4 u_LightCol;
uniform sampler2D u_Texture;    // the input texture

varying vec3 v_Position;
varying vec3 v_Normal;
varying vec2 v_TexCoordinate;

void main() {
    // distance for attenuation.
    float distance = length(u_LightPos - v_Position);

    // lighting direction vector from the light to the vertex
    vec3 lightVector = normalize(u_LightPos - v_Position);

    // dot product of the light vector and vertex normal.
    // If the normal and light vector are
    // pointing in the same direction then it will get max
    // illumination.
    float diffuse = max(dot(v_Normal, lightVector), 0.01);

    // Add a tiny bit of ambient lighting (this is outerspace)
    diffuse = diffuse + 0.025;

    // Multiply the color by the diffuse illumination level and
    // texture value to get final output color
    gl_FragColor = texture2D(u_Texture, v_TexCoordinate) * u_LightCol * diffuse;
}
