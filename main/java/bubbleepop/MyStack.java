package bubbleepop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by emmanuelh on 21/04/17.
 */

// Représente la pioche
public class MyStack {

    private List<String> deck;

    public MyStack(){
        deck = new ArrayList<>();
        for(int i=0 ; i<14 ; i++){
            deck.add("red");
        }
        for(int i=0 ; i<14 ; i++){
            deck.add("purple");
        }
        for(int i=0 ; i<14 ; i++){
            deck.add("yellow");
        }
        for(int i=0 ; i<14 ; i++){
            deck.add("blue");
        }
        for(int i=0 ; i<14 ; i++){
            deck.add("green");
        }
        for(int i=0 ; i<3 ; i++){
            deck.add("black");
        }
        Collections.shuffle(deck);
    }

    public String pop(){
        String nextBubble = this.deck.get(0);
        this.deck.remove(0);
        return nextBubble;
    }

    public int length(){
        return this.deck.size();
    }
}
