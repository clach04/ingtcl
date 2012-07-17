
/* 
 * See the file "license.terms" for information on usage and redistribution
 * of this file, and for a DISCLAIMER OF ALL WARRANTIES.                   
 *                                                                       
 * Copyright (c) 2012 Tomasz Konojacki
 *
 */

EXEC SQL INCLUDE 'ingtcl.sh';

static int current_session = 1;
static int next_session = 1;

static int current_statement = 1;
static int next_statement = 1;

EXEC SQL BEGIN DECLARE SECTION;
    static char errstr[1024];
EXEC SQL END DECLARE SECTION;


static int 
DeleteCursor_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
/* 
 * (For use from Tcl)
 *
 * IngTcl::delete_cursor $session $statement
 *
 * This function deletes specified closed cursor.
 * On success it returns true to Tcl, otherwise it throws an exception.
 *
 * Well, it only frees cursor, not sqldata, so memory leaks are present.
 *
 */
{
    ing_cursor_t *cursor;
    IISQLDA *sqlda, *ph_sqlda;
    int i = 0; /* Counter */
    int number_of_arguments = objc - 1;

    if (number_of_arguments != 2)
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: delete_cursor: Expected exactly two arguments, see usage:\n\n"
                "IngTcl::delete_cursor $session $cursor\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    sscanf(Tcl_GetString(objv[2]), "%p", &cursor);

    sqlda = cursor->sqlda;
    ph_sqlda = cursor->ph_sqlda;

    ckfree(sqlda);
    ckfree(ph_sqlda);
    ckfree((char*)cursor);

    Tcl_SetObjResult(interp, Tcl_NewBooleanObj(1));
    return TCL_OK;
}

static int 
SetAutoCommit_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
/* 
 * (For use from Tcl)
 *
 * IngTcl::set_autocommit $session $enable_autocommit
 *
 * This function enables or disables autocommit.
 * On success it returns true to Tcl, on failure it throws exception.
 *
 */
{
    EXEC SQL BEGIN DECLARE SECTION;
        int session;
    EXEC SQL END DECLARE SECTION;
    int enable_autocommit;
    int number_of_arguments = objc - 1;

    if (number_of_arguments != 2)
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: set_autocommit: Expected exactly two arguments, see usage:\n\n"
                "IngTcl::set_autocommit $session $bool\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    if (Tcl_GetIntFromObj(interp, objv[1], &session) != TCL_OK)
    {
        /* If it is impossible to get int from object, then it is not integer */
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: set_autocommit: First supplied argument to IngTcl::set_autocommit is not integer, see usage:\n\n"
                "IngTcl::set_autocommit $session\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    if (Tcl_GetBooleanFromObj(interp, objv[2], &enable_autocommit) != TCL_OK)
    {
        /* If it is impossible to get int from object, then it is not integer */
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: set_autocommit: First supplied argument to IngTcl::set_autocommit is not integer, see usage:\n\n"
                "IngTcl::set_autocommit $session\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    EXEC SQL SET_SQL(SESSION = :session);
    
    if (enable_autocommit)
    {
        EXEC SQL SET AUTOCOMMIT ON;
    }
    else
    {
        EXEC SQL SET AUTOCOMMIT OFF;
    }

    if (!sql_check())
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(errstr, -1));
        return TCL_ERROR;
    }

    Tcl_SetObjResult(interp, Tcl_NewBooleanObj(1));
    return TCL_OK;
}

static int 
Rollback_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
/* 
 * (For use from Tcl)
 *
 * IngTcl::rollback $session
 *
 * This function rollbacks transaction.
 * On success it returns true to Tcl, on failure it throws exception.
 *
 */
{
    EXEC SQL BEGIN DECLARE SECTION;
        int session;
    EXEC SQL END DECLARE SECTION;
    int number_of_arguments = objc - 1;

    if (number_of_arguments != 1)
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: rollback: Expected exactly one argument, see usage:\n\n"
                "IngTcl::rollback $session\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    if (Tcl_GetIntFromObj(interp, objv[1], &session) != TCL_OK)
    {
        /* If it is impossible to get int from object, then it is not integer */
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: rollback: First supplied argument to IngTcl::rollback is not integer, see usage:\n\n"
                "IngTcl::rollback $session\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    EXEC SQL SET_SQL(SESSION = :session);
    EXEC SQL ROLLBACK;

    if (!sql_check())
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(errstr, -1));
        return TCL_ERROR;
    }

    Tcl_SetObjResult(interp, Tcl_NewBooleanObj(1));
    return TCL_OK;

}

static int 
Commit_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
/* 
 * (For use from Tcl)
 *
 * IngTcl::commit $session
 *
 * This function commits transaction.
 * On success it returns true to Tcl, on failure it throws exception.
 *
 */
{
    EXEC SQL BEGIN DECLARE SECTION;
        int session;
    EXEC SQL END DECLARE SECTION;
    int number_of_arguments = objc - 1;

    if (number_of_arguments != 1)
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: commit: Expected exactly one argument, see usage:\n\n"
                "IngTcl::commit $session\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    if (Tcl_GetIntFromObj(interp, objv[1], &session) != TCL_OK)
    {
        /* If it is impossible to get int from object, then it is not integer */
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: commit: First supplied argument to IngTcl::commit is not integer, see usage:\n\n"
                "IngTcl::commmit $session\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    EXEC SQL SET_SQL(SESSION = :session);
    EXEC SQL COMMIT;

    if (!sql_check())
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(errstr, -1));
        return TCL_ERROR;
    }

    Tcl_SetObjResult(interp, Tcl_NewBooleanObj(1));
    return TCL_OK;

}

static int 
Disconnect_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
/* 
 * (For use from Tcl)
 *
 * IngTcl::disconnect $session
 *
 * This function closes database connection.
 * On success it returns true to Tcl, on failure it throws exception.
 *
 */
{
    EXEC SQL BEGIN DECLARE SECTION;
        int session, transaction_active;
    EXEC SQL END DECLARE SECTION;
    int number_of_arguments = objc - 1;
    
    if (number_of_arguments != 1)
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: disconnect: Expected exactly one argument, see usage:\n\n"
                "IngTcl::disconnect $session\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    if (Tcl_GetIntFromObj(interp, objv[1], &session) != TCL_OK)
    {
        /* If it is impossible to get int from object, then it is not integer */
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: disconnect: First supplied argument to IngTcl::disconnect is not integer, see usage:\n\n"
                "IngTcl::disconnect $session\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    EXEC SQL SET_SQL(SESSION = :session);

    /* If transaction is active, rollback it */
    EXEC SQL INQUIRE_INGRES(:transaction_active = TRANSACTION);

    if (transaction_active == 1)
    {
        EXEC SQL ROLLBACK;
    }

    EXEC SQL DISCONNECT;

    Tcl_SetObjResult(interp, Tcl_NewBooleanObj(1));
    return TCL_OK;
}

static int 
CloseCursor_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
/* 
 * (For use from Tcl)
 *
 * IngTcl::close_cursor $session $statement
 *
 * This function closes specified cursor.
 * On success it returns true to Tcl, otherwise it throws an exception.
 *
 */
{
    ing_cursor_t *cursor;
    int number_of_arguments = objc - 1;

    if (number_of_arguments != 2)
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: close_cursor: Expected exactly two arguments, see usage:\n\n"
                "IngTcl::close_cursor $session $cursor\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    sscanf(Tcl_GetString(objv[2]), "%p", &cursor);

    close_cursor(cursor);

    Tcl_SetObjResult(interp, Tcl_NewBooleanObj(1));
    return TCL_OK;
}

static int 
Do_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
/* 
 * (For use from Tcl)
 *
 * IngTcl::do $session $statement
 *
 * This function executes provided statement. It NEEDS NOT to be SELECT 
 * statement.
 * On success it returns true to Tcl, otherwise it throws an exception.
 *
 */
{
    EXEC SQL BEGIN DECLARE SECTION;
        int session;
        char *statement;
    EXEC SQL END DECLARE SECTION;
    int number_of_arguments = objc - 1;
    
    /* Process arguments to function */

    if (number_of_arguments != 2)
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: do: Expected exactly two arguments, see usage:\n\n"
                "IngTcl::do $session $statement\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    if (Tcl_GetIntFromObj(interp, objv[1], &session) != TCL_OK)
    {
        /* If it is impossible to get int from object, then it is not integer */
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: do: First supplied argument to IngTcl::do is not integer, see usage:\n\n"
                "IngTcl::do $session $statement\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    statement = Tcl_GetString(objv[2]);

    /* Execute statement */
    EXEC SQL SET_SQL(SESSION = :session);
    EXEC SQL EXECUTE IMMEDIATE :statement;

    /* Check for errors */
    if (!sql_check())
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(errstr, -1));
        return TCL_ERROR;
    }

    Tcl_SetObjResult(interp, Tcl_NewBooleanObj(1));

    return TCL_OK;

}

static int 
Execute_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
/* 
 * (For use from Tcl)
 *
 * IngTcl::execute $session $statement $arg1 $arg2 ...
 *
 * This function fetches executes statement prepared by IngTcl::prepare. 
 * On success it returns true to Tcl, otherwise it throws an exception.
 *
 */
{
    EXEC SQL BEGIN DECLARE SECTION;
        int session;
        char *statement_name;
    EXEC SQL END DECLARE SECTION;
    IISQLDA *sqlda, *ph_sqlda; /* First one is for statement, second for placeholders */
    ing_cursor_t *cursor;
    int i = 0; /* Counter */
    int number_of_arguments = objc - 1;
    
    if (number_of_arguments < 2)
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: execute: Expected at least two arguments, see usage:\n\n"
                "IngTcl::execute $session $statement $arg1 $arg2 ...\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    if (Tcl_GetIntFromObj(interp, objv[1], &session) != TCL_OK)
    {
        /* If it is impossible to get int from object, then it is not integer */
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: execute: First supplied argument to IngTcl::execute is not integer, see usage:\n\n"
                "IngTcl::execute $session $statement $arg1 $arg2 ...\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    /* Retrieve pointer to cursor */
    sscanf(Tcl_GetString(objv[2]), "%p", &cursor);
    
    if (cursor->is_closed)
    {
        /* You can't use closed cursor */
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: execute: You can't use closed cursor:\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    sqlda = cursor->sqlda;
    ph_sqlda = cursor->ph_sqlda;
    statement_name = (char*)&cursor->name;

    EXEC SQL SET_SQL(SESSION = :session);

    /* Number of arguments must equal (number of placeholders + 2) */
    if ((ph_sqlda->sqld > 0) && ((number_of_arguments - 2) != ph_sqlda->sqld))
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: execute: In prepared statement you used more (or less)\n"
                "placeholders than number of values you supplied to execute\n",
                -1
            )
        );
        return TCL_ERROR;            
    }
    else
    {
        for (i = 3; i <= number_of_arguments; ++i)
        {
            int *integer;
            double *floating_point;
            char *string;
            IISQLVAR *var;

            var = &ph_sqlda->sqlvar[i - 3];
            /* Allocate memory for variables */
            integer = (int*)ckalloc(sizeof(integer));
            floating_point = (double*)ckalloc(sizeof(floating_point));
            
            var->sqlind = NULL;
            /* Populate IISQLVAR structure */
            if (Tcl_GetIntFromObj(interp, objv[i], integer) != TCL_OK)
            {
                if (Tcl_GetDoubleFromObj(interp, objv[i], floating_point) != TCL_OK)
                {
                    /* String type */
                    int length;
                    string = Tcl_GetStringFromObj(objv[i], &length);
                    
                    var->sqltype = IISQ_CHA_TYPE;
                    var->sqllen = (short)length;
                    var->sqldata = string;
                }
                else
                {
                    /* Double precision type */
                    var->sqltype = IISQ_FLT_TYPE;
                    var->sqllen = (short)sizeof(double);
                    var->sqldata = (char*)floating_point;
                }
            }
            else {
                /* Integer */
                var->sqltype = IISQ_INT_TYPE;
                var->sqllen = (short)sizeof(int);
                var->sqldata = (char*)integer;
            }
        }
    }

    if (sqlda->sqld == 0) 
    {
        /* Non-SELECT statement */
        if (ph_sqlda->sqld > 0)
        {
            EXEC SQL EXECUTE :statement_name USING DESCRIPTOR ph_sqlda;
        }
        else {
            EXEC SQL EXECUTE :statement_name;
        }
    }
    else {
        /* SELECT statement */
        EXEC SQL DECLARE :statement_name CURSOR FOR :statement_name;

        if (ph_sqlda->sqld > 0)
        {
            EXEC SQL OPEN :statement_name USING DESCRIPTOR ph_sqlda;
        }
        else
        {
            EXEC SQL OPEN :statement_name;
        }
    }
    
    /* Check for errors */
    if (!sql_check())
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(errstr, -1));
        return TCL_ERROR;
    }

    Tcl_SetObjResult(interp, Tcl_NewBooleanObj(1));

    return TCL_OK;
}

static int 
FetchRow_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
/* 
 * (For use from Tcl)
 *
 * IngTcl::fetch_row $session $statement
 *
 * This function fetches row from database, on success it returns to Tcl list 
 * containing table row, on fail it throws exception.
 *
 */
{
    EXEC SQL BEGIN DECLARE SECTION;
        int session;
    EXEC SQL END DECLARE SECTION;
    Tcl_Obj *row; /* List containing row, it will be returned to Tcl on success */
    IISQLDA *sqlda;
    char *statement_name;
    ing_cursor_t *cursor;
    int i = 0; /* Counter */
    int number_of_arguments = objc - 1;

    if (number_of_arguments != 2)
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: fetch_row: Expected excatly arguments, see usage:\n\n"
                "IngTcl::fetch_row $session $statement\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    if (Tcl_GetIntFromObj(interp, objv[1], &session) != TCL_OK)
    {
        /* If it is impossible to get int from object, then it is not integer */
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: fetch_row: First supplied argument to IngTcl::fetch_row is not integer, see usage:\n\n"
                "IngTcl::execute $session $statement\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    /* Retrieve pointer to cursor */
    sscanf(Tcl_GetString(objv[2]), "%p", &cursor);

    /* Retrieve statement_name */
    statement_name = (char*)&cursor->name;

    if (cursor->is_closed)
    {
        /* You can't use closed cursor */
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: fetch_row: You can't use closed cursor:\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    sqlda = cursor->sqlda;

    /* Check whether it is SELECT statement */
    if (sqlda->sqld == 0)
    {
        /* If sqld equals 0 then it is not SELECT statement */
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: fetch_row: This is not SELECT statement:\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    
    /* Initialize memory for row which we want to fetch */
    for (i = 0; i < sqlda->sqld; ++i)
    {
        IISQLVAR *var;

        var = &sqlda->sqlvar[i];

        if(var->sqltype < 0)
            var->sqlind = (short*)ckalloc(sizeof(short));
        else
            var->sqlind = NULL;

        var->sqltype = abs(var->sqltype);

        switch(var->sqltype) {
        case IISQ_INT_TYPE:
            var->sqllen = sizeof(int);
            var->sqldata = ckalloc(sizeof(int));
            break;
        case IISQ_MNY_TYPE:
        case IISQ_DEC_TYPE:
        case IISQ_FLT_TYPE:
            var->sqllen = sizeof(double);
            var->sqldata = ckalloc(sizeof(double));
            var->sqltype = IISQ_FLT_TYPE;
            break;
        case IISQ_DTE_TYPE:
            var->sqllen = IISQ_DTE_LEN;
            var->sqldata = ckalloc(var->sqllen);
            var->sqltype = IISQ_VCH_TYPE;
            break;
        case IISQ_TSW_TYPE:
            var->sqllen = IISQ_TSW_LEN;
            var->sqldata = ckalloc(var->sqllen);
            var->sqltype = IISQ_VCH_TYPE;
            break;
        case IISQ_TSWO_TYPE:
            var->sqllen = IISQ_TSWO_LEN;
            var->sqldata = ckalloc(var->sqllen);
            var->sqltype = IISQ_VCH_TYPE;
            break;
        case IISQ_TSTMP_TYPE:
            var->sqllen = IISQ_TSTMP_LEN;
            var->sqldata = ckalloc(var->sqllen);
            var->sqltype = IISQ_VCH_TYPE;
            break;
        case IISQ_ADTE_TYPE:
            var->sqllen = IISQ_ADTE_LEN;
            var->sqldata = ckalloc(var->sqllen);
            var->sqltype = IISQ_VCH_TYPE;
            break;
        case IISQ_TMWO_TYPE:
            var->sqllen = IISQ_TMWO_LEN;
            var->sqldata = ckalloc(var->sqllen);
            var->sqltype = IISQ_VCH_TYPE;
            break;
        case IISQ_TMW_TYPE:
            var->sqllen = IISQ_TMW_LEN;
            var->sqldata = ckalloc(var->sqllen);
            var->sqltype = IISQ_VCH_TYPE;
            break;
        case IISQ_TME_TYPE:
            var->sqllen = IISQ_TME_LEN;
            var->sqldata = ckalloc(var->sqllen);
            var->sqltype = IISQ_VCH_TYPE;
            break;
        case IISQ_INYM_TYPE:
            var->sqllen = IISQ_INTYM_LEN;
            var->sqldata = ckalloc(var->sqllen);
            var->sqltype = IISQ_VCH_TYPE;
            break;
        case IISQ_INDS_TYPE:
            var->sqllen = IISQ_INTDS_LEN;
            var->sqldata = ckalloc(var->sqllen);
            var->sqltype = IISQ_VCH_TYPE;
            break;
        case IISQ_CHR_TYPE:
        case IISQ_CHA_TYPE:
        case IISQ_BYTE_TYPE:
        case IISQ_TXT_TYPE:
            var->sqldata = ckalloc(var->sqllen);
            var->sqltype = IISQ_CHA_TYPE;
            break;
        case IISQ_VCH_TYPE:
        case IISQ_VBYTE_TYPE:
            var->sqldata = ckalloc(sizeof(short) + var->sqllen);
            var->sqltype = IISQ_VCH_TYPE;
            break;
        case IISQ_BOO_TYPE:
            var->sqldata = ckalloc(sizeof(int));
            break;
        default:
            Tcl_SetObjResult(interp, Tcl_NewStringObj("IngTcl: fetch_row: Unsupported type (m)!", -1));
            return TCL_ERROR;
        }
    }

    /* Fetch row */
    EXEC SQL SET_SQL(SESSION = :session);
    EXEC SQL FETCH :statement_name USING DESCRIPTOR :sqlda;
    
    if (sqlca.sqlcode == 100)
    {
        /* There is no more data so we can close cursor */
        close_cursor(cursor);
    }
    
    /* Check for errors */
    if (!sql_check())
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(errstr, -1));
        return TCL_ERROR;
    }

    /* Something is fetched, create empty list */
    row = Tcl_NewListObj(0, NULL);

    /* Put fetched row into list */
    for (i = 0; i < sqlda->sqld; ++i)
    {
        IISQLVAR *var;
        Tcl_Obj *tcl_var;

        var = &sqlda->sqlvar[i];

        switch (var->sqltype)
        {
        case IISQ_INT_TYPE:
            tcl_var = Tcl_NewIntObj(*(int*)var->sqldata);
            Tcl_ListObjAppendElement(interp, row, tcl_var);
            break;
        case IISQ_FLT_TYPE:
            tcl_var = Tcl_NewDoubleObj(*(double*)var->sqldata);
            Tcl_ListObjAppendElement(interp, row, tcl_var);
            break;
        case IISQ_CHA_TYPE:
            tcl_var = Tcl_NewStringObj(var->sqldata, (short)var->sqllen);
            Tcl_ListObjAppendElement(interp, row, tcl_var);
            break;
        /* date/time types are now IISQ_VCH_TYPE */
        case IISQ_VCH_TYPE:
            tcl_var = Tcl_NewStringObj(var->sqldata + sizeof(short), (short)*(var->sqldata));
            Tcl_ListObjAppendElement(interp, row, tcl_var);
            break;
        case IISQ_BOO_TYPE:
            tcl_var = Tcl_NewBooleanObj(*(var->sqldata));
            Tcl_ListObjAppendElement(interp, row, tcl_var);
            break;
        default:
            Tcl_SetObjResult(interp, Tcl_NewStringObj("IngTcl: fetch_row: Unsupported type! (f)", -1));
            return TCL_ERROR;
        }
    }

    Tcl_SetObjResult(interp, row);
    return TCL_OK;

}

static int 
Prepare_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
/* 
 * (For use from Tcl)
 *
 * IngTcl::prepare $session $statement
 *
 * This function prepares SQL statement for SQL error. On error it throws
 * exception, on success it returns statement cursor to Tcl.
 * cursor to Tcl.
 *
 */
{
    EXEC SQL BEGIN DECLARE SECTION;
        int session;
        char *statement, statement_name[12];
    EXEC SQL END DECLARE SECTION;
    IISQLDA *sqlda;
    char cursor_pointer[17]; /* max pointer size is 8 bytes + \0 */
    ing_cursor_t *cursor;
    int statement_number;
    int number_of_arguments = objc - 1;
    
    /* Process arguments to function */

    if (number_of_arguments != 2)
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: prepare: Expected exactly two arguments, see usage:\n\n"
                "IngTcl::prepare $session $statement\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    if (Tcl_GetIntFromObj(interp, objv[1], &session) != TCL_OK)
    {
        /* If it is impossible to get int from object, then it is not integer */
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: prepare: First supplied argument to IngTcl::prepare is not integer, see usage:\n\n"
                "IngTcl::prepare $session $statement\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    statement = Tcl_GetString(objv[2]);

    /* Allocate memory for cursor */
    cursor = (ing_cursor_t*)ckalloc(sizeof(ing_cursor_t));

    cursor->is_closed = 0;

    cursor->sqlda = (IISQLDA*)ckalloc(IISQDA_HEAD_SIZE + (IISQDA_VAR_SIZE * IISQ_MAX_COLS));
    memset(cursor->sqlda, 0, (IISQDA_HEAD_SIZE + (IISQ_MAX_COLS * IISQDA_VAR_SIZE)));

    sqlda = cursor->sqlda;

    sqlda->sqln = IISQ_MAX_COLS;

    /* Generate statement name */
    statement_number = current_statement = next_statement++;
    sprintf(statement_name, "s%010d", statement_number);
    strcpy(cursor->name, statement_name);

    /* Set current session */
    EXEC SQL SET_SQL(SESSION = :session);
    /* Prepare statement */
    EXEC SQL PREPARE :statement_name INTO sqlda FROM :statement;
    
    /* Check for errors */
    if (!sql_check())
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(errstr, -1));
        return TCL_ERROR;
    }
    
    {
        /* Get information about placeholders */
        IISQLDA *sqlda;

        cursor->ph_sqlda = (IISQLDA*)ckalloc(IISQDA_HEAD_SIZE + (IISQDA_VAR_SIZE * IISQ_MAX_COLS));
        memset(cursor->ph_sqlda, 0, (IISQDA_HEAD_SIZE + (IISQ_MAX_COLS * IISQDA_VAR_SIZE)));

        sqlda = cursor->ph_sqlda;
        sqlda->sqln = IISQ_MAX_COLS;

        EXEC SQL DESCRIBE INPUT :statement_name USING DESCRIPTOR sqlda;

        if (!sql_check())
        {
            Tcl_SetObjResult(interp, Tcl_NewStringObj(errstr, -1));
            return TCL_ERROR;
        }
        
        if (sqlda->sqld > sqlda->sqln)
        {
            Tcl_SetObjResult(interp, Tcl_NewStringObj(
                    "IngTcl: prepare: sqld is bigger than sqln, report this problem to author",
                    -1
                )
            );
            return TCL_ERROR;
        }
    }

    /* Store pointer to cursor as string */
    sprintf(cursor_pointer, "%p", cursor);

    /* Return statement cursor to Tcl */
    Tcl_SetObjResult(interp, Tcl_NewStringObj(cursor_pointer, -1));

    return TCL_OK;

}

static int 
Connect_Cmd(ClientData cdata, Tcl_Interp *interp, int objc, Tcl_Obj *const objv[])
/* 
 * (For use from Tcl)
 *
 * IngTcl::connect $dbname
 * or:
 * IngTcl::connect $dbname $login
 * or:
 * IngTcl::connect $dbname $login $password
 * or:
 * IngTcl::connect $dbname $login $password $options
 *
 * This function connects to Ingres database. On error it throws exception,
 * otherwise it returns session ID to Tcl.
 *
 */
{
    EXEC SQL BEGIN DECLARE SECTION;
        int session;
        char *dbname, *login, *password, *options;
    EXEC SQL END DECLARE SECTION;
    int number_of_arguments = objc - 1;
    
    /* Create session ID */
    session = current_session = next_session++;

    /* Process arguments and connect to database */
    switch (number_of_arguments)
    {
    case 1: /* Just database name */
        dbname = Tcl_GetString(objv[1]);
        EXEC SQL CONNECT :dbname SESSION :session;
        break;
    case 2: /* Database name and login */
        dbname = Tcl_GetString(objv[1]);
        login = Tcl_GetString(objv[2]);
        EXEC SQL CONNECT :dbname SESSION :session IDENTIFIED BY :login;
        break;
    case 3: /* Database name, login and password */
        dbname = Tcl_GetString(objv[1]);
        login = Tcl_GetString(objv[2]);
        password = Tcl_GetString(objv[3]);
        EXEC SQL CONNECT :dbname SESSION :session IDENTIFIED BY :login DBMS_PASSWORD=:password;
        break;    
    case 4: /* Database name, login, password and options */
        dbname = Tcl_GetString(objv[1]);
        login = Tcl_GetString(objv[2]);
        password = Tcl_GetString(objv[3]);
        options = Tcl_GetString(objv[4]);
        EXEC SQL CONNECT :dbname SESSION :session IDENTIFIED BY :login DBMS_PASSWORD=:password OPTIONS=:options;
        break;    
    default:
        Tcl_SetObjResult(interp, Tcl_NewStringObj(
                "IngTcl: connect: Invalid number of arguments\n"
                "Usage:\n\n"
                "IngTcl::connect $dbname\n\n"
                "or:\n\n"
                "IngTcl::connect $dbname $login\n\n"
                "or:\n\n"
                "IngTcl::connect $dbname $login $password\n\n"
                "or:\n\n"
                "IngTcl::connect $dbname $login $password $options\n",
                -1
            )
        );
        return TCL_ERROR;
    }

    /* Check for errors */
    if (!sql_check())
    {
        Tcl_SetObjResult(interp, Tcl_NewStringObj(errstr, -1));
        return TCL_ERROR;
    }

    /* Return session ID to Tcl */
    Tcl_SetObjResult(interp, Tcl_NewIntObj(session));

    return TCL_OK;
}

static int
close_cursor(ing_cursor_t *cursor)
/* 
 * (For use from C)
 *
 * This function closes specified cursor. It returns 1 on success, 0 on fail.
 *
 */
{
    char *statement_name;

    statement_name = (char*)&cursor->name;
    if (!cursor->is_closed)
    {
        cursor->is_closed = 1;
        EXEC SQL CLOSE :statement_name;
        return 1;
    }
    else
        return 0;
}

static int
sql_check(void)
/* 
 * (For use from C)
 *
 * This function checks for SQL error. If there is one, it sets global variable
 * errstr.
 * It returns 1 if there are no errors, and 0 if error is present.
 *
 */
{   
    if (sqlca.sqlcode < 0)
    {
        EXEC SQL INQUIRE_INGRES(:errstr = ERRORTEXT);
        return 0;
    }
    else
        return 1;
}

int DLLEXPORT
Ingtcl_Init(Tcl_Interp *interp)
/* 
 * (For use from Tcl)
 *
 * It is so-called entry point for Tcl, it registers namespace and procedures.
 *
 */
{
    Tcl_Namespace *nsPtr;

    if (Tcl_InitStubs(interp, TCL_VERSION, 0) == NULL)
        return TCL_ERROR;
        
    nsPtr = Tcl_CreateNamespace(interp, "IngTcl", NULL, NULL);
    if (nsPtr == NULL)
        return TCL_ERROR;

    Tcl_CreateObjCommand(interp, "IngTcl::connect", Connect_Cmd, NULL, NULL);
    Tcl_CreateObjCommand(interp, "IngTcl::prepare", Prepare_Cmd, NULL, NULL);
    Tcl_CreateObjCommand(interp, "IngTcl::execute", Execute_Cmd, NULL, NULL);
    Tcl_CreateObjCommand(interp, "IngTcl::do", Do_Cmd, NULL, NULL);
    Tcl_CreateObjCommand(interp, "IngTcl::fetch_row", FetchRow_Cmd, NULL, NULL);
    Tcl_CreateObjCommand(interp, "IngTcl::close_cursor", CloseCursor_Cmd, NULL, NULL);
    Tcl_CreateObjCommand(interp, "IngTcl::disconnect", Disconnect_Cmd, NULL, NULL);
    Tcl_CreateObjCommand(interp, "IngTcl::commit", Commit_Cmd, NULL, NULL);
    Tcl_CreateObjCommand(interp, "IngTcl::rollback", Rollback_Cmd, NULL, NULL);
    Tcl_CreateObjCommand(interp, "IngTcl::set_autocommit", SetAutoCommit_Cmd, NULL, NULL);
    Tcl_CreateObjCommand(interp, "IngTcl::delete_cursor", DeleteCursor_Cmd, NULL, NULL);

    Tcl_PkgProvide(interp, "IngTcl", INGTCL_VERSION);

    return TCL_OK;
}
