package exoBonus;

public class Jeu {
    private char[][] grille;

    public Jeu() {
        grille = new char[6][7];
        // init grille vide
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 7; j++) {
                grille[i][j] = '-';
            }
        }
    }

    // vérifie si la colonne n'est pas pleine
    public synchronized boolean estCoupValide(int colonne) {
        if (colonne < 0 || colonne > 6) {
            return false;
        }
        return grille[0][colonne] == '-';
    }

    // trouve la case la plus basse disponible dans la colonne
    public synchronized int trouverLigne(int colonne) {
        for (int i = 5; i >= 0; i--) {
            if (grille[i][colonne] == '-') {
                return i;
            }
        }
        return -1;
    }

    public synchronized void jouerCoup(int ligne, int colonne, char symbole) {
        grille[ligne][colonne] = symbole;
    }

    // vérifie les alignements de 4 jetons
    public synchronized boolean aGagne(char s) {
        // Horizontale
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                if (grille[i][j] == s && grille[i][j+1] == s && grille[i][j+2] == s && grille[i][j+3] == s) return true;
            }
        }
        // Verticale
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 7; j++) {
                if (grille[i][j] == s && grille[i+1][j] == s && grille[i+2][j] == s && grille[i+3][j] == s) return true;
            }
        }
        // Diagonale descendante
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                if (grille[i][j] == s && grille[i+1][j+1] == s && grille[i+2][j+2] == s && grille[i+3][j+3] == s) return true;
            }
        }
        // Diagonale montante
        for (int i = 3; i < 6; i++) {
            for (int j = 0; j < 4; j++) {
                if (grille[i][j] == s && grille[i-1][j+1] == s && grille[i-2][j+2] == s && grille[i-3][j+3] == s) return true;
            }
        }
        return false;
    }

    // vérifie si la ligne du haut est totalement remplie
    public synchronized boolean estPleine() {
        for (int j = 0; j < 7; j++) {
            if (grille[0][j] == '-') return false;
        }
        return true;
    }
}