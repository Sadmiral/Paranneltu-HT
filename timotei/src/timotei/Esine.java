/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package timotei;

import java.util.ArrayList;

/**
 *
 * @author Sadmiral
 */
public class Esine {
    Integer esineID;
    String nimi;
    Double koko;
    Boolean brk;
    Float massa;
    public ArrayList<Esine> esine_array;
    
    public Esine(Integer eID, String n, Double k, Boolean b, Float m) {
        esineID = eID;
        nimi = n;
        koko = k;
        brk = b;
        massa = m;
    }
    public Integer getEsineID() {
        return esineID;
    }
    public String getNimi() {
        return nimi;
    }
    public Double getKoko() {
        return koko;
    }
    public Boolean getBrk() {
        return brk;
    }
    public Float getMassa() {
        return massa;
    }
}
