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
package petascope.wcps.server.core;

import petascope.exceptions.WCPSException;
import org.w3c.dom.*;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.util.WcpsConstants;

public class RangeField extends AbstractRasNode {
    
    private static Logger log = LoggerFactory.getLogger(RangeField.class);

    private String type;

    public RangeField(Node node, XmlQuery xq) throws WCPSException {
        while ((node != null) && node.getNodeName().equals("#" + WcpsConstants.MSG_TEXT)) {
            node = node.getNextSibling();
        }
        log.trace(node.getNodeName());

        if (node == null) {
            throw new WCPSException("RangeFieldType parsing error.");
        }

        String nodeName = node.getNodeName();

        if (nodeName.equals(WcpsConstants.MSG_TYPE)) {
            String rangeType = node.getTextContent();
            if(rangeType.equals(WcpsConstants.MSG_NULL)){
                throw new WCPSException("Invalid RangeField type");
            }
            this.type = rangeType;
            log.trace("Range field type: " + type);
        }
    }

    public String toRasQL() {
        String result = this.type.toLowerCase();

        if(result.equals(WcpsConstants.MSG_BOOLEAN)){
            result = WcpsConstants.MSG_BOOL;
        }else if(result.equals(WcpsConstants.MSG_CHAR)){
            result = WcpsConstants.MSG_OCTET;
        }else if(result.equals(WcpsConstants.MSG_UNSIGNED_CHAR)){
            result = WcpsConstants.MSG_CHAR;
        }else if(result.equals(WcpsConstants.MSG_INT)){
            result = WcpsConstants.MSG_LONG;
        }else if(result.equals(WcpsConstants.MSG_UNSIGNED_INT)){
            result = WcpsConstants.MSG_UNSIGNED_LONG;
        }else if(result.equals(WcpsConstants.MSG_UNSIGNED_LONG)){
            result = WcpsConstants.MSG_LONG;
        }else if(result.equals(WcpsConstants.MSG_COMPLEX + "2")) {
            result = WcpsConstants.MSG_COMPLEX + "d";
        }
        //short, unsigned short and complex have identity mapping
        return result;
    }
};
