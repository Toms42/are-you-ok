/* autogenerated by Processing revision 1276 on 2021-10-03 */
import processing.core.*;
import processing.data.*;
import processing.event.*;
import processing.opengl.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class sketch extends PApplet {

/**
 * Words. 
 * 
 * The text() function is used for writing words to the screen.
 * The letters can be aligned left, center, or right with the 
 * textAlign() function. 
 */
  
class Prompt {
  String value;
  Prompt yes;
  Prompt no;

  Prompt(String value, Prompt yes, Prompt no) {
    this.value = value;
    this.yes = yes;
    this.no = no;
  }
  //Prompt(String value,  Prompt yes, Prompt no) {
  //  this.value = value;
  //  this.yes = yes;
  //  this.no = no;
  //}
}

PFont f;

int promptStartTime;
Prompt activePrompt;

//public enum Prompt {
//    OK,
//    DEPRESSED,
//    DONE
//}


 public void setup() {
  /* size commented out by preprocessor */;
  
  // Create the font
  //printArray(PFont.list());
  f = createFont("courier", 20);
  textFont(f);
  textAlign(CENTER, CENTER);
  
  promptStartTime = millis();
  activePrompt = new Prompt("are you ok?",
    null,
    new Prompt("have you been feeling depressed recently?", 
      new Prompt("I'm sorry to hear that.\nHave you been getting enough sleep?",
        null,
        null),
      new Prompt("Are you feeling anxious?",
        new Prompt("Try taking a deep breath!",
          null,
          null
        ),
        null
      )
    )
  );
}

 public void draw() {
  background(0);
  
  boolean pressingYes = keyPressed && key == 'y';
  boolean pressingNo = keyPressed && key == 'n';
  int now = millis();
  
  String promptString = "";
  if (activePrompt != null) {
    promptString = activePrompt.value;
  } else {
    promptString = "Thank you for using this mental health\nsupport kiosk!\nWe hope you are feeling better.\n\nPlease watch the following video from\nour sponsor";
  }
  
  boolean[] questionStatus = drawTextScrollQuestion(promptString, now - promptStartTime, 1000, 70, activePrompt != null);
  //drawTextScrollQuestion("Have you been feeling depressed recently?", now - START, 3000);
  boolean promptDone = questionStatus[0];
  boolean lightsOn = questionStatus[1];

  if (activePrompt != null && promptDone && pressingYes) {
    activePrompt = activePrompt.yes;
    promptStartTime = millis();
  }
  if (activePrompt != null && promptDone && pressingNo) {
    activePrompt = activePrompt.no;
    promptStartTime = millis();
  }
  
  
  
}

// Returns: {done prompting, blink state for buttons}
 public boolean[] drawTextScrollQuestion(String text, int elapsed, int duration, int minDelay, boolean getInput) {
  boolean[] res = {false, false};
  if (elapsed < 0) {
    return res;
  }
  int nchars = text.length();
  int nToDraw = min(nchars * elapsed / max(duration, nchars * minDelay), nchars);
  String prompt_so_far = text.substring(0, nToDraw);
  String todraw = prompt_so_far;
  if (nchars == nToDraw) {
    res[0] = true;
    if (((elapsed - duration) / 1000) % 2 == 1 && getInput) {
      text("Y/N", width/2, height*5/6);
      res[1] = true;
    } 
  }
  text(todraw, width/2, height/3);
  return res;
}


  public void settings() { size(640, 480); }

  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "sketch" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
