import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.io.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class sketch extends PApplet {



int noLED = 27;
int yesLED = 22;
int noButton = 4;
int yesButton = 5;

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

boolean speak;
int promptStartTime;
int lastPromptEndTime;
Prompt activePrompt;
Prompt rootPrompt;

//public enum Prompt {
//    OK,
//    DEPRESSED,
//    DONE
//}


public void setup() {
  //size(640, 480);
  
  frameRate(40);
  
  // Create the font
  //printArray(PFont.list());
  f = createFont("courier", 20);
  textFont(f);
  textAlign(CENTER, CENTER);
  
  boolean configured = false;
  int tries = 0;
  while (!configured && tries < 50) {
    try {
      GPIO.pinMode(noLED, GPIO.OUTPUT);
      GPIO.pinMode(yesLED, GPIO.OUTPUT);
      GPIO.pinMode(noButton, GPIO.INPUT_PULLUP);
      GPIO.pinMode(yesButton, GPIO.INPUT_PULLUP);
      configured = true;
      tries++;
    }
    catch(Exception e) {
    }
  }
  if (configured) {
    print("Successfully configured GPIO!");
  } else {
    print("Failed to configure GPIO!");
    return;
  }
  
  promptStartTime = millis();
  rootPrompt = new Prompt("are you ok?",
    null,
    new Prompt("have you been feeling depressed recently?", 
      new Prompt("I'm sorry to hear that.\nHave you been getting enough sleep?",
        new Prompt("Have you been exercising regularly?",
          new Prompt("Have you been eating a nutritionally rich diet?\nStudies show that processed foods\nhave a negative effect on mental health!",
            new Prompt("Try high-fiving a nearbye friend!\n................................\nAre you still feeling depressed?",
              new Prompt("Have you experienced the death of\na family member recently?",
                new Prompt("I'm sorry for your loss.", null, null),
                new Prompt("Are you sure you are depressed?",
                  new Prompt("Have you considered that many people\nhave it worse than you?", 
                    new Prompt("Hmm.. Consider reaching out to CAPS™!", null, null), 
                    new Prompt("That's awfully selfish of you.", null, null)
                    ),
                  null
                  )
                ),
              null
              ),
            new Prompt("Try eating nutritionally rich meals, like those\nsold at Nourish™ in the Cohen University Center,\nor by using a service such as HelloFresh™!", null, null)
            ),
          new Prompt("Try exercising.", null, null)
          ),
        new Prompt("Try getting more sleep.", null, null)),
      new Prompt("Are you feeling anxious?",
        new Prompt("Try taking a deep breath!\n------------------------------------\nAre you still feeling anxious?",
          new Prompt("Do you think your anxiety is due to an\nimpending climate disaster?",
            new Prompt("Try taking pleasure in the little things\nand avoid thinking about the future.\nGreen energy companies such as Exxonmobil™\nhave their best scientists on it!", null, null),
            new Prompt("Are you worried about your future?",
              new Prompt("Are you struggling to find a job?", 
                new Prompt("Did you decide to enter a field without\na stable job market?",
                  new Prompt("Consider attending an MBA program\nsuch as Carnegie Mellon's Tepper School of Business!", null, null),
                  new Prompt("Consider contacting the Career and Professional\nDevelopment Center for help updating your résumé!", null, null)
                  ),
                new Prompt("Are you struggling to find a job that you like?",
                  new Prompt("Are you worried that a career in your field\nwon't be emotionally fulfilling",
                    new Prompt("Does you career pay well?",
                      new Prompt("I don't see what you have to complain about then.", null, null),
                      new Prompt("Neither do I.\nMany people do not like their jobs.\n", null, null)
                      ),
                    new Prompt("Have you considered disconnecting from the world\nto join a commune?", 
                      new Prompt("Try it.", null, null),
                      new Prompt("Consider disconnecting from the world\nto join a commune!", null, null)
                      )
                    ),
                  new Prompt("That's good. Things could be much worse.\nAre you sure you are worried?",
                    new Prompt("I'm sorry to hear that. Please reach out if there's\nanything I can do to help.", null, null), 
                    null)
                  )
                ),
              new Prompt("Congrats!",
                null,
                new Prompt("No? You should feel lucky.\n78% of users are worried about their future.\nI'm worried about my future.", null, null)
                )
              )
            ),
          null
          ),
        new Prompt("Are you feeling stressed?",
          new Prompt("Are you stressed do to coursework or an\nupcoming exam?",
            new Prompt("Try reaching out to your professors for assistance.", null, null),
            new Prompt("Are you stressed due to a relationship\n or a friend?",
              new Prompt("Try cutting out the harmful or sad people\nin your life. That worked well for me.", null, null), 
              new Prompt("Have you tried chamomile tea?\n",
                new Prompt("Have you considered meditation?",
                  new Prompt("", null, null),
                  new Prompt("Try meditating while I read this text", null, null)
                  ),
                new Prompt("Try chamomile tea!\nIt is very calming.", null, null)
                )
              )
            ), //TODO
          new Prompt("Do you have persistent existential dread?",
            new Prompt("Does capitalism have you feeling down?", 
              new Prompt("Maybe this excerpt from Ayn Rand's novel The Fountainhead", null, null), 
              null
              ),
            new Prompt("Are you sure you're not ok?", 
              new Prompt("Most users are ok.\nYou might be overthinking things.", null, null),
              new Prompt("Great. You might be ok", null, null)
              )
            )
          )
        )
    )
  );
  activePrompt = rootPrompt;
  speak = true;
  noCursor();
}

public void draw() {
  background(0);
  
  boolean pressingYes = (keyPressed && key == 'y') || GPIO.digitalRead(yesButton) == GPIO.LOW;
  boolean pressingNo = (keyPressed && key == 'n') || GPIO.digitalRead(noButton) == GPIO.LOW;
  int now = millis();
  
  String promptString = "";
  if (activePrompt != null) {
    promptString = activePrompt.value;
  } else {
    promptString = "Thank you for using this mental health\nsupport kiosk!\nWe hope you are feeling better.";
  }
  
  if (speak) {
    //launch("/bin/bash", "-c", "'", "espeak", "\"" + promptString + "\"", "-p", "0", "'");
    //try {
    //  Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "'", "espeak", "\"" + promptString + "\"", "-p", "0", "'"});
    //}
    //catch(Exception e) {
    //  print("Failed to use espeak!\n");
    //}
    ProcessBuilder pb = new ProcessBuilder("espeak", "-p", "0", "\"" + promptString + "\"");
    try {
      pb.start();
    } catch(Exception e) {
      print("Failed!");
    }
    print("\n\n/bin/bash", "-c", "'", "espeak", "\"" + promptString + "\"", "-p", "0", "'");
    speak = false;
  }
  
  boolean[] questionStatus = drawTextScrollQuestion(promptString, now - promptStartTime, 1000, 50, activePrompt != null); //5 for testing, otherwise 70.
  //drawTextScrollQuestion("Have you been feeling depressed recently?", now - START, 3000);
  boolean promptDone = questionStatus[0];
  boolean lightsOn = questionStatus[1];
  
  if (!promptDone) {
    lastPromptEndTime = millis();
  }
  if (millis() - lastPromptEndTime > 15000 && activePrompt != rootPrompt) {
    activePrompt = rootPrompt;
    speak = true;
    promptStartTime = millis();
  }
  
  if (activePrompt == null && promptDone && millis() - lastPromptEndTime > 2000) {
    activePrompt = rootPrompt;
    speak = true;
    promptStartTime = millis();
  }

  if (activePrompt != null && promptDone && pressingYes) {
    activePrompt = activePrompt.yes;
    speak = true;
    promptStartTime = millis();
  }
  if (activePrompt != null && promptDone && pressingNo) {
    activePrompt = activePrompt.no;
    speak = true;
    promptStartTime = millis();
  }
  
  if (lightsOn) {
    GPIO.digitalWrite(noLED, true);
    GPIO.digitalWrite(yesLED, true);
  } else {
    GPIO.digitalWrite(noLED, false);
    GPIO.digitalWrite(yesLED, false);
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
      text("NO                      YES", width/2, height*5/6);
      res[1] = true;
    } 
  }
  text(todraw, width/2, height/3);
  return res;
}
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--stop-color=#cccccc", "sketch" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
