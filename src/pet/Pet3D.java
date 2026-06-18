package pet;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.transform.Rotate;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.animation.*;
import javafx.util.Duration;

/**
 * Super Kawaii / Chibi-style 3D pet built entirely from JavaFX 3-D primitives.
 * High-end visual overhaul with claymation shading, smooth curves, organic transitions,
 * and no clipping eye layers.
 */
public class Pet3D {

    /* â”€â”€ scene graph roots â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private final Group root = new Group();
    private final Group characterGroup = new Group();
    private final Rotate yRotate = new Rotate(0, Rotate.Y_AXIS);
    private final Rotate xRotate = new Rotate(0, Rotate.X_AXIS);
    private final Group shadowGroup = new Group();

    /* â”€â”€ body parts â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private Sphere head, body;
    private Group leftEarGroup, rightEarGroup;
    private Sphere leftEye, rightEye;
    private Sphere leftIris, rightIris;
    private Sphere leftPupil, rightPupil;
    private Sphere leftGlint1, rightGlint1;   // main glint
    private Sphere leftGlint2, rightGlint2;   // secondary glint
    private Sphere nose, leftBlush, rightBlush;
    private MeshView mouth;
    private Cylinder leftLeg, rightLeg;
    private Sphere leftPaw, rightPaw;
    private Node tail;
    private Group leftWing, rightWing;

    /* â”€â”€ materials â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private PhongMaterial furMat, furDarkMat, eyeMat, pupilMat;
    private PhongMaterial noseMat, blushMat, pawMat, irisMat;

    /* â”€â”€ state â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private final String species;
    private Timeline idleFloat, idleSway, blinkTimer;
    private AnimationTimer customAnimTimer;

    /* â”€â”€ proportions (chibi/kawaii) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private static final double HR = 55;   // head radius â€” big!
    private static final double BR = 30;   // body radius â€” round!

    /* ================================================================
     *  CONSTRUCTOR
     * ================================================================ */
    public Pet3D(String species) {
        this.species = species;
        buildPet();
        startIdleAnimations();
    }

    /* ================================================================
     *  BUILD
     * ================================================================ */
    private void buildPet() {
        characterGroup.getTransforms().addAll(yRotate, xRotate);
        createMaterials();
        createBody();
        createHead();
        createEyes();
        createNose();
        createMouth();
        createBlush();
        createLegs();
        createFloorShadow();
        addSpeciesFeatures();
        characterGroup.setTranslateY(25);
        root.getChildren().addAll(shadowGroup, characterGroup);
    }

    /* â”€â”€ materials â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private void createMaterials() {
        furMat = new PhongMaterial();
        furMat.setSpecularPower(40);
        furMat.setSpecularColor(Color.WHITE.deriveColor(0, 1, 1, 0.15));

        furDarkMat = new PhongMaterial();
        furDarkMat.setSpecularPower(30);
        furDarkMat.setSpecularColor(Color.WHITE.deriveColor(0, 1, 1, 0.12));

        eyeMat = new PhongMaterial(Color.WHITE);
        eyeMat.setSpecularPower(50);

        pupilMat = new PhongMaterial(Color.rgb(15, 15, 18));
        pupilMat.setSpecularPower(60);

        noseMat = new PhongMaterial();
        noseMat.setSpecularPower(45);

        blushMat = new PhongMaterial(Color.rgb(255, 120, 145, 0.7));
        blushMat.setSpecularPower(10);

        pawMat = new PhongMaterial(Color.rgb(255, 180, 190));
        pawMat.setSpecularPower(25);

        irisMat = new PhongMaterial();
        irisMat.setSpecularPower(45);
    }

    /* â”€â”€ body (pear-shaped & overlapping head to remove stiff neck gap) â”€â”€ */
    private void createBody() {
        body = new Sphere(BR);
        // Overlap head slightly for modern chibi look (no visible neck)
        body.setTranslateY(HR + BR - 10); 
        body.setScaleX(1.15);
        body.setScaleY(1.25);
        body.setScaleZ(1.05);
        body.setMaterial(furMat);
        characterGroup.getChildren().add(body);
    }

    /* â”€â”€ head (tembam/chibi: scaleX=1.15, scaleY=0.92) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private void createHead() {
        head = new Sphere(HR);
        head.setScaleX(1.15);
        head.setScaleY(0.92);
        head.setMaterial(furMat);
        characterGroup.getChildren().add(head);
    }

    /* â”€â”€ eyes (mathematical stacking to prevent clipping/submerging) â”€â”€ */
    private void createEyes() {
        double ey = 0;          // lowered position for maximum cuteness
        double ez = HR - 4;     // base Z coordinate
        double eo = 23;         // widened to the sides

        // white sclera
        leftEye = new Sphere(13);
        leftEye.setScaleX(1.15);
        leftEye.setScaleY(1.15);
        leftEye.setTranslateX(-eo); leftEye.setTranslateY(ey); leftEye.setTranslateZ(ez);
        leftEye.setMaterial(eyeMat);

        rightEye = new Sphere(13);
        rightEye.setScaleX(1.15);
        rightEye.setScaleY(1.15);
        rightEye.setTranslateX(eo); rightEye.setTranslateY(ey); rightEye.setTranslateZ(ez);
        rightEye.setMaterial(eyeMat);

        // colored iris (stacked +2.5 in front of sclera)
        leftIris = new Sphere(9.5);
        leftIris.setScaleY(1.05);
        leftIris.setTranslateX(-eo); leftIris.setTranslateY(ey); leftIris.setTranslateZ(ez + 2.5);
        leftIris.setMaterial(irisMat);

        rightIris = new Sphere(9.5);
        rightIris.setScaleY(1.05);
        rightIris.setTranslateX(eo); rightIris.setTranslateY(ey); rightIris.setTranslateZ(ez + 2.5);
        rightIris.setMaterial(irisMat);

        // pupils (stacked +5.0 in front of sclera)
        leftPupil = new Sphere(6);
        leftPupil.setScaleY(1.05);
        leftPupil.setTranslateX(-eo); leftPupil.setTranslateY(ey); leftPupil.setTranslateZ(ez + 5.0);
        leftPupil.setMaterial(pupilMat);

        rightPupil = new Sphere(6);
        rightPupil.setScaleY(1.05);
        rightPupil.setTranslateX(eo); rightPupil.setTranslateY(ey); rightPupil.setTranslateZ(ez + 5.0);
        rightPupil.setMaterial(pupilMat);

        // main glint (big, stacked +7.5 in front of sclera)
        PhongMaterial glintMat = new PhongMaterial(Color.WHITE);
        glintMat.setSpecularPower(80);
        
        leftGlint1 = new Sphere(2.8);
        leftGlint1.setTranslateX(-eo - 3); leftGlint1.setTranslateY(ey - 3); leftGlint1.setTranslateZ(ez + 7.5);
        leftGlint1.setMaterial(glintMat);

        rightGlint1 = new Sphere(2.8);
        rightGlint1.setTranslateX(eo - 3); rightGlint1.setTranslateY(ey - 3); rightGlint1.setTranslateZ(ez + 7.5);
        rightGlint1.setMaterial(glintMat);

        // secondary glint (small, stacked +7.5 in front of sclera)
        leftGlint2 = new Sphere(1.5);
        leftGlint2.setTranslateX(-eo + 2.5); leftGlint2.setTranslateY(ey + 2); leftGlint2.setTranslateZ(ez + 7.5);
        leftGlint2.setMaterial(glintMat);

        rightGlint2 = new Sphere(1.5);
        rightGlint2.setTranslateX(eo + 2.5); rightGlint2.setTranslateY(ey + 2); rightGlint2.setTranslateZ(ez + 7.5);
        rightGlint2.setMaterial(glintMat);

        characterGroup.getChildren().addAll(
            leftEye, rightEye,
            leftIris, rightIris,
            leftPupil, rightPupil,
            leftGlint1, rightGlint1,
            leftGlint2, rightGlint2
        );
    }

    /* â”€â”€ nose â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private void createNose() {
        nose = new Sphere(3.5);
        nose.setTranslateY(8);
        nose.setTranslateZ(HR - 1);
        nose.setMaterial(noseMat);
        characterGroup.getChildren().add(nose);
    }

    /* â”€â”€ mouth â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private void createMouth() {
        TriangleMesh mesh = new TriangleMesh();
        float z = (float)(HR - 1);
        float[] pts = {
            -7f,  9f,  z,        // 0  left corner
            -3.5f, 6.5f,  z,        // 1  left peak
             0f,  8.5f,  z,        // 2  center dip
             3.5f, 6.5f,  z,        // 3  right peak
             7f,  9f,  z,        // 4  right corner
            -5f, 12f, z - 0.5f, // 5  bottom-left
             0f,  11f, z - 0.5f,  // 6  bottom-center
             5f, 12f, z - 0.5f  // 7  bottom-right
        };
        float[] uv = {
            0f,0f,  0.25f,0f,  0.5f,0f,  0.75f,0f,  1f,0f,
            0f,1f,  0.5f,1f,   1f,1f
        };
        int[] faces = {
            0,0, 1,1, 5,5,
            1,1, 2,2, 6,6,   1,1, 6,6, 5,5,
            2,2, 3,3, 6,6,   3,3, 7,7, 6,6,
            3,3, 4,4, 7,7
        };
        mesh.getPoints().addAll(pts);
        mesh.getTexCoords().addAll(uv);
        mesh.getFaces().addAll(faces);

        mouth = new MeshView(mesh);
        mouth.setMaterial(new PhongMaterial(Color.rgb(205, 90, 105)));
        characterGroup.getChildren().add(mouth);
    }

    /* â”€â”€ blush (positioned to fit wide cheeks) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private void createBlush() {
        double by = 10, bz = HR - 4, bo = 30;
        leftBlush = new Sphere(7);
        leftBlush.setScaleX(1.3);
        leftBlush.setScaleY(0.8);
        leftBlush.setScaleZ(0.8);
        leftBlush.setTranslateX(-bo); leftBlush.setTranslateY(by); leftBlush.setTranslateZ(bz);
        leftBlush.setMaterial(blushMat);

        rightBlush = new Sphere(7);
        rightBlush.setScaleX(1.3);
        rightBlush.setScaleY(0.8);
        rightBlush.setScaleZ(0.8);
        rightBlush.setTranslateX(bo); rightBlush.setTranslateY(by); rightBlush.setTranslateZ(bz);
        rightBlush.setMaterial(blushMat);

        characterGroup.getChildren().addAll(leftBlush, rightBlush);
    }

    /* â”€â”€ legs & paws (adjusted to new body Y coordinate) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private void createLegs() {
        double ly = HR + BR - 10 + 15; // align to base of body
        double lr = 6.0, lh = 9.0, lo = 13.0;

        leftLeg = new Cylinder(lr, lh);
        leftLeg.setTranslateX(-lo); leftLeg.setTranslateY(ly);
        leftLeg.setMaterial(furMat);

        rightLeg = new Cylinder(lr, lh);
        rightLeg.setTranslateX(lo); rightLeg.setTranslateY(ly);
        rightLeg.setMaterial(furMat);

        leftPaw = new Sphere(6.5);
        leftPaw.setTranslateX(-lo); leftPaw.setTranslateY(ly + lh / 2 + 2);
        leftPaw.setScaleY(0.45);
        leftPaw.setMaterial(pawMat);

        rightPaw = new Sphere(6.5);
        rightPaw.setTranslateX(lo); rightPaw.setTranslateY(ly + lh / 2 + 2);
        rightPaw.setScaleY(0.45);
        rightPaw.setMaterial(pawMat);

        characterGroup.getChildren().addAll(leftLeg, rightLeg, leftPaw, rightPaw);
    }

    /* â”€â”€ floor shadow â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private void createFloorShadow() {
        Circle shadow = new Circle(65, Color.rgb(0, 0, 0, 0.15));
        shadow.setTranslateY(HR + BR + 20);
        shadow.setScaleX(1.4);
        shadowGroup.getChildren().add(shadow);
    }

    /* ================================================================
     *  SPECIES FEATURES
     * ================================================================ */
    private void addSpeciesFeatures() {
        switch (species.toLowerCase()) {
            case "kucing": buildCat(); break;
            case "anjing": buildDog(); break;
            case "burung": buildBird(); break;
        }
    }

    /* â”€â”€ triangular ear helper â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private MeshView makeTriEar(double x, double y, double w, double h, Color color) {
        TriangleMesh mesh = new TriangleMesh();
        float hw = (float) w / 2;
        mesh.getPoints().addAll(
            0, (float) -h, 0,
            -hw, 0, 2,
            hw, 0, 2,
            0, 0, -2
        );
        mesh.getTexCoords().addAll(0, 0, 1, 0, 0.5f, 1);
        mesh.getFaces().addAll(
            0, 0, 1, 1, 3, 2,
            0, 0, 3, 2, 2, 1,
            1, 0, 0, 1, 3, 2,
            2, 0, 0, 1, 3, 2
        );
        MeshView ear = new MeshView(mesh);
        ear.setMaterial(new PhongMaterial(color));
        ear.setTranslateX(x);
        ear.setTranslateY(y);
        return ear;
    }

    /* â”€â”€ KUCING (cat) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private void buildCat() {
        furMat.setDiffuseColor(Color.rgb(255, 185, 140)); // pastel peach
        furMat.setSpecularColor(Color.rgb(255, 225, 205));
        furDarkMat.setDiffuseColor(Color.rgb(255, 205, 175));
        irisMat.setDiffuseColor(Color.rgb(65, 175, 125)); // emerald green
        noseMat.setDiffuseColor(Color.rgb(255, 115, 145));
        pawMat.setDiffuseColor(Color.rgb(255, 170, 180));

        // triangular ears
        leftEarGroup = new Group();
        leftEarGroup.getChildren().add(makeTriEar(-24, -50, 26, 32, Color.rgb(255, 205, 175)));
        leftEarGroup.setRotate(-12);
        leftEarGroup.setRotationAxis(Rotate.Z_AXIS);

        rightEarGroup = new Group();
        rightEarGroup.getChildren().add(makeTriEar(24, -50, 26, 32, Color.rgb(255, 205, 175)));
        rightEarGroup.setRotate(12);
        rightEarGroup.setRotationAxis(Rotate.Z_AXIS);

        characterGroup.getChildren().addAll(leftEarGroup, rightEarGroup);

        // inner ear pink
        MeshView innerL = makeTriEar(-24, -48, 16, 20, Color.rgb(255, 175, 190));
        innerL.setRotate(-12);
        innerL.setRotationAxis(Rotate.Z_AXIS);

        MeshView innerR = makeTriEar(24, -48, 16, 20, Color.rgb(255, 175, 190));
        innerR.setRotate(12);
        innerR.setRotationAxis(Rotate.Z_AXIS);

        characterGroup.getChildren().addAll(innerL, innerR);

        // whiskers (3 per side)
        PhongMaterial whiskerMat = new PhongMaterial(Color.rgb(240, 240, 240, 0.8));
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 3; i++) {
                Cylinder w = new Cylinder(0.6, 22);
                w.setTranslateX(side * 30);
                w.setTranslateY(6 + i * 3);
                w.setTranslateZ(HR - 6);
                w.setRotate(side * (10 + i * 6));
                w.setRotationAxis(Rotate.Z_AXIS);
                w.setMaterial(whiskerMat);
                characterGroup.getChildren().add(w);
            }
        }

        // tail
        Cylinder t = new Cylinder(3.5, 35);
        t.setTranslateX(-6);
        t.setTranslateY(HR + BR + 5);
        t.setTranslateZ(-BR - 3);
        t.setRotate(45);
        t.setRotationAxis(Rotate.X_AXIS);
        t.setMaterial(furMat);
        tail = t;
        characterGroup.getChildren().add(tail);

        // Pink ribbon / collar at neck
        PhongMaterial pinkRibbonMat = new PhongMaterial(Color.rgb(255, 95, 155));
        pinkRibbonMat.setSpecularColor(Color.rgb(255, 180, 200));

        Cylinder ribbonCollar = new Cylinder(32, 5);
        ribbonCollar.setTranslateY(46);
        ribbonCollar.setTranslateZ(0);
        ribbonCollar.setMaterial(pinkRibbonMat);

        Sphere bowCenter = new Sphere(5.5);
        bowCenter.setTranslateX(0);
        bowCenter.setTranslateY(46);
        bowCenter.setTranslateZ(32);
        bowCenter.setMaterial(pinkRibbonMat);

        Sphere bowLeft = new Sphere(7.5);
        bowLeft.setTranslateX(-10);
        bowLeft.setTranslateY(46);
        bowLeft.setTranslateZ(30);
        bowLeft.setScaleX(1.6);
        bowLeft.setScaleY(0.9);
        bowLeft.setScaleZ(0.7);
        bowLeft.setMaterial(pinkRibbonMat);

        Sphere bowRight = new Sphere(7.5);
        bowRight.setTranslateX(10);
        bowRight.setTranslateY(46);
        bowRight.setTranslateZ(30);
        bowRight.setScaleX(1.6);
        bowRight.setScaleY(0.9);
        bowRight.setScaleZ(0.7);
        bowRight.setMaterial(pinkRibbonMat);

        characterGroup.getChildren().addAll(ribbonCollar, bowCenter, bowLeft, bowRight);
    }

    /* â”€â”€ ANJING (dog) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private void buildDog() {
        furMat.setDiffuseColor(Color.rgb(235, 160, 95)); // gold Shiba Inu
        furMat.setSpecularColor(Color.rgb(255, 205, 160));
        furDarkMat.setDiffuseColor(Color.rgb(195, 125, 65));
        irisMat.setDiffuseColor(Color.rgb(95, 60, 35));
        noseMat.setDiffuseColor(Color.rgb(35, 35, 35));
        pawMat.setDiffuseColor(Color.rgb(215, 155, 160));

        // Shiba cream white muzzle
        PhongMaterial creamMat = new PhongMaterial(Color.rgb(250, 243, 225));
        Sphere muzzle = new Sphere(18);
        muzzle.setScaleX(1.45);
        muzzle.setScaleY(1.0);
        muzzle.setScaleZ(0.85);
        muzzle.setTranslateX(0);
        muzzle.setTranslateY(12);
        muzzle.setTranslateZ(HR - 13);
        muzzle.setMaterial(creamMat);
        characterGroup.getChildren().add(muzzle);

        // bigger nose for dog, sitting on muzzle
        nose.setRadius(5.0);
        nose.setTranslateY(7);
        nose.setTranslateZ(HR);

        // floppy ears
        leftEarGroup = new Group();
        Sphere le = new Sphere(13);
        le.setScaleY(1.9); le.setScaleZ(0.5);
        le.setMaterial(furDarkMat);
        le.setTranslateY(8);
        leftEarGroup.getChildren().add(le);
        leftEarGroup.setTranslateX(-27); leftEarGroup.setTranslateY(-42);
        leftEarGroup.setRotate(25); leftEarGroup.setRotationAxis(Rotate.X_AXIS);

        rightEarGroup = new Group();
        Sphere re = new Sphere(13);
        re.setScaleY(1.9); re.setScaleZ(0.5);
        re.setMaterial(furDarkMat);
        re.setTranslateY(8);
        rightEarGroup.getChildren().add(re);
        rightEarGroup.setTranslateX(27); rightEarGroup.setTranslateY(-42);
        rightEarGroup.setRotate(25); rightEarGroup.setRotationAxis(Rotate.X_AXIS);

        characterGroup.getChildren().addAll(leftEarGroup, rightEarGroup);

        // tongue
        Sphere tongue = new Sphere(4.5);
        tongue.setScaleY(1.7);
        tongue.setTranslateY(14);
        tongue.setTranslateZ(HR - 1);
        tongue.setMaterial(new PhongMaterial(Color.rgb(255, 125, 150)));
        characterGroup.getChildren().add(tongue);

        // Shiba style cream white eyebrows (cute dots)
        for (int side = -1; side <= 1; side += 2) {
            Sphere browDot = new Sphere(5);
            browDot.setScaleX(1.3);
            browDot.setScaleY(1.0);
            browDot.setScaleZ(0.6);
            browDot.setTranslateX(side * 14);
            browDot.setTranslateY(-18);
            browDot.setTranslateZ(HR - 5);
            browDot.setMaterial(creamMat);
            characterGroup.getChildren().add(browDot);
        }

        // Red collar with gold bell
        PhongMaterial collarMat = new PhongMaterial(Color.rgb(215, 35, 35));
        collarMat.setSpecularColor(Color.rgb(255, 90, 90));

        Cylinder collar = new Cylinder(32, 5);
        collar.setTranslateY(46);
        collar.setTranslateZ(0);
        collar.setMaterial(collarMat);

        PhongMaterial bellMat = new PhongMaterial(Color.rgb(255, 215, 0));
        bellMat.setSpecularColor(Color.WHITE);
        bellMat.setSpecularPower(45);

        Sphere bell = new Sphere(6.0);
        bell.setTranslateX(0);
        bell.setTranslateY(47);
        bell.setTranslateZ(32);
        bell.setMaterial(bellMat);

        Cylinder bellRing = new Cylinder(1.5, 3);
        bellRing.setTranslateX(0);
        bellRing.setTranslateY(44);
        bellRing.setTranslateZ(31);
        bellRing.setRotate(90);
        bellRing.setRotationAxis(Rotate.X_AXIS);
        bellRing.setMaterial(bellMat);

        characterGroup.getChildren().addAll(collar, bell, bellRing);

        // tail
        Cylinder t = new Cylinder(5, 26);
        t.setTranslateX(7);
        t.setTranslateY(HR + BR - 4);
        t.setTranslateZ(-BR + 1);
        t.setRotate(25);
        t.setRotationAxis(Rotate.X_AXIS);
        t.setMaterial(furMat);
        tail = t;
        characterGroup.getChildren().add(tail);

        // Adjust mouth position to sit perfectly on muzzle
        mouth.setTranslateZ(6);
    }

    /* â”€â”€ BURUNG (bird) â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private void buildBird() {
        furMat.setDiffuseColor(Color.rgb(255, 215, 55)); // warm yellow
        furMat.setSpecularColor(Color.rgb(255, 245, 160));
        furDarkMat.setDiffuseColor(Color.rgb(255, 185, 45));
        irisMat.setDiffuseColor(Color.rgb(25, 25, 25));
        noseMat.setDiffuseColor(Color.rgb(255, 140, 20));
        pawMat.setDiffuseColor(Color.rgb(255, 165, 105));

        // Remove/hide default nose
        nose.setRadius(0.1);

        // 3D double-cone beak
        Color beakColor = Color.rgb(255, 140, 15);
        MeshView upperBeak = makeBeakPart(17, 9, 16, beakColor, true);
        upperBeak.setTranslateY(10);
        upperBeak.setTranslateZ(HR - 2);

        MeshView lowerBeak = makeBeakPart(13, 6, 12, Color.rgb(230, 120, 10), false);
        lowerBeak.setTranslateY(10.5);
        lowerBeak.setTranslateZ(HR - 2);

        characterGroup.getChildren().addAll(upperBeak, lowerBeak);

        // Make blush pop out as prominent 3D spheres
        leftBlush.setRadius(9.5);
        leftBlush.setMaterial(new PhongMaterial(Color.rgb(255, 95, 120)));
        leftBlush.setScaleX(1.25); leftBlush.setScaleY(0.95); leftBlush.setScaleZ(0.85);
        leftBlush.setTranslateX(-27); leftBlush.setTranslateY(10); leftBlush.setTranslateZ(HR - 11);

        rightBlush.setRadius(9.5);
        rightBlush.setMaterial(new PhongMaterial(Color.rgb(255, 95, 120)));
        rightBlush.setScaleX(1.25); rightBlush.setScaleY(0.95); rightBlush.setScaleZ(0.85);
        rightBlush.setTranslateX(27); rightBlush.setTranslateY(10); rightBlush.setTranslateZ(HR - 11);

        // crest (3 spheres on top of head)
        for (int i = 0; i < 3; i++) {
            Sphere cr = new Sphere(5 - i);
            cr.setTranslateY(-HR * 0.92 + i * 3);
            cr.setTranslateX(-3 + i * 3);
            cr.setTranslateZ(HR - 5);
            cr.setMaterial(furMat);
            characterGroup.getChildren().add(cr);
        }

        // Layered flat-ellipsoid wings
        leftWing = new Group();
        rightWing = new Group();

        for (int i = 0; i < 3; i++) {
            // Left wing layer
            Sphere lwLayer = new Sphere(14 - i * 1.5);
            lwLayer.setScaleX(0.18);
            lwLayer.setScaleY(0.95);
            lwLayer.setScaleZ(2.1 - i * 0.3);
            lwLayer.setTranslateX(i * 1.5);
            lwLayer.setTranslateY(i * 3);
            lwLayer.setTranslateZ(-i * 4);
            lwLayer.setRotate(-i * 12);
            lwLayer.setRotationAxis(Rotate.Y_AXIS);
            lwLayer.setMaterial(i % 2 == 0 ? furDarkMat : furMat);
            leftWing.getChildren().add(lwLayer);

            // Right wing layer
            Sphere rwLayer = new Sphere(14 - i * 1.5);
            rwLayer.setScaleX(0.18);
            rwLayer.setScaleY(0.95);
            rwLayer.setScaleZ(2.1 - i * 0.3);
            rwLayer.setTranslateX(-i * 1.5);
            rwLayer.setTranslateY(i * 3);
            rwLayer.setTranslateZ(-i * 4);
            rwLayer.setRotate(i * 12);
            rwLayer.setRotationAxis(Rotate.Y_AXIS);
            rwLayer.setMaterial(i % 2 == 0 ? furDarkMat : furMat);
            rightWing.getChildren().add(rwLayer);
        }

        leftWing.setTranslateX(-BR - 6);
        leftWing.setTranslateY(HR + BR - 10 - 2);
        leftWing.setTranslateZ(0);

        rightWing.setTranslateX(BR + 6);
        rightWing.setTranslateY(HR + BR - 10 - 2);
        rightWing.setTranslateZ(0);

        characterGroup.getChildren().addAll(leftWing, rightWing);

        // tail feathers
        Cylinder t1 = new Cylinder(2.5, 22);
        t1.setTranslateY(HR + BR - 5);
        t1.setTranslateZ(-10);
        t1.setRotate(35); t1.setRotationAxis(Rotate.X_AXIS);
        t1.setMaterial(furMat);

        Cylinder t2 = new Cylinder(2, 18);
        t2.setTranslateY(HR + BR - 7);
        t2.setTranslateZ(-14);
        t2.setRotate(45); t2.setRotationAxis(Rotate.X_AXIS);
        t2.setMaterial(furDarkMat);

        characterGroup.getChildren().addAll(t1, t2);
    }

    /* â”€â”€ Helper to build 3D cone-like beak parts â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private MeshView makeBeakPart(double width, double height, double length, Color color, boolean upper) {
        TriangleMesh mesh = new TriangleMesh();
        float w = (float) width / 2;
        float h = (float) height;
        float l = (float) length;

        float yTip = upper ? -1f : 1f;
        float yBase = upper ? h : -h;

        mesh.getPoints().addAll(
            0f, yTip, l,          // 0: tip
            -w, 0f, 0f,           // 1: left
            w, 0f, 0f,            // 2: right
            0f, yBase, 0f         // 3: back/base
        );

        mesh.getTexCoords().addAll(0.5f, 0f, 0f, 1f, 1f, 1f, 0.5f, 0.5f);

        mesh.getFaces().addAll(
            0,0, 1,1, 2,2,
            0,0, 2,1, 3,2,
            0,0, 3,1, 1,2,
            1,0, 3,1, 2,2
        );

        MeshView mv = new MeshView(mesh);
        mv.setMaterial(new PhongMaterial(color));
        return mv;
    }

    /* ================================================================
     *  IDLE ANIMATIONS
     * ================================================================ */
    private void startIdleAnimations() {
        // 1. float up/down
        idleFloat = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(characterGroup.translateYProperty(), 23, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(1.5),
                new KeyValue(characterGroup.translateYProperty(), 27, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(3),
                new KeyValue(characterGroup.translateYProperty(), 23, Interpolator.EASE_BOTH))
        );
        idleFloat.setCycleCount(Animation.INDEFINITE);
        idleFloat.play();

        // 2. gentle sway
        idleSway = new Timeline(
            new KeyFrame(Duration.ZERO,
                new KeyValue(yRotate.angleProperty(), -4, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(2.5),
                new KeyValue(yRotate.angleProperty(), 4, Interpolator.EASE_BOTH)),
            new KeyFrame(Duration.seconds(5),
                new KeyValue(yRotate.angleProperty(), -4, Interpolator.EASE_BOTH))
        );
        idleSway.setCycleCount(Animation.INDEFINITE);
        idleSway.play();

        // 3. blink
        scheduleNextBlink();

        // 4. ear wiggle (cat only)
        if (leftEarGroup != null && species.equalsIgnoreCase("kucing")) {
            Timeline earWiggle = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(leftEarGroup.rotateProperty(), -12, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(1.2),
                    new KeyValue(leftEarGroup.rotateProperty(), -6, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.seconds(2.4),
                    new KeyValue(leftEarGroup.rotateProperty(), -12, Interpolator.EASE_BOTH))
            );
            earWiggle.setCycleCount(Animation.INDEFINITE);
            earWiggle.play();
        }

        // 5. Custom Real-time Animations (Breathing, Sine-wave tail, Soft wing flaps)
        customAnimTimer = new AnimationTimer() {
            private final long startTime = System.nanoTime();
            @Override
            public void handle(long now) {
                double elapsedSeconds = (now - startTime) / 1_000_000_000.0;

                // Breathing (kembang-kempis) applied to body scale
                double breath = 1.0 + 0.035 * Math.sin(elapsedSeconds * 4.0);
                if (body != null) {
                    body.setScaleY(1.25 * breath);
                    body.setScaleZ(1.05 * breath);
                    body.setScaleX(1.15 + 0.02 * Math.sin(elapsedSeconds * 4.0));
                }

                // Head also breathes slightly out of sync for organic movement
                double headBreath = 1.0 + 0.015 * Math.sin(elapsedSeconds * 4.0 + 0.5);
                if (head != null) {
                    head.setScaleX(1.15 * headBreath);
                    head.setScaleY(0.92 * headBreath);
                }

                // Tail waving (gelombang sinus)
                if (tail != null) {
                    double tailWag = 20.0 * Math.sin(elapsedSeconds * 7.0);
                    tail.setRotate(25.0 + tailWag);
                    if (species.equalsIgnoreCase("kucing")) {
                        tail.setRotationAxis(Rotate.Z_AXIS);
                    } else if (species.equalsIgnoreCase("anjing")) {
                        tail.setRotationAxis(Rotate.Y_AXIS);
                    }
                }

                // Wing flapping (soft wing flaps)
                if (species.equalsIgnoreCase("burung")) {
                    if (leftWing != null && rightWing != null) {
                        double wingFlap = 18.0 * Math.sin(elapsedSeconds * 9.0);
                        leftWing.setRotate(-wingFlap);
                        leftWing.setRotationAxis(Rotate.Z_AXIS);
                        rightWing.setRotate(wingFlap);
                        rightWing.setRotationAxis(Rotate.Z_AXIS);
                    }
                }
            }
        };
        customAnimTimer.start();
    }

    /* â”€â”€ blink logic â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
    private void scheduleNextBlink() {
        blinkTimer = new Timeline(
            new KeyFrame(Duration.seconds(2.5 + Math.random() * 2), e -> doBlink())
        );
        blinkTimer.setCycleCount(Animation.INDEFINITE);
        blinkTimer.play();
    }

    private void doBlink() {
        Timeline blink = new Timeline(
            new KeyFrame(Duration.millis(0),
                new KeyValue(leftEye.scaleYProperty(), 1.15),
                new KeyValue(rightEye.scaleYProperty(), 1.15),
                new KeyValue(leftIris.scaleYProperty(), 1.05),
                new KeyValue(rightIris.scaleYProperty(), 1.05),
                new KeyValue(leftPupil.scaleYProperty(), 1.05),
                new KeyValue(rightPupil.scaleYProperty(), 1.05)),
            new KeyFrame(Duration.millis(80),
                new KeyValue(leftEye.scaleYProperty(), 0.1),
                new KeyValue(rightEye.scaleYProperty(), 0.1),
                new KeyValue(leftIris.scaleYProperty(), 0.1),
                new KeyValue(rightIris.scaleYProperty(), 0.1),
                new KeyValue(leftPupil.scaleYProperty(), 0.1),
                new KeyValue(rightPupil.scaleYProperty(), 0.1)),
            new KeyFrame(Duration.millis(160),
                new KeyValue(leftEye.scaleYProperty(), 1.15),
                new KeyValue(rightEye.scaleYProperty(), 1.15),
                new KeyValue(leftIris.scaleYProperty(), 1.05),
                new KeyValue(rightIris.scaleYProperty(), 1.05),
                new KeyValue(leftPupil.scaleYProperty(), 1.05),
                new KeyValue(rightPupil.scaleYProperty(), 1.05))
        );
        blink.play();

        // re-randomise next blink interval
        blinkTimer.stop();
        blinkTimer.getKeyFrames().set(0,
            new KeyFrame(Duration.seconds(2.5 + Math.random() * 2), ev -> doBlink()));
        blinkTimer.play();
    }

    /* ================================================================
     *  PARTICLE EFFECTS
     * ================================================================ */
    private void spawnParticles(String type, int count) {
        Color color;
        switch (type) {
            case "heart":   color = Color.rgb(255, 100, 130); break;
            case "star":    color = Color.rgb(255, 230, 50);  break;
            case "sparkle": color = Color.WHITE;              break;
            case "food":    color = Color.rgb(210, 150, 80);  break;
            case "water":   color = Color.rgb(100, 180, 255); break;
            case "zzz":     color = Color.rgb(180, 180, 255); break;
            default:        color = Color.WHITE;              break;
        }

        PhongMaterial pMat = new PhongMaterial(color);
        for (int i = 0; i < count; i++) {
            double r = 3 + Math.random() * 2;
            Sphere p = new Sphere(r);
            p.setMaterial(pMat);

            double startX = -20 + Math.random() * 40;
            double startY = -HR + Math.random() * 20;
            p.setTranslateX(startX);
            p.setTranslateY(startY);
            p.setTranslateZ(HR * 0.5);

            characterGroup.getChildren().add(p);

            double dur = 1.0 + Math.random() * 0.5;
            double drift = -40 - Math.random() * 40;

            Timeline pt = new Timeline(
                new KeyFrame(Duration.ZERO,
                    new KeyValue(p.translateYProperty(), startY),
                    new KeyValue(p.scaleXProperty(), 1),
                    new KeyValue(p.scaleYProperty(), 1),
                    new KeyValue(p.scaleZProperty(), 1)),
                new KeyFrame(Duration.seconds(dur),
                    new KeyValue(p.translateYProperty(), startY + drift, Interpolator.EASE_OUT),
                    new KeyValue(p.scaleXProperty(), 0, Interpolator.EASE_IN),
                    new KeyValue(p.scaleYProperty(), 0, Interpolator.EASE_IN),
                    new KeyValue(p.scaleZProperty(), 0, Interpolator.EASE_IN))
            );
            pt.setOnFinished(e -> characterGroup.getChildren().remove(p));
            pt.setDelay(Duration.millis(i * 120));
            pt.play();
        }
    }

    /* ================================================================
     *  ACTION ANIMATIONS
     * ================================================================ */
    public void animateAction(String action) {
        Timeline tl = new Timeline(60);
        tl.setCycleCount(1);

        double cy = characterGroup.getTranslateY();
        double ca = yRotate.getAngle();

        switch (action) {
            case "feed":
                tl.getKeyFrames().addAll(
                    kf(0,    cy,     ca, 1,    1),
                    kf(0.08, cy - 6, ca, 0.92, 1.08),
                    kf(0.16, cy,     ca, 1,    1)
                );
                spawnParticles("food", 4);
                break;

            case "play":
                tl.getKeyFrames().addAll(
                    kf(0,    cy,      ca,       1,    1),
                    kf(0.1,  cy - 25, ca,       0.9,  1.1),
                    kf(0.25, cy,      ca + 360, 1,    1),
                    kf(0.35, cy - 18, ca + 360, 0.92, 1.08),
                    kf(0.5,  cy,      ca + 720, 1,    1)
                );
                spawnParticles("star", 5);
                break;

            case "bath":
                tl.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO,
                        new KeyValue(characterGroup.translateXProperty(), 0)),
                    new KeyFrame(Duration.millis(50),
                        new KeyValue(characterGroup.translateXProperty(), 8, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(100),
                        new KeyValue(characterGroup.translateXProperty(), -8, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(150),
                        new KeyValue(characterGroup.translateXProperty(), 5, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(200),
                        new KeyValue(characterGroup.translateXProperty(), -5, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(250),
                        new KeyValue(characterGroup.translateXProperty(), 0, Interpolator.EASE_BOTH))
                );
                spawnParticles("water", 5);
                break;

            case "sleep":
                tl.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO,
                        new KeyValue(xRotate.angleProperty(), xRotate.getAngle())),
                    new KeyFrame(Duration.seconds(0.6),
                        new KeyValue(xRotate.angleProperty(), 85, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.seconds(0.9),
                        new KeyValue(characterGroup.translateYProperty(), cy + 22, Interpolator.EASE_BOTH))
                );
                spawnParticles("zzz", 3);
                break;

            case "vitamin":
                tl.getKeyFrames().addAll(
                    kf(0,    cy,     ca, 1,    1),
                    kf(0.07, cy - 3, ca, 1.05, 0.95),
                    kf(0.14, cy,     ca, 0.95, 1.05),
                    kf(0.21, cy - 3, ca, 1.05, 0.95),
                    kf(0.28, cy,     ca, 1,    1)
                );
                spawnParticles("sparkle", 5);
                break;
        }
        tl.play();
    }

    /** Convenience helper â€“ builds a keyframe with y-translate, rotation, and squash-stretch. */
    private KeyFrame kf(double t, double y, double r, double sx, double sy) {
        Duration d = t == 0 ? Duration.ZERO : Duration.seconds(t);
        return new KeyFrame(d,
            new KeyValue(characterGroup.translateYProperty(), y, Interpolator.EASE_BOTH),
            new KeyValue(yRotate.angleProperty(), r, Interpolator.EASE_BOTH),
            new KeyValue(characterGroup.scaleXProperty(), sx, Interpolator.EASE_BOTH),
            new KeyValue(characterGroup.scaleYProperty(), sy, Interpolator.EASE_BOTH));
    }

    /* ================================================================
     *  EYE TRACKING
     * ================================================================ */
    public void lookAt(double mx, double my, double paneWidth, double paneHeight) {
        double cx = paneWidth / 2;
        double cy = paneHeight / 2;
        double dx = (mx - cx) / cx;
        double dy = (my - cy) / cy;
        double maxOff = 4.5;
        double ox = Math.max(-maxOff, Math.min(maxOff, dx * maxOff));
        double oy = Math.max(-maxOff, Math.min(maxOff, dy * maxOff));

        leftPupil.setTranslateX(-23 + ox);
        leftPupil.setTranslateY(oy);
        rightPupil.setTranslateX(23 + ox);
        rightPupil.setTranslateY(oy);

        leftIris.setTranslateX(-23 + ox * 0.7);
        leftIris.setTranslateY(oy * 0.7);
        rightIris.setTranslateX(23 + ox * 0.7);
        rightIris.setTranslateY(oy * 0.7);

        leftGlint1.setTranslateX(-26 + ox);
        leftGlint1.setTranslateY(-3 + oy);
        rightGlint1.setTranslateX(20 + ox);
        rightGlint1.setTranslateY(-3 + oy);
        leftGlint2.setTranslateX(-20.5 + ox);
        leftGlint2.setTranslateY(2 + oy);
        rightGlint2.setTranslateX(25.5 + ox);
        rightGlint2.setTranslateY(2 + oy);
    }

    /* ================================================================
     *  EXPRESSIONS
     * ================================================================ */
    public void setExpression(String expr) {
        // Reset scale and positions to normal state
        leftEye.setScaleY(1.15); leftEye.setScaleX(1.15);
        rightEye.setScaleY(1.15); rightEye.setScaleX(1.15);
        leftIris.setScaleY(1.05); leftIris.setScaleX(1);
        rightIris.setScaleY(1.05); rightIris.setScaleX(1);
        leftPupil.setScaleY(1.05); leftPupil.setScaleX(1);
        rightPupil.setScaleY(1.05); rightPupil.setScaleX(1);
        leftGlint1.setScaleX(1); leftGlint1.setScaleY(1);
        rightGlint1.setScaleX(1); rightGlint1.setScaleY(1);

        if (leftBlush != null && rightBlush != null) {
            leftBlush.setScaleY(0.8); leftBlush.setScaleX(1.3);
            rightBlush.setScaleY(0.8); rightBlush.setScaleX(1.3);
        }

        switch (expr.toLowerCase()) {
            case "happy":
                leftEye.setScaleY(0.5); leftEye.setScaleX(1.3);
                rightEye.setScaleY(0.5); rightEye.setScaleX(1.3);
                leftIris.setScaleY(0.5);
                rightIris.setScaleY(0.5);
                leftPupil.setScaleY(0.5);
                rightPupil.setScaleY(0.5);
                leftGlint1.setScaleY(0.5);
                rightGlint1.setScaleY(0.5);
                if (leftBlush != null && rightBlush != null) {
                    leftBlush.setScaleX(1.55); leftBlush.setScaleY(1.2);
                    rightBlush.setScaleX(1.55); rightBlush.setScaleY(1.2);
                }
                mouth.setScaleX(1.4); mouth.setScaleY(0.55);
                break;

            case "sad":
                leftEye.setScaleY(0.65);
                rightEye.setScaleY(0.65);
                leftIris.setScaleY(0.65);
                rightIris.setScaleY(0.65);
                leftPupil.setScaleY(0.65);
                rightPupil.setScaleY(0.65);
                if (leftBlush != null && rightBlush != null) {
                    leftBlush.setScaleX(0.7); leftBlush.setScaleY(0.7);
                    rightBlush.setScaleX(0.7); rightBlush.setScaleY(0.7);
                }
                mouth.setScaleX(0.75); mouth.setScaleY(1.4);
                break;

            case "sleepy":
                leftEye.setScaleY(0.12);
                rightEye.setScaleY(0.12);
                leftIris.setScaleY(0.12);
                rightIris.setScaleY(0.12);
                leftPupil.setScaleY(0.12);
                rightPupil.setScaleY(0.12);
                if (leftBlush != null && rightBlush != null) {
                    leftBlush.setScaleX(0.85); leftBlush.setScaleY(0.8);
                    rightBlush.setScaleX(0.85); rightBlush.setScaleY(0.8);
                }
                mouth.setScaleX(0.5); mouth.setScaleY(0.5);
                break;

            default: // "normal"
                mouth.setScaleX(1); mouth.setScaleY(1);
                break;
        }
    }

    /* ================================================================
     *  IDLE CONTROL
     * ================================================================ */
    public void restartIdle() {
        idleFloat.play();
        idleSway.play();
        blinkTimer.play();
        if (customAnimTimer != null) {
            customAnimTimer.start();
        }
    }

    public void stopIdle() {
        idleFloat.stop();
        idleSway.stop();
        blinkTimer.stop();
        if (customAnimTimer != null) {
            customAnimTimer.stop();
        }
    }

    /* ================================================================
     *  PUBLIC ACCESSORS
     * ================================================================ */
    public Rotate getYRotate() { return yRotate; }
    public Rotate getXRotate() { return xRotate; }
    public Group getRoot()     { return root; }
}

