package com.cloudbees.eclipse.core.gc.api;

public class AccountServiceStatusResponse extends GCBaseResponse {

  public Boolean authorized;

  public AccountServices services;

  public String username;

  public static class AccountServices {

    public JaasService haas;
    public ForgeService forge;

    public static class ForgeService {
      public Subscription subscription;
      public Repo[] repos;

      public static class Repo {
        public String url; // ssh://git@cloudbees.forge.beescloud.com......
        public String type; // git, svn
      }

    }

    public static class JaasService {
      public Subscription subscription;
    }

    public static class Subscription {
      public String name; // Beta, Dev+ etc
      public String status; // active, inactive
    }

  }


}
