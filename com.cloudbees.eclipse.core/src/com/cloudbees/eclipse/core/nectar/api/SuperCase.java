package com.cloudbees.eclipse.core.nectar.api;

abstract class SuperCase {

  static {
    System.out.println("SuperCase-static");
  }

  static void callable() {
    System.out.println("callable");
  }

}
