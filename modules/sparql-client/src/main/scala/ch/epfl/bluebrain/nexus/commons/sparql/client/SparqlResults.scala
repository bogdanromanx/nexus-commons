package ch.epfl.bluebrain.nexus.commons.sparql.client

import akka.http.scaladsl.model.Uri
import ch.epfl.bluebrain.nexus.commons.sparql.client.SparqlResults._
import io.circe.generic.semiauto._
import io.circe.{Decoder, Encoder}
import io.circe.generic.auto._

import scala.util.Try

/**
  * Sparql query results representation.
  *
  * @param head    the variables mentioned in the results and may contain a "link" member
  * @param results a collection of bindings
  */
final case class SparqlResults(head: Head, results: Bindings) {

  /**
    * Creates a new sparql result which is a merge of the provided results and the current results
    * @param that the provided head
    */
  def ++(that: SparqlResults): SparqlResults = SparqlResults(head ++ that.head, results ++ that.results)
}

object SparqlResults {

  /**
    * The "head" member gives the variables mentioned in the results and may contain a "link" member.
    *
    * @param vars  an array giving the names of the variables used in the results.
    *              These are the projected variables from the query.
    *              A variable is not necessarily given a value in every query solution of the results.
    * @param link an array of URIs, as strings, to refer for further information.
    *              The format and content of these link references is not defined by this document.
    */
  final case class Head(vars: List[String], link: Option[List[Uri]] = None) {

    /**
      * Creates a new head which is a merge of the provided head and the current head
      * @param that the provided head
      */
    def ++(that: Head): Head = Head(vars ++ that.vars, link.map(l => that.link.getOrElse(List.empty) ++ l))
  }

  /**
    * The value of the "bindings" member is a map with zero or more elements, one element per query solution.
    * Each query solution is a Binding object. Each key of this object is a variable name from the query solution.
    */
  final case class Bindings(bindings: List[Map[String, Binding]]) {

    /**
      * Creates a new bindings which is a merge of the provided bindings and the current bindings
      * @param that the provided head
      */
    def ++(that: Bindings): Bindings = Bindings(bindings ++ that.bindings)
  }

  object Bindings {
    def apply(values: Map[String, Binding]*): Bindings = Bindings(values.toList)
  }

  /**
    * Encodes an RDF term
    *
    * @param `type`     the type of the term
    * @param value      the value of the term
    * @param `xml:lang` the language tag (when the term is a literal)
    * @param datatype   the data type information of the term
    */
  final case class Binding(`type`: String,
                           value: String,
                           `xml:lang`: Option[String] = None,
                           datatype: Option[String] = None) {
    def isLiteral: Boolean = `type` == "literal"
  }

  private implicit val uriEncoder: Encoder[Uri] = Encoder.encodeString.contramap(_.toString)
  private implicit val uriDecoder: Decoder[Uri] = Decoder.decodeString.emapTry(uri => Try(Uri(uri)))

  final implicit val sparqlResultsEncoder: Encoder[SparqlResults] = deriveEncoder[SparqlResults]
  final implicit val sparqlResultsDecoder: Decoder[SparqlResults] = deriveDecoder[SparqlResults]
}