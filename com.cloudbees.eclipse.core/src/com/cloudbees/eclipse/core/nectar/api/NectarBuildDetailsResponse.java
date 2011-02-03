package com.cloudbees.eclipse.core.nectar.api;

/**
 * Main response object for the job view
 * 
 * @author ahti
 */
public class NectarBuildDetailsResponse extends BaseNectarResponse {

  public Action[] actions;

  public final static String QTREE = QTreeFactory.create(NectarBuildDetailsResponse.class);

  public static class Action {

    // Can be null
    public Cause[] causes;

    // Can be null. junit
    public Long failCount;
    public Long skipCount;
    public Long totalCount;
    public String urlName;

    public static class Cause {
      public String shortDescription;
    }
  }

  public Artifact[] artifacts;

  public Boolean building;
  public String description;
  public String fullDisplayName;
  public String id;
  //public Boolean keepLog;
  public long number;
  public String result; // SUCCESS
  public long timestamp;
  public String url;
  public String builtOn;

  public ChangeSet changeSet;

  public static class ChangeSet {

    public ChangeSetItem[] items;
    public String kind; // hg

    public Author[] culprits;

    public static class ChangeSetItem {
      public String[] addedPaths;
      public Author author;
      public String date;
      public String[] deletedPaths;
      public String[] modifiedPaths;
      public Boolean merge;
      public String msg;
      //public String node;
      public long rev;

    }

    public static class Author {
      public String absoluteUrl;
      public String fullName;
    }

  }

  public static class Artifact {
    public String displayPath;
    public String fileName;
    public String relativePath;
  }

}
