package caching;

import com.google.protobuf.Message;
import providers.BasePojoProvider;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ProtoToPojoCachingHelper {

    private final Map<Class<?>, Map<Class<?>, BasePojoProvider>> fromProtoCache = Collections.synchronizedMap(new LinkedHashMap<>());

    public Optional<? extends BasePojoProvider> getPojoProviderFor(Class<? extends Message> clazz, Class<?> pojoClass){
        if(fromProtoCache.get(clazz) != null){
            if(fromProtoCache.get(clazz).get(pojoClass) != null){
                return Optional.of(fromProtoCache.get(clazz).get(pojoClass));
            }
        }
        return Optional.empty();
    }

    public <M extends BasePojoProvider> void addOrUpdatePojoProvider(Class<? extends Message> protoClass, Class<?> pojoClass, M mapping){
        if(!fromProtoCache.containsKey(protoClass)){
            fromProtoCache.put(protoClass, new LinkedHashMap<>());
            fromProtoCache.get(protoClass).put(pojoClass, mapping);
        }
        else {
            fromProtoCache.get(protoClass).put(pojoClass, mapping);
        }
    }

}
