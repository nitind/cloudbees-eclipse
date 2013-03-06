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
package com.cloudbees.eclipse.ui;

import org.eclipse.osgi.util.NLS;

/**
 * Translation/i18n main class. See messages.properties at the same package for localization.
 * 
 * @author ahtik
 */
public final class Messages extends NLS {

  private static final String BUNDLE_NAME = "com.cloudbees.eclipse.ui.messages"; //$NON-NLS-1$

  static {
    NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  }

  public static String pref_description;

  public static String pref_group_login;
  public static String pref_email;
  public static String pref_password;
  public static String pref_signup;
  public static String pref_signup_tooltip;
  public static String pref_validate_login;

  public static String pref_group_devAtCloud;
  public static String pref_enable_jaas;
  public static String pref_enable_forge;
  public static String pref_attach_jenkins;
  public static String pref_attach_jenkins_tooltip;
  public static String pref_group_allJenkins;
  public static String pref_jenkins_refresh_interval;
  public static String pref_jenkins_refresh_enabled;

  public static String git_access_reminder;
}
