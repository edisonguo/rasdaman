# -*-Makefile-*-
# This file is part of rasdaman community.
#
# Rasdaman community is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# Rasdaman community is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with rasdaman community.  If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann /
# rasdaman GmbH.
#
# For more information please see <http://www.rasdaman.org>
# or contact Peter Baumann via <baumann@rasdaman.com>.
#
# MAKEFILE FOR:
#   rasgeo
#
#
# COMMENTS:
#
##################################################################

# extend the AM_CXXFLAGS since it is set in configure.ac
AM_CXXFLAGS=@AM_CXXFLAGS@ @CLIENTCXXFLAGS@ @BASEDBCXXFLAGS@ @GDAL_CXXFLAGS@
AM_CFLAGS=$(CXXFLAGS)
AM_LDFLAGS=@CLIENTLDFLAGS@

if ENABLE_JAVA
if HAVE_GDAL

bin_PROGRAMS=rasimport raserase

rasimport_SOURCES= rasimport.cc rasimport.hh RasdamanConnector.cc RasdamanHelper2.cc rasgeo_template_inst.hh RasdamanConnector.hh RasdamanHelper2.hh ../../debug/debug-clt.hh
rasimport_LDADD = ../../rasodmg/librasodmg.a ../../clientcomm/libclientcomm.a \
              ../../raslib/libraslib.a \
              ../../conversion/libconversion.a ../../commline/libcommline.a \
              ../../network/libnetwork.a \
              @GDAL_LDFLAGS@

if RMANRASNET
    rasimport_LDADD +=  ../../rasnet/librasnet.a ../../common/src/libcommon.a @GRPC_LIBS@ @PROTOBUF_LIBS@ @BOOST_LDFLAGS@ -lboost_thread -lboost_regex -lboost_program_options -lboost_system
endif

raserase_SOURCES= raserase.cc raserase.hh RasdamanConnector.cc RasdamanHelper2.cc rasgeo_template_inst.hh RasdamanConnector.hh RasdamanHelper2.hh ../../debug/debug-clt.hh
raserase_LDADD = ../../rasodmg/librasodmg.a ../../clientcomm/libclientcomm.a \
              ../../raslib/libraslib.a \
              ../../conversion/libconversion.a ../../commline/libcommline.a \
              ../../network/libnetwork.a \
              @GDAL_LDFLAGS@

if RMANRASNET
    raserase_LDADD += ../../rasnet/librasnet.a ../../common/src/libcommon.a @GRPC_LIBS@ @PROTOBUF_LIBS@ @BOOST_LDFLAGS@ -lboost_thread -lboost_regex -lboost_program_options -lboost_system
endif

SUBDIRS= ../../rasodmg ../../clientcomm \
         ../../raslib ../../conversion ../../commline ../../network

install-data-hook:
		mkdir -p $(HOME)/.rasdaman; \
		if [ ! -f $(HOME)/.rasdaman/rasconnect ]; then \
			cp rasconnect $(HOME)/.rasdaman/; \
			echo "rasimport configuration file installed successfully in $(HOME)/.rasdaman/rasconnect"; \
		fi
endif
endif
