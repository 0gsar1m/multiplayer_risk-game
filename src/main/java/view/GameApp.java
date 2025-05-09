package view;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.GameManager;
import model.Player;
import model.Territory;

public class GameApp extends Application {

    private GameManager gameManager;
    private Text turnLabel;
    private final double scaleX = 1024.0 / 1275.0;
    private final double scaleY = 768.0 / 958.0;

    @Override
    public void start(Stage primaryStage) {
        gameManager = GameManager.getInstance();

        Image mapImage = new Image("file:src/main/resources/risk_map.png");
        ImageView mapView = new ImageView(mapImage);
        mapView.setFitWidth(1024);
        mapView.setFitHeight(768);
        mapView.setPreserveRatio(true);

        Pane content = new Pane();

        turnLabel = new Text("Sıra: " + gameManager.getCurrentPlayer().getName());
        turnLabel.setX(10);
        turnLabel.setY(20);
        turnLabel.setFill(Color.BLACK);
        turnLabel.setStyle("-fx-font-size: 16px;");
        content.getChildren().add(turnLabel);

        for (Territory territory : gameManager.getTerritories()) {
            Circle circle = new Circle(8);
            double scaledX = territory.getX() * scaleX;
            double scaledY = territory.getY() * scaleY;
            circle.setCenterX(scaledX);
            circle.setCenterY(scaledY);

            // Ordu Sayısını Gösterme
            Text armyText = new Text(String.valueOf(territory.getArmies()));
            armyText.setX(scaledX - 5);
            armyText.setY(scaledY - 10);
            armyText.setFill(Color.BLACK);
            armyText.setStyle("-fx-font-size: 12px;");

            // Renk Belirleme
            Player owner = territory.getOwner();
            if (owner != null) {
                if (owner.getName().equals("Kırmızı")) {
                    circle.setFill(Color.RED);
                } else if (owner.getName().equals("Mavi")) {
                    circle.setFill(Color.BLUE);
                }
            } else {
                circle.setFill(Color.GRAY);
            }

            // Tıklama Olayı - Sıra Kontrolü ve Ordu Ekleme
            circle.setOnMouseClicked(e -> {
                Player currentPlayer = gameManager.getCurrentPlayer();
                if (owner != null && owner.equals(currentPlayer)) {
                    gameManager.addArmyToTerritory(territory);
                    armyText.setText(String.valueOf(territory.getArmies()));
                    System.out.println("Ordu eklendi: " + territory.getTerritoryName());
                } else {
                    System.out.println("Bu ülke size ait değil veya sıranız değil!");
                }
                gameManager.nextTurn();
                turnLabel.setText("Sıra: " + gameManager.getCurrentPlayer().getName());
            });

            content.getChildren().addAll(circle, armyText);
        }

        StackPane root = new StackPane(mapView, content);
        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setTitle("Risk Game - Sıra ve Ordu Ekleme");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
