package Models;

public abstract class Personne {
    private int id;
    private String nom;

    public Personne(String nom) {
        this.id = id;
        this.nom=nom;
    }

    public int getId() {
        return id;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return nom;
    }

    public void setId(int id) {
        this.id = id;
    }

    public abstract void exprimerSatisfaction();
}
