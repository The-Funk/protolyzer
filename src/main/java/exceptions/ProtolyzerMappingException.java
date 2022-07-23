package exceptions;

/**
 * This is a wrapper exception type used to provide more useful error messages in the event of a failure.
 * <p>
 * All exceptions reported by protolyzer should be of type ProtolyzerMappingException.
 * </p>
 */
public class ProtolyzerMappingException extends Exception {

    public ProtolyzerMappingException(String reason){
        super(reason);
    }

    public ProtolyzerMappingException(String reason, Throwable e){
        super(reason, e);
    }

}
