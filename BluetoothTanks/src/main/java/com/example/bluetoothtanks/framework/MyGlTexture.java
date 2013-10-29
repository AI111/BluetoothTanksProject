package com.example.bluetoothtanks.framework;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Sasha on 28.08.13.
 */
public class MyGlTexture {

    String fileName;
    GL10 gl;
    Resources res;
    public int textureId;
    int minFilter;
    int magFilter;

    public MyGlTexture(GL10 gl,Resources res,String fileName){
        this.gl=gl;
        this.fileName=fileName;
        this.res=res;
        load();

    }
    private void load() {
        AssetManager assetManager =res.getAssets();
        int[] textureIds = new int[1];
        gl.glGenTextures(1, textureIds, 0);
        textureId = textureIds[0];
        InputStream in = null;
        Log.v("BITMAP RESOLUTION ", " *************************************");
        Log.v("BITMAP RESOLUTION ", " *************************************");
        Log.v("BITMAP RESOLUTION ", " *************************************");
        try {
            in = assetManager.open(fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            Log.v("BITMAP RESOLUTION ", " WIGHT " + bitmap.getWidth() + " HIGHT " + bitmap.getWidth());
            gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
            setFilters(GL10.GL_NEAREST, GL10.GL_NEAREST);
            gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
        } catch(IOException e) {
            throw new RuntimeException("Couldn't load texture '"
                    + fileName +"'", e);
        } finally {
            if(in != null)
                try { in.close(); } catch (IOException e) { }
        }
    }
    public void reload() {
        load();
        bind();
        setFilters(minFilter, magFilter);

        gl.glBindTexture(GL10.GL_TEXTURE_2D, 0);
    }
    public void setFilters(int minFilter, int magFilter) {
        this.minFilter = minFilter;
        this.magFilter = magFilter;

        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER,
                minFilter);
        gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER,
                magFilter); }
    public void bind() {

        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
    }
    public void dispose() {

        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureId);
        int[] textureIds = { textureId };
        gl.glDeleteTextures(1, textureIds, 0);
}
}
