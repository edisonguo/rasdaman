# -*-Makefile-*- (for Emacs)
#
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>. 
# 
# MAKEFILE FOR:  
#   module conversion
# 
#
# COMMENTS: 
# 
##################################################################

lib_LIBRARIES=libconversion.a
AM_CXXFLAGS=@AM_CXXFLAGS@ @GDAL_CXXFLAGS@

libconversion_a_SOURCES= convertor.cc convfactory.cc tiff.cc hdf.cc \
                         csv.cc json.cc dem.cc memfs.cc netcdf.cc \
                         convertor.hh convfactory.hh tiff.hh hdf.hh \
                         csv.hh json.hh dem.hh memfs.hh \
                         netcdf.hh grib.cc grib.hh mimetypes.cc mimetypes.hh \
                         tmpfile.cc tmpfile.hh gdal.hh gdal.cc \
                         convutil.cc convutil.hh formatparams.cc formatparams.hh
