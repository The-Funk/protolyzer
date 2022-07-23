package base;

import com.google.protobuf.Message;
import enums.NameMappingSchema;
import enums.TypeMappingSchema;
import exceptions.ProtolyzerMappingException;
import java.util.*;


public abstract class Protolyzer {

    protected final ProtoToPojoMapper protoMapper = new ProtoToPojoMapper();

    /**
     * Given a collection of protobuf object instances, returns a collection of POJO instances.
     * <p>
     * This method converts a collection of protobuf object instances to a collection of a given POJO.
     * The Collection itself is implied, only the Collection's inner class needs to be specified as the pojoClass parameter.
     * </p>
     * @param   protos a collection of object instances corresponding to a "repeated" protobuf message
     * @param   pojoClass the POJO class to attempt mapping the proto object instances to individually
     * @return  a Collection of POJO instances
     * @throws  ProtolyzerMappingException a generic wrapper exception indicating what kind of issue has occurred
     * @see     #mapToBean(Message, Class)
     */
    public <T, P extends Message> Collection<T> mapToCollection(Collection<P> protos, Class<T> pojoClass) throws ProtolyzerMappingException {
        return protoMapper.mapToCollectionFn(protos, pojoClass);
    }

    /**
     * Given a protobuf object instance, returns a POJO instance.
     * <p>
     * This method converts a protobuf object instance to a POJO instance of a specified class.
     * </p>
     * @param   proto an object instance corresponding to a protobuf message
     * @param   pojoClass the POJO class to attempt mapping the protobuf object instance
     * @return  a POJO instance
     * @throws  ProtolyzerMappingException a generic wrapper exception indicating what kind of issue has occurred
     */
    public final <T, P extends Message> T mapToBean(P proto, Class<T> pojoClass) throws ProtolyzerMappingException {
        return protoMapper.mapToBeanFn(proto, pojoClass);
    }

    // Should return P
    public <T, P extends Message.Builder> void mapToProto(T pojo, Class<P> builderClass) throws ProtolyzerMappingException {
//        Optional<? extends BaseProtoProvider> protoProvider = cachingHelper.getProtoProviderFor(pojo.getClass(), builderClass);
//        if(protoProvider.isPresent()){
//            //return mapToBean(proto, pojoClass, protoMapping.get());
//        }
//        else {
//            ProtoProvider provider = new ProtoProvider(nameMappingSchema, typeMappingSchema);
//            if(autoConfigureMappings(proto, pojoClass, provider)){
//                cachingHelper.addOrUpdateProtoMapping(proto.getClass(), pojoClass, mapping);
//                return mapToBean(proto, pojoClass, mapping);
//            }
//            throw new ProtolyzerMappingException("Unable to generate an acceptable mapping for these classes");
//        }
    }

    // Should return List<P>
    public <T, P extends Message.Builder> void mapToRepeated(Collection<T> pojos, Class<P> clazz){
//        List<T> list = new ArrayList<>();
//        for(T pojo : pojos){
//            list.add(mapToProto(proto, pojoClass));
//        }
//        return list;
    }

    public abstract Protolyzer registerInstanceNameMappingSchema(NameMappingSchema schema);

    public abstract Protolyzer registerInstanceTypeMappingSchema(TypeMappingSchema schema);

}
