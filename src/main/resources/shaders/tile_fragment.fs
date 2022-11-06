#version 330

const int MAX_POINT_LIGHTS = 5;
const int MAX_SPOT_LIGHTS = 5;
const int MAX_DIRECTIONAL_LIGHTS = 2;

in vec2 outTexCoord;
in vec3 mvVertexNormal;
in vec3 mvVertexPos;
in vec3 mwVertexPos;

out vec4 fragColor;

struct Attenuation
{
    float constant;
    float linear;
    float exponent;
};

struct PointLight
{
    vec3 color;
    vec3 position;
    float intensity;
    Attenuation att;
};

struct SpotLight
{
    PointLight pl;
    vec3 conedir;
    float cutoff;
};

struct DirectionalLight
{
    vec3 color;
    vec3 direction;
    float intensity;
};

struct Material
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    int hasTexture;
    float reflectance;
};

uniform sampler2D texture_sampler;
uniform vec3 ambientLight;
uniform float specularPower;
uniform Material material;
uniform PointLight pointLights[MAX_POINT_LIGHTS];
uniform SpotLight spotLights[MAX_SPOT_LIGHTS];
uniform DirectionalLight directionalLights[MAX_DIRECTIONAL_LIGHTS];

uniform float depth;

vec4 ambientC;
vec4 diffuseC;
vec4 specularC;

void setupColors(Material material, vec2 textCoord)
{
    if (material.hasTexture == 1)
    {
        ambientC = texture(texture_sampler, textCoord);
        diffuseC = ambientC;
        specularC = ambientC;
    }
    else
    {
        ambientC = material.ambient;
        diffuseC = material.diffuse;
        specularC = material.specular;
    }
}

vec4 calcLightColor(vec3 light_color, float light_intensity, vec3 position, vec3 to_light_dir, vec3 normal)
{
    vec4 diffuseColor = vec4(0, 0, 0, 0);
    vec4 specularColor = vec4(0, 0, 0, 0);

    // Diffuse Light
    float diffuseFactor = max(dot(normal, to_light_dir), 0.0);
    diffuseColor = diffuseC * vec4(light_color, 1.0) * light_intensity * diffuseFactor;

    // Specular Light
    vec3 camera_direction = normalize(-position);
    vec3 from_light_dir = -to_light_dir;
    vec3 reflected_light = normalize(reflect(from_light_dir , normal));
    float specularFactor = max( dot(camera_direction, reflected_light), 0.0);
    specularFactor = pow(specularFactor, specularPower);
    specularColor = specularC * light_intensity  * specularFactor * material.reflectance * vec4(light_color, 1.0);

    return (diffuseColor + specularColor);
}

vec4 calcPointLight(PointLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.position - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec4 light_color = calcLightColor(light.color, light.intensity, position, to_light_dir, normal);

    // Apply Attenuation
    float distance = length(light_direction);
    float attenuationInv = light.att.constant + light.att.linear * distance +
        light.att.exponent * distance * distance;
    return light_color / attenuationInv;
}

vec4 calcSpotLight(SpotLight light, vec3 position, vec3 normal)
{
    vec3 light_direction = light.pl.position - position;
    vec3 to_light_dir  = normalize(light_direction);
    vec3 from_light_dir  = -to_light_dir;
    float spot_alfa = dot(from_light_dir, normalize(light.conedir));

    vec4 color = vec4(0, 0, 0, 0);

    if ( spot_alfa > light.cutoff )
    {
        color = calcPointLight(light.pl, position, normal);
        color *= (1.0 - (1.0 - spot_alfa)/(1.0 - light.cutoff));
    }
    return color;
}

vec4 calcDirectionalLight(DirectionalLight light, vec3 position, vec3 worldPos, vec3 normal)
{
    vec4 lightColor = calcLightColor(light.color, light.intensity, position, normalize(light.direction), normal);

   return lightColor * (1 - (20 * depth));
}

void main()
{
    setupColors(material, outTexCoord);

    vec4 diffuseSpecularComp;
    
    for (int i = 0; i < 1/*MAX_DIRECTIONAL_LIGHTS*/; i++) {
        if (directionalLights[i].intensity > 0) {
            diffuseSpecularComp += calcDirectionalLight(directionalLights[i], mvVertexPos, mwVertexPos, mvVertexNormal);
        }
    }

    for (int i = 0; i < MAX_POINT_LIGHTS; i++)
    {
        if (pointLights[i].intensity > 0) {
            diffuseSpecularComp += calcPointLight(pointLights[i], mvVertexPos, mvVertexNormal);
        }
    }

    for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
        if (spotLights[i].pl.intensity > 0) {
            diffuseSpecularComp += calcSpotLight(spotLights[i], mvVertexPos, mvVertexNormal);
        }
    }

    fragColor = ambientC * vec4(ambientLight, 1) + diffuseSpecularComp;

    float _OutlineWidth = 0.9;
    float _OutlineSoftness = 0.25;
    float _OutlinePower = 0.9;
    vec3 viewWS = vec3(0, 0, -1);
    vec4 _OutlineColor = fragColor;//vec4(1, 1, 1, 1);


    float edge1 = 1 - _OutlineWidth;
    float edge2 = edge1 + _OutlineSoftness;
    float fresnel = pow(1.0 - clamp(dot(mvVertexNormal, viewWS), 0.0, 1.0), _OutlinePower);
    fragColor = mix(1, smoothstep(edge1, edge2, fresnel), step(0, edge1)) * _OutlineColor;
}