package fs2.data.csv
package generic

import weaver._

object CellDecoderTest extends SimpleIOSuite {

  pureTest("derivation for coproducts should work out of the box for enum-style sealed traits") {
    val simpleDecoder: CellDecoder[Simple] = semiauto.deriveCellDecoder

    expect(simpleDecoder("On") == Right(On)) and
      expect(simpleDecoder("Off") == Right(Off)) and
      expect(simpleDecoder("foo").isLeft == true)
  }

  pureTest("derivation for coproducts should handle non-case object cases") {
    implicit val numberedDecoder: CellDecoder[Numbered] =
      CellDecoder[Int].map(Numbered(_))
    implicit val unknownDecoder: CellDecoder[Unknown] =
      CellDecoder[String].map(Unknown(_))
    val complexDecoder: CellDecoder[Complex] = semiauto.deriveCellDecoder

    expect(complexDecoder("Active") == Right(Active)) and
//      expect(complexDecoder("Inactive") == Right(Inactive)) and
      expect(complexDecoder("inactive") == Right(Unknown("inactive"))) and
      expect(complexDecoder("7") == Right(Numbered(7))) and
      expect(complexDecoder("foo") == Right(Unknown("foo")))
  }

  pureTest("derivation for coproducts should respect @CsvValue annotations") {
    val alphabetDecoder: CellDecoder[Alphabet] = semiauto.deriveCellDecoder

    expect(alphabetDecoder("A") == Right(Alpha)) and
      expect(alphabetDecoder("B") == Right(Beta)) and
      expect(alphabetDecoder("Gamma") == Right(Gamma)) and
      expect(alphabetDecoder("foobar").isLeft == true)
  }

  pureTest("derivation for unary products should work for standard types") {
    expect(semiauto.deriveCellDecoder[IntResultWrapper].apply("7") == Right(IntResultWrapper(Right(7))))
  }

  pureTest("derivation for unary products should work for types with implicit decoder") {
    implicit val thingDecoder: CellDecoder[Thing] =
      CellDecoder[String].map(Thing(_, 7))
    expect(semiauto.deriveCellDecoder[ThingWrapper].apply("cell") == Right(ThingWrapper(Thing("cell", 7))))
  }

  pureTest("derivation for unary products should work for types with arguments") {
    expect(semiauto.deriveCellDecoder[Wrapper[Int]].apply("7") == Right(Wrapper(7)))
  }
}
