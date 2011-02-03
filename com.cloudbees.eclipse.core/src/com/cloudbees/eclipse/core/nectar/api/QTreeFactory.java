package com.cloudbees.eclipse.core.nectar.api;

import java.lang.reflect.Field;

import com.google.gson.annotations.Expose;

public class QTreeFactory {

  public static String create(final Class cls) {
    return buildTreeQuery(cls.getPackage().getName(), cls, new StringBuffer()).toString();

  }

  private final static StringBuffer buildTreeQuery(final String pkg, final Class<?> baseClass, final StringBuffer sb) {

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

      if (cl.getPackage() != null && pkg.equals(cl.getPackage().getName())) {
        sb.append("[");
        buildTreeQuery(pkg, cl, sb);
        sb.append("]");
      } else if (cl.isArray()) {
        sb.append("[");
        buildTreeQuery(pkg, cl.getComponentType(), sb);
        sb.append("]");
      }

      if (i < fields.length - 1 - excluded) {
        sb.append(",");
      }

    }

    return sb;
  }

}
