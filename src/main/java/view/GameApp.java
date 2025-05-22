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
import model.PostAttackReinforceListener;
import model.Territory;

public class GameApp extends Application implements PostAttackReinforceListener {
    private GameManager gameManager;
    private Text turnLabel;
    private Label reinforcementLabel;
    private TextField armyInput;
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
    private boolean mustReinforceAfterConquest = false;

    private Button plusButton;
    private Button minusButton;
    private Button confirmMoveButton;
    private Label moveLabel;

    private int armiesToMove = 1;

    /**
     * Saldırı sonrası takviye aşamasının başlatılması.
     */
    @Override
    public void onPostAttackReinforce(Territory attacker, Territory conquered) {
        System.out.println("Saldırı sonrası takviye işlemi başlatıldı.");
        handlePostAttackReinforce(attacker, conquered);
    }

    private void handlePostAttackReinforce(Territory from, Territory to) {
        System.out.println("🧩 handlePostAttackReinforce çağrıldı");
        System.out.println("🧩 from: " + (from != null ? from.getTerritoryName() : "null"));
        System.out.println("🧩 to: " + (to != null ? to.getTerritoryName() : "null"));
        if (!isPostAttackReinforcePhase) {
            System.out.println("Saldırı sonrası takviye aşamasında değilsiniz.");
            return;
        }

        if (from == null || to == null) {
            System.out.println("Takviye yapılacak ülkeler seçilmedi.");
            return;
        }

        if (from.getOwner() != gameManager.getCurrentPlayer() || to.getOwner() != gameManager.getCurrentPlayer()) {
            System.out.println("Sadece kendi ülkeleriniz arasında takviye yapabilirsiniz.");
            return;
        }

        if (from.getArmies() <= 1) {
            System.out.println(from.getTerritoryName() + " ülkesinde yeterli ordu yok.");
            return;
        }

        selectedTerritory = from;
        targetTerritory = to;

        System.out.println("✅ selectedTerritory: " + selectedTerritory.getTerritoryName());
        System.out.println("✅ targetTerritory: " + targetTerritory.getTerritoryName());

        enablePostAttackControls();

        System.out.println("🟢 " + from.getTerritoryName() + " ülkesinden " + to.getTerritoryName() + " ülkesine takviye yapabilirsiniz.");
    }


//    private void executePostAttackReinforce(Territory from, Territory to) {
//        if (!isPostAttackReinforcePhase) {
//            System.out.println("Saldırı sonrası takviye aşamasında değilsiniz.");
//            return;
//        }
//
//        if (from == null || to == null) {
//            System.out.println("Takviye yapılacak ülkeler seçilmedi.");
//            return;
//        }
//
//        try {
//            int armiesToSend = Integer.parseInt(postAttackInput.getText());
//
//            if (armiesToSend < 1 || armiesToSend >= from.getArmies()) {
//                System.out.println("Takviye için geçersiz sayı.");
//                return;
//            }
//
//            gameManager.reinforceBetweenTerritories(from, to, armiesToSend);
//
//            updateArmyText();
//            resetCircleColors();
//
//            selectedTerritory = null;
//            targetTerritory = null;
//
//            postAttackInput.clear();
//            postAttackInput.setDisable(true);
//            postAttackButton.setDisable(true);
//
//            // Saldırı sonrası takviye aşaması sona erdi
//            isPostAttackReinforcePhase = false;
//
//            // Mavi oyuncunun takviye aşamasına geçiş
//            gameManager.nextTurn();
//            Player nextPlayer = gameManager.getCurrentPlayer();
//            turnLabel.setText("Takviye Aşaması - " + nextPlayer.getName());
//            reinforcementLabel.setText("Kalan Takviye: " + gameManager.getTempReinforcement());
//            armyInput.setDisable(false);
//            armyInput.clear();
//
//            isReinforcementPhase = true; // Mavi oyuncunun takviye aşaması başlıyor
//            System.out.println(nextPlayer.getName() + " oyuncusu takviye aşamasında.");
//
//        } catch (NumberFormatException ex) {
//            System.out.println("Geçerli bir sayı girin.");
//        }
//    }


    @Override
    public void start(Stage primaryStage) {
        gameManager = GameManager.getInstance();
        gameManager.setPostAttackReinforceListener(this::handlePostAttackReinforce);
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

        // + - butonları ve taşıma label'ı
        moveLabel = new Label("Taşınacak Ordu: 1");
        moveLabel.setLayoutX(10);
        moveLabel.setLayoutY(110);
        moveLabel.setVisible(false);
        content.getChildren().add(moveLabel);

        plusButton = new Button("+");
        plusButton.setLayoutX(150);
        plusButton.setLayoutY(110);
        plusButton.setDisable(true);
        plusButton.setVisible(false);
        content.getChildren().add(plusButton);

        minusButton = new Button("-");
        minusButton.setLayoutX(180);
        minusButton.setLayoutY(110);
        minusButton.setDisable(true);
        minusButton.setVisible(false);
        content.getChildren().add(minusButton);

        confirmMoveButton = new Button("Onayla");
        confirmMoveButton.setLayoutX(220);
        confirmMoveButton.setLayoutY(110);
        confirmMoveButton.setDisable(true);
        confirmMoveButton.setVisible(false);
        content.getChildren().add(confirmMoveButton);

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

//        // Burada postAttackButton'un aksiyonunu tanımlıyoruz
//        postAttackButton.setOnAction(event -> {
//            if (selectedTerritory != null && targetTerritory != null) {
//                executePostAttackReinforce(selectedTerritory, targetTerritory);
//            } else {
//                System.out.println("Takviye yapılacak hedef ülke seçilmedi veya geçersiz seçim.");
//            }
//        });
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
                // 1. Saldırı Sonrası Takviye Aşaması
                if (isPostAttackReinforcePhase) {
                    if (selectedTerritory != null) {
                        handlePostAttackReinforce(selectedTerritory, territory);
                    } else {
                        System.out.println("Saldırı sonrası takviye aşamasında seçili ülke yok.");
                    }
                }
                // 2. Saldırı Aşaması
                else if (isAttackPhase) {
                    handleAttackPhase(territory);
                }
                // 3. Takviye Aşaması
                else if (isReinforcementPhase) {
                    handleReinforcementPhase(territory, armyText); // İKİ PARAMETRE İLE GÖNDERİYORUZ
                }
            });


            content.getChildren().addAll(circle, armyText);
        }

        plusButton.setOnAction(e -> {
            if (selectedTerritory != null) {
                int maxMove = selectedTerritory.getArmies() - 1;
                if (armiesToMove < maxMove) {
                    armiesToMove++;
                    moveLabel.setText("Taşınacak Ordu: " + armiesToMove);
                }
            }
        });

        minusButton.setOnAction(e -> {
            if (armiesToMove > 1) {
                armiesToMove--;
                moveLabel.setText("Taşınacak Ordu: " + armiesToMove);
            }
        });


        confirmMoveButton.setOnAction(e -> {
            System.out.println("🎯 Onayla butonuna basıldı");

            if (selectedTerritory != null && targetTerritory != null) {
                System.out.println("Seçilen kaynak: " + selectedTerritory.getTerritoryName() + " (" + selectedTerritory.getArmies() + " ordu)");
                System.out.println("Hedef ülke: " + targetTerritory.getTerritoryName() + " (" + targetTerritory.getArmies() + " ordu)");
                System.out.println("Taşınacak ordu: " + armiesToMove);

                gameManager.reinforceBetweenTerritories(selectedTerritory, targetTerritory, armiesToMove);

                System.out.println("✅ Takviye işlemi GameManager üzerinden çağrıldı.");

                updateArmyText();
                resetCircleColors();
                disablePostAttackControls();

                mustReinforceAfterConquest = false;

                selectedTerritory = null;
                targetTerritory = null;
                armiesToMove = 1;

                // 🧠 Şimdi sıradaki hamle: tekrar saldırabilir mi?
                if (hasAnyAttackCapableTerritory(gameManager.getCurrentPlayer())) {
                    System.out.println("🔁 Saldırıya devam edilebilir. Ülke seçimi bekleniyor...");
                    promptAttackStartSelection();  // saldırı fazına geri dön
                    isReinforcementPhase = false;
                    isAttackPhase = true;
                } else {
                    // Eğer saldıracak ülke yoksa sıradaki oyuncuya geç
                    gameManager.nextTurn();
                    Player nextPlayer = gameManager.getCurrentPlayer();
                    gameManager.startReinforcementPhase(nextPlayer);

                    System.out.println("🛑 Saldırı bitti. Yeni oyuncu: " + nextPlayer.getName());

                    turnLabel.setText("Takviye Aşaması - " + nextPlayer.getName());
                    reinforcementLabel.setText("Kalan Takviye: " + gameManager.getTempReinforcement());
                    armyInput.setDisable(false);
                    isReinforcementPhase = true;
                    isAttackPhase = false;
                }

            } else {
                System.out.println("❌ selectedTerritory veya targetTerritory null!");
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
        System.out.println("==> handleReinforcementPhase çağrıldı");
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

                currentPlayer = gameManager.getCurrentPlayer();

                if (!hasAnyAttackCapableTerritory(currentPlayer)) {
                    System.out.println("Saldırı yapabilecek ülke yok, sıra geçiyor.");
                    handleEndAttackPhase();
                } else {
                    promptAttackStartSelection();
                }
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

//        postAttackInput.clear();
//        postAttackInput.setDisable(true);
//        postAttackButton.setDisable(true);
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
//        postAttackInput.clear();
//        postAttackInput.setDisable(true);
//        postAttackButton.setDisable(true);
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
        System.out.println("==> handleAttackPhase çağrıldı");

        if (mustReinforceAfterConquest) {
            System.out.println("🔒 Önce fetih sonrası asker aktarımı yapılmalı!");
            return;
        }

        Player currentPlayer = gameManager.getCurrentPlayer();

        // Eğer henüz saldıran ülke seçilmemişse
        if (selectedTerritory == null) {
            if (territory.getOwner() == currentPlayer && canTerritoryAttack(territory, currentPlayer)) {
                selectedTerritory = territory;
                highlightNeighbors(selectedTerritory);
                System.out.println("Saldıran ülke seçildi: " + selectedTerritory.getTerritoryName());
            } else {
                System.out.println("Bu ülke ile saldırı yapamazsınız (ya size ait değil ya da uygun değil).");
            }
        }

        // Saldıran ülke zaten seçilmişse
        else {
            // Tıklanan ülke düşman ve komşu ise saldırı başlatılır
            if (territory.getOwner() != currentPlayer && selectedTerritory.getNeighbors().contains(territory)) {
                System.out.println("Saldırı başlatılıyor...");
                gameManager.attack(selectedTerritory, territory);

                updateArmyText();
                resetCircleColors();

                if (territory.getOwner() == currentPlayer) {
                    System.out.println("İşgal gerçekleşti: " + territory.getTerritoryName());

                    // Fetih sonrası kaynak ve hedefi ayır
                    lastAttacker = selectedTerritory;
                    lastConquered = territory;

                    selectedTerritory = lastAttacker;
                    targetTerritory = lastConquered;

                    gameManager.postAttackReinforce(lastAttacker, lastConquered);

                    highlightNeighbors(selectedTerritory);
                    enablePostAttackControls();
                    mustReinforceAfterConquest = true;

                    return;
                }


                // Eğer işgal olmadıysa ve saldıran ülkenin ordusu 1'e düştüyse
                if (selectedTerritory.getArmies() <= 1) {
                    System.out.println("Saldıran ülkenin ordusu 1'e düştü. Bu ülke pasif hale geldi.");
                    selectedTerritory = null;
                    resetCircleColors();

                    if (!hasAnyAttackCapableTerritory(currentPlayer)) {
                        System.out.println("Saldıracak başka ülke kalmadı. Tur bitiriliyor.");
                        handleEndAttackPhase();
                    }

                    return;
                }

                // Saldırı başarısız ama başka deneme olabilir
                selectedTerritory = null;
            } else {
                System.out.println("Hedef ülke geçersiz veya saldırı yapılamaz.");
            }
        }
    }


    private void handleEndAttackPhase() {
        System.out.println("Saldırı aşaması manuel olarak sonlandırıldı.");

        selectedTerritory = null;
        targetTerritory = null;

        endAttackButton.setDisable(true);

        isAttackPhase = false;
        isReinforcementPhase = false;
        isPostAttackReinforcePhase = false;

//        postAttackInput.clear();
//        postAttackInput.setDisable(true);
//        postAttackButton.setDisable(true);
        endAttackButton.setDisable(true);
        endAttackButton.setVisible(false); // Eğer kullanıyorsan


        // Sıradaki oyuncuya geç
        gameManager.nextTurn();
        Player nextPlayer = gameManager.getCurrentPlayer();

        if (nextPlayer.getOwnedTerritories().size() == 1) {
            System.out.println(nextPlayer.getName() + " sadece 1 ülkeye sahip, takviye aşaması atlanıyor.");
            startAttackPhase();
        } else {
            System.out.println("Takviye aşamasına geçiliyor: " + nextPlayer.getName());
            reinforcementLabel.setText("Kalan Takviye: " + gameManager.getTempReinforcement());
            armyInput.setDisable(false);
            isReinforcementPhase = true;
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
        Player currentPlayer = gameManager.getCurrentPlayer(); // Mevcut oyuncuyu al

        if (selected.getArmies() <= 1) {
            System.out.println("Bu ülkenin saldırı yapacak ordusu yok.");
            return; // hiç highlight yapma
        }

        for (Territory neighbor : selected.getNeighbors()) {
            System.out.println("Komşu: " + neighbor.getTerritoryName() +
                    " | Sahibi: " + (neighbor.getOwner() != null ? neighbor.getOwner().getName() : "null") +
                    " | Mevcut oyuncu: " + currentPlayer.getName());

            if (neighbor.getOwner() != selected.getOwner()) {
                Circle neighborCircle = getCircleByTerritory(neighbor);
                if (neighborCircle != null) {
                    neighborCircle.setFill(Color.YELLOW);
                } else {
                    System.out.println("⚠️ Circle bulunamadı: " + neighbor.getTerritoryName());
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

    /**
     * Verilen ülke saldırı yapabilir mi?
     * - En az 2 orduya sahip olmalı (1 ordu bırakmak zorunda)
     * - En az bir düşman komşusu olmalı
     */
    private boolean canTerritoryAttack(Territory t, Player p) {
        if (t.getArmies() <= 1) return false;

        return t.getNeighbors().stream()
                .anyMatch(n -> n.getOwner() != p);
    }

    private boolean hasAnyAttackCapableTerritory(Player player) {
        return player.getOwnedTerritories().stream()
                .anyMatch(t -> canTerritoryAttack(t, player));
    }

    private void promptAttackStartSelection() {
        endAttackButton.setDisable(false);
        endAttackButton.setVisible(true);

        System.out.println("Takviye tamamlandı, saldırı için ülkenizi seçin.");

        turnLabel.setText("Takviye tamamlandı, saldırı için ülkenizi seçin.");

        Player currentPlayer = gameManager.getCurrentPlayer();

        for (Territory territory : gameManager.getTerritories()) {
            Circle circle = getCircleByTerritory(territory);
            if (circle != null) {
                if (territory.getOwner() == currentPlayer) {
                    if (canTerritoryAttack(territory, currentPlayer)) {
                        circle.setFill(currentPlayer.getName().equals("Kırmızı") ? Color.RED : Color.BLUE);
                    } else {
                        circle.setFill(currentPlayer.getName().equals("Kırmızı") ? Color.DARKRED : Color.DARKBLUE);
                    }

                }
            }
        }

        // Artık kullanıcı ülkesini seçene kadar saldırı başlamayacak
        isReinforcementPhase = false;
        isAttackPhase = true;
    }

    private void enablePostAttackControls() {
        if (selectedTerritory == null || targetTerritory == null) {
            System.out.println("❌ Kontroller aktif edilemedi çünkü selectedTerritory veya targetTerritory null.");
            return;
        }

        int maxMove = selectedTerritory.getArmies() - 1;

        if (maxMove < 1) {
            System.out.println("❌ Takviye yapılacak yeterli ordu yok. Mevcut: " + selectedTerritory.getArmies());
            return;
        }

        armiesToMove = 1;
        moveLabel.setText("Taşınacak Ordu: " + armiesToMove);

        moveLabel.setVisible(true);
        plusButton.setVisible(true);
        minusButton.setVisible(true);
        confirmMoveButton.setVisible(true);

        plusButton.setDisable(false);
        minusButton.setDisable(false);
        confirmMoveButton.setDisable(false);

        System.out.println("✅ Takviye alanı aktif. maxMove: " + maxMove);
    }



    private void disablePostAttackControls() {
        moveLabel.setVisible(false);
        plusButton.setVisible(false);
        minusButton.setVisible(false);
        confirmMoveButton.setVisible(false);

        plusButton.setDisable(true);
        minusButton.setDisable(true);
        confirmMoveButton.setDisable(true);
    }


}
