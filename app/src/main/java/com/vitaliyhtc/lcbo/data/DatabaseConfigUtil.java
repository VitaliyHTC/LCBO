package com.vitaliyhtc.lcbo.data;

import com.j256.ormlite.android.apptools.OrmLiteConfigUtil;

import java.io.IOException;
import java.sql.SQLException;

/**
 * DatabaseConfigUtl writes a configuration file to avoid using annotation processing in runtime which is very slow
 * under Android. This gains a noticeable performance improvement.
 *
 * The configuration file is written to /res/raw/ by default. More info at: http://ormlite.com/docs/table-config
 */
public class DatabaseConfigUtil extends OrmLiteConfigUtil {

    public static void main(String[] args) throws SQLException, IOException {
        writeConfigFile("ormlite_config.txt");
    }
}

/**
 * To run this, see next:
 * http://stackoverflow.com/questions/17298773/android-studio-run-configuration-for-ormlite-config-generation
 *           read 2-nd answer by Dan J. Than others.
 *
 *
 *
 */
