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
