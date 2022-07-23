package base;

import enums.NameMappingSchema;
import enums.TypeMappingSchema;

public class MultiProtolyzer extends Protolyzer {

    @Override
    public MultiProtolyzer registerInstanceNameMappingSchema(NameMappingSchema schema) {
        protoMapper.setNameMappingSchema(schema);
        return this;
    }

    @Override
    public MultiProtolyzer registerInstanceTypeMappingSchema(TypeMappingSchema schema) {
        protoMapper.setTypeMappingSchema(schema);
        return this;
    }
}
