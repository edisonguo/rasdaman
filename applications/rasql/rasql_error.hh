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
* Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009 Peter Baumann / rasdaman GmbH.
*
* For more information please see <http://www.rasdaman.org>
* or contact Peter Baumann via <baumann@rasdaman.com>.
*/

/**
* rasqlError
*
* Provides a error checking for rasql queries
*
* COMMENTS:
*
*       No Comments
*/

#ifndef _RASQL_ERROR_HH_
#define _RASQL_ERROR_HH_

#ifdef __VISUALC__
#pragma warning( disable : 4290 )
#endif

//@ManMemo: Module: {\bf raslib}

/*@Doc:

 This class ...
*/


/// valid error codes:
#define ALLDONE                         -1
#define OK                              0
#define NOQUERY                         1
#define ERRORPARSINGCOMMANDLINE         2
#define ILLEGALOUTPUTTYPE               3
#define FILEINACCESSIBLE                4
#define UNABLETOCLAIMRESOURCEFORFILE    5
#define NOVALIDDOMAIN                   6
#define MDDTYPEINVALID                  7
#define FILESIZEMISMATCH                8
#define NOFILEWRITEPERMISSION           9
#define UNABLETOWRITETOFILE             10
#define FILEEMPTY						11

class RasqlError // : public std::exception
{
public:

    /// constructor receiving an error number
    RasqlError( int e );

    /// destructor
    virtual ~RasqlError();

    /// get an error description
    virtual const char * what();

private:
    /// error information
    int error_code;

};

#endif // _RASQL_ERROR_HH_
