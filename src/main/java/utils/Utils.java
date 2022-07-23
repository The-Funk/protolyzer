package utils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

public class Utils {

    public static  <T> PropertyDescriptor[] getBeanProperties(Class<? extends T> childClass, Class<?> parentClass) throws IntrospectionException {
        if(parentClass != null){
            Class<?> clazz = getTopLevelClassWithCap(childClass, parentClass);
            if(clazz != childClass){
                return Introspector.getBeanInfo(childClass, clazz).getPropertyDescriptors();
            }
        }
        return Introspector.getBeanInfo(childClass).getPropertyDescriptors();
    }

    public static  <T> Class<?> getTopLevelClassWithCap(Class<? extends T> childClass, Class<?> parentClass){
        Class<?> c = childClass;
        while(c.isAssignableFrom(parentClass)){
            if(c.getSuperclass() != parentClass){
                c = c.getSuperclass();
            }
            else { break; }
        }
        return c;
    }



}
