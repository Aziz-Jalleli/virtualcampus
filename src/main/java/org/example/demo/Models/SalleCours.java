package org.example.demo.Models;
public class SalleCours extends Batiment {
    private int num;
    public SalleCours(int id, String nom, int capacite, double cons_res,int satisfaction, int num) {
        super(id, nom, "Salle de cours", capacite,cons_res, satisfaction);
        this.num=num;

    }

    @Override
    public void utiliser() {
        System.out.println(getNom() + " est utilisée pour un cours.");
    }
}
