package model;

import Models.Batiment;

public class Laboratoire extends Batiment {

    public Laboratoire(int id, String nom, int capacite, double cons_res) {
        super(id, nom, "Laboratoire", capacite, cons_res, 7);
    }

    @Override
    public void utiliser() {
        System.out.println(getClass() + " est utilisée pour les expériences scientifiques.");
    }
}
