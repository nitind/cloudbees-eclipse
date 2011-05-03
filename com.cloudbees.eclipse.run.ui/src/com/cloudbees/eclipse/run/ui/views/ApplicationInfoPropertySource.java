package com.cloudbees.eclipse.run.ui.views;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.PropertyDescriptor;

import com.cloudbees.api.ApplicationInfo;

public class ApplicationInfoPropertySource implements IPropertySource {

  private static final String BASE = ApplicationInfoPropertySource.class.getSimpleName().toLowerCase();
  private static final String PROPRTY_CREATED_DATE = BASE + ".created.date";
  private static final String PROPERTY_URL = BASE + ".url";

  private IPropertyDescriptor[] propertyDescriptors;
  private final ApplicationInfo appInfo;

  public ApplicationInfoPropertySource(ApplicationInfo appInfo) {
    this.appInfo = appInfo;
  }

  @Override
  public Object getEditableValue() {
    return this;
  }

  @Override
  public IPropertyDescriptor[] getPropertyDescriptors() {
    if (this.propertyDescriptors == null) {

      PropertyDescriptor createdDateDescriptor = new PropertyDescriptor(PROPRTY_CREATED_DATE, "Created Date");
      createdDateDescriptor.setLabelProvider(new LabelProvider() {
        @Override
        public String getText(Object element) {
          if (element instanceof Date) {
            return new SimpleDateFormat("yyyy/MM/dd HH:mm").format((Date) element);
          } else {
            return super.getText(element);
          }
        }
      });
      //createdDateDescriptor.setCategory("Label");

      PropertyDescriptor urlDescriptor = new PropertyDescriptor(PROPERTY_URL, "URL");
      urlDescriptor.setLabelProvider(new LabelProvider() {
        @Override
        public String getText(Object element) {
          return "http://" + element;
        }
      });
      //urlDescriptor.setCategory("Label");

      this.propertyDescriptors = new IPropertyDescriptor[] { createdDateDescriptor, urlDescriptor };
    }

    return this.propertyDescriptors;
  }

  @Override
  public Object getPropertyValue(Object id) {
    if (id.equals(PROPRTY_CREATED_DATE)) {
      return this.appInfo.getCreated();
    } else if (id.equals(PROPERTY_URL)) {
      return this.appInfo.getUrls()[0];
    }
    return null;
  }

  @Override
  public boolean isPropertySet(Object id) {
    return false;
  }

  @Override
  public void resetPropertyValue(Object id) {
  }

  @Override
  public void setPropertyValue(Object id, Object value) {
    System.out.println("setting property value id=" + id + " value=" + value);
  }

}
