package org.subethamail.smtp.command;

import org.subethamail.smtp.session.Session;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class Command {
  //TODO(imf): Extract commandRegistry into its own class. Inject it.
  private String name;
  protected HelpMessage helpMessage;
  protected CommandDispatcher commandDispatcher;

  public Command(CommandDispatcher commandDispatcher, String name) {
    this.name = name;
    this.commandDispatcher = commandDispatcher;
    try {
      commandDispatcher.add(name, this);
    } catch (InvalidCommandNameException e) {
      throw new RuntimeException("Could not instantiate improperly named command: " + name, e);
    }
  }

  public void initHelpMessage() {
    helpMessage = new HelpMessage(name, "No help for " + name + ".");
  }

  public String execute(String commandString, Session session) {
    return "500 Not Implemented";
  }

  public HelpMessage getHelp() {
    return helpMessage;
  }

  protected String getArgPredicate(String commandString) {
    return commandString.substring(4).trim();
  }

  public String getName() {
    return name;
  }

  protected boolean isValidEmailAddress(String address) {
    // TODO(imf): Make this more robust.
    return address.indexOf("@") > 0;
  }

  protected String[] getArgs(String commandString) {
    List<String> strings = new ArrayList<String>();
    StringTokenizer stringTokenizer = new StringTokenizer(commandString);
    while (stringTokenizer.hasMoreTokens()) {
      strings.add(stringTokenizer.nextToken());
    }
    return strings.toArray(new String[strings.size()]);
  }
}
