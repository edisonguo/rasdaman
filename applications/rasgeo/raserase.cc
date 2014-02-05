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
 * Copyright 2003 - 2011 Peter Baumann / rasdaman GmbH.
 *
 * For more information please see <http://www.rasdaman.org>
 * or contact Peter Baumann via <baumann@rasdaman.com>.
 */

/*
 * Contributed to rasdaman by Alexander Herzig, Landcare Research New Zealand
 */

#include "config.h"
#include <sstream>
#include <string>
#include <algorithm>
#include <unistd.h>

#include "raserase.hh"
#include "include/globals.hh"

#define DEBUG_MAIN
#include "debug-clt.hh"

using namespace std;


void showEraseHelp()
{
    cout << endl << "raserase v1.0" << endl << endl;

    cout << "Usage: raserase {--coll <collection> [--oid <OID>] | --coverage <coverage_name>} "
              "[--conn <connection file>] "
              "[--ps-metadata]" << endl << endl;

    cout << " --coll        name of rasdaman collection to delete" << endl;
    cout << " --conn        connection file specifying rasdaman and postgres DB " << endl;
    cout << "               connection parameters" << endl;
    cout << " --coverage    delete rasdaman image which is exposed as 'coverage_name' " << endl;
    cout << "               by petascope web services" << endl;
    cout << " --oid         local object identifier (OID) of image to delete" << endl;
    cout << " --ps-metadata delete only petascope metadata, which is associated with " << endl;
    cout << "               the specified coverage (no image(s) are deleted!)" << endl;
    cout << endl;
}


int main(int argc, char** argv)
{
    SET_OUTPUT( true );
    const string ctx = "raserase::main()";

    ENTER(ctx);

    if (argc < 2)
    {
        showEraseHelp();
        return EXIT_SUCCESS;
    }

    string collname;
    double oid = -1;
    string connfile;
    string coverage;
    bool bMetaOnly = false;

    // parse command line arguments
    int arg = 1;
    while (arg < argc-1)
    {
        string theArg = argv[arg];
        transform(theArg.begin(), theArg.end(),
                       theArg.begin(), ::tolower);

        if (theArg == "--coll")
        {
            collname = argv[arg+1];
            if (collname.empty())
            {
                cerr << ctx
                << ": missing parameter for --coll: please "
                << "specify a target collection!" << endl;
                LEAVE(ctx);
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--oid")
        {
            oid = atof(argv[arg+1]);
            if (oid <= 0)
            {
                cerr << ctx << ": invalid OID specified!"
                << endl;
                LEAVE(ctx);
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--conn")
        {
            connfile = argv[arg+1];
            if (access(connfile.c_str(), R_OK) != 0)
            {
                cerr << ctx
                << ": invalid parameter for --conn: could "
                << "not access connection file '" << connfile << "'!"
                << endl;
                LEAVE(ctx);
                return EXIT_FAILURE;
            }
        }
        else if (theArg == "--coverage")
        {
            coverage = argv[arg+1];
        }
        else if (theArg == "--ps-metadata")
        {
            bMetaOnly = true;
        }
        else if (theArg == "--help")
        {
            showEraseHelp();
            LEAVE(ctx);
            return EXIT_SUCCESS;
        }
        arg++;
    }

    string lastarg = argv[argc-1];
    if (lastarg == "--help")
    {
        showEraseHelp();
        LEAVE(ctx);
        return EXIT_SUCCESS;
    }
    else if (lastarg == "--ps-metadata")
    {
        bMetaOnly = true;
    }

    // ---------------------------------------------------------------------
    // get the connection file and check readability
    if (connfile.empty())
    {
        connfile = string(getenv("HOME")) + "/" + RAS_USER_RESOURCEDIR + "/rasconnect";
        if (access(connfile.c_str(), R_OK) != 0)
        {
            cerr << ctx
            << ": could not access connection file '"
            << connfile << "'!" << endl;
            LEAVE(ctx);
            return EXIT_FAILURE;
        }
    }

    // --------------------------------------------------------------------------
    // that's what we've got

    TALK("SPECIFIED PARAMETERS: ...");
    TALK("collname:  " << collname);
    TALK("coverage:  " << coverage);
    TALK("oid:       " << oid);
    TALK("only meta: " << (bMetaOnly ? "true" : "false"));

    // ---------------------------------------------------------------------------
    // evaluate user specifications


    RasdamanConnector rasconn(connfile);
    RasdamanHelper2 helper(&rasconn);

    if (collname.empty() && coverage.empty())
    {
        cerr << ctx
        << ": please specify a collection or image (coverage) to delete!"
        << endl;
        showEraseHelp();
        LEAVE(ctx);
        return EXIT_FAILURE;
    }

    try
    {
        // coverage overwrite everything
        if (!coverage.empty())
        {
            oid = helper.getOIDFromCoverageName(coverage);
            if (oid < 0)
            {
                cerr << ctx
                << ": couldn't find an image referred to as '"
                << coverage << "' in the data base!"
                << endl;
                LEAVE(ctx);
                return EXIT_SUCCESS;
            }

            collname = helper.getCollectionNameFromOID(oid);
         }

        if (helper.doesCollectionExist(collname) == -1)
        {
            cerr << ctx
            << ": couldn't find collection '" << collname
            << "' in the data base!"
            << endl;
            LEAVE(ctx);
            return EXIT_SUCCESS;
        }

        if (!helper.deletePSMetadata(collname, oid))
        {
            cerr << ctx
            << ": couldn't delete petascope metadata!"
            << endl;
            //LEAVE(ctx);
            //return EXIT_FAILURE;
        }

        if (!bMetaOnly)
        {
            // drop either image or collection
            if (oid == -1)
                helper.dropCollection(collname);
            else
                helper.dropImage(collname, oid);
        }
    }
    catch(r_Error& re)
    {
        cerr << ctx << ": "
        << re.what() << endl;
        LEAVE(ctx);
        return EXIT_FAILURE;
    }

    LEAVE(ctx);
    return EXIT_SUCCESS;
}