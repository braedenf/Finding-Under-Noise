
// A simple Particle class

class Particle {
  PVector location;
  PVector velocity;
  PVector acceleration;
  float lifespan;

  Particle(PVector l, PVector d) {
    acceleration = new PVector(0, 0.0);
    //d.div(10);
    d.normalize();
    //velocity = d.get();
    velocity = new PVector(0, 0, map(fft.calcAvg(20,5000),0,15,-30,40));
    location = l.get();
    lifespan = random (10, 100);
  }

  void run() {
    update();
    display();
  }

  // Method to update location
  void update() {
    velocity.add(acceleration);
    location.add(velocity);
    lifespan -= 3.0;
  }

  // Method to display
  void display() {
    //stroke(0,0);
    fill(255, lifespan);
    pushMatrix();
    translate(location.x, location.y, location.z);
    box(random(5, 15));
    popMatrix();
  }

  // Is the particle still useful?
  boolean isDead() {
    return (lifespan < 0.0);
  }
}