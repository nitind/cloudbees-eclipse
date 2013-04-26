/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Cloud Bees, Inc. - initial API and implementation 
 *******************************************************************************/
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
