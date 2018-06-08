package ia;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Etat implements Comparable<Etat>{
    public static final byte OBJET_TOMBE_PAS = 0;
    public static final byte OBJET_TOMBE = 1;

    public static final byte ENNEMI_VA_EN_HAUT = 0;
    public static final byte ENNEMI_VA_A_GAUCHE = 1;
    public static final byte ENNEMI_VA_A_DROITE = 2;
    public static final byte ENNEMI_VA_EN_BAS = 3;

    public static final byte VIDE = 0;
    public static final byte MUR = 1;
    public static final byte CLAY = 2;
    public static final byte DIAMAND = 3;
    public static final byte PIERRE = 4;
    public static final byte MONSTRE_BLEU = 5;
    public static final byte MONSTRE_ROUGE = 6;
    public static final byte MINEUR = 7;
    public static final byte PORTE = 8;


    private byte[][] currentState;
    private byte[][] currentInfos;
    private int ligneMineur;
    private int colonneMineur;
    private double g_value;
    private double f_value;
    private int[] coordonneesObjectif;
    private byte nbDiamantsEncoreAAttraper;
    private Etat etatParent;

    public Etat(int ligneMineur, int colonneMineur, byte[][] currentState, byte[][] currentInfos, byte nbDiamantsEncoreAAttraper){
        this.currentState = currentState;
        this.currentInfos = currentInfos;
        this.nbDiamantsEncoreAAttraper = nbDiamantsEncoreAAttraper;

        this.ligneMineur = ligneMineur;
        this.colonneMineur = colonneMineur;

        this.etatParent = null;
    }

    public Etat(int ligneMineur, int colonneMineur, byte[][] currentState, byte[][] currentInfos, byte nbDiamantsEncoreAAttraper, int[] coordonneesObjectif){
        this.currentState = currentState;
        this.currentInfos = currentInfos;
        this.nbDiamantsEncoreAAttraper = nbDiamantsEncoreAAttraper;

        this.ligneMineur = ligneMineur;
        this.colonneMineur = colonneMineur;

        this.etatParent = null;
        this.coordonneesObjectif = coordonneesObjectif;

        this.g_value = 0;
        this.f_value = Math.sqrt(Math.pow(ligneMineur - coordonneesObjectif[0], 2) + Math.pow(colonneMineur - coordonneesObjectif[1], 2));
    }

    private Etat(int ligneMineur, int colonneMineur, byte[][] currentState, byte[][] currentInfos, byte nbDiamantsEncoreAAttraper, int[] coordonneesObjectif, Etat parent){
        this.currentState = currentState;
        this.currentInfos = currentInfos;
        this.nbDiamantsEncoreAAttraper = nbDiamantsEncoreAAttraper;

        this.ligneMineur = ligneMineur;
        this.colonneMineur = colonneMineur;

        this.etatParent = parent;
        this.coordonneesObjectif = coordonneesObjectif;

        this.g_value = parent.g_value + 1;
        this.f_value = this.g_value + Math.sqrt(Math.pow(ligneMineur - coordonneesObjectif[0], 2) + Math.pow(colonneMineur - coordonneesObjectif[1], 2));
    }

    public List<Etat> getSuivants(){
        List<Etat> etats = new ArrayList<>();
        Etat etat;

        if((etat=getSuivantHaut()) != null) etats.add(etat);
        if((etat=getSuivantGauche()) != null) etats.add(etat);
        if((etat=getSuivantDroite()) != null) etats.add(etat);
        if((etat=getSuivantBas()) != null) etats.add(etat);

        return etats;
    }

    private Etat getSuivantHaut(){
        //Si on ne peut pas se déplacer en haut
        if(ligneMineur == 0 || currentState[ligneMineur -1][colonneMineur] == MUR || currentState[ligneMineur-1][colonneMineur] == PIERRE ||
                (currentState[ligneMineur-1][colonneMineur] == DIAMAND && currentInfos[ligneMineur-1][colonneMineur] == OBJET_TOMBE) ||
                currentState[ligneMineur-1][colonneMineur] == MONSTRE_BLEU || currentState[ligneMineur-1][colonneMineur] == MONSTRE_ROUGE) {
            return null;
        }

        byte[][] newState = Arrays.stream(currentState)
                .map(byte[]::clone)
                .toArray(byte[][]::new);
        byte[][] newInfos = Arrays.stream(currentInfos)
                .map(byte[]::clone)
                .toArray(byte[][]::new);

        byte newNbDiamantsEncoreAAttraper = nbDiamantsEncoreAAttraper;
        if(newState[ligneMineur-1][colonneMineur] == DIAMAND) newNbDiamantsEncoreAAttraper--;
        newState[ligneMineur-1][colonneMineur] = MINEUR;
        newState[ligneMineur][colonneMineur] = VIDE;
        int newLigneMineur = ligneMineur-1;

        gestionDeplacementsNonIA(newState, newInfos, newLigneMineur, colonneMineur); //Changements effectués dans newState et newInfos

        if((newLigneMineur != 0 && (newState[newLigneMineur-1][colonneMineur] == MONSTRE_ROUGE || newState[newLigneMineur-1][colonneMineur] == MONSTRE_BLEU)) ||
                (newLigneMineur != newState.length-1 && (newState[newLigneMineur+1][colonneMineur] == MONSTRE_ROUGE || newState[newLigneMineur+1][colonneMineur] == MONSTRE_BLEU)) ||
                (colonneMineur != 0 && (newState[newLigneMineur][colonneMineur-1] == MONSTRE_ROUGE || newState[newLigneMineur][colonneMineur-1] == MONSTRE_BLEU)) ||
                (colonneMineur != newState[0].length-1 && (newState[newLigneMineur][colonneMineur+1] == MONSTRE_ROUGE || newState[newLigneMineur][colonneMineur+1] == MONSTRE_BLEU)) ||
                (newLigneMineur !=0 && (newState[newLigneMineur-1][colonneMineur] == PIERRE || newState[newLigneMineur-1][colonneMineur] == DIAMAND) && newInfos[newLigneMineur-1][colonneMineur] == OBJET_TOMBE)){
            return null;    //Si un monstre finit dans une case à coté du mineur, cet état n'est pas bon
        }

        return new Etat(newLigneMineur, colonneMineur, newState, newInfos, newNbDiamantsEncoreAAttraper, coordonneesObjectif, this);
    }

    private Etat getSuivantGauche(){
        //Si on ne peut pas se déplacer à droite
        if(colonneMineur == 0 || currentState[ligneMineur][colonneMineur-1] == MUR ||
                (currentState[ligneMineur][colonneMineur-1] == PIERRE && currentInfos[ligneMineur][colonneMineur-1] == OBJET_TOMBE) ||
                (currentState[ligneMineur][colonneMineur-1] == PIERRE && colonneMineur-1 == 0) ||
                (currentState[ligneMineur][colonneMineur-1] == PIERRE && currentState[ligneMineur][colonneMineur-2] != VIDE) ||
                currentState[ligneMineur][colonneMineur-1] == MONSTRE_BLEU || currentState[ligneMineur][colonneMineur-1] == MONSTRE_ROUGE) {
            return null;
        }

        byte[][] newState = Arrays.stream(currentState)
                .map(byte[]::clone)
                .toArray(byte[][]::new);
        byte[][] newInfos = Arrays.stream(currentInfos)
                .map(byte[]::clone)
                .toArray(byte[][]::new);

        if(newState[ligneMineur][colonneMineur-1] == PIERRE){
            newState[ligneMineur][colonneMineur-2] = PIERRE;
            newInfos[ligneMineur][colonneMineur-2] = OBJET_TOMBE_PAS;
        }

        byte newNbDiamantsEncoreAAttraper = nbDiamantsEncoreAAttraper;
        if(newState[ligneMineur][colonneMineur-1] == DIAMAND) newNbDiamantsEncoreAAttraper--;
        newState[ligneMineur][colonneMineur-1] = MINEUR;
        newState[ligneMineur][colonneMineur] = VIDE;
        int newColonneMineur = colonneMineur-1;

        gestionDeplacementsNonIA(newState, newInfos, ligneMineur, newColonneMineur); //Changements effectués dans newState et newInfos

        if((ligneMineur != 0 && (newState[ligneMineur-1][newColonneMineur] == MONSTRE_ROUGE || newState[ligneMineur-1][newColonneMineur] == MONSTRE_BLEU)) ||
                (ligneMineur != newState.length-1 && (newState[ligneMineur+1][newColonneMineur] == MONSTRE_ROUGE || newState[ligneMineur+1][newColonneMineur] == MONSTRE_BLEU)) ||
                (newColonneMineur != 0 && (newState[ligneMineur][newColonneMineur-1] == MONSTRE_ROUGE || newState[ligneMineur][newColonneMineur-1] == MONSTRE_BLEU)) ||
                (newColonneMineur != newState[0].length-1 && (newState[ligneMineur][newColonneMineur+1] == MONSTRE_ROUGE || newState[ligneMineur][newColonneMineur+1] == MONSTRE_BLEU)) ||
                (ligneMineur !=0 && (newState[ligneMineur-1][newColonneMineur] == PIERRE || newState[ligneMineur-1][newColonneMineur] == DIAMAND) && newInfos[ligneMineur-1][newColonneMineur] == OBJET_TOMBE)){
            return null;    //Si un monstre finit dans une case à coté du mineur, ou qu'on ce prend quelque chose sur la tête, cet état n'est pas bon
        }

        return new Etat(ligneMineur, newColonneMineur, newState, newInfos, newNbDiamantsEncoreAAttraper, coordonneesObjectif, this);
    }

    private Etat getSuivantDroite(){
        //Si on ne peut pas se déplacer à droite
        if(colonneMineur == currentState[0].length-1 || currentState[ligneMineur][colonneMineur+1] == MUR ||
                (currentState[ligneMineur][colonneMineur+1] == PIERRE && currentInfos[ligneMineur][colonneMineur+1] == OBJET_TOMBE) ||
                (currentState[ligneMineur][colonneMineur+1] == PIERRE && colonneMineur+1 == currentState[0].length-1) ||
                (currentState[ligneMineur][colonneMineur+1] == PIERRE && currentState[ligneMineur][colonneMineur+2] != VIDE) ||
                currentState[ligneMineur][colonneMineur+1] == MONSTRE_BLEU || currentState[ligneMineur][colonneMineur+1] == MONSTRE_ROUGE) {
            return null;
        }

        byte[][] newState = Arrays.stream(currentState)
                .map(byte[]::clone)
                .toArray(byte[][]::new);
        byte[][] newInfos = Arrays.stream(currentInfos)
                .map(byte[]::clone)
                .toArray(byte[][]::new);

        if(newState[ligneMineur][colonneMineur+1] == PIERRE){
            newState[ligneMineur][colonneMineur+2] = PIERRE;
            newInfos[ligneMineur][colonneMineur+2] = OBJET_TOMBE_PAS;
        }

        byte newNbDiamantsEncoreAAttraper = nbDiamantsEncoreAAttraper;
        if(newState[ligneMineur][colonneMineur+1] == DIAMAND) newNbDiamantsEncoreAAttraper--;
        newState[ligneMineur][colonneMineur+1] = MINEUR;
        newState[ligneMineur][colonneMineur] = VIDE;
        int newColonneMineur = colonneMineur+1;

        gestionDeplacementsNonIA(newState, newInfos, ligneMineur, newColonneMineur); //Changements effectués dans newState et newInfos

        if((ligneMineur != 0 && (newState[ligneMineur-1][newColonneMineur] == MONSTRE_ROUGE || newState[ligneMineur-1][newColonneMineur] == MONSTRE_BLEU)) ||
                (ligneMineur != newState.length-1 && (newState[ligneMineur+1][newColonneMineur] == MONSTRE_ROUGE || newState[ligneMineur+1][newColonneMineur] == MONSTRE_BLEU)) ||
                (newColonneMineur != 0 && (newState[ligneMineur][newColonneMineur-1] == MONSTRE_ROUGE || newState[ligneMineur][newColonneMineur-1] == MONSTRE_BLEU)) ||
                (newColonneMineur != newState[0].length-1 && (newState[ligneMineur][newColonneMineur+1] == MONSTRE_ROUGE || newState[ligneMineur][newColonneMineur+1] == MONSTRE_BLEU)) ||
                (ligneMineur !=0 && (newState[ligneMineur-1][newColonneMineur] == PIERRE || newState[ligneMineur-1][newColonneMineur] == DIAMAND) && newInfos[ligneMineur-1][newColonneMineur] == OBJET_TOMBE)){
            return null;    //Si un monstre finit dans une case à coté du mineur, ou qu'on ce prend quelque chose sur la tête, cet état n'est pas bon
        }

        return new Etat(ligneMineur, newColonneMineur, newState, newInfos, newNbDiamantsEncoreAAttraper, coordonneesObjectif, this);
    }

    private Etat getSuivantBas(){
        //Si on ne peut pas se déplacer en bas
        if(ligneMineur == currentState.length-1 || currentState[ligneMineur +1][colonneMineur] == MUR || currentState[ligneMineur+1][colonneMineur] == PIERRE ||
                currentState[ligneMineur+1][colonneMineur] == MONSTRE_BLEU || currentState[ligneMineur+1][colonneMineur] == MONSTRE_ROUGE) {
            return null;
        }

        byte[][] newState = Arrays.stream(currentState)
                .map(byte[]::clone)
                .toArray(byte[][]::new);
        byte[][] newInfos = Arrays.stream(currentInfos)
                .map(byte[]::clone)
                .toArray(byte[][]::new);

        byte newNbDiamantsEncoreAAttraper = nbDiamantsEncoreAAttraper;
        if(newState[ligneMineur+1][colonneMineur] == DIAMAND) newNbDiamantsEncoreAAttraper--;
        newState[ligneMineur+1][colonneMineur] = MINEUR;
        newState[ligneMineur][colonneMineur] = VIDE;
        int newLigneMineur = ligneMineur+1;

        gestionDeplacementsNonIA(newState, newInfos, newLigneMineur, colonneMineur); //Changements effectués dans newState et newInfos

        if((newLigneMineur != 0 && (newState[newLigneMineur-1][colonneMineur] == MONSTRE_ROUGE || newState[newLigneMineur-1][colonneMineur] == MONSTRE_BLEU)) ||
                (newLigneMineur != newState.length-1 && (newState[newLigneMineur+1][colonneMineur] == MONSTRE_ROUGE || newState[newLigneMineur+1][colonneMineur] == MONSTRE_BLEU)) ||
                (colonneMineur != 0 && (newState[newLigneMineur][colonneMineur-1] == MONSTRE_ROUGE || newState[newLigneMineur][colonneMineur-1] == MONSTRE_BLEU)) ||
                (colonneMineur != newState[0].length-1 && (newState[newLigneMineur][colonneMineur+1] == MONSTRE_ROUGE || newState[newLigneMineur][colonneMineur+1] == MONSTRE_BLEU)) ||
                (newLigneMineur !=0 && (newState[newLigneMineur-1][colonneMineur] == PIERRE || newState[newLigneMineur-1][colonneMineur] == DIAMAND) && newInfos[newLigneMineur-1][colonneMineur] == OBJET_TOMBE)){
            return null;    //Si un monstre finit dans une case à coté du mineur, ou qu'on ce prend quelque chose sur la tête, cet état n'est pas bon
        }

        return new Etat(newLigneMineur, colonneMineur, newState, newInfos, newNbDiamantsEncoreAAttraper, coordonneesObjectif, this);
    }

    private void gestionDeplacementsNonIA(byte[][] state, byte[][] infos, int ligneMineur, int colonneMineur){
        for(int ligne=0 ; ligne<state.length ; ligne++){
            for(int colonne = 0 ; colonne<state[0].length ; colonne++){
                if(state[ligne][colonne] == DIAMAND || state[ligne][colonne] == PIERRE) {

                    if(state[ligne+1][colonne] == VIDE){
                        state[ligne+1][colonne] = state[ligne][colonne];
                        infos[ligne+1][colonne] = OBJET_TOMBE;
                        state[ligne][colonne] = VIDE;
                    }
                    else if(state[ligne+1][colonne] == DIAMAND || state[ligne+1][colonne] == PIERRE) {
                        if(colonne>0 && state[ligne+1][colonne-1] == VIDE && state[ligne][colonne-1] == VIDE &&
                                (ligneMineur!=ligne+1 || colonneMineur!=colonne-1)) {

                            state[ligne+1][colonne-1] = state[ligne][colonne];
                            state[ligne][colonne] = VIDE;
                            infos[ligne+1][colonne-1] = OBJET_TOMBE;
                        } else if(colonne<state[0].length-1 && state[ligne+1][colonne+1] == VIDE && state[ligne][colonne+1] == VIDE &&
                                (ligneMineur!=ligne+1 || colonneMineur!=colonne+1)) {
                            state[ligne+1][colonne+1] = state[ligne][colonne];
                            state[ligne][colonne] = VIDE;
                            infos[ligne+1][colonne+1] = OBJET_TOMBE;
                        }
                    }
                    else infos[ligne][colonne] = OBJET_TOMBE_PAS;
                }

                else if(state[ligne][colonne] == MONSTRE_BLEU || state[ligne][colonne] == MONSTRE_ROUGE) {
                    byte directionAvant = infos[ligne][colonne];
                    byte directionGauche = getGauche(directionAvant);
                    byte directionArriere = getGauche(directionGauche);
                    byte directionDroite = getGauche(directionArriere);

                    int[] deplacementAvant = getDeplacementFromDirection(directionAvant);
                    int[] deplacementGauche = getDeplacementFromDirection(directionGauche);
                    int[] deplacementArriere = getDeplacementFromDirection(directionArriere);
                    int[] deplacementDroite = getDeplacementFromDirection(directionDroite);

                    if(state[ligne+deplacementGauche[0]][colonne+deplacementGauche[1]] == VIDE){
                        state[ligne+deplacementGauche[0]][colonne+deplacementGauche[1]] = state[ligne][colonne];
                        state[ligne][colonne] = VIDE;
                        infos[ligne+deplacementGauche[0]][colonne+deplacementGauche[1]] = directionGauche;
                    }
                    else if(state[ligne+deplacementAvant[0]][colonne+deplacementAvant[1]] == VIDE){
                        state[ligne+deplacementAvant[0]][colonne+deplacementAvant[1]] = state[ligne][colonne];
                        state[ligne][colonne] = VIDE;
                        infos[ligne+deplacementAvant[0]][colonne+deplacementAvant[1]] = directionAvant;
                    }
                    else if(state[ligne+deplacementDroite[0]][colonne+deplacementDroite[1]] == VIDE){
                        state[ligne+deplacementDroite[0]][colonne+deplacementDroite[1]] = state[ligne][colonne];
                        state[ligne][colonne] = VIDE;
                        infos[ligne+deplacementDroite[0]][colonne+deplacementDroite[1]] = directionDroite;
                    }
                    else if(state[ligne+deplacementArriere[0]][colonne+deplacementArriere[1]] == VIDE){
                        state[ligne+deplacementArriere[0]][colonne+deplacementArriere[1]] = state[ligne][colonne];
                        state[ligne][colonne] = VIDE;
                        infos[ligne+deplacementArriere[0]][colonne+deplacementArriere[1]] = directionArriere;
                    }
                    //sinon il reste immobile
                }
            }

        }
    }

    private byte getGauche(byte direction){    //direction est l'une des quatre constantes de direction
        if(direction<3) return (byte)(direction+1);
        else return ENNEMI_VA_EN_HAUT;
    }
    private int[] getDeplacementFromDirection(byte direction){    //direction est l'une des quatre constantes de direction
        int ligne;
        int colonne;

        if(direction == ENNEMI_VA_A_GAUCHE){
            ligne = 0;
            colonne = -1;
        }
        else if(direction == ENNEMI_VA_A_DROITE){
            ligne = 0;
            colonne = 1;
        }
        else if(direction == ENNEMI_VA_EN_HAUT){
            ligne = -1;
            colonne = 0;
        }
        else{
            ligne = 1;
            colonne = 0;
        }

        return new int[]{ligne, colonne};
    }

    public boolean objectifEstAtteint(int[] objectif){
        return ligneMineur == objectif[0] && colonneMineur == objectif[1];
    }

    @Override
    public int compareTo(Etat o) {
        if(f_value > o.f_value) return 1;
        else if(f_value == o.f_value) return 0;
        else return -1;
    }

    public Etat getEtatParent() {
        return etatParent;
    }
    public byte[][] getCurrentState() {
        return currentState;
    }
    public byte getNbDiamantsEncoreAAttraper(){return nbDiamantsEncoreAAttraper;}

    public void defineNewObjectif(int[] coordonneesObjectif){
        this.etatParent = null;
        this.coordonneesObjectif = coordonneesObjectif;
        this.g_value = 0;
        this.f_value = Math.sqrt(Math.pow(ligneMineur - coordonneesObjectif[0], 2) + Math.pow(colonneMineur - coordonneesObjectif[1], 2));
    }
}
