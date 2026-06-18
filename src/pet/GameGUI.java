package pet;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.animation.*;
import javafx.util.Duration;
import pet.DatabaseManager.PetSaveData;
import java.util.Random;

import java.sql.*;

public class GameGUI extends Application {

    private static final int W = 1050, H = 750;

    private Pet pet;
    private Pet2D pet2D;
    private Pet3D pet3D;
    private boolean use3D = false;
    private SubScene subScene3D;
    private PerspectiveCamera camera3D;
    private DatabaseManager db;
    private SoundManager sound;
    private boolean soundEnabled = true;

    private int petId = -1, level = 1, totalFeeds = 0, totalPlays = 0;
    private int currentFoodType = 0;
    private boolean petSick = false;
    private boolean soundAvailable = true;
    private double mx, my;

    private Label nameLabel, speciesLabel, levelLabel, speechLabel;
    private Label hungerVal, happinessVal, energyVal, healthVal;
    private Rectangle hungerFill, happinessFill, energyFill, healthFill;
    private Button soundBtn;
    private boolean sleeping = false;

    private Timeline gameLoop, saveTimer, speechFadeTimer;
    private int tickCount = 0;
    private int speechCooldownTicks = 0;

    private Pane centerPane;
    private Stage stage;
    private VBox toastContainer;

    @Override
    public void start(Stage stage) {
        db = DatabaseManager.getInstance();
        sound = SoundManager.getInstance();
        soundAvailable = sound.hasSounds();
        if (!soundAvailable) System.out.println("[Game] Sound folder kosong, efek suara nonaktif");

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

        this.stage = stage;
        Scene scene = new Scene(root, W, H);
        scene.getStylesheets().add("file:styles.css");
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case DIGIT1: doAction("feed"); break;
                case DIGIT2: doAction("play"); break;
                case DIGIT3: doAction("bath"); break;
                case DIGIT4: doAction("vitamin"); break;
                case DIGIT5: doAction("sleep"); break;
            }
        });

        if (db.isConnected()) {
            loadFromDB();
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
        HBox bar = new HBox(10);
        bar.getStyleClass().add("top-bar");
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("\uD83D\uDC3E Simulasi Pet 2D Modern");
        title.getStyleClass().add("title-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        nameLabel = new Label("--");
        nameLabel.getStyleClass().add("pet-name-label");

        speciesLabel = new Label("");
        speciesLabel.getStyleClass().add("species-label");

        levelLabel = new Label("Lv.1");
        levelLabel.getStyleClass().add("level-badge");

        soundBtn = new Button("\uD83D\uDD0A");
        soundBtn.getStyleClass().addAll("top-icon-btn", "sound-btn");
        soundBtn.setOnAction(e -> toggleSound());
        if (!soundAvailable) {
            soundBtn.setText("\uD83D\uDD07");
            soundBtn.setStyle(soundBtn.getStyle() + ";-fx-opacity:0.5;");
        }

        Button viewBtn = new Button("\uD83D\uDD3D");
        viewBtn.getStyleClass().addAll("top-icon-btn", "view-btn");
        viewBtn.setOnAction(e -> toggleView());

        Button saveBtn = new Button("\uD83D\uDCBE");
        saveBtn.getStyleClass().addAll("top-icon-btn", "save-btn");
        saveBtn.setOnAction(e -> saveToDB());

        Button closeBtn = new Button("\u2716");
        closeBtn.getStyleClass().addAll("top-icon-btn", "close-btn");
        closeBtn.setOnAction(e -> {
            saveToDB();
            stage.close();
        });

        bar.getChildren().addAll(title, spacer, nameLabel, speciesLabel,
            levelLabel, soundBtn, saveBtn, viewBtn, closeBtn);
        return bar;
    }

    private Node buildCenter() {
        centerPane = new Pane();
        centerPane.setMinSize(0, 0);
        centerPane.setPrefSize(W - 40, 360);

        // Core eye tracking: look at the mouse location
        centerPane.setOnMouseMoved(e -> {
            if (sleeping) return;
            if (use3D && pet3D != null) {
                pet3D.lookAt(e.getX(), e.getY(), centerPane.getWidth(), centerPane.getHeight());
            } else if (pet2D != null) {
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

        // Ensure UI overlays always render on top of pet
        speechLabel.toFront();
        toastContainer.toFront();

        addBackgroundDecorations();

        return centerPane;
    }

    private void addBackgroundDecorations() {
        String[] emojis = {"\u2601\uFE0F", "\u2B50", "\uD83D\uDCAB", "\uD83C\uDF38", "\uD83D\uDC95", "\u2728"};
        java.util.Random rng = new java.util.Random();
        for (int i = 0; i < 8; i++) {
            Label deco = new Label(emojis[rng.nextInt(emojis.length)]);
            deco.setStyle("-fx-font-size: " + (16 + rng.nextInt(18)) + "px; -fx-opacity: " + (0.15 + rng.nextDouble() * 0.2) + ";");
            deco.setManaged(false);
            deco.setMouseTransparent(true);
            double startX = rng.nextDouble() * 900;
            double startY = rng.nextDouble() * 400;
            deco.setLayoutX(startX);
            deco.setLayoutY(startY);

            Timeline floatAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(deco.layoutYProperty(), startY)),
                new KeyFrame(Duration.seconds(8 + rng.nextDouble() * 10),
                    new KeyValue(deco.layoutYProperty(), startY - 100 - rng.nextDouble() * 200, Interpolator.EASE_BOTH))
            );
            floatAnim.setCycleCount(Animation.INDEFINITE);
            floatAnim.setAutoReverse(true);
            floatAnim.play();

            Timeline swayAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(deco.layoutXProperty(), startX)),
                new KeyFrame(Duration.seconds(6 + rng.nextDouble() * 8),
                    new KeyValue(deco.layoutXProperty(), startX + 30 - rng.nextDouble() * 60, Interpolator.EASE_BOTH))
            );
            swayAnim.setCycleCount(Animation.INDEFINITE);
            swayAnim.setAutoReverse(true);
            swayAnim.play();

            centerPane.getChildren().add(0, deco);
        }
    }

    private Node buildBottom() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(10, 20, 15, 20));
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
            statusGroup("\u2764\uFE0F", "Sehat", "#77DD77", healthFill, healthVal)
        );

        HBox btnBox = new HBox(16);
        btnBox.setAlignment(Pos.CENTER);
        btnBox.setPadding(new Insets(4, 0, 4, 0));

        btnBox.getChildren().addAll(
            makeActionBtn("\uD83C\uDF56", "Makan", "#FFB347", () -> doAction("feed")),
            makeActionBtn("\u26BD", "Bermain", "#FF8FAB", () -> doAction("play")),
            makeActionBtn("\uD83D\uDEC1", "Mandi", "#89CFF0", () -> doAction("bath")),
            makeActionBtn("\uD83D\uDC8A", "Vitamin", "#77DD77", () -> doAction("vitamin")),
            makeActionBtn("\uD83D\uDE34", "Tidur", "#C490E4", () -> doAction("sleep"))
        );

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
            case "Makan": return "1";
            case "Bermain": return "2";
            case "Mandi": return "3";
            case "Vitamin": return "4";
            case "Tidur": return "5";
            default: return "";
        }
    }

    private String tooltipText(String label) {
        switch (label) {
            case "Makan": return "Beri pet makanan [" + shortcutKey(label) + "]";
            case "Bermain": return "Ajak pet bermain [" + shortcutKey(label) + "]";
            case "Mandi": return "Mandikan pet [" + shortcutKey(label) + "]";
            case "Vitamin": return "Beri vitamin [" + shortcutKey(label) + "]";
            case "Tidur": return "Tidurkan / bangunkan [" + shortcutKey(label) + "]";
            default: return "";
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
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + color + "90; -fx-border-color: " + color + ";"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "40; -fx-border-color: " + color + "70;"));

        Label lbl = new Label("[" + shortcutKey(labelText) + "] " + labelText);
        lbl.getStyleClass().add("action-label");

        VBox box = new VBox(4);
        box.setAlignment(Pos.CENTER);
        box.getChildren().addAll(btn, lbl);
        return box;
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
                    new KeyValue(toast.translateYProperty(), 20, Interpolator.EASE_BOTH))
            );
            anim.setOnFinished(e -> toastContainer.getChildren().remove(toast));
            anim.play();
        });
    }

    /* ================================================================
     *  FEEDING (direct)
     * ================================================================ */
    /* Feeding now handled directly via doAction("feed") with food cycling */

    private void doAction(String action) {
        if (pet == null || (pet2D == null && pet3D == null)) return;

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
                if (pet.getHunger() < 20) {
                    showSpeech("Aduh perutku kenyang banget! \uD83D\uDE2D");
                    showToast("Perut penuh! Tunggu lapar dulu.");
                    break;
                }
                Food selectedFood;
                String foodName;
                switch (currentFoodType) {
                    case 0: selectedFood = new DryFood("Makanan Kering"); foodName = "\uD83C\uDF5A Makanan Kering"; break;
                    case 1: selectedFood = new WetFood("Makanan Basah"); foodName = "\uD83E\uDD5B Makanan Basah"; break;
                    default: selectedFood = new Treat("Snack"); foodName = "\uD83C\uDF6C Snack"; break;
                }
                currentFoodType = (currentFoodType + 1) % 3;
                showToast(foodName + " \u2714\uFE0F");
                pet.feed(selectedFood);
                totalFeeds++;
                showSpeech("Nyam nyam enak! \uD83D\uDE0B");
                sound.play("eat");
                if (use3D && pet3D != null) {
                    pet3D.setExpression("happy");
                    pet3D.animateAction("feed");
                } else if (pet2D != null) {
                    pet2D.setExpression("happy");
                    pet2D.animateAction("feed");
                }
                new Thread(() -> {
                    try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
                    Platform.runLater(() -> {
                        if (use3D && pet3D != null) pet3D.setExpression("normal");
                        else if (pet2D != null) pet2D.setExpression("normal");
                    });
                }).start();
                break;

            case "play":
                int energyBefore = pet.getEnergy();
                pet.play();
                if (pet.getEnergy() == energyBefore) {
                    showSpeech("Capek... istirahat dulu ya! \uD83D\uDE34");
                    showToast("Energi terlalu rendah untuk bermain.");
                    return;
                }
                totalPlays++;
                showSpeech("Yeay seru banget! \u2728");
                sound.play("happy");
                if (use3D && pet3D != null) {
                    pet3D.setExpression("happy");
                    pet3D.animateAction("play");
                } else if (pet2D != null) {
                    pet2D.setExpression("happy");
                    pet2D.animateAction("play");
                }
                new Thread(() -> {
                    try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                    Platform.runLater(() -> {
                        if (use3D && pet3D != null) pet3D.setExpression("normal");
                        else if (pet2D != null) pet2D.setExpression("normal");
                    });
                }).start();
                break;

            case "bath":
                if (pet instanceof Careable) {
                    ((Careable) pet).groom();
                    sound.play("water");
                    if (use3D && pet3D != null) {
                        pet3D.setExpression("happy");
                        pet3D.animateAction("bath");
                    } else if (pet2D != null) {
                        pet2D.setExpression("happy");
                        pet2D.animateAction("bath");
                    }
                    showSpeech("Segar setelah mandi! \uD83D\uDEC1");
                    showToast("Sudah dimandikan. Senang bertambah!");
                }
                break;

            case "vitamin":
                if (pet instanceof Careable) {
                    ((Careable) pet).giveVitamin();
                    sound.play("chime");
                    if (use3D && pet3D != null) {
                        pet3D.setExpression("happy");
                        pet3D.animateAction("vitamin");
                    } else if (pet2D != null) {
                        pet2D.setExpression("happy");
                        pet2D.animateAction("vitamin");
                    }
                    petSick = false;
                    showSpeech("Sehat dan kuat! \u2728");
                    showToast("Vitamin diberikan. Sehat bertambah!");
                }
                break;

            case "sleep":
                if (sleeping) {
                    showSpeech("Sudah bangun! Selamat pagi! \uD83D\uDE0A");
                    sleeping = false;
                    pet.setEnergy(Math.min(100, pet.getEnergy() + 10));
                    if (use3D && pet3D != null) {
                        pet3D.setExpression("normal");
                        pet3D.restartIdle();
                    } else if (pet2D != null) {
                        pet2D.setExpression("normal");
                        pet2D.restartIdle();
                    }
                } else {
                    pet.sleep();
                    sleeping = true;
                    if (use3D && pet3D != null) {
                        pet3D.setExpression("sleepy");
                        pet3D.stopIdle();
                        pet3D.animateAction("sleep");
                    } else if (pet2D != null) {
                        pet2D.setExpression("sleepy");
                        pet2D.stopIdle();
                        pet2D.animateAction("sleep");
                    }
                    sound.play("snore");
                    showSpeech("Selamat tidur... Zzz \uD83D\uDE34");
                    showToast("Pet tidur nyenyak. Energi bertambah!");
                    spawnFloatingIndicator("\uD83D\uDCA4 Zzz\uD83D\uDCA4", centerPane.getWidth() / 2 - 60, centerPane.getHeight() / 2 - 100, Color.web("#C490E4"));
                }
                break;
        }

        updateStatus();
        saveToDB();

        int diffHunger = pet.getHunger() - oldHunger;
        int diffHappiness = pet.getHappiness() - oldHappiness;
        int diffEnergy = pet.getEnergy() - oldEnergy;
        int diffHealth = pet.getHealth() - oldHealth;

        java.util.List<FloatingInfo> indicators = new java.util.ArrayList<>();
        if (diffHunger != 0) {
            indicators.add(new FloatingInfo((diffHunger < 0 ? "": "+") + diffHunger + " Lapar", Color.web("#FFB347")));
        }
        if (diffHappiness != 0) {
            indicators.add(new FloatingInfo((diffHappiness > 0 ? "+" : "") + diffHappiness + " Senang", Color.web("#FF8FAB")));
        }
        if (diffEnergy != 0) {
            indicators.add(new FloatingInfo((diffEnergy > 0 ? "+" : "") + diffEnergy + " Energi", Color.web("#89CFF0")));
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
                    new KeyValue(label.opacityProperty(), 1.0)
                ),
                new KeyFrame(Duration.seconds(1.5),
                    new KeyValue(label.layoutYProperty(), startY - 80, Interpolator.EASE_OUT),
                    new KeyValue(label.opacityProperty(), 0.0, Interpolator.EASE_IN)
                )
            );
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
        double paneW = centerPane.getWidth();
        if (paneW < 100) paneW = 800;
        double labelW = speechLabel.prefWidth(-1);
        if (labelW < 0) labelW = 300;
        speechLabel.setTranslateX(Math.max(10, (paneW - labelW) / 2));
        speechLabel.setTranslateY(20);
        speechCooldownTicks = 3;

        speechFadeTimer = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(speechLabel.opacityProperty(), 1)),
            new KeyFrame(Duration.seconds(2.5),
                new KeyValue(speechLabel.opacityProperty(), 0, Interpolator.EASE_BOTH))
        );
        speechFadeTimer.setOnFinished(e -> { speechLabel.setVisible(false); speechLabel.setOpacity(1); });
        speechFadeTimer.play();
    }

    private void updateStatus() {
        if (pet == null) return;
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
        if (inverted) val = 100 - val;
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
                    new KeyValue(fill.widthProperty(), targetW, Interpolator.EASE_BOTH))
            );
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

    private void createPet(String name, String species) {
        switch (species.toLowerCase()) {
            case "kucing": pet = new Cat(name); break;
            case "anjing": pet = new Dog(name); break;
            case "burung": pet = new Bird(name); break;
            default: pet = new Cat(name);
        }

        nameLabel.setText(pet.getName());
        speciesLabel.setText(pet.getSpecies());
        levelLabel.setText("Lv." + level);
        updateStatus();
        stage.setTitle("\uD83D\uDC3E " + pet.getName() + " (" + pet.getSpecies() + ")");
        showToast("Pet baru lahir! Halo " + pet.getName() + "! \uD83C\uDF89");
        showSpeech("Hai! Namaku " + pet.getName() + "! \uD83D\uDE0A");
        sound.playSpeciesSound(species);

        build2DPet(species);
        saveToDB();
    }

    private void build2DPet(String species) {
        // Remove 3D scene if exists
        if (subScene3D != null) {
            centerPane.getChildren().remove(subScene3D);
            subScene3D = null;
        }
        if (pet3D != null) {
            pet3D.stopIdle();
            pet3D = null;
        }
        use3D = false;

        if (pet2D != null) {
            centerPane.getChildren().remove(pet2D);
        }
        pet2D = new Pet2D(species);
        
        // Center position with dynamic resize support
        Runnable centerPet2D = () -> {
            double pw = pet2D.prefWidth(-1);
            double ph = pet2D.prefHeight(-1);
            pet2D.setLayoutX((centerPane.getWidth() - pw) / 2);
            pet2D.setLayoutY((centerPane.getHeight() - ph) / 2 + 20);
        };
        centerPet2D.run();
        centerPane.widthProperty().addListener((obs, o, n) -> centerPet2D.run());
        centerPane.heightProperty().addListener((obs, o, h) -> centerPet2D.run());
        
        // Pet click → play species sound
        pet2D.setOnMouseClicked(ev -> {
            if (sleeping) return;
            pet.makeSound();
            sound.playSpeciesSound(pet.getSpecies());
        });

        // Petting physics (Stroking mouse-drag over pet)
        pet2D.setOnMouseDragged(ev -> {
            if (sleeping) return;
            // Spawn hearts, play happy sounds, increase happiness
            if (Math.random() < 0.12) {
                spawnFloatingIndicator("\uD83D\uDC95 Sayang!", ev.getSceneX(), ev.getSceneY() - 35, Color.web("#FF5E7E"));
                sound.play("happy");
                pet.setHappiness(Math.min(100, pet.getHappiness() + 3));
                updateStatus();
            }
        });

        // Petting for 3D mode via centerPane (not when rotating 3D model)
        centerPane.setOnMouseDragged(ev -> {
            if (sleeping || !use3D || ev.getTarget() == subScene3D) return;
            if (Math.random() < 0.12) {
                spawnFloatingIndicator("\uD83D\uDC95 Sayang!", ev.getSceneX(), ev.getSceneY() - 35, Color.web("#FF5E7E"));
                sound.play("happy");
                pet.setHappiness(Math.min(100, pet.getHappiness() + 3));
                updateStatus();
            }
        });

        // 3D mode click reaction (only when clicking the 3D scene, not overlays)
        centerPane.setOnMouseClicked(ev -> {
            if (sleeping || !use3D || pet3D == null) return;
            if (ev.getTarget() != subScene3D) return;
            pet3D.setExpression("happy");
            if (Math.random() < 0.3) {
                spawnFloatingIndicator("\u2728", ev.getSceneX(), ev.getSceneY() - 30, Color.web("#C490E4"));
            }
        });
        
        centerPane.getChildren().add(pet2D);
        speechLabel.toFront();
        toastContainer.toFront();
    }

    private void toggleView() {
        if (use3D) {
            // Switch to 2D
            use3D = false;
            if (subScene3D != null) {
                centerPane.getChildren().remove(subScene3D);
                subScene3D = null;
            }
            if (pet2D != null) {
                pet2D.setVisible(true);
                if (pet3D != null) {
                    pet3D.stopIdle();
                }
            }
            showToast("Mode 2D");
            if (pet != null) stage.setTitle("\uD83D\uDC3E " + pet.getName() + " (2D)");
        } else {
            // Switch to 3D
            use3D = true;
            if (pet2D != null) {
                pet2D.setVisible(false);
                pet2D.stopIdle();
            }
            if (pet3D == null && pet != null) {
                build3DPet(pet.getSpecies());
            }
            if (pet3D != null && subScene3D == null) {
                build3DScene();
            }
            if (subScene3D != null) {
                subScene3D.setVisible(true);
                centerPane.getChildren().add(subScene3D);
                pet3D.restartIdle();
            }
            showToast("Mode 3D");
            if (pet != null) stage.setTitle("\uD83D\uDC3E " + pet.getName() + " (3D)");
            speechLabel.toFront();
            toastContainer.toFront();
        }
    }

    private void build3DPet(String species) {
        pet3D = new Pet3D(species);
    }

    private void build3DScene() {
        if (pet3D == null) return;

        camera3D = new PerspectiveCamera(true);
        camera3D.setNearClip(0.1);
        camera3D.setFarClip(1000);
        camera3D.setTranslateZ(-350);
        camera3D.setTranslateY(-10);

        Group root3D = pet3D.getRoot();
        root3D.setScaleX(1.8);
        root3D.setScaleY(1.8);
        root3D.setScaleZ(1.8);

        // Lighting
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(-100);
        light.setTranslateY(-100);
        light.setTranslateZ(-200);
        root3D.getChildren().add(light);

        PointLight light2 = new PointLight(Color.rgb(255, 200, 200));
        light2.setTranslateX(100);
        light2.setTranslateY(100);
        light2.setTranslateZ(-200);
        root3D.getChildren().add(light2);

        subScene3D = new SubScene(root3D, centerPane.getWidth(), centerPane.getHeight(), true, SceneAntialiasing.BALANCED);
        subScene3D.setCamera(camera3D);
        subScene3D.setFill(Color.TRANSPARENT);

        centerPane.widthProperty().addListener((obs, o, n) -> {
            if (subScene3D != null) subScene3D.setWidth(n.doubleValue());
        });
        centerPane.heightProperty().addListener((obs, o, h) -> {
            if (subScene3D != null) subScene3D.setHeight(h.doubleValue());
        });

        // Drag to rotate in 3D mode
        subScene3D.setOnMouseDragged(ev -> {
            if (pet3D != null) {
                pet3D.getYRotate().setAngle(pet3D.getYRotate().getAngle() - ev.getX() * 0.3);
                pet3D.getXRotate().setAngle(Math.max(-20, Math.min(20, pet3D.getXRotate().getAngle() + ev.getY() * 0.2)));
            }
        });
    }

    private void loadFromDB() {
        PetSaveData data = db.loadLatestPet();
        if (data != null && data.petName != null) {
            switch (data.species.toLowerCase()) {
                case "kucing": pet = new Cat(data.petName); break;
                case "anjing": pet = new Dog(data.petName); break;
                case "burung": pet = new Bird(data.petName); break;
                default: pet = new Cat(data.petName);
            }
            pet.setHunger(data.hunger);
            pet.setHappiness(data.happiness);
            pet.setEnergy(data.energy);
            pet.setHealth(data.health);
            petId = data.id;
            level = data.level;
            totalFeeds = data.totalFeeds;
            totalPlays = data.totalPlays;

            nameLabel.setText(pet.getName());
            speciesLabel.setText(pet.getSpecies());
            levelLabel.setText("Lv." + level);
            updateStatus();
            stage.setTitle("\uD83D\uDC3E " + pet.getName() + " (2D)");
            showToast("Selamat datang kembali, " + pet.getName() + "!");
            showToast("Level " + level + " | Sudah makan " + totalFeeds + " kali, bermain " + totalPlays + " kali");

            build2DPet(data.species);
            showSpeech("Aku kangen! \u2728");
        } else {
            showCreateScreen();
        }
    }

    private void showCreateScreen() {
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(40));
        form.setMaxWidth(400);
        form.getStyleClass().add("create-form");

        Label welcome = new Label("\u2728 SELAMAT DATANG! \u2728");
        welcome.setFont(Font.font("Segoe UI", FontWeight.BOLD, 30));
        welcome.setTextFill(Color.web("#C490E4"));
        welcome.setEffect(new DropShadow(15, Color.rgb(196, 144, 228, 0.6)));

        Label sub = new Label("Pilih dan beri nama pet virtualmu!");
        sub.setFont(Font.font("Segoe UI", 15));
        sub.setTextFill(Color.web("#8B6BAA"));

        TextField nameField = new TextField();
        nameField.setPromptText("Ketik nama pet kamu...");
        nameField.getStyleClass().add("name-field");

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
            if (name.isEmpty()) { nameField.setPromptText("Nama dulu dong! \uD83D\uDE0A"); return; }
            if (name.length() > 50) { nameField.setText(name.substring(0, 50)); showToast("Nama dipotong ke 50 karakter"); }
            String species = ((ToggleButton) speciesGroup.getSelectedToggle()).getUserData().toString();
            centerPane.getChildren().remove(form);
            createPet(name, species);
        });

        form.getChildren().addAll(welcome, sub, nameField, speciesBox, createBtn);
        form.setTranslateX((centerPane.getWidth() - 400) / 2);
        form.setTranslateY(30);

        centerPane.widthProperty().addListener((obs, o, n) ->
            form.setTranslateX((n.doubleValue() - 400) / 2));

        centerPane.getChildren().add(form);
    }

    private ToggleButton speciesBtn(String text, String name, ToggleGroup group) {
        ToggleButton btn = new ToggleButton(text);
        btn.setUserData(name);
        btn.setToggleGroup(group);
        btn.getStyleClass().add("species-btn");
        btn.selectedProperty().addListener((obs, o, n) -> {
            if (n) btn.getStyleClass().add("selected");
            else btn.getStyleClass().remove("selected");
        });
        return btn;
    }

    private void saveToDB() {
        if (pet == null || !db.isConnected()) return;
        try {
            if (petId < 0) {
                String sql = "INSERT INTO pet_save (pet_name, species, hunger, happiness, energy, health, level, total_feeds, total_plays) VALUES (?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, pet.getName());
                    ps.setString(2, pet.getSpecies());
                    ps.setInt(3, pet.getHunger());
                    ps.setInt(4, pet.getHappiness());
                    ps.setInt(5, pet.getEnergy());
                    ps.setInt(6, pet.getHealth());
                    ps.setInt(7, level);
                    ps.setInt(8, totalFeeds);
                    ps.setInt(9, totalPlays);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) petId = rs.getInt(1);
                    }
                }
            } else {
                String sql = "UPDATE pet_save SET hunger=?, happiness=?, energy=?, health=?, level=?, total_feeds=?, total_plays=?, updated_at=CURRENT_TIMESTAMP WHERE id=?";
                try (PreparedStatement ps = db.getConnection().prepareStatement(sql)) {
                    ps.setInt(1, pet.getHunger());
                    ps.setInt(2, pet.getHappiness());
                    ps.setInt(3, pet.getEnergy());
                    ps.setInt(4, pet.getHealth());
                    ps.setInt(5, level);
                    ps.setInt(6, totalFeeds);
                    ps.setInt(7, totalPlays);
                    ps.setInt(8, petId);
                    ps.executeUpdate();
                }
            }
            showToast("Tersimpan! \uD83D\uDCBE");
        } catch (SQLException e) {
            showToast("Gagal menyimpan: " + e.getMessage());
        }
    }

    private void startGameLoop() {
        gameLoop = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
            if (pet == null) return;
            tickCount++;

            // Every 8 ticks (16 seconds), let stats decay via timePasses
            if (tickCount % 8 == 0 && !sleeping) {
                pet.timePasses();
            }

            // If sleeping, regain energy automatically
            if (sleeping) {
                if (tickCount % 4 == 0) {
                    pet.setEnergy(Math.min(100, pet.getEnergy() + 8));
                    pet.setHunger(Math.min(100, pet.getHunger() + 2)); // hunger slowly decays (hunger increases)
                    updateStatus();
                }
            }

            if (!sleeping) {
                if (speechCooldownTicks > 0) speechCooldownTicks--;
                if (pet.getHealth() <= 0) {
                    if (speechCooldownTicks == 0) showSpeech("Sakit... tolong aku! \uD83D\uDE22");
                    if (use3D && pet3D != null) pet3D.setExpression("sad");
                    else if (pet2D != null) pet2D.setExpression("sad");
                } else if (pet.getHunger() >= 80) {
                    if (speechCooldownTicks == 0) showSpeech("Laper... minta makan dong! \uD83D\uDE22");
                    if (use3D && pet3D != null) pet3D.setExpression("sad");
                    else if (pet2D != null) pet2D.setExpression("sad");
                } else if (pet.getHappiness() <= 25) {
                    if (speechCooldownTicks == 0) showSpeech("Bosan... ajak main dong! \uD83D\uDE1E");
                    if (use3D && pet3D != null) pet3D.setExpression("sad");
                    else if (pet2D != null) pet2D.setExpression("sad");
                } else {
                    if (use3D && pet3D != null) pet3D.setExpression("normal");
                    else if (pet2D != null) pet2D.setExpression("normal");
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
                    spawnFloatingIndicator("\u2B50 Level Up!", centerPane.getWidth() / 2, centerPane.getHeight() / 2 - 60, Color.web("#C490E4"));
                }
                levelLabel.setText("Lv." + level);
            }

            updateStatus();

            if (pet.getHealth() <= 0) {
                showToast("\u26A0\uFE0F Kesehatan pet sangat rendah!");
            }
        }));
        gameLoop.setCycleCount(Animation.INDEFINITE);
        gameLoop.play();
    }

    private void startAutoSave() {
        saveTimer = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            if (pet != null) saveToDB();
        }));
        saveTimer.setCycleCount(Animation.INDEFINITE);
        saveTimer.play();
    }

    @Override
    public void stop() {
        saveToDB();
        if (db != null) db.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}







