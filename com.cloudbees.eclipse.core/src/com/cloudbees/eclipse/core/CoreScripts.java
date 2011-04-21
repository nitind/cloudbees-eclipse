package com.cloudbees.eclipse.core;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

public class CoreScripts {

  public static File getMockConfigXML() throws IOException {
    Bundle bundle = CloudBeesCorePlugin.getDefault().getBundle();
    Path path = new Path("scripts/config.xml");
    return new File(FileLocator.toFileURL(FileLocator.find(bundle, path, null)).getFile());
  }

}
