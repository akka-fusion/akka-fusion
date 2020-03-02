/*
 * Copyright 2019 helloscala.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fusion.job.impl;

import org.quartz.SchedulerConfigException;
import org.quartz.utils.PoolingConnectionProvider;
import org.quartz.utils.PropertiesParser;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Properties;

public class FactoryHelper {
    public final PropertiesParser cfg;

    public FactoryHelper(Properties props) {
        props.forEach((key, value) -> props.setProperty(key.toString(), value.toString()));
        cfg = new PropertiesParser(props);
    }

    public <T> T setBeanProps(T obj, String propsPrefix) throws InvocationTargetException, NoSuchMethodException, IntrospectionException, SchedulerConfigException, IllegalAccessException {
        return setBeanProps(obj, propsPrefix, true);
    }

    public <T> T setBeanProps(T obj, String propsPrefix, boolean stripPrefix) throws InvocationTargetException, NoSuchMethodException, IntrospectionException, SchedulerConfigException, IllegalAccessException {
        Properties props = cfg.getPropertyGroup(propsPrefix, stripPrefix);
        return setBeanProps(obj, props);
    }

    public <T> T setBeanProps(T obj, Properties props) throws NoSuchMethodException, IllegalAccessException, java.lang.reflect.InvocationTargetException, IntrospectionException, SchedulerConfigException {
        props.remove("class");
        props.remove(PoolingConnectionProvider.POOLING_PROVIDER);

        BeanInfo bi = Introspector.getBeanInfo(obj.getClass());
        PropertyDescriptor[] propDescs = bi.getPropertyDescriptors();
        PropertiesParser pp = new PropertiesParser(props);


        java.util.Enumeration<Object> keys = props.keys();
        while (keys.hasMoreElements()) {
            String name = (String) keys.nextElement();
            String c = name.substring(0, 1).toUpperCase(Locale.US);
            String methName = "set" + c + name.substring(1);

            java.lang.reflect.Method setMeth = getSetMethod(methName, propDescs);

            try {
                if (setMeth == null) {
                    throw new NoSuchMethodException(
                            "No setter for property '" + name + "'");
                }

                Class<?>[] params = setMeth.getParameterTypes();
                if (params.length != 1) {
                    throw new NoSuchMethodException(
                            "No 1-argument setter for property '" + name + "'");
                }

                // does the property value reference another property's value? If so, swap to look at its value
                PropertiesParser refProps = pp;
                String refName = pp.getStringProperty(name);
                if (refName != null && refName.startsWith("$@")) {
                    refName = refName.substring(2);
                    refProps = cfg;
                } else
                    refName = name;

                if (params[0].equals(int.class)) {
                    setMeth.invoke(obj, new Object[]{Integer.valueOf(refProps.getIntProperty(refName))});
                } else if (params[0].equals(long.class)) {
                    setMeth.invoke(obj, new Object[]{Long.valueOf(refProps.getLongProperty(refName))});
                } else if (params[0].equals(float.class)) {
                    setMeth.invoke(obj, new Object[]{Float.valueOf(refProps.getFloatProperty(refName))});
                } else if (params[0].equals(double.class)) {
                    setMeth.invoke(obj, new Object[]{Double.valueOf(refProps.getDoubleProperty(refName))});
                } else if (params[0].equals(boolean.class)) {
                    setMeth.invoke(obj, new Object[]{Boolean.valueOf(refProps.getBooleanProperty(refName))});
                } else if (params[0].equals(String.class)) {
                    setMeth.invoke(obj, new Object[]{refProps.getStringProperty(refName)});
                } else {
                    throw new NoSuchMethodException(
                            "No primitive-type setter for property '" + name
                                    + "'");
                }
            } catch (NumberFormatException nfe) {
                throw new SchedulerConfigException("Could not parse property '"
                        + name + "' into correct data type: " + nfe.toString());
            }
        }
        return obj;
    }

    public java.lang.reflect.Method getSetMethod(String name, PropertyDescriptor[] props) {
        for (PropertyDescriptor prop : props) {
            Method wMeth = prop.getWriteMethod();
            if (wMeth != null && wMeth.getName().equals(name)) {
                return wMeth;
            }
        }
        return null;
    }
}
