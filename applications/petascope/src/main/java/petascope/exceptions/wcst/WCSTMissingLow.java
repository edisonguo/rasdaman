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
package petascope.exceptions.wcst;

import petascope.exceptions.ExceptionCode;
import petascope.util.XMLSymbols;

/**
 *
 * @author rasdaman
 */
public class WCSTMissingLow extends WCSTException {

    public WCSTMissingLow() {
        super(ExceptionCode.WCSTMissingLow, EXCEPTION_TEXT);
    }

    private static final String EXCEPTION_TEXT = "The \"" + XMLSymbols.LABEL_GRID_ENVELOPE + "\" element contains the wrong number of \""
            + XMLSymbols.LABEL_LOW + "\" (exactly 1 expected).";
}
