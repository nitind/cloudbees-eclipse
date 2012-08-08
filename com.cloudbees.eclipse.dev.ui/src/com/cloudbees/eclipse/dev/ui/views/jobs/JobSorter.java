package com.cloudbees.eclipse.dev.ui.views.jobs;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;

import com.cloudbees.eclipse.core.jenkins.api.HealthReport;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsBuild;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.Job;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.JobViewGeneric;
import com.cloudbees.eclipse.core.jenkins.api.JenkinsJobsResponse.View;

public class JobSorter extends ViewerSorter {

  public final static int STATE = 1;
  public final static int JOB = 2;
  public final static int LAST_BUILD = 3;
  public final static int LAST_SUCCESS = 4;
  public final static int LAST_FAILURE = 5;
  public static final int BUILD_STABILITY = 6;

  private int sortCol;
  private int direction;

  public JobSorter(final int sortCol) {
    super();
    this.sortCol = sortCol;
    this.direction = SWT.DOWN;
  }

  @Override
  public int compare(final Viewer viewer, final Object e1, final Object e2) {

    if (! (e1 instanceof JobHolder) || !(e2 instanceof JobHolder)) {
      if ((e1 instanceof JobHolder)) {
        return -1;
      }
      if ((e1 instanceof JobHolder)) {
        return 1;
      }
      return e1.toString().compareTo(e2.toString());
    }
    
    JenkinsJobsResponse.JobViewGeneric j1 = ((JobHolder)e1).job;
    JenkinsJobsResponse.JobViewGeneric j2 = ((JobHolder)e2).job;

    switch (this.sortCol) {
    case STATE:
      return rev() * compState(j1, j2);

    case JOB:
      return rev() * compJob(j1, j2);

    case LAST_BUILD:
      if (j1 instanceof View && j2 instanceof View) {
        return rev() * (j1.getName().compareTo(j2.getName()));
      }
      if (j1 instanceof View) {
        return rev() * 1;
      }
      if (j2 instanceof View) {
        return rev() * -1;
      }

      return rev() * compareBuildTimestamps(((Job)j1).lastBuild, ((Job)j2).lastBuild);

    case LAST_SUCCESS:
      if (j1 instanceof View && j2 instanceof View) {
        return rev() * (j1.getName().compareTo(j2.getName()));
      }
      if (j1 instanceof View) {
        return rev() * 1;
      }
      if (j2 instanceof View) {
        return rev() * -1;
      }

      return rev() * compareBuildTimestamps(((Job)j1).lastSuccessfulBuild, ((Job)j2).lastSuccessfulBuild);

    case LAST_FAILURE:
      if (j1 instanceof View && j2 instanceof View) {
        return rev() * (j1.getName().compareTo(j2.getName()));
      }
      if (j1 instanceof View) {
        return rev() * 1;
      }
      if (j2 instanceof View) {
        return rev() * -1;
      }

      return rev() * compareBuildTimestamps(((Job)j1).lastFailedBuild, ((Job)j2).lastFailedBuild);

    case BUILD_STABILITY:
      if (j1 instanceof View && j2 instanceof View) {
        return rev() * (j1.getName().compareTo(j2.getName()));
      }
      if (j1 instanceof View) {
        return rev() * 1;
      }
      if (j2 instanceof View) {
        return rev() * -1;
      }

      return rev() * compareBuildStability(((Job)j1).healthReport, ((Job)j2).healthReport);
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

  private int compJob(final JobViewGeneric j1, final JobViewGeneric j2) {
    try {
      if (j1 instanceof Job && j2 instanceof View) {
        return 1;
      }
      if (j2 instanceof Job && j1 instanceof View) {
        return -1;
      }
      return j1.getName().compareToIgnoreCase(j2.getName());
    } catch (Exception e) {
      e.printStackTrace(); // TODO handle better
      return 0;
    }
  }

  private int compState(final JobViewGeneric j1, final JobViewGeneric j2) {
    int res = j1.getState().compareTo(j2.getState());
    if (res == 0) {
      return compJob(j1, j2);
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
