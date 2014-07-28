/*
Finding Under Noise
Author: Hubbub Studios
(Reuben Poharama, Braeden Foster, Martin Hill)
Translates the noise of the environment into a visual representation.
Version: 2.2
Date: 1.11.13
*/



import ddf.minim.analysis.*;
import ddf.minim.*;
import toxi.geom.*;


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

void setup () 
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

void draw() 
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

void addSprite1() {

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


void activateGravity(){
 if (fft.calcAvg(80, 1000) > actGravThresh){
  if(!gravityActive){
    // changes mode of gravity
    gravityMode = int(random(1,5));
  }
   gravityActive = true;
  
 } 
}


void plantMove(){
  plantY = 0;
  plantX = width/3 - 40*sprite1Field.size();
  plantZ = -1000-(100*sprite1Field.size());
}





  
  

void keyPressed(){
  
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

void keyReleased(){
  
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

void changeVariables(){
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
  
  float change(float variable){
    if (upPress){
      variable += 0.01;
    }
    if (downPress){
     variable -= 0.01;
    }
    return variable;
  }
boolean sketchFullScreen(){ return true; }
