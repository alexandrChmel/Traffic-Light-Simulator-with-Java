import org.hyperskill.hstest.exception.outcomes.WrongAnswer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SystemOutput {
  int interval, maxRoads, seconds;
  List<Road> roadLines;

  private SystemOutput(int interval, int maxRoads, int seconds, List<Road> roadLines) {
    this.interval = interval;
    this.maxRoads = maxRoads;
    this.seconds = seconds;
    this.roadLines = roadLines;
  }

  public static SystemOutput parseStringInfo(String[] lines, boolean parseRoads) {
    Pattern pattern = Pattern.compile("(\\D*)(\\d+)(\\D*)");

    Matcher matcher =pattern.matcher(lines[0]);
    if (!matcher.matches()) {
      throw new WrongAnswer("The line, that shows time since the start of the program, should contain " +
              "only one integer - amount of seconds");
    }
    int seconds = Integer.parseInt(matcher.group(2));

    matcher =pattern.matcher(lines[1]);
    if (!matcher.matches()) {
      throw new WrongAnswer("The line, that shows number of roads, provided by user, should contain " +
              "only one integer - exact number, that was set by user");
    }
    int maxRoads = Integer.parseInt(matcher.group(2));

    matcher =pattern.matcher(lines[2]);
    if (!matcher.matches()) {
      throw new WrongAnswer("The line, that shows interval, provided by user, should contain " +
              "only one integer - interval, that was set by user");
    }
    int interval = Integer.parseInt(matcher.group(2));

    List<Road> roadLines = new ArrayList<>();
    for(int i=3;i<lines.length-1;i++){
      roadLines.add(new Road(lines[i], parseRoads));
    }
    return new SystemOutput(interval, maxRoads, seconds, roadLines);
  }
}

class Road {
  String line;
  int seconds;

  Road(String line, boolean parseRoads){
    this.line = line;
    if(parseRoads) {
      if (!line.contains("open") && !line.contains("closed")) {
        throw new WrongAnswer("All lines with elements in queue should contain \"open\" or \"closed\" " +
                "substring, describing it's state.");
      }
      Pattern pattern = Pattern.compile("((?!(\\d+)s\\.).)*(\\d+)s\\.((?!(\\d+)s\\.).)*");
      Matcher matcher =pattern.matcher(line);
      if (!matcher.matches()) {
        throw new WrongAnswer("All lines with elements in queue should contain only one \"Ns.\" substring " +
                "(where N is a number) - amount of seconds until it closes/opens");
      }
      seconds = Integer.parseInt(matcher.group(3));
    }else{
      seconds = -1;
    }
  }

  boolean isOpen() {
    return line.contains("open");
  }

  @Override
  public String toString() {
    int index = line.indexOf("n3w_v3ry_unu5u4l_r04d_n4m3_");
    return "Road"+line.charAt(index+27)+", "+(isOpen()?"open":"closed")+", "+seconds+"s.";
  }
}
