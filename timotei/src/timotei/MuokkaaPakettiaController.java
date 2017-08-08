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
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Sadmiral
 */
public class MuokkaaPakettiaController implements Initializable {
    
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
    private Label infotextArea;
    @FXML
    private Label pktidLabel;
    @FXML
    private Button poistaButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        SmartPostit sp = SmartPostit.getInstance();
        Connection connection = SqliteConnection.Connector();
        String prev = null;
        
        if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        
        //Haetaan comboboxeihin tarvittavat tiedot
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
    
    //Alustetaan aluksi paketin alkuperäiset arvot kenttiin
    public void setValues(Integer pID) {
        Connection connection;
        connection = SqliteConnection.Connector();
        pktidLabel.setText(String.valueOf(pID));
        
        if (connection == null) {
            System.out.println("Tietokantaan yhdistäminen epäonnistui.");
            System.exit(1);
        }
        try {
            PreparedStatement psPaketti = connection.prepareStatement("SELECT * FROM paketti JOIN esine ON esineID = ? "
                    + "JOIN asiakas ON paketti.asiakasID = asiakas.asiakasID WHERE esineID = pakettiID AND paketti.asiakasID = asiakas.asiakasID");
            psPaketti.setInt(1,pID);
            ResultSet rsP = psPaketti.executeQuery();

            Integer ID = Integer.valueOf(rsP.getString(2));
            String nimi = rsP.getString(5);
            Double koko = Double.valueOf(rsP.getString(6));
            Boolean brk = Boolean.valueOf(rsP.getString(7));
            Float massa = Float.valueOf(rsP.getString(8));
            String aNimi = rsP.getString(10);
            
            if (brk == true)
                särkyvääCheckBox.setSelected(true);
            
            nimiField.setText(nimi);
            kokoField.setText(String.valueOf(koko));
            massaField.setText(String.valueOf(massa));
            
            psPaketti.close();
            rsP.close();
        } catch (SQLException ex) {
            Logger.getLogger(FXMLDocumentController.class.getName()).log(Level.SEVERE, null, ex);
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

    //Muutetaan pakettia tietokannassa käyttäjän syötteiden mukaan
    @FXML
    private void Luo(ActionEvent event) {
        SmartPostit sp = SmartPostit.getInstance();
        Connection connection = SqliteConnection.Connector();
        String luokka = String.valueOf(valitseluokkaComboBox.getValue());
        Character c = luokka.charAt(0);
        Integer lk = Character.getNumericValue(c), x = null, o1 = 0, o2 = 0;
        
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
            Integer pktID = Integer.valueOf(pktidLabel.getText());
            
            for (Integer i = 0; i < sp.paketti_array.size(); i++) {
                if (pktID == sp.paketti_array.get(i).getPakettiID()) 
                    x = i;
            }
            PreparedStatement psEsine = connection.prepareStatement("UPDATE esine SET (nimi, koko, breakable, massa) = (?,?,?,?) WHERE esineID = ?");
            PreparedStatement psPaketti = connection.prepareStatement("UPDATE paketti SET (ID) = (?)");
            PreparedStatement psReitti = connection.prepareStatement("UPDATE reitti SET (osoite1, osoite2) = (?,?) WHERE reittiID = ?");

            psEsine.setString(1, eNimi);
            psEsine.setDouble(2, eKoko);
            psEsine.setBoolean(3, eBrk);
            psEsine.setFloat(4, eMassa);
            psEsine.setInt(5, pktID);
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
            psReitti.setInt(3, pktID);
            
            psReitti.executeUpdate();

            psPaketti.setInt(1, lk);
            psPaketti.executeUpdate();
            
            Integer asID = sp.paketti_array.get(x).getAsiakasID();
            sp.paketti_array.remove(x);
            sp.paketti_array.add(x, new Paketti(pktID, lk, asID));
            sp.esine_array.remove(x);
            sp.esine_array.add(x, new Esine(pktID, eNimi, eKoko, eBrk, eMassa));
            Stage stage = (Stage) luopakettiButton.getScene().getWindow();
            stage.close();
                     
            psPaketti.close();
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

    @FXML
    private void Poista(ActionEvent event) {
        SmartPostit sp = SmartPostit.getInstance();
        try {
            Connection connection = SqliteConnection.Connector();
            Integer pktID = Integer.valueOf(pktidLabel.getText());

            if (connection == null) {
                System.out.println("Tietokantaan yhdistäminen epäonnistui.");
                System.exit(1);
            }
            PreparedStatement psEsine = connection.prepareStatement("DELETE FROM esine WHERE esineID = ?");
            PreparedStatement psPaketti = connection.prepareStatement("DELETE FROM paketti WHERE pakettiID = (?)");
            PreparedStatement psReitti = connection.prepareStatement("DELETE FROM reitti WHERE reittiID = ?");
            
            psEsine.setInt(1, pktID);
            psPaketti.setInt(1, pktID);
            psReitti.setInt(1, pktID);
            
            psEsine.executeUpdate();
            psPaketti.executeUpdate();
            psReitti.executeUpdate();
            
            for (int i = 0; i < sp.esine_array.size(); i++) {
                if (pktID == sp.esine_array.get(i).getEsineID()) {
                    sp.esine_array.remove(i);
                    sp.paketti_array.remove(i);
                }
            }
            psEsine.close();
            psPaketti.close();
            psReitti.close();
            
            Stage stage = (Stage) poistaButton.getScene().getWindow();
            stage.close();
        } catch (SQLException ex) {
            Logger.getLogger(MuokkaaPakettiaController.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }
}
