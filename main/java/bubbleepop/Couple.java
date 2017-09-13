package bubbleepop;

/**
 * Created by emmanuelh on 24/04/17.
 */


// Produit un couple. Cette classe est utilisée lorsqu'il faut lister tous les voisins d'une bulle
// pour effectuer une réduction si des bulles adjacentes ont la même couleur
public class Couple {
    private int x;
    private int y;

    public Couple(int x, int y){
        this.x=x;
        this.y=y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String toString(){
        return x+"-"+y;
    }

    public boolean equals(Object o){
        return this.x==((Couple)o).getX() && this.y==((Couple)o).getY();
    }
}
