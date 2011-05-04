package com.cloudbees.eclipse.core.forge.api;

public class ForgeInstance {

  public static enum TYPE {
    SVN, GIT, CVS
  }

  public static enum STATUS {
    UNKNOWN("Unknown"), SYNCED("Synced"), SKIPPED("Skipped"), CANCELLED("Cancelled");

    private final String label;

    private STATUS(final String label) {
      this.label = label;
    }

    public String getLabel() {
      return this.label;
    }
  }

  public String url;
  public String user;
  public String account;
  public TYPE type;
  public STATUS status = STATUS.UNKNOWN;

  public ForgeInstance(final String url, final String user, final TYPE type) {
    this.url = url;
    this.user = user;
    this.type = type;
  }

}
