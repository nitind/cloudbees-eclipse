package com.cloudbees.eclipse.dev.ui.views.build;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;

import com.cloudbees.eclipse.core.jenkins.api.HealthReport;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuild;

public class BuildSorter extends ViewerSorter {

  public final static int STATE = 1;
  public final static int BUILD = 2;
  public final static int DURATION = 3;
  public final static int TESTS = 4;
  public final static int CAUSE = 5;
  public static final int TIME = 6;

  private int sortCol;
  private int direction;

  public BuildSorter(final int sortCol) {
    super();
    this.sortCol = sortCol;
    this.direction = SWT.DOWN;
  }

  @Override
  public int compare(final Viewer viewer, final Object e1, final Object e2) {

    JenkinsBuild b1 = (JenkinsBuild) e1;
    JenkinsBuild b2 = (JenkinsBuild) e2;

    switch (this.sortCol) {
    case STATE:
      return rev() * compState(b1, b2);

    case BUILD:
      return rev() * compBuild(b1, b2);

    case TIME:
      return rev() * compareBuildTimestamps(b1, b2);

    case DURATION:
      return rev() * ((int) (b1.duration - b2.duration));

//    case TESTS:
      //      return rev() * compareBuildTimestamps(b1.tests, b2.tests);

      //    case CAUSE:
      //      return rev() * compareBuildTimestamps(b1.cause, b2.cause);

    default:
      break;
    }

    return rev() * super.compare(viewer, e1, e2);
  }

  private int compareBuildStability(final HealthReport[] b1, final HealthReport[] b2) {
    if (b1 == null || b2 == null) {
      if (b1 != null) {
        return -1;
      }
      if (b2 != null) {
        return 1;
      }
      return 0;
    }
    if (b1.length == 0 || b2.length == 0) {
      if (b1.length != 0) {
        return -1;
      }
      if (b2.length != 0) {
        return 1;
      }
      return 0;
    }

    long b1Score = 0;
    for (int h = 0; h < b1.length; h++) {
      String desc = b1[h].description;
      if (desc != null && desc.startsWith("Build stability: ")) {
        if (b1[h].score != null) {
          b1Score = b1[h].score.longValue();
        }
      }
    }

    long b2Score = 0;
    for (int h = 0; h < b2.length; h++) {
      String desc = b2[h].description;
      if (desc != null && desc.startsWith("Build stability: ")) {
        if (b2[h].score != null) {
          b2Score = b2[h].score.longValue();
        }
      }
    }

    if (b1Score == b2Score) {
      return 0;
    }
    return b1Score > b2Score ? -1 : 1;
  }

  private int rev() {
    return this.direction == SWT.UP ? -1 : 1;
  }

  public void setDirection(final int newDirection) {
    this.direction = newDirection;
  }

  private int compareBuildTimestamps(final JenkinsBuild b1, final JenkinsBuild b2) {
    if (b1 == null || b2 == null) {
      if (b1 != null) {
        return -1;
      }
      if (b2 != null) {
        return 1;
      }
      return 0;
    }
    if (b1.timestamp == null || b2.timestamp == null) {
      if (b1.timestamp != null) {
        return -1;
      }
      if (b2.timestamp != null) {
        return 1;
      }
      return 0;
    }
    return -1 * b1.timestamp.compareTo(b2.timestamp);
  }

  private int compBuild(final JenkinsBuild j1, final JenkinsBuild j2) {
    try {
      return (int) (j1.number - j2.number);
    } catch (Exception e) {
      e.printStackTrace(); // TODO handle better
      return 0;
    }
  }

  private int compState(final JenkinsBuild j1, final JenkinsBuild j2) {
    int res = j1.result.compareTo(j2.result);
    if (res == 0) {
      return compBuild(j1, j2);
    }
    return res;
  }

  public int getSortCol() {
    return this.sortCol;
  }

  public void setSortCol(final int col) {
    this.sortCol = col;
  }

}
