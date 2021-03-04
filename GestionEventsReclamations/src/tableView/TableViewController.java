/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tableView;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import helpers.DbConnect;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Observable;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import jdk.nashorn.internal.objects.annotations.Property;
import models.Events;

/**
 * FXML Controller class
 *
 * @author MED
 */
public class TableViewController implements Initializable {

    @FXML
    private TableView<Events> eventsTable;
    @FXML
    private TableColumn<Events, String> idCol;
    @FXML
    private TableColumn<Events, String> catCol;
    @FXML
    private TableColumn<Events, String> titleCol;
    @FXML
    private TableColumn<Events, String> DesCol;
    @FXML
    private TableColumn<Events, String> locationCol;
    @FXML
    private TableColumn<Events, String> stCol;
    @FXML
    private TableColumn<Events, String> enCol;
    
    

    String query = null;
    Connection connection = null;
    PreparedStatement preparedStatement = null;
    ResultSet resultSet = null;
    Events Events = null;

    ObservableList<Events> EventsList = FXCollections.observableArrayList();
    @FXML
    private TableColumn<Events, String> editCol;
    @FXML
    private TextField filterField;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadDate();
    }

    @FXML
    private void close(MouseEvent event) {
                Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    @FXML
    private void getAddView(MouseEvent event) {
        try {
            Parent parent = FXMLLoader.load(getClass().getResource("/tableView/addEvent.fxml"));
            Scene scene = new Scene(parent);
            Stage stage = new Stage();
            stage.setScene(scene);
            stage.initStyle(StageStyle.UTILITY);
            stage.show();
        } catch (IOException ex) {
            Logger.getLogger(TableViewController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void refreshTable() {
        try {
            EventsList.clear();

            query = "SELECT * FROM `events`";
            preparedStatement = connection.prepareStatement(query);
            resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                EventsList.add(new Events(
                        resultSet.getInt("EvID"),
                        resultSet.getString("EvCategory"),
                        resultSet.getString("EvTitle"),
                        resultSet.getString("EvDescription"),
                        resultSet.getString("EvLocation"),
                        resultSet.getDate("EvStartDate"),
                        resultSet.getDate("EvEndDate")));
                eventsTable.setItems(EventsList);
            }
        } catch (SQLException ex) {
            Logger.getLogger(TableViewController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void loadDate() {
        connection = DbConnect.getConnect();
        refreshTable();
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        catCol.setCellValueFactory(new PropertyValueFactory<>("category"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        DesCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        locationCol.setCellValueFactory(new PropertyValueFactory<>("location"));
        stCol.setCellValueFactory(new PropertyValueFactory<>("StartDate"));
        enCol.setCellValueFactory(new PropertyValueFactory<>("EndDate"));

                //add cell of button edit 
         Callback<TableColumn<Events, String>, TableCell<Events, String>> cellFoctory = (TableColumn<Events, String> param) -> {
            // make cell containing buttons
            final TableCell<Events, String> cell = new TableCell<Events, String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    //that cell created only on non-empty rows
                    if (empty) {
                        setGraphic(null);
                        setText(null);

                    } else {

                        FontAwesomeIconView deleteIcon = new FontAwesomeIconView(FontAwesomeIcon.TRASH);
                        FontAwesomeIconView editIcon = new FontAwesomeIconView(FontAwesomeIcon.PENCIL_SQUARE);

                        deleteIcon.setStyle(
                                " -fx-cursor: hand ;"
                                + "-glyph-size:28px;"
                                //+ "-fx-fill:#ff1744;"
                        );
                        editIcon.setStyle(
                                " -fx-cursor: hand ;"
                                + "-glyph-size:28px;"
                                //+ "-fx-fill:#00E676;"
                        );
                        deleteIcon.setOnMouseClicked((MouseEvent event) -> {
                            
                            try {
                                Events = eventsTable.getSelectionModel().getSelectedItem();
                                query = "DELETE FROM `events` WHERE EvID  ="+Events.getId();
                                connection = DbConnect.getConnect();
                                preparedStatement = connection.prepareStatement(query);
                                preparedStatement.execute();
                                refreshTable();
                                
                            } catch (SQLException ex) {
                                Logger.getLogger(TableViewController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                           

                          

                        });
                        editIcon.setOnMouseClicked((MouseEvent event) -> {
                            
                            Events = eventsTable.getSelectionModel().getSelectedItem();
                            FXMLLoader loader = new FXMLLoader ();
                            loader.setLocation(getClass().getResource("/tableView/addEvent.fxml"));
                            try {
                                loader.load();
                            } catch (IOException ex) {
                                Logger.getLogger(TableViewController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            
                            AddEventController AddEventController = loader.getController();
                            AddEventController.setUpdate(true);
                            AddEventController.setTextField(Events.getId(), Events.getTitle(),Events.getCategory(),Events.getDescription(),Events.getLocation(),
                                    Events.getStartDate().toLocalDate(),Events.getEndDate().toLocalDate());
                            Parent parent = loader.getRoot();
                            Stage stage = new Stage();
                            stage.setScene(new Scene(parent));
                            stage.initStyle(StageStyle.UTILITY);
                            stage.show();
                            

                           

                        });

                        HBox managebtn = new HBox(editIcon, deleteIcon);
                        managebtn.setStyle("-fx-alignment:center");
                        HBox.setMargin(deleteIcon, new Insets(2, 2, 0, 3));
                        HBox.setMargin(editIcon, new Insets(2, 3, 0, 2));

                        setGraphic(managebtn);

                        setText(null);

                    }
                }

            };

            return cell;
        };
         editCol.setCellFactory(cellFoctory);
         eventsTable.setItems(EventsList);
    }

}
