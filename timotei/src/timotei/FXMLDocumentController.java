/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package timotei;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

/**
 *
 * @author Sadmiral
 */
public class FXMLDocumentController implements Initializable {
    
    @FXML
    private ComboBox<Object> kaupunkiComboBox;
    @FXML
    private Button lisääkartalleButton;
    @FXML
    private Button päivitäpakettiButton;
    @FXML
    private Button luopakettiButton;
    @FXML
    private Button lähetäpakettiButton;
    @FXML
    private Button poistareititButton;
    @FXML
    private ComboBox<Object> postiComboBox;
    @FXML
    private javafx.scene.web.WebView web;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        timoteiDB tDB = new timoteiDB();
        SmartPostit sp = SmartPostit.getInstance();
        String prev = null;
        
        Connection connection;
        connection = SqliteConnection.Connector();

        if (connection != null) {
            tDB.insertAutomaatit();
        }
        else if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        web.getEngine().load(getClass().getResource("index.html").toExternalForm());
        try {
            PreparedStatement psPaketti = connection.prepareStatement("SELECT * FROM paketti JOIN esine ON esineID = pakettiID JOIN luokka ON paketti.ID = luokka.ID");
            ResultSet rsP = psPaketti.executeQuery();

            for (Integer i = 0;i < sp.maatti_array.size();i++) {
                if (!sp.maatti_array.get(i).getToimipaikka().equals(prev)) {
                    kaupunkiComboBox.getItems().add(sp.maatti_array.get(i).getToimipaikka());
                }
                prev = sp.maatti_array.get(i).getToimipaikka();
            }
            while (rsP.next()) {
                sp.addPaketti(Integer.parseInt(rsP.getString(1)), Integer.parseInt(rsP.getString(2)), Integer.parseInt(rsP.getString(3)));
                sp.addEsine(Integer.parseInt(rsP.getString(4)), rsP.getString(5), Double.parseDouble(rsP.getString(3)), Boolean.parseBoolean(rsP.getString(7)), Float.parseFloat(rsP.getString(8)));
                postiComboBox.getItems().add(rsP.getString(1) + "   " + rsP.getString(5) + " " + rsP.getString(2) + ". Luokka");
            }
            psPaketti.close();
            rsP.close();
        } catch (SQLException ex) {
        Logger.getLogger(LuoPakettiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void drawPin(ActionEvent event) {
        SmartPostit sp = SmartPostit.getInstance();
        Connection connection = SqliteConnection.Connector();
        
        if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        //piirretään kaikki automaatit käyttäjän antaman toimipisteen mukaan
        for(Integer i = 0;i < sp.maatti_array.size();i++) {
            if(kaupunkiComboBox.getValue().equals(sp.maatti_array.get(i).getToimipaikka())) {
            web.getEngine().executeScript("document.goToLocation('" + sp.maatti_array.get(i).getOsoite() + ", " + sp.maatti_array.get(i).getPostinumero() + 
                    " " + sp.maatti_array.get(i).getToimipaikka() + "', 'Auki: " + sp.maatti_array.get(i).getAukioloaika() + "', 'red')");
            }
        }
    }
    
    @FXML
    private void drawRoad(ActionEvent event) {
        ArrayList<Float> al = new ArrayList<>();
        SmartPostit sp = SmartPostit.getInstance();
        Connection connection = SqliteConnection.Connector();
        Integer pktID = null, x = 0, x1 = 0, x2 = 0;
        
        if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        
        try {
            if (postiComboBox.getValue() != null) {
                String paketti = String.valueOf(postiComboBox.getValue());
                Character c1 = paketti.charAt(0);
                Character c2 = paketti.charAt(1);
                if (c2 == ' ') {
                    pktID = Character.getNumericValue(c1);
                }
                else {
                    String c12 = "" + c1 + c2;
                    pktID = Integer.parseInt(c12);
                }
                //Haetaan tietokannasta kaikki tarvittavat tiedot teiden, ja automaattien piirtämiseen kartalle
                PreparedStatement psLuokka = connection.prepareStatement("SELECT * FROM luokka WHERE ID = ?");
                PreparedStatement psKuljetus = connection.prepareStatement("INSERT INTO kuljetus (kuljetusID) VALUES (?)");
                PreparedStatement psAutomaatti1 = connection.prepareStatement("SELECT * FROM automaatti JOIN reitti ON osoite1 = osoite JOIN paketti ON " + pktID + " = reittiID WHERE osoite1 = osoite");
                PreparedStatement psAutomaatti2 = connection.prepareStatement("SELECT * FROM automaatti JOIN reitti ON osoite2 = osoite JOIN paketti ON " + pktID + " = reittiID WHERE osoite2 = osoite");
    
                for(Integer i = 0; i < sp.paketti_array.size();i++) {
                    if (sp.paketti_array.get(i).getPakettiID() == pktID) {
                        x = i;
                    }
                }
                psLuokka.setInt(1, sp.paketti_array.get(x).getLuokkaID());
                
                ResultSet rsLk = psLuokka.executeQuery();
                ResultSet rsA1 = psAutomaatti1.executeQuery();
                ResultSet rsA2 = psAutomaatti2.executeQuery();
                
                String ko1 = rsA1.getString(2);
                String ko2 = rsA2.getString(2);

                for (Integer i = 0; i < sp.maatti_array.size(); i++) {
                    if(ko1.equals(sp.maatti_array.get(i).getOsoite())) {
                        x1 = i;
                    }
                    if(ko2.equals(sp.maatti_array.get(i).getOsoite())) {
                        x2 = i;
                    }
                }
                String katuosoite1 = sp.maatti_array.get(x1).getOsoite();
                String postinumero1 = sp.maatti_array.get(x1).getPostinumero();
                String toimipaikka1 = sp.maatti_array.get(x1).getToimipaikka();
                String nimi1 = sp.maatti_array.get(x1).getNimi();
                String aukioloajat1 = sp.maatti_array.get(x1).getAukioloaika();
                String katuosoite2 = sp.maatti_array.get(x2).getOsoite();
                String postinumero2 = sp.maatti_array.get(x2).getPostinumero();
                String toimipaikka2 = sp.maatti_array.get(x2).getToimipaikka();
                String nimi2 = sp.maatti_array.get(x2).getNimi();
                String aukioloajat2 = sp.maatti_array.get(x2).getAukioloaika();
                
                String eNimi = sp.esine_array.get(x).getNimi();
                Double eKoko = sp.esine_array.get(x).getKoko();
                Boolean eBrk = sp.esine_array.get(x).getBrk();
                Float eMassa = sp.esine_array.get(x).getMassa();
                Double maxDist = Double.valueOf(rsLk.getString(2));
                Float maxKG = Float.valueOf(rsLk.getString(3));
                Double maxKoko = Double.valueOf(rsLk.getString(4));
                
                psKuljetus.setInt(1, pktID);
                
                Float lat1 = Float.parseFloat(sp.maatti_array.get(x1).getLatitude());
                Float lon1 = Float.parseFloat(sp.maatti_array.get(x1).getLongitude());
                Float lat2 = Float.parseFloat(sp.maatti_array.get(x2).getLatitude());
                Float lon2 = Float.parseFloat(sp.maatti_array.get(x2).getLongitude());
                
                al.add(lat1);
                al.add(lon1);
                al.add(lat2);
                al.add(lon2);

                Integer luokkaID = Integer.valueOf(rsLk.getString(1));
                Double distance = Distance(lat1, lon1, lat2, lon2);
                
                //Tarkastetaan meneekö esine rikki/voiko pakettia lähettää
                if (luokkaID == 1) {
                    if (distance > maxDist) {
                        try {
                            FXMLLoader Loader = new FXMLLoader();
                            Loader.setLocation(getClass().getResource("popupBrk.fxml"));
                            Loader.load();

                            PopupLabelController display = Loader.getController();
                            display.setText("Ensimmäisen luokan pakettia ei voi lähettää yli 150km päähän!");
                            Parent p = Loader.getRoot();
                            Stage stage = new Stage();
                            stage.setScene(new Scene(p));
                            stage.show();
                        } catch (IOException ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else if (eBrk == true) {
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite1 + ", " + postinumero1 + " " + toimipaikka1 + "','" + nimi1 + " Aukioloajat: " + aukioloajat1 + "', 'blue')");
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite2 + ", " + postinumero2 + " " + toimipaikka2 + "','" + nimi2 + " Aukioloajat: " + aukioloajat2 + "', 'yellow')");
                        web.getEngine().executeScript("document.createPath(" + al + ", 'red', 2)");
                        try {
                            FXMLLoader Loader = new FXMLLoader();
                            Loader.setLocation(getClass().getResource("popupBrk.fxml"));
                            Loader.load();

                            PopupLabelController display = Loader.getController();
                            display.setText("Paketin sisältämä esine meni rikki!");
                            Parent p = Loader.getRoot();
                            Stage stage = new Stage();
                            stage.setScene(new Scene(p));
                            stage.show();
                        } catch (IOException ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else {
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite1 + ", " + postinumero1 + " " + toimipaikka1 + "','" + nimi1 + " Aukioloajat: " + aukioloajat1 + "', 'blue')");
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite2 + ", " + postinumero2 + " " + toimipaikka2 + "','" + nimi2 + " Aukioloajat: " + aukioloajat2 + "', 'yellow')");
                        web.getEngine().executeScript("document.createPath(" + al + ", 'red', 2)");
                    }
                }
                else if (luokkaID == 2) {
                    if (eKoko > maxKoko & eBrk == true) {
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite1 + ", " + postinumero1 + " " + toimipaikka1 + "','" + nimi1 + " Aukioloajat: " + aukioloajat1 + "', 'blue')");
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite2 + ", " + postinumero2 + " " + toimipaikka2 + "','" + nimi2 + " Aukioloajat: " + aukioloajat2 + "', 'yellow')");
                        web.getEngine().executeScript("document.createPath(" + al + ", 'red', 2)");
                        try {
                            FXMLLoader Loader = new FXMLLoader();
                            Loader.setLocation(getClass().getResource("popupBrk.fxml"));
                            Loader.load();

                            PopupLabelController display = Loader.getController();
                            display.setText("Paketin sisältämä esine meni rikki!");
                            Parent p = Loader.getRoot();
                            Stage stage = new Stage();
                            stage.setScene(new Scene(p));
                            stage.show();
                        } catch (IOException ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else {
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite1 + ", " + postinumero1 + " " + toimipaikka1 + "','" + nimi1 + " Aukioloajat: " + aukioloajat1 + "', 'blue')");
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite2 + ", " + postinumero2 + " " + toimipaikka2 + "','" + nimi2 + " Aukioloajat: " + aukioloajat2 + "', 'yellow')");
                        web.getEngine().executeScript("document.createPath(" + al + ", 'red', 2)");
                    }
                }
                else if (luokkaID == 3) {
                    if (eBrk == true & eKoko < 12000 | eMassa < 12) {
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite1 + ", " + postinumero1 + " " + toimipaikka1 + "','" + nimi1 + " Aukioloajat: " + aukioloajat1 + "', 'blue')");
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite2 + ", " + postinumero2 + " " + toimipaikka2 + "','" + nimi2 + " Aukioloajat: " + aukioloajat2 + "', 'yellow')");
                        web.getEngine().executeScript("document.createPath(" + al + ", 'red', 2)");
                        try {
                            FXMLLoader Loader = new FXMLLoader();
                            Loader.setLocation(getClass().getResource("popupBrk.fxml"));
                            Loader.load();

                            PopupLabelController display = Loader.getController();
                            display.setText("Paketin sisältämä esine meni rikki!");
                            Parent p = Loader.getRoot();
                            Stage stage = new Stage();
                            stage.setScene(new Scene(p));
                            stage.show();
                        } catch (IOException ex) {
                            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    else {
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite1 + ", " + postinumero1 + " " + toimipaikka1 + "','" + nimi1 + " Aukioloajat: " + aukioloajat1 + "', 'blue')");
                        web.getEngine().executeScript("document.goToLocation('" + katuosoite2 + ", " + postinumero2 + " " + toimipaikka2 + "','" + nimi2 + " Aukioloajat: " + aukioloajat2 + "', 'yellow')");
                        web.getEngine().executeScript("document.createPath(" + al + ", 'red', 2)");
                    }
                }
                psKuljetus.close();
                psLuokka.close();
                psAutomaatti1.close();
                psAutomaatti2.close();
                rsA1.close();
                rsA2.close();
                rsLk.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(LuoPakettiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @FXML
    private void deleteRoads(ActionEvent event) {
        web.getEngine().executeScript("document.deletePaths()");
    }
    @FXML
    private void luoPaketti(ActionEvent event) {
        try {
            Stage paketinluonti = new Stage();
            Parent page = FXMLLoader.load(getClass().getResource("luoPaketti.fxml"));
            
            Scene scene = new Scene(page);
            
            paketinluonti.setScene(scene);
            paketinluonti.show();
            
        } catch (IOException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @FXML
    private void muokkaaPakettia(ActionEvent event) {
        SmartPostit sp = SmartPostit.getInstance();
        Integer pktID = null;
        if (postiComboBox.getValue() != null) {
            String paketti = String.valueOf(postiComboBox.getValue());
            Character c1 = paketti.charAt(0);
            Character c2 = paketti.charAt(1);
            if (c2 == ' ') {
                pktID = Character.getNumericValue(c1);
            }
            else {
                String c12 = "" + c1 + c2;
                pktID = Integer.parseInt(c12);
            }
            try {
                FXMLLoader Loader = new FXMLLoader();
                Loader.setLocation(getClass().getResource("muokkaaPakettia.fxml"));
                Loader.load();
                MuokkaaPakettiaController display = Loader.getController();
                display.setValues(pktID);
                Parent p = Loader.getRoot();
                Stage stage = new Stage();
                stage.setScene(new Scene(p));
                stage.show();
            } catch (IOException ex) {
                Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    //Päivitetään paketit comboboxiin aina kun comboboxi valitaan
    @FXML
    private void updatePaketit(MouseEvent event) {
        SmartPostit sp = SmartPostit.getInstance();
        
        while (postiComboBox.getItems().size() > 0) {
            postiComboBox.getItems().remove(0);
        }
        for (Integer i = 0;i < sp.paketti_array.size();i++) {
            postiComboBox.getItems().add(sp.paketti_array.get(i).getPakettiID() + "   " + 
                    sp.esine_array.get(i).getNimi() + " " + sp.paketti_array.get(i).getLuokkaID() + ". Luokka");
        }
    }
    
    //Lasketaan automaattien välinen etäisyys longituden ja latituden avulla
    public static double Distance(float lng1, float lat1, float lng2, float lat2) {
        double earthRadius = 6371000; 
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c)/1000;

        return dist;
    }
}