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
package com.cloudbees.eclipse.wst;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jst.j2ee.datamodel.properties.IJ2EEComponentExportDataModelProperties;
import org.eclipse.jst.j2ee.internal.archive.operations.JavaEEComponentExportOperation;
import org.eclipse.jst.j2ee.internal.web.archive.operations.WebComponentExportDataModelProvider;
import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
import org.eclipse.wst.common.frameworks.datamodel.IDataModel;

import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.run.core.WarBuilderHook;

public class DynamicWebWarBuilderHook implements WarBuilderHook {

  public DynamicWebWarBuilderHook() {
  }

  public IFile buildProject(IProject project, IProgressMonitor monitor) throws CloudBeesException {
    WebComponentExportDataModelProvider provider = new WebComponentExportDataModelProvider();

    IDataModel model = DataModelFactory.createDataModel(provider);

    model.setProperty(IJ2EEComponentExportDataModelProperties.PROJECT_NAME, project.getName());
    model.setBooleanProperty(IJ2EEComponentExportDataModelProperties.OVERWRITE_EXISTING, true);

    IFile retfile = project.getFile("/build/" + project.getName() + ".war");
    String path = retfile.getRawLocation().toFile().getAbsolutePath();
    model.setStringProperty(IJ2EEComponentExportDataModelProperties.ARCHIVE_DESTINATION, path);

    JavaEEComponentExportOperation op = new JavaEEComponentExportOperation(model);
    try {
      op.execute(monitor, null);
      if (retfile.exists()) {
        return retfile;
      }
    } catch (ExecutionException e) {
      throw new CloudBeesException("Failed to create war package", e);
    }
    return null;
  }

}
