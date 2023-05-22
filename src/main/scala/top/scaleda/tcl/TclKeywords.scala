package top.scaleda
package tcl

/** @see https://www.tcl.tk/man/tcl/TclCmd/contents.html
  *
  * @note Tcl does not have keywords, all "keywords" are funtions
  *       supported by specific platforms
  */
object TclKeywords {
  var TCL_KEYWORDS: Array[String] =
    Array[String](
      "after",
      "errorInfo",
      "load",
      "re_syntax",
      "tcl_startOfNextWord",
      "append",
      "eval",
      "lrange",
      "read",
      "tcl_startOfPreviousWord",
      "apply",
      "exec",
      "lrepeat",
      "refchan",
      "tcl_traceCompile",
      "argc",
      "exit",
      "lreplace",
      "regexp",
      "tcl_traceExec",
      "argv",
      "expr",
      "lreverse",
      "registry",
      "tcl_version",
      "argv0",
      "fblocked",
      "lsearch",
      "regsub",
      "tcl_wordBreakAfter",
      "array",
      "fconfigure",
      "lset",
      "rename",
      "tcl_wordBreakBefore",
      "auto_execok",
      "fcopy",
      "lsort",
      "return",
      "tcl_wordchars",
      "auto_import",
      "file",
      "mathfunc",
      "safe",
      "tcltest",
      "auto_load",
      "fileevent",
      "mathop",
      "scan",
      "tell",
      "auto_mkindex",
      "filename",
      "memory",
      "seek",
      "throw",
      "auto_path",
      "flush",
      "msgcat",
      "self",
      "time",
      "auto_qualify",
      "for",
      "my",
      "set",
      "timerate",
      "auto_reset",
      "foreach",
      "namespace",
      "socket",
      "tm",
      "bgerror",
      "format",
      "next",
      "source",
      "trace",
      "binary",
      "gets",
      "nextto",
      "split",
      "transchan",
      "break",
      "glob",
      "oo::class",
      "string",
      "try",
      "catch",
      "global",
      "oo::copy",
      "subst",
      "unknown",
      "cd",
      "history",
      "oo::define",
      "switch",
      "unload",
      "chan",
      "http",
      "oo::objdefine",
      "tailcall",
      "unset",
      "clock",
      "if",
      "oo::object",
      "Tcl",
      "update",
      "close",
      "incr",
      "open",
      "tcl::prefix",
      "uplevel",
      "concat",
      "info",
      "package",
      "tcl_endOfWord",
      "upvar",
      "continue",
      "interp",
      "parray",
      "tcl_findLibrary",
      "variable",
      "coroutine",
      "join",
      "pid",
      "tcl_interactive",
      "vwait",
      "dde",
      "lappend",
      "pkg::create",
      "tcl_library",
      "while",
      "dict",
      "lassign",
      "pkg_mkIndex",
      "tcl_nonwordchars",
      "yield",
      "encoding",
      "lindex",
      "platform",
      "tcl_patchLevel",
      "yieldto",
      "env",
      "linsert",
      "platform::shell",
      "tcl_pkgPath",
      "zlib",
      "eof",
      "list",
      "proc",
      "tcl_platform",
      "error",
      "llength",
      "puts",
      "tcl_precision",
      "errorCode",
      "lmap",
      "pwd",
      "tcl_rcFileName"
    )
}