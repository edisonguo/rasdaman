/*
 * This file is part of rasdaman community.
 *
 * Rasdaman community is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rasdaman community is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2003 - 2012 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */
package secore.util;

/**
 * Global constants.
 *
 * @author Dimitar Misev
 */
public interface Constants {
  
  String URN_SEPARATOR = ":";
  String URN_PREFIX = "urn:ogc:def";
  String URN_SHORT_PREFIX = "urn:ogc";
  String REST_SEPARATOR = "/";
  
  String EMPTY = "";
  String ZERO = "0";
  
  String PAIR_SEPARATOR = "&";
  String FRAGMENT_SEPARATOR = "?";
  String KEY_VALUE_SEPARATOR = "=";
  
  String NEW_LINE = "\n";
  String COMMA = ",";
  String WHITESPACE = " ";
  char NEW_LINE_CHAR = '\n';
  
  String XML_DECL = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n";
  String COPYRIGHT = "Generated by SECORE. Copyright (c) 2011 Peter Baumann, Dimitar Misev, Mihaela Rusu @ Jacobs University.";
  
  String GMD_PREFIX = "gmd";
  String GMD_NAMESPACE = "http://www.isotc211.org/2005/gmd";
  String GCO_PREFIX = "gco";
  String GCO_NAMESPACE = "http://www.isotc211.org/2005/gco";
  String CRSNTS_PREFIX = "crs-nts";
  String CRSNTS_NAMESPACE = "http://www.opengis.net/crs-nts/1.0";
  
  
  String ADMIN_FILE = "browse.jsp";
  String SYNONYMS_FILE = "synonyms.jsp";
  String INDEX_FILE = "index.jsp";
  String DEMO_FILE = "demo.jsp";
  
  String NAMESPACE_GML = "http://www.opengis.net/gml";
  String NAMESPACE_XLINK = "http://www.w3.org/1999/xlink";
  String NAMESPACE_EPSG = "urn:x-ogp:spec:schema-xsd:EPSG:0.1:dataset";
  
  String NAME_LABEL = "name";
  String IDENTIFIER_LABEL = "identifier";
  String METADATA_LABEL = "metaDataProperty";
}
