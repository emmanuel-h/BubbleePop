package bubbleepop;

/**
 * Created by emmanuelh on 12/04/17.
 */

//Représente le contenu d'une bulle sur le plateau
public class Case {

    //Le nom de la couleur
    private String colorName;
    //true si un changement a été apporté et que la bulle doit être redessinée
    private boolean changed;
    //Coordonnées en pixels de la position sur l'écran de la bulle
    private float[] coords;
    //Square associé à la bulle
    private Square square;
    //Nouvelle couleur a dessiner
    private float[] newColor;

    public Case(float x, float y, float[] color, String colorName){
        this.changed=true;
        this.coords = new float[2];
        this.coords[0]=x;
        this.coords[1]=y;
        this.colorName = colorName;
        this.newColor = new float[color.length];
        for(int i=0 ; i<color.length ; i++){
            this.newColor[i]=color[i];
        }
    }

    //Si les coordonnées changent, on les modifie directement dans le square
    public void setCoord(float x, float y) {
        this.coords[0]=x;
        this.coords[1]=y;
        this.square.set_position(this.coords);
    }

    public float getCoordx(){
        return this.coords[0];
    }

    public float getCoordy(){
        return this.coords[1];
    }

    public String getColorName() {
        return colorName;
    }

    public Square getSquare() {
        return this.square;
    }

    public boolean hasChanged() {
        return this.changed;
    }

    //Quand la couleur de la bulle a changé, on prépare les modification pour le futur affichage
    public void changed(float[] newColor, String colorName) {
        this.colorName=colorName;
        this.changed = true;
        this.newColor = new float[newColor.length];
        for(int i=0 ; i<newColor.length ; i++){
            this.newColor[i] = newColor[i];
        }
    }

    //Création d'une nouveau square
    public void createSquare(){
        this.square = new Square(coords, newColor);
        this.changed=false;
    }

    public String toString(){
        String toString=colorName;
        for(int i=0;i<newColor.length;i++) {
            toString += newColor[i];
        }
        return toString;
    }
}
