package exo5;

public class Jeu {
    private char[][] grille;

    public Jeu() {
        grille = new char[3][3];
        // init grille vide
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                grille[i][j] = '-';
            }
        }
    }

    // accès concurrent sécurisé
    public synchronized boolean estCoupValide(int ligne, int colonne) {
        if (ligne < 0 || ligne > 2 || colonne < 0 || colonne > 2) {
            return false;
        }
        return grille[ligne][colonne] == '-';
    }

    public synchronized void jouerCoup(int ligne, int colonne, char symbole) {
        if (estCoupValide(ligne, colonne)) {
            grille[ligne][colonne] = symbole;
        }
    }

    // export de la grille pour affichage client
    public synchronized String afficherGrille() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                sb.append(grille[i][j]).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}