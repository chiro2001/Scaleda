package top.criwits.scaleda
package verilog.psi.nodes.expression

import verilog.psi.nodes.signal.SignalIdentifierPsiNode

import com.intellij.lang.ASTNode
import com.intellij.psi.util.PsiTreeUtil
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

import scala.jdk.CollectionConverters._

class ExpressionPsiNode(node: ASTNode) extends ANTLRPsiNode(node) with IntEvaluable {
  override def evaluate: Option[Int] = {
    // TODO: CURRENTLY ONLY SUPPORT SINGLE-VALUE
    val children = getChildren
    if (children.length != 1) return None
    children.head match {
      case term: TermPsiNode => term.evaluate
      case _                 => None
    }
  }

  def getHierarchical = PsiTreeUtil.findChildrenOfType(this, classOf[HierarchicalIdentifierPsiNode]).asScala

  def getUsedSignals = getHierarchical
    .map(_.getReference.resolve())
    .filter(_.isInstanceOf[SignalIdentifierPsiNode])
    .map(_.asInstanceOf[SignalIdentifierPsiNode])
}
