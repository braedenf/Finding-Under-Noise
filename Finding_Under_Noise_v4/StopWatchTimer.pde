class StopWatchTimer {
  int startTime = 0, stopTime = 0;
  boolean running = false;  


  void start() {
    startTime = millis();
    running = true;
  }
  void stop() {
    stopTime = millis();
    running = false;
  }
  int getElapsedTime() {
    int elapsed;
    if (running) {
      elapsed = (millis() - startTime);
    }
    else {
      elapsed = (stopTime - startTime);
    }
    return elapsed;
  }

  void reset()
  {
    startTime = 0;
    stopTime = 0;
  }
}