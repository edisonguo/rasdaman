"""
 *
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
 * Copyright 2003 - 2015 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 *
"""
from config_manager import ConfigManager
from master.error.runtime_exception import RuntimeException
from util.crs_util import CRSUtil
from util.gdal_field import GDALField


class GDALGmlUtil:
    def __init__(self, gdal_file_path):
        """
        Utility class to extract information from a gdal file. Best to isolate all gdal fuctionality to one class
        as gdallib is known to be problematic in imports
        :param str gdal_file_path: the file path to the gdal supported file
        """
        # GDAL wants filename in utf8 or filename with spaces could not open.
        import osgeo.gdal as gdal

        self.gdal_file_path = gdal_file_path
        self.gdal_dataset = gdal.Open(self.gdal_file_path.encode('utf8'))
        if self.gdal_dataset is None:
            raise RuntimeException("The file at path " + gdal_file_path + " is not a valid GDAL decodable file.")

    def get_offset_vectors(self):
        """
        Returns the offset vectors for the coverage calculated from the dataset
        :rtype: list[float]
        """
        offset_x = self.gdal_dataset.GetGeoTransform()[1]
        offset_y = self.gdal_dataset.GetGeoTransform()[5]
        return offset_x, offset_y

    def get_offset_vector_x(self):
        """
        Returns the offset vectors for the coverage calculated from the dataset on the x axis
        :rtype: float
        """
        return self.get_offset_vectors()[0]

    def get_offset_vector_y(self):
        """
        Returns the offset vectors for the coverage calculated from the dataset on the y axis
        :rtype: float
        """
        return self.get_offset_vectors()[1]

    def get_origin_x(self):
        """
        Returns the origin of the dataset on the x axis
        :rtype: str
        """
        return self.get_origin()[0]

    def get_origin_y(self):
        """
        Returns the origin of the dataset on the y axis
        :rtype: str
        """
        return self.get_origin()[1]

    def get_origin(self):
        """
        Returns the origin of the dataset. This is calculated using the origin from the dataset + 0.5 of an offset
                vector, as petascope requires it so.
        :rtype: list[str]
        """
        geo = self.gdal_dataset.GetGeoTransform()
        return str(geo[0] + 0.5 * self.gdal_dataset.GetGeoTransform()[1]), \
               str(geo[3] + 0.5 * self.gdal_dataset.GetGeoTransform()[5])

    def get_coefficients(self):
        """
        Returns the coefficients for the
        :rtype: list[float]
        """
        return [0, 0]

    def get_coefficient_x(self):
        """
        Return the coefficients of the dataset for the x axis
        :rtype: float
        """
        return self.get_coefficients()[0]

    def get_coefficient_y(self):
        """
        Return the coefficients of the dataset for the y axis
        :rtype: float
        """
        return self.get_coefficients()[1]

    def get_extents(self):
        """
        Return the extents of the dataset
        :rtype: dict[str, list]
        """
        geo_transform = self.gdal_dataset.GetGeoTransform()
        minx = geo_transform[0]
        maxy = geo_transform[3]
        maxx = minx + geo_transform[1] * self.gdal_dataset.RasterXSize
        miny = maxy + geo_transform[5] * self.gdal_dataset.RasterYSize
        return {
            "x": (minx, maxx),
            "y": (miny, maxy)
        }

    def get_extents_x(self):
        """
        Return the extents of the dataset for the x axis
        :rtype: tuple
        """
        return self.get_extents()['x']

    def get_extents_y(self):
        """
        Return the extents of the dataset for the y axis
        :rtype: tuple
        """
        return self.get_extents()['y']

    def get_resolution_x(self):
        """
        Returns the resolution of the coverage calculated from the dataset
        :rtype: float
        """
        geo_transform = self.gdal_dataset.GetGeoTransform()
        return geo_transform[1]

    def get_resolution_y(self):
        """
        Returns the resolution of the coverage calculated from the dataset
        :rtype: float
        """
        geo_transform = self.gdal_dataset.GetGeoTransform()
        return geo_transform[5]

    def get_fields_range_type(self):
        """
        Returns the range type fields from a gdal dataset
        :rtype: list[GDALField]
        """
        import osgeo.gdal as gdal

        fields = []
        for i in range(1, self.gdal_dataset.RasterCount + 1):
            band = self.gdal_dataset.GetRasterBand(i)

            # Get the field name
            if band.GetColorInterpretation():
                field_name = gdal.GetColorInterpretationName(band.GetColorInterpretation())
            else:
                field_name = ConfigManager.default_field_name_prefix + str(i)

            nil_value = str(band.GetNoDataValue()) if band.GetNoDataValue() is not None else ""
            # Check if the nil value is an integer and remove it if not as rasdaman does not support floating null values
            # TODO: Remove this check once rasdaman supports floating null values
            if nil_value is not None and not nil_value.isdigit():
                nil_value = ""
            if nil_value == "":
                dfn = ConfigManager.default_null_values
                if len(dfn) > i - 1:
                    nil_value = str(dfn[i - 1])

            if nil_value is None:
                nil_values = [None]
            else:
                nil_values = nil_value.strip().split(",")

            # Get the unit of measure
            uom = band.GetUnitType() if band.GetUnitType() else ConfigManager.default_unit_of_measure

            # Add it to the list of fields
            fields.append(GDALField(field_name, uom, nil_values))

        return fields

    def get_band_gdal_type(self):
        """
        Returns the gdal data type
        :rtype: str
        """
        import osgeo.gdal as gdal

        return gdal.GetDataTypeName(self.gdal_dataset.GetRasterBand(1).DataType)

    def get_crs(self):
        """
        Returns the CRS associated with this dataset. If none is found the default for the session is returned
        :rtype: str
        """
        import osgeo.osr as osr

        wkt = self.gdal_dataset.GetProjection()
        spatial_ref = osr.SpatialReference()
        spatial_ref.ImportFromWkt(wkt)
        crs = ConfigManager.default_crs
        if spatial_ref.GetAuthorityName(None) is not None:
            crs = CRSUtil.get_crs_url(spatial_ref.GetAuthorityName(None),
                                      spatial_ref.GetAuthorityCode(None))
        return crs

    def get_crs_code(self):
        """
        Returns the CRS code associated with this dataset. If none is found the default for the session is returned
        :rtype: str
        """
        import osgeo.osr as osr

        wkt = self.gdal_dataset.GetProjection()
        spatial_ref = osr.SpatialReference()
        spatial_ref.ImportFromWkt(wkt)
        return spatial_ref.GetAuthorityCode(None)

    def get_raster_x_size(self):
        """
        Returns the raster size on the x axis in pixels
        :rtype: int
        """
        return self.gdal_dataset.RasterXSize

    def get_raster_y_size(self):
        """
        Returns the raster size on the y axis in pixels
        :rtype: int
        """
        return self.gdal_dataset.RasterYSize

    def get_datetime(self, time_tag=None):
        """
        Returns the datetime value for the dataset if one exists
        :param str time_tag: the tag if it is different from TIFFTAG_DATETIME
        :rtype: str
        """
        if time_tag is None:
            time_tag = "TIFFTAG_DATETIME"
        metadata = self.gdal_dataset.GetMetadata()
        if time_tag not in metadata:
            raise RuntimeException(
                "No tifftag " + time_tag + " for datetime was found in the file: " + self.gdal_file_path)
        return metadata[time_tag]

    def get_metadata_tag(self, tag):
        """
        Returns a specific metadata tag
        :param str tag: the tag to return
        :return: str
        """
        metadata = self.gdal_dataset.GetMetadata()
        if tag not in metadata:
            raise RuntimeException(
                "No tifftag " + tag + " found for " + self.gdal_file_path)
        return metadata[tag]
