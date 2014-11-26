/** \file
 *  \brief parser for AMoRE [generalized] regular expressions
 *
 *  Copyright (c) ?    - 2000 Lehrstuhl fuer Informatik VII, RWTH Aachen
 *  Copyright (c) 2000 - 2002 Burak Emir
 *  This file is part of the libAMoRE library.
 *
 *  libAMoRE is  free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with the GNU C Library; if not, write to the Free
 *  Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 *  02111-1307 USA.
 */

#ifndef _PARSE_H
#define _PARSE_H

#include <amore/parser_types.h>

#ifdef __cplusplus
extern "C" {
#endif

/** parser
 *  @ingroup PARSER
 */
PARSE_RESULT parser(PARSE_INPUT instruct);

#ifdef __cplusplus
} // extern "C"
#endif

#endif
