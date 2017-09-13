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

import android.content.Context;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/* La classe MyGLSurfaceView avec en particulier la gestion des événements
  et la création de l'objet renderer

*/


/* On va dessiner un carré qui peut se déplacer grace à une translation via l'écran tactile */

public class MyGLSurfaceView extends GLSurfaceView {

    /* Un attribut : le renderer (GLSurfaceView.Renderer est une interface générique disponible) */
    /* MyGLRenderer va implémenter les méthodes de cette interface */

    private final MyGLRenderer mRenderer;
    private final MyStack deck = new MyStack();
    Context context;

    public MyGLSurfaceView(Context context) {
        super(context);
        this.context=context;
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        // Création d'un context OpenGLES 2.0
        setEGLContextClientVersion(3);

        // Création du renderer qui va être lié au conteneur View créé
        mRenderer = new MyGLRenderer();
        setRenderer(mRenderer);

        // Option pour indiquer qu'on redessine uniquement si les données changent
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    /* pour gérer la translation */
    //Les positions précédentes du pointeur
    private float mPreviousX=-1;
    private float mPreviousY=-1;
    //Les coordonnées précédentes
    private int mPreviousCoordX=-1;
    private int mPreviousCoordY=-1;
    private boolean condition = false;
    //Nombre de wipe auxquels l'utilisateur a le droit
    private final int NUMBER_OF_SWIPE=1;
    //Nombre restants de swipes disponibles
    private int hasSwipe=NUMBER_OF_SWIPE;
    //C'est au joueur DOWN de jouer quand true, UP quand false
    private boolean turnDown=true;
    //Coordonées de la première bulle touchée
    private int firstBubbleX=-1;
    private int firstBubbleY=-1;
    //Si un joueur a gagné
    private boolean win=false;
    //Nombre de bulles déjà sélectionnées par l'utilisateur
    private int selectNumber=0;

    /* Comment interpréter les événements sur l'écran tactile */
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // Les coordonnées du point touché sur l'écran
        float x = e.getX();
        float y = e.getY();

        // la taille de l'écran en pixels
        float screen_x = getWidth();
        float screen_y = getHeight();

        /* accès aux paramètres du rendu (cf MyGLRenderer.java)
        soit la position courante du centre du carré
         */
        //float[] pos = mRenderer.getPosition();

        /* Conversion des coordonnées pixel en coordonnées OpenGL
        Attention l'axe x est inversé par rapport à OpenGLSL
        On suppose que l'écran correspond à un carré d'arete 2 centré en 0
         */

        float x_opengl = 20.0f*x/getWidth() - 10.0f;
        float y_opengl = -20.0f*y/getHeight() + 10.0f;

        /* Le carré représenté a une arete de 2 (oui il va falloir changer cette valeur en dur !!)
        /* On teste si le point touché appartient au carré ou pas car on ne doit le déplacer que si ce point est dans le carré
        */

        int coordX = -1;
        int coordY = -1;
        boolean test_square=false;
        Case[][] reference = mRenderer.getReference();
        for(int i=mRenderer.getLengthPlayer() ; i<mRenderer.getLengthPlayer()+mRenderer.getLengthSky() ; i++){
            for(int j=0 ; j<reference[0].length ; j++){
                test_square = ((x_opengl < reference[i][j].getCoordx()+1.0) && (x_opengl > reference[i][j].getCoordx()-1.0)
                        && (y_opengl < reference[i][j].getCoordy()+1.0) && (y_opengl > reference[i][j].getCoordy()-1.0));
                if(test_square){
                    coordX = j;
                    coordY = i;
                    i=reference.length;
                    break;
                }
            }
        }
        if (condition || test_square) {

            switch (e.getAction()) {
                /* Lorsqu'on touche l'écran on mémorise juste le point */
                case MotionEvent.ACTION_DOWN:
                    mPreviousX = x;
                    mPreviousY = y;
                    mPreviousCoordX=coordX;
                    mPreviousCoordY=coordY;
                    condition=true;
                    break;
                case MotionEvent.ACTION_UP:
                        if (mPreviousCoordY != -1 && mPreviousCoordX != -1) {
                            float deltaX = mPreviousX - x;
                            float deltaY = mPreviousY - y;
                            //swipe right
                            if (Math.abs(deltaX) > Math.abs(deltaY) && deltaX < 0) {
                                if (mPreviousCoordX < mRenderer.getReference()[0].length - 1) {
                                    coordX = mPreviousCoordX + 1;
                                    coordY = mPreviousCoordY;
                                }
                            }
                            //swipe left
                            if (Math.abs(deltaX) > Math.abs(deltaY) && deltaX > 0) {
                                if (mPreviousCoordX > 0) {
                                    coordX = mPreviousCoordX - 1;
                                    coordY = mPreviousCoordY;
                                }
                            }
                            //swipe down
                            if (Math.abs(deltaX) < Math.abs(deltaY) && deltaY < 0) {
                                if (mPreviousCoordY != mRenderer.getLengthPlayer()) {
                                    coordY = mPreviousCoordY - 1;
                                    coordX = mPreviousCoordX;
                                }
                            }
                            //swipe up
                            if (Math.abs(deltaX) < Math.abs(deltaY) && deltaY > 0) {
                                if (mPreviousCoordY != mRenderer.getLengthPlayer() + 1) {
                                    coordY = mPreviousCoordY + 1;
                                    coordX = mPreviousCoordX;
                                }
                            }

                            // Si l'utilisateur veut faire tomber une paire
                            if (deltaY == 0 && deltaX == 0) {
                                if(selectNumber == 0){
                                    selectNumber = 1;
                                    firstBubbleX = coordX;
                                    firstBubbleY = coordY;
                                    String colorChoose = mRenderer.colorToChoose(mRenderer.getColor(coordY,coordX));
                                    mRenderer.setColor(coordY,coordX,mRenderer.colorChosen(colorChoose),colorChoose);
                                }
                                //Si les deux bulles touchées ne sont pas à côté on reset
                                else if(!sideBySide(firstBubbleX,firstBubbleY,coordX,coordY)){
                                    selectNumber=0;
                                    String colorNotChoose = mRenderer.chooseToColor(mRenderer.getColor(firstBubbleY,firstBubbleX));
                                    mRenderer.setColor(firstBubbleY,firstBubbleX,mRenderer.matchColor(colorNotChoose),colorNotChoose);
                                    firstBubbleX=-1;
                                    firstBubbleY=-1;
                                    mPreviousCoordX=-1;
                                    mPreviousCoordY=-1;
                                } else {
                                    //Les deux bulles selectionnées sont legit, on peut commencer l'algorithme
                                    String colorNotChoose = mRenderer.chooseToColor(mRenderer.getColor(firstBubbleY,firstBubbleX));
                                    mRenderer.setColor(firstBubbleY,firstBubbleX,mRenderer.matchColor(colorNotChoose),colorNotChoose);
                                    selectNumber=0;
                                    if (turnDown) {

                                        //Remet les bulles dans le bon ordre
                                        int temp;
                                        if(firstBubbleY<coordY) {
                                            temp = coordY;
                                            coordY = firstBubbleY;
                                            firstBubbleY = temp;
                                        }

                                        // Pour les deux chutes de bulles, on vérifie si on a le droit de les faire tomber
                                        if (!canIFall(firstBubbleX, mRenderer.getLengthPlayer() + mRenderer.getLengthSky())) {
                                            loose();
                                            win=true;
                                        }
                                        moveBubbleDown(firstBubbleX, firstBubbleY);
                                        toSimplify("down");

                                        if (!canIFall(coordX, mRenderer.getLengthPlayer() + mRenderer.getLengthSky())) {
                                            loose();
                                            win=true;
                                        }
                                        moveBubbleDown(coordX, coordY);
                                        toSimplify("down");
                                        if(!win) {
                                            Toast.makeText(context, "Turn Player Up", Toast.LENGTH_SHORT).show();
                                        }

                                    } else {

                                        //Remet les bulles dans le bon ordre
                                        int temp;
                                        if (firstBubbleY > coordY) {
                                            temp = coordY;
                                            coordY = firstBubbleY;
                                            firstBubbleY = temp;
                                        }

                                        //coordY = mRenderer.getLengthPlayer();
                                        if (!canIFall(firstBubbleX, mRenderer.getLengthPlayer() - 1)) {
                                            loose();
                                            win = true;
                                        }
                                        moveBubbleUp(firstBubbleX, firstBubbleY);
                                        toSimplify("up");

                                        if (!canIFall(coordX, mRenderer.getLengthPlayer() - 1)) {
                                            loose();
                                            win = true;
                                        }
                                        moveBubbleUp(coordX, coordY);
                                        toSimplify("up");
                                        if (!win) {
                                            Toast.makeText(context, "Turn Player Down", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    turnDown = !turnDown;
                                    hasSwipe = NUMBER_OF_SWIPE;
                                }
                                // S'il reste des bulles dans la pioche on remplit le ciel
                                if(deck.length()>0) {
                                    fillSky();
                                } else {
                                    exaequo();
                                }
                            // Si l'utilisateur veut faire pivoter deux bulles
                            } else {
                                //Si les coordonnées sont bonnes et qu'il nous reste des swipes
                                if (coordX != -1 && coordY != -1 && hasSwipe > 0) {

                                    // Si une bulle avait été précedemment sélectionnée, on la désélectionne
                                    if (firstBubbleX != -1 && firstBubbleY != -1) {
                                        String colorNotChoose = mRenderer.chooseToColor(mRenderer.getColor(firstBubbleY, firstBubbleX));
                                        mRenderer.setColor(firstBubbleY, firstBubbleX, mRenderer.matchColor(colorNotChoose), colorNotChoose);
                                    }

                                    // Echange des deux couleurs
                                    String color1 = mRenderer.getColor(mPreviousCoordY, mPreviousCoordX);
                                    mRenderer.setColor(mPreviousCoordY, mPreviousCoordX, mRenderer.matchColor(mRenderer.getColor(coordY, coordX)), mRenderer.getColor(coordY, coordX));
                                    mRenderer.setColor(coordY, coordX, mRenderer.matchColor(color1), color1);
                                    mPreviousCoordX = -1;
                                    mPreviousCoordY = -1;
                                    hasSwipe--;
                                    selectNumber=0;
                                }
                            }
                        }
                        condition = false;

            }
        }
        requestRender();
        return true;
    }

    //Teste si les deux bulles sont bien côte à côte
    private boolean sideBySide(int firstBubbleX, int firstBubbleY, int coordX, int coordY) {
        if(turnDown == true && coordY == mRenderer.getLengthPlayer() && firstBubbleY == mRenderer.getLengthPlayer()){
            return false;
        }
        if(turnDown == false && coordY == mRenderer.getLengthPlayer()+1 && firstBubbleY == mRenderer.getLengthPlayer()+1){
            return false;
        }
        if(!(Math.abs(firstBubbleX-coordX)+Math.abs(firstBubbleY-coordY) == 1)){
            return false;
        }
        return true;
    }

    // Remplit le ciel après qu'un joueur ait fait tomber deux bulles
    private void fillSky() {
        String newColor;
        for(int i=mRenderer.getLengthPlayer() ; i<mRenderer.getLengthPlayer()+mRenderer.getLengthSky() ; i++) {
            for (int j = 0; j < mRenderer.getReference()[0].length; j++) {
                if(mRenderer.getReference()[i][j].getColorName().equals("white")){
                    newColor = deck.pop();
                    mRenderer.getReference()[i][j].changed(mRenderer.matchColor(newColor),newColor);
                }
            }
        }
    }

    // La partie termine sur un match nul
    private void exaequo() {
        Intent myIntent = new Intent(context, OpenGLES20Activity.class);
        Toast.makeText(context, "No more Bubbles, it's a TIE", Toast.LENGTH_LONG).show();
        context.startActivity(myIntent);
    }

    // La partie est loose pour un des deux joueurs
    private void loose() {
        Intent myIntent = new Intent(context, OpenGLES20Activity.class);
        if(turnDown) {
            myIntent.putExtra("winner","Player UP win");
        } else {
            myIntent.putExtra("winner","Player DOWN win");
        }
        context.startActivity(myIntent);
    }

    // Calcule si la bulle peut tomber à cet emplacement
    private boolean canIFall(int coordX, int coordY) {
        return(mRenderer.getReference()[coordY][coordX].getColorName().equals("white"));
    }

    // A chaque fois qu'une simplification a lieu, on en relance une autre au cas où cette simplification en ait permise une autre
    private void toSimplify(String pos){
        boolean simplifyAgain=true;
        while(simplifyAgain){
            simplifyAgain=simplify(pos);
        }
    }

    // Simplifie le jeu par rapport à une boule donnée. Renvoie vrai si une simplification a été effectuée
    private boolean simplify(String pos) {
        Case[][] reference = mRenderer.getReference();
        String color;
        List<Couple> toSimplify;
        int beginI;
        int endI;
        int beginJ = 0;
        int endJ = reference[0].length;
        if(pos.equals("up")){
            beginI=0;
            endI=mRenderer.getLengthPlayer();
        } else {
            beginI=mRenderer.getLengthPlayer()+mRenderer.getLengthSky();
            endI=mRenderer.getLengthPlayer()*2+mRenderer.getLengthSky();
        }
        // Pour chaque case dans la partie d'un joueur
        for(int i=beginI ; i<endI ; i++){
            for(int j=beginJ ; j<endJ ; j++){
                color = reference[i][j].getColorName();
                // Si la bulle n'est pas blanche il faut vérifier son voisinage
                if(!color.equals("white")){
                    if(detectLine(i,j,color) >= 3){
                        toSimplify = new ArrayList<>();
                        toSimplify.add(new Couple(i,j));
                        testColor(i,j,color,toSimplify,pos);
                        for(int k=0 ; k<toSimplify.size() ; k++){
                            mRenderer.setColor(toSimplify.get(k).getX(),toSimplify.get(k).getY(),mRenderer.matchColor("white"),"white");
                        }
                        // On fait ensuite tomber les boules dans les cases libérées
                        if(pos.equals("up")) {
                            for (int k = beginI; k < endI; k++) {
                                for (int l = beginJ; l < endJ; l++) {
                                    moveBubbleUp(l, k);
                                }
                            }
                        } else {
                            for (int k = endI-1; k >= beginI; k--) {
                                for (int l = beginJ; l < endJ; l++) {
                                    moveBubbleDown(l, k);
                                }
                            }

                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private int detectLine(int i, int j, String color) {
        int sameColor = 1;
        Case[][]reference = mRenderer.getReference();
        // A-t-on une colonne de 3 au moins ?
        for(int ii = i+1 ; ii<reference.length ; ii++){
            if(reference[ii][j].getColorName().equals(color)){
                sameColor++;
            } else {
                break;
            }
        }
        if(sameColor >= 3){
            return sameColor;
        }
        sameColor = 1;
        // A-t-on une ligne de trois au moins ?
        for(int jj = j+1 ; jj<reference[0].length ; jj++){
            if(reference[i][jj].getColorName().equals(color)){
                sameColor++;
            } else {
                break;
            }
        }
        return sameColor;
    }

    // récupération de toutes les billes de même couleur adjacentes les unes aux autres par rapport à un point de départ
    private List<Couple> testColor(int x, int y, String color, List<Couple> toSimplify, String pos) {
        ArrayList<Couple> neighborhood = getNeighborhood(x,y,pos);
        Iterator iterator = neighborhood.iterator();
        Couple test;
        // On supprime les voisins de la même couleur que l'on a déjà parcourus
        while (iterator.hasNext()) {
            test=(Couple)iterator.next();
            if(toSimplify.contains(test)){
                iterator.remove();
            }
        }
        // Pour chaque voisin de la même couleur, on le rajoute à la liste de bulles à simplifier
        for(int i=0 ; i< neighborhood.size() ; i++){
            if(mRenderer.getReference()[neighborhood.get(i).getX()][neighborhood.get(i).getY()].getColorName().equals(color)){
                toSimplify.add(neighborhood.get(i));
            }
        }

        //Puis pour chacun de nos voisins de la même couleur on rappelle récursivement cette méthode pour voir leurs voisins
        for(int i=0 ; i< neighborhood.size() ; i++){
            if(mRenderer.getReference()[neighborhood.get(i).getX()][neighborhood.get(i).getY()].getColorName().equals(color)) {
                testColor(neighborhood.get(i).getX(), neighborhood.get(i).getY(), color, toSimplify, pos);
            }
        }
        return toSimplify;
    }

    // Renvoie toutes les cases adjacentes à une position
    private ArrayList<Couple> getNeighborhood(int x, int y ,String pos) {
        int lengthPlayer = mRenderer.getLengthPlayer();
        int lengthSky = mRenderer.getLengthSky();
        ArrayList<Couple>neighbours = new ArrayList<>();
        //Joueur up Position haut
        if(pos.equals("up") && x-1 >= 0){
            neighbours.add(new Couple(x-1,y));
        }
        //Joueur up Position bas
        if(pos.equals("up") && x+1 < lengthPlayer){
            neighbours.add(new Couple(x+1,y));
        }
        //Joueur down Position haut
        if(pos.equals("down") && x-1 >=lengthPlayer+lengthSky){
            neighbours.add(new Couple(x-1,y));
        }
        //Joueur down Position bas
        if(pos.equals("down") && x+1 <lengthPlayer*2+lengthSky){
            neighbours.add(new Couple(x+1,y));
        }
        //Gauche
        if(y-1 >= 0){
            neighbours.add(new Couple(x,y-1));
        }
        //Droite
        if(y+1 < 5){
            neighbours.add(new Couple(x,y+1));
        }
        return neighbours;
    }

    // Fait tomber une bulle dans le sens du joueur UP
    private void moveBubbleUp(int coordX, int coordY) {
        String color = mRenderer.getReference()[coordY][coordX].getColorName();
        while ((coordY > 0) && (mRenderer.getReference()[coordY - 1][coordX].getColorName().equals("white"))){
            mRenderer.setColor(coordY - 1, coordX, mRenderer.matchColor(color), color);
            mRenderer.setColor(coordY, coordX, mRenderer.matchColor("white"), "white");
            coordY--;
        }
    }

    // Fait tomber une bulle dans le sens du joueur DOWN
    private void moveBubbleDown(int coordX, int coordY) {
        String color = mRenderer.getReference()[coordY][coordX].getColorName();
        while ((coordY < mRenderer.getReference().length - 1) && (mRenderer.getReference()[coordY + 1][coordX].getColorName().equals("white"))){
            mRenderer.setColor(coordY + 1, coordX, mRenderer.matchColor(color), color);
            mRenderer.setColor(coordY, coordX, mRenderer.matchColor("white"), "white");
            coordY++;
        }
    }
}
