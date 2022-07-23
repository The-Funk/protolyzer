package caching;

import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import providers.BaseProtoProvider;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class PojoToProtoCachingHelper {

    private final Map<Class<?>, Map<Class<?>, BaseProtoProvider>> fromPojoCache = Collections.synchronizedMap(new LinkedHashMap<>());

    public Optional<? extends BaseProtoProvider> getProtoProviderFor(Class<?> pojoClass, Class<? extends GeneratedMessageV3.Builder<?>> builderClass){
        if(fromPojoCache.get(pojoClass) != null){
            return Optional.of(fromPojoCache.get(pojoClass).get(builderClass));
        }
        return Optional.empty();
    }

    public <M extends BaseProtoProvider> void addOrUpdateProtoProvider(Class<?> pojoClass, Class<? extends Message.Builder> builderClass, M mapping){
        if(!fromPojoCache.containsKey(pojoClass)){
            fromPojoCache.put(pojoClass, new LinkedHashMap<>());
            fromPojoCache.get(pojoClass).put(builderClass, mapping);
        }
        else {
            fromPojoCache.get(pojoClass).put(builderClass, mapping);
        }
    }

}
