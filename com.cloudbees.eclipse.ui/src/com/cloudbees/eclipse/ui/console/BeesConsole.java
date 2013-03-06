package com.cloudbees.eclipse.ui.console;

import java.io.IOException;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.internal.console.ConsoleView;

import com.cloudbees.eclipse.ui.CloudBeesUIPlugin;

public class BeesConsole extends IOConsole {

  public final static String BEES_CONSOLE_TYPE = CloudBeesUIPlugin.PLUGIN_ID + ".BeesConsoleType";

  public BeesConsole() {
    super("CloudBees SDK Console", BEES_CONSOLE_TYPE, null, true);
  }

  protected void init() {
    super.init();
    IOConsoleOutputStream info = newOutputStream(); // create a stream to write info message to
    try {
      info.write("Welcome to the CloudBees SDK Console!\nType 'bees help' for help.\n\nbees ");

      new BeesRunner().run(new BeesConsoleSession() {

        IOConsoleOutputStream newOutputStream() {
          return BeesConsole.this.newOutputStream();
        }

        IOConsoleInputStream getInputStream() {
          return BeesConsole.this.getInputStream();
        }
      });

      moveCaret();
      
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        info.close();
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
  }

  public static void moveCaret() {
    new Thread(new Runnable() {
      public void run() {
        try {
          //Dirty temporary trick to force caret to the end. It's hard to detect the actual widget content change at this point as only the model is changed.
          //FIXME Consider removing or replacing with a better implementation
          Thread.currentThread().sleep(200);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

        Display.getDefault().asyncExec(new Runnable() {
          public void run() {

            IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
            for (int i = 0; i < workbenchWindows.length; i++) {
              IWorkbenchWindow window = workbenchWindows[i];
              if (window != null) {
                IWorkbenchPage page = window.getActivePage();
                if (page != null) {
                  IViewPart part = page.findView(IConsoleConstants.ID_CONSOLE_VIEW);
                  if (part != null && part instanceof IConsoleView) {
                    ConsoleView view = (ConsoleView) part;
                    if (view != null && BeesConsole.BEES_CONSOLE_TYPE.equals(view.getConsole().getType())) {
                      StyledText control = (StyledText) view.getCurrentPage().getControl();
                      if (!control.isDisposed()) {
                        control.setCaretOffset(control.getCharCount());
                      }
                    }
                  }

                }
              }
            }
          }
        });
      }
    }).start();
  }

  
  public static void focusConsole() {

    Display.getDefault().asyncExec(new Runnable() {
      public void run() {

        IWorkbenchWindow[] workbenchWindows = PlatformUI.getWorkbench().getWorkbenchWindows();
        for (int i = 0; i < workbenchWindows.length; i++) {
          IWorkbenchWindow window = workbenchWindows[i];
          if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
              IViewPart part = page.findView(IConsoleConstants.ID_CONSOLE_VIEW);
              if (part != null && part instanceof IConsoleView) {
                ConsoleView view = (ConsoleView) part;
                if (view != null && view.getConsole()!=null && BeesConsole.BEES_CONSOLE_TYPE.equals(view.getConsole().getType())) {
                  view.getCurrentPage().setFocus();
                }
              }

            }
          }
        }
      }
    });
  }
}
