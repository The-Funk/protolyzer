package base;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import enums.NameMappingSchema;
import enums.TypeMappingSchema;
import exceptions.ProtolyzerMappingException;
import lombok.Getter;
import lombok.Setter;
import providers.BasePojoProvider;
import providers.BaseProtoProvider;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;

import static utils.Utils.getBeanProperties;

public class PojoToProtoMapper {

    @Getter @Setter
    private NameMappingSchema nameMappingSchema = NameMappingSchema.BEST_EFFORT;
    @Getter @Setter
    private TypeMappingSchema typeMappingSchema = TypeMappingSchema.STRICT;

    private <P extends Message, J, M extends BaseProtoProvider> boolean autoConfigureProtoProvider(P proto, Class<J> pojoClass, M provider) throws ProtolyzerMappingException {
        // Protobuf objects can't be mapped to other Protobuf objects at this time.
        if(Message.class.isAssignableFrom(pojoClass)){
            throw new ProtolyzerMappingException("Both classes in given provider are Protobuf generated classes. Mapping from proto to proto is not yet supported.");
        }
        // If only source is a Protobuf generated class
        else {
            try {
                // Get all PropertyDescriptors for the class to map to, one time.
                PropertyDescriptor[] pds = getBeanProperties(pojoClass, Object.class);
                // For each PD do auto config task for individual proto field to corresponding proto field
                for(Descriptors.FieldDescriptor fd : proto.getAllFields().keySet()){
                    //configureProtoFieldToPojoField(fd, pds, pojoClass, provider);
                }
                // Successful mapping
                return true;
            }
            catch (IntrospectionException e){
                // Unsuccessful mapping
                return false;
            }
        }
    }

}
