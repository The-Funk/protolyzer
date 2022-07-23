package providers;

import com.google.protobuf.Descriptors;
import lombok.Getter;
import lombok.Setter;
import wrappers.PBeanDescriptor;

import java.util.*;

public abstract class BasePojoProvider extends MappingProvider {

    @Getter @Setter
    protected Map<Descriptors.FieldDescriptor, PBeanDescriptor> fieldMappings = Collections.synchronizedMap(new LinkedHashMap<>());
    @Getter @Setter
    protected Map<Descriptors.FieldDescriptor, PBeanDescriptor> requiredMappings = Collections.synchronizedMap(new LinkedHashMap<>());

    public void addFieldMapping(Descriptors.FieldDescriptor field1, PBeanDescriptor field2) {
        fieldMappings.put(field1, field2);
    }

    public void addRequiredMapping(Descriptors.FieldDescriptor field1, PBeanDescriptor field2) {
        requiredMappings.put(field1, field2);
    }

}
