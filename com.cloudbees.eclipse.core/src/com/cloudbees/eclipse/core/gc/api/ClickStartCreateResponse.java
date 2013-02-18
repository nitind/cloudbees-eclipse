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
  public String foprgeUrl;
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
