import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.exception.outcomes.TestPassed;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testing.TestedProgram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlobalTests extends StageTest {

  int STAGE = 6;
  String name = "n3w_v3ry_unu5u4l_r04d_n4m3_";

  public void ForStages(int[] stages) {
    if (Arrays.binarySearch(stages, STAGE) < 0) {
      throw new TestPassed();
    }
  }

  static public void CheckMenu(String[] lines, String testCase) {
    String ADD_INFO = "Incorrect menu output in the following case: " + testCase + ". ";
    if (lines.length != 5) {
      throw new WrongAnswer(ADD_INFO + "It should contain exactly 5 lines but there were " + lines.length + " instead");
    }

    if (!lines[0].contains("menu")) {
      throw new WrongAnswer(ADD_INFO + "First line should " +
              "contain \"Menu\" substring");
    }

    String[] starts = new String[]{"1", "2", "3", "0"};
    String[] contain = new String[]{"Add", "Delete", "System", "Quit"};
    for (int i = 0; i < starts.length; i++) {
      if (!lines[1 + i].startsWith(starts[i]) || !lines[1 + i].contains(contain[i].toLowerCase())) {
        throw new WrongAnswer(String.format(ADD_INFO + "The %d line of options' list should start with \"%s\" " +
                "and contain \"%s\" substring as in example", i + 1, starts[i], contain[i]));
      }
    }
  }

  static public SystemOutput GetSystemInfo(String output, int roadsAmount, boolean parseRoads) {
    String[] lines = output.toLowerCase().split("\s*[\r\n]+\s*");
    if (lines.length != 4 && roadsAmount == 0) {
      throw new WrongAnswer("System information printed each second should contain exactly 4 " +
              "non-empty lines, when no roads were added: one that shows amount of time since the start of the " +
              "program, next two should show the provided initial settings and the last, that asks user to " +
              "press Enter to show options, as in example");
    }
    if (roadsAmount != 0 && lines.length != 4 + roadsAmount) {
      throw new WrongAnswer("When the user provided any changes to queue, output of system mode should " +
              "change. There should be exactly 4+n non-empty lines, where n is the amount of elements in " +
              "queue, in such order, just like in the example:\n" +
              "1. Line, that shows amount of time since the start of the program\n" +
              "2. Line, that shows max number of elements, provided by user\n" +
              "3. Line, that shows interval, provided by user\n" +
              "...\n" +
              "*queue*\n" +
              "...\n" +
              "n+4. Line, that that asks user to press 'Enter' to show options");
    }

    if (!lines[1].contains("number")) {
      throw new WrongAnswer("The line, that shows number of roads, provided by user, should contain " +
              "\"number\" substring");
    }

    if (!lines[2].contains("interval")) {
      throw new WrongAnswer("The line, that shows interval, provided by user, should contain " +
              "\"interval\" substring");
    }

    if (!lines[lines.length - 1].contains("enter")) {
      throw new WrongAnswer("The last line, that asks user to press Enter to show options should contain" +
              " \"Enter\" substring");
    }

    return SystemOutput.parseStringInfo(lines, parseRoads);
  }

  static public int ProcessSystemSecondsInitial(SystemOutput info, int startSecond, int initRoads,
                                                int initInterval) {
    if (startSecond != -1) {
      if (info.seconds != startSecond + 1) {
        throw new WrongAnswer("Time difference between two outputs (current and a second earlier)" +
                " is not equal to 1:\nSecond earlier: " + startSecond + "\nCurrent: " + info.seconds);
      }
      if (info.maxRoads != initRoads) {
        throw new WrongAnswer("Line with initial setting (number of roads) shows incorrect value.");
      }
      if (info.interval != initInterval) {
        throw new WrongAnswer("Line with initial setting (interval) shows incorrect value.");
      }
    }
    return info.seconds;
  }

  public static Thread GetUsersThreadByName(String threadName) {
    Thread usersThread = null;
    for (Thread t : Thread.getAllStackTraces().keySet()) {
      if (t.getName().equals(threadName))
        usersThread = t;
    }
    if (usersThread == null) {
      throw new WrongAnswer("There should be created new thread when number of roads and interval settings were " +
              "set, named as \"QueueThread\". Make sure, that it was created properly, didn't terminate immediately " +
              "and was not misspelled");
    }
    return usersThread;
  }

  public String AwaitOutputAtStart(TestedProgram pr) {
    String output = null;
    int millisAwait = 0;
    boolean outputPerformed = false;
    while (millisAwait < 1050 && !outputPerformed) {
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      millisAwait += 50;
      output = pr.getOutput().toLowerCase();
      if (!output.equals("")) {
        outputPerformed = true;
      }
    }
    if (output.equals("") || millisAwait > 1050) {
      throw new WrongAnswer("When the user selected '3' as an option, program should print new system " +
              "information each second, but after 1 second of waiting there was no output.");
    }
    try {
      Thread.sleep(200);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    String addOutput = pr.getOutput().toLowerCase();
    return output + addOutput;
  }

  public List<String> GetSystemOutputInSeconds(TestedProgram pr, int seconds) {
    String output = AwaitOutputAtStart(pr);
    if (seconds > 1) {
      try {
        Thread.sleep(1050 * (seconds - 1));
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      String newOutput = pr.getOutput().toLowerCase();
      output += newOutput;
    }
    List<String> outputs = new ArrayList<>();
    StringBuilder piece = new StringBuilder();
    for (String line : output.split("\\n")) {
      piece.append(line).append("\n");
      if (line.contains("enter")) {
        outputs.add(piece.toString());
        piece = new StringBuilder();
      }
    }
    if (!piece.toString().equals("")) {
      outputs.add(piece.toString());
    }
    return outputs;
  }

  public SystemOutput ProcessConditions(String output, String correct, SystemOutput previous, int roadsAmount,
                                        int interval, boolean reveal, String actionInBetween) {
    SystemOutput info = GetSystemInfo(output, roadsAmount, true);
    String[] roads = correct.equals("") ? new String[]{} : correct.split(";");

    if (info.roadLines.size() != roads.length) {
      throw new WrongAnswer("Incorrect number of roads was found in output after action: "
              + (previous == null ? "Started." + actionInBetween : actionInBetween));
    }
    for (int j = 0; j < info.roadLines.size(); j++) {
      String[] data = roads[j].split(",");
      if (info.roadLines.get(j).isOpen() != data[0].equals("1")) {
        throw new WrongAnswer("Some roads describe their state incorrectly. Road should be \"closed\", " +
                "but found \"open\" or vise versa."
                + revealTest(previous, info, correct, actionInBetween, interval, reveal));
      }
      if (info.roadLines.get(j).seconds != Integer.parseInt(data[1])) {
        throw new WrongAnswer("Some roads' time to close/open is incorrect."
                + revealTest(previous, info, correct, actionInBetween, interval, reveal));
      }
    }
    return info;
  }

  @DynamicTest(order = 0)
  CheckResult test_initial_and_menu() {

    TestedProgram pr = new TestedProgram();
    String output = pr.start().toLowerCase();

    String[] lines = output.split("\s*[\r\n]+\s*");

    if (lines.length != 2) {
      return CheckResult.wrong("There should be exactly 2 lines in the output when the program just started, " +
              "but there were " + lines.length + " instead");
    }

    if (!lines[0].contains("welcome") || (!lines[0].contains("traffic management system"))) {
      return CheckResult.wrong("The first line of output should contain a greeting, as in example");
    }

    if (!lines[1].contains("input") || !lines[1].contains("number")) {
      return CheckResult.wrong("When the program just started, there should be a line, that asks user to input " +
              "number of roads with \"Input\" and \"Number\" substrings");
    }

    output = pr.execute("5").toLowerCase();
    lines = output.split("\s*[\r\n]+\s*");

    if (lines.length != 1) {
      return CheckResult.wrong("There should be exactly 1 line printed when the user inputted desired number of " +
              "roads, but there were " + lines.length + " instead");
    }

    if (!lines[0].contains("input") || !lines[0].contains("interval")) {
      return CheckResult.wrong("When the user provided number of roads, there should be a line, that asks user to " +
              "input " +
              "interval value with \"Input\" and \"Interval\" substrings");
    }

    output = pr.execute("3").toLowerCase();

    CheckMenu(output.split("\s*[\r\n]+\s*"), "Start of the program");

    pr.execute("0");
    if (!pr.isFinished()) {
      return CheckResult.wrong("After user inputted '0' as a desired option, program should finish it's execution");
    }

    return CheckResult.correct();
  }

  Object[][] stubs = {
          {"1", "add", 5},
          {"2", "delete", 5},
          {"3", "system", 4}
  };

  @DynamicTest(order = 1, data = "stubs")
  CheckResult test_stubs_and_quit(String option, String stubContain, int endStage) {
    if (STAGE <= 2 || STAGE >= endStage) {
      return CheckResult.correct();
    }
    TestedProgram pr = new TestedProgram();
    pr.start();

    for (String s : new String[]{"5", "3"})
      pr.execute(s);

    String output = pr.execute(option).toLowerCase();
    String[] lines = output.split("\s*[\r\n]+\s*");

    if (lines.length != 1 || !lines[0].contains(stubContain))
      return CheckResult.wrong(String.format("For \"%s\" option on current stage there should be a simple one-line " +
              "stub, containing \"%s\" substring, followed by input to return back to menu", option, stubContain));

    output = pr.execute("").toLowerCase();

    CheckMenu(output.split("\s*[\r\n]+\s*"), String.format("Stub for \"%s\" option shown and blank input provided",
            option));

    if (pr.isFinished()) {
      return CheckResult.wrong("Option's selection should be looped");
    }
    pr.execute("0");
    if (!pr.isFinished()) {
      return CheckResult.wrong("After user inputted '0' as a desired option, program should finish it's execution");
    }

    return CheckResult.correct();
  }

  @DynamicTest(order = 2)
  CheckResult test_incorrect_initial() {
    ForStages(new int[]{3, 4, 5, 6});
    TestedProgram pr = new TestedProgram();
    pr.start();
    String output;

    String[] lines;

    for (String ex : new String[]{"asd", "-1", "6-", "0", "Hello world!"}) {
      output = pr.execute(ex).toLowerCase();
      lines = output.split("\s*[\r\n]+\s*");
      if (lines.length != 1 || !lines[0].contains("incorrect input") || !lines[0].contains("again")) {
        return CheckResult.wrong("When the user provides incorrect input for number of roads (<=0 or not " +
                "numeric), there should be printed exactly one line, containing \"incorrect input\" and " +
                "\"again\" substrings, followed by new input for number of roads");
      }
    }
    output = pr.execute("5").toLowerCase();
    lines = output.split("\s*[\r\n]+\s*");

    if (lines.length != 1) {
      return CheckResult.wrong("There should be exactly 1 line printed when the user inputted desired number of " +
              "roads, " +
              "but there were " + lines.length + " instead");
    }

    if (!lines[0].contains("input") || !lines[0].contains("interval")) {
      return CheckResult.wrong("When the user provided number of roads, there should be a line, that asks user to " +
              "input " +
              "interval value with \"Input\" and \"Interval\" substrings");
    }

    for (String ex : new String[]{"asd", "-1", "6-", "0", "Hello world!"}) {
      output = pr.execute(ex).toLowerCase();
      lines = output.split("\s*[\r\n]+\s*");
      if (lines.length != 1 || !lines[0].contains("incorrect input") || !lines[0].contains("again")) {
        return CheckResult.wrong("When the user provides incorrect input for interval value (<=0 or not " +
                "numeric), there should be printed exactly one line, containing \"incorrect input\" and " +
                "\"again\" substrings, followed by new input for interval value");
      }
    }
    output = pr.execute("5").toLowerCase();

    CheckMenu(output.split("\s*[\r\n]+\s*"), "Start of the program after correct input for initial settings");

    pr.execute("0");
    if (!pr.isFinished()) {
      return CheckResult.wrong("After user inputted '0' as a desired option, program should finish it's execution");
    }

    return CheckResult.correct();
  }

  @DynamicTest(order = 3)
  CheckResult test_incorrect_options() {
    ForStages(new int[]{3, 4, 5, 6});
    TestedProgram pr = new TestedProgram();
    pr.start();

    for (String s : new String[]{"5", "3"})
      pr.execute(s);

    String output;
    String[] lines;

    for (String ex : new String[]{"asd", "-1", "6-", "Hello world!", "4", "5"}) {
      output = pr.execute(ex).toLowerCase();
      lines = output.split("\s*[\r\n]+\s*");
      if (lines.length != 1 || !lines[0].contains("incorrect option")) {
        return CheckResult.wrong("When the user provides incorrect input while choosing an option (not '1', '2' or " +
                "'3'), there should be printed exactly one line, containing \"incorrect option\" " +
                "substring, followed by input to return back to menu");
      }
      output = pr.execute("").toLowerCase();
      CheckMenu(output.split("\s*[\r\n]+\s*"), "New iteration after incorrect input for option");
    }

    pr.execute("0");
    if (!pr.isFinished()) {
      return CheckResult.wrong("After user inputted '0' as a desired option, program should finish it's execution");
    }

    return CheckResult.correct();
  }

  String[] settings = {"1", "24367587"};

  @DynamicTest(data = "settings", order = 4)
  CheckResult test_system_info(String init) {
    ForStages(new int[]{4, 5, 6});
    TestedProgram pr = new TestedProgram();
    pr.start();

    for (String s : new String[]{init, init})
      pr.execute(s);

    Thread usersThread = GetUsersThreadByName("QueueThread");

    pr.execute("3");

    List<String> outputs = GetSystemOutputInSeconds(pr, 4);

    int prevSeconds = -1;
    for (String info : outputs) {
      SystemOutput soInfo = GetSystemInfo(info, 0, false);
      prevSeconds = ProcessSystemSecondsInitial(soInfo, prevSeconds, Integer.parseInt(init), Integer.parseInt(init));
    }

    String output = pr.execute("").toLowerCase();
    String[] lines = output.split("\s*[\r\n]+\s*");
    CheckMenu(lines, "Pressed \"Enter\" to return from system mode");

    pr.execute("3");
    pr.getOutput();

    String newOutput = GetSystemOutputInSeconds(pr, 1).get(0);
    SystemOutput soInfo = GetSystemInfo(newOutput, 0, false);
    ProcessSystemSecondsInitial(soInfo, prevSeconds, Integer.parseInt(init), Integer.parseInt(init));

    pr.execute("");
    pr.execute("0");

    if (!pr.isFinished()) {
      return CheckResult.wrong("After user inputted '0' as a desired option, program should finish it's " +
              "execution");
    }

    try {
      usersThread.join(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    if (usersThread.isAlive()) {
      return CheckResult.wrong("You should kill the created thread when the program is finished");
    }

    return CheckResult.correct();
  }

  //init, action, resultType
  // 1 - queue is full
  // 2 - queue is empty
  // 3 - add
  // 4 - delete
  Object[][] dataForActions = {
          {"1", new String[]{"2", "1", "1", "2", "1"}, new int[]{2, 3, 1, 4, 3}},
          {"2", new String[]{"2", "1", "1", "1", "2", "2", "2", "1"}, new int[]{2, 3, 3, 1, 4, 4, 2, 3}}
  };

  @DynamicTest(data = "dataForActions", order = 5)
  CheckResult test_roads_menu_output(String init, String[] actions, int[] result) {
    ForStages(new int[]{5, 6});
    TestedProgram pr = new TestedProgram();
    pr.start();

    for (String s : new String[]{init, init})
      pr.execute(s);

    List<String> names = new ArrayList<>();

    for (int i = 0; i < actions.length; i++) {
      String output = pr.execute(actions[i]).toLowerCase();
      String[] lines = output.split("\s*[\r\n]+\s*");
      if (actions[i].equals("1")) {
        if (lines.length != 1 || !lines[0].contains("input")) {
          return CheckResult.wrong("When the user selected '1' as an option, program should print " +
                  "exactly 1 line, that contains \"input\" substring, followed by new input for " +
                  "element's name");
        }
        output = pr.execute(name + i).toLowerCase();
        lines = output.split("\s*[\r\n]+\s*");
      }
      switch (result[i]) {
        case 1: //QUEUE IS FULL
          if (lines.length != 1 || !output.contains("queue is full"))
            return CheckResult.wrong("When the user selected '1' as an option and provided new road's name, " +
                    "while queue is full, program should print exactly 1 line, that contains \"queue is full\" " +
                    "substring.");
          break;
        case 2: //QUEUE IS EMPTY
          if (lines.length != 1 || !output.contains("queue is empty"))
            return CheckResult.wrong("When the user selected '2' as an option, while queue is empty, " +
                    "program should print exactly 1 line, that contains \"queue is empty\" substring.");
          break;
        case 3: //ADD
          if (lines.length != 1 || !output.contains("add") || !output.contains(name + i))
            return CheckResult.wrong("When the user selected '1' as an option and successfully added new road, " +
                    "program should print exactly 1 line, that contains road's name and \"add\" substrings.");
          names.add(name + i);
          break;
        case 4: //DELETE
          if (lines.length != 1 || !output.contains("delete") || !output.contains(names.get(0)))
            return CheckResult.wrong("When the user selected '2' as an option and successfully removed a road, " +
                    "program should print exactly 1 line, that contains road's name and \"delete\" substrings.");
          names.remove(0);
          break;
      }
      CheckMenu(pr.execute("").toLowerCase().split("\s*[\r\n]+\s*"),
              "New iteration after attempt to delete/add a road");
    }
    pr.execute("0");

    if (!pr.isFinished()) {
      return CheckResult.wrong("After user inputted '0' as a desired option, program should finish it's " +
              "execution");
    }
    return CheckResult.correct();
  }


  @DynamicTest(data = "dataForActions", order = 6)
  CheckResult test_system_info_with_roads(String init, String[] actions, int[] result) {
    ForStages(new int[]{5, 6});
    TestedProgram pr = new TestedProgram();
    pr.start();

    for (String s : new String[]{init, init})
      pr.execute(s);

    List<String> names = new ArrayList<>();

    for (int i = 0; i < actions.length; i++) {
      pr.execute(actions[i]);
      if (actions[i].equals("1")) {
        pr.execute(name + i);
      }
      if (result[i] == 3)
        names.add(name + i);
      if (result[i] == 4)
        names.remove(0);
      pr.execute("");

      pr.execute("3");
      String output = GetSystemOutputInSeconds(pr, 1).get(0);
      SystemOutput info = GetSystemInfo(output, names.size(), false);

      if (info.roadLines.size() != names.size()) {
        return CheckResult.wrong("The amount of printed road lines from the system output is incorrect.");
      }

      for (int j = 0; j < names.size(); j++) {
        if (!info.roadLines.get(j).line.contains(names.get(j))) {
          return CheckResult.wrong("Between settings lines and the line, that asks user to press Enter to show " +
                  "options, there should be printed all elements in queue from front to rear, containing " +
                  "elements' names in such order.");
        }
      }

      pr.execute("");
    }
    pr.execute("0");

    if (!pr.isFinished()) {
      return CheckResult.wrong("After user inputted '0' as a desired option, program should finish it's " +
              "execution.");
    }
    return CheckResult.correct();
  }

  @DynamicTest(data = "finalActionsSimple", order = 7)
  CheckResult test_roads_conditions_simple(int maxRoads, int interval, boolean reveal, String[] correct) {
    ForStages(new int[]{6});

    TestedProgram pr = new TestedProgram();
    pr.start();

    for (String s : new String[]{String.valueOf(maxRoads), String.valueOf(interval)})
      pr.execute(s);

    for (int i = 0; i < maxRoads; i++) {
      pr.execute("1");
      pr.execute(name + i);
      pr.execute("");
    }

    pr.execute("3");
    List<String> outputs = GetSystemOutputInSeconds(pr, interval * (maxRoads + 1));

    pr.execute("");
    pr.execute("0");

    if (!pr.isFinished()) {
      return CheckResult.wrong("After user inputted '0' as a desired option, program should finish it's " +
              "execution.");
    }

    if (outputs.size() < correct.length) {
      return CheckResult.wrong("Incorrect number of system outputs was found. Make sure, that system prints new info" +
              " each second");
    }

    SystemOutput previousUsers = null;

    for (int i = 0; i < correct.length; i++) {
      previousUsers = ProcessConditions(outputs.get(i), correct[i], previousUsers, maxRoads, interval, reveal,
              previousUsers == null ? "Added " + maxRoads + " roads." : "Waited 1 second.");
    }

    return CheckResult.correct();
  }

  @DynamicTest(data = "finalActionsAdvanced", order = 8)
  CheckResult test_roads_conditions_advanced(int maxRoads, int interval, boolean reveal, String[] correct) {
    ForStages(new int[]{6});

    TestedProgram pr = new TestedProgram();
    pr.start();
    for (String s : new String[]{String.valueOf(maxRoads), String.valueOf(interval)})
      pr.execute(s);

    SystemOutput previousUsers = null;

    int amountOfRoads = 0;

    int j = 0;
    for (int i = 0; i < maxRoads; i++) {
      pr.execute("1");
      pr.execute(name + i);
      pr.execute("");
      pr.execute("3");

      String output = GetSystemOutputInSeconds(pr, 1).get(0);
      previousUsers = ProcessConditions(output, correct[j++], previousUsers, ++amountOfRoads, interval, reveal,
              previousUsers == null ? "Added 1 road." : "Added 1 road. Waited 1 second.");
      pr.execute("");
    }

    pr.execute("3");
    String output = GetSystemOutputInSeconds(pr, 1).get(0);
    previousUsers = ProcessConditions(output, correct[j++], previousUsers, amountOfRoads, interval, reveal,
            "Waited 1 second.");
    pr.execute("");

    for (int i = 0; i < maxRoads; i++) {
      pr.execute("2");
      pr.execute("");
      pr.execute("3");

      List<String> outputs = GetSystemOutputInSeconds(pr, 2);
      amountOfRoads--;
      previousUsers = ProcessConditions(outputs.get(0), correct[j++], previousUsers, amountOfRoads, interval, reveal,
              "Deleted 1 road. Waited 1 second.");
      previousUsers = ProcessConditions(outputs.get(1), correct[j++], previousUsers, amountOfRoads, interval, reveal,
              "Waited 1 second.");
      pr.execute("");
    }
    pr.execute("0");

    return CheckResult.correct();
  }

  String revealTest(SystemOutput previousUsers, SystemOutput usersOutput, String correct, String actionInBetween,
                    int interval, boolean reveal) {
    if (!reveal)
      return "";
    String[] correctRoads = correct.split(";");
    if (previousUsers == null) {
      actionInBetween = "Started. " + actionInBetween;
    }
    String output = "---Interval: " + interval + "---\n";
    if (previousUsers != null) {
      output = output.concat("...\n");
      for (Road r : previousUsers.roadLines) {
        output = output.concat(r.toString() + "\n");
      }
    }
    output = output.concat("---Performed action: " + actionInBetween + "---\n");

    String expected = "", got = "";
    for (int j = 0; j < usersOutput.roadLines.size(); j++) {
      String[] data = correctRoads[j].split(",");
      String gotOutput = usersOutput.roadLines.get(j).toString() + "\n";
      int gotSeconds = usersOutput.roadLines.get(j).seconds;
      got = got.concat(gotOutput);

      if (usersOutput.roadLines.get(j).isOpen() != data[0].equals("1")) {
        gotOutput = gotOutput.replace("closed", "^*#")
                .replace("open", "closed")
                .replace("^*#", "open");
      }
      if (gotSeconds != Integer.parseInt(data[1])) {
        gotOutput = gotOutput.replace(gotSeconds + "s.", data[1] + "s.");
      }
      expected = expected.concat(gotOutput);
    }
    if (expected.equals("")) {
      expected = "(No roads)\n";
    }
    if (got.equals("")) {
      got = "(No roads)\n";
    }
    return " Formal snippet of expected/got output:\n" + output + "---Expected:---\n" + expected + "---Got:---\n" + got;
  }


  //maxRoads, interval, reveal, correct
  //2 roads, with interval 2
  //3 roads, with interval 1
  //4 roads, with interval 3
  //4 roads, with interval 1
  Object[][] finalActionsSimple = {
          {2, 2, true, new String[]{"1,2;0,2", "1,1;0,1", "0,2;1,2", "0,1;1,1", "1,2;0,2", "1,1;0,1"}},
          {2, 1, true, new String[]{"1,1;0,1", "0,1;1,1", "1,1;0,1"}},
          {3, 3, true, new String[]{"1,3;0,3;0,6", "1,2;0,2;0,5", "1,1;0,1;0,4",
                  "0,6;1,3;0,3", "0,5;1,2;0,2", "0,4;1,1;0,1",
                  "0,3;0,6;1,3", "0,2;0,5;1,2", "0,1;0,4;1,1",
                  "1,3;0,3;0,6", "1,2;0,2;0,5", "1,1;0,1;0,4"}
          },
          {3, 1, true, new String[]{"1,1;0,1;0,2", "0,2;1,1;0,1", "0,1;0,2;1,1", "1,1;0,1;0,2"}}
  };
  //2 roads, with interval 2, 1 road, 1 seconds, 1 road, 2 seconds, remove road, 2 seconds, remove road, 2 seconds
  //3 roads, with interval 3, 1 road, 1 seconds, 1 road, 1 seconds, 1 road, 2 seconds, remove road, 2 seconds, remove
  // road, 2 seconds, remove road, 2 seconds
  Object[][] finalActionsAdvanced = {
          {2, 2, true, new String[]{"1,2", "1,1;0,1", "0,2;1,2", "1,1", "1,2", "", ""}},
          {3, 3, true, new String[]{"1,3", "1,2;0,2", "1,1;0,1;0,4", "0,6;1,3;0,3", "1,2;0,2", "1,1;0,1", "1,3",
                  "1,2", "", ""}
          }
  };
}