package wrappers;

import annotations.ProtoClass;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import exceptions.ProtolyzerMappingException;
import lombok.Data;
import lombok.NonNull;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Data
public class PBeanDescriptor {

    private Field field;
    private PropertyDescriptor descriptor;
    private boolean ofIterableType;
    private boolean ofProtolyzerMessageType;
    private boolean ofGoogleMessageType;
    private Class<?> collectionInnerType;
    private Class<? extends Message> matchingMessageType;

    public PBeanDescriptor(@NonNull Class<?> pojoClass, @NonNull PropertyDescriptor pd){
        setDescriptor(pd);
        setField(safelyGetPojoField(pojoClass, pd));
    }

    private Field safelyGetPojoField(@NonNull Class<?> pojoClass, @NonNull PropertyDescriptor pd){
        Class<?> clazz = pojoClass;
        while(clazz != Object.class){
            for (Field pojoField : clazz.getDeclaredFields()){
                // We need to equals ignore case because the introspector doesn't want us to access a field directly.
                if (pojoField.getName().equalsIgnoreCase(pd.getName())){
                    return pojoField;
                }
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    private boolean genIterableTypeStatus(){
        return Iterable.class.isAssignableFrom(descriptor.getPropertyType());
    }

    private void genProtoMessageTypeDetails(Class<?> type) throws ProtolyzerMappingException {
        if(type.isAnnotationPresent(ProtoClass.class)){
            setOfProtolyzerMessageType(true);
            if(type.getAnnotation(ProtoClass.class).value() != null){
                setMatchingMessageType(type.getAnnotation(ProtoClass.class).value());
            }
            else{
                throw new ProtolyzerMappingException("Inner message annotation cannot have null protoClass value." + type.getName());
            }
        }
        else {
            setOfProtolyzerMessageType(false);
            setOfGoogleMessageType(type.getName().startsWith("com.google.protobuf"));
        }
    }

    public void process() throws ProtolyzerMappingException {
        setOfIterableType(genIterableTypeStatus());
        if(isOfIterableType()){
            Type[] typesInIterable = ((ParameterizedType) getField().getGenericType()).getActualTypeArguments();
            if(typesInIterable.length == 1){
                if(typesInIterable[0] instanceof Class){
                    setCollectionInnerType((Class<?>) typesInIterable[0]);
                    genProtoMessageTypeDetails(collectionInnerType);
                }
                else {
                    System.out.println("Iterable type not instance of class =O It's a " + typesInIterable[0].getTypeName() );
                }
            }
        }
        else {
            genProtoMessageTypeDetails(getField().getType());
        }
    }

}
