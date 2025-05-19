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
    private Button endAttackButton;

    /**
     * Saldırı sonrası takviye işlemi.
     */
    /*private void handlePostAttackReinforce(Territory fromTerritory, Territory toTerritory) {
        if (isAttackPhase || isReinforcementPhase) {
            System.out.println("Saldırı veya takviye aşamasında post-attack reinforcement yapılamaz.");
            return;
        }

        Player currentPlayer = gameManager.getCurrentPlayer();

        if (fromTerritory.getOwner() != currentPlayer || toTerritory.getOwner() != currentPlayer) {
            System.out.println("Sadece kendi ülkeleriniz arasında takviye yapabilirsiniz.");
            return;
        }

        if (fromTerritory.getArmies() <= 1) {
            System.out.println(fromTerritory.getTerritoryName() + " ülkesinde yeterli ordu yok.");
            return;
        }

        selectedTerritory = fromTerritory;
        targetTerritory = toTerritory;

        postAttackButton.setDisable(false);
        postAttackInput.setDisable(false);

        System.out.println(fromTerritory.getTerritoryName() + " ülkesinden " + toTerritory.getTerritoryName() + " ülkesine takviye yapabilirsiniz.");
    }*/
    /**
     * Saldırı sonrası takviye aşamasının başlatılması.
     */
    private void handlePostAttackReinforce(Territory attacker, Territory conquered) {
        System.out.println("Saldırı sonrası takviye aşamasına geçildi.");

        this.selectedTerritory = attacker;
        this.targetTerritory = conquered;

        // Post-attack input ve buton aktif hale getiriliyor
        postAttackInput.setDisable(false);
        postAttackButton.setDisable(false);

        System.out.println(attacker.getTerritoryName() + " ülkesinden " + conquered.getTerritoryName() + " ülkesine takviye yapabilirsiniz.");
    }

    /**
     * Saldırı sonrası takviye işlemi.
     */
    private void executePostAttackReinforce(Territory from, Territory to) {
        if (from == null || to == null) {
            System.out.println("Takviye yapılacak ülkeler seçilmedi.");
            return;
        }

        // Hedef ve kaynak ülke aynıysa işlem yapılmaz
        if (from == to) {
            System.out.println("Kaynak ve hedef ülke aynı olamaz.");
            return;
        }

        try {
            int armiesToSend = Integer.parseInt(postAttackInput.getText().trim());

            // Gönderilecek ordu sayısı kontrolü
            if (armiesToSend < 1 || armiesToSend >= from.getArmies()) {
                System.out.println("Takviye için geçersiz sayı. Gönderilebilecek maksimum ordu sayısı: " + (from.getArmies() - 1));
                return;
            }

            // Takviye işlemi
            gameManager.reinforceBetweenTerritories(from, to, armiesToSend);

            // Ordu sayıları güncelleniyor
            updateArmyText();
            resetCircleColors();

            System.out.println(from.getTerritoryName() + " ülkesinden " + to.getTerritoryName() + " ülkesine " + armiesToSend + " ordu gönderildi.");

            // Saldırı sonrası aşama tamamlandığında
            selectedTerritory = null;
            targetTerritory = null;

            postAttackInput.clear();
            postAttackInput.setDisable(true);
            postAttackButton.setDisable(true);

            // Sıradaki oyuncuya geç
            gameManager.nextTurn();
            Player nextPlayer = gameManager.getCurrentPlayer();
            turnLabel.setText("Takviye Aşaması - " + nextPlayer.getName());

            // Takviye aşamasını başlat
            if (nextPlayer.getOwnedTerritories().size() == 1) {
                System.out.println(nextPlayer.getName() + " sadece 1 ülkeye sahip, takviye aşaması atlanıyor.");
                startAttackPhase();
            } else {
                reinforcementLabel.setText("Kalan Takviye: " + gameManager.getTempReinforcement());
                armyInput.setDisable(false);
                armyInput.clear();
            }

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
            isAttackPhase = false;
            isReinforcementPhase = true;
            reinforcementLabel.setText("Kalan Takviye: " + gameManager.getTempReinforcement());
        }

        // Burada postAttackButton'un aksiyonunu tanımlıyoruz
        postAttackButton.setOnAction(event -> {
            if (selectedTerritory != null && targetTerritory != null) {
                executePostAttackReinforce(selectedTerritory, targetTerritory);
            } else {
                System.out.println("Takviye yapılacak hedef ülke seçilmedi veya geçersiz seçim.");
            }
        });
        // Saldırıyı Bitir Butonu
        endAttackButton = new Button("Saldırıyı Bitir");
        endAttackButton.setLayoutX(300);
        endAttackButton.setLayoutY(110);
        endAttackButton.setDisable(true);
        endAttackButton.setOnAction(e -> handleEndAttackPhase());
        content.getChildren().add(endAttackButton);


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
                if (isReinforcementPhase) {
                    handleReinforcementPhase(territory, armyText);
                }
                else if (isAttackPhase) {
                    handleAttackPhase(territory);
                }
                else if (!isReinforcementPhase && !isAttackPhase) {
                    // Saldırı sonrası takviye aşaması
                    if (selectedTerritory != null && selectedTerritory.getOwner() == gameManager.getCurrentPlayer()) {
                        handlePostAttackReinforce(selectedTerritory, territory);
                    } else {
                        System.out.println("Takviye yapılacak kaynak ülke seçilmedi veya geçersiz seçim.");
                    }
                }
            });

            content.getChildren().addAll(circle, armyText);
        }

        // Buton işlemi
        postAttackButton.setOnAction(event -> {
            if (selectedTerritory != null && targetTerritory != null) {
                executePostAttackReinforce(selectedTerritory, targetTerritory);
            } else {
                System.out.println("Takviye yapılacak hedef ülke seçilmedi veya geçersiz seçim.");
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

        if (territory.getOwner() != currentPlayer) {
            System.out.println("Bu ülke size ait değil, takviye yapamazsınız.");
            return;
        }

        try {
            int armiesToAdd = Integer.parseInt(armyInput.getText());

            if (armiesToAdd < 1 || armiesToAdd > gameManager.getTempReinforcement()) {
                System.out.println("Hatalı ordu sayısı: " + armiesToAdd);
                return;
            }

            gameManager.reinforceTerritory(territory, armiesToAdd);
            armyText.setText(String.valueOf(territory.getArmies()));
            reinforcementLabel.setText("Kalan Takviye: " + gameManager.getTempReinforcement());
            armyInput.clear();

            // Eğer kalan takviye hakkı 0 ise saldırı aşamasına geç
            if (gameManager.getTempReinforcement() == 0) {
                isReinforcementPhase = false;
                isAttackPhase = true;
                turnLabel.setText("Saldırı Aşaması - " + currentPlayer.getName());
                System.out.println("Saldırı aşamasına geçildi.");
            }

        } catch (NumberFormatException ex) {
            System.out.println("Lütfen geçerli bir sayı girin.");
        }
    }

    /**
     * Saldırı aşamasının başlatılması.
     */
    private void startAttackPhase() {
        isReinforcementPhase = false;
        isAttackPhase = true;
        selectedTerritory = null;
        targetTerritory = null;

        postAttackInput.clear();
        postAttackInput.setDisable(true);
        postAttackButton.setDisable(true);
        endAttackButton.setDisable(false);

        turnLabel.setText("Saldırı Aşaması - " + gameManager.getCurrentPlayer().getName());
        System.out.println("Saldırı aşamasına geçildi.");
    }

    /**
     * Saldırı aşamasının tamamen sona erdirilmesi ve sıradaki oyuncuya geçiş.
     */
    private void endAttackPhase() {
        isAttackPhase = false;
        isReinforcementPhase = true;

        // Takviye butonunu ve input alanını kapat
        postAttackInput.clear();
        postAttackInput.setDisable(true);
        postAttackButton.setDisable(true);
        endAttackButton.setDisable(true);

        gameManager.nextTurn();
        Player nextPlayer = gameManager.getCurrentPlayer();
        turnLabel.setText("Takviye Aşaması - " + nextPlayer.getName());

        // Eğer oyuncunun sadece 1 ülkesi varsa takviye aşaması atlanır
        if (nextPlayer.getOwnedTerritories().size() == 1) {
            System.out.println(nextPlayer.getName() + " sadece 1 ülkeye sahip, takviye aşaması atlanıyor.");
            startAttackPhase();
        } else {
            gameManager.startReinforcementPhase(nextPlayer);
            reinforcementLabel.setText("Kalan Takviye: " + gameManager.getTempReinforcement());
            armyInput.setDisable(false);
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


    private void handleAttackPhase(Territory territory) {
        Player currentPlayer = gameManager.getCurrentPlayer();

        if (selectedTerritory == null) {
            if (territory.getOwner() == currentPlayer && territory.getArmies() > 1) {
                selectedTerritory = territory;
                highlightNeighbors(selectedTerritory);
                System.out.println("Saldıran ülke seçildi: " + selectedTerritory.getTerritoryName());
            } else {
                System.out.println("Saldırı yapabileceğiniz ülkeyi seçin.");
            }
        }
        // Eğer saldıran ülke seçilmişse
        else {
            if (territory.getOwner() != currentPlayer && selectedTerritory.getNeighbors().contains(territory)) {
                System.out.println("Saldırı başlatılıyor...");
                gameManager.attack(selectedTerritory, territory);

                // Ordu sayısını ve yeni ülke aidiyetlerini güncelle
                updateArmyText();
                resetCircleColors();

                // Eğer saldıran ülkenin ordusu 1'e düştüyse saldırı aşaması sona erer
                if (selectedTerritory.getArmies() <= 1) {
                    System.out.println("Saldıran ülkenin ordusu 1'e düştü. Saldırı sona eriyor.");

                    /*// Eğer oyuncunun sadece 1 ülkesi varsa, saldırı sonrası takviye yapamaz
                    if (currentPlayer.getOwnedTerritories().size() == 1) {
                        System.out.println("Sadece 1 ülkeniz var, takviye yapamazsınız.");
                        postAttackInput.setDisable(true);
                        postAttackButton.setDisable(true);

                        // Sıradaki oyuncuya geç
                        gameManager.nextTurn();
                        Player nextPlayer = gameManager.getCurrentPlayer();
                        turnLabel.setText("Takviye Aşaması - " + nextPlayer.getName());
                        reinforcementLabel.setText("Kalan Takviye: " + gameManager.getTempReinforcement());
                        armyInput.setDisable(false);
                        armyInput.clear();
                        return;
                    }*/


                    handleEndAttackPhase();
                    return;
                }

                // Saldırı sonrası takviye aşaması (eğer target country artık saldıranın olmuşsa)
                if (territory.getOwner() == currentPlayer) {
                    handlePostAttackReinforce(selectedTerritory, territory);
                }

                // Saldırı tamamlandı
                selectedTerritory = null;

            } else {
                System.out.println("Hedef ülke geçersiz veya saldırı yapılamaz.");
            }
        }
    }


    /**
     * Saldırı aşamasını manuel olarak sonlandırır ve takviye aşamasına geçer.
     */
    private void handleEndAttackPhase() {
        System.out.println("Saldırı aşaması manuel olarak sonlandırıldı.");

        // Saldıran ve hedef ülkeler sıfırlanıyor
        selectedTerritory = null;
        targetTerritory = null;

        // Butonları devre dışı bırak
        endAttackButton.setDisable(true);
        postAttackButton.setDisable(true);
        postAttackInput.setDisable(true);

        // Saldırı aşaması bitiyor, takviye aşamasına geçiliyor
        isAttackPhase = false;
        isReinforcementPhase = true;

        // Sıradaki oyuncuya geç
        gameManager.nextTurn();
        Player nextPlayer = gameManager.getCurrentPlayer();

        // Sadece 1 ülkeye sahipse takviye aşaması atlanır
        if (nextPlayer.getOwnedTerritories().size() == 1) {
            System.out.println("Sadece 1 ülkeniz var, takviye yapamazsınız.");
            armyInput.setDisable(true);
            postAttackInput.setDisable(true);
            postAttackButton.setDisable(true);

            // Doğrudan saldırı aşamasına geç
            isReinforcementPhase = false;
            isAttackPhase = true;
            turnLabel.setText("Saldırı Aşaması - " + nextPlayer.getName());
            System.out.println("Saldırı aşamasına geçildi.");
        } else {
            turnLabel.setText("Takviye Aşaması - " + nextPlayer.getName());
            reinforcementLabel.setText("Kalan Takviye: " + gameManager.getTempReinforcement());
            armyInput.setDisable(false);
            armyInput.clear();
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
