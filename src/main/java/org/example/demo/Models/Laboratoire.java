package org.example.demo.Models;


public class Laboratoire extends Batiment {

    public Laboratoire(int id, String nom, int capacite, double cons_res,int gridX,int gridY) {
        super(id, nom, "Laboratoire", capacite, cons_res, 7,gridX,gridY);
    }

    @Override
    public void utiliser() {
        System.out.println(getClass() + " est utilisée pour les expériences scientifiques.");
    }
}
