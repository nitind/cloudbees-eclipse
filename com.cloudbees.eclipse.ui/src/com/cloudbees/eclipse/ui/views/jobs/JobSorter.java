package com.cloudbees.eclipse.ui.views.jobs;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;

import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse.Job;
import com.cloudbees.eclipse.core.nectar.api.NectarJobsResponse.Job.Build;

public class JobSorter extends ViewerSorter {

  public final static int STATE = 1;
  public final static int JOB = 2;
  public final static int LAST_BUILD = 3;
  public final static int LAST_SUCCESS = 4;
  public final static int LAST_FAILURE = 5;
  private int sortCol;
  private int direction;

  public JobSorter(int sortCol) {
    super();
    this.sortCol = sortCol;
    this.direction = SWT.DOWN;
  }

  @Override
  public int compare(Viewer viewer, Object e1, Object e2) {

    NectarJobsResponse.Job j1 = (NectarJobsResponse.Job) e1;
    NectarJobsResponse.Job j2 = (NectarJobsResponse.Job) e2;

    switch (sortCol) {
    case STATE:
      return rev() * compState(j1, j2);

    case JOB:
      return rev() * compJob(j1, j2);

    case LAST_BUILD:
      return rev() * compareBuildTimestamps(j1.lastBuild, j2.lastBuild);

    case LAST_SUCCESS:
      return rev() * compareBuildTimestamps(j1.lastSuccessfulBuild, j2.lastSuccessfulBuild);

    case LAST_FAILURE:
      return rev() * compareBuildTimestamps(j1.lastFailedBuild, j2.lastFailedBuild);

    default:
      break;
    }
    return rev() * super.compare(viewer, e1, e2);
  }

  private int rev() {
    return direction == SWT.UP ? -1 : 1;
  }

  public void setDirection(int newDirection) {
    direction = newDirection;
  }

  private int compareBuildTimestamps(Build b1, Build b2) {
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

  private int compJob(Job j1, Job j2) {
    return j1.displayName.compareToIgnoreCase(j2.displayName);
  }

  private int compState(Job j1, Job j2) {
    int res = j1.color.compareTo(j2.color);
    if (res == 0) {
      return compJob(j1, j2);
    }
    return res;
  }

  public int getSortCol() {
    return sortCol;
  }

  public void setSortCol(int col) {
    sortCol = col;
  }

}
