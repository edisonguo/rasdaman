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
 * Copyright 2003, 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

#ifndef RASMGR_X_SRC_REMOTECLIENTSESSION_HH
#define RASMGR_X_SRC_REMOTECLIENTSESSION_HH

#include <string>
#include <boost/cstdint.hpp>
namespace rasmgr
{
/**
 * @brief The RemoteClientSession struct contains information identifying a remote client session
 */
class RemoteClientSession
{
public:
    RemoteClientSession(const std::string& clientSessionId, const std::string& dbSessionId);

    std::string getClientSessionId() const;

    std::string getDbSessionId() const;

private:
    std::string clientSessionId;/*!< String identifying the client session on the remote rasmgr */

    std::string dbSessionId;/*!< String identifying the database session on the remote rasmgr*/
};

}

#endif // REMOTECLIENTSESSION_HH
