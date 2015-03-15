package org.zerotul.specificaion.jackson;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.util.VersionUtil;

/**
 * Created by zerotul on 13.03.15.
 */
public class PackageVersion implements Versioned {


    public final static Version VERSION = VersionUtil.parseVersion(
            "1.0-SNAPSHOT", "org.zerotul", "jackson-datatype-specification"
    );

    @Override
    public Version version()
    {
        return VERSION;
    }
}
