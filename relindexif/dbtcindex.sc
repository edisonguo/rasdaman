#include "config.h"
#include "mymalloc/mymalloc.h"

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
/*************************************************************
 *
 *
 * PURPOSE:
 *   Code with embedded SQL for PostgreSQL DBMS
 *
 *
 * COMMENTS:
 * - not yet implemented.
 *
 ************************************************************/

#include "debug-srv.hh"

#include "dbtcindex.hh"
#include "raslib/rmdebug.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/sqlglobals.h"

void
DBTCIndex::deleteFromDb() throw (r_Error)
{
    RMDBGENTER(5, RMDebug::module_indexif, "DBTCIndex", "deleteFromDb() " << myOId);
    throw r_Error( r_Error::r_Error_BaseDBMSFailed );
}

void
DBTCIndex::storeTiles()
{
    RMDBGENTER(5, RMDebug::module_indexif, "DBTCIndex", "storeTiles() " << myOId);
    throw r_Error( r_Error::r_Error_BaseDBMSFailed );
}

void
DBTCIndex::insertBlob()
{
    RMDBGENTER(5, RMDebug::module_indexif, "DBTCIndex", "insertBlob() " << myOId);
    throw r_Error( r_Error::r_Error_BaseDBMSFailed );
}

void
DBTCIndex::writeInlineTiles(char* theblob, r_Bytes blobSize) throw (r_Error)
{
    RMDBGENTER(3, RMDebug::module_indexif, "DBTCIndex", "writeInlineTiles() " << myOId);
    throw r_Error( r_Error::r_Error_BaseDBMSFailed );
}

void
DBTCIndex::readInlineTiles() throw (r_Error)
{
    RMDBGENTER(3, RMDebug::module_indexif, "DBTCIndex", "readInlineTiles() " << myOId);
    throw r_Error( r_Error::r_Error_BaseDBMSFailed );
}

void
DBTCIndex::updateTileIndexMappings() throw (r_Error)
{
    RMDBGENTER(5, RMDebug::module_indexif, "DBTCIndex", "updateTileIndexMappings() " << myOId);
    throw r_Error( r_Error::r_Error_BaseDBMSFailed );
}