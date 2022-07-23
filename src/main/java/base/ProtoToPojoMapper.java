package base;

import annotations.ProtoIgnore;
import annotations.ProtoProperty;
import annotations.ProtoRequired;
import caching.ProtoToPojoCachingHelper;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import enums.NameMappingSchema;
import enums.TypeMappingSchema;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import providers.BasePojoProvider;
import providers.PojoProvider;
import java.beans.PropertyDescriptor;
import wrappers.PBeanDescriptor;

import exceptions.ProtolyzerMappingException;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static utils.Utils.getBeanProperties;

public class ProtoToPojoMapper {

    @Getter @Setter
    private NameMappingSchema nameMappingSchema = NameMappingSchema.BEST_EFFORT;
    @Getter @Setter
    private TypeMappingSchema typeMappingSchema = TypeMappingSchema.STRICT;

    private final ProtoToPojoCachingHelper cachingHelper = new ProtoToPojoCachingHelper();

    ProtoToPojoMapper(){}

    <T, P extends Message> Collection<T> mapToCollectionFn(@NonNull Collection<P> protos, @NonNull Class<T> pojoClass) throws ProtolyzerMappingException {
        Collection<T> collection = new ArrayList<>();
        for(P proto : protos){
            collection.add(mapToBeanFn(proto, pojoClass));
        }
        return collection;
    }

    <T, P extends Message> T mapToBeanFn(P proto, Class<T> pojoClass) throws ProtolyzerMappingException {
        Optional<? extends BasePojoProvider> pojoProvider = cachingHelper.getPojoProviderFor(proto.getClass(), pojoClass);
        if(pojoProvider.isPresent()){
            return mapToBean(proto, pojoClass, pojoProvider.get());
        }
        else {
            PojoProvider provider = new PojoProvider();
            if(autoConfigurePojoProvider(proto, pojoClass, provider)){
                cachingHelper.addOrUpdatePojoProvider(proto.getClass(), pojoClass, provider);
                return mapToBean(proto, pojoClass, provider);
            }
            throw new ProtolyzerMappingException("Unable to generate an acceptable mapping for these classes");
        }
    }

    private <P extends Message, J, M extends BasePojoProvider> J mapToBean(P proto, Class<J> pojoClass, M provider) throws ProtolyzerMappingException {
        J pojo = constructPojo(proto, pojoClass, provider);

        for(FieldDescriptor fd : proto.getDescriptorForType().getFields()){
            if(provider.getFieldMappings().containsKey(fd)){
                PBeanDescriptor bd = provider.getFieldMappings().get(fd);
                try {
                    if(fd.isRepeated()){
                        Collection<Object> objs = new ArrayList<>();
                        int numFields = proto.getRepeatedFieldCount(fd);
                        if(fd.getType().getJavaType() == JavaType.MESSAGE){
                            if(bd.isOfProtolyzerMessageType()){
                                for (int ny = 0; ny < numFields; ny++){
                                    objs.add(mapToBeanFn((bd.getMatchingMessageType()).cast(proto.getRepeatedField(fd, ny)), bd.getCollectionInnerType()));
                                }
                                // TODO more collections types
                                bd.getDescriptor().getWriteMethod().invoke(pojo, objs);
                            }
                            else if(bd.isOfGoogleMessageType()){
                                for (int ny = 0; ny < numFields; ny++){
                                    Optional<Object> res = convert(proto.getRepeatedField(fd, ny), fd, bd);
                                    res.ifPresent(objs::add);
                                }
                            }
                        }
                        else {
                            for (int ny = 0; ny < numFields; ny++){
                                Optional<Object> res = convert(proto.getRepeatedField(fd, ny), fd, bd);
                                res.ifPresent(objs::add);
                            }
                            // TODO more collections types
                            bd.getDescriptor().getWriteMethod().invoke(pojo, objs);
                        }
                    }
                    else if(fd.getJavaType() == JavaType.MESSAGE){
                        if(bd.isOfProtolyzerMessageType()){
                            Object obj = mapToBeanFn((bd.getMatchingMessageType()).cast(proto.getField(fd)), bd.getDescriptor().getPropertyType());
                            bd.getDescriptor().getWriteMethod().invoke(pojo, obj);
                        }
                        else if(bd.isOfGoogleMessageType()){

                        }
                    }
                    else {
                        Optional<Object> opRes = convert(proto.getField(fd), fd, bd);
                        if(opRes.isPresent()){
                            bd.getDescriptor().getWriteMethod().invoke(pojo, opRes.get());
                        }
                    }
                }
                catch (InvocationTargetException | IllegalAccessException e){
                    throw new ProtolyzerMappingException("Unable to properly access bean setter method.", e);
                }
            }
        }
        return pojo;
    }


    private <P extends Message, J, M extends BasePojoProvider> boolean autoConfigurePojoProvider(@NonNull P proto, @NonNull Class<J> pojoClass, @NonNull M provider) throws ProtolyzerMappingException {
        // Protobuf objects can't be mapped to other Protobuf objects at this time. Possibly available in the future.
        if(Message.class.isAssignableFrom(pojoClass)){
            throw new ProtolyzerMappingException("Both classes in given provider are Protobuf generated classes. Mapping from proto to proto is not yet supported.");
        }
        // If only source is a Protobuf generated class
        else {
            try {
                // Get all PropertyDescriptors for the class to map to, one time.
                PropertyDescriptor[] pds = getBeanProperties(pojoClass, Object.class);

                // For each FD, pass PDs, find match, cache necessary details
                if(pds != null && pds.length > 0){
                    for(FieldDescriptor fd : proto.getDescriptorForType().getFields()){
                        configureProtoFieldToPojoField(fd, pds, pojoClass, provider);
                    }
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

    private <M extends BasePojoProvider> void configureProtoFieldToPojoField(@NonNull FieldDescriptor fd, @NonNull PropertyDescriptor[] pds, @NonNull Class<?> pojoClass, @NonNull M provider) throws ProtolyzerMappingException {
        Optional<PBeanDescriptor> bd = getMatchingBeanDescriptor(fd, pds, pojoClass);
        if(bd.isPresent()){
            bd.get().process();
            if(bd.get().getField() != null){
                if(bd.get().getField().isAnnotationPresent(ProtoRequired.class)){
                    provider.addRequiredMapping(fd, bd.get());
                }
                else {
                    provider.addFieldMapping(fd, bd.get());
                }
            }
            else {
                throw new ProtolyzerMappingException("Unrecoverable mismatch between reflective field name and introspected field name in class " + pojoClass.getName());
            }
        }
    }

    /**
     * Maps a given Protobuf FieldDescriptor to a custom Protolyzer PBeanDescriptor.
     * @param   fd a field descriptor for a given Protobuf generated class
     * @param   pds all possible PropertyDescriptors in the corresponding Bean class
     * @param   pojoClass the POJO/Bean class to attempt mapping the protobuf object instance
     * @return  Optional<PBeanDescriptor> The matching PBeanDescriptor or null
     */
    private <J> Optional<PBeanDescriptor> getMatchingBeanDescriptor(@NonNull FieldDescriptor fd, @NonNull PropertyDescriptor[] pds, @NonNull Class<J> pojoClass) {
        for(PropertyDescriptor pojoProp : pds){
            PBeanDescriptor pb = new PBeanDescriptor(pojoClass, pojoProp);
            if(pb.getField() != null){
                if(!pb.getField().isAnnotationPresent(ProtoIgnore.class) || !pb.getField().getAnnotation(ProtoIgnore.class).incoming()){
                    if(nameMappingSchema == NameMappingSchema.BEST_EFFORT){
                        if(pb.getField().isAnnotationPresent(ProtoProperty.class)){
                            if(fd.getName().equals(pb.getField().getAnnotation(ProtoProperty.class).proto())){
                                return Optional.of(pb);
                            }
                        }
                        else if(fd.getName().replace("_", "").equalsIgnoreCase(pojoProp.getName().replace("_", ""))){
                            return Optional.of(pb);
                        }
                    }
                    if(nameMappingSchema == NameMappingSchema.EXACT_MATCH_ONLY){
                        if(fd.getName().equals(pojoProp.getName())){
                            return Optional.of(pb);
                        }
                    }
                    if(nameMappingSchema == NameMappingSchema.MANUAL_MATCH_ONLY){
                        if(pb.getField().isAnnotationPresent(ProtoProperty.class)){
                            if(fd.getName().equals(pb.getField().getAnnotation(ProtoProperty.class).proto())){
                                return Optional.of(pb);
                            }
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private <P extends Message, M extends BasePojoProvider, J> J constructPojo(@NonNull P proto, @NonNull Class<J> pojoClass, @NonNull M provider) throws ProtolyzerMappingException {
        try {
            if(!provider.getRequiredMappings().isEmpty()){
                List<Object> args = new ArrayList<>();
                Class<?>[] classes = new Class<?>[provider.getRequiredMappings().size()];
                int i = 0;
                for(FieldDescriptor fd : provider.getRequiredMappings().keySet()){
                    if(proto.hasField(fd)){
                        if(fd.isRepeated()){
                            System.out.println(fd.getType().getJavaType());
                            Collection<Object> objs = new ArrayList<>();
                            int numFields = proto.getRepeatedFieldCount(fd);
                            for (int ny = 0; ny < numFields; ny++){
                                Optional<Object> res = convert(proto.getRepeatedField(fd, ny), fd, provider.getFieldMappings().get(fd));
                                res.ifPresent(objs::add);
                            }                        }
                        else if(fd.getJavaType() == JavaType.MESSAGE) {

                        }
                        convert(proto.getField(fd), fd, provider.getRequiredMappings().get(fd));
                        args.add(proto.getField(fd));
                        classes[i] = provider.getRequiredMappings().get(fd).getDescriptor().getPropertyType();
                        i++;
                    }
                    else { throw new ProtolyzerMappingException("Required (@NonNull/@NotNull) field value missing from proto object, unable to convert to POJO."); }
                }
                return pojoClass.getDeclaredConstructor(classes).newInstance(args.toArray());
            }
            return pojoClass.getDeclaredConstructor().newInstance();
        }
        catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new ProtolyzerMappingException("\nUnable to invoke necessary constructor.\n IF using null safety annotations:\n" +
                    "* Ensure there is a public constructor with all necessary args (in order of field appearance).\n" +
                    "* Ensure that all non null fields are also annotated with @ProtoRequired\n" +
                    "IF not using null safety annotations:\n* Ensure there is a default public constructor for your class.", e);
        }
    }

    private Optional<Object> convert(@NonNull Object obj, @NonNull FieldDescriptor fd, @NonNull PBeanDescriptor bd) {
        JavaType type1 = fd.getJavaType();
        Class<?> type2 = bd.getDescriptor().getPropertyType();

        if(typeMappingSchema == TypeMappingSchema.STRICT){
            if(type1 == JavaType.INT){
                if(type2.getName().equals("int") || type2.getName().equals("java.lang.Integer")){
                    return Optional.of(obj);
                }
            }
            if(type1 == JavaType.LONG){
                if(type2.getName().equals("long") || type2.getName().equals("java.lang.Long")){
                    return Optional.of(obj);
                }
            }
            if(type1 == JavaType.FLOAT){
                if(type2.getName().equals("float") || type2.getName().equals("java.lang.Float")){
                    return Optional.of(obj);
                }
            }
            if(type1 == JavaType.DOUBLE){
                if(type2.getName().equals("double") || type2.getName().equals("java.lang.Double")){
                    return Optional.of(obj);
                }
            }
            if(type1 == JavaType.BOOLEAN){
                if(type2.getName().equals("boolean") || type2.getName().equals("java.lang.Boolean")){
                    return Optional.of(obj);
                }
            }
            if(type1 == JavaType.STRING){
                if(type2.getName().equals("java.lang.String")){
                    return Optional.of(obj);
                }
            }
            if(type1 == JavaType.BYTE_STRING){
                if(type2.getName().equals("com.google.protobuf.ByteString")){
                    return Optional.of(obj);
                }
            }
        }
        else if(typeMappingSchema == TypeMappingSchema.FLEXIBLE){}
        return Optional.empty();
    }

}
