package timotei;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LuoPakettiController implements Initializable {
    
    @FXML
    private ComboBox<Object> valitseesineComboBox;
    @FXML
    private ComboBox<Object> lähtöComboBox;
    @FXML
    private ComboBox<Object> lähtöautomaattiComboBox;
    @FXML
    private ComboBox<Object> kohdeComboBox;
    @FXML
    private ComboBox<Object> kohdeautomaattiComboBox;
    @FXML
    private ComboBox<Object> valitseluokkaComboBox;
    @FXML
    private Button peruutaButton;
    @FXML
    private Button luopakettiButton;
    @FXML
    private TextField nimiField;
    @FXML
    private TextField kokoField;
    @FXML
    private TextField massaField;
    @FXML
    private CheckBox särkyvääCheckBox;
    @FXML
    private TextArea infotextArea;
    @FXML
    private TextField lähettäjäField;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SmartPostit sp = SmartPostit.getInstance();
        String prev = null;

        for(Integer i = 0;i < sp.esine_array.size();i++) {
            valitseesineComboBox.getItems().add(sp.esine_array.get(i).getNimi());
        }

        for (Integer i = 0;i < 3; i++) {
            String num = String.valueOf(i+1);
            valitseluokkaComboBox.getItems().add(num + ". Luokka");
        }
        for (Integer i = 0;i < sp.maatti_array.size();i++) {
            if (!sp.maatti_array.get(i).getToimipaikka().equals(prev)) {
                lähtöComboBox.getItems().add(sp.maatti_array.get(i).getToimipaikka());
                kohdeComboBox.getItems().add(sp.maatti_array.get(i).getToimipaikka());
            }
            prev = sp.maatti_array.get(i).getToimipaikka();
        }
    }  

    @FXML
    private void lähtöSelect(ActionEvent event) {
        SmartPostit sp = SmartPostit.getInstance();

        while (lähtöautomaattiComboBox.getItems().size() > 0) {
            lähtöautomaattiComboBox.getItems().remove(0);
        }
        for (Integer i = 0;i < sp.maatti_array.size();i++) {
            if(sp.maatti_array.get(i).getToimipaikka().equals(lähtöComboBox.getValue()))
                lähtöautomaattiComboBox.getItems().add(sp.maatti_array.get(i).getNimi());
        }
    }

    @FXML
    private void kohdeSelect(ActionEvent event) {
        SmartPostit sp = SmartPostit.getInstance();

        while (kohdeautomaattiComboBox.getItems().size() > 0) {
            kohdeautomaattiComboBox.getItems().remove(0);
        }
        for (Integer i = 0;i < sp.maatti_array.size();i++) {
            if(sp.maatti_array.get(i).getToimipaikka().equals(kohdeComboBox.getValue()))
                kohdeautomaattiComboBox.getItems().add(sp.maatti_array.get(i).getNimi());
        }
    }

    @FXML
    private void luokanInfo(ActionEvent event) {
        if (valitseluokkaComboBox.getValue().equals("1. Luokka"))
            infotextArea.setText("Ensimmäisen luokan toimitukset ovat nopeimpia, mutta toimituksien maksimi etäisyys on 150km. Myöskin kaikki särkyvät esineet tulevat menemään rikki ensimmäisen luokan lähetyksessä.");
        
        else if (valitseluokkaComboBox.getValue().equals("2. Luokka"))
            infotextArea.setText("Toisen luokan toimitukset ovat turvallisimpia, ja luokka sallii särkyvien esineiden lähettämisen kunhan esineet eivät ole liian isoja (<12000cm^3).");
        
        else if (valitseluokkaComboBox.getValue().equals("3. Luokka"))
            infotextArea.setText("Kolmannen luokan toimitukset ovat kaikkein riskialttiimpia särkymiselle, ellei esine ole suuri kokoinen (>12000cm^3) ja painava (>12kg). Tämä on myös kaikkein hitain toimitusluokka.");
        else
            infotextArea.setText("");
    }

    @FXML
    private void Peruuta(ActionEvent event) throws SQLException {
        Stage stage = (Stage) peruutaButton.getScene().getWindow();
        stage.close();
    }
    
    //Luodaan uusi paketti
    @FXML
    private void Luo(ActionEvent event) {
        SmartPostit sp = SmartPostit.getInstance();
        Connection connection = SqliteConnection.Connector();
        String luokka = String.valueOf(valitseluokkaComboBox.getValue());
        Character c = luokka.charAt(0);
        Integer lk = Character.getNumericValue(c);
        Integer pktID = null, asiakasID = null, xe = 0, o1 = 0, o2 = 0;
        
        if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        
        if (!nimiField.getText().isEmpty() & !kokoField.getText().isEmpty() & !massaField.getText().isEmpty() & valitseluokkaComboBox.getValue() != null & lähtöautomaattiComboBox.getValue() != null & kohdeautomaattiComboBox.getValue() != null) {
            String eNimi = nimiField.getText();
            Double eKoko = Double.valueOf(kokoField.getText());
            Float eMassa = Float.valueOf(massaField.getText());
            Boolean eBrk = särkyvääCheckBox.isSelected();
            try {
                PreparedStatement psEsine = connection.prepareStatement("INSERT INTO esine (nimi, koko, breakable, massa) VALUES (?,?,?,?)");
                PreparedStatement psPaketti = connection.prepareStatement("INSERT INTO paketti (ID, asiakasID) VALUES (?,?)");
                PreparedStatement psReitti = connection.prepareStatement("INSERT INTO reitti (osoite1, osoite2) VALUES (?,?)");
                PreparedStatement psAsiakas = connection.prepareStatement("INSERT INTO asiakas (aNimi) VALUES (?)");
                PreparedStatement psAid = connection.prepareStatement("SELECT asiakasID FROM asiakas WHERE aNimi = ?");

                psEsine.setString(1, eNimi);
                psEsine.setDouble(2, eKoko);
                psEsine.setBoolean(3, eBrk);
                psEsine.setFloat(4, eMassa);
                psEsine.executeUpdate();
                
                for(Integer i = 0; i < sp.maatti_array.size();i++) {
                    if(sp.maatti_array.get(i).getNimi().equals(lähtöautomaattiComboBox.getValue())) {
                        o1 = i;
                    }
                    if(sp.maatti_array.get(i).getNimi().equals(kohdeautomaattiComboBox.getValue())) {
                        o2 = i;
                    }
                }
                psReitti.setString(1, sp.maatti_array.get(o1).getOsoite());
                psReitti.setString(2, sp.maatti_array.get(o2).getOsoite());

                psReitti.executeUpdate();
                
                for(Integer i = 0;i<sp.paketti_array.size();i++) {
                    pktID = sp.paketti_array.get(i).getPakettiID();
                    asiakasID = sp.paketti_array.get(i).getAsiakasID();
                }
            
                psPaketti.setInt(1, lk);
                if (lähettäjäField.getText().isEmpty()) { 
                    psPaketti.setInt(2, 1);
                    if (pktID != null)
                        sp.paketti_array.add(new Paketti(pktID+1, lk, 1));
                    sp.esine_array.add(new Esine(pktID+1, eNimi, eKoko, eBrk, eMassa));
                    psPaketti.executeUpdate();
                }
                else {
                    psAsiakas.setString(1, lähettäjäField.getText());
                    psAsiakas.executeUpdate();

                    psAid.setString(1, lähettäjäField.getText());
                    ResultSet rsID = psAid.executeQuery();
                    psPaketti.setInt(2, Integer.valueOf(rsID.getString(1)));

                   if (pktID != null & asiakasID != null)
                        sp.paketti_array.add(new Paketti(pktID+1, lk, asiakasID));
                    sp.esine_array.add(new Esine(pktID+1, eNimi, eKoko, eBrk, eMassa));
                    psPaketti.executeUpdate();
                    rsID.close();
                }

                Stage stage = (Stage) luopakettiButton.getScene().getWindow();
                stage.close();      
                psPaketti.close();
                psEsine.close();
                psAsiakas.close();
                psAid.close();
                psReitti.close();

                } catch (SQLException ex) {
                    Logger.getLogger(LuoPakettiController.class.getName()).log(Level.SEVERE, null, ex);
                }
        }
        else if (valitseesineComboBox.getValue() != null & valitseluokkaComboBox.getValue() != null & lähtöautomaattiComboBox.getValue() != null & kohdeautomaattiComboBox.getValue() != null) {
            try {
                PreparedStatement psEsine = connection.prepareStatement("INSERT INTO esine (nimi, koko, breakable, massa) VALUES (?,?,?,?)");
                PreparedStatement psPaketti = connection.prepareStatement("INSERT INTO paketti (ID, asiakasID) VALUES (?,?)");
                PreparedStatement psReitti = connection.prepareStatement("INSERT INTO reitti (osoite1, osoite2) VALUES (?,?)");
                PreparedStatement psAsiakas = connection.prepareStatement("INSERT INTO asiakas (aNimi) VALUES (?)");
                PreparedStatement psAid = connection.prepareStatement("SELECT asiakasID FROM asiakas WHERE aNimi = ?");

                for (Integer i = 0; i < sp.esine_array.size(); i++) {
                    if(sp.esine_array.get(i).getNimi().equals(valitseesineComboBox.getValue())) {
                        xe = i;
                    }
                }
                psEsine.setString(1, sp.esine_array.get(xe).getNimi());
                psEsine.setDouble(2, sp.esine_array.get(xe).getKoko());
                psEsine.setBoolean(3, sp.esine_array.get(xe).getBrk());
                psEsine.setFloat(4, sp.esine_array.get(xe).getMassa());
                psEsine.executeUpdate();
                
                for(Integer i = 0; i < sp.maatti_array.size();i++) {
                    if(sp.maatti_array.get(i).getNimi().equals(lähtöautomaattiComboBox.getValue())) {
                        o1 = i;
                    }
                    if(sp.maatti_array.get(i).getNimi().equals(kohdeautomaattiComboBox.getValue())) {
                        o2 = i;
                    }
                }
                psReitti.setString(1, sp.maatti_array.get(o1).getOsoite());
                psReitti.setString(2, sp.maatti_array.get(o2).getOsoite());

                psReitti.executeUpdate();

                psPaketti.setInt(1, lk);
                if (lähettäjäField.getText().isEmpty()) { 
                    psPaketti.setInt(2, 1);
                    psPaketti.executeUpdate();
                }
                else {
                    psAsiakas.setString(1, lähettäjäField.getText());
                    psAsiakas.executeUpdate();
                    
                    psAid.setString(1, lähettäjäField.getText());
                    ResultSet rsID = psAid.executeQuery();
                    
                    psPaketti.setInt(2, Integer.valueOf(rsID.getString(1)));
                    psPaketti.executeUpdate();
                    
                    rsID.close();
                }
                Stage stage = (Stage) luopakettiButton.getScene().getWindow();
                stage.close();
                
                psPaketti.close();
                psAsiakas.close();
                psAid.close();
                psEsine.close();
                psReitti.close();
            } catch (SQLException ex) {
                Logger.getLogger(LuoPakettiController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else {
            infotextArea.setText("Syötä tarvittavat tiedot ennen paketin luontia!");
        }
    }
}
