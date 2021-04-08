package io.getquill.context

import scala.language.higherKinds
import scala.language.experimental.macros
//import io.getquill.generic.Dsl
//import io.getquill.util.Messages.fail
import java.io.Closeable
import scala.compiletime.summonFrom
import scala.util.Try
import io.getquill.{ ReturnAction }
import io.getquill.generic.EncodingDsl
import io.getquill.Quoted
import io.getquill.QueryMeta
import io.getquill.generic._
import io.getquill.context.mirror.MirrorDecoders
import io.getquill.context.mirror.Row
import io.getquill.generic.GenericDecoder
import io.getquill.generic.GenericEncoder
import io.getquill.Planter
import io.getquill.EagerPlanter
import io.getquill.LazyPlanter
import io.getquill.ast.Ast
import io.getquill.ast.ScalarTag
import scala.quoted._
import io.getquill.ast.{Transform, QuotationTag}
import io.getquill.QuotationLot
import io.getquill.metaprog.QuotedExpr
import io.getquill.metaprog.PlanterExpr
import io.getquill.metaprog.EagerEntitiesPlanterExpr
import io.getquill.Planter
import io.getquill.idiom.ReifyStatement
import io.getquill.Query
import io.getquill.Action
import io.getquill.idiom.Idiom
import io.getquill.NamingStrategy
import io.getquill.metaprog.Extractors
import io.getquill.BatchAction
import io.getquill.metaprog.QuotationLotExpr
import io.getquill.metaprog.QuotationLotExpr._
import io.getquill.util.Format
import io.getquill.context.LiftMacro

trait BatchContextOperation[T, A <: Action[_], D <: Idiom, N <: NamingStrategy, PrepareRow, ResultRow, Res](val idiom: D, val naming: N) {
  def execute(sql: String, prepare: List[PrepareRow => (List[Any], PrepareRow)], executionType: ExecutionType): Res
}

object BatchQueryExecution:
  class RunQuery[
    T: Type,
    A <: Action[_]: Type,
    ResultRow: Type, 
    PrepareRow: Type, 
    D <: Idiom: Type, 
    N <: NamingStrategy: Type, 
    Res: Type
  ](quoted: Expr[Quoted[BatchAction[A]]],
    batchContextOperation: Expr[BatchContextOperation[T, A, D, N, PrepareRow, ResultRow, Res]])(using val qctx: Quotes) 
  extends Extractors:
    import quotes.reflect._

    def apply(): Expr[Res] = 
      UntypeExpr(quoted) match
        case QuotedExpr.UprootableWithLifts(QuotedExpr(ast, _, _), planters) =>
          planters match
            case List(planterModel @ EagerEntitiesPlanterExpr(_, iterableExpr)) =>

              println("========================== HERE ==========================")
              println( Format.Expr(quoted) )
              
              // Get the expression type
              val exprType = 
                planterModel.tpe match
                  case '[tt] => TypeRepr.of[tt]
              
              val (ast, lifts) = LiftMacro.liftInjectedProduct[T, PrepareRow]
              //val insertQuery = 

              report.warning("List length is: " + lifts.length)

              lifts.foreach(lift => println(io.getquill.util.Format.Expr(lift)))
              

              // synthesize a runnable query and get it's static state (for batch queries it must exist, I don't see how I could be dynamic)
              // inject the lifts and return the query

              //report.throwError(s"Got to BatchQueryExecutionPoint: ${Format.Expr(iterableExpr)}, type: ${Format.Type(planterModel.tpe)}", quoted)

              // Use expander of InjectMacro to expand out lifts to series of expressions?
              // Put those into injectable planters?
              ???

            case _ =>
              report.throwError(s"Invalid liftQuery clause: ${planters}. Must be a single EagerEntitiesPlanter", quoted)

        case _ =>
          report.throwError(s"Batch actions must be static quotations. Found: ${Format.Expr(quoted)}", quoted)

  end RunQuery

  inline def apply[
    T, 
    A <: Action[_],
    ResultRow, 
    PrepareRow, 
    D <: Idiom, 
    N <: NamingStrategy, 
    Res
  ](inline quoted: Quoted[BatchAction[A]], ctx: BatchContextOperation[T, A, D, N, PrepareRow, ResultRow, Res]) = 
    ${ applyImpl[T, A, ResultRow, PrepareRow, D, N, Res]('quoted, 'ctx) }
  
  def applyImpl[
    T: Type,
    A <: Action[_]: Type,
    ResultRow: Type,
    PrepareRow: Type, 
    D <: Idiom: Type, 
    N <: NamingStrategy: Type, 
    Res: Type
  ](quoted: Expr[Quoted[BatchAction[A]]],
    ctx: Expr[BatchContextOperation[T, A, D, N, PrepareRow, ResultRow, Res]])(using qctx: Quotes): Expr[Res] =
    new RunQuery[T, A, ResultRow, PrepareRow, D, N, Res](quoted, ctx).apply()

end BatchQueryExecution
