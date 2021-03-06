package ch.epfl.bluebrain.nexus.commons.test

import _root_.io.circe._
import _root_.io.circe.syntax._
import org.scalatest.matchers.{MatchResult, Matcher}

trait CirceEq {
  private implicit val printer = Printer.noSpaces.copy(dropNullValues = true)

  def equalIgnoreArrayOrder(json: Json) = IgnoredArrayOrder(json)

  case class IgnoredArrayOrder(json: Json) extends Matcher[Json] {
    private def sortKeys(value: Json): Json = {
      def canonicalJson(json: Json): Json =
        json.arrayOrObject[Json](
          json,
          arr => Json.fromValues(arr.sortBy(_.hashCode()).map(canonicalJson)),
          obj => sorted(obj).asJson
        )

      def sorted(jObj: JsonObject): JsonObject =
        JsonObject.fromIterable(jObj.toVector.sortBy(_._1).map { case (k, v) => k -> canonicalJson(v) })

      canonicalJson(value)
    }

    override def apply(left: Json): MatchResult = {
      val leftSorted  = sortKeys(left)
      val rightSorted = sortKeys(json)
      MatchResult(
        leftSorted == rightSorted || printer.print(leftSorted) == printer.print(rightSorted),
        s"Both Json are not equal (ignoring array order)\n${printer.print(leftSorted)}\ndid not equal\n${printer.print(rightSorted)}",
        ""
      )
    }
  }

}
