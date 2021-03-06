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
package petascope.wcps2.metadata.service;

import petascope.wcps2.error.managed.processing.InvalidIntervalNumberFormat;
import petascope.wcps2.metadata.model.NumericTrimming;
import petascope.wcps2.metadata.model.Subset;
import petascope.wcps2.metadata.model.ParsedSubset;
import petascope.wcps2.metadata.model.WcpsCoverageMetadata;
import petascope.wcps2.result.parameters.SubsetDimension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import petascope.exceptions.WCSException;
import petascope.util.AxisTypes;
import petascope.util.CrsProjectionUtil;
import petascope.util.TimeUtil;
import petascope.wcps2.error.managed.processing.InvalidDomainInSubsettingCrsTransformException;
import petascope.wcps2.error.managed.processing.InvalidSlicingException;
import petascope.wcps2.error.managed.processing.OutOfBoundsSubsettingException;
import petascope.wcps2.metadata.model.Axis;
import petascope.wcps2.metadata.model.NumericSlicing;
import petascope.wcps2.metadata.model.NumericSubset;
import petascope.wcps2.metadata.model.RegularAxis;
import petascope.wcps2.result.parameters.SliceSubsetDimension;
import petascope.wcps2.result.parameters.TrimSubsetDimension;
import petascope.wcs2.parsers.subsets.DimensionSubset;

/**
 * This class has the purpose of translating subsets coming from the users into numerical subsets usable by wcps.
 *
 * @author <a href="merticariu@rasdaman.com">Vlad Merticariu</a>
 * @author <a href="mailto:bphamhuu@jacobs-university.net">Bang Pham Huu</a>
 */
public class SubsetParsingService {

    public SubsetParsingService() {

    }

    /**
     * Get a list of subset dimensions which does not contains axis iterator (e.g: Lat($px))
     * @param subsetDimensions
     * @return
     */
    public List<SubsetDimension> getPureSubsetDimensions(List<SubsetDimension> subsetDimensions) {
        List<SubsetDimension> pureSubsetDimensions = new ArrayList<SubsetDimension>();

        for (SubsetDimension subsetDimension:subsetDimensions) {
            boolean pureSubsetDimension = true;
            if (subsetDimension instanceof TrimSubsetDimension) {
                // trim subset dimension
                String lowerBound = ((TrimSubsetDimension)subsetDimension).getLowerBound();
                String upperBound = ((TrimSubsetDimension)subsetDimension).getUpperBound();
                if (lowerBound.contains(SubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN) || upperBound.contains(SubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN)) {
                    pureSubsetDimension = false;
                }
            } else {
                // slice subset dimension
                String bound = ((SliceSubsetDimension)subsetDimension).getBound();
                if (bound.contains(SubsetDimension.AXIS_ITERATOR_DOLLAR_SIGN)) {
                    pureSubsetDimension = false;
                }
            }

            // Only add subset dimension which does not contain "$"
            if(pureSubsetDimension) {
                pureSubsetDimensions.add(subsetDimension);
            }
        }

        return pureSubsetDimensions;
    }


    /**
     * Get a list of subset dimensions which contains axis iterator (e.g: Lat($px))
     * @param subsetDimensions
     * @return
     */
    public List<SubsetDimension> getAxisIteratorSubsetDimensions(List<SubsetDimension> subsetDimensions) {
        List<SubsetDimension> pureSubsetDimensions = new ArrayList<SubsetDimension>();

        for (SubsetDimension subsetDimension:subsetDimensions) {
            boolean pureSubsetDimension = false;
            if (subsetDimension instanceof TrimSubsetDimension) {
                // trim subset dimension
                String lowerBound = ((TrimSubsetDimension)subsetDimension).getLowerBound();
                String upperBound = ((TrimSubsetDimension)subsetDimension).getUpperBound();
                if (lowerBound.contains("$") || upperBound.contains("$")) {
                    pureSubsetDimension = true;
                }
            } else {
                // slice subset dimension
                String bound = ((SliceSubsetDimension)subsetDimension).getBound();
                if (bound.contains("$")) {
                    pureSubsetDimension = true;
                }
            }

            // Only add subset dimension which does not contain "$"
            if(pureSubsetDimension) {
                pureSubsetDimensions.add(subsetDimension);
            }
        }

        return pureSubsetDimensions;
    }

    /**
     * Used in slicing,trimming expression then convert list of subsetDimension to subset
     * @param dimensions
     * @param metadata
     * @return
     */
    public List<Subset> convertToNumericSubsets(List<SubsetDimension> dimensions, WcpsCoverageMetadata metadata){
        List<Subset> result = new ArrayList();
        for(SubsetDimension subsetDimension: dimensions){
            result.add(convertToNumericSubset(subsetDimension, metadata));
        }
        return result;
    }

    /**
     * Used in axis iterator to convert list of subsetDimension to subset
     * @param dimensions
     * @return
     */
    public List<Subset> convertToRawNumericSubsets(List<SubsetDimension> dimensions){
        List<Subset> result = new ArrayList();
        for(SubsetDimension subsetDimension: dimensions){
            result.add(convertToRawNumericSubset(subsetDimension));
        }
        return result;
    }

    /**
     * Used in axis iterator
     * @param dimension
     * @return
     */
    public Subset convertToRawNumericSubset(SubsetDimension dimension){
        String axisName = dimension.getAxisName();
        String crs = dimension.getCrs();
        BigDecimal lowerBound = BigDecimal.ZERO;
        BigDecimal upperBound = BigDecimal.ZERO;

        NumericSubset numericSubset = null;
        //try to parse numbers
        try{
            if (dimension instanceof TrimSubsetDimension) {
                lowerBound = new BigDecimal(((TrimSubsetDimension)dimension).getLowerBound());
                upperBound = new BigDecimal(((TrimSubsetDimension)dimension).getUpperBound());

                numericSubset = new NumericTrimming(lowerBound, upperBound);
            } else {
                lowerBound = new BigDecimal(((SliceSubsetDimension)dimension).getBound());
                upperBound = lowerBound;

                numericSubset = new NumericSlicing(lowerBound);
            }
        }
        catch (NumberFormatException ex){
            throw new InvalidIntervalNumberFormat(lowerBound.toPlainString(), upperBound.toPlainString());
        }
        return new Subset(numericSubset, crs, axisName);
    }

    /**
     * Supports * and time in the subset.
     * @param dimension
     * @param metadata
     * @return
     */
    public Subset convertToNumericSubset(SubsetDimension dimension, WcpsCoverageMetadata metadata){

        // This needs to be added transform() if dimension has crs which is different with native axis from coverage
        String axisName = dimension.getAxisName();
        String sourceCrs = dimension.getCrs();

        Axis axis = metadata.getAxisByName(axisName);

        // Normally subsettingCrs will be null or empty (e.g: Lat(20:30) not Lat:"http://.../4269(20:30)")
        // then it is nativeCrs of axis
        if (sourceCrs == null || sourceCrs.equals("")) {
            sourceCrs = axis.getCrsUri();
        }

        // Check if user set subsettingCrs in geo-referenced axis
        if (CrsUtility.geoReferencedSubsettingCrs(axisName, sourceCrs, metadata)) {
            try {
                // Then transform dimension with the subsettingCrs to nativeCrs of axis
                String targetCrs = axis.getCrsUri();
                transformSubset(axis, axis.getAxisType(), sourceCrs, targetCrs, dimension);
            } catch (WCSException ex) {
                throw new InvalidDomainInSubsettingCrsTransformException(axisName, sourceCrs, ex.getMessage());
            }
        }

        BigDecimal lowerBound = null;
        BigDecimal upperBound = null;

        NumericSubset numericSubset = null;

        //try to parse numbers
        if (dimension instanceof TrimSubsetDimension) {
            // convert each slicing point of trimming subset to numeric
            // NOTE: it cannot parse expression in the axis interval (e.g: Lat(1 + 1:2 + avg(c)))
            lowerBound = convertPointToBigDecimal(true, true, axis, ((TrimSubsetDimension)dimension).getLowerBound());
            upperBound = convertPointToBigDecimal(true, false, axis, ((TrimSubsetDimension)dimension).getUpperBound());

            numericSubset = new NumericTrimming(lowerBound, upperBound);
        } else {
            // NOTE: it cannot parse expression in the axis interval (e.g: Lat(1 + 1))
            lowerBound = convertPointToBigDecimal(false, true, axis, ((SliceSubsetDimension)dimension).getBound());
            numericSubset = new NumericSlicing(lowerBound);
        }

        return new Subset(numericSubset, sourceCrs, axisName);
    }

    /**
     * Try to parse a slicing point (from a slicing subset or low/high of trimming subset) to numeric
     * @param isTrimming check if subset is trimming
     * @param isLowerPoint check if point is in lower or upper subset
     * @param axisName axis name
     * @param point the value of slicing point (can be numeric, date time or string (throw exception if cannot parse))
     * @return
     */
    private BigDecimal convertPointToBigDecimal(boolean isTrimming, boolean isLowerPoint, Axis axis, String point) {
        BigDecimal result = null;
        if(point.equals(DimensionSubset.ASTERISK)) {
            if(isTrimming) {
                if(isLowerPoint) {
                    result = ((NumericTrimming)axis.getGeoBounds()).getLowerLimit();
                } else {
                    result = ((NumericTrimming)axis.getGeoBounds()).getUpperLimit();
                }
            } else {
                // is slicing, throw exception does not support (Lat(*))
                throw new InvalidSlicingException(axis.getLabel(), point);
            }
        }
        else if (numericPoint(point)) {
            // Check if point is Numeric
            result = new BigDecimal(point);
        } else if (TimeUtil.isValidTimestamp(point)) {
            // Convert date time to numeric
            if (axis instanceof RegularAxis) {
                // regular axis
                result = TimeConversionService.getTimeInGridPointForRegularAxis(axis, point);
            } else {
               // irregular axis
               result = TimeConversionService.getTimeInGridPointForIrregularAxis(axis, point);
            }
        } else {
            // throw exception when cannot parse a slicing subset point (e.g: Lat(1 + 1)
            throw new InvalidSlicingException(axis.getLabel(), point);
        }

        return result;
    }

    /**
     * Check a slicing point is numeric
     * @param point
     * @return
     */
    private boolean numericPoint(String point) {
        try {
            BigDecimal bigDecimal = new BigDecimal(point);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Transform subset with the subsettingCrs
     */
    private void transformSubset(Axis axis, String axisType, String sourceCrs, String targetCrs, SubsetDimension dimension) throws WCSException {
        double[] srcCoords = new double[2];
        boolean isTrim = true;

        // if axis type x then transform(x.lo, 0) then (x.hi, 0) and get only the first value
        // if axis type y then transform(0, y.lo) then (0, y.hi) and get only the second value
        int realIndex = 0;
        int dummyIndex = 1;

        if (axisType.equals(AxisTypes.Y_AXIS)) {
            realIndex = 1;
            dummyIndex = 0;
        }

        String lowerBound = "", upperBound = "";
        List<BigDecimal> transformedCoords;
        if (dimension instanceof TrimSubsetDimension) {

            // NOTE: need to convert lower and upper as 2 points
            // And because of subset in 1 axis (e.g: x or y) then cannot know the other value for point.
            // then just pass 0 for the missing value.

            // low
            srcCoords[realIndex] = Double.valueOf(((TrimSubsetDimension) dimension).getLowerBound());
            srcCoords[dummyIndex] = 0;
            transformedCoords = CrsProjectionUtil.transform(sourceCrs, targetCrs, srcCoords, false);
            lowerBound = transformedCoords.get(realIndex).toPlainString();

            // high
            srcCoords[realIndex] = Double.valueOf(((TrimSubsetDimension) dimension).getUpperBound());
            srcCoords[dummyIndex] = 0;
            transformedCoords = CrsProjectionUtil.transform(sourceCrs, targetCrs, srcCoords, false);
            upperBound = transformedCoords.get(realIndex).toPlainString();

        } else {
            isTrim = false;
            // low
            srcCoords[realIndex] = Double.valueOf(((SliceSubsetDimension) dimension).getBound());
            srcCoords[dummyIndex] = 0;
            transformedCoords = CrsProjectionUtil.transform(sourceCrs, targetCrs, srcCoords, false);
            lowerBound = transformedCoords.get(realIndex).toPlainString();
            upperBound = lowerBound;
        }

        // update the transformed bound for the dimension.
        if (isTrim) {
            // Check if the transformed subset inside the trimming axis domain first [lo:hi]
            if (isValidGeoSubsetDimension(axis, lowerBound, upperBound, isTrim)) {
                ((TrimSubsetDimension) dimension).setLowerBound(lowerBound);
                ((TrimSubsetDimension) dimension).setUpperBound(upperBound);
            }
        } else {
            // Check if the transformed subset inside tthe slicing axis domain fisrt [lo:hi]
            if (isValidGeoSubsetDimension(axis, lowerBound, upperBound, isTrim)) {
                ((SliceSubsetDimension) dimension).setBound(lowerBound);
            }
        }
    }

    /**
     * Check if subsetDimension is valid (within the axis domain interval or not)
     * @return
     */
    private boolean isValidGeoSubsetDimension(Axis axis, String lowerBound, String upperBound, boolean isTrim) {
        BigDecimal axisLowerBound = null;
        BigDecimal axisUpperBound = null;

        if(axis.getGeoBounds() instanceof NumericTrimming) {
            axisLowerBound = ((NumericTrimming)axis.getGeoBounds()).getLowerLimit();
            axisUpperBound = ((NumericTrimming)axis.getGeoBounds()).getUpperLimit();
        } else {
            axisLowerBound = ((NumericSlicing)axis.getGeoBounds()).getBound();
            axisUpperBound = axisLowerBound;
        }

        BigDecimal lowerBoundTmp = BigDecimal.valueOf(Double.valueOf(lowerBound));
        BigDecimal upperBoundTmp = BigDecimal.valueOf(Double.valueOf(upperBound));

        ParsedSubset subset = new ParsedSubset(lowerBound, upperBound);
        subset.setTrimming(isTrim);

        // If subset interval is out of axis bound then throw exception
        if (!(lowerBoundTmp.compareTo(axisLowerBound) >= 0 && upperBoundTmp.compareTo(axisUpperBound) <= 0)) {
            throw new OutOfBoundsSubsettingException(axis.getLabel(), subset, axisLowerBound.toPlainString(), axisUpperBound.toPlainString());
        }
        return true;
    }
}
