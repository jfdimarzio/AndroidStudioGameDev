package com.jfdimarzio.androidstudiogamedev;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by jfdimarzio on 10/21/2015.
 */
public class GameRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "GameRenderer";
    private Context context;
    public static float[] mMVPMatrix = new float[16];
    public static float[] mProjectionMatrix = new float[16];
    public static float[] mViewMatrix = new float[16];
    public static float[] mTranslationMatrix = new float[16];

    private Starfield starfield;
    private Debris debris;
    private Hero hero;

    float starfieldScroll = 0;
    float debrisScroll = 0;

    float heroSprite = 0;

    float heroMove = 0;

    public GameRenderer(Context gameContext) {
        context = gameContext;


    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        starfield = new Starfield();
        debris = new Debris();
        hero = new Hero();

        starfield.loadTexture(R.drawable.starfield, context);
        debris.loadTexture(R.drawable.debris, context);
        hero.loadTexture(R.drawable.ships, context);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);

    }

    @Override
    public void onDrawFrame(GL10 unused) {
        float[] matrix = new float[16];

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        starfield.draw(mMVPMatrix, starfieldScroll);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //debris.draw(mMVPMatrix, debrisScroll);
        Matrix.setIdentityM(mTranslationMatrix,0);
        Matrix.translateM(mTranslationMatrix, 0,heroMove,-.5f,0);

        Matrix.multiplyMM(matrix, 0, mMVPMatrix, 0, mTranslationMatrix, 0);

        hero.draw(matrix,0,0);

        GLES20.glDisable(GLES20.GL_BLEND);

        if(starfieldScroll == Float.MAX_VALUE){
            starfieldScroll = 0;
        }
        if(debrisScroll == Float.MAX_VALUE){
            debrisScroll = 0;
        }
        starfieldScroll += .001;
        debrisScroll += .01;


    }

    public static int loadShader(int type, String shaderCode){

        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public void setHeroMove(float movement){
        heroMove = movement;
    }
    public float getHeroMove(){
        return heroMove;
    }
}
