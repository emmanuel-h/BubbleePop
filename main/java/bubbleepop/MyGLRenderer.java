/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package bubbleepop;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.util.Random;

/* MyGLRenderer implémente l'interface générique GLSurfaceView.Renderer */

public class MyGLRenderer implements GLSurfaceView.Renderer {

    // Les codes couleur pour les bulles avec une moitié de blanc (représentant les bulles sélectionnées par le joueur
    public static final float[] blackChoose = {1f,1f,1f,1f,1f,1f,1f,1f,0f,0f,0f,1f,0f,0f,0f,1f};
    public static final float[] redChoose = {1f,1f,1f,1f,1f,1f,1f,1f,1f,0f,0f,1f,1f,0f,0f,1f};
    public static final float[] purpleChoose = {1f,1f,1f,1f,1f,1f,1f,1f,1f,0f,1f,1f,1f,0f,1f,1f};
    public static final float[] yellowChoose = {1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,0f,1f,1f,1f,0f,1f};
    public static final float[] greenChoose = {1f,1f,1f,1f,1f,1f,1f,1f,0f,1f,0f,1f,0f,1f,0f,1f};
    public static final float[] blueChoose = {1f,1f,1f,1f,1f,1f,1f,1f,0f,0f,1f,1f,0f,0f,1f,1f};

    // Les codes couleur pour les bulles de couleur
    public static final float[] black = {0f,0f,0f,1f,0f,0f,0f,1f,0f,0f,0f,1f,0f,0f,0f,1f};
    public static final float[] white = {1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f,1f};
    public static final float[] red = {1f,0f,0f,1f,1f,0f,0f,1f,1f,0f,0f,1f,1f,0f,0f,1f};
    public static final float[] purple = {1f,0f,1f,1f,1f,0f,1f,1f,1f,0f,1f,1f,1f,0f,1f,1f};
    public static final float[] yellow = {1f,1f,0f,1f,1f,1f,0f,1f,1f,1f,0f,1f,1f,1f,0f,1f};
    public static final float[] green = {0f,1f,0f,1f,0f,1f,0f,1f,0f,1f,0f,1f,0f,1f,0f,1f};
    public static final float[] blue = {0f,0f,1f,1f,0f,0f,1f,1f,0f,0f,1f,1f,0f,0f,1f,1f};
    public static final float[] skyColor = {0f,153/255f,153/255f,1f,0f,153/255f,153/255f,1f,0f,153/255f,153/255f,1f,0f,153/255f,153/255f,1f};

    private static final String TAG = "MyGLRenderer";

    // Les matrices habituelles Model/View/Projection

    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];
    private final float[] mModelMatrix = new float[16];

    private int widthRef=5;
    private int heightRef=10;
    private int lengthPlayer=4;
    private int lengthSky=2;

    private Case[][] reference = new Case[heightRef][widthRef];

    /* Première méthode équivalente à la fonction init en OpenGLSL */
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {

        // la couleur du fond d'écran
        GLES30.glClearColor(0.0f, 153/255.0f, 153/255.0f, 1.0f);

        //Construction de la mtrice 10*5
        //On commence par le joueur UP
        float decalX=-8;
        float decalY=13;
        System.out.println("LUL");

        for(int i=0 ; i<lengthPlayer ; i++) {
            for (int j = 0; j < widthRef; j++) {
                reference[i][j] = new Case(decalX,decalY,white,"white");
                decalX += 4;
            }
            decalY-=2.5f;
            decalX=-8;
        }
        //On construit le ciel
        decalX=-8;
        decalY=1.5f;
        for(int i=0 ; i<lengthSky ; i++) {
            for (int j = 0; j < widthRef; j++) {
                reference[lengthPlayer+i][j] = new Case(decalX,decalY,white,"white");
                decalX += 4;
            }
            decalY-=2.5f;
            decalX=-8;
        }
        //Et on finit avec le joueur DOWN
        decalX=-8;
        decalY=-5.5f;
        for(int i=0 ; i<lengthPlayer ; i++) {
            for (int j = 0; j < widthRef; j++) {
                reference[lengthPlayer+lengthSky+i][j] = new Case(decalX,decalY,white,"white");
                decalX += 4;
            }
            decalY-=2.5f;
            decalX=-8;
        }
        initializePlayers();
        initializeSky();
    }

    //Initialise les 3 bulles blanches dans le camps des deux joueurs
    public void initializePlayers(){
        //j1
        reference[0][0].changed(black,"black");
        reference[0][2].changed(black,"black");
        reference[0][4].changed(black,"black");

        //j2
        reference[9][0].changed(black,"black");
        reference[9][2].changed(black,"black");
        reference[9][4].changed(black,"black");
    }

    /* Deuxième méthode équivalente à la fonction Display */
    @Override
    public void onDrawFrame(GL10 unused) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        float[] scratch;

        //On parcourt toutes nos bulles
        for(int i=0; i<reference.length;i++) {
            for (int j = 0; j < reference[0].length; j++) {
                //Si une bulle a changé il faudra la redessiner
                if(reference[i][j].hasChanged()) {
                    reference[i][j].createSquare();
                }
                scratch = new float[16]; // pour stocker une matrice


                /* on utilise une classe Matrix (similaire à glm) pour définir nos matrices P, V et M*/

                /* Pour le moment on va utiliser une projection orthographique
                   donc View = Identity
                 */

                /*pour positionner la caméra mais ici on n'en a pas besoin*/

                // Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);
                Matrix.setIdentityM(mViewMatrix, 0);

                // Calculate the projection and view transformation
                Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

                Matrix.setIdentityM(mModelMatrix, 0);

                /* Pour définir une translation on donne les paramètres de la translation
                et la matrice (ici mModelMatrix) est multipliée par la translation correspondante
                 */
                //Matrix.translateM(mModelMatrix, 0, mSquarePosition[0], mSquarePosition[1], 0);
                Matrix.translateM(mModelMatrix, 0, reference[i][j].getCoordx(), reference[i][j].getCoordy(), 0);

                /* scratch est la matrice PxVxM finale */
                Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mModelMatrix, 0);

                /* on appelle la méthode dessin du carré élémentaire */
                reference[i][j].getSquare().draw(scratch);
            }
        }
    }

    /* équivalent au Reshape en OpenGLSL */
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        /* ici on aurait pu se passer de cette méthode et déclarer
        la projection qu'à la création de la surface !!
         */
        float ratio = (float)height/(float)width;
        GLES30.glViewport(0, 0, width, height);
        Matrix.orthoM(mProjectionMatrix, 0, -10.0f, 10.0f, -10.0f*ratio, 10.0f*ratio, -1.0f, 1.0f);

    }

    /* La gestion des shaders ... */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES30.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES30.GL_FRAGMENT_SHADER)
        int shader = GLES30.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES30.glShaderSource(shader, shaderCode);
        GLES30.glCompileShader(shader);

        return shader;
    }


    /* Les méthodes nécessaires à la manipulation de la position finale du carré */
    public void setPosition(int coordX, int coordY, float x, float y) {
        reference[coordX][coordY].setCoord(x,y);
    }

    //Charge la nouvelle couleur dans une case donnée, elle sera mise à jour lors du prochain render
    public void setColor(int coordX, int coordY, float[] color, String colorName){
        reference[coordX][coordY].changed(color,colorName);
    }

    //On initialise le ciel avec deux boules de chaque couleur
    public void initializeSky(){
        String[] pool = new String[] {"green","blue","red","purple","yellow"};
        shuffleArray(pool);
        for(int i=0 ; i<widthRef ; i++) {
            reference[lengthPlayer][i].changed(matchColor(pool[i]),pool[i]);
            reference[lengthPlayer+1][i].changed(matchColor(pool[(i+2)%pool.length]),pool[(i+2)%pool.length]);
        }
    }

    //Mélange le tableau
    public static void shuffleArray(String[] a) {
        int n = a.length;
        Random random = new Random();
        random.nextInt();
        for (int i = 0; i < n; i++) {
            int change = i + random.nextInt(n - i);
            swap(a, i, change);
        }
    }

    //Echange deux éléments du tableau
    private static void swap(String[] a, int i, int change) {
        String helper = a[i];
        a[i] = a[change];
        a[change] = helper;
    }

    //renvoie le tableau de floats correspondant à une couleur
    public float[] matchColor(String color){
        switch (color){
            case "green":
                return green;
            case "blue":
                return blue;
            case "red":
                return red;
            case "purple":
                return  purple;
            case "black":
                return black;
            case  "yellow":
                return yellow;
            case "white":
                return white;
            default:
                return skyColor;
        }
    }

    //Renvoie la mtrice de Cases
    public Case[][] getReference(){
        return this.reference;
    }

    //Renvoie la taille de la zone de jeu d'un joueur
    public int getLengthPlayer(){
        return this.lengthPlayer;
    }

    //Renvoie la taille du ciel
    public int getLengthSky(){
        return this.lengthSky;
    }

    //Renvoie la couleur d'une bulle donnée
    public String getColor(int x, int y) {
        return reference[x][y].getColorName();
    }

    // Renvoie le code couleur d'une bulle selectionnée (avec une moitié de blanc dedans)
    public float[] colorChosen(String color){
        switch (color) {
            case "greenChoose":
                return greenChoose;
            case "blueChoose":
                return blueChoose;
            case "redChoose":
                return redChoose;
            case "purpleChoose":
                return purpleChoose;
            case "blackChoose":
                return blackChoose;
            case "yellowChoose":
                return yellowChoose;
            default:
                return skyColor;
        }
    }

    // Transforme une couleur en couleur sélectionnée
    public String colorToChoose(String color){
        switch (color) {
            case "green":case "greenChoose":
                return "greenChoose";
            case "blue":case "blueChoose":
                return "blueChoose";
            case "red":case "redChoose":
                return "redChoose";
            case "purple":case "purpleChoose":
                return "purpleChoose";
            case "black":case "blackChoose":
                return "blackChoose";
            case "yellow":case "yellowChoose":
                return "yellowChoose";
            default:
                return "skyColor";
        }
    }

    //Transforme une couleur sélectionnée en couleur simple
    public String chooseToColor(String color){
        switch (color) {
            case "greenChoose":case "green":
                return "green";
            case "blueChoose":case "blue":
                return "blue";
            case "redChoose":case "red":
                return "red";
            case "purpleChoose":case "purple":
                return "purple";
            case "blackChoose":case "black":
                return "black";
            case "yellowChoose":case "yellow":
                return "yellow";
            default:
                return "skyColor";
        }
    }
}
