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
    private Territory selectedTerritory = null;
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

            Text armyText = new Text(String.valueOf(territory.getArmies()));
            armyText.setX(scaledX - 5);
            armyText.setY(scaledY - 10);
            armyText.setFill(Color.BLACK);
            armyText.setStyle("-fx-font-size: 12px;");

            updateCircleColor(circle, territory);

            circle.setOnMouseClicked(e -> {
                Player currentPlayer = gameManager.getCurrentPlayer();

                if (selectedTerritory == null) {
                    if (territory.getOwner() != null && territory.getOwner().equals(currentPlayer)) {
                        selectedTerritory = territory;
                        System.out.println("Seçilen ülke: " + selectedTerritory.getTerritoryName());

                        highlightNeighbors(content, selectedTerritory, currentPlayer);
                    } else {
                        System.out.println("Bu ülke size ait değil veya sıranız değil!");
                    }
                } else {
                    if (selectedTerritory != territory) {
                        if (selectedTerritory.getNeighbors().contains(territory)) {
                            gameManager.attack(selectedTerritory, territory);

                            Text defenderText = getArmyTextByTerritory(territory, content);
                            if (defenderText != null) {
                                defenderText.setText(String.valueOf(territory.getArmies()));
                            }

                            Text attackerText = getArmyTextByTerritory(selectedTerritory, content);
                            if (attackerText != null) {
                                attackerText.setText(String.valueOf(selectedTerritory.getArmies()));
                            }

                            Circle defenderCircle = getCircleByTerritory(territory, content);
                            if (defenderCircle != null) {
                                updateCircleColor(defenderCircle, territory);
                            }

                            Circle attackerCircle = getCircleByTerritory(selectedTerritory, content);
                            if (attackerCircle != null) {
                                updateCircleColor(attackerCircle, selectedTerritory);
                            }
                        }
                    }

                    resetCircleColors(content);
                    selectedTerritory = null;
                    gameManager.nextTurn();
                    turnLabel.setText("Sıra: " + gameManager.getCurrentPlayer().getName());

                    if (gameManager.isGameOver()) {
                        displayGameOver(content, gameManager.getWinner());

                    }
                }
            });

            content.getChildren().addAll(circle, armyText);
        }

        StackPane root = new StackPane(mapView, content);
        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setTitle("Risk Game - Saldırı Mekanizması");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void displayGameOver(Pane content, Player winner) {
        content.setDisable(true);

        Text gameOverText = new Text("GAME OVER\nKazanan: " + winner.getName());
        gameOverText.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        gameOverText.setFill(winner.getName().equals("Kırmızı") ? Color.RED : Color.BLUE);
        gameOverText.setX(450);
        gameOverText.setY(400);

        content.getChildren().add(gameOverText);
    }

    private void highlightNeighbors(Pane content, Territory selected, Player currentPlayer) {
        for (Territory neighbor : selected.getNeighbors()) {
            if (neighbor.getOwner() != null && neighbor.getOwner() != currentPlayer) {
                Circle neighborCircle = getCircleByTerritory(neighbor, content);
                if (neighborCircle != null) {
                    neighborCircle.setFill(Color.YELLOW);
                }
            }
        }
    }

    private void resetCircleColors(Pane content) {
        for (var node : content.getChildren()) {
            if (node instanceof Circle) {
                Circle circle = (Circle) node;
                Territory territory = getTerritoryByCircle(circle);
                if (territory != null) {
                    updateCircleColor(circle, territory);
                }
            }
        }
    }

    private void updateCircleColor(Circle circle, Territory territory) {
        Player owner = territory.getOwner();
        if (owner != null) {
            circle.setFill(owner.getName().equals("Kırmızı") ? Color.RED : Color.BLUE);
        } else {
            circle.setFill(Color.GRAY);
        }
    }

    private Territory getTerritoryByCircle(Circle circle) {
        for (Territory territory : gameManager.getTerritories()) {
            double scaledX = territory.getX() * scaleX;
            double scaledY = territory.getY() * scaleY;
            if (circle.getCenterX() == scaledX && circle.getCenterY() == scaledY) {
                return territory;
            }
        }
        return null;
    }

    private Circle getCircleByTerritory(Territory territory, Pane content) {
        for (var node : content.getChildren()) {
            if (node instanceof Circle) {
                Circle c = (Circle) node;
                double scaledX = territory.getX() * scaleX;
                double scaledY = territory.getY() * scaleY;
                if (c.getCenterX() == scaledX && c.getCenterY() == scaledY) {
                    return c;
                }
            }
        }
        return null;
    }

    private Text getArmyTextByTerritory(Territory territory, Pane content) {
        for (var node : content.getChildren()) {
            if (node instanceof Text) {
                Text t = (Text) node;
                double scaledX = territory.getX() * scaleX - 5;
                double scaledY = territory.getY() * scaleY - 10;
                if (t.getX() == scaledX && t.getY() == scaledY) {
                    return t;
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
