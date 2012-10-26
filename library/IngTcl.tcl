#
# See the file "license.terms" for information on usage and redistribution
# of this file, and for a DISCLAIMER OF ALL WARRANTIES.                   
#                                                                       
# Copyright (c) 2012 Tomasz Konojacki
#

package provide IngTcl 0.1

namespace eval ::IngTcl {
    namespace export *
}

proc ::IngTcl::quote {str} {
    regsub -all {'} $str {''} str
    return "'$str'"
}
