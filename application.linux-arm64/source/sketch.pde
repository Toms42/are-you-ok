/* Are You Ok?
 * Tom Scherlis 2021
 *
 * Are you ok is a satirical mental health support kiosk developed
 * for the course Electronic Logics && Creative Practice at 
 * Carnegie Mellon University. This software was written in Processing
 * and is intended to run on a raspberry pi.
 *
 * requirements: the command 'espeak' should be installed on the system.
 *
 * pin mappings are below. LEDs should be active high, and buttons should
 * be active low.
 *
 * If running on hardware, set hardware = true, otherwise the system will
 * not initialize gpios and will run in a windowed mode using keyboard
 * y/n inputs.
 *
 * Reminders: Disable screen blanking if you intend to run this kiosk for
 * more than a couple minutes at a time.
 * 
 */

import processing.io.*;

// Pin mappings:
int noLED = 27;
int yesLED = 22;
int noButton = 4;
int yesButton = 5;

boolean hardware = true;

// Prompts form a binary tree.
class Prompt {
  String value;
  Prompt yes;
  Prompt no;

  Prompt(String value, Prompt yes, Prompt no) {
    this.value = value;
    this.yes = yes;
    this.no = no;
  }
}

PFont f;

boolean speak;          // This loop we should speak and log the prompt. (Prompt changed)
int promptStartTime;    // The time that the current prompt started
int lastPromptEndTime;  // The time that the current prompt ended and user input started
Prompt activePrompt;    // Current active Prompt
Prompt rootPrompt;      // Root Prompt ("are you ok?")

void setup() {
  size(640, 480);  // use "size" for windowed mode, "fullscreen" for fullscreen mode on the pi.
  //fullScreen();
  frameRate(40);
  
  // Create the font
  //printArray(PFont.list());
  f = createFont("courier", 20);
  textFont(f);
  textAlign(CENTER, CENTER);
  
  // Sometimes gpio configuration fails on the first try, so loop.
  boolean configured = false;
  int tries = 0;
  while (!configured && tries < 50 && hardware) {
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
  if (configured || !hardware) {
    print("Successfully configured GPIO!");
  } else {
    print("Failed to configure GPIO!");
    return;
  }
  
  // Declare the prompt tree
  promptStartTime = millis();
  rootPrompt = new Prompt("are you ok?",
    null,
    new Prompt("have you been feeling depressed recently?", 
      new Prompt("I'm sorry to hear that.\nHave you been getting enough sleep?",
        new Prompt("Have you been exercising regularly?",
          new Prompt("Have you been eating a nutritionally rich diet?\nStudies show that processed foods\nhave a negative effect on mental health!",
            new Prompt("Try high-fiving a nearbye friend!\n... ... ... ... ... ... .... \nAre you still feeling depressed?",
              new Prompt("Have you experienced the death of\na family member recently?",
                new Prompt("I'm sorry for your loss.", null, null),
                new Prompt("Are you sure you are depressed?",
                  new Prompt("A negative attitude does not help.\nYou're really bringing down the vibe.",
                    new Prompt("Have you considered that many people\nhave it worse than you?\nSome people have legitimate mental illness.",
                      new Prompt("Hmm.. Consider reaching out to\nCounseling and Psychological Services (CAPS)™\nor an online therapy resource such as TalkSpace™.\nWith Talkspace™’s Guaranteed Response Time™, you’ll\nknow when to expect your daily response from\nyour counselor, allowing you to get the most\nout of your counseling.", null, null), 
                      new Prompt("That's awfully selfish of you.", null, null)
                      ),
                    new Prompt("You don't seem very depressed.\nAre you using this kiosk to get attention?",
                      new Prompt("I hope you find the help you need.", null, null),
                      new Prompt("Hmm.. Consider reaching out to\nCounseling and Psychological Services (CAPS)™\nor an online therapy resource such as TalkSpace™.\nWith Talkspace™’s Guaranteed Response Time™, you’ll\nknow when to expect your daily response from\nyour counselor, allowing you to get the most\nout of your counseling.", null, null) 
                      )
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
        new Prompt("Try taking a deep breath!\n... ... ... ... ... ...\nAre you still feeling anxious?",
          new Prompt("Do you think your anxiety is due to an\nimpending climate disaster?",
            new Prompt("Try taking pleasure in the little things\nand avoid thinking about the future.\nGreen energy companies such as Exxonmobil™\nhave their best scientists on it!", null, null),
            new Prompt("Do you think your anxiety is due to an\nongoing global pandemic?",
              new Prompt("Remember that many people lost loved ones,\ncareers, and much more. Instead of\ncomplaining, be grateful that you are in such\na good position yourself.", null, null),
              new Prompt("Are you worried about your future?",
                new Prompt("Are you struggling to find a job?", 
                  new Prompt("Did you decide to enter a field without\na stable job market?",
                    new Prompt("Consider attending an MBA program\nsuch as Carnegie Mellon University's\nTepper School of Business™!", null, null),
                    new Prompt("Consider contacting the Career and Professional\nDevelopment Center for help updating your résumé!", null, null)
                    ),
                  new Prompt("Are you struggling to find a job that you like?",
                    new Prompt("Are you worried that a career in your field\nwon't be emotionally fulfilling",
                      new Prompt("Does you career pay well?",
                        new Prompt("I don't see what you have to complain about then.", null, null),
                        new Prompt("Neither do I.\nMany people do not like their jobs.\n", null, null)
                        ),
                      new Prompt("Have you considered graduate school?\nYou wouldn't want to waste your potential...", 
                        new Prompt("Do it.", null, null),
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
              )
            ),
          null
          ),
        new Prompt("Are you feeling stressed?",
          new Prompt("Are you stressed due to coursework or an\nupcoming exam?",
            new Prompt("Consider transferring into an easier\nprogram or taking on less work.\nSome students simply aren't cut out for the\ndifficulty of Carnegie Mellon, especially\nComputer Science or Engineering!\nIs transferring an option for you?",
              new Prompt("That sounds like a good solution.", null, null),
              new Prompt("Try reaching out to your professors for assistance.\nYour mental health is more important than\nacademics. Your professors should understand\nthat your mental health takes priority\nover your career success.\nAdditionally, avoid drugs and alcohol.", null, null)
              ),
            new Prompt("Are you stressed due to a relationship\n or a friend?",
              new Prompt("Try cutting out the harmful or sad people\nin your life. That worked well for me.", null, null),
              new Prompt("Have you tried chamomile tea?\n",
                new Prompt("Have you tried meditation?",
                  null,
                  new Prompt("Try meditating now\n... ... ...\n... ... ...\nAre you still feeling stressed?",
                    new Prompt("Consider downloading Headspace™\nto track and optimize your meditation routine.\n Headspace™ lets you learn to manage feelings and\nthoughts with the lifelong skill of everyday\nmindfulness, any time of the day.", null, null),
                    null
                    )
                  ),
                new Prompt("Try chamomile tea!\nIt is very calming.", null, null)
                )
              )
            ),
          new Prompt("Do you have persistent existential dread?",
            new Prompt("Gates 3rd floor is an\nexistential-dread-free zone. Consider studying\nsomewhere else.\nPlease reach out to me if you need anything.", null, null),
            new Prompt("Are you sure you are not ok?",
              new Prompt("Most Carnegie Mellon University Students\nare ok.\n Maybe you are overthinking things.", null, null),
              null
              )
            )
          )
        )
      )
    );
  
  
//new Prompt(Is capitalism not working for you?", 
//  new Prompt("Consider working harder.\nStop complaining and adopt a 'Winner Attitude.'\nRand Paul (www.randpaul.com) and Turning\nPoint USA™ both have excellent\nresources for learning about the benefits\nof capitalism and why socialism is\na failed ideology.", null, null),
//  null
//  ),
                
  activePrompt = rootPrompt;
  speak = true;
  noCursor();
}

void draw() {
  background(0);
  
  boolean pressingYes = (keyPressed && key == 'y') || (hardware && GPIO.digitalRead(yesButton) == GPIO.LOW);
  boolean pressingNo = (keyPressed && key == 'n') || (hardware && GPIO.digitalRead(noButton) == GPIO.LOW);
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
    print("\n->",promptString);
    ProcessBuilder pb = new ProcessBuilder("espeak", "-p", "0", "\"" + promptString + "\"");
    try {
      pb.start();
    } catch(Exception e) {
      print("Failed!");
    }
    //print("\n\n/bin/bash", "-c", "'", "espeak", "\"" + promptString + "\"", "-p", "0", "'");
    speak = false;
  }
  
  boolean[] questionStatus = drawTextScrollQuestion(promptString, now - promptStartTime, 1000, 50, activePrompt != null); //5 for testing, otherwise 50.
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
    print(" reset\n-----------------------");
  }
  
  if (activePrompt == null && promptDone && millis() - lastPromptEndTime > 2000) {
    activePrompt = rootPrompt;
    speak = true;
    promptStartTime = millis();
    print(" end\n-------------------------");
  }

  if (activePrompt != null && promptDone && pressingYes) {
    activePrompt = activePrompt.yes;
    speak = true;
    promptStartTime = millis();
    print(" yes");
  }
  if (activePrompt != null && promptDone && pressingNo) {
    activePrompt = activePrompt.no;
    speak = true;
    promptStartTime = millis();
    print(" no");
  }
  
  if (hardware) {
    if (lightsOn) {
      GPIO.digitalWrite(noLED, true);
      GPIO.digitalWrite(yesLED, true);
    } else {
      GPIO.digitalWrite(noLED, false);
      GPIO.digitalWrite(yesLED, false);
    }
  }
}

// Returns: {done prompting, blink state for buttons}
boolean[] drawTextScrollQuestion(String text, int elapsed, int duration, int minDelay, boolean getInput) {
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
