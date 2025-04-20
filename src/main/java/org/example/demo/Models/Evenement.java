package org.example.demo.Models;
import org.example.demo.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Random;
public class Evenement {
    private int id;
    private String titre;
    private String description;
    private int impactSatisfaction;
    private String impactRessource;
    private LocalDateTime dateEvent;
    private int campusId;

    private static final String[] EVENEMENTS = {
            "Grève des profs",
            "Coupure Wi-Fi",
            "Cafétéria infestée",
            "Examens en approche",
            "Journée portes ouvertes"
    };
    public Evenement() {
    }

    // Constructor without ID (e.g., for inserting a new event)
    public Evenement(String titre, String description, int impactSatisfaction,
                     String impactRessource, LocalDateTime dateEvent, int campusId) {
        this.titre = titre;
        this.description = description;
        this.impactSatisfaction = impactSatisfaction;
        this.impactRessource = impactRessource;
        this.dateEvent = dateEvent;
        this.campusId = campusId;
    }

    // Full constructor with ID
    public Evenement(int id, String titre, String description, int impactSatisfaction,
                     String impactRessource, LocalDateTime dateEvent, int campusId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.impactSatisfaction = impactSatisfaction;
        this.impactRessource = impactRessource;
        this.dateEvent = dateEvent;
        this.campusId = campusId;
    }

        public static Evenement genererEvenementAleatoire(int campusId) {
            Random random = new Random();
            int index = random.nextInt(EVENEMENTS.length);
            String evenement = EVENEMENTS[index];
            LocalDate currentDate = LocalDate.now();

            Evenement e = null;

            switch (evenement) {
                case "Grève des profs":
                    e = new Evenement(
                            "Grève des profs",
                            "Les professeurs sont en grève, les cours sont suspendus.",
                            -20,
                            "",
                            currentDate.atStartOfDay(),
                            campusId
                    );
                    String sql = "UPDATE campus SET satisfaction = satisfaction - 10 WHERE id = ?";

                    try (Connection conn = DBConnection.connect();
                         PreparedStatement stmt = conn.prepareStatement(sql)) {

                        stmt.setInt(1, campusId);
                        int result = stmt.executeUpdate();

                        if (result > 0) {
                            System.out.println("Mise à jour réussie Le campus a été modifié avec succès.");
                        } else {
                            System.out.println("Erreur Impossible de modifier le campus.");
                        }

                    } catch (SQLException e1) {
                        e1.printStackTrace();
                        System.out.println("Erreur base de données Impossible de modifier le campus: " + e1.getMessage());
                    }
                    break;
                case "Coupure Wi-Fi":
                    e = new Evenement(
                            "Coupure Wi-Fi",
                            "Le réseau Wi-Fi est en panne, les cours en ligne sont bloqués.",
                            -15,
                            "",
                            currentDate.atStartOfDay(),
                            campusId
                    );
                    break;
                case "Cafétéria infestée":
                    e = new Evenement(
                            "Cafétéria infestée",
                            "La cafétéria est fermée pour des raisons sanitaires.",
                            -5,
                            "",
                            currentDate.atStartOfDay(),
                            campusId
                    );
                    break;
                case "Examens en approche":
                    e = new Evenement(
                            "Examens en approche",
                            "Les examens approchent, le stress des étudiants augmente.",
                            0,
                            "",
                            currentDate.atStartOfDay(),
                            campusId
                    );
                    break;
                case "Journée portes ouvertes":
                    e = new Evenement(
                            "Journée portes ouvertes",
                            "De nombreux visiteurs arrivent sur le campus.",
                            10,
                            "",
                            currentDate.atStartOfDay(),
                            campusId
                    );
                    break;
            }

            if (e != null) {
                sauvegarderDansBD(e); // Sauvegarde automatique dans la base
            }

            return e;
        }

        private static void sauvegarderDansBD(Evenement e) {
            String sql = "INSERT INTO evenements (titre, description, impact_satisfaction, impact_ressource, date_event, campus_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.connect();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, e.getTitre());
                stmt.setString(2, e.getDescription());
                stmt.setInt(3, e.getImpactSatisfaction());
                stmt.setString(4, e.getImpactRessource());
                stmt.setTimestamp(5, java.sql.Timestamp.valueOf(e.getDateEvent()));
                stmt.setInt(6, Campus.getInstance().getId());

                stmt.executeUpdate();

                System.out.println("✅ Événement enregistré avec succès dans la base de données.");

            } catch (SQLException ex) {
                System.err.println("Erreur lors de l'insertion de l'événement : " + ex.getMessage());
            }
        }




    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getImpactSatisfaction() {
        return impactSatisfaction;
    }

    public void setImpactSatisfaction(int impactSatisfaction) {
        this.impactSatisfaction = impactSatisfaction;
    }

    public String getImpactRessource() {
        return impactRessource;
    }

    public void setImpactRessource(String impactRessource) {
        this.impactRessource = impactRessource;
    }

    public LocalDateTime getDateEvent() {
        return dateEvent;
    }

    public void setDateEvent(LocalDateTime dateEvent) {
        this.dateEvent = dateEvent;
    }

    public int getCampusId() {
        return campusId;
    }

    public void setCampusId(int campusId) {
        this.campusId = campusId;
    }
}

