import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.analysis.*; 
import ddf.minim.*; 
import toxi.geom.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Finding_Under_Noise extends PApplet {

/*
Finding Under Noise
Author: Hubbub Studios
(Reuben Poharama, Braeden Foster, Martin Hill)
Translates the noise of the environment into a visual representation.
Version: 2.2
Date: 1.11.13
*/








Minim minim;
AudioInput in;
FFT fft;
Stick bob;

//declare
ArrayList <Sprite1> sprite1Field;
ArrayList <Stick> allBobs;
ArrayList <PVector> joints;
ArrayList <PVector> directions;
ArrayList<Particle> particles;    // An arraylist for all the particles
float maxSprite1, sprite1GenerationRate, strucChangeTime;
float addSpriteThreshold, spinThreshold, moveThreshold, actGravThresh, addBranchThreshold, sens, noise, treeNoiseAvg, totalTreeNoise, totalNoise;
StopWatchTimer swSprite1, swTree;
Sprite1 mySprite1;
IntList avgNoise;
FloatList treeNoise;
boolean gravityActive, upPress, downPress, aPress, sPress, mPress, gPress, textVisible;
int lastNoiseGet, lastCalTime, keyUp, keyDown, keyA, keyS, keyM, keyG, keyC;
int plantX, plantY, plantZ, totalBranch, vSoft, soft, qSoft, qLoud, loud, vLoud;
int strucState, gravityMode;


//ArrayList<ParticleSystem> systems;
int PNum = 10;

public void setup () 
{
  size (displayWidth, displayHeight, P3D);
  sketchFullScreen();
  noCursor();

  //initialise the minim object
  minim = new Minim (this);
  in = minim.getLineIn(Minim.STEREO, 512);

  //initialise the FFT object
  fft = new FFT(in.bufferSize(), in.sampleRate());
  //cuts up the buffer
  fft.logAverages(60, 10);
  
  
  joints = new ArrayList <PVector> ();     // initialise joints list
  particles = new ArrayList<Particle>();   // Initialise the arraylist
  directions = new ArrayList<PVector>();   // Initialise the directions
    
  allBobs = new ArrayList <Stick> ();
  addBranchThreshold = 20;
  totalBranch = 1;

  sprite1Field = new ArrayList <Sprite1> ();
  avgNoise = new IntList();
  treeNoise = new FloatList();
  maxSprite1 = 20;
  addSpriteThreshold = 5;
  spinThreshold = 3; 
  moveThreshold = 10;
  actGravThresh = 10;
  gravityActive = false;
  sprite1GenerationRate = 1000;
  strucChangeTime = 5000;
  strucState = 1;
  lastCalTime = 0;
  swSprite1 = new StopWatchTimer();
  swSprite1.start();
  swTree = new StopWatchTimer();
  swTree.start();
  treeNoiseAvg = 0;
  plantX = width/3;
  plantY = height/2;
  plantZ = -30;
  
  //calibration levels
  vSoft = 1;
  soft = 2;
  qSoft = 3;
  qLoud = 4;
  loud = 5;
  vLoud = 6;
  
  mySprite1 = new Sprite1(); 
  sprite1Field.add (mySprite1);

  //keys
  keyUp = 38;
  keyDown = 40;
  keyA = 65;
  keyS = 83;
  keyM = 77;
  keyG = 71;
  keyC = 67;
  
  upPress = false;
  downPress = false;
  aPress = false;
  sPress = false;
  mPress = false;
  gPress = false;
  
  textVisible = false;
  sens = 1;
  

}

public void draw() 
{
  //get noise
  averageNoise();
  
  //calibrate every second
  if (millis() > lastCalTime+1000){
    calibrate();
  }
  
  //black background, refreshes screen
  background(0);



// L-System -------------------------------------------------
  plantMove(); //changes postition of LSystem
  
    allBobs.clear();
  Vec3D iniVel = new Vec3D (340,20,0); //_vel
  Vec3D v = new Vec3D (plantX,plantY,plantZ); //_loc
  
  bob = new Stick(v, iniVel, totalBranch, "A");
  allBobs.add(bob);
  
  for(Stick b : allBobs) {
    b.run();
  }
  
  if (fft.getAvg(40) > addBranchThreshold && totalBranch < 8) {
   totalBranch++;
  }
  
  if (millis() % 10000 > 9920 && totalBranch >1){
    totalBranch--;
  }


//particles-----------------------------

  for (int i = 0; i < joints.size(); i++) {
    if (random(1,10) < 1); // add particles out of ten times  
    particles.add(new Particle(joints.get(i), directions.get(i)));    // Add particles to the arraylist
  }

    
  // Cycle through the ArrayList backwards, because we are deleting while iterating
  for (int i = particles.size()-1; i >= 0; i--) {
    Particle p = particles.get(i);
    p.run();
    if (p.isDead()) {
      particles.remove(i);
    }
    
  }
  
  


  



  //mixes left and right channels together
  fft.forward(in.mix);

  activateGravity();  
  
  //change variables on keypress
  changeVariables();
  


  for (int i = 0; i < sprite1Field.size(); i++) 
  {
    Sprite1 mySprite1 = (Sprite1) sprite1Field.get(i);

    mySprite1.update();
    mySprite1.render();
    
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    if (mySprite1.dead() && sprite1Field.size() > 1){
      sprite1Field.remove(i);
    }
  }
  
  if (fft.getAvg(40) > addSpriteThreshold) {
    addSprite1();
  }
  
  if (textVisible){
    fill(255);
    text("addSprite:"+addSpriteThreshold + ", spin:"+spinThreshold + ", move:"+moveThreshold + ", gravity:"+actGravThresh + ", sprites:"+sprite1Field.size() + ", sens"+sens + ", noise"+noise + ", tree"+totalTreeNoise, 0, height - 10);
    text("gravityMode="+gravityMode, 10,30);
  }
}

public void addSprite1() {

  mySprite1 = new Sprite1();
  if (swSprite1.getElapsedTime() >= sprite1GenerationRate) {
    if (sprite1Field.size() < maxSprite1) {
      sprite1Field.add (mySprite1);
      swSprite1.stop();
      swSprite1.reset();
      swSprite1.start();
    }
  }
}


public void activateGravity(){
 if (fft.calcAvg(80, 1000) > actGravThresh){
  if(!gravityActive){
    // changes mode of gravity
    gravityMode = PApplet.parseInt(random(1,5));
  }
   gravityActive = true;
  
 } 
}


public void plantMove(){
  plantY = 0;
  plantX = width/3 - 40*sprite1Field.size();
  plantZ = -1000-(100*sprite1Field.size());
}





  
  

public void keyPressed(){
  
  if (keyCode == keyUp){
    upPress = true;
  }
  if (keyCode == keyDown){
    downPress = true;
  }
  if (keyCode == keyA){
    aPress = true;
  }
  if (keyCode == keyS){
    sPress = true;
  }
  if (keyCode == keyM){
    mPress = true;
  }
  if (keyCode == keyG){
    gPress = true;
  }
  if (keyCode == keyC){
    calibrate();
  }
  if (key == 't'){
    textVisible = !textVisible;
  }
}

public void keyReleased(){
  
  if (keyCode == keyUp){
    upPress = false;
  }
  if (keyCode == keyDown){
    downPress = false;
  }
  if (keyCode == keyA){
    aPress = false;
  }
  if (keyCode == keyS){
    sPress = false;
  }
  if (keyCode == keyM){
    mPress = false;
  }
  if (keyCode == keyG){
    gPress = false;
  }
}

public void changeVariables(){
  if (aPress){
    addSpriteThreshold = change(addSpriteThreshold);
  }
  if (sPress){
    spinThreshold = change(spinThreshold);
  }
  if (mPress){
    moveThreshold = change(moveThreshold);
  }
  if (gPress){
    //actGravThresh = change(actGravThresh);
    gravityActive = true;
  }
}
  
  public float change(float variable){
    if (upPress){
      variable += 0.01f;
    }
    if (downPress){
     variable -= 0.01f;
    }
    return variable;
  }
public boolean sketchFullScreen(){ return true; }
public void averageNoise(){  

  //store average noise level
    avgNoise.append(PApplet.parseInt(fft.calcAvg(1,20000)));
    treeNoise.append(PApplet.parseInt(fft.calcAvg(1,20000)));
    
    totalTreeNoise = 0;
    
    
    for (int i = 0; i < treeNoise.size(); i++){
      totalTreeNoise += treeNoise.get(i);
    }
    
    treeNoiseAvg = totalTreeNoise/treeNoise.size();
    
    if (treeNoise.size() > 1000){
      treeNoise.remove(0);
    }
    
    if (avgNoise.size() > 3000){
      avgNoise.remove(0);
    }
}
  
public void calibrate(){
  totalNoise = 0;
  //get average of list
  for (int i = 0; i < avgNoise.size(); i++){
    totalNoise += avgNoise.get(i);
  }
  noise = 3*totalNoise/avgNoise.size();
  
  //change sens
  
//  if (noise > noiseBefore && noise >0){
//    sens -= noiseBefore-noise;
//  }
//  if (noise < noiseBefore){
//    sens += noiseBefore-noise;
//  }
  
    addSpriteThreshold = 5+5*noise;
    spinThreshold = 3+3*noise;
    moveThreshold = 10+10*noise;
    actGravThresh = 10+10*noise;
    addBranchThreshold = 5+5*noise;
  
  
  //clear list
  avgNoise.clear();
  
  lastCalTime = millis();
  
}
class Sprite1 {

  //field-------------------------------------------------------------------------------------------]  
  //fades after image of spectrum bands into the background
  PImage fade;
  float rWidth, rHeight, spriteRadius;


  int hVal;

  //size determined by volume
  float sSens; 
  float sSens2;
  float colourChangeThreshold;
  float x, y, z, speedX, speedY, speedZ, gravity, spinSpeed, alpha, colAlpha;
  float resist, spinAngle, lifeSpan, whiteStroke, CPD;
  

  //constructor-------------------------------------------------------------------------------------] 
  Sprite1() {
    fade = get(0, 0, width, height);
    rWidth = width * 0.99f;
    rHeight = height * 0.99f;
    hVal = PApplet.parseInt(random(0,255));
    spriteRadius = map(sprite1Field.size(), 0, maxSprite1, random(50, 70), random(200,240)) ;
    sSens = spriteRadius*0.25f/(1+noise);
    sSens2 = spriteRadius/(1+noise);
    alpha = (random(20,100)); //white alpha
    colAlpha = 60; // colour alpha
    whiteStroke = 0.3f;
    colourChangeThreshold = 5;
    x = random(spriteRadius*5, width - spriteRadius*5);
    y = random(spriteRadius*5, height - spriteRadius*5);
    z = 0;
    speedX = random(-4, 4);
    speedY = random(-4, 4);
    speedZ = 0;
    gravity = 3;
    resist = 0.5f;  
    spinAngle = 0; 
    lifeSpan = random(500, 2000);
    CPD = 1;  
  }

  //build-------------------------------------------------------------------------------------------]  

  public void update() {
    move();
    confine();
    changeCol();
    whiteStroke=map(fft.calcAvg(0,3000),0,30,0.1f,1);
    resist(); 
    spin();
        
    if (gravityActive && sprite1Field.size() >= maxSprite1*0.60f) {
      gravity();
      CPD = 2;
    }
    else {
      gravityActive=false;
      CPD = 1;
    
      if(alpha==255){ alpha = (random(20,100));}
    }
  }







  public void render() {

    strokeWeight(spriteRadius);
    colorMode(HSB);
    stroke(hVal, 255, 255, colAlpha);


    /*checks the entire array of bands
     i is the x position in pixils,
     x position times by width divded
     by average*/
    for (int i = 0; i < fft.avgSize(); i++) {
      float a=radians((i* (360/fft.avgSize()))+spinAngle);  //+++++++++++++++++++++++++++++++++++++++++
      float r = spriteRadius;
      int cx = PApplet.parseInt(x + r*(sin(a)));
      int cy = PApplet.parseInt(y + r*(-cos(a)));

      line(cx, cy, z, cx + fft.getAvg(i) * sin(a) * sSens, cy + fft.getAvg(i) * -cos(a) * sSens, z);
    }




    //first layer is white
    strokeWeight(spriteRadius*whiteStroke);
    colorMode(RGB);
    stroke(255, alpha);
    for (int i = 0; i < fft.avgSize(); i++) {
      float a=radians( ( i*( 360/fft.avgSize() ) )+spinAngle);  //++++++++++++++++++++++++++++++++++++++
      float cx = x;
      float cy = y;
      line(cx, cy, z, cx + fft.getAvg(i) * sin(a) * sSens2, cy + fft.getAvg(i) * -cos(a) * sSens2, z);
    }
  }

  public void move() {
    y += speedY;
    x += speedX;
    z += speedZ;
    if (fft.calcAvg(80, 700) > moveThreshold) {
      speedX = random(-4, 4);
      speedY = random(-4, 4);
    }
    if(lifeSpan < 100){
      speedZ = map(fft.calcAvg(20,5000),0,20,5,30);
    }
    
  }

  public void confine() {
    if (x <= spriteRadius) {
      speedX = -speedX;
      x= spriteRadius;
    }
    if (x >= width - spriteRadius) {
      speedX = -speedX;
      x= width-(spriteRadius);
    }
    if (y <= spriteRadius) {
      speedY = -speedY;
      y=spriteRadius;
    }
    if (y >= height - spriteRadius) {
      speedY = -speedY;
      y=height-(spriteRadius);
    }
  }

  public void changeCol() {
    if (fft.calcAvg(100,600) > colourChangeThreshold) 
    {
      hVal = hVal + 5;
    }
    if ( hVal > 255) {
      hVal = 0;
    }
  }


  public void resist() {
    speedX /= 1+(0.01f*resist);
    speedY /= 1+(0.01f*resist);
  }

  public void spin() {
    if (fft.calcAvg(1000, 3000) > spinThreshold) {
      spinSpeed = random(-20, 20);
    }
    spinSpeed *= 0.99f;
    spinAngle += spinSpeed;
  }

  public boolean dead () {
    lifeSpan -= CPD; //lifeSpan decreases due to cigarettes (cigs per day)
    if (lifeSpan < 0) {
      return true;
    }
    else {
      return false;
    }
  }

  public void gravity() {
    alpha=255;
    //fill(255);
    //textSize(70);
    //switch (gravityMode){
    //case '1':
    if (gravityMode == 1){
      if (x < width/2) { 
        speedX += gravity;
      } 
      else { 
        speedX -= gravity;
      } 
      if (y < height/2) { 
        speedY += gravity;
      } 
      else { 
        speedY -= gravity;
      }
      //break;
    }
      
     // case '2':
    if (gravityMode == 2){
      if (x < width/2) { 
        speedX -= gravity;
      } 
      else { 
        speedX += gravity;
      } 
      if (y < height/2) { 
        speedY -= gravity;
      } 
      else { 
        speedY += gravity;
      }
      //break;
    }
      
    //case '3':
    if (gravityMode == 3){
      speedY += gravity;
    //break;  
    }
    
    //case '4':
    if (gravityMode == 4){
      speedY -= gravity;
    //break; 
    }
    
  }
  
//
//  void collide(){
//    for (int o = sprite1Field.size()-1; o>= 0; o--){
//      Sprite1 l = sprite1Field.get(o);
//      if (dist(x, y, l.x, l.y) <= spriteRadius+l.spriteRadius){   //balls are together
//        
//        x = -x;
//        y = -y;
//        l.x = -l.x;
//        l.y = -l.y;
//        
//      
////       float totaldx = (speedX + l.speedX)/2;  //calculate total speedx
////       float totaldy = (speedY + l.speedY)/2;  //calculate total speedy
////   
////       //apply total velocities to both balls
////       speedX = totaldx;
////       speedY = totaldy;
////       speedX = totaldx;
////       speedY = totaldy;
//      }
//    }
//  }
}
class Stick {
  //Global Variables----------------------------]
  Vec3D loc;
  Vec3D vel;
  Vec3D oriLoc;
  StopWatchTimer swAddBranch;

  int generations;
  String type;
  float stickAngle;
  int alpha;



  //Constructor--------------------------]
  Stick(Vec3D _loc, Vec3D _vel, int _generations, String _type) {
    loc = _loc;
    vel = _vel;
    oriLoc = _loc.copy();
    generations = _generations;

    type = _type;
    stickAngle = 0;
    alpha = PApplet.parseInt(random(20, 60));
    //if (totalBranch >= 7){ alpha = 20; }
    //alpha = int(map(totalBranch,0,7,60,20));


    //Stack of functions that only get executed once
    updateLoc();
    updateDir();
    spawn();
  }

  //Build--------------------------------]
  public void run() {
    display();
  }



  public void spawn() {

    joints.clear();

    if (generations > 0) {
      if (type == "A") {
        Vec3D iniVel = vel.copy();
        ;
        Vec3D v = loc.copy();
        Stick newBob = new Stick(v, iniVel, generations-1, "A");

        allBobs.add(newBob);

        Vec3D iniVel2 = vel.copy();
        ;
        Vec3D v2 = loc.copy();
        Stick newBob2 = new Stick(v2, iniVel2, generations-1, "B");

        allBobs.add(newBob2);
      }
      if (type == "B") {
        Vec3D iniVel = vel.copy();
        ;
        Vec3D v = loc.copy();
        Stick newBob = new Stick(v, iniVel, generations-1, "C");

        allBobs.add(newBob);
      }
      if (type == "C") {
        Vec3D iniVel = vel.copy();
        ;
        Vec3D v = loc.copy();
        Stick newBob = new Stick(v, iniVel, generations-1, "A");

        allBobs.add(newBob);
      }
    }
  }


  public void updateLoc() {
    loc.addSelf(vel);
  }



  public void updateDir() {
    stickAngle += 5;

    if (swTree.getElapsedTime() >= strucChangeTime) {
      strucState = PApplet.parseInt(random(1, 4));

      swTree.stop();
      swTree.reset();
      swTree.start();
    }

    if (type == "A") {
      float angle1 = 1;
      float angle2 = 1;
      float angle3 = 1;
      if (strucState == 1) {
        angle1 = radians (map(fft.calcAvg(0, 600), 0, 10, 10, 80));
        angle2 = radians(map(fft.calcAvg(100, 900), 0, 10, 20, 400));
        angle3 = radians(random(40, 42));
      }
      if (strucState == 2) {
        angle1 = radians (map(fft.calcAvg(0, 600), 0, 10, 10, 180));
        angle2 = radians(map(fft.calcAvg(100, 900), 0, 10, 80, 300));
        angle3 = radians(random(50, 52));
      }
      if (strucState == 3) {
        angle1 = radians (map(fft.calcAvg(0, 600), 0, 10, 10, 380));
        angle2 = radians(map(fft.calcAvg(100, 900), 0, 10, 50, 700));
        angle3 = radians(random(80, 82));
      }
      vel.rotateX(-angle3);
      vel.rotateY(-angle1);
      vel.rotateZ(angle3);
    }
    if (type == "B") {
      float angle1 = 1;
      float angle2 = 1;
      float angle3 = 1;
      if (strucState == 1) {
        angle1 = radians(map(fft.calcAvg(900, 4000), 0, 15, 5, 200));
        angle2 = radians(map(fft.calcAvg(2000, 5000), 0, 20, 50, 380));
        angle3 = radians(noise);
      }
      if (strucState == 2) {
        angle1 = radians(map(fft.calcAvg(900, 4000), 0, 15, 70, 100));
        angle2 = radians(map(fft.calcAvg(2000, 5000), 0, 20, 0, 380));
        angle3 = radians(noise);
      }
      if (strucState == 3) {
        angle1 = radians(map(fft.calcAvg(900, 4000), 0, 15, 15, 20));
        angle2 = radians(map(fft.calcAvg(2000, 5000), 0, 20, 80, 480));
        angle3 = radians(noise);
      }

      vel.rotateX(angle2);
      vel.rotateY(angle1);
      vel.rotateZ(angle3);
    }
    if (type == "C") {
      float angle1 = radians(150);
      float angle2 = radians(map(fft.calcAvg(0, 3000), 0, 25, 0, 100));
      float angle3 = radians(random(180, 183));
      vel.rotateX(angle1);
      vel.rotateY(angle2);
      vel.rotateZ(angle3);
    }
  }

  public void display() {
    stroke(255, 0, 0, 100);
    strokeWeight(4);
    point(loc.x, loc.y, loc.z);

    stroke(255, alpha);
    strokeWeight(4);
    line(loc.x, loc.y, loc.z, oriLoc.x, oriLoc.y, oriLoc.z);

    joints.add(new PVector(loc.x, loc.y, loc.z));
    directions.add(new PVector(vel.x, vel.y, vel.z));
  }
}

class StopWatchTimer {
  int startTime = 0, stopTime = 0;
  boolean running = false;  


  public void start() {
    startTime = millis();
    running = true;
  }
  public void stop() {
    stopTime = millis();
    running = false;
  }
  public int getElapsedTime() {
    int elapsed;
    if (running) {
      elapsed = (millis() - startTime);
    }
    else {
      elapsed = (stopTime - startTime);
    }
    return elapsed;
  }

  public void reset()
  {
    startTime = 0;
    stopTime = 0;
  }
}


// A simple Particle class

class Particle {
  PVector location;
  PVector velocity;
  PVector acceleration;
  float lifespan;

  Particle(PVector l, PVector d) {
    acceleration = new PVector(0, 0.0f);
    //d.div(10);
    d.normalize();
    //velocity = d.get();
    velocity = new PVector(0, 0, map(fft.calcAvg(20,5000),0,15,-30,40));
    location = l.get();
    lifespan = random (10, 100);
  }

  public void run() {
    update();
    display();
  }

  // Method to update location
  public void update() {
    velocity.add(acceleration);
    location.add(velocity);
    lifespan -= 3.0f;
  }

  // Method to display
  public void display() {
    //stroke(0,0);
    fill(255, lifespan);
    pushMatrix();
    translate(location.x, location.y, location.z);
    box(random(5, 15));
    popMatrix();
  }

  // Is the particle still useful?
  public boolean isDead() {
    return (lifespan < 0.0f);
  }
}

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--full-screen", "--bgcolor=#666666", "--stop-color=#cccccc", "Finding_Under_Noise" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
