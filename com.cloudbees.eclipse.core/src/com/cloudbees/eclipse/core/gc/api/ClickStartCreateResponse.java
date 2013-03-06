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

import com.google.gson.annotations.SerializedName;

/*
 * 
{
   "name":"Node.js web server",
   "description":"Deploy a node.js application on CloudBees with continuous deployment (beta)",
   "icon":"https://d3ko533tu1ozfq.cloudfront.net/clickstart/nodejs_large.png",
   "id":"https://raw.github.com/CloudBees-community/nodejs-clickstart/master/clickstart.json",
   "appUrl":"http://nodejspn1.michaelnealeclickstart2.cloudbees.net",
   "source":"ssh://git@git.cloudbees.com/michaelnealeclickstart2/nodejspn1.git",
   "forgeUrl":"https://michaelnealeclickstart2.forge.cloudbees.com",
   "appManageUrl":"https://run.cloudbees.com/a/michaelnealeclickstart2#app-manage/development:michaelnealeclickstart2/nodejspn1",
   "jenkinsUrl":"https://michaelnealeclickstart2.ci.cloudbees.com//job/nodejspn1",
   "dbManageUrl":null,
   "components":[
      {
         "name":"Source repository",
         "icon":"https://s3.amazonaws.com/cloudbees-downloads/clickstart/repo.png",
         "description":"Git source repository",
         "url":"ssh://git@git.cloudbees.com/michaelnealeclickstart2/nodejspn1.git",
         "managementUrl":"https://michaelnealeclickstart2.forge.cloudbees.com",
         "key":"Source_repository"
      },
      {
         "name":"Jenkins build",
         "icon":"https://s3.amazonaws.com/cloudbees-downloads/clickstart/jenkins.png",
         "description":"Build service",
         "url":null,
         "managementUrl":"https://michaelnealeclickstart2.ci.cloudbees.com//job/nodejspn1",
         "key":"Jenkins_build"
      },
      {
         "name":"Web Application nodejspn1",
         "icon":"https://s3.amazonaws.com/cloudbees-downloads/clickstart/apps.png",
         "description":"Deployed application nodejspn1",
         "url":"http://nodejspn1.michaelnealeclickstart2.cloudbees.net",
         "managementUrl":"https://run.cloudbees.com/a/michaelnealeclickstart2#app-manage/development:michaelnealeclickstart2/nodejspn1",
         "key":"Web_Application_nodejspn1"
      }
   ],
   "doc-url":null,
   "reservation_id":"http://nodejspn1.michaelnealeclickstart2.cloudbees.net"
}
 */
public class ClickStartCreateResponse extends ClickStartResponseBase {
  
  public String name;
  public String description;
  public String icon;
  public String id;
  public String appUrl;
  public String source;
  public String forgeUrl;
  public String appManagerUrl;
  public String jenkinsUrl;
  public String dbManagerUrl;
  public Component[] components;
  @SerializedName("doc-url") public String docUrl;
  @SerializedName("reservation_id")  public String reservationId;
  
  
  public static class Component {
    public String name;
    public String icon;
    public String description;
    public String url;
    public String managementUrl;
    public String key;
  }
  
  
}
