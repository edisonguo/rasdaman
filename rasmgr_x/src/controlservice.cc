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

#define BOOST_SPIRIT_USE_PHOENIX_V3
#include <boost/spirit/include/qi.hpp>
#include <boost/spirit/include/phoenix.hpp>

#include "common/src/logging/easylogging++.hh"

#include "controlservice.hh"

namespace rasmgr
{

ControlService::ControlService(boost::shared_ptr<ControlCommandExecutor> commandExecutor):commandExecutor(commandExecutor)
{}


ControlService::~ControlService()
{}

void ControlService::ExecuteCommand(::google::protobuf::RpcController* controller,
                                    const ::rasnet::service::RasCtrlRequest* request,
                                    ::rasnet::service::RasCtrlResponse* response,
                                    ::google::protobuf::Closure* done)
{
	std::string result;
    if(!request->has_user_name() || !request->has_password_hash())
    {
        result = "The user's credentials are not set";
    }
    else
    {
	result = this->commandExecutor->executeCommand(request->command(), request->user_name(), request->password_hash());
    }

    response->set_message(result);
}


}
/* namespace rasmgr */