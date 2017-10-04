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
    alpha = int(random(20, 60));
    //if (totalBranch >= 7){ alpha = 20; }
    //alpha = int(map(totalBranch,0,7,60,20));


    //Stack of functions that only get executed once
    updateLoc();
    updateDir();
    spawn();
  }

  //Build--------------------------------]
  void run() {
    display();
  }



  void spawn() {

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


  void updateLoc() {
    loc.addSelf(vel);
  }



  void updateDir() {
    stickAngle += 5;

    if (swTree.getElapsedTime() >= strucChangeTime) {
      strucState = int(random(1, 4));

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

  void display() {
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

