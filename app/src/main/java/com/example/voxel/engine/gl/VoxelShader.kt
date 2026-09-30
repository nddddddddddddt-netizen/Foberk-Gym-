package com.example.voxel.engine.gl

import android.opengl.GLES20

class VoxelShader {

    companion object {
        const val VERTEX_SHADER_CODE = """
            uniform mat4 u_MVPMatrix;
            uniform mat4 u_MVMatrix;
            uniform vec3 u_SunDir;
            uniform vec3 u_SunColor;
            uniform vec3 u_AmbientLight;
            uniform vec3 u_FogColor;
            uniform float u_FogStart;
            uniform float u_FogEnd;

            attribute vec4 a_Position;
            attribute vec3 a_Normal;
            attribute vec4 a_Color;
            attribute float a_AO;

            varying vec4 v_Color;
            varying float v_FogFactor;

            void main() {
                gl_Position = u_MVPMatrix * a_Position;

                // Simple directional diffuse lighting
                float diffuse = max(dot(a_Normal, u_SunDir), 0.0);
                vec3 lighting = u_AmbientLight + (u_SunColor * diffuse * 0.7);

                // Apply Ambient Occlusion (a_AO: 0.4 .. 1.0)
                lighting *= a_AO;

                v_Color = vec4(a_Color.rgb * lighting, a_Color.a);

                // Distance fog calculation
                vec4 eyePos = u_MVMatrix * a_Position;
                float dist = length(eyePos.xyz);
                v_FogFactor = clamp((u_FogEnd - dist) / (u_FogEnd - u_FogStart), 0.0, 1.0);
            }
        """

        const val FRAGMENT_SHADER_CODE = """
            precision mediump float;

            uniform vec3 u_FogColor;
            varying vec4 v_Color;
            varying float v_FogFactor;

            void main() {
                // Blend with distance fog
                vec3 finalColor = mix(u_FogColor, v_Color.rgb, v_FogFactor);
                gl_FragColor = vec4(finalColor, v_Color.a);
            }
        """

        const val WIRE_VERTEX_SHADER = """
            uniform mat4 u_MVPMatrix;
            attribute vec4 a_Position;
            void main() {
                gl_Position = u_MVPMatrix * a_Position;
            }
        """

        const val WIRE_FRAGMENT_SHADER = """
            precision mediump float;
            uniform vec4 u_Color;
            void main() {
                gl_FragColor = u_Color;
            }
        """

        fun loadShader(type: Int, shaderCode: String): Int {
            val shader = GLES20.glCreateShader(type)
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
            return shader
        }

        fun createProgram(vertexCode: String, fragmentCode: String): Int {
            val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexCode)
            val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentCode)
            val program = GLES20.glCreateProgram()
            GLES20.glAttachShader(program, vertexShader)
            GLES20.glAttachShader(program, fragmentShader)
            GLES20.glLinkProgram(program)
            return program
        }
    }

    var programId: Int = 0
    var mvpMatrixHandle: Int = 0
    var mvMatrixHandle: Int = 0
    var sunDirHandle: Int = 0
    var sunColorHandle: Int = 0
    var ambientLightHandle: Int = 0
    var fogColorHandle: Int = 0
    var fogStartHandle: Int = 0
    var fogEndHandle: Int = 0

    var positionHandle: Int = 0
    var normalHandle: Int = 0
    var colorHandle: Int = 0
    var aoHandle: Int = 0

    // Wireframe selection shader handles
    var wireProgramId: Int = 0
    var wireMvpHandle: Int = 0
    var wirePosHandle: Int = 0
    var wireColorHandle: Int = 0

    fun init() {
        programId = createProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE)
        mvpMatrixHandle = GLES20.glGetUniformLocation(programId, "u_MVPMatrix")
        mvMatrixHandle = GLES20.glGetUniformLocation(programId, "u_MVMatrix")
        sunDirHandle = GLES20.glGetUniformLocation(programId, "u_SunDir")
        sunColorHandle = GLES20.glGetUniformLocation(programId, "u_SunColor")
        ambientLightHandle = GLES20.glGetUniformLocation(programId, "u_AmbientLight")
        fogColorHandle = GLES20.glGetUniformLocation(programId, "u_FogColor")
        fogStartHandle = GLES20.glGetUniformLocation(programId, "u_FogStart")
        fogEndHandle = GLES20.glGetUniformLocation(programId, "u_FogEnd")

        positionHandle = GLES20.glGetAttribLocation(programId, "a_Position")
        normalHandle = GLES20.glGetAttribLocation(programId, "a_Normal")
        colorHandle = GLES20.glGetAttribLocation(programId, "a_Color")
        aoHandle = GLES20.glGetAttribLocation(programId, "a_AO")

        // Wire program
        wireProgramId = createProgram(WIRE_VERTEX_SHADER, WIRE_FRAGMENT_SHADER)
        wireMvpHandle = GLES20.glGetUniformLocation(wireProgramId, "u_MVPMatrix")
        wirePosHandle = GLES20.glGetAttribLocation(wireProgramId, "a_Position")
        wireColorHandle = GLES20.glGetUniformLocation(wireProgramId, "u_Color")
    }
}
