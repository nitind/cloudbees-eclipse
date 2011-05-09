package com.cloudbees.eclipse.dev.ui.views.instances;

public final class FavoritesInstanceGroup extends InstanceGroup {
  FavoritesInstanceGroup(final String name, final boolean cloudHosted) {
    super(name, cloudHosted);
  }

  @Override
  public int getOrder() {
    return 1;
  }
}
