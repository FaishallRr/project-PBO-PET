package pet;

import javafx.scene.layout.Pane;
import javafx.scene.shape.*;
import javafx.scene.paint.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.transform.Rotate;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.geometry.Point2D;

public class Pet2D extends Pane {

    private final String species;
    private double startTime = System.nanoTime();
    private AnimationTimer customAnimTimer;

    // Body parts
    private Ellipse body;
    private Ellipse head;
    
    // Eyes
    private Ellipse leftEye, rightEye;
    private Circle leftPupil, rightPupil;
    private Circle leftGlint1, rightGlint1;
    private Circle circleLeftGlint2, circleRightGlint2;

    // Face elements
    private Circle nose;
    private Path mouth;
    private Ellipse leftBlush, rightBlush;

    // Species specific
    private Path leftEar, rightEar;
    private Path tail;
    private Pane leftWing, rightWing;
    private Circle birdCrest1, birdCrest2;
    private Path birdBeakUpper, birdBeakLower;

    // Interaction states
    private double baseHeadY = 90;
    private double baseBodyY = 185;
    
    private Point2D leftEyeCenter = new Point2D(95, 95);
    private Point2D rightEyeCenter = new Point2D(165, 95);

    public Pet2D(String species) {
        this.species = species.toLowerCase();
        setPrefSize(260, 320);
        buildPet();
        startAnimations();
        setupClickHotspots();
    }

    private void buildPet() {
        // Shadow
        Ellipse shadow = new Ellipse(130, 290, 68, 15);
        shadow.setFill(Color.rgb(0, 0, 0, 0.12));
        getChildren().add(shadow);

        // Materials / Colors
        Color primaryColor, darkColor, accentColor, blushColor;
        switch (species) {
            case "kucing":
                primaryColor = Color.rgb(255, 185, 140);
                darkColor = Color.rgb(240, 165, 120);
                accentColor = Color.rgb(65, 175, 125); // emerald iris
                blushColor = Color.rgb(255, 120, 145, 0.7);
                break;
            case "anjing":
                primaryColor = Color.rgb(235, 160, 95);
                darkColor = Color.rgb(195, 125, 65);
                accentColor = Color.rgb(95, 60, 35);
                blushColor = Color.rgb(255, 140, 150, 0.6);
                break;
            case "burung":
                primaryColor = Color.rgb(255, 215, 55);
                darkColor = Color.rgb(255, 185, 45);
                accentColor = Color.rgb(25, 25, 25);
                blushColor = Color.rgb(255, 95, 120, 0.75);
                break;
            default:
                primaryColor = Color.LIGHTPINK;
                darkColor = Color.PINK;
                accentColor = Color.BLUE;
                blushColor = Color.rgb(255, 180, 200);
        }

        // DropShadow effect for clay style
        DropShadow clayShadow = new DropShadow(12, 0, 5, Color.rgb(0, 0, 0, 0.15));

        // 1. Tail (Behind Body)
        buildTail(primaryColor, darkColor);

        // 1.5 Legs (Behind Body)
        buildLegs(primaryColor, darkColor);

        // 2. Body
        body = new Ellipse(130, baseBodyY, 60, 75);
        body.setFill(new RadialGradient(180, 0.2, 130, baseBodyY - 15, 80, false, CycleMethod.NO_CYCLE,
                new Stop(0, primaryColor.interpolate(Color.WHITE, 0.2)),
                new Stop(1, darkColor)));
        body.setEffect(clayShadow);
        getChildren().add(body);

        // 3. Wings (Bird Only)
        if (species.equals("burung")) {
            buildWings(darkColor, primaryColor);
        }

        // 4. Ears/Crest Behind Head (Cat / Bird Only)
        if (species.equals("kucing")) {
            buildCatEars(darkColor, Color.rgb(255, 175, 190));
        } else if (species.equals("burung")) {
            buildBirdCrest(primaryColor);
        }

        // 5. Head
        head = new Ellipse(130, baseHeadY, 86, 68);
        head.setFill(new RadialGradient(180, 0.2, 130, baseHeadY - 15, 80, false, CycleMethod.NO_CYCLE,
                new Stop(0, primaryColor.interpolate(Color.WHITE, 0.2)),
                new Stop(1, darkColor)));
        head.setEffect(clayShadow);
        getChildren().add(head);

        // 5.5 Ears in Front of Head (Dog Only)
        if (species.equals("anjing")) {
            buildDogEars(darkColor);
        }

        // 6. Eyes
        leftEye = new Ellipse(leftEyeCenter.getX(), leftEyeCenter.getY(), 18, 21);
        leftEye.setFill(Color.WHITE);
        rightEye = new Ellipse(rightEyeCenter.getX(), rightEyeCenter.getY(), 18, 21);
        rightEye.setFill(Color.WHITE);

        leftPupil = new Circle(leftEyeCenter.getX(), leftEyeCenter.getY(), 12);
        leftPupil.setFill(accentColor);
        rightPupil = new Circle(rightEyeCenter.getX(), rightEyeCenter.getY(), 12);
        rightPupil.setFill(accentColor);

        leftGlint1 = new Circle(leftEyeCenter.getX() - 4, leftEyeCenter.getY() - 4, 4);
        leftGlint1.setFill(Color.WHITE);
        rightGlint1 = new Circle(rightEyeCenter.getX() - 4, rightEyeCenter.getY() - 4, 4);
        rightGlint1.setFill(Color.WHITE);

        circleLeftGlint2 = new Circle(leftEyeCenter.getX() + 4, leftEyeCenter.getY() + 4, 2);
        circleLeftGlint2.setFill(Color.WHITE);
        circleRightGlint2 = new Circle(rightEyeCenter.getX() + 4, rightEyeCenter.getY() + 4, 2);
        circleRightGlint2.setFill(Color.WHITE);

        getChildren().addAll(leftEye, rightEye, leftPupil, rightPupil, leftGlint1, rightGlint1, circleLeftGlint2, circleRightGlint2);

        // 7. Blush
        leftBlush = new Ellipse(leftEyeCenter.getX() - 15, leftEyeCenter.getY() + 23, 13, 8);
        leftBlush.setFill(blushColor);
        rightBlush = new Ellipse(rightEyeCenter.getX() + 15, rightEyeCenter.getY() + 23, 13, 8);
        rightBlush.setFill(blushColor);
        getChildren().addAll(leftBlush, rightBlush);

        // 8. Muzzle / Nose / Beak
        if (species.equals("anjing")) {
            // White cream Shiba muzzle
            Ellipse muzzle = new Ellipse(130, baseHeadY + 16, 24, 16);
            muzzle.setFill(Color.rgb(250, 243, 225));
            getChildren().add(muzzle);

            nose = new Circle(130, baseHeadY + 8, 6.0);
            nose.setFill(Color.rgb(40, 40, 40));
            getChildren().add(nose);
        } else if (species.equals("kucing")) {
            nose = new Circle(130, baseHeadY + 8, 4.5);
            nose.setFill(Color.rgb(255, 115, 145));
            getChildren().add(nose);
        } else if (species.equals("burung")) {
            // Beak
            birdBeakUpper = new Path();
            birdBeakUpper.getElements().addAll(
                new MoveTo(116, baseHeadY + 6),
                new LineTo(144, baseHeadY + 6),
                new LineTo(130, baseHeadY + 22),
                new ClosePath()
            );
            birdBeakUpper.setFill(Color.rgb(255, 140, 15));
            birdBeakUpper.setStroke(Color.rgb(230, 120, 10));

            birdBeakLower = new Path();
            birdBeakLower.getElements().addAll(
                new MoveTo(120, baseHeadY + 7),
                new LineTo(140, baseHeadY + 7),
                new LineTo(130, baseHeadY + 16),
                new ClosePath()
            );
            birdBeakLower.setFill(Color.rgb(230, 120, 10));
            getChildren().addAll(birdBeakLower, birdBeakUpper);
        }

        // 9. Mouth (for cat & dog)
        if (!species.equals("burung")) {
            mouth = new Path();
            mouth.getElements().addAll(
                new MoveTo(120, baseHeadY + 17),
                new QuadCurveTo(125, baseHeadY + 22, 130, baseHeadY + 17),
                new QuadCurveTo(135, baseHeadY + 22, 140, baseHeadY + 17)
            );
            mouth.setStroke(Color.rgb(120, 50, 60));
            mouth.setStrokeWidth(3.0);
            mouth.setFill(null);
            getChildren().add(mouth);
        }

        // 10. Whiskers (Cat only)
        if (species.equals("kucing")) {
            buildWhiskers();
        }

        // 11. Ribbon / Collar
        buildCollar();
    }

    private void buildLegs(Color primaryColor, Color darkColor) {
        DropShadow clayShadow = new DropShadow(10, 0, 5, Color.rgb(0, 0, 0, 0.15));
        double legY = baseBodyY + 52;
        
        Color pawColor;
        if (species.equals("kucing")) pawColor = Color.rgb(255, 170, 180);
        else if (species.equals("anjing")) pawColor = Color.rgb(250, 243, 225);
        else pawColor = Color.rgb(255, 165, 105);

        Ellipse leftLegShape = new Ellipse(100, legY, 16, 24);
        leftLegShape.setFill(darkColor);
        leftLegShape.setEffect(clayShadow);

        Ellipse rightLegShape = new Ellipse(160, legY, 16, 24);
        rightLegShape.setFill(darkColor);
        rightLegShape.setEffect(clayShadow);

        Ellipse leftPawShape = new Ellipse(100, legY + 20, 20, 10);
        leftPawShape.setFill(pawColor);
        leftPawShape.setEffect(clayShadow);

        Ellipse rightPawShape = new Ellipse(160, legY + 20, 20, 10);
        rightPawShape.setFill(pawColor);
        rightPawShape.setEffect(clayShadow);

        getChildren().addAll(leftLegShape, rightLegShape, leftPawShape, rightPawShape);
    }

    private void buildCatEars(Color furColor, Color pinkColor) {
        leftEar = new Path();
        leftEar.getElements().addAll(
            new MoveTo(60, baseHeadY - 45),
            new QuadCurveTo(32, baseHeadY - 88, 38, baseHeadY - 88),
            new QuadCurveTo(72, baseHeadY - 68, 80, baseHeadY - 52),
            new ClosePath()
        );
        leftEar.setFill(furColor);

        Path innerEarL = new Path();
        innerEarL.getElements().addAll(
            new MoveTo(61, baseHeadY - 49),
            new QuadCurveTo(42, baseHeadY - 78, 45, baseHeadY - 78),
            new QuadCurveTo(68, baseHeadY - 62, 73, baseHeadY - 52),
            new ClosePath()
        );
        innerEarL.setFill(pinkColor);

        rightEar = new Path();
        rightEar.getElements().addAll(
            new MoveTo(200, baseHeadY - 45),
            new QuadCurveTo(228, baseHeadY - 88, 222, baseHeadY - 88),
            new QuadCurveTo(188, baseHeadY - 68, 180, baseHeadY - 52),
            new ClosePath()
        );
        rightEar.setFill(furColor);

        Path innerEarR = new Path();
        innerEarR.getElements().addAll(
            new MoveTo(199, baseHeadY - 49),
            new QuadCurveTo(218, baseHeadY - 78, 215, baseHeadY - 78),
            new QuadCurveTo(192, baseHeadY - 62, 187, baseHeadY - 52),
            new ClosePath()
        );
        innerEarR.setFill(pinkColor);

        getChildren().addAll(leftEar, rightEar, innerEarL, innerEarR);
    }

    private void buildDogEars(Color furDarkColor) {
        leftEar = new Path();
        leftEar.getElements().addAll(
            new MoveTo(62, baseHeadY - 32),
            new QuadCurveTo(25, baseHeadY - 38, 28, baseHeadY + 8),
            new QuadCurveTo(45, baseHeadY + 28, 68, baseHeadY - 15),
            new ClosePath()
        );
        leftEar.setFill(furDarkColor);

        rightEar = new Path();
        rightEar.getElements().addAll(
            new MoveTo(198, baseHeadY - 32),
            new QuadCurveTo(235, baseHeadY - 38, 232, baseHeadY + 8),
            new QuadCurveTo(215, baseHeadY + 28, 192, baseHeadY - 15),
            new ClosePath()
        );
        rightEar.setFill(furDarkColor);

        getChildren().addAll(leftEar, rightEar);
    }

    private void buildBirdCrest(Color primaryColor) {
        birdCrest1 = new Circle(120, baseHeadY - 65, 11);
        birdCrest1.setFill(primaryColor);
        birdCrest2 = new Circle(140, baseHeadY - 65, 8);
        birdCrest2.setFill(primaryColor);
        getChildren().addAll(birdCrest1, birdCrest2);
    }

    private void buildTail(Color furColor, Color darkColor) {
        tail = new Path();
        if (species.equals("kucing")) {
            tail.getElements().addAll(
                new MoveTo(185, baseBodyY + 40),
                new QuadCurveTo(240, baseBodyY + 40, 250, baseBodyY - 15),
                new QuadCurveTo(256, baseBodyY - 55, 242, baseBodyY - 55),
                new QuadCurveTo(236, baseBodyY - 28, 226, baseBodyY + 12),
                new QuadCurveTo(210, baseBodyY + 25, 185, baseBodyY + 46),
                new ClosePath()
            );
            tail.setFill(furColor);
        } else if (species.equals("anjing")) {
            tail.getElements().addAll(
                new MoveTo(185, baseBodyY + 28),
                new QuadCurveTo(230, baseBodyY + 20, 242, baseBodyY - 20),
                new QuadCurveTo(230, baseBodyY - 6, 185, baseBodyY + 40),
                new ClosePath()
            );
            tail.setFill(darkColor);
        } else {
            // Bird tail feathers
            tail.getElements().addAll(
                new MoveTo(116, baseBodyY + 60),
                new LineTo(102, baseBodyY + 95),
                new LineTo(130, baseBodyY + 75),
                new LineTo(158, baseBodyY + 95),
                new LineTo(144, baseBodyY + 60),
                new ClosePath()
            );
            tail.setFill(darkColor);
        }
        getChildren().add(tail);
    }

    private void buildWings(Color darkColor, Color primaryColor) {
        leftWing = new Pane();
        Ellipse wL1 = new Ellipse(0, 0, 18, 40);
        wL1.setFill(darkColor);
        wL1.setRotate(-25);
        Ellipse wL2 = new Ellipse(-8, 10, 13, 32);
        wL2.setFill(primaryColor);
        wL2.setRotate(-20);
        leftWing.getChildren().addAll(wL1, wL2);
        leftWing.setLayoutX(65);
        leftWing.setLayoutY(baseBodyY - 6);

        rightWing = new Pane();
        Ellipse wR1 = new Ellipse(0, 0, 18, 40);
        wR1.setFill(darkColor);
        wR1.setRotate(25);
        Ellipse wR2 = new Ellipse(8, 10, 13, 32);
        wR2.setFill(primaryColor);
        wR2.setRotate(20);
        rightWing.getChildren().addAll(wR1, wR2);
        rightWing.setLayoutX(195);
        rightWing.setLayoutY(baseBodyY - 6);

        getChildren().addAll(leftWing, rightWing);
    }

    private void buildWhiskers() {
        for (int i = 0; i < 3; i++) {
            Line wl = new Line(95, baseHeadY + 12 + i * 4, 60, baseHeadY + 10 + i * 7);
            wl.setStroke(Color.rgb(240, 240, 240, 0.8));
            wl.setStrokeWidth(2.0);
            Line wr = new Line(165, baseHeadY + 12 + i * 4, 200, baseHeadY + 10 + i * 7);
            wr.setStroke(Color.rgb(240, 240, 240, 0.8));
            wr.setStrokeWidth(2.0);
            getChildren().addAll(wl, wr);
        }
    }

    private void buildCollar() {
        if (species.equals("kucing")) {
            // Pink ribbon collar
            Rectangle collar = new Rectangle(98, baseHeadY + 48, 64, 7);
            collar.setArcWidth(5);
            collar.setArcHeight(5);
            collar.setFill(Color.rgb(255, 95, 155));

            Circle center = new Circle(130, baseHeadY + 51, 7);
            center.setFill(Color.rgb(255, 95, 155));

            Ellipse leftBow = new Ellipse(120, baseHeadY + 51, 11, 7);
            leftBow.setFill(Color.rgb(255, 95, 155));
            leftBow.setRotate(-15);

            Ellipse rightBow = new Ellipse(140, baseHeadY + 51, 11, 7);
            rightBow.setFill(Color.rgb(255, 95, 155));
            rightBow.setRotate(15);

            getChildren().addAll(collar, leftBow, rightBow, center);
        } else if (species.equals("anjing")) {
            // Red collar with gold bell
            Rectangle collar = new Rectangle(96, baseHeadY + 48, 68, 8);
            collar.setArcWidth(5);
            collar.setArcHeight(5);
            collar.setFill(Color.rgb(215, 35, 35));

            Circle bell = new Circle(130, baseHeadY + 55, 7.5);
            bell.setFill(Color.rgb(255, 215, 0));
            bell.setStroke(Color.rgb(210, 165, 0));
            bell.setStrokeWidth(1.2);

            getChildren().addAll(collar, bell);
        }
    }

    /* ================================================================
     *  EYE TRACKING
     * ================================================================ */
    public void lookAt(double mx, double my) {
        double lx = leftEyeCenter.getX();
        double ly = leftEyeCenter.getY();
        double rx = rightEyeCenter.getX();
        double ry = rightEyeCenter.getY();

        Point2D mousePos = new Point2D(mx, my);

        Point2D lOffset = calculateEyeOffset(lx, ly, mousePos);
        Point2D rOffset = calculateEyeOffset(rx, ry, mousePos);

        leftPupil.setTranslateX(lOffset.getX());
        leftPupil.setTranslateY(lOffset.getY());
        leftGlint1.setTranslateX(lOffset.getX());
        leftGlint1.setTranslateY(lOffset.getY());
        circleLeftGlint2.setTranslateX(lOffset.getX());
        circleLeftGlint2.setTranslateY(lOffset.getY());

        rightPupil.setTranslateX(rOffset.getX());
        rightPupil.setTranslateY(rOffset.getY());
        rightGlint1.setTranslateX(rOffset.getX());
        rightGlint1.setTranslateY(rOffset.getY());
        circleRightGlint2.setTranslateX(rOffset.getX());
        circleRightGlint2.setTranslateY(rOffset.getY());
    }

    private Point2D calculateEyeOffset(double eyeX, double eyeY, Point2D mousePos) {
        double absoluteEyeX = getLayoutX() + eyeX;
        double absoluteEyeY = getLayoutY() + eyeY;

        double dx = mousePos.getX() - absoluteEyeX;
        double dy = mousePos.getY() - absoluteEyeY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance == 0) return new Point2D(0, 0);

        double maxOffset = 5.5;
        double offsetScale = Math.min(maxOffset, distance * 0.025);

        return new Point2D((dx / distance) * offsetScale, (dy / distance) * offsetScale);
    }

    /* ================================================================
     *  ANIMATIONS
     * ================================================================ */
    private void startAnimations() {
        customAnimTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double t = (now - startTime) / 1_000_000_000.0;

                // 1. Organic Breathing
                double breath = 1.0 + 0.025 * Math.sin(t * 3.5);
                body.setScaleY(breath);
                body.setScaleX(1.0 + 0.01 * Math.cos(t * 3.5));
                
                double headBreath = 1.0 + 0.01 * Math.sin(t * 3.5 + 0.4);
                head.setScaleX(headBreath);
                head.setScaleY(headBreath);

                // 2. Tail Waving (sine wave)
                if (tail != null) {
                    double tailWag = 15.0 * Math.sin(t * 6.5);
                    tail.setRotate(tailWag);
                    tail.setRotationAxis(Rotate.Z_AXIS);
                }

                // 3. Wing Flapping (Bird only)
                if (species.equals("burung")) {
                    if (leftWing != null && rightWing != null) {
                        double wingAngle = 18.0 * Math.sin(t * 8.0);
                        leftWing.setRotate(-wingAngle);
                        rightWing.setRotate(wingAngle);
                    }
                }
            }
        };
        customAnimTimer.start();
    }

    /* ================================================================
     *  INTERACTION HOTSPOTS
     * ================================================================ */
    private void setupClickHotspots() {
        // Head Click -> Wiggle, blush, bounce
        head.setOnMouseClicked(e -> {
            SoundManager.getInstance().play("happy");
            ScaleTransition blushAnim = new ScaleTransition(Duration.millis(300), leftBlush);
            blushAnim.setToX(1.6);
            blushAnim.setToY(1.3);
            blushAnim.setAutoReverse(true);
            blushAnim.setCycleCount(2);
            blushAnim.play();

            ScaleTransition blushAnimR = new ScaleTransition(Duration.millis(300), rightBlush);
            blushAnimR.setToX(1.6);
            blushAnimR.setToY(1.3);
            blushAnimR.setAutoReverse(true);
            blushAnimR.setCycleCount(2);
            blushAnimR.play();

            ScaleTransition headPress = new ScaleTransition(Duration.millis(150), head);
            headPress.setToX(1.12);
            headPress.setToY(0.78);
            headPress.setAutoReverse(true);
            headPress.setCycleCount(2);
            headPress.play();
        });

        // Body Click -> Tickle shake
        body.setOnMouseClicked(e -> {
            SoundManager.getInstance().play("click");
            TranslateTransition tickle = new TranslateTransition(Duration.millis(80), this);
            tickle.setByX(12);
            tickle.setAutoReverse(true);
            tickle.setCycleCount(6);
            tickle.play();
        });

        // Tail Click -> Frenzy tail wag
        if (tail != null) {
            tail.setOnMouseClicked(e -> {
                SoundManager.getInstance().play("happy");
                Timeline tailFrenzy = new Timeline();
                for (int i = 0; i < 10; i++) {
                    tailFrenzy.getKeyFrames().add(new KeyFrame(Duration.millis(i * 100), 
                        new KeyValue(tail.rotateProperty(), (i % 2 == 0 ? 35 : -15))
                    ));
                }
                tailFrenzy.play();
            });
        }
    }

    /* ================================================================
     *  EXPRESSIONS
     * ================================================================ */
    public void setExpression(String expr) {
        // Reset default eyes
        leftEye.setScaleY(1.0); leftEye.setScaleX(1.0);
        rightEye.setScaleY(1.0); rightEye.setScaleX(1.0);
        leftBlush.setScaleX(1.0); leftBlush.setScaleY(1.0);
        rightBlush.setScaleX(1.0); rightBlush.setScaleY(1.0);

        if (mouth != null) {
            mouth.setScaleX(1.0);
            mouth.setScaleY(1.0);
        }

        switch (expr.toLowerCase()) {
            case "happy":
                leftEye.setScaleY(0.4);
                rightEye.setScaleY(0.4);
                leftBlush.setScaleX(1.4);
                leftBlush.setScaleY(1.2);
                rightBlush.setScaleX(1.4);
                rightBlush.setScaleY(1.2);
                if (mouth != null) {
                    mouth.setScaleX(1.5);
                    mouth.setScaleY(0.5);
                }
                break;
            case "sad":
                leftEye.setScaleY(0.65);
                rightEye.setScaleY(0.65);
                if (mouth != null) {
                    mouth.setScaleX(0.85);
                    mouth.setScaleY(-1.2); // invert curve for sad frown
                }
                break;
            case "sleepy":
                leftEye.setScaleY(0.1);
                rightEye.setScaleY(0.1);
                if (mouth != null) {
                    mouth.setScaleX(0.5);
                    mouth.setScaleY(0.5);
                }
                break;
            default: // normal
                break;
        }
    }

    public void animateAction(String action) {
        switch (action.toLowerCase()) {
            case "feed":
            case "play":
            case "vitamin":
                ScaleTransition jump = new ScaleTransition(Duration.millis(250), this);
                jump.setToY(0.75);
                jump.setToX(1.18);
                jump.setAutoReverse(true);
                jump.setCycleCount(2);
                
                TranslateTransition hop = new TranslateTransition(Duration.millis(250), this);
                hop.setByY(-50);
                hop.setAutoReverse(true);
                hop.setCycleCount(2);

                jump.play();
                hop.play();
                break;

            case "bath":
                RotateTransition spin = new RotateTransition(Duration.millis(400), this);
                spin.setByAngle(18);
                spin.setAutoReverse(true);
                spin.setCycleCount(4);
                spin.play();
                break;

            case "sleep":
                RotateTransition tilt = new RotateTransition(Duration.millis(600), this);
                tilt.setToAngle(15);
                tilt.play();
                break;
        }
    }

    public void stopIdle() {
        if (customAnimTimer != null) customAnimTimer.stop();
    }

    public void restartIdle() {
        if (customAnimTimer != null) customAnimTimer.start();
        setRotate(0);
    }
}
