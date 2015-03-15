package org.zerotul.specificaion.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.zerotul.specificaion.jackson.deserializer.SpecificationDeserializer;
import org.zerotul.specification.Specification;
import org.zerotul.specification.SpecificationImpl;

import java.time.*;

/**
 * Created by zerotul on 13.03.15.
 */
public class SpecificationModule extends SimpleModule {

    public SpecificationModule() {
            super(PackageVersion.VERSION);

        addDeserializer(SpecificationImpl.class, new SpecificationDeserializer());
        addDeserializer(Specification.class, new SpecificationDeserializer());
    }
}
