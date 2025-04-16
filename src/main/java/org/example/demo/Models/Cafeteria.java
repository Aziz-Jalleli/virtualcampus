package org.example.demo.Models;

import org.example.demo.Models.Batiment;

public class Cafeteria extends Batiment {

    public Cafeteria(int id, String nom, int capacite, int cons_res,int gridX,int gridY) {
        super(id, nom, "Cafeteria", capacite, cons_res, 6,gridX,gridY);
    }

    @Override
    public void utiliser() {
        System.out.println(getNom() + " est utilisée pour manger et se détendre.");
    }
}

