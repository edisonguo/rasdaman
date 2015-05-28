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
 *      Class implementation. For further information see qlparser/qtcaseequality.hh
 *
 * COMMENTS:
 *
 ************************************************************/

#include "qlparser/qtcaseequality.hh"

/**
 * Class constructor.
 * @param commonOperand the operand which is common to several equality operations.
 * @param input2 the second operand.
 */
QtCaseEquality::QtCaseEquality(QtOperation* commonOperand, QtOperation* input2)
    : QtEqual(commonOperand, input2)  {
    commonOperandDeleted = false;
}

/**
 * Setter for the commonOperandDeleted attribute.
 * @param commonOperandDeleted
 */
void QtCaseEquality::setCommonOperadDeleted(bool commonOperandDeleted){
    this->commonOperandDeleted = commonOperandDeleted;
}

/**
 * Destructor override. If the commonOperand has already been deleted, mark it
 * as NULL.
 */
QtCaseEquality::~QtCaseEquality() {
    if(commonOperandDeleted){
       input1 = NULL;
    }
}