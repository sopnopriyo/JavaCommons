/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.configuration.plist;

import java.io.File;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationRuntimeException;

/**
 * Test class for the events generated by PropertyListConfiguration.
 *
 * @version $Id: TestPropertyListConfigurationEvents.java 1301994 2012-03-17 20:21:23Z sebb $
 */
public class TestPropertyListConfigurationEvents extends
        AbstractTestPListEvents
{
    /** Constant for the test file that will be loaded. */
    private static final File TEST_FILE = new File("conf/test.plist");

    @Override
    protected AbstractConfiguration createConfiguration()
    {
        try
        {
            return new PropertyListConfiguration(TEST_FILE);
        }
        catch (ConfigurationException cex)
        {
            throw new ConfigurationRuntimeException(cex);
        }
    }

}
