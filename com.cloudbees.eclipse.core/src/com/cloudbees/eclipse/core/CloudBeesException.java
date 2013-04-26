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
package com.cloudbees.eclipse.core;

public class CloudBeesException extends Exception {

  private static final long serialVersionUID = -6129647305172827815L;

  public CloudBeesException(String msg, Throwable t) {
    super(msg, t);
  }

  public CloudBeesException(Throwable t) {
    super(t);
  }

  public CloudBeesException(String msg) {
    super(msg);
  }

}
