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

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import petascope.exceptions.ExceptionCode;
import petascope.exceptions.WCPSException;
import petascope.exceptions.SecoreException;
import petascope.wcps.metadata.CoverageInfo;
import petascope.util.WcpsConstants;
import org.w3c.dom.*;
import petascope.core.EncodeCrsProperties;
import petascope.util.AxisTypes;
import petascope.util.CrsUtil;
import petascope.wcps.metadata.DomainElement;

/**
 * Handle crsTransform() in WCPS
 * 
 * e.g: 
 * crsTransform(c[ansi(148654)]
 * ,{ E:"http://www.opengis.net/def/crs/EPSG/0/3542", N:"http://www.opengis.net/def/crs/EPSG/0/3542"}, {})
 * then will convert the 2D coverage with outputCrs (3542) instead of nativeCRS (e.g: 32633, 4326)
 * NOTE: It cannot convert any coverage which is not 2D and native CRS should be geo-referenced CRS (not GridAxis).
 * 
 * @author <a href="mailto:bphamhuu@jacobs-university.de">Bang Pham Huu</a>
 */

/** NOTE(campalani): rasdaman enterprise already implements reprojection of coverage
 *  through 'project' function.
 */
public class CrsTransformCoverageExpr extends AbstractRasNode implements ICoverageInfo {
    
    private static final boolean ENTERPRISE = true;
    private static Logger log = LoggerFactory.getLogger(CrsTransformCoverageExpr.class);
    
    private CoverageExpr coverageExprType;
    private CoverageInfo coverageInfo;
    
    // NOTE: if coverage has compoundCrs 
    // (e.g: http://localhost:8080/def/crs-compound?1=http://localhost:8080/def/crs/EPSG/0/32633&2=http://localhost:8080/def/crs/OGC/0/AnsiDate)
    // Only store the geo-referenced CRS (http://localhost:8080/def/crs/EPSG/0/32633)
    private String sourceCrsName;
    private String targetCrsName;
    private String bbox; // geo bbox
    private FieldInterpolationElement fieldInterp;
 
    private List<DimensionIntervalElement> axisList;
    private String[] dimNames;
    private int dims;
    private DimensionIntervalElement elem;

    public CrsTransformCoverageExpr(Node node, XmlQuery xq) throws WCPSException, SecoreException {
        log.trace(node.getNodeName());
        Node child;
        String nodeName;

        axisList = new ArrayList<DimensionIntervalElement>();

        child = node.getFirstChild();
        while (child != null) {
            nodeName = child.getNodeName();

            if (nodeName.equals("#text")) {
                child = child.getNextSibling();
                continue;
            }

            if (nodeName.equals(WcpsConstants.MSG_AXIS)) {
                // Start a new axis and save it
                log.trace("  axis");
                elem = new DimensionIntervalElement(child, xq, coverageInfo);
                targetCrsName = elem.getCrs();
                axisList.add(elem);
                child = elem.getNextNode();
            } else if (nodeName.equals(WcpsConstants.MSG_NAME)) {
                log.trace("  field interpolation");
                fieldInterp = new FieldInterpolationElement(child, xq);
                child = fieldInterp.getNextNode();
            } else {
                // has to be the coverage expression
                try {
                    log.trace("  coverage expression");
                    coverageExprType = new CoverageExpr(child, xq);
                    coverageInfo = coverageExprType.getCoverageInfo();
                    super.children.add(coverageExprType);
                    child = child.getNextSibling();
                } catch (WCPSException ex) {
                    log.error("  unknown node for CrsTransformCoverageExpr expression:" + child.getNodeName());
                    throw new WCPSException(ExceptionCode.InvalidMetadata, "Unknown node for CrsTransformCoverageExpr expression:" + child.getNodeName());
                }
            }
        }
        
        // Add children to let the XML query be re-traversed
        super.children.addAll(axisList);  
        
        EncodeCrsProperties encodeCrsProperties = new EncodeCrsProperties((CoverageExpr)coverageExprType, 2);
        if (coverageInfo != null) {
            // Build the whole string (dimensions of reqBounds are already checked inside getRequestBounds)
            if (coverageInfo.getBbox() != null) {
                // NOTE: only get CRS for axis which is geo-referenced (not time, high, pressure,...)
                for(DomainElement element:coverageInfo.getDomains()) {
                    if(element.getType().equals(AxisTypes.X_AXIS)) {
                        sourceCrsName = element.getNativeCrs();
                        break;
                    }
                }                
                encodeCrsProperties.setCrs(sourceCrsName);
            }
        }
        bbox = encodeCrsProperties.toRasqlProjectBbox();
    }

    public String toRasQL() {
        String result = "project( " + coverageExprType.toRasQL() + ", ";
        result += "\"" + bbox + "\", ";
        result += "\"" + computeSourceCrsName() + "\", ";
        result += "\"" + computeTargetCrsName() + "\" )";
        return result;
    }

    public CoverageInfo getCoverageInfo() {
        return coverageInfo;
    }

    public String getSourceCrsName() {
        return sourceCrsName;
    }

    public String getTargetCrsName() {
        return targetCrsName;
    }

    private String computeSourceCrsName() {
        //all axises have the same crsName, only accept 2D CRS, to be updated after CrsUtil.getGmlDefinition() is available
        return CrsUtil.CrsUri.getAuthority(sourceCrsName) +
                ":" +
                CrsUtil.CrsUri.getCode(sourceCrsName);
    }

    private String computeTargetCrsName() {
        //all axises have the same crsName, only accept 2D CRS, to be updated after CrsUtil.getGmlDefinition() is available   
        return CrsUtil.CrsUri.getAuthority(targetCrsName) +
                ":" +
                CrsUtil.CrsUri.getCode(targetCrsName);
    }
 
}