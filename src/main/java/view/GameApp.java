package view;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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
    private Label reinforcementLabel;
    private TextField armyInput;
    private TextField postAttackInput;
    private Button postAttackButton;
    private Pane content;
    private Territory selectedTerritory = null;
    private Territory lastAttacker = null;
    private Territory lastConquered = null;
    private final double scaleX = 1024.0 / 1275.0;
    private final double scaleY = 768.0 / 958.0;
    private boolean isReinforcementPhase = true;
    private boolean isAttackPhase = false;
    private boolean isPostAttackReinforcePhase = false;
    private Territory targetTerritory = null;

    private void handlePostAttackReinforce(Territory fromTerritory) {
        Player currentPlayer = gameManager.getCurrentPlayer();

        if (fromTerritory.getOwner() != currentPlayer) {
            System.out.println("Yalnızca kendi ülkelerinizden takviye yapabilirsiniz.");
            return;
        }

        if (fromTerritory.getArmies() <= 1) {
            System.out.println(fromTerritory.getTerritoryName() + " ülkesinde yeterli ordu yok.");
            return;
        }

        if (selectedTerritory == null) {
            selectedTerritory = fromTerritory;
            highlightFriendlyNeighbors(fromTerritory);
            System.out.println("Kaynak ülke seçildi: " + selectedTerritory.getTerritoryName());
        } else if (fromTerritory != selectedTerritory && selectedTerritory.getNeighbors().contains(fromTerritory)) {
            targetTerritory = fromTerritory;
            System.out.println("Hedef ülke seçildi: " + targetTerritory.getTerritoryName());

            // Takviye input ve butonunu aktif et
            postAttackInput.setDisable(false);
            postAttackButton.setDisable(false);
        } else {
            System.out.println("Bu ülkeye takviye gönderilemez.");
        }
    }


    /**
     * Saldırı sonrası takviye işlemi.
     */
    private void executePostAttackReinforce() {
        if (selectedTerritory == null || targetTerritory == null) {
            System.out.println("Takviye yapılacak ülkeler seçilmedi.");
            return;
        }

        try {
            int armiesToSend = Integer.parseInt(postAttackInput.getText());

            if (armiesToSend < 1 || armiesToSend >= selectedTerritory.getArmies()) {
                System.out.println("Takviye için geçersiz sayı.");
                return;
            }

            gameManager.reinforceBetweenTerritories(selectedTerritory, targetTerritory, armiesToSend);

            updateArmyText();
            resetCircleColors();

            System.out.println(selectedTerritory.getTerritoryName() + " ülkesinden " + targetTerritory.getTerritoryName() + " ülkesine " + armiesToSend + " ordu takviye edildi.");

            // Seçimlerin sıfırlanması
            selectedTerritory = null;
            targetTerritory = null;

            postAttackInput.clear();
            postAttackInput.setDisable(true);
            postAttackButton.setDisable(true);

            // Sıradaki oyuncuya geç
            gameManager.nextTurn();
            Player nextPlayer = gameManager.getCurrentPlayer();
            turnLabel.setText("Takviye Aşaması - " + nextPlayer.getName());
            reinforcementLabel.setText("Kalan Takviye: " + gameManager.getTempReinforcement());
            armyInput.setDisable(false);
            armyInput.clear();

        } catch (NumberFormatException ex) {
            System.out.println("Geçerli bir sayı girin.");
        }
    }


    @Override
    public void start(Stage primaryStage) {
        gameManager = GameManager.getInstance();
        content = new Pane();

        Image mapImage = new Image("file:src/main/resources/risk_map.png");
        ImageView mapView = new ImageView(mapImage);
        mapView.setFitWidth(1024);
        mapView.setFitHeight(768);
        mapView.setPreserveRatio(true);

        turnLabel = new Text("Sıra: " + gameManager.getCurrentPlayer().getName());
        turnLabel.setX(10);
        turnLabel.setY(20);
        content.getChildren().add(turnLabel);

        //Player currentPlayer = gameManager.getCurrentPlayer();

        reinforcementLabel = new Label("Kalan Takviye: " + gameManager.getTempReinforcement());
        reinforcementLabel.setLayoutX(10);
        reinforcementLabel.setLayoutY(80);
        content.getChildren().add(reinforcementLabel);

        armyInput = new TextField();
        armyInput.setPromptText("Ordu sayısını girin");
        armyInput.setLayoutX(10);
        armyInput.setLayoutY(50);
        content.getChildren().add(armyInput);

        postAttackInput = new TextField();
        postAttackInput.setPromptText("Takviye sayısı girin");
        postAttackInput.setLayoutX(10);
        postAttackInput.setLayoutY(110);
        postAttackInput.setDisable(true);
        content.getChildren().add(postAttackInput);

        postAttackButton = new Button("Takviye Yap");
        postAttackButton.setLayoutX(150);
        postAttackButton.setLayoutY(110);
        postAttackButton.setDisable(true);
        content.getChildren().add(postAttackButton);

        Player currentPlayer = gameManager.getCurrentPlayer();
        // Oyuncunun sadece 1 ülkesi varsa takviye yapamaz
        if (currentPlayer.getOwnedTerritories().size() == 1) {
            armyInput.setDisable(true);
            reinforcementLabel.setText("Sadece 1 ülkeniz olduğu için takviye yapamazsınız.");
            isReinforcementPhase = false;
            isAttackPhase = true;
            turnLabel.setText("Saldırı Aşaması - " + currentPlayer.getName());
            System.out.println("Saldırı aşamasına geçildi.");
        } else {
            reinforcementLabel.setText("Kalan Takviye: " + gameManager.getTempReinforcement());
        }

        // Burada postAttackButton'un aksiyonunu tanımlıyoruz
        postAttackButton.setOnAction(event -> {
            if (targetTerritory != null) {
                executePostAttackReinforce();
            } else {
                System.out.println("Hedef ülke seçilmedi.");
            }
        });


        for (Territory territory : gameManager.getTerritories()) {
            Circle circle = new Circle(8);
            double scaledX = territory.getX() * scaleX;
            double scaledY = territory.getY() * scaleY;
            circle.setCenterX(scaledX);
            circle.setCenterY(scaledY);
            updateCircleColor(circle, territory);

            Text armyText = new Text(String.valueOf(territory.getArmies()));
            armyText.setX(scaledX - 5);
            armyText.setY(scaledY - 10);
            armyText.setFill(Color.BLACK);

            circle.setOnMouseClicked(e -> {

                // Takviye aşaması
                if (isReinforcementPhase) {
                    handleReinforcementPhase(territory, armyText);
                }

                // Saldırı aşaması
                else if (isAttackPhase) {
                    handleAttackPhase(territory);
                }

                // Saldırı sonrası takviye aşaması
                else if (!isReinforcementPhase && !isAttackPhase && selectedTerritory != null) {

                    if (territory.getOwner() == gameManager.getCurrentPlayer() && selectedTerritory.getNeighbors().contains(territory)) {
                        targetTerritory = territory;
                        System.out.println("Saldırı sonrası takviye: " + selectedTerritory.getTerritoryName() + " -> " + targetTerritory.getTerritoryName());

                        // Takviye butonunu aktif hale getir
                        postAttackButton.setDisable(false);
                        postAttackInput.setDisable(false);
                    } else {
                        System.out.println("Bu ülkeye takviye yapamazsınız.");
                    }
                }
            });

            content.getChildren().addAll(circle, armyText);
        }

        // Buton işlemi
        postAttackButton.setOnAction(event -> {
            if (selectedTerritory != null && targetTerritory != null) {
                executePostAttackReinforce();
            } else {
                System.out.println("Takviye yapılacak hedef ülke seçilmedi.");
            }
        });

        StackPane root = new StackPane(mapView, content);
        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Risk - Saldırı ve Takviye");
        primaryStage.show();
    }

    /**
     * Takviye aşaması: Oyuncu, sahip olduğu ülkelere orduları dağıtır.
     */
    private void handleReinforcementPhase(Territory territory, Text armyText) {
        Player currentPlayer = gameManager.getCurrentPlayer();

        if (territory.getOwner() == currentPlayer) {
            try {
                int armiesToAdd = Integer.parseInt(armyInput.getText());

                gameManager.reinforceTerritory(territory, armiesToAdd);
                armyText.setText(String.valueOf(territory.getArmies()));
                reinforcementLabel.setText("Kalan Takviye: " + gameManager.getTempReinforcement());
                armyInput.clear();

                // Eğer kalan takviye 0'a düştüyse saldırı aşamasına geç
                if (gameManager.getTempReinforcement() == 0) {
                    isReinforcementPhase = false;
                    isAttackPhase = true;
                    turnLabel.setText("Saldırı Aşaması - " + currentPlayer.getName());
                    armyInput.setDisable(true);
                    System.out.println("Saldırı aşamasına geçildi.");
                }

            } catch (NumberFormatException ex) {
                System.out.println("Geçersiz sayı!");
            }
        }
    }


    /**
     * Bir Territory'ye karşılık gelen Circle'ı bulur.
     */
    private Circle getCircleByTerritory(Territory territory) {
        double scaledX = territory.getX() * scaleX;
        double scaledY = territory.getY() * scaleY;

        for (var node : content.getChildren()) {
            if (node instanceof Circle) {
                Circle circle = (Circle) node;
                if (Math.abs(circle.getCenterX() - scaledX) < 1 && Math.abs(circle.getCenterY() - scaledY) < 1) {
                    return circle;
                }
            }
        }
        return null;
    }


    /**
     * Tüm Circle'ların renklerini orijinal durumlarına döndürür.
     */
    private void resetCircleColors() {
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

    /**
     * Oyunun bittiğini ve kazananı ekranda gösterir.
     */
    private void displayGameOver() {
        content.setDisable(true);  // Artık ekranda hiçbir işlem yapılmamalı

        Player winner = gameManager.getWinner();
        String winnerText = (winner != null) ? "Kazanan: " + winner.getName() : "Berabere!";

        Text gameOverText = new Text("GAME OVER\n" + winnerText);
        gameOverText.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        gameOverText.setFill(winner != null && winner.getName().equals("Kırmızı") ? Color.RED : Color.BLUE);
        gameOverText.setX(450);
        gameOverText.setY(400);

        content.getChildren().add(gameOverText);
        System.out.println("Oyun Bitti! " + winnerText);
    }


    private void handleAttackPhase(Territory targetTerritory) {
        Player currentPlayer = gameManager.getCurrentPlayer();

        if (selectedTerritory == null) {
            // Saldıran ülke seçimi
            if (targetTerritory.getOwner() == currentPlayer && targetTerritory.getArmies() > 1) {
                selectedTerritory = targetTerritory;
                highlightNeighbors(selectedTerritory);
                System.out.println("Saldıran ülke seçildi: " + selectedTerritory.getTerritoryName());
            }
        } else {
            // Hedef ülke seçimi
            if (selectedTerritory.getNeighbors().contains(targetTerritory) && targetTerritory.getOwner() != currentPlayer) {
                gameManager.attack(selectedTerritory, targetTerritory);
                updateArmyText();
                resetCircleColors();

                // Saldırı sonrası takviye aşaması
                if (targetTerritory.getArmies() == 0) {
                    System.out.println("Takviye aşaması: " + selectedTerritory.getTerritoryName() + " ile " + targetTerritory.getTerritoryName());
                    isAttackPhase = false;  // Saldırı aşaması bitti
                    postAttackInput.setDisable(false);
                    postAttackButton.setDisable(false);
                    selectedTerritory = targetTerritory; // Takviye için seçilen ülke
                    targetTerritory = null;
                } else {
                    // Eğer hedef ülke ele geçirilmediyse, sıradaki oyuncuya geç
                    isAttackPhase = false;
                    isReinforcementPhase = true;
                    gameManager.nextTurn();
                    Player nextPlayer = gameManager.getCurrentPlayer();
                    turnLabel.setText("Takviye Aşaması - " + nextPlayer.getName());
                    reinforcementLabel.setText("Kalan Takviye: " + gameManager.getTempReinforcement());
                    armyInput.setDisable(false);
                    armyInput.clear();
                }
            } else {
                System.out.println("Hedef ülke geçersiz veya saldırı yapılamaz.");
            }
        }
    }

    /**
     * Saldırı sonrası takviye için dost komşu ülkeleri sarı renkle vurgular.
     */
    private void highlightFriendlyNeighbors(Territory selected) {
        for (Territory neighbor : selected.getNeighbors()) {
            if (neighbor.getOwner() == selected.getOwner()) {
                Circle neighborCircle = getCircleByTerritory(neighbor);
                if (neighborCircle != null) {
                    neighborCircle.setFill(Color.YELLOW);
                }
            }
        }
    }




    /**
     * Renkleri günceller
     */
    private void updateCircleColor(Circle circle, Territory territory) {
        Player owner = territory.getOwner();
        if (owner != null) {
            circle.setFill(owner.getName().equals("Kırmızı") ? Color.RED : Color.BLUE);
        } else {
            circle.setFill(Color.GRAY);
        }
    }

    /**
     * Bir Circle'a karşılık gelen Territory'yi bulur.
     */
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

    /**
     * Saldıran ülkenin komşu düşmanlarını sarı renkle vurgular.
     */
    private void highlightNeighbors(Territory selected) {
        for (Territory neighbor : selected.getNeighbors()) {
            if (neighbor.getOwner() != selected.getOwner()) {
                Circle neighborCircle = getCircleByTerritory(neighbor);
                if (neighborCircle != null) {
                    neighborCircle.setFill(Color.YELLOW);
                }
            }
        }
    }

    /**
     * Ordu sayısını ekrandaki Text nesnelerinde günceller.
     */
    private void updateArmyText() {
        for (var node : content.getChildren()) {
            if (node instanceof Text) {
                Text textNode = (Text) node;
                Territory territory = getTerritoryByText(textNode);

                if (territory != null) {
                    textNode.setText(String.valueOf(territory.getArmies()));
                }
            }
        }
    }


    /**
     * Bir Text nesnesinin x ve y koordinatlarına göre Territory'yi bulur.
     */
    private Territory getTerritoryByText(Text textNode) {
        double textX = textNode.getX() + 5;  // Text'in Circle ile hizalanması için kaydırma
        double textY = textNode.getY() + 10;

        for (Territory territory : gameManager.getTerritories()) {
            double scaledX = territory.getX() * scaleX;
            double scaledY = territory.getY() * scaleY;

            if (Math.abs(scaledX - textX) < 5 && Math.abs(scaledY - textY) < 5) {
                return territory;
            }
        }

        return null;
    }




}
