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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
*/
/**
 * INCLUDE: grib.hh
 *
 * MODULE:  conversion
 *
 * CLASSES: r_Conv_GRIB
 *
 * COMMENTS:
 *
 * Provides functions to convert data from GRIB only.
 *
 */

#ifndef _R_CONV_GRIB_HH_
#define _R_CONV_GRIB_HH_

#include "conversion/convertor.hh"
#include "raslib/minterval.hh"

#include <json/json.h>
#include <stdio.h>
#include <map>
#include <string>

//@ManMemo: Module {\bf conversion}

/*@Doc:
 GRIB convertor class.
 */
class r_Conv_GRIB : public r_Convert_Memory
{
public:
    /// constructor using an r_Type object. Exception if the type isn't atomic.
    r_Conv_GRIB(const char *src, const r_Minterval &interv, const r_Type *tp) throw (r_Error);
    /// constructor using convert_type_e shortcut
    r_Conv_GRIB(const char *src, const r_Minterval &interv, int tp) throw (r_Error);
    /// destructor
    ~r_Conv_GRIB(void);

    /// convert to NETCDF
    virtual r_convDesc &convertTo(const char *options = NULL) throw (r_Error);
    /// convert from NETCDF
    virtual r_convDesc &convertFrom(const char *options = NULL) throw (r_Error);

    /// cloning
    virtual r_Convertor *clone(void) const;

    /// identification
    virtual const char *get_name(void) const;
    virtual r_Data_Format get_data_format(void) const;

private:

    /// parse the options to a JSON object
    Json::Value parseOptions(const char* options) throw (r_Error);
    
    /// get a handle to the GRIB file
    FILE* getFileHandle() throw (r_Error);
    
    /// collect the message ids from the format parameters
    std::map<int, r_Minterval> getMessageDomainsMap(const Json::Value& messageDomains) throw (r_Error);
    
    /// compute final bounding box from all message domains
    r_Minterval computeBoundingBox(const std::map<int, r_Minterval>& messageDomains) throw (r_Error);
    
    /// get an r_Minterval object for domain in string representation
    r_Minterval domainStringToMinterval(const  char* domain) throw (r_Error);
    
    /// transpose src 2D array of size NxM to dst of size MxN
    void transpose(double *src, double *dst, const int N, const int M);
    
    /// the first dims-2 bounds must be slices (low == high); the last two (x/y) must be trims
    void checkDomain(const r_Minterval& domain) throw (r_Error);
    
    /// return true if domainAxis is a slice (lower = upper bound)
    bool isSlice(const r_Sinterval& domainAxis);
    
    /// return the offset in bytes of messageDomain within domain
    size_t getSliceOffset(const r_Minterval& domain, const r_Minterval& messageDomain, size_t xyLen);
};

#endif
