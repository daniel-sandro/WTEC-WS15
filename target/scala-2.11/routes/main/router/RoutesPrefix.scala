
// @GENERATOR:play-routes-compiler
// @SOURCE:/Users/dbarelop/WTEC1516/conf/routes
// @DATE:Wed Oct 21 17:06:22 CEST 2015


package router {
  object RoutesPrefix {
    private var _prefix: String = "/"
    def setPrefix(p: String): Unit = {
      _prefix = p
    }
    def prefix: String = _prefix
    val byNamePrefix: Function0[String] = { () => prefix }
  }
}
