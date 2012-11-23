#
# See the file "license.terms" for information on usage and redistribution
# of this file, and for a DISCLAIMER OF ALL WARRANTIES.
#
# Copyright (c) 2012 Tomasz Konojacki
#

package provide IngTcl 0.3

namespace eval ::IngTcl {
    namespace export *
}

proc ::IngTcl::quote {str} {
    regsub -all {'} $str {''} str
    return "'$str'"
}

proc ::IngTcl::dbms_name {session} {
    set row {}
    set cursor {}
    set name {}

    set cursor [IngTcl::prepare $session "SELECT dbmsinfo('_version')"]
    IngTcl::execute $session $cursor
    set row [IngTcl::fetch_row $session $cursor]

    IngTcl::close_cursor $session $cursor
    IngTcl::delete_cursor $session $cursor

    if {[regexp {^\w{2}} [lindex $row 0] name]} {
        switch $name {
            II {
                return Ingres
            }

            VW {
                return Vectorwise
            }

            default {
                return unknown
            }
        }
    }

    return unknown
}

proc ::IngTcl::dbms_version {session} {
    set row {}
    set cursor {}
    set version {}

    set cursor [IngTcl::prepare $session "SELECT dbmsinfo('_version')"]
    IngTcl::execute $session $cursor
    set row [IngTcl::fetch_row $session $cursor]

    IngTcl::close_cursor $session $cursor
    IngTcl::delete_cursor $session $cursor

    if {[regexp {^(II|VW) (\d+\.\d+\.\d+)} [lindex $row 0] {} {} version]} {
        return $version
    }

    return unknown
}

proc ::IngTcl::is_vectorwise {session} {
    if {[::IngTcl::dbms_name $session] eq "Vectorwise"} {
        return true
    }

    return false
}