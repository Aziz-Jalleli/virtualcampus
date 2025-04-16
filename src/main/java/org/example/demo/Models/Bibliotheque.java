package org.example.demo.Models;
public class Bibliotheque extends Batiment {

    public Bibliotheque(int id, String nom, int capacite, int cons_res,int gridX,int gridY ) {
        super(id, nom, "Bibliotheque", capacite, cons_res, 8,gridX,gridY);
    }

    @Override
    public void utiliser() {
        System.out.println(getNom() + " est utilisée pour étudier ou emprunter des livres.");
    }
}
