package org.example.excel;

public class Pair<T, V>{
    private T firstEl;
    private V secondEl;
    private boolean prepared = false;

    private boolean isPrepared(){
        return prepared;
    }
    public Pair(){}

    public Pair(T firstEl, V secondEl){
        prepared = true;
        this.firstEl = firstEl;
        this.secondEl = secondEl;

    }
    public T getFirstEl(){
        if(!isPrepared())
            throw new IllegalCallerException("Pair isnt preprared");
        return firstEl;
    }

    public V getSecondEl(){
        if(!isPrepared())
            throw new IllegalCallerException("Pair isnt preprared");
        return secondEl;
    }

    public void setFirstEl(T firstEl){
        prepared = true;
        this.firstEl = firstEl;
    }

    public void setSecondEl(V secondEl){
        prepared = true;
        this.secondEl = secondEl;
    }
}
