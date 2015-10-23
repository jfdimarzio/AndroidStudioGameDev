package com.jfdimarzio.androidstudiogamedev;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by jerome on 10/22/2015.
 */
public class Hero {

    static float squareCoords[] = {
            -0.25f,  0.25f, 0.0f,   // top left
            -0.25f, -0.25f, 0.0f,   // bottom left
            0.25f, -0.25f, 0.0f,   // bottom right
            0.25f,  0.25f, 0.0f }; // top right

    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 };


    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 TexCoordIn;" +
                    "varying vec2 TexCoordOut;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  TexCoordOut = TexCoordIn;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "uniform sampler2D TexCoordIn;" +
                    "uniform float posX;" +
                    "uniform float posY;" +
                    "varying vec2 TexCoordOut;" +
                    "void main() {" +
                    " gl_FragColor = texture2D(TexCoordIn, vec2(TexCoordOut.x + posX ,TexCoordOut.y + posY ));" +
                    "}";
    private float texture[] = {
            0f, 0.25f,
            0f, 0f,
            0.25f, 0f,
            0.25f, 0.25f,
    };

    private int[] textures = new int[1];
    private final FloatBuffer vertexBuffer;
    private final ShortBuffer drawListBuffer;
    private final FloatBuffer textureBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mMVPMatrixHandle;

    static final int COORDS_PER_TEXTURE = 2;
    static final int COORDS_PER_VERTEX = 3;
    private final int vertexStride = COORDS_PER_VERTEX * 4;
    public static int textureStride = COORDS_PER_TEXTURE * 4;

    public void loadTexture(int texture, Context context) {
        InputStream imagestream = context.getResources().openRawResource(texture);
        Bitmap bitmap = null;

        android.graphics.Matrix flip = new android.graphics.Matrix();
        flip.postScale(-1f, -1f);

        try {

            bitmap = BitmapFactory.decodeStream(imagestream);

        }catch(Exception e){

        }finally {
            try {
                imagestream.close();
                imagestream = null;
            } catch (IOException e) {
            }
        }

        GLES20.glGenTextures(1, textures, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        bitmap.recycle();
    }
    public Hero() {
        ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        bb = ByteBuffer.allocateDirect(texture.length * 4); //
        bb.order(ByteOrder.nativeOrder()); //
        textureBuffer = bb.asFloatBuffer(); //
        textureBuffer.put(texture); //
        textureBuffer.position(0); //

        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

        int vertexShader = GameRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = GameRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mvpMatrix, float posX, float posY) {

        GLES20.glUseProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        GLES20.glEnableVertexAttribArray(mPositionHandle);

        int vsTextureCoord = GLES20.glGetAttribLocation(mProgram, "TexCoordIn"); //


        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);


        GLES20.glVertexAttribPointer(vsTextureCoord, COORDS_PER_TEXTURE,
                GLES20.GL_FLOAT, false,
                textureStride, textureBuffer);

        GLES20.glEnableVertexAttribArray(vsTextureCoord);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        int fsTexture = GLES20.glGetUniformLocation(mProgram, "TexCoordOut");
        int fsPosX = GLES20.glGetUniformLocation(mProgram, "posX");
        int fsPosY = GLES20.glGetUniformLocation(mProgram, "posY");
        GLES20.glUniform1i(fsTexture, 0);
        GLES20.glUniform1f(fsPosX, posX);
        GLES20.glUniform1f(fsPosY, posY);
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GameRenderer.checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        GameRenderer.checkGlError("glUniformMatrix4fv");

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
