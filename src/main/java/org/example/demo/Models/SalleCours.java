package org.example.demo.Models;
public class SalleCours extends Batiment {
    private int num;
    public SalleCours(int id, String nom, int capacite, int cons_res,int satisfaction, int num,int gridX, int gridY) {
        super(id, nom, "SalleCours", capacite,cons_res, satisfaction, gridX, gridY);
        this.num=num;

    }

    @Override
    public void utiliser() {
        System.out.println(getNom() + " est utilisée pour un cours.");
    }
}
