package timotei;

import java.util.ArrayList;

public class Posti {
    String nimi, osoite, toimipaikka, aukioloaika, postinumero, longitude, latitude;
    public ArrayList<Posti> maatti_array;
    
    public Posti(String n, String o, String tp, String aa, String pn, String lon, String lat) {
        nimi = n;
        osoite = o;
        toimipaikka = tp;
        aukioloaika = aa;
        postinumero = pn;
        longitude = lon;
        latitude = lat;
    }
    public String getNimi() {
        return nimi;
    }
    public String getOsoite() {
        return osoite;
    }
    public String getToimipaikka() {
        return toimipaikka;
    }
    public String getAukioloaika() {
        return aukioloaika;
    }
    public String getPostinumero() {
        return postinumero;
    }
    public String getLongitude() {
        return longitude;
    }
    public String getLatitude() {
        return latitude;
    }
}