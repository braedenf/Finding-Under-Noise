void averageNoise(){  

  //store average noise level
    avgNoise.append(int(fft.calcAvg(1,20000)));
    treeNoise.append(int(fft.calcAvg(1,20000)));
    
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
  
void calibrate(){
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