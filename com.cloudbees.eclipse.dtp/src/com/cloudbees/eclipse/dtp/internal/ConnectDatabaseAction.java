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
package com.cloudbees.eclipse.dtp.internal;

import java.io.File;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.datatools.connectivity.ConnectionProfileConstants;
import org.eclipse.datatools.connectivity.IConnectionProfile;
import org.eclipse.datatools.connectivity.ProfileManager;
import org.eclipse.datatools.connectivity.drivers.DriverInstance;
import org.eclipse.datatools.connectivity.drivers.DriverManager;
import org.eclipse.datatools.connectivity.drivers.IDriverMgmtConstants;
import org.eclipse.datatools.connectivity.drivers.IPropertySet;
import org.eclipse.datatools.connectivity.drivers.PropertySetImpl;
import org.eclipse.datatools.connectivity.drivers.XMLFileManager;
import org.eclipse.datatools.connectivity.drivers.jdbc.IJDBCConnectionProfileConstants;
import org.eclipse.datatools.connectivity.drivers.models.TemplateDescriptor;
import org.eclipse.datatools.connectivity.internal.ConnectivityPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.ObjectPluginAction;

import com.cloudbees.api.DatabaseInfo;
import com.cloudbees.eclipse.core.CloudBeesException;
import com.cloudbees.eclipse.dtp.CloudBeesDataToolsPlugin;
import com.cloudbees.eclipse.dtp.Images;
import com.cloudbees.eclipse.run.core.BeesSDK;
import com.cloudbees.eclipse.run.sdk.CBSdkActivator;
import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;
import com.cloudbees.eclipse.ui.views.CBTreeAction;

public class ConnectDatabaseAction extends CBTreeAction implements IObjectActionDelegate {

  private final static String DRIVER_INSTANCE_ID = "cloudbees-mysql-5.1-driver";
  public final static String DRIVER_DEF_ID = "DriverDefn.org.eclipse.datatools.enablement.mysql.5_1.driverTemplate.MySQL "
      + DRIVER_INSTANCE_ID;
  private final static String PROVIDER_ID = "org.eclipse.datatools.enablement.mysql.connectionProfile";

  protected static final String DTP_PERSPECTIVE = "org.eclipse.datatools.sqltools.sqleditor.perspectives.EditorPerspective";

  public ConnectDatabaseAction() {
    super(true);
    setText("Connect...");
    setToolTipText("Connect to the database");
    setImageDescriptor(CloudBeesDataToolsPlugin.getImageDescription(Images.CLOUDBEES_REFRESH));
  }

  public static void connect() {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Loading Database info") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {

        monitor.beginTask("Loading Database info", 100);
        try {
          CloudBeesDataToolsPlugin.getPoller().fetchAndUpdateDatabases(monitor);
          monitor.worked(75);
          CloudBeesUIPlugin.getDefault().fireDatabaseInfoChanged();
          monitor.worked(25);

          return Status.OK_STATUS;
        } catch (Exception e) {
          String msg = e.getLocalizedMessage();
          if (e instanceof CloudBeesException) {
            e = (Exception) e.getCause();
          }
          CloudBeesDataToolsPlugin.getDefault().getLogger().error(msg, e);
          return new Status(IStatus.ERROR, CloudBeesDataToolsPlugin.PLUGIN_ID, 0, msg, e);
        } finally {
          monitor.done();
        }
      }
    };

    job.setUser(true);
    job.schedule();

  }

  @Override
  public void run(IAction action) {
    if (action instanceof ObjectPluginAction) {
      ObjectPluginAction pluginAction = (ObjectPluginAction) action;
      ISelection selection = pluginAction.getSelection();

      if (selection instanceof IStructuredSelection) {
        IStructuredSelection structSelection = (IStructuredSelection) selection;
        Object element = structSelection.getFirstElement();

        if (element instanceof DatabaseInfo) {
          DatabaseInfo db = (DatabaseInfo) element;
          connectNewDatabase(db);
          showDataExplorer();
        }
      }
    }

  }

  public final static void showDataExplorer() {
    CloudBeesDataToolsPlugin.getDefault().getWorkbench().getDisplay().asyncExec(new Runnable() {
      @Override
      public void run() {
        try {
          IWorkbenchPage activePage = CloudBeesUIPlugin.getActiveWindow().getActivePage();
          if (activePage.getPerspective().getId() != DTP_PERSPECTIVE) {
            CloudBeesUIPlugin.getActiveWindow().getWorkbench()
                .showPerspective(DTP_PERSPECTIVE, CloudBeesUIPlugin.getActiveWindow());
          }

          activePage.showView("org.eclipse.datatools.connectivity.DataSourceExplorerNavigator");
        } catch (Exception e) {
          CloudBeesUIPlugin.showError("Failed to show Data Source Explorer", e);
        }
      }
    });

  }

  @Override
  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
  }

  @Override
  public boolean isPopup() {
    return false;
  }

  @Override
  public boolean isPullDown() {
    return true;
  }

  @Override
  public boolean isToolbar() {
    return false;
  }

  public final static void connectNewDatabase(final DatabaseInfo dbinfo) {
    org.eclipse.core.runtime.jobs.Job job = new org.eclipse.core.runtime.jobs.Job("Configuring and connecting to '"
        + dbinfo.getName() + "'") {
      @Override
      protected IStatus run(final IProgressMonitor monitor) {

        monitor.beginTask("Configuring", 100);
        try {

          internalConnectDb(dbinfo, monitor);

          return Status.OK_STATUS;
        } catch (Exception e) {
          String msg = e.getLocalizedMessage();
          if (e instanceof CloudBeesException) {
            e = (Exception) e.getCause();
          }
          CloudBeesDataToolsPlugin.getDefault().getLogger().error(msg, e);
          return new Status(IStatus.ERROR, CloudBeesDataToolsPlugin.PLUGIN_ID, 0, msg, e);
        } finally {
          monitor.done();
        }
      }

    };

    job.setUser(true);
    job.schedule();

  }

  private static void internalConnectDb(DatabaseInfo dbinfo, IProgressMonitor monitor) throws Exception {

    DatabaseInfo db;
    monitor.subTask("Fetching credentials");
    db = BeesSDK.getDatabaseInfo(dbinfo.getName(), true);
    monitor.worked(10);
    /*      DriverInstance[] instances = DriverManager.getInstance().getAllDriverInstances();
          System.out.println("Listing driver instances:");

          for (DriverInstance i : instances) {
            System.out.println("id:" + i.getId() + "; jarlist:" + i.getJarList() + "; name:" + i.getName());
            System.out.println(" instance props: " + i.getPropertySet().getBaseProperties());

          }
    */
    // id:DriverDefn.org.eclipse.datatools.enablement.mysql.5_0.driverTemplate.MySQL JDBC Driver test 1;
    // jarlist:C:\Java\mysql-connector-java-5.0.8-bin.jar;
    // name:MySQL JDBC Driver test 1

    // instance props: {
    // org.eclipse.datatools.connectivity.drivers.defnType=org.eclipse.datatools.enablement.mysql.5_0.driverTemplate,
    // jarList=C:\Java\mysql-connector-java-5.0.8-bin.jar,
    // org.eclipse.datatools.connectivity.db.username=root,
    // org.eclipse.datatools.connectivity.db.driverClass=com.mysql.jdbc.Driver,
    // org.eclipse.datatools.connectivity.db.databaseName=database,
    // org.eclipse.datatools.connectivity.db.password=,
    // org.eclipse.datatools.connectivity.db.URL=jdbc:mysql://localhost:3306/database,
    // org.eclipse.datatools.connectivity.db.version=5.0,
    // org.eclipse.datatools.connectivity.db.vendor=MySql}

    // ensure a cloudbees-created driver exists

    monitor.subTask("Validating driver definition");
    reloadDriverDefinition();
    monitor.worked(10);
    //
    // create connection profile

    //Profile: New MySQL; profId=org.eclipse.datatools.enablement.mysql.connectionProfile; 8151b550-8162-11e2-a508-e9b8c18b9243
    //.. props: {
    // org.eclipse.datatools.connectivity.db.connectionProperties=,
    // org.eclipse.datatools.connectivity.db.savePWD=true,
    // org.eclipse.datatools.connectivity.drivers.defnType=org.eclipse.datatools.enablement.mysql.5_0.driverTemplate,
    // jarList=C:\Java\mysql-connector-java-5.0.8-bin.jar,
    // org.eclipse.datatools.connectivity.db.username=w125uaajcavrn,
    // org.eclipse.datatools.connectivity.db.driverClass=com.mysql.jdbc.Driver,
    // org.eclipse.datatools.connectivity.db.databaseName=w125uaajcavrn,
    // org.eclipse.datatools.connectivity.driverDefinitionID=DriverDefn.org.eclipse.datatools.enablement.mysql.5_0.driverTemplate.MySQL JDBC Driver test 1,
    // org.eclipse.datatools.connectivity.db.password=a89e534b9fbc4d6abf015f9ab101b48b,
    // org.eclipse.datatools.connectivity.db.URL=jdbc:mysql://ec2-50-19-213-178.compute-1.amazonaws.com:3306/w125uaajcavrn,
    // org.eclipse.datatools.connectivity.db.version=5.0,
    // org.eclipse.datatools.connectivity.db.vendor=MySql}            
    //}

    String pName = db.getOwner() + "/" + db.getName();

    IConnectionProfile exp = ProfileManager.getInstance().getProfileByName(pName);
    if (exp != null) {
      monitor.subTask("Connection profile already existed. Connecting to '" + pName + "'");
      monitor.worked(30);
      exp.connect();
      monitor.worked(100);
      monitor.done();
      return;
    }

    /*          IConnectionProfile[] profiles = ProfileManager.getInstance().getProfiles();
              System.out.println("Listing profiles:");
              for (int i = 0; i < profiles.length; i++) {
                IConnectionProfile p = profiles[i];
                System.out.println("Profile: " + p.getName() + "; profId=" + p.getProviderId() + "; " + p.getInstanceID());
                System.out.println(".. props: " + p.getBaseProperties());

              }
    */
    //          IConnectionProfile newprofile;
    //        Properties props = new Properties();
    //try {
    //ProfileManager.getInstance().createProfile(db.getName(), "Automatically created connection profile for account "+db.getOwner(), PROVIDER_ID, props);
    //} catch (ConnectionProfileException e) {
    // TODO Auto-generated catch block
    //  e.printStackTrace();
    //}

    /*          baseProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_JARLIST,
                  "C:\\Java\\mysql-connector-java-5.0.8-bin.jar");
              baseProperties.setProperty(IJDBCConnectionProfileConstants.DRIVER_CLASS_PROP_ID, "com.mysql.jdbc.Driver");
    */
    Properties baseProperties = getBaseProps();
    baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_NAME_PROP_ID, db.getName());
    baseProperties.setProperty(IJDBCConnectionProfileConstants.URL_PROP_ID,
        "jdbc:mysql://" + db.getMaster() + ":" + db.getPort() + "/" + db.getName());
    baseProperties.setProperty(IJDBCConnectionProfileConstants.USERNAME_PROP_ID, db.getUsername());

    baseProperties.setProperty(IJDBCConnectionProfileConstants.PASSWORD_PROP_ID, db.getPassword());

    baseProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_TYPE,
        "org.eclipse.datatools.enablement.mysql.5_0.driverTemplate");

    baseProperties.setProperty(ConnectionProfileConstants.PROP_DRIVER_DEFINITION_ID, DRIVER_DEF_ID);

    baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_NAME_PROP_ID, db.getName());

    //baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_VENDOR_PROP_ID, "MySql");
    //baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_VERSION_PROP_ID, "5.0");
    //baseProperties.setProperty(IJDBCConnectionProfileConstants.SAVE_PASSWORD_PROP_ID, String.valueOf(true));

    monitor.subTask("Creating new connection profile");
    monitor.worked(20);
    IConnectionProfile profile = ProfileManager.getInstance().createProfile(pName,
        "Automatically created connection profile for account " + db.getOwner(), PROVIDER_ID, baseProperties);

    monitor.subTask("Connecting to '" + pName + "'");
    profile.connect();
    monitor.done();
  }

  public final static void reloadDriverDefinition() {
    Properties baseProperties = getBaseProps();
    DriverInstance driver = DriverManager.getInstance().getDriverInstanceByID(DRIVER_DEF_ID);
    if (driver == null) {
      /*            Properties pr = new Properties();
                  pr.put("jarList", "C:\\Java\\mysql-connector-java-5.0.8-bin.jar");
                  pr.put("org.eclipse.datatools.connectivity.drivers.defnType",
                      "org.eclipse.datatools.enablement.mysql.5_0.driverTemplate");
                  pr.put("org.eclipse.datatools.connectivity.db.driverClass", "com.mysql.jdbc.Driver");
                  pr.put("org.eclipse.datatools.connectivity.db.databaseName", "database");
                  pr.put("org.eclipse.datatools.connectivity.db.version", "5.0");
                  pr.put("org.eclipse.datatools.connectivity.db.vendor", "MySql");
      */

      IPropertySet ips = new PropertySetImpl("CloudBees Driver Definition for MySQL 5.1", DRIVER_DEF_ID);
      ips.setBaseProperties(baseProperties);
      DriverInstance di = new DriverInstance(ips);
      DriverManager.getInstance().addDriverInstance(di);

      //OverrideTemplateDescriptor.getByDriverTemplate(driverTemplateId)
      //di.getPropertySet().setBaseProperties(baseProperties);

      //DriverInstance ndri = DriverManager.getInstance().createNewDriverInstance(DRIVER_INSTANCE_ID, "CloudBees MySQL Driver", "C:\\Java\\mysql-connector-java-5.0.8-bin.jar", "com.mysql.jdbc.Driver");

    } else {
      // already exists, update jar location if needed
      IPropertySet ps = driver.getPropertySet();
      ps.setBaseProperties(baseProperties);

      //XMLFileManager.setFileName(IDriverMgmtConstants.DRIVER_FILE);
      //XMLFileManager.saveNamedPropertySet(new IPropertySet[]{ps});

      // force property reload by creating&removing a dummy template. didn't find a safer way to guarantee reload
      IPropertySet ips = new PropertySetImpl("tempCloudBees Driver Definition for MySQL 5.1", DRIVER_DEF_ID + "dummy");
      ips.setBaseProperties(baseProperties);
      DriverInstance di = new DriverInstance(ips);
      DriverManager.getInstance().addDriverInstance(di);
      DriverManager.getInstance().removeDriverInstance(di.getId());

      //DriverManager.getInstance().resetDefaultInstances();

      /*      TemplateDescriptor types[] = TemplateDescriptor
                .getDriverTemplateDescriptors();
            for (TemplateDescriptor templ : types) {
            }
      */
      // workaround to trigger driver instances reload      
      DriverManager.getInstance().resetDefaultInstances();

      //addDriverInstances(new IPropertySet[]{});

      //ProfileManager.getInstance().modifyProfile(profile);

    }

  }

  private static Properties getBaseProps() {
    Properties baseProperties = new Properties();
    baseProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_TYPE,
        "org.eclipse.datatools.enablement.mysql.5_1.driverTemplate");

    baseProperties.setProperty(IDriverMgmtConstants.PROP_DEFN_JARLIST, getJarList());
    baseProperties.setProperty(IJDBCConnectionProfileConstants.DRIVER_CLASS_PROP_ID, "com.mysql.jdbc.Driver");

    //baseProperties.setProperty(ConnectionProfileConstants.PROP_DRIVER_DEFINITION_ID, "DriverDefn.org.eclipse.datatools.enablement.mysql.5_0.driverTemplate.MySQL JDBC Driver test 1");

    baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_VENDOR_PROP_ID, "MySql");
    baseProperties.setProperty(IJDBCConnectionProfileConstants.DATABASE_VERSION_PROP_ID, "5.1");
    baseProperties.setProperty(IJDBCConnectionProfileConstants.SAVE_PASSWORD_PROP_ID, String.valueOf(true));
    return baseProperties;
  }

  private static String getJarList() {
    String dirs = CBSdkActivator.getDefault().getBeesHome();

    dirs = dirs.replace('/', File.separator.charAt(0));

    if (!dirs.endsWith(File.separator)) {
      dirs = dirs + File.separator;
    }

    return dirs + "lib" + File.separator + "mysql-connector-java-5.1.15.jar";
  }

}
