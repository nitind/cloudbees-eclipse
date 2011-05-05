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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
    result = prime * result + ((this.url == null) ? 0 : this.url.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof ForgeInstance)) {
      return false;
    }
    ForgeInstance other = (ForgeInstance) obj;
    if (this.type != other.type) {
      return false;
    }
    if (this.url == null) {
      if (other.url != null) {
        return false;
      }
    } else if (!this.url.equals(other.url)) {
      return false;
    }
    return true;
  }

}