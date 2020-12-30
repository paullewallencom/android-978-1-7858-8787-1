package com.cardbookvr.renderbox.materials;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.cardbookvr.renderbox.R;
import com.cardbookvr.renderbox.RenderBox;
import com.cardbookvr.renderbox.components.RenderObject;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Schoen and Jonathan on 4/21/2016.
 */
public class DiffuseLightingMaterial extends Material {
    private static final String TAG = "diffuselightingmaterial";

    int textureId;
    static int program = -1;
    //Initialize to a totally invalid value for setup state
    static int positionParam;
    static int texCoordParam;
    static int textureParam;
    static int normalParam;
    static int MVParam;
    static int MVPParam;
    static int lightPosParam;
    static int lightColParam;

    FloatBuffer vertexBuffer;
    FloatBuffer texCoordBuffer;
    FloatBuffer normalBuffer;
    ShortBuffer indexBuffer;
    int numIndices;

    public DiffuseLightingMaterial(int resourceId) {
        super();
        setupProgram();
        this.textureId = RenderBox.loadTexture(resourceId);
    }

    public static void setupProgram() {
        //Already setup?
        if (program != -1) return;

        //Create shader program
        program = createProgram(R.raw.diffuse_lighting_vertex, R.raw.diffuse_lighting_fragment);
        RenderBox.checkGLError("Diffuse Texture Color Lighting shader compile");

        //Get vertex attribute parameters
        positionParam = GLES20.glGetAttribLocation(program, "a_Position");
        normalParam = GLES20.glGetAttribLocation(program, "a_Normal");
        texCoordParam = GLES20.glGetAttribLocation(program, "a_TexCoordinate");

        //Enable them (turns out this is kind of a big deal ;)
        GLES20.glEnableVertexAttribArray(positionParam);
        GLES20.glEnableVertexAttribArray(normalParam);
        GLES20.glEnableVertexAttribArray(texCoordParam);

        //Shader-specific parameters
        textureParam = GLES20.glGetUniformLocation(program, "u_Texture");
        MVParam = GLES20.glGetUniformLocation(program, "u_MV");
        MVPParam = GLES20.glGetUniformLocation(program, "u_MVP");
        lightPosParam = GLES20.glGetUniformLocation(program, "u_LightPos");
        lightColParam = GLES20.glGetUniformLocation(program, "u_LightCol");

        RenderBox.checkGLError("Diffuse Texture Color Lighting params");
    }

    public void setBuffers(FloatBuffer vertexBuffer, FloatBuffer normalBuffer,
                           FloatBuffer texCoordBuffer, ShortBuffer indexBuffer, int numIndices) {
        //Associate VBO data with this instance of the material
        this.vertexBuffer = vertexBuffer;
        this.normalBuffer = normalBuffer;
        this.texCoordBuffer = texCoordBuffer;
        this.indexBuffer = indexBuffer;
        this.numIndices = numIndices;
    }

    @Override
    public void draw(float[] view, float[] perspective) {
        GLES20.glUseProgram(program);

        // Set the active texture unit to texture unit 0.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        // Bind the texture to this unit.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        // Tell the texture uniform sampler to use this texture in
        // the shader by binding to texture unit 0.
        GLES20.glUniform1i(textureParam, 0);

        //Technically, we don't need to do this with every draw
        //call, but the light could move.
        //We could also add a step for shader-global parameters
        //which don't vary per-object
        GLES20.glUniform3fv(lightPosParam, 1, RenderBox.instance.mainLight.lightPosInEyeSpace, 0);
        GLES20.glUniform4fv(lightColParam, 1, RenderBox.instance.mainLight.color, 0);

        Matrix.multiplyMM(modelView, 0, view, 0, RenderObject.lightingModel, 0);
        // Set the ModelView in the shader, used to calculate
        // lighting
        GLES20.glUniformMatrix4fv(MVParam, 1, false, modelView, 0);
        Matrix.multiplyMM(modelView, 0, view, 0, RenderObject.model, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        // Set the ModelViewProjection matrix for eye position.
        GLES20.glUniformMatrix4fv(MVPParam, 1, false, modelViewProjection, 0);

        //Set vertex attributes
        GLES20.glVertexAttribPointer(positionParam, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
        GLES20.glVertexAttribPointer(normalParam, 3, GLES20.GL_FLOAT, false, 0, normalBuffer);
        GLES20.glVertexAttribPointer(texCoordParam, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, numIndices, GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        RenderBox.checkGLError("Diffuse Texture Color Lighting draw");
    }

}
