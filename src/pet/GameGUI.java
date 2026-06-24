package pet;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;
import java.util.Random;
import java.util.*;

public class GameGUI extends Application {

    private static final int W = 1050, H = 750;

    private Pet pet;
    private Pet2D pet2D;
    private DatabaseManager db;
    private SoundManager sound;
    private FileSaveManager fileSave;
    private boolean soundEnabled = true;

    private int petId = -1, level = 1, totalFeeds = 0, totalPlays = 0;
    private boolean petSick = false;
    private boolean soundAvailable = true;

    private String owner = "Player";
    private List<PetSaveData> petList = new ArrayList<>();
    private int currentPetIndex = -1;
    private int coins = 0;
    private int age = 0;
    private int dryFoodStock, wetFoodStock, treatStock, vitaminStock;

    private Label nameLabel, speciesLabel, levelLabel, speechLabel;
    private Label coinLabel, ageLabel;
    private Label hungerVal, happinessVal, energyVal, healthVal;
    private Rectangle hungerFill, happinessFill, energyFill, healthFill;
    private Button soundBtn, shopBtn, leaderboardBtn, prevPetBtn, nextPetBtn, newPetBtn, miniGameBtn;
    private boolean sleeping = false;

    private Timeline gameLoop, saveTimer, speechFadeTimer;
    private int tickCount = 0;
    private int speechCooldownTicks = 0;

    private Pane centerPane;
    private BorderPane root;
    private Stage stage;
    private VBox toastContainer;
    private VBox createForm;
    private Node currentOverlay;
    private Rectangle currentOverlayBg;
    private double overlayW, overlayH;

    @Override
    public void start(Stage stage) {
        db = DatabaseManager.getInstance();
        sound = SoundManager.getInstance();
        fileSave = new FileSaveManager();
        soundAvailable = sound.hasSounds();
        if (!soundAvailable)
            System.out.println("[Game] Sound folder kosong, efek suara nonaktif");

        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(
                new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(245, 230, 255)),
                        new Stop(0.4, Color.rgb(255, 228, 235)),
                        new Stop(0.7, Color.rgb(230, 240, 255)),
                        new Stop(1, Color.rgb(220, 245, 230))),
                CornerRadii.EMPTY, Insets.EMPTY)));
        root.setTop(buildTopBar(stage));
        root.setCenter(buildCenter());
        root.setBottom(buildBottom());

        this.root = root;
        this.stage = stage;
        Scene scene = new Scene(root, W, H);
        scene.getStylesheets().add("file:styles.css");
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case DIGIT1:
                    doAction("feed");
                    break;
                case DIGIT2:
                    doAction("play");
                    break;
                case DIGIT3:
                    doAction("bath");
                    break;
                case DIGIT4:
                    doAction("vitamin");
                    break;
                case DIGIT5:
                    doAction("sleep");
                    break;
                default:
                    break;
            }
        });

        if (db.isConnected()) {
            petList = db.getPetsByOwner(owner);
        }
        if (petList.isEmpty()) {
            List<PetSaveData> filePets = fileSave.load();
            if (!filePets.isEmpty()) {
                petList = filePets;
            }
        }

        if (!petList.isEmpty()) {
            switchToPet(0);
        } else {
            showCreateScreen();
        }

        stage.setTitle("\uD83D\uDC3E Simulasi Pet 2D Modern");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();

        startGameLoop();
        startAutoSave();
    }

    private Node buildTopBar(Stage stage) {
        HBox bar = new HBox(8);
        bar.getStyleClass().add("top-bar");
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(8, 16, 8, 16));

        Label title = new Label("\uD83D\uDC3E Pet Simulator");
        title.getStyleClass().add("title-label");

        prevPetBtn = new Button("\u25C0");
        prevPetBtn.getStyleClass().add("nav-btn");
        prevPetBtn.setOnAction(e -> switchPet(-1));
        prevPetBtn.setDisable(true);

        nameLabel = new Label("--");
        nameLabel.getStyleClass().add("pet-name-label");

        nextPetBtn = new Button("\u25B6");
        nextPetBtn.getStyleClass().add("nav-btn");
        nextPetBtn.setOnAction(e -> switchPet(1));
        nextPetBtn.setDisable(true);

        speciesLabel = new Label("");
        speciesLabel.getStyleClass().add("species-label");

        ageLabel = new Label("");
        ageLabel.getStyleClass().add("age-label");

        levelLabel = new Label("Lv.1");
        levelLabel.getStyleClass().add("level-badge");

        coinLabel = new Label("\uD83E\uDE99 0");
        coinLabel.getStyleClass().add("coin-label");

        miniGameBtn = makeFeatureBtn("\uD83C\uDFAE", "Mini Game", "#89CFF0",
                () -> showOverlay("minigame", this::showMiniGame));
        shopBtn = makeFeatureBtn("\uD83D\uDED2", "Shop", "#FFB347", () -> showOverlay("shop", this::showShop));
        leaderboardBtn = makeFeatureBtn("\uD83C\uDFC6", "Leaderboard", "#C490E4",
                () -> showOverlay("leaderboard", this::showLeaderboard));
        newPetBtn = makeFeatureBtn("\u2795", "Pet Baru", "#77DD77", this::showCreateScreen);

        soundBtn = new Button("\uD83D\uDD0A");
        soundBtn.getStyleClass().addAll("top-icon-btn", "sound-btn");
        soundBtn.setOnAction(e -> toggleSound());
        if (!soundAvailable) {
            soundBtn.setText("\uD83D\uDD07");
            soundBtn.setStyle(soundBtn.getStyle() + ";-fx-opacity:0.5;");
        }

        Button saveBtn = new Button("\uD83D\uDCBE");
        saveBtn.getStyleClass().addAll("top-icon-btn", "save-btn");
        saveBtn.setOnAction(e -> saveToDB());

        Button closeBtn = new Button("\u2716");
        closeBtn.getStyleClass().addAll("top-icon-btn", "close-btn");
        closeBtn.setOnAction(e -> {
            saveToDB();
            fileSave.save(petList);
            stage.close();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bar.getChildren().addAll(title, spacer,
                prevPetBtn, nameLabel, nextPetBtn, speciesLabel,
                ageLabel, levelLabel, coinLabel,
                miniGameBtn, shopBtn, leaderboardBtn, newPetBtn,
                soundBtn, saveBtn, closeBtn);
        return bar;
    }

    private Button makeFeatureBtn(String emoji, String tooltip, String color, Runnable action) {
        Button btn = new Button(emoji);
        btn.getStyleClass().add("feature-btn");
        btn.setStyle("-fx-background-color: " + color + "30; -fx-border-color: " + color + "60;");
        Tooltip tp = new Tooltip(tooltip);
        tp.setShowDelay(Duration.millis(400));
        Tooltip.install(btn, tp);
        btn.setOnAction(e -> action.run());
        btn.setOnMouseEntered(
                e -> btn.setStyle("-fx-background-color: " + color + "70; -fx-border-color: " + color + ";"));
        btn.setOnMouseExited(
                e -> btn.setStyle("-fx-background-color: " + color + "30; -fx-border-color: " + color + "60;"));
        return btn;
    }

    private Node buildCenter() {
        centerPane = new Pane();
        centerPane.setMinSize(0, 0);
        centerPane.setPrefSize(W - 40, 340);

        centerPane.setOnMouseMoved(e -> {
            if (sleeping)
                return;
            if (pet2D != null) {
                pet2D.lookAt(e.getX(), e.getY());
            }
        });

        speechLabel = new Label("");
        speechLabel.getStyleClass().add("speech-bubble");
        speechLabel.setManaged(false);
        speechLabel.setVisible(false);

        toastContainer = new VBox(6);
        toastContainer.setManaged(false);
        toastContainer.setAlignment(Pos.BOTTOM_RIGHT);
        toastContainer.setPadding(new Insets(0, 16, 16, 0));
        toastContainer.setPickOnBounds(false);
        toastContainer.setMaxWidth(400);
        toastContainer.layoutXProperty().bind(
                centerPane.widthProperty().subtract(toastContainer.widthProperty()).subtract(16));
        toastContainer.layoutYProperty().bind(
                centerPane.heightProperty().subtract(toastContainer.heightProperty()).subtract(16));

        centerPane.getChildren().addAll(speechLabel, toastContainer);
        speechLabel.toFront();
        toastContainer.toFront();

        addBackgroundDecorations();

        return centerPane;
    }

    private void addBackgroundDecorations() {
        String[] emojis = { "\u2601\uFE0F", "\u2B50", "\uD83D\uDCAB", "\uD83C\uDF38", "\uD83D\uDC95", "\u2728" };
        Random rng = new Random();
        for (int i = 0; i < 8; i++) {
            Label deco = new Label(emojis[rng.nextInt(emojis.length)]);
            deco.setStyle("-fx-font-size: " + (16 + rng.nextInt(18)) + "px; -fx-opacity: "
                    + (0.15 + rng.nextDouble() * 0.2) + ";");
            deco.setManaged(false);
            deco.setMouseTransparent(true);
            double startX = rng.nextDouble() * 900;
            double startY = rng.nextDouble() * 350;
            deco.setLayoutX(startX);
            deco.setLayoutY(startY);

            Timeline floatAnim = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(deco.layoutYProperty(), startY)),
                    new KeyFrame(Duration.seconds(8 + rng.nextDouble() * 10),
                            new KeyValue(deco.layoutYProperty(), startY - 100 - rng.nextDouble() * 200,
                                    Interpolator.EASE_BOTH)));
            floatAnim.setCycleCount(Animation.INDEFINITE);
            floatAnim.setAutoReverse(true);
            floatAnim.play();

            Timeline swayAnim = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(deco.layoutXProperty(), startX)),
                    new KeyFrame(Duration.seconds(6 + rng.nextDouble() * 8),
                            new KeyValue(deco.layoutXProperty(), startX + 30 - rng.nextDouble() * 60,
                                    Interpolator.EASE_BOTH)));
            swayAnim.setCycleCount(Animation.INDEFINITE);
            swayAnim.setAutoReverse(true);
            swayAnim.play();

            centerPane.getChildren().add(0, deco);
        }
    }

    private Node buildBottom() {
        VBox box = new VBox(8);
        box.setPadding(new Insets(8, 20, 12, 20));
        box.setAlignment(Pos.CENTER);

        HBox statusBox = new HBox(12);
        statusBox.getStyleClass().add("status-panel");
        statusBox.setAlignment(Pos.CENTER_LEFT);

        hungerVal = new Label("0%");
        happinessVal = new Label("0%");
        energyVal = new Label("0%");
        healthVal = new Label("0%");

        hungerFill = new Rectangle();
        happinessFill = new Rectangle();
        energyFill = new Rectangle();
        healthFill = new Rectangle();

        statusBox.getChildren().addAll(
                statusGroup("\uD83C\uDF56", "Lapar", "#FFB347", hungerFill, hungerVal),
                statusGroup("\uD83D\uDE0A", "Senang", "#FF8FAB", happinessFill, happinessVal),
                statusGroup("\u26A1", "Energi", "#89CFF0", energyFill, energyVal),
                statusGroup("\u2764\uFE0F", "Sehat", "#77DD77", healthFill, healthVal));

        HBox btnBox = new HBox(16);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(4, 0, 4, 0));

        btnBox.getChildren().addAll(
                makeActionBtn("\uD83C\uDF56", "Makan", "#FFB347", () -> doAction("feed")),
                makeActionBtn("\u26BD", "Bermain", "#FF8FAB", () -> doAction("play")),
                makeActionBtn("\uD83D\uDEC1", "Mandi", "#89CFF0", () -> doAction("bath")),
                makeActionBtn("\uD83D\uDC8A", "Vitamin", "#77DD77", () -> doAction("vitamin")),
                makeActionBtn("\uD83D\uDE34", "Tidur", "#C490E4", () -> doAction("sleep")));

        box.getChildren().addAll(statusBox, btnBox);
        return box;
    }

    private Node statusGroup(String icon, String label, String color, Rectangle fill, Label val) {
        VBox g = new VBox(3);
        g.setPrefWidth(170);

        Label iconLbl = new Label(icon + "  " + label);
        iconLbl.getStyleClass().add("status-label");

        HBox barRow = new HBox(6);
        barRow.setAlignment(Pos.CENTER_LEFT);

        Pane barPane = new Pane();
        barPane.setPrefSize(120, 14);
        barPane.setMinSize(120, 14);

        Rectangle track = new Rectangle(120, 14);
        track.setArcWidth(14);
        track.setArcHeight(14);
        track.setFill(Color.rgb(230, 220, 240, 0.4));

        fill.setArcWidth(14);
        fill.setArcHeight(14);
        fill.setWidth(0);
        fill.setHeight(14);
        fill.setFill(Color.web(color));

        Rectangle clipRect = new Rectangle(120, 14);
        clipRect.setArcWidth(14);
        clipRect.setArcHeight(14);
        fill.setClip(clipRect);

        barPane.getChildren().addAll(track, fill);

        val.getStyleClass().add("status-value");
        barRow.getChildren().addAll(barPane, val);
        g.getChildren().addAll(iconLbl, barRow);
        return g;
    }

    private String shortcutKey(String label) {
        switch (label) {
            case "Makan":
                return "1";
            case "Bermain":
                return "2";
            case "Mandi":
                return "3";
            case "Vitamin":
                return "4";
            case "Tidur":
                return "5";
            default:
                return "";
        }
    }

    private String tooltipText(String label) {
        switch (label) {
            case "Makan":
                return "Beri pet makanan [" + shortcutKey(label) + "]";
            case "Bermain":
                return "Ajak pet bermain [" + shortcutKey(label) + "]";
            case "Mandi":
                return "Mandikan pet [" + shortcutKey(label) + "]";
            case "Vitamin":
                return "Beri vitamin [" + shortcutKey(label) + "]";
            case "Tidur":
                return "Tidurkan / bangunkan [" + shortcutKey(label) + "]";
            default:
                return "";
        }
    }

    private VBox makeActionBtn(String emoji, String labelText, String color, Runnable action) {
        Button btn = new Button(emoji);
        btn.getStyleClass().add("action-btn");
        btn.setStyle("-fx-background-color: " + color + "40; -fx-border-color: " + color + "70;");
        Tooltip tp = new Tooltip(tooltipText(labelText));
        tp.setShowDelay(javafx.util.Duration.millis(400));
        Tooltip.install(btn, tp);
        btn.setOnAction(e -> {
            ScaleTransition pulse = new ScaleTransition(Duration.millis(150), btn);
            pulse.setToX(0.88);
            pulse.setToY(0.88);
            pulse.setAutoReverse(true);
            pulse.setCycleCount(2);
            pulse.play();
            action.run();
        });
        btn.setOnMouseEntered(
                e -> btn.setStyle("-fx-background-color: " + color + "90; -fx-border-color: " + color + ";"));
        btn.setOnMouseExited(
                e -> btn.setStyle("-fx-background-color: " + color + "40; -fx-border-color: " + color + "70;"));

        Label lbl = new Label("[" + shortcutKey(labelText) + "] " + labelText);
        lbl.getStyleClass().add("action-label");

        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(btn, lbl);
        return box;
    }

    private void showFeedMenu() {
        if (pet == null || pet2D == null)
            return;
        if (pet.getHunger() < 20) {
            showSpeech("Aduh perutku kenyang banget! \uD83D\uDE2D");
            showToast("Perut penuh! Tunggu lapar dulu.");
            sound.play("sad");
            return;
        }
        beginOverlay(0.45);
        Pane pane = new Pane();
        pane.setManaged(false);
        pane.setPickOnBounds(false);
        pane.resize(overlayW, overlayH);
        pane.setLayoutX(0);
        pane.setLayoutY(0);

        VBox card = new VBox(14);
        card.getStyleClass().add("overlay-card");
        card.setMaxWidth(340);
        card.setPrefWidth(340);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(24));
        card.setLayoutX((overlayW - 340) / 2);
        card.setLayoutY((overlayH - 360) / 2);
        Label title = new Label("\uD83C\uDF56 Pilih Makanan");
        title.getStyleClass().add("overlay-title");
        VBox list = new VBox(10);
        list.setAlignment(Pos.CENTER);
        int[] stocks = { dryFoodStock, wetFoodStock, treatStock, vitaminStock };
        String[] stockNames = { "Makanan Kering", "Makanan Basah", "Snack", "Vitamin" };
        String[] stockEmojis = { "\uD83C\uDF5A", "\uD83E\uDD5B", "\uD83C\uDF6C", "\uD83D\uDC8A" };
        for (int i = 0; i < stockNames.length; i++) {
            int idx = i;
            Button btn = new Button(stockEmojis[i] + "  " + stockNames[i] + "  (\u00D7" + stocks[i] + ")");
            btn.setStyle(
                    "-fx-background-color: #A8E6CF; -fx-text-fill: #2D3436; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 12; -fx-cursor: hand;");
            btn.setMaxWidth(260);
            btn.setOnAction(e -> {
                int[] curStocks = { dryFoodStock, wetFoodStock, treatStock, vitaminStock };
                if (curStocks[idx] <= 0) {
                    showToast("Stok " + stockNames[idx] + " habis! Beli di toko.");
                    sound.play("sad");
                    return;
                }
                closeCurrentOverlay();
                switch (idx) {
                    case 0:
                        dryFoodStock--;
                        pet.feed(new DryFood("Makanan Kering"));
                        break;
                    case 1:
                        wetFoodStock--;
                        pet.feed(new WetFood("Makanan Basah"));
                        break;
                    case 2:
                        treatStock--;
                        pet.feed(new Treat("Snack"));
                        break;
                    case 3:
                        vitaminStock--;
                        if (pet instanceof Careable) {
                            ((Careable) pet).giveVitamin();
                            petSick = false;
                        }
                        break;
                }
                totalFeeds++;
                pet.addCoins(1);
                showSpeech("Nyam nyam enak! \uD83D\uDE0B");
                sound.play("eat");
                if (pet2D != null) {
                    pet2D.setExpression("happy");
                    pet2D.animateAction("feed");
                }
                new Thread(() -> {
                    try {
                        Thread.sleep(1200);
                    } catch (InterruptedException ignored) {
                    }
                    Platform.runLater(() -> {
                        if (pet2D != null)
                            pet2D.setExpression("normal");
                    });
                }).start();
                updateStatus();
                saveToDB();
            });
            list.getChildren().add(btn);
        }
        Button closeBtn = new Button("Tutup");
        closeBtn.setStyle(
                "-fx-background-color: #FF6B6B; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 32; -fx-background-radius: 12; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> closeCurrentOverlay());
        card.getChildren().addAll(title, list, closeBtn);
        pane.getChildren().add(card);
        centerPane.getChildren().add(pane);
        currentOverlay = pane;
        pane.setUserData("feed");
    }

    private void showToast(String message) {
        Platform.runLater(() -> {
            Label toast = new Label(message);
            toast.getStyleClass().add("toast");
            toast.setWrapText(true);
            toast.setMaxWidth(380);
            toast.setOpacity(0);
            toast.setTranslateY(30);

            if (toastContainer.getChildren().size() >= 3) {
                toastContainer.getChildren().remove(0);
            }
            toastContainer.getChildren().add(toast);

            Timeline anim = new Timeline(
                    new KeyFrame(Duration.millis(250),
                            new KeyValue(toast.opacityProperty(), 1, Interpolator.EASE_BOTH),
                            new KeyValue(toast.translateYProperty(), 0, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.seconds(2.5),
                            new KeyValue(toast.opacityProperty(), 1, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.seconds(2.8),
                            new KeyValue(toast.opacityProperty(), 0, Interpolator.EASE_BOTH),
                            new KeyValue(toast.translateYProperty(), 20, Interpolator.EASE_BOTH)));
            anim.setOnFinished(e -> toastContainer.getChildren().remove(toast));
            anim.play();
        });
    }

    private void doAction(String action) {
        if (pet == null || pet2D == null)
            return;

        if (petSick && !action.equals("vitamin")) {
            showSpeech("Aku sakit... kasih vitamin dong! \uD83D\uDE22");
            showToast("Pet sedang sakit! Beri vitamin dulu!");
            return;
        }

        sound.play("click");
        if (sleeping && !action.equals("sleep")) {
            showSpeech("Ssstt... sedang tidur! \uD83D\uDE34");
            return;
        }

        int oldHunger = pet.getHunger();
        int oldHappiness = pet.getHappiness();
        int oldEnergy = pet.getEnergy();
        int oldHealth = pet.getHealth();

        switch (action) {
            case "feed":
                showOverlay("feed", this::showFeedMenu);
                break;

            case "play":
                int energyBefore = pet.getEnergy();
                pet.play();
                if (pet.getEnergy() == energyBefore) {
                    showSpeech("Capek... istirahat dulu ya! \uD83D\uDE34");
                    showToast("Energi terlalu rendah untuk bermain.");
                    sound.play("sad");
                    return;
                }
                totalPlays++;
                pet.addCoins(2);
                showSpeech("Yeay seru banget! \u2728");
                sound.play("happy");
                if (pet2D != null) {
                    pet2D.setExpression("happy");
                    pet2D.animateAction("play");
                }
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                    Platform.runLater(() -> {
                        if (pet2D != null)
                            pet2D.setExpression("normal");
                    });
                }).start();
                break;

            case "bath":
                if (pet instanceof Careable) {
                    ((Careable) pet).groom();
                    sound.play("water");
                    if (pet2D != null) {
                        pet2D.setExpression("happy");
                        pet2D.animateAction("bath");
                    }
                    showSpeech("Segar setelah mandi! \uD83D\uDEC1");
                    showToast("Sudah dimandikan. Senang bertambah!");
                } else {
                    sound.play("sad");
                    showToast("Pet ini tidak bisa dimandikan!");
                }
                break;

            case "vitamin":
                if (pet instanceof Careable) {
                    ((Careable) pet).giveVitamin();
                    sound.play("chime");
                    if (pet2D != null) {
                        pet2D.setExpression("happy");
                        pet2D.animateAction("vitamin");
                    }
                    petSick = false;
                    showSpeech("Sehat dan kuat! \u2728");
                    showToast("Vitamin diberikan. Sehat bertambah!");
                } else {
                    sound.play("sad");
                    showToast("Pet ini tidak bisa dikasih vitamin!");
                }
                break;

            case "sleep":
                if (sleeping) {
                    showSpeech("Sudah bangun! Selamat pagi! \uD83D\uDE0A");
                    sleeping = false;
                    pet.setEnergy(Math.min(100, pet.getEnergy() + 10));
                    if (pet2D != null) {
                        pet2D.setExpression("normal");
                        pet2D.restartIdle();
                    }
                } else {
                    pet.sleep();
                    sleeping = true;
                    if (pet2D != null) {
                        pet2D.setExpression("sleepy");
                        pet2D.stopIdle();
                        pet2D.animateAction("sleep");
                    }
                    sound.play("snore");
                    showSpeech("Selamat tidur... Zzz \uD83D\uDE34");
                    showToast("Pet tidur nyenyak. Energi bertambah!");
                    spawnFloatingIndicator("\uD83D\uDCA4 Zzz\uD83D\uDCA4",
                            centerPane.getWidth() / 2 - 60, centerPane.getHeight() / 2 - 100, Color.web("#C490E4"));
                }
                break;
        }

        updateCoinsDisplay();
        updateStatus();
        saveToDB();

        int diffHunger = pet.getHunger() - oldHunger;
        int diffHappiness = pet.getHappiness() - oldHappiness;
        int diffEnergy = pet.getEnergy() - oldEnergy;
        int diffHealth = pet.getHealth() - oldHealth;

        java.util.List<FloatingInfo> indicators = new java.util.ArrayList<>();
        if (diffHunger != 0) {
            indicators.add(new FloatingInfo((diffHunger < 0 ? "" : "+") + diffHunger + " Lapar", Color.web("#FFB347")));
        }
        if (diffHappiness != 0) {
            indicators.add(
                    new FloatingInfo((diffHappiness > 0 ? "+" : "") + diffHappiness + " Senang", Color.web("#FF8FAB")));
        }
        if (diffEnergy != 0) {
            indicators
                    .add(new FloatingInfo((diffEnergy > 0 ? "+" : "") + diffEnergy + " Energi", Color.web("#89CFF0")));
        }
        if (diffHealth != 0) {
            indicators.add(new FloatingInfo((diffHealth > 0 ? "+" : "") + diffHealth + " Sehat", Color.web("#77DD77")));
        }

        if (!indicators.isEmpty()) {
            double centerX = centerPane.getWidth() / 2;
            double centerY = centerPane.getHeight() / 2 - 40;
            for (int i = 0; i < indicators.size(); i++) {
                FloatingInfo info = indicators.get(i);
                double offset = (i - (indicators.size() - 1) / 2.0) * 110;
                spawnFloatingIndicator(info.text, centerX + offset, centerY, info.color);
            }
        }
    }

    private static class FloatingInfo {
        String text;
        Color color;

        FloatingInfo(String text, Color color) {
            this.text = text;
            this.color = color;
        }
    }

    private void spawnFloatingIndicator(String text, double startX, double startY, Color textColor) {
        Platform.runLater(() -> {
            Label label = new Label(text);
            label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
            label.setTextFill(textColor);

            DropShadow ds = new DropShadow();
            ds.setOffsetY(2.0);
            ds.setColor(Color.color(0, 0, 0, 0.5));
            label.setEffect(ds);

            label.setManaged(false);
            label.setLayoutX(startX - 55);
            label.setLayoutY(startY);

            centerPane.getChildren().add(label);

            Timeline animation = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(label.layoutYProperty(), startY),
                            new KeyValue(label.opacityProperty(), 1.0)),
                    new KeyFrame(Duration.seconds(1.5),
                            new KeyValue(label.layoutYProperty(), startY - 80, Interpolator.EASE_OUT),
                            new KeyValue(label.opacityProperty(), 0.0, Interpolator.EASE_IN)));
            animation.setOnFinished(e -> centerPane.getChildren().remove(label));
            animation.play();
        });
    }

    private void showSpeech(String text) {
        if (speechFadeTimer != null) {
            speechFadeTimer.stop();
        }

        speechLabel.setText(text);
        speechLabel.setVisible(true);
        speechLabel.toFront();
        speechLabel.setOpacity(1);

        // Set width & height dengan reasonable default
        speechLabel.setPrefWidth(350);
        speechLabel.setWrapText(true);

        // Position di tengah atas
        double screenW = stage.getWidth();
        if (screenW < 400) screenW = 1000;

        double x = (screenW - 350) / 2.0;
        if (x < 10) x = 10;

        speechLabel.setLayoutX(x);
        speechLabel.setLayoutY(20);

        speechCooldownTicks = 3;

        speechFadeTimer = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(speechLabel.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(2.5),
                        new KeyValue(speechLabel.opacityProperty(), 0, Interpolator.EASE_BOTH)));
        speechFadeTimer.setOnFinished(e -> {
            speechLabel.setVisible(false);
            speechLabel.setOpacity(1);
        });
        speechFadeTimer.play();
    }

    private void updateCoinsDisplay() {
        if (pet != null) {
            coins = pet.getCoins();
            Platform.runLater(() -> coinLabel.setText("\uD83E\uDE99 " + coins));
        }
    }

    private void updateStatus() {
        if (pet == null)
            return;
        Platform.runLater(() -> {
            int hVal = pet.getHunger();
            int haVal = pet.getHappiness();
            int eVal = pet.getEnergy();
            int heVal = pet.getHealth();

            animateBar(hungerFill, hVal / 100.0, colorForStat(hVal, true));
            animateBar(happinessFill, haVal / 100.0, colorForStat(haVal, false));
            animateBar(energyFill, eVal / 100.0, colorForStat(eVal, false));
            animateBar(healthFill, heVal / 100.0, colorForStat(heVal, false));

            hungerVal.setText(hVal + "%");
            happinessVal.setText(haVal + "%");
            energyVal.setText(eVal + "%");
            healthVal.setText(heVal + "%");
        });
    }

    private Color colorForStat(int val, boolean inverted) {
        if (inverted)
            val = 100 - val;
        if (val >= 70) {
            return switch (val / 10) {
                case 7 -> Color.web("#A8E6CF");
                case 8 -> Color.web("#77DD77");
                case 9, 10 -> Color.web("#4CAF50");
                default -> Color.web("#77DD77");
            };
        } else if (val >= 30) {
            return Color.web("#FFD93D");
        } else {
            return Color.web("#FF6B6B");
        }
    }

    private void animateBar(Rectangle fill, double fraction, Color targetColor) {
        double targetW = 120 * Math.max(0, Math.min(1, fraction));
        if (!fill.getFill().equals(targetColor)) {
            fill.setFill(targetColor);
        }
        if (Math.abs(fill.getWidth() - targetW) > 0.5) {
            Timeline anim = new Timeline(
                    new KeyFrame(Duration.millis(250),
                            new KeyValue(fill.widthProperty(), targetW, Interpolator.EASE_BOTH)));
            anim.play();
        } else {
            fill.setWidth(targetW);
        }
    }

    private void toggleSound() {
        soundEnabled = !soundEnabled;
        sound.setEnabled(soundEnabled);
        soundBtn.setText(soundEnabled ? "\uD83D\uDD0A" : "\uD83D\uDD07");
    }

    /*
     * ================================================================
     * CREATE & SWITCH PETS
     * ================================================================
     */

    private void createPet(String name, String species) {
        switch (species.toLowerCase()) {
            case "kucing":
                pet = new Cat(name);
                break;
            case "anjing":
                pet = new Dog(name);
                break;
            case "burung":
                pet = new Bird(name);
                break;
            default:
                pet = new Cat(name);
        }
        age = 0;
        level = 1;
        totalFeeds = 0;
        totalPlays = 0;
        dryFoodStock = 0;
        wetFoodStock = 0;
        treatStock = 0;
        vitaminStock = 0;
        petSick = false;
        sleeping = false;
        petId = -1;

        updateUI();
        stage.setTitle("\uD83D\uDC3E " + pet.getName() + " (" + pet.getSpecies() + ")");
        showToast("Pet baru lahir! Halo " + pet.getName() + "! \uD83C\uDF89");
        showSpeech("Hai! Namaku " + pet.getName() + "! \uD83D\uDE0A");
        sound.playSpeciesSound(species);

        build2DPet(species);
        saveToDB();

        petList = db.isConnected() ? db.getPetsByOwner(owner) : fileSave.load();
        currentPetIndex = petList.size() - 1;
        updatePetNavButtons();
    }

    private void build2DPet(String species) {
        if (pet2D != null) {
            pet2D.stopIdle();
            centerPane.getChildren().remove(pet2D);
        }
        pet2D = new Pet2D(species);

        // Age-based scaling
        double ageScale = 1.0;
        if (age < 50)
            ageScale = 0.75;
        else if (age > 200)
            ageScale = 0.85;
        pet2D.setScaleX(ageScale);
        pet2D.setScaleY(ageScale);

        Runnable centerPet2D = () -> {
            double pw = pet2D.prefWidth(-1);
            double ph = pet2D.prefHeight(-1);
            pet2D.setLayoutX((centerPane.getWidth() - pw) / 2);
            pet2D.setLayoutY((centerPane.getHeight() - ph) / 2 + 20);
        };
        centerPet2D.run();
        centerPane.widthProperty().addListener((obs, o, n) -> centerPet2D.run());
        centerPane.heightProperty().addListener((obs, o, h) -> centerPet2D.run());

        pet2D.setOnMouseClicked(ev -> {
            if (sleeping)
                return;
            pet.makeSound();
            sound.playSpeciesSound(pet.getSpecies());
        });

        pet2D.setOnMouseDragged(ev -> {
            if (sleeping)
                return;
            if (Math.random() < 0.12) {
                spawnFloatingIndicator("\uD83D\uDC95 Sayang!", ev.getSceneX(), ev.getSceneY() - 35,
                        Color.web("#FF5E7E"));
                sound.play("happy");
                pet.setHappiness(Math.min(100, pet.getHappiness() + 3));
                updateStatus();
            }
        });

        centerPane.getChildren().add(pet2D);
        speechLabel.toFront();
        toastContainer.toFront();
    }

    private void switchToPet(int index) {
        if (index < 0 || index >= petList.size())
            return;

        saveToDB();

        currentPetIndex = index;
        PetSaveData data = petList.get(index);

        switch (data.species.toLowerCase()) {
            case "kucing":
                pet = new Cat(data.petName);
                break;
            case "anjing":
                pet = new Dog(data.petName);
                break;
            case "burung":
                pet = new Bird(data.petName);
                break;
            default:
                pet = new Cat(data.petName);
        }

        pet.setHunger(data.hunger);
        pet.setHappiness(data.happiness);
        pet.setEnergy(data.energy);
        pet.setHealth(data.health);
        pet.setAge(data.age);
        pet.setCoins(data.coins);
        petId = data.id;
        level = data.level;
        totalFeeds = data.totalFeeds;
        totalPlays = data.totalPlays;
        coins = data.coins;
        age = data.age;
        dryFoodStock = data.dryFood;
        wetFoodStock = data.wetFood;
        treatStock = data.treat;
        vitaminStock = data.vitamin;
        sleeping = false;
        petSick = (data.health <= 0);

        updateUI();
        build2DPet(data.species);
        showSpeech("Bertemu lagi! \u2728");
        showToast("Memuat " + pet.getName() + "...");
    }

    private void switchPet(int direction) {
        int newIndex = currentPetIndex + direction;
        if (newIndex >= 0 && newIndex < petList.size()) {
            switchToPet(newIndex);
        }
    }

    private void updateUI() {
        if (pet == null)
            return;
        Platform.runLater(() -> {
            nameLabel.setText(pet.getName());
            speciesLabel.setText(pet.getSpecies());
            levelLabel.setText("Lv." + level);
            ageLabel.setText(getAgeLabel(age));
            updateCoinsDisplay();
            updateStatus();
            updatePetNavButtons();
        });
    }

    private String getAgeLabel(int age) {
        if (age < 50)
            return "\uD83D\uDC76 Baby";
        else if (age < 200)
            return "\uD83D\uDC3E Adult";
        else
            return "\uD83D\uDC34 Senior";
    }

    private void updatePetNavButtons() {
        prevPetBtn.setDisable(currentPetIndex <= 0);
        nextPetBtn.setDisable(currentPetIndex >= petList.size() - 1);
    }

    /*
     * ================================================================
     * SAVE / LOAD
     * ================================================================
     */

    private void saveToDB() {
        if (pet == null)
            return;

        PetSaveData data = new PetSaveData();
        data.id = petId;
        data.owner = owner;
        data.petName = pet.getName();
        data.species = pet.getSpecies();
        data.age = age;
        data.coins = pet.getCoins();
        data.hunger = pet.getHunger();
        data.happiness = pet.getHappiness();
        data.energy = pet.getEnergy();
        data.health = pet.getHealth();
        data.level = level;
        data.totalFeeds = totalFeeds;
        data.totalPlays = totalPlays;
        data.dryFood = dryFoodStock;
        data.wetFood = wetFoodStock;
        data.treat = treatStock;
        data.vitamin = vitaminStock;

        if (db.isConnected()) {
            int newId = db.savePet(data);
            if (petId < 0 && newId > 0)
                petId = newId;
            data.id = petId;

            // Update petList
            if (currentPetIndex >= 0 && currentPetIndex < petList.size()) {
                petList.set(currentPetIndex, data);
            }
        }

        fileSave.save(petList);
    }

    /*
     * ================================================================
     * GAME LOOP
     * ================================================================
     */

    private void startGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            if (pet == null)
                return;
            tickCount++;

            if (tickCount % 8 == 0 && !sleeping) {
                pet.timePasses();
            }

            // Age progression
            if (tickCount % 50 == 0) { // 50 kali loop
                age++;
                if (pet != null) {
                    pet.setAge(age);
                    Platform.runLater(() -> ageLabel.setText(getAgeLabel(age)));
                    // Update visual scale
                    if (pet2D != null) {
                        double ageScale = 1.0;
                        if (age < 50)
                            ageScale = 0.75;
                        else if (age > 200)
                            ageScale = 0.85;
                        pet2D.setScaleX(ageScale);
                        pet2D.setScaleY(ageScale);
                    }
                }
            }

            if (sleeping) {
                if (tickCount % 4 == 0) {
                    pet.setEnergy(Math.min(100, pet.getEnergy() + 8));
                    pet.setHunger(Math.min(100, pet.getHunger() + 2));
                    updateStatus();
                }
            }

            if (!sleeping) {
                if (speechCooldownTicks > 0)
                    speechCooldownTicks--;
                if (pet.getHealth() <= 0) {
                    if (speechCooldownTicks == 0)
                        showSpeech("Sakit... tolong aku! \uD83D\uDE22");
                    if (pet2D != null)
                        pet2D.setExpression("sad");
                } else if (pet.getHunger() >= 80) {
                    if (speechCooldownTicks == 0)
                        showSpeech("Laper... minta makan dong! \uD83D\uDE22");
                    if (pet2D != null)
                        pet2D.setExpression("sad");
                } else if (pet.getHappiness() <= 25) {
                    if (speechCooldownTicks == 0)
                        showSpeech("Bosan... ajak main dong! \uD83D\uDE1E");
                    if (pet2D != null)
                        pet2D.setExpression("sad");
                } else {
                    if (pet2D != null)
                        pet2D.setExpression("normal");
                }
            }

            if (pet.getHealth() <= 0 && !petSick) {
                petSick = true;
                showToast("\u26A0\uFE0F PET SAKIT! Beri vitamin segera!");
            } else if (pet.getHealth() > 0 && petSick) {
                petSick = false;
            }

            if (tickCount % 10 == 0 && pet.getHealth() > 0) {
                int oldLevel = level;
                level = 1 + (totalFeeds + totalPlays) / 10;
                if (level > oldLevel) {
                    showToast("\uD83C\uDF89 Level UP! Sekarang Lv." + level + "!");
                    spawnFloatingIndicator("\u2B50 Level Up!", centerPane.getWidth() / 2,
                            centerPane.getHeight() / 2 - 60, Color.web("#C490E4"));
                }
                levelLabel.setText("Lv." + level);
            }

            updateStatus();
            updateCoinsDisplay();

            if (pet.getHealth() <= 0) {
                showToast("\u26A0\uFE0F Kesehatan pet sangat rendah!");
            }
        }));
        gameLoop.setCycleCount(Animation.INDEFINITE);
        gameLoop.play();
    }

    private void startAutoSave() {
        saveTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            if (pet != null) {
                saveToDB();
            }
        }));
        saveTimer.setCycleCount(Animation.INDEFINITE);
        saveTimer.play();
    }

    /*
     * ================================================================
     * CREATE SCREEN
     * ================================================================
     */

    private void showCreateScreen() {
        removeCreateForm();

        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(40));
        form.setMaxWidth(420);
        form.getStyleClass().add("create-form");
        form.setTranslateX((centerPane.getWidth() - 420) / 2);
        form.setTranslateY(20);

        centerPane.widthProperty().addListener((obs, o, n) -> form.setTranslateX((n.doubleValue() - 420) / 2));

        Label welcome = new Label("\u2728 SELAMAT DATANG! \u2728");
        welcome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        welcome.setTextFill(Color.web("#C490E4"));
        welcome.setEffect(new DropShadow(15, Color.rgb(196, 144, 228, 0.6)));

        Label sub = new Label("Pilih dan beri nama pet virtualmu!");
        sub.setFont(Font.font("Segoe UI", 15));
        sub.setTextFill(Color.web("#8B6BAA"));

        TextField nameField = new TextField();
        nameField.setPromptText("Nama pet kamu...");
        nameField.getStyleClass().add("name-field");

        TextField ownerField = new TextField();
        ownerField.setPromptText("Nama kamu (untuk leaderboard)...");
        ownerField.getStyleClass().add("name-field");
        ownerField.setText(owner);

        ToggleGroup speciesGroup = new ToggleGroup();
        HBox speciesBox = new HBox(12);
        speciesBox.setAlignment(Pos.CENTER);

        ToggleButton catBtn = speciesBtn("\uD83D\uDC31\nKucing\nManis & Manja!", "Kucing", speciesGroup);
        ToggleButton dogBtn = speciesBtn("\uD83D\uDC36\nAnjing\nSetia & Aktif!", "Anjing", speciesGroup);
        ToggleButton birdBtn = speciesBtn("\uD83D\uDC26\nBurung\nCeria & Lincah!", "Burung", speciesGroup);

        catBtn.setSelected(true);
        speciesBox.getChildren().addAll(catBtn, dogBtn, birdBtn);

        Button createBtn = new Button("\uD83C\uDF89 BUAT PET!");
        createBtn.getStyleClass().add("create-btn");
        createBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                nameField.setPromptText("Nama dulu dong! \uD83D\uDE0A");
                return;
            }
            if (name.length() > 50) {
                nameField.setText(name.substring(0, 50));
                showToast("Nama dipotong ke 50 karakter");
            }
            owner = ownerField.getText().trim();
            if (owner.isEmpty())
                owner = "Player";
            String species = ((ToggleButton) speciesGroup.getSelectedToggle()).getUserData().toString();
            centerPane.getChildren().remove(form);
            createPet(name, species);
        });

        form.getChildren().addAll(welcome, sub, nameField, ownerField, speciesBox, createBtn);

        Label note = new Label("(Pastikan MySQL menyala agar tersimpan di leaderboard!)");
        note.setFont(Font.font("Segoe UI", 12));
        note.setTextFill(Color.web("#B8A0CC"));
        form.getChildren().add(note);

        centerPane.getChildren().add(form);
        createForm = form;
    }

    private void removeCreateForm() {
        if (createForm != null) {
            centerPane.getChildren().remove(createForm);
            createForm = null;
        }
    }

    private ToggleButton speciesBtn(String text, String name, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(text);
        btn.setUserData(name);
        btn.setToggleGroup(group);
        btn.getStyleClass().add("species-btn");
        btn.selectedProperty().addListener((obs, o, n) -> {
            if (n)
                btn.getStyleClass().add("selected");
            else
                btn.getStyleClass().remove("selected");
        });
        return btn;
    }

    private void closeCurrentOverlay() {
        if (currentOverlay != null) {
            centerPane.getChildren().remove(currentOverlay);  // ✅ REMOVE FROM centerPane
            currentOverlay = null;
        }
        if (currentOverlayBg != null) {
            centerPane.getChildren().remove(currentOverlayBg);  // ✅ REMOVE FROM centerPane
            currentOverlayBg = null;
        }
    }

    private void beginOverlay(double bgOpacity) {
        closeCurrentOverlay();
        overlayW = centerPane.getWidth() < 100 ? W : centerPane.getWidth();
        overlayH = centerPane.getHeight() < 100 ? H : centerPane.getHeight();
        Rectangle bg = new Rectangle(overlayW, overlayH, Color.rgb(0, 0, 0, bgOpacity));
        bg.setManaged(false);
        bg.setMouseTransparent(true);
        centerPane.getChildren().add(bg);  // ✅ ADD TO centerPane, NOT root
        currentOverlayBg = bg;
    }

    private void showOverlay(String type, Runnable showFn) {
        if (currentOverlay != null) {
            Object data = currentOverlay.getUserData();
            if (data != null && data.equals(type)) {
                closeCurrentOverlay();
                return;
            }
            closeCurrentOverlay();
        }
        showFn.run();
    }

    /*
     * ================================================================
     * MINI GAME
     * ================================================================
     */

    private void showMiniGame() {
        if (pet == null)
            return;
        if (sleeping) {
            showToast("Bangunkan pet dulu sebelum main game!");
            return;
        }

        beginOverlay(0.7);

        Label titleLbl = new Label("\uD83C\uDFAE Reaction Clicker");
        titleLbl.getStyleClass().add("overlay-title");
        titleLbl.setStyle(titleLbl.getStyle() + ";-fx-text-fill: white;");
        titleLbl.setLayoutX(overlayW / 2 - 80);
        titleLbl.setLayoutY(20);

        Label scoreLbl = new Label("Score: 0");
        scoreLbl.getStyleClass().add("game-score-label");
        scoreLbl.setLayoutX(30);
        scoreLbl.setLayoutY(20);

        Label timeLbl = new Label("Time: 20s");
        timeLbl.getStyleClass().add("game-time-label");
        timeLbl.setLayoutX(overlayW - 120);
        timeLbl.setLayoutY(22);

        Button target = new Button();
        target.getStyleClass().add("game-target");
        target.setVisible(false);

        Button closeBtn = new Button("✖ Keluar");
        closeBtn.getStyleClass().add("overlay-close-btn");
        closeBtn.setLayoutX(20);
        closeBtn.setLayoutY(overlayH - 55);

        Group content = new Group(titleLbl, scoreLbl, timeLbl, target, closeBtn);
        content.setManaged(false);
        centerPane.getChildren().add(content);
        currentOverlay = content;
        content.setUserData("minigame");

        Random rng = new Random();
        int[] score = { 0 };
        int[] timeLeft = { 20 };
        double gameW = overlayW - 100;
        double gameH = overlayH - 120;

        Timeline moveTimer = new Timeline(new KeyFrame(Duration.millis(1200), e -> {
            double nx = 40 + rng.nextDouble() * gameW;
            double ny = 60 + rng.nextDouble() * gameH;
            target.setLayoutX(nx);
            target.setLayoutY(ny);
            target.setVisible(true);
        }));
        moveTimer.setCycleCount(Animation.INDEFINITE);

        Timeline countdown = new Timeline();
        countdown.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
            timeLeft[0]--;
            timeLbl.setText("Time: " + timeLeft[0] + "s");
            if (timeLeft[0] <= 0) {
                countdown.stop();
                moveTimer.stop();
                target.setVisible(false);

                int reward = score[0] * 3 + 5;
                pet.addCoins(reward);
                pet.setHappiness(Math.min(100, pet.getHappiness() + score[0] * 2));
                updateCoinsDisplay();
                updateStatus();
                updateUI();

                saveToDB();
                showToast("\uD83C\uDF89 Game selesai! Score: " + score[0] + " | Reward: " + reward + " koin!");
                sound.play("chime");

                Label resultLbl = new Label(
                        "\uD83C\uDFC6 Selesai! Score: " + score[0] + " | +" + reward + " \uD83E\uDE99");
                resultLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
                resultLbl.setTextFill(Color.web("#FFD700"));
                resultLbl.setLayoutX(overlayW / 2 - 180);
                resultLbl.setLayoutY(overlayH / 2 - 20);
                content.getChildren().add(resultLbl);

                spawnFloatingIndicator("+" + reward + " Koin!", overlayW / 2 - 40, overlayH / 2 - 80,
                        Color.web("#FFD700"));
            }
        }));
        countdown.setCycleCount(20);

        target.setOnAction(e -> {
            score[0]++;
            scoreLbl.setText("Score: " + score[0]);
            sound.play("click");
            target.setVisible(false);

            double newRate = Math.max(300, 1200 - score[0] * 50);
            moveTimer.stop();
            moveTimer.getKeyFrames().clear();
            moveTimer.getKeyFrames().add(
                    new KeyFrame(Duration.millis(newRate), ev -> {
                        double nx = 40 + rng.nextDouble() * gameW;
                        double ny = 60 + rng.nextDouble() * gameH;
                        target.setLayoutX(nx);
                        target.setLayoutY(ny);
                        target.setVisible(true);
                    }));
            moveTimer.play();
        });

        closeBtn.setOnAction(e -> {
            countdown.stop();
            moveTimer.stop();
            closeCurrentOverlay();
        });

        moveTimer.play();
        countdown.play();
    }

    /*
     * ================================================================
     * SHOP
     * ================================================================
     */

    private void showShop() {
        if (pet == null)
            return;

        beginOverlay(0.55);

        Pane pane = new Pane();
        pane.setManaged(false);
        pane.setPickOnBounds(false);
        pane.resize(overlayW, overlayH);
        pane.setLayoutX(0);
        pane.setLayoutY(0);

        VBox card = new VBox(16);
        card.getStyleClass().add("overlay-card");
        card.setMaxWidth(520);
        card.setPrefWidth(520);
        card.setAlignment(Pos.CENTER);
        card.setLayoutX((overlayW - 520) / 2);
        card.setLayoutY((overlayH - 500) / 2);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("\uD83D\uDED2 TOKO");
        title.getStyleClass().add("overlay-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label coinDisplay = new Label("\uD83E\uDE99 " + pet.getCoins());
        coinDisplay.getStyleClass().add("coin-label");
        coinDisplay.setStyle(coinDisplay.getStyle() + "; -fx-font-size: 16px;");
        header.getChildren().addAll(title, sp, coinDisplay);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setAlignment(Pos.CENTER);

        String[] shopEmojis = { "\uD83C\uDF5A", "\uD83E\uDD5B", "\uD83C\uDF6C", "\uD83D\uDC8A" };
        String[] itemNames = { "Makanan Kering", "Makanan Basah", "Snack", "Vitamin" };
        int[] itemPrices = { 5, 10, 7, 15 };
        String[] itemInfos = { "Lapar -10\nSenang +2", "Lapar -25\nSenang +8", "Lapar -5\nSenang +15",
                "Sehat +15\n(Vitamin)" };

        for (int i = 0; i < 4; i++) {
            int idx = i;
            VBox itemCard = new VBox(8);
            itemCard.getStyleClass().add("shop-item");
            itemCard.setAlignment(Pos.CENTER);

            Label emojiLbl = new Label(shopEmojis[i]);
            emojiLbl.setFont(Font.font("Segoe UI", 36));

            Label nameLbl = new Label(itemNames[i]);
            nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            nameLbl.setTextFill(Color.web("#4A3B5C"));

            Label infoLbl = new Label(itemInfos[i]);
            infoLbl.setFont(Font.font("Segoe UI", 11));
            infoLbl.setTextFill(Color.web("#8B6BAA"));

            Label priceLbl = new Label("\uD83E\uDE99 " + itemPrices[i]);
            priceLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            priceLbl.setTextFill(Color.web("#D4A017"));

            Button buyBtn = new Button("Beli");
            buyBtn.getStyleClass().add("shop-buy-btn");

            buyBtn.setOnAction(e -> {
                if (pet.getCoins() < itemPrices[idx]) {
                    showToast("Koin tidak cukup! Butuh " + itemPrices[idx] + " koin!");
                    sound.play("sad");
                    return;
                }
                pet.addCoins(-itemPrices[idx]);
                updateCoinsDisplay();
                coinDisplay.setText("\uD83E\uDE99 " + pet.getCoins());

                switch (idx) {
                    case 0:
                        dryFoodStock++;
                        break;
                    case 1:
                        wetFoodStock++;
                        break;
                    case 2:
                        treatStock++;
                        break;
                    case 3:
                        vitaminStock++;
                        break;
                }
                updateStatus();
                sound.play("chime");
                showToast("Berhasil membeli " + itemNames[idx] + "! \u2705");
                saveToDB();
            });

            itemCard.getChildren().addAll(emojiLbl, nameLbl, infoLbl, priceLbl, buyBtn);
            grid.add(itemCard, i % 2, i / 2);
        }

        HBox btnRow = new HBox();
        btnRow.setAlignment(Pos.CENTER);
        Button closeBtn = new Button("Tutup");
        closeBtn.getStyleClass().add("overlay-close-btn");
        closeBtn.setOnAction(e -> closeCurrentOverlay());
        btnRow.getChildren().add(closeBtn);

        card.getChildren().addAll(header, grid, btnRow);
        centerPane.getChildren().add(card);
        card.applyCss();
        card.autosize();
        card.layout();
        currentOverlay = card;
        card.setUserData("shop");
    }

    /*
     * ================================================================
     * LEADERBOARD
     * ================================================================
     */

    private void showLeaderboard() {
        beginOverlay(0.55);

        Pane pane = new Pane();
        pane.setManaged(false);
        pane.setPickOnBounds(false);
        pane.resize(overlayW, overlayH);

        VBox card = new VBox(12);
        card.setManaged(false);
        card.resize(520, 450);
        card.getStyleClass().add("overlay-card");
        card.setMaxWidth(520);
        card.setMaxHeight(450);
        card.setAlignment(Pos.TOP_CENTER);
        card.setLayoutX((overlayW - 520) / 2);
        card.setLayoutY((overlayH - 450) / 2);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("\uD83C\uDFC6 LEADERBOARD");
        title.getStyleClass().add("overlay-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button closeBtn = new Button("Tutup");
        closeBtn.getStyleClass().add("overlay-close-btn");
        closeBtn.setOnAction(e -> closeCurrentOverlay());
        header.getChildren().addAll(title, sp, closeBtn);

        VBox listBox = new VBox(6);
        listBox.setPadding(new Insets(8, 0, 0, 0));
        ScrollPane scroll = new ScrollPane(listBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPrefHeight(320);

        List<PetSaveData> lb = db.isConnected() ? db.getLeaderboard() : new ArrayList<>();

        if (lb.isEmpty()) {
            Label emptyLbl = new Label("Belum ada data leaderboard.\nPastikan MySQL menyala!");
            emptyLbl.setFont(Font.font("Segoe UI", 14));
            emptyLbl.setTextFill(Color.web("#B8A0CC"));
            emptyLbl.setAlignment(Pos.CENTER);
            listBox.getChildren().add(emptyLbl);
        } else {
            int maxRank = Math.min(20, lb.size());
            for (int i = 0; i < maxRank; i++) {
                PetSaveData d = lb.get(i);
                int rank = i + 1;

                HBox row = new HBox(10);
                row.getStyleClass().add("lb-row");
                row.setAlignment(Pos.CENTER_LEFT);

                String medal = rank == 1 ? "\uD83E\uDD47"
                        : rank == 2 ? "\uD83E\uDD48" : rank == 3 ? "\uD83E\uDD49" : "#" + rank;
                Label rankLbl = new Label(medal);
                rankLbl.getStyleClass().add("lb-rank");

                VBox info = new VBox(2);
                Label nameLbl = new Label(d.petName + " (" + d.species + ")");
                nameLbl.getStyleClass().add("lb-name");
                Label details = new Label(d.owner + "  \u2022  Lv." + d.level + "  \u2022  \uD83E\uDE99" + d.coins
                        + "  \u2022  " + getAgeLabel(d.age));
                details.getStyleClass().add("lb-detail");
                info.getChildren().addAll(nameLbl, details);

                Region spacer2 = new Region();
                HBox.setHgrow(spacer2, Priority.ALWAYS);

                Button giftBtn = new Button("\uD83C\uDF81");
                giftBtn.setStyle(
                        "-fx-background-color: rgba(255,143,171,0.2); -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 16px; -fx-padding: 4 8;");
                Tooltip.install(giftBtn, new Tooltip("Kirim hadiah"));
                Button gbBtn = new Button("\uD83D\uDCDD");
                gbBtn.setStyle(
                        "-fx-background-color: rgba(137,207,240,0.2); -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 16px; -fx-padding: 4 8;");
                Tooltip.install(gbBtn, new Tooltip("Guestbook"));

                int finalPetId = d.id;
                String toOwner = d.owner;
                String toPetName = d.petName;

                giftBtn.setOnAction(e -> showOverlay("gift", () -> sendGift(toOwner, toPetName)));
                gbBtn.setOnAction(e -> showOverlay("guestbook", () -> showGuestbook(finalPetId, toPetName)));

                row.getChildren().addAll(rankLbl, info, spacer2, giftBtn, gbBtn);
                listBox.getChildren().add(row);
            }
        }

        Button refreshBtn = new Button("\uD83D\uDD04 Refresh");
        refreshBtn.setStyle(
                "-fx-background-color: rgba(196,144,228,0.2); -fx-background-radius: 10; -fx-cursor: hand; -fx-padding: 6 18; -fx-text-fill: #8B6BAA; -fx-font-size: 13px; -fx-border-color: rgba(196,144,228,0.3); -fx-border-radius: 10;");
        refreshBtn.setOnAction(e -> {
            closeCurrentOverlay();
            showLeaderboard();
        });

        card.getChildren().addAll(header, scroll, refreshBtn);
        pane.getChildren().add(card);
        centerPane.getChildren().add(pane);
        currentOverlay = pane;
        pane.setUserData("leaderboard");
    }

    private void sendGift(String toOwner, String toPetName) {
        if (pet == null)
            return;
        if (!db.isConnected()) {
            showToast("Koneksi database diperlukan untuk kirim hadiah!");
            return;
        }

        if (!db.canSendGiftToday(owner, toOwner, toPetName)) {
            showToast("Sudah kirim hadiah ke " + toPetName + " hari ini!");
            sound.play("sad");
            return;
        }

        beginOverlay(0.6);

        VBox card = new VBox(16);
        card.setManaged(false);
        card.resize(360, 280);
        card.getStyleClass().add("overlay-card");
        card.setMaxWidth(360);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(30));
        card.setLayoutX((overlayW - 360) / 2);
        card.setLayoutY((overlayH - 280) / 2);

        Label title = new Label("\uD83C\uDF81 Kirim Hadiah");
        title.getStyleClass().add("overlay-title");

        Label info = new Label("Kirim hadiah untuk " + toPetName + "!");
        info.setFont(Font.font("Segoe UI", 14));
        info.setTextFill(Color.web("#8B6BAA"));

        HBox btnBox = new HBox(16);
        btnBox.setAlignment(Pos.CENTER);

        Button snackBtn = new Button("\uD83C\uDF6C Snack (+5 Senang)");
        snackBtn.setStyle(
                "-fx-background-color: rgba(255,143,171,0.25); -fx-background-radius: 12; -fx-padding: 10 16; -fx-cursor: hand; -fx-border-color: #FF8FAB; -fx-border-radius: 12; -fx-text-fill: #4A3B5C; -fx-font-size: 13px;");

        Button vitaminBtn = new Button("\uD83D\uDC8A Vitamin (+5 Sehat)");
        vitaminBtn.setStyle(
                "-fx-background-color: rgba(119,221,119,0.25); -fx-background-radius: 12; -fx-padding: 10 16; -fx-cursor: hand; -fx-border-color: #77DD77; -fx-border-radius: 12; -fx-text-fill: #4A3B5C; -fx-font-size: 13px;");

        Button closeBtn = new Button("Batal");
        closeBtn.getStyleClass().add("overlay-close-btn");

        javafx.event.EventHandler<javafx.event.ActionEvent> sendHandler = e -> {
            String giftType = e.getSource() == snackBtn ? "snack" : "vitamin";
            db.sendGift(owner, toOwner, toPetName, giftType);
            closeCurrentOverlay();
            showToast("\u2705 Hadiah terkirim ke " + toPetName + "!");
            sound.play("chime");
        };

        snackBtn.setOnAction(sendHandler);
        vitaminBtn.setOnAction(sendHandler);
        closeBtn.setOnAction(e -> closeCurrentOverlay());

        btnBox.getChildren().addAll(snackBtn, vitaminBtn);
        card.getChildren().addAll(title, info, btnBox, closeBtn);
        centerPane.getChildren().add(card);
        card.applyCss();
        card.autosize();
        card.layout();
        currentOverlay = card;
        card.setUserData("gift");
    }

    /*
     * ================================================================
     * GUESTBOOK
     * ================================================================
     */

    private void showGuestbook(int petId, String petName) {
        beginOverlay(0.55);

        Pane pane = new Pane();
        pane.setManaged(false);
        pane.setPickOnBounds(false);
        pane.resize(overlayW, overlayH);

        VBox card = new VBox(12);
        card.setManaged(false);
        card.resize(460, 420);
        card.getStyleClass().add("overlay-card");
        card.setMaxWidth(460);
        card.setMaxHeight(420);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.setLayoutX((overlayW - 460) / 2);
        card.setLayoutY((overlayH - 420) / 2);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("\uD83D\uDCDD Guestbook - " + petName);
        title.getStyleClass().add("overlay-title");
        title.setStyle(title.getStyle() + "; -fx-font-size: 18px;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button closeBtn = new Button("Tutup");
        closeBtn.getStyleClass().add("overlay-close-btn");
        closeBtn.setOnAction(e -> closeCurrentOverlay());
        header.getChildren().addAll(title, sp, closeBtn);

        TextField msgField = new TextField();
        msgField.setPromptText("Tulis pesan untuk " + petName + "...");
        msgField.getStyleClass().add("name-field");

        Button sendBtn = new Button("\u2709\uFE0F Kirim");
        sendBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #C490E4, #89CFF0); -fx-text-fill: white; -fx-background-radius: 12; -fx-padding: 8 20; -fx-cursor: hand; -fx-font-weight: bold;");

        HBox inputRow = new HBox(8);
        inputRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(msgField, Priority.ALWAYS);
        inputRow.getChildren().addAll(msgField, sendBtn);

        VBox msgBox = new VBox(6);
        ScrollPane scroll = new ScrollPane(msgBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroll.setPrefHeight(240);

        // Load messages
        List<String[]> messages = db.isConnected() ? db.getGuestbook(petId) : new ArrayList<>();
        if (messages.isEmpty()) {
            Label emptyLbl = new Label("Belum ada pesan. Jadilah yang pertama!");
            emptyLbl.setFont(Font.font("Segoe UI", 13));
            emptyLbl.setTextFill(Color.web("#B8A0CC"));
            emptyLbl.setAlignment(Pos.CENTER);
            msgBox.getChildren().add(emptyLbl);
        } else {
            for (String[] msg : messages) {
                VBox entry = new VBox(2);
                entry.getStyleClass().add("gb-entry");
                Label visitor = new Label(msg[0]);
                visitor.getStyleClass().add("gb-visitor");
                Label text = new Label(msg[1]);
                text.getStyleClass().add("gb-message");
                text.setWrapText(true);
                Label date = new Label(msg[2]);
                date.getStyleClass().add("gb-date");
                entry.getChildren().addAll(visitor, text, date);
                msgBox.getChildren().add(entry);
            }
        }

        sendBtn.setOnAction(e -> {
            String msg = msgField.getText().trim();
            if (msg.isEmpty())
                return;
            if (db.isConnected()) {
                db.addGuestbookEntry(petId, owner, msg);
            }
            msgField.clear();
            closeCurrentOverlay();
            showToast("Pesan terkirim! \u2705");
            sound.play("chime");

            showGuestbook(petId, petName);
        });

        card.getChildren().addAll(header, inputRow, scroll);
        pane.getChildren().add(card);
        centerPane.getChildren().add(pane);
        currentOverlay = pane;
        pane.setUserData("guestbook");
    }

    /*
     * ================================================================
     * CLEANUP
     * ================================================================
     */

    @Override
    public void stop() {
        saveToDB();
        fileSave.save(petList);
        if (db != null)
            db.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
