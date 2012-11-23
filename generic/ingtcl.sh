
/* 
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.                   
 *                                                                       
 * Copyright (c) 2012 Tomasz Konojacki
 *
 */

#ifndef __INGTCL_H__
#   pragma once

#   define __INGTCL_H__

#   include <tcl.h>
#   include <stdlib.h>
#   include <stdio.h>
#   include <string.h>

EXEC SQL INCLUDE SQLDA;
EXEC SQL INCLUDE SQLCA;

#   define INGTCL_VERSION "0.3"

#   ifndef IISQ_BOO_TYPE
    /* For 9.3 and older */
#       define IISQ_BOO_TYPE -9998
#   endif

typedef struct cursor {
    short is_closed;
    char name[12]; /* statement name */
    IISQLDA *sqlda; /* statement descriptor */ 
    IISQLDA *ph_sqlda;
} ing_cursor_t;

/*** Function prototypes ***/


/* Tcl commands */


static int DeleteCursor_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[]);
static int SetAutoCommit_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[]);
static int Rollback_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[]);
static int Commit_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[]);
static int Disconnect_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[]);
static int IsCursorClosed_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[]);
static int CloseCursor_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[]);
static int Do_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[]);
static int Execute_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[]);
static int FetchColumnNames_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[]);
static int FetchRow_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[]);
static int Prepare_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[]);
static int Connect_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[]);

/* Tcl entrypoint */
int DLLEXPORT Ingtcl_Init(Tcl_Interp *interp);

/* Functions for use in C */
static int close_cursor(ing_cursor_t *cursor);
static int sql_check(void);

#endif
