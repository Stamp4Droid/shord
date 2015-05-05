/** \file
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
#ifndef _GLOBAL_H
#define _GLOBAL_H

#ifdef __cplusplus
extern "C" {
#endif

#ifdef DEBUG
#include<stdio.h>		/* perror, printf */
#endif

#include "typedefs.h"		/* basic type definitions */

#include <malloc.h>		/* calloc */
#include <setjmp.h>		/* calloc */
#include <amore/buffer.h>	/* newbuf */
extern jmp_buf _jmp;

#include <amore/liberror.h>

void OUT_OF_MEM();


/** get version string of libAMoRE
 */
const char* libamore_version();

/* ------------------------------------------------- extern variables */

#include <amore/ext.h>

/* ------------------------------------  some funcs for basic typedefs */

/** allocates a new array_of_int of mkrfin
 */
mrkfin newfinal(posint a);

/** allocates a new array_of_int
 */
array_of_int newar(posint a);

/** allocates a new array_of_int of boole
 */
boole *newbarray(posint a);

/** allocates a new array_of_int_array
 */
array_of_int_array newarray_of_int1(posint a);


/* ----------------------- other global func */
/** posint to ascii
 */
char *pi2a(posint pi);

/** Converts a posint n to a string
 */
char* pi2s(posint n);

/* -----------------------------------------------------  some macros */
# define FALSE	(0)
# define TRUE	(1)

/** free alloc'ed memory
 */
#define dispose(A)		(free((char *)A))

#define SIZEOFBYTE 8

/* --------------------- constants */

#include "cons.h"

/* used in longjmp, setjmp-handler
 * remark: the only setjump is in the evalkey() function
 */
/** operating system out of memory */
#define EXCEPTION_OUT_OF_MEMORY 1
/** buffer is too small */
#define EXCEPTION_PARAM_TOO_SMALL 2
/** user pressed control-C */
#define EXCEPTION_USER_ABORT 3

#ifdef __cplusplus
} // extern "C"
#endif

#endif
