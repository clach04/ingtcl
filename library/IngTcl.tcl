package provide IngTcl 0.1;

namespace eval ::IngTcl {
    namespace export *
}

proc ::IngTcl::quote {str} {
    regsub -all {'} $str {''} str;
    return "'$str'";
}
