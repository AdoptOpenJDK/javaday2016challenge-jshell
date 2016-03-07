
/classpath jpct-1.jar
/classpath lwjgl_util-2.9.3.jar

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.threed.jpct.*;
import com.threed.jpct.util.*;
import java.util.Random;




float DAMPING = 0.1f;

float SPEED = 1f;

float MAXSPEED = 1f;

Graphics g = null;

KeyMapper keyMapper = null;

FrameBuffer fb = null;

World world = null;
Camera cam = null;

Object3D plane = null;

Object3D ramp = null;

Object3D cube = null;

Object3D cube2 = null;

Object3D sphere = null;

boolean up = false;

boolean down = false;

boolean left = false;

boolean right = false;

boolean doloop = true;

SimpleVector moveRes = new SimpleVector(0, 0, 0);

SimpleVector ellipsoid = new SimpleVector(2, 2, 2);

JFrame frame = new JFrame();

void loadSwingGUI() {

    int numberOfProcs = Runtime.getRuntime().availableProcessors();

    Config.useMultipleThreads = numberOfProcs > 1;
    Config.useMultiThreadedBlitting = numberOfProcs > 1;
    Config.loadBalancingStrategy = 1;
    Config.maxNumberOfCores = numberOfProcs;
    Config.lightMul = 1;
    Config.mtDebug = false;

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setSize(1024, 768);
    frame.setResizable(false);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    g = frame.getGraphics();
}

void initGame() {
    fb = new FrameBuffer(1024, 768, FrameBuffer.SAMPLINGMODE_NORMAL);
    world = new World();
    fb.enableRenderer(IRenderer.RENDERER_SOFTWARE);
    keyMapper = new KeyMapper(frame);

    plane = Primitives.getPlane(20, 10);
    plane.rotateX((float) Math.PI / 2f);

    ramp = Primitives.getCylinder(20);
    ramp.rotateX((float) Math.PI / 2f);

    sphere = Primitives.getSphere(30);
    sphere.translate(-50, 10, 50);

    cube2 = Primitives.getCube(20);
    cube2.translate(60, -20, 60);

    cube = Primitives.getCube(2);
    cube.translate(-50, -10, -50);

    plane.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
    ramp.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
    sphere.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
    cube2.setCollisionMode(Object3D.COLLISION_CHECK_OTHERS);
    cube.setCollisionMode(Object3D.COLLISION_CHECK_SELF);

    world.addObject(plane);
    world.addObject(ramp);
    world.addObject(cube);
    world.addObject(sphere);
    world.addObject(cube2);

    Light light = new Light(world);
    light.setPosition(new SimpleVector(0, -80, 0));
    light.setIntensity(140, 120, 120);
    light.setAttenuation(-1);

    world.setAmbientLight(20, 20, 20);

    world.buildAllObjects();
    
    cam = world.getCamera();
    cam.moveCamera(Camera.CAMERA_MOVEOUT, 100);
    cam.moveCamera(Camera.CAMERA_MOVEUP, 100);
    cam.lookAt(ramp.getTransformedCenter());
    
    
}

void move() {
    KeyState ks = null;
    while ((ks = keyMapper.poll()) != KeyState.NONE) {
        if (ks.getKeyCode() == KeyEvent.VK_UP) {
            up = ks.getState();
        }
        if (ks.getKeyCode() == KeyEvent.VK_DOWN) {
            down = ks.getState();
        }
        if (ks.getKeyCode() == KeyEvent.VK_LEFT) {
            left = ks.getState();
        }
        if (ks.getKeyCode() == KeyEvent.VK_RIGHT) {
            right = ks.getState();
        }

        if (ks.getKeyCode() == KeyEvent.VK_ESCAPE) {
            doloop = false;
        }
    }

    // move the cube
    if (up) {
        SimpleVector t = cube.getZAxis();
        t.scalarMul(SPEED);
        moveRes.add(t);
    }

    if (down) {
        SimpleVector t = cube.getZAxis();
        t.scalarMul(-SPEED);
        moveRes.add(t);
    }

    if (left) {
        cube.rotateY((float) Math.toRadians(-1));
    }

    if (right) {
        cube.rotateY((float) Math.toRadians(1));
    }

    // avoid high speeds
    if (moveRes.length() > MAXSPEED) {
        moveRes.makeEqualLength(new SimpleVector(0, 0, MAXSPEED));
    }

    cube.translate(0, -0.02f, 0);

    moveRes = cube.checkForCollisionEllipsoid(moveRes, ellipsoid, 8);
    cube.translate(moveRes);

    // ly apply the gravity:
    SimpleVector t = new SimpleVector(0, 1, 0);
    t = cube.checkForCollisionEllipsoid(t, ellipsoid, 1);
    cube.translate(t);

    // damping
    if (moveRes.length() > DAMPING) {
        moveRes.makeEqualLength(new SimpleVector(0, 0, DAMPING));
    } else {
        moveRes = new SimpleVector(0, 0, 0);
    }
}

void gameLoop() throws Exception {
    while (doloop) {
        move();
        refresh();
    }
    //fb.disableRenderer(IRenderer.RENDERER_SOFTWARE);
    //System.exit(0);
}
void refresh(){
        cam.setPositionToCenter(cube);
        cam.align(cube);
        cam.rotateCameraX((float) Math.toRadians(30));
        cam.moveCamera(Camera.CAMERA_MOVEOUT, 100);

        fb.clear(Color.YELLOW);
        world.renderScene(fb);
        world.draw(fb);

        fb.update();
        fb.display(g);
}

 


