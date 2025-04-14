package org.example.demo.Models;
public class Bibliotheque extends Models.Batiment {

    public Bibliotheque(int id, String nom, int capacite, double cons_res) {
        super(id, nom, "Bibliothèque", capacite, cons_res, 8);
    }

    @Override
    public void utiliser() {
        System.out.println(getNom() + " est utilisée pour étudier ou emprunter des livres.");
    }
}
