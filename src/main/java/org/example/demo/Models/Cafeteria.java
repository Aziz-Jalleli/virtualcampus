package Models;
public class Cafeteria extends Batiment {

    public Cafeteria(int id, String nom, int capacite, double cons_res) {
        super(id, nom, "Cafétéria", capacite, cons_res, 6);
    }

    @Override
    public void utiliser() {
        System.out.println(getNom() + " est utilisée pour manger et se détendre.");
    }
}

