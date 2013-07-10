package com.cdmtech.sol.maven.plugin.exporter;

import com.cdmtech.core.client.bean.BeanCriteria;
import com.cdmtech.core.client.bean.View;

public class QueryCriteria {
   protected String className;
   protected boolean includeSubclasses;
   
   public BeanCriteria getBeanCriteria(View<?> view) {
      BeanCriteria beanCriteria = new BeanCriteria(view, className, null);
      beanCriteria.setIncludeSubclasses(includeSubclasses);
      
      return beanCriteria;
   }
}
