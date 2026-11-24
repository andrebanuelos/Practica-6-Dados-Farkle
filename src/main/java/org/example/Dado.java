package org.example;
import java.util.Random;
public class Dado
{
    private int numeroCaras;
    private int valorVisible;

    public Dado(){
        numeroCaras=6;
        valorVisible=1;

    }
    public Dado(int numeroCaras){
        setNumeroCaras(numeroCaras);
        valorVisible=1;
    }
    public void setNumeroCaras(int numeroCaras){
        if(numeroCaras < 6){
            throw new IllegalArgumentException("El dado debe tener 6 o mas caras");
        }
        this.numeroCaras=numeroCaras;
        if(valorVisible > numeroCaras){
            valorVisible=numeroCaras;
        }
    }
    public int getNumeroCaras(){
        return numeroCaras;
    }
    public void lanzar(){
        Random rmd=new Random();
        valorVisible=rmd.nextInt(numeroCaras)+1;
    }
    public int getValorVisible(){
        return valorVisible;
    }
    public String toString(){
        return "Dado con " + numeroCaras +  " lados cuyo valor actual es " + valorVisible;
    }
}

