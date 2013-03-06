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
package com.cloudbees.eclipse.run.ui.views;

import java.util.ArrayList;

import com.cloudbees.api.ApplicationInfo;
import com.cloudbees.api.ApplicationListResponse;
import com.cloudbees.eclipse.run.core.ApplicationStatusUpdater;

public class AppStatusUpdater implements ApplicationStatusUpdater {

  private static final ArrayList<ApplicationStatusUpdater> listeners = new ArrayList<ApplicationStatusUpdater>();

  public AppStatusUpdater() {
  }

  @Override
  public void update(String id, String status, ApplicationInfo info) {
    // Don't care about single updates
  }

  @Override
  public void update(ApplicationListResponse response) {
    for (ApplicationStatusUpdater l : listeners) {
      l.update(response);
    }
  }

  public static void addListener(ApplicationStatusUpdater listener) {
    listeners.add(listener);
  }

  public static void removeListener(ApplicationStatusUpdater listener) {
    listeners.remove(listener);
  }
}
