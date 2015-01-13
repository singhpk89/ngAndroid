package com.ngandroid.lib.ngbind;

import java.util.List;
import java.util.Map;

/**
 * Created by davityle on 1/12/15.
 */
public class MethodInvoker {
    private final Map<String, List<BindingMethod>> methodMap;
    private final Map<String, Object> fieldMap;

    public MethodInvoker(Map<String, List<BindingMethod>> methodMap, Map<String, Object> fieldMap) {
        this.methodMap = methodMap;
        this.fieldMap = fieldMap;
    }

    public Object invoke(Object o, String methodName, Object[] objects) throws Throwable{
        if(methodName.startsWith("get")){
            String fieldName = methodName.substring(3).toLowerCase();
            return fieldMap.get(fieldName);
        }else if(methodName.startsWith("set")){
            String fieldName = methodName.substring(3).toLowerCase();
            fieldMap.put(fieldName, objects[0]);
            List<BindingMethod> setters = methodMap.get(methodName.toLowerCase());
            for(BindingMethod bindingMethod : setters){
                bindingMethod.invoke(fieldName, objects);
            }
            return null;
        }else{
            // TODO throw error
            return null;
        }
    }
}