case class Location(x: Int, y: Int) {
  def +(loc: Location): Location =
    new Location(x + loc.x, y + loc.y)

  def -(loc: Location): Location =
    new Location(x + loc.x, y + loc.y)
}