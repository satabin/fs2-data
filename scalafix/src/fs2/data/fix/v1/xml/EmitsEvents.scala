/*
 * Copyright 2020 Lucas Satabin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fs2
package data
package fix
package xml

import scalafix.v1._
import scala.meta._

/** Find these patterns:
  * {{{
  * someStream.flatMap(s => Stream.emits(s)).through(events[F])
  * }}}
  *
  * and replace with:
  * {{{
  * someStream.through(events[F, String])
  * }}}
  *
  * or
  * {{{
  * someStream.through(events[F])
  * }}}
  *
  * with
  * {{{
  * someStream.through(events[F, Char])
  * }}}
  *
  */
class EmitsEvents extends SyntacticRule("XmlEmitsEvents") {

  def isName(term: Term, name: String): Boolean =
    term match {
      case Term.Name(n) => n == name
      case _            => false
    }

  def isEmitsString(term: Term): Boolean =
    term match {
      case q"Stream.emits(_)"            => true
      case q"$param => Stream.emits($s)" => isName(s, param.name.value)
      case _                             => false
    }

  override def fix(implicit doc: SyntacticDocument): Patch =
    doc.tree.collect {
      case t @ q"$base.flatMap($fun).through(events[$f])" if isEmitsString(fun) =>
        Patch.replaceTree(t, s"${base.syntax}.through(events[$f, String])")
      case q"$_.through(events[$f])" =>
        Patch.addRight(f, ", Char")
    }.asPatch

}
