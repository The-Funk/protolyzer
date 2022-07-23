package base;

import enums.NameMappingSchema;
import enums.TypeMappingSchema;

public class SingletonProtolyzer extends Protolyzer {

    private SingletonProtolyzer(){}

    public static SingletonProtolyzer getInstance(){
        return SingletonProtolyzer.Singleton.INSTANCE.getInstance();
    }

    @Override
    public SingletonProtolyzer registerInstanceNameMappingSchema(NameMappingSchema schema) {
        protoMapper.setNameMappingSchema(schema);
        return SingletonProtolyzer.getInstance();
    }

    @Override
    public SingletonProtolyzer registerInstanceTypeMappingSchema(TypeMappingSchema schema) {
        protoMapper.setTypeMappingSchema(schema);
        return SingletonProtolyzer.getInstance();
    }

    private enum Singleton {
        INSTANCE;
        private static final SingletonProtolyzer FACTORY_INSTANCE = new SingletonProtolyzer();
        Singleton(){}
        public SingletonProtolyzer getInstance() { return FACTORY_INSTANCE; }
    }

}
