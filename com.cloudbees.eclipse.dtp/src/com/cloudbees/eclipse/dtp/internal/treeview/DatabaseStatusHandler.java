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
package com.cloudbees.eclipse.dtp.internal.treeview;

import java.util.ArrayList;

import com.cloudbees.api.DatabaseInfo;
import com.cloudbees.api.DatabaseListResponse;
import com.cloudbees.eclipse.dtp.internal.DatabaseStatusUpdate;

public class DatabaseStatusHandler implements DatabaseStatusUpdate {

  private static final ArrayList<DatabaseStatusUpdate> listeners = new ArrayList<DatabaseStatusUpdate>();

  public DatabaseStatusHandler() {
  }

  @Override
  public void update(String id, String status, DatabaseInfo info) {
    // Don't care about single updates
  }

  @Override
  public void update(DatabaseListResponse response) {
    for (DatabaseStatusUpdate l : listeners) {
      l.update(response);
    }
  }

  public static void addListener(DatabaseStatusUpdate listener) {
    listeners.add(listener);
  }

  public static void removeListener(DatabaseStatusUpdate listener) {
    listeners.remove(listener);
  }
}
