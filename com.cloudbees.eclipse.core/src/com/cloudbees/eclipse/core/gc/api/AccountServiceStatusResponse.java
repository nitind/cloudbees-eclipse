/*******************************************************************************
 * Copyright (c) 2013 Cloud Bees, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *              http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
