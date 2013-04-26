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
package com.cloudbees.eclipse.dtp.internal;

import com.cloudbees.api.DatabaseInfo;
import com.cloudbees.api.DatabaseListResponse;

public interface DatabaseStatusUpdate {
  public void update(String id, String status, DatabaseInfo info);

  public void update(DatabaseListResponse response);
}
