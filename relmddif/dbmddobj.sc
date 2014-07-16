// This is -*- C++ -*-

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
 *
 *
 * COMMENTS:
 *
 ************************************************************/


#include "config.h"
#include "reladminif/oidif.hh"
#include "reladminif/dbref.hh"
#include "reladminif/sqlerror.hh"
#include "reladminif/externs.h"
#include "reladminif/sqlglobals.h"
#include "raslib/rmdebug.hh"
#include "catalogmgr/typefactory.hh"
#include "relcatalogif/mddbasetype.hh"
#include "relcatalogif/basetype.hh"
#include "reladminif/objectbroker.hh"
#include "relcatalogif/dbminterval.hh"
#include "relstorageif/dbstoragelayout.hh"
#include "dbmddobj.hh"
#include "relindexif/indexid.hh"
#include "indexmgr/indexds.hh"
#include "reladminif/sqlitewrapper.hh"

#include "debug-srv.hh"

DBMDDObj::DBMDDObj()
: DBObject(),
myDomain(NULL),
objIxId(),
mddType(NULL),
persistentRefCount(0),
storageLayoutId(new DBStorageLayout())
{
    RMDBGENTER(7, RMDebug::module_mddif, "DBMDDObj", "DBMDDObj()");
    ENTER("DBMDDObj::DBMDDObj");

    objecttype = OId::MDDOID;
    myDomain = new DBMinterval();
    myDomain->setPersistent(true);
    storageLayoutId->setPersistent(true);

    LEAVE("DBMDDObj::DBMDDObj");
    RMDBGEXIT(7, RMDebug::module_mddif, "DBMDDObj", "DBMDDObj()");
}

DBMDDObj::DBMDDObj(const OId& id) throw (r_Error)
: DBObject(id),
myDomain(NULL),
objIxId(),
persistentRefCount(0),
mddType(NULL),
storageLayoutId(0LL)
{
    RMDBGENTER(7, RMDebug::module_mddif, "DBMDDObj", "DBMDDObj(" << myOId << ")");
    ENTER("DBMDDObj::DBMDDObj, id=" << id);

    objecttype = OId::MDDOID;
    readFromDb();

    LEAVE("DBMDDObj::DBMDDObj");
    RMDBGEXIT(7, RMDebug::module_mddif, "DBMDDObj", "DBMDDObj(" << myOId << ")");
}

DBMDDObj::DBMDDObj(const MDDBaseType* newMDDType,
                   const r_Minterval& domain,
                   const DBObjectId& newObjIx,
                   const DBStorageLayoutId& newSL,
                   const OId& newOId) throw (r_Error)
: DBObject(),
objIxId(newObjIx.getOId()),
mddType(newMDDType),
persistentRefCount(0),
storageLayoutId(newSL),
myDomain(NULL)
{
    RMDBGENTER(7, RMDebug::module_mddif, "DBMDDObj", "DBMDDObj(" << newMDDType->getName() << ", " << domain << ", Ix " << newObjIx.getOId() << ", Sl " << newSL.getOId() << ", " << newOId << ")");
    ENTER("DBMDDObj::DBMDDObj");

    objecttype = OId::MDDOID;
    long long testoid1;

    testoid1 = newOId.getCounter();

    // (1) --- fetch tuple from database
    SQLiteQuery query("SELECT MDDId FROM RAS_MDDOBJECTS WHERE MDDId = %lld", testoid1);
    if (query.nextRow())
    {
        ((DBObjectId) newObjIx)->setPersistent(false);
        ((DBObject*) newSL.ptr())->setPersistent(false);
        RMInit::logOut << "DBMDDObj::DBMDDObj() - mdd object: "
                << testoid1 << " already exists in the database." << endl;
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "mdd object already exists in the database.");
    }

    if (newMDDType->isPersistent())
        mddType = newMDDType;
    else
        mddType = (const MDDBaseType*) TypeFactory::addMDDType(newMDDType);
    myDomain = new DBMinterval(domain);
    _isPersistent = true;
    _isModified = true;
    myOId = newOId;
    setPersistent(true);

    LEAVE("DBMDDObj::DBMDDObj, " << newMDDType->getName() << ", " << domain << ", Ix " << newObjIx.getOId() << ", " << myOId << ") " << myOId);
    RMDBGEXIT(7, RMDebug::module_mddif, "DBMDDObj", "DBMDDObj(" << newMDDType->getName() << ", " << domain << ", Ix " << newObjIx.getOId() << ", " << myOId << ") " << myOId);
}

DBMDDObj::DBMDDObj(const DBMDDObj& old)
: DBObject(old),
objIxId(),
mddType(NULL),
persistentRefCount(0),
storageLayoutId(),
myDomain(NULL)
{
    RMDBGENTER(7, RMDebug::module_mddif, "DBMDDObj", "DBMDDObj(const DBMDDObj&" << old.getOId() << ")");
    ENTER("DBMDDObj::DBMDDObj");

    if (old.myDomain)
    {
        if (old.myDomain->isPersistent())
        {
            myDomain = (DBMinterval*) ObjectBroker::getObjectByOId(old.myDomain->getOId());
        }
        else
        {
            myDomain = new DBMinterval(*old.myDomain);
            myDomain->setPersistent(true);
        }
    }
    else
    {
        myDomain = NULL;
    }

    objIxId = old.objIxId;
    storageLayoutId = old.storageLayoutId;
    persistentRefCount = old.persistentRefCount;
    mddType = old.mddType;

    LEAVE("DBMDDObj::DBMDDObj");
    RMDBGEXIT(7, RMDebug::module_mddif, "DBMDDObj", "DBMDDObj(const DBMDDObj& " << old.getOId() << ")");
}

DBMDDObj::DBMDDObj(const MDDBaseType* newMDDType, const r_Minterval& domain, const DBObjectId& newObjIx, const DBStorageLayoutId& newSL)
: DBObject(),
objIxId(newObjIx),
mddType(NULL),
persistentRefCount(0),
storageLayoutId(newSL),
myDomain(NULL)
{
    RMDBGENTER(7, RMDebug::module_mddif, "DBMDDObj", "DBMDDObj(" << newMDDType->getName() << ", " << domain << ", Ix " << newObjIx.getOId() << ", Sl " << newSL.getOId() << ")");
    ENTER("DBMDDObj::DBMDDObj");

    objecttype = OId::MDDOID;
    myDomain = new DBMinterval(domain);
    mddType = newMDDType;

    LEAVE("DBMDDObj::DBMDDObj");
    RMDBGEXIT(7, RMDebug::module_mddif, "DBMDDObj", "DBMDDObj(" << newMDDType->getName() << ", " << domain << ", Ix " << newObjIx.getOId() << ") " << myOId);
}

DBMDDObj::~DBMDDObj()
{
    RMDBGENTER(7, RMDebug::module_mddif, "DBMDDObj", "~DBMDDObj() " << myOId);
    ENTER("DBMDDObj::~DBMDDObj");

    validate();
    if (myDomain)
        delete myDomain;
    myDomain = NULL;

    LEAVE("DBMDDObj::~DBMDDObj");
    RMDBGEXIT(7, RMDebug::module_mddif, "DBMDDObj", "~DBMDDObj() " << myOId);
}

DBStorageLayoutId
DBMDDObj::getDBStorageLayout() const
{
    return storageLayoutId;
}

r_Bytes
DBMDDObj::getMemorySize() const
{
    return DBObject::getMemorySize() + sizeof (long) + sizeof (MDDBaseType*) + sizeof (DBMinterval*) + sizeof (OId) + myDomain->getMemorySize() + mddType->getMemorySize() + sizeof (OId);
}

const MDDBaseType*
DBMDDObj::getMDDBaseType() const
{
    return mddType;
}

const BaseType*
DBMDDObj::getCellType() const
{
    return mddType->getBaseType();
}

r_Dimension
DBMDDObj::dimensionality() const
{
    return myDomain->dimension();
}

void
DBMDDObj::setCached(bool ic)
{
    DBObject::setCached(ic);
    if (myDomain)
        myDomain->setCached(ic);
}

//this should only receive an setPersistent(false)

void
DBMDDObj::setPersistent(bool o) throw (r_Error)
{
    RMDBGENTER(7, RMDebug::module_mddif, "DBMDDObj", "setPersistent(" << (int) o << ") " << myOId);
    ENTER("DBMDDObj::setPersistent, o=" << o);

    DBObject::setPersistent(o);
    if (!o)
        setCached(false);
    if (myDomain)
        myDomain->setPersistent(o);
    DBObjectId testIx(objIxId);
    if (testIx.is_null())
    {
        RMDBGMIDDLE(0, RMDebug::module_mddif, "DBMDDObj", "index object is not valid " << myOId << " index " << objIxId.getOId());
        throw r_Error(INDEX_OF_MDD_IS_NULL);
    }
    else
    {
        testIx->setPersistent(o);
        if (o)
        {
            objIxId.release();
        }
    }

    if (storageLayoutId.is_null())
    {
        RMDBGMIDDLE(0, RMDebug::module_mddif, "DBMDDObj", "layout object is not valid " << myOId << " layout " << storageLayoutId.getOId());
        RMInit::logOut << "DBMDDObj::setPersistent() layout object is not valid" << endl;
        throw r_Error(STORAGE_OF_MDD_IS_NULL);
    }
    else
    {
        storageLayoutId->setPersistent(o);
    }
    if (o && !mddType->isPersistent())
        mddType = (const MDDBaseType*) TypeFactory::addMDDType(mddType);

    LEAVE("DBMDDObj::setPersistent, o=" << o);
    RMDBGEXIT(7, RMDebug::module_mddif, "DBMDDObj", "setPersistent(" << (int) o << ") " << myOId);
}

const char*
DBMDDObj::getCellTypeName() const
{
    return mddType->getBaseType()->getTypeName();
}

r_Minterval
DBMDDObj::getDefinitionDomain() const
{
    return *myDomain;
}

r_Bytes
DBMDDObj::getHeaderSize() const
{
    r_Bytes sz = sizeof (MDDBaseType*) + sizeof (r_Minterval*) + sizeof (DBObjectId) + sizeof (DBObject) + sizeof (DBStorageLayoutId);
    return sz;
}

void
DBMDDObj::printStatus(unsigned int level, ostream& stream) const
{
    DBObject::printStatus(level, stream);
    stream << *myDomain << endl;
    mddType->printStatus(level + 1, stream);
    DBObjectId testIx(objIxId);
    if (!testIx.is_null())
        testIx->printStatus(level + 1, stream);
    else
        stream << "index is invalid " << objIxId.getOId();
    if (storageLayoutId.is_null())
        stream << "storagelayout is invalid " << storageLayoutId.getOId();
    else
        storageLayoutId->printStatus(level + 1, stream);
}

void
DBMDDObj::setIx(const DBObjectId& newIx)
{
    ENTER("DBMDDObj::setIx");

    if (isPersistent())
    {
        if (objIxId.getOId() != newIx.getOId())
        {
            objIxId = newIx.getOId();
            setModified();
        }
    }
    else
    {
        objIxId = newIx;
    }

    LEAVE("DBMDDObj::setIx");
}

void
DBMDDObj::updateInDb() throw (r_Error)
{
    RMDBGENTER(7, RMDebug::module_mddif, "DBMDDObj", "updateInDb() " << myOId);
    ENTER("DBMDDObj::updateInDb");

    long long mddoid3;
    long long objindex3;
    long persRefCount3;
    long long nullvalueoid;
    long long oldnullvalueoid;
    long long settypeoid;
    long count;

    objindex3 = objIxId.getOId();
    mddoid3 = myOId.getCounter();
    persRefCount3 = persistentRefCount;

    SQLiteQuery::executeWithParams("UPDATE RAS_MDDOBJECTS SET PersRefCount = %ld, NodeOId = %lld WHERE MDDId = %lld",
                                   persRefCount3, objindex3, mddoid3);

    DBObject::updateInDb();

    LEAVE("DBMDDObj::updateInDb");
    RMDBGEXIT(7, RMDebug::module_mddif, "DBMDDObj", "updateInDb() " << myOId);
}

void
DBMDDObj::insertInDb() throw (r_Error)
{
    RMDBGENTER(7, RMDebug::module_mddif, "DBMDDObj", "insertInDb() " << myOId);
    ENTER("DBMDDObj::insertInDb");

    long long mddoid;
    long long basetypeid;
    long long storage;
    long long domainid;
    long long objindex;
    long persRefCount;

    storage = storageLayoutId->getOId();
    objindex = objIxId.getOId();
    mddoid = myOId.getCounter();
    basetypeid = mddType->getOId();
    domainid = myDomain->getOId().getCounter();
    persRefCount = persistentRefCount;

    SQLiteQuery::executeWithParams("INSERT INTO RAS_MDDOBJECTS ( MDDId, BaseTypeOId, DomainId, PersRefCount, NodeOId, StorageOId) VALUES (%lld, %lld, %lld, %ld, %lld, %lld)",
                                   mddoid, basetypeid, domainid, persRefCount, objindex, storage);
    DBObject::insertInDb();

    LEAVE("DBMDDObj::insertInDb");
    RMDBGEXIT(7, RMDebug::module_mddif, "DBMDDObj", "insertInDb() " << myOId);
}

void
DBMDDObj::deleteFromDb() throw (r_Error)
{
    RMDBGENTER(7, RMDebug::module_mddif, "DBMDDObj", "deleteFromDb() " << myOId);
    ENTER("DBMDDObj::deleteFromDb");

    long long mddoid1 = myOId.getCounter();
    SQLiteQuery::executeWithParams("DELETE FROM RAS_MDDOBJECTS WHERE MDDId = %lld", mddoid1);
    DBObject::deleteFromDb();

    LEAVE("DBMDDObj::deleteFromDb");
    RMDBGEXIT(7, RMDebug::module_mddif, "DBMDDObj", "deleteFromDb() " << myOId);
}

void
DBMDDObj::readFromDb() throw (r_Error)
{
    RMDBGENTER(7, RMDebug::module_mddif, "DBMDDObj", "readFromDb() " << myOId);
    ENTER("DBMDDObj::readFromDb");

#ifdef RMANBENCHMARK
    DBObject::readTimer.resume();
#endif
    long long mddoid2;
    long long basetypeid2;
    long long domainid2;
    long long objindex2;
    long persRefCount2;
    long long storage2;

    mddoid2 = myOId.getCounter();

    SQLiteQuery query("SELECT BaseTypeOId, DomainId, PersRefCount, NodeOId, StorageOId FROM RAS_MDDOBJECTS WHERE MDDId = %lld", mddoid2);
    if (query.nextRow())
    {
        basetypeid2 = query.nextColumnLong();
        domainid2 = query.nextColumnLong();
        persRefCount2 = query.nextColumnLong();
        objindex2 = query.nextColumnLong();
        storage2 = query.nextColumnLong();
    }
    else
    {
        RMInit::logOut << "DBMDDObj::readFromDb() - mdd object: "
                << mddoid2 << " not found in the database." << endl;
        throw r_Ebase_dbms(SQLITE_NOTFOUND, "mdd object not found in the database.");
    }

    objIxId = OId(objindex2);
    storageLayoutId = OId(storage2);
    persistentRefCount = persRefCount2;
    mddType = (MDDBaseType*) ObjectBroker::getObjectByOId(OId(basetypeid2));
    myDomain = (DBMinterval*) ObjectBroker::getObjectByOId(OId(domainid2, OId::DBMINTERVALOID));
    myDomain->setCached(true);

    DBObject::readFromDb();
#ifdef RMANBENCHMARK
    DBObject::readTimer.pause();
#endif

    LEAVE("DBMDDObj::readFromDb");
    RMDBGEXIT(7, RMDebug::module_mddif, "DBMDDObj", "readFromDb() " << myOId);
}

DBObjectId
DBMDDObj::getDBIndexDS() const
{
    return objIxId;
}

int
DBMDDObj::getPersRefCount() const
{
    return persistentRefCount;
}

void
DBMDDObj::incrementPersRefCount()
{
    persistentRefCount++;
    setModified();
}

void
DBMDDObj::decrementPersRefCount()
{
    persistentRefCount--;
    if (persistentRefCount == 0)
        setPersistent(false);
    setModified();
}
