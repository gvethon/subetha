package org.subethamail.smtp.command;

import org.subethamail.smtp.session.Session;

/**
 * @author Ian McFarland &lt;ian@neo.com&gt;
 */
public class ResetCommand extends BaseCommand {
  public ResetCommand(CommandDispatcher commandDispatcher) {
    super(commandDispatcher, "RSET");
    helpMessage = new HelpMessage("RSET", "Resets the system.");
  }

  @Override
  public String execute(String commandString, Session session) {
    session.reset();
    return "250 Reset state";
  }

}
