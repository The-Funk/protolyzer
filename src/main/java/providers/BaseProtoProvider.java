package providers;

import lombok.Getter;
import lombok.Setter;

import java.beans.PropertyDescriptor;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseProtoProvider extends MappingProvider {

    @Getter @Setter
    protected ConcurrentHashMap<PropertyDescriptor, PropertyDescriptor> fieldMappings;

    public void addFieldMapping(PropertyDescriptor field1, PropertyDescriptor field2) {
        fieldMappings.put(field1, field2);
    }

}
