class Sprite1 {

  //field-------------------------------------------------------------------------------------------]  
  //fades after image of spectrum bands into the background
  PImage fade;
  float rWidth, rHeight, spriteRadius;


  int hVal;
  int gbVal = 70;
  //size determined by volume
  float sSens; 
  float sSens2;
  float colourChangeThreshold;
  float x, y, z, speedX, speedY, speedZ, gravity, spinSpeed, alpha, colAlpha;
  float resist, spinAngle, lifeSpan, whiteStroke, CPD;
  

  //constructor-------------------------------------------------------------------------------------] 
  Sprite1() {
    fade = get(0, 0, width, height);
    rWidth = width * 0.99;
    rHeight = height * 0.99;
    hVal = 26;
    spriteRadius = map(sprite1Field.size(), 0, maxSprite1, random(50, 70), random(200,240)) ;
    sSens = spriteRadius*0.25/(1+noise);
    sSens2 = spriteRadius/(1+noise);
    alpha = (random(20,100)); //white alpha
    colAlpha = 60; // colour alpha
    whiteStroke = 0.3;
    colourChangeThreshold = 5;
    x = random(spriteRadius*5, width - spriteRadius*5);
    y = random(spriteRadius*5, height - spriteRadius*5);
    z = 0;
    speedX = random(-4, 4);
    speedY = random(-4, 4);
    speedZ = 0;
    gravity = 3;
    resist = 0.5;  
    spinAngle = 0; 
    lifeSpan = random(500, 2000);
    CPD = 1;  
  }

  //build-------------------------------------------------------------------------------------------]  

  void update() {
    move();
    confine();
    changeCol();
    whiteStroke=map(fft.calcAvg(0,3000),0,30,0.1,1);
    resist(); 
    spin();
        
    if (gravityActive && sprite1Field.size() >= maxSprite1*0.60) {
      gravity();
      CPD = 2;
    }
    else {
      gravityActive=false;
      CPD = 1;
    
      if(alpha==255){ alpha = (random(20,100));}
    }
  }







  void render() {  
    strokeWeight(spriteRadius);
    colorMode(HSB);
    stroke(hVal, gbVal, gbVal, colAlpha);
    

    /*checks the entire array of bands
     i is the x position in pixils,
     x position times by width divded
     by average*/
    for (int i = 0; i < fft.avgSize(); i++) {
      float a=radians((i* (360/fft.avgSize()))+spinAngle);  //+++++++++++++++++++++++++++++++++++++++++
      float r = spriteRadius;
      int cx = int(x + r*(sin(a)));
      int cy = int(y + r*(-cos(a)));

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

  void move() {
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

  void confine() {
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

  void changeCol() {
    if (fft.calcAvg(100,600) > colourChangeThreshold) 
    {
      hVal = hVal + 3;
      gbVal = gbVal * 2;
      
    }
    if ( hVal > 255) {
      hVal = 0;
    }
    if ( gbVal > 255) {
      gbVal = 70; 
    }
  }


  void resist() {
    speedX /= 1+(0.01*resist);
    speedY /= 1+(0.01*resist);
  }

  void spin() {
    if (fft.calcAvg(1000, 3000) > spinThreshold) {
      spinSpeed = random(-20, 20);
    }
    spinSpeed *= 0.99;
    spinAngle += spinSpeed;
  }

  boolean dead () {
    lifeSpan -= CPD; //lifeSpan decreases due to cigarettes (cigs per day)
    if (lifeSpan < 0) {
      return true;
    }
    else {
      return false;
    }
  }

  void gravity() {
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