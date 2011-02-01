package com.cloudbees.eclipse.core.nectar.api;

public class Case extends SuperCase {

  static {
    System.out.println("Case-static");
  }

  public static void getMe() {
    System.out.println("GetMe");
    callable();
  }

}

