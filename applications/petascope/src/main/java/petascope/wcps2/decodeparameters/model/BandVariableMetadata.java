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
package petascope.wcps2.decodeparameters.model;

import java.util.List;

/**
 * This class represents the metadata for a variable representing a band in netcdf.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 */
public class BandVariableMetadata {

    private String description;

    private List<Double> missing_value;

    private String units;

    private String definition;

    public BandVariableMetadata(String description, List<Double> missing_value, String units, String definition) {
        this.description = description;
        this.missing_value = missing_value;
        this.units = units;
        this.definition = definition;
    }

    public BandVariableMetadata() {
    }

    public String getDescription() {
        return description;
    }

    public List<Double> getMissing_value() {
        return missing_value;
    }

    public String getUnits() {
        return units;
    }

    public String getDefinition() {
        return definition;
    }
}
