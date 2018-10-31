package com.bluelightning;

import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class OpenObjectInputStream extends ObjectInputStream {

    public OpenObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
        ObjectStreamClass resultClassDescriptor = super.readClassDescriptor(); // initially streams descriptor
        @SuppressWarnings("rawtypes")
		Class localClass; // the class in the local JVM that this descriptor represents.
        try {
            localClass = Class.forName(resultClassDescriptor.getName()); 
        } catch (ClassNotFoundException e) {
            RobautoMain.logger.error("No local class for " + resultClassDescriptor.getName(), e);
            return resultClassDescriptor;
        }
        ObjectStreamClass localClassDescriptor = ObjectStreamClass.lookup(localClass);
        if (localClassDescriptor != null) { // only if class implements serializable
            final long localSUID = localClassDescriptor.getSerialVersionUID();
            final long streamSUID = resultClassDescriptor.getSerialVersionUID();
            if (streamSUID != localSUID) { // check for serialVersionUID mismatch.
                final StringBuffer s = new StringBuffer("Overriding serialized class version mismatch: ");
                s.append("local serialVersionUID = ").append(localSUID);
                s.append(" stream serialVersionUID = ").append(streamSUID);
                if (localSUID == 1L && (streamSUID < 0L || streamSUID > 10L)) {
                	RobautoMain.logger.info( s.toString() );
                } else {
	                Exception e = new InvalidClassException(s.toString());
	                RobautoMain.logger.error("Potentially Fatal Deserialization Operation.", e);
                }
                resultClassDescriptor = localClassDescriptor; // Use local class descriptor for deserialization
            }
        }
        return resultClassDescriptor;
    }
}