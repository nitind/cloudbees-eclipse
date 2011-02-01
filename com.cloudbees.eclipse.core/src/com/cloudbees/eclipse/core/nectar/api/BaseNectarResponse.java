package com.cloudbees.eclipse.core.nectar.api;

import java.lang.reflect.Field;

import com.google.gson.annotations.Expose;

abstract public class BaseNectarResponse {

  @Expose(deserialize = false, serialize = false)
  public String serviceUrl;

  /**
   * <code>null</code> if default view
   */
  @Expose(deserialize = false, serialize = false)
  public String viewUrl;

  @Expose(deserialize = false, serialize = false)
  private static String PACKAGE_NAME;

  @Expose(deserialize = false, serialize = false)
  private static String TREE_QUERY;

  private final static StringBuffer buildTreeQuery(Class<?> baseClass, StringBuffer sb) {

    int excluded = 0;

    Field[] fields = baseClass.getFields();

    for (int i = 0; i < fields.length; i++) {
      Field field = fields[i];

      Expose exposed = field.getAnnotation(Expose.class);
      if (exposed != null) {
        if (!(exposed.deserialize() && exposed.serialize())) {
          excluded++;
          continue;
        }
      }
    }

    for (int i = 0; i < fields.length; i++) {
      Field field = fields[i];

      Expose exposed = field.getAnnotation(Expose.class);
      if (exposed != null) {
        if (!(exposed.deserialize() && exposed.serialize())) {
          continue;
        }
      }

      sb.append(field.getName());

      Class<?> cl = field.getType();

      if (cl.getPackage() != null && PACKAGE_NAME.equals(cl.getPackage().getName())) {
        sb.append("[");
        buildTreeQuery(cl, sb);
        sb.append("]");
      } else if (cl.isArray()) {
        sb.append("[");
        buildTreeQuery(cl.getComponentType(), sb);
        sb.append("]");
      }

      if (i < fields.length - 1 - excluded) {
        sb.append(",");
      }

    }

    return sb;
  }

  protected final static String _getTreeQuery() {
    return TREE_QUERY;
  }

  protected static void initTreeQuery(Class cls) {
    PACKAGE_NAME = cls.getPackage().getName();
    TREE_QUERY = buildTreeQuery(cls, new StringBuffer()).toString();
  }

}
