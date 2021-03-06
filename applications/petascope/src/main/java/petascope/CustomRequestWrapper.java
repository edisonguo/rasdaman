/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU  General Public License for more details.
 *
 * You should have received a copy of the GNU  General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package petascope;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import petascope.util.StringUtil;

public class CustomRequestWrapper extends HttpServletRequestWrapper {

    private HttpServletRequest wrapped;

    private Map<String, String[]> parameterMap;
    private String queryString;

    public CustomRequestWrapper(HttpServletRequest wrapped) throws UnsupportedEncodingException {
        super(wrapped);
        this.wrapped = wrapped;
        // NOTE: if request in XML then no parameters in query string as it is NULL
        if (wrapped.getQueryString() != null) {
            this.queryString = wrapped.getQueryString();
            parseQueryString();
        }
    }

    /**
     * Add new parameter and value to query string
     * @param name
     * @param value
     */
    public void addParameter(String name, String value) {
        if (parameterMap == null) {
                parameterMap = new HashMap<String, String[]>();
                parameterMap.putAll(wrapped.getParameterMap());
        }
        String[] values = parameterMap.get(name);
        if (values == null) {
                values = new String[0];
        }
        List<String> list = new ArrayList<String>(values.length + 1);
        list.addAll(Arrays.asList(values));
        list.add(value);
        parameterMap.put(name, list.toArray(new String[0]));
    }

    @Override
    public String getParameter(String name) {
        if (parameterMap == null) {
                return wrapped.getParameter(name);
        }

        String[] strings = parameterMap.get(name);
        if (strings != null) {
                return strings[0];
        }
        return null;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (parameterMap == null) {
                return wrapped.getParameterMap();
        }

        return Collections.unmodifiableMap(parameterMap);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (parameterMap == null) {
                return wrapped.getParameterNames();
        }

        return Collections.enumeration(parameterMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        if (parameterMap == null) {
                return wrapped.getParameterValues(name);
        }
        return parameterMap.get(name);
    }

    /**
     * Parse query string to parameters map
     * NOTE: replace "+" with "%2B"
     */
    private void parseQueryString() throws UnsupportedEncodingException {
        this.queryString = this.queryString.replace("+", "%2B");
        String[] params = this.queryString.split("&");
        params = StringUtil.clean(params);

        // Parse all the parameters and values to a hash map
        Map<String, List<String>> paramsMapTmp = new LinkedHashMap<String, List<String>>();
        parameterMap = new HashMap<String, String[]>();
        for (String param:params) {
            String[] paramTmp = param.split("=");
            // NOTE: some param like &Style= does not have value then it is empty
            String name = param.split("=")[0];
            String value = "";
            if (paramTmp.length > 1) {
                value = URLDecoder.decode(param.split("=")[1], "UTF-8");
            }

            Object listValues = paramsMapTmp.get(name);
            if (listValues != null) {
                // add another values for the existence parameter (e.g:query="1"&query="2"...)
                if (listValues instanceof List) {
                    ((List<String>)listValues).add(value);
                }
            } else {
                // new key-value in paramsMap (e.g:query="1")
                List<String> tmp = new LinkedList<String>();
                tmp.add(value);
                paramsMapTmp.put(name, tmp);
            }
        }

        // Add the parsed parameters to parameters map of HTTPRequest object
        for (Map.Entry<String, List<String>> entry: paramsMapTmp.entrySet()) {
            String paramName = entry.getKey();
            int size = entry.getValue().size();
            String[] paramValues = entry.getValue().toArray(new String[size]);
            parameterMap.put(paramName, paramValues);
        }
    }
}