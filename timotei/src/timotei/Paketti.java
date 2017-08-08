package timotei;

import java.util.ArrayList;


public class Paketti {
    Integer pakettiID, ID, asiakasID;
    public ArrayList<Paketti> paketti_array;
    
    public Paketti(Integer pID,Integer id ,Integer aID) {
        pakettiID = pID;
        ID = id;
        asiakasID = aID;
    }
    public Integer getPakettiID() {
        return pakettiID;
    }
    public Integer getLuokkaID() {
        return ID;
    }
    public Integer getAsiakasID() {
        return asiakasID;
    }
}
