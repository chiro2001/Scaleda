package top.criwits.scaleda
package verilog.psi.nodes.assignments

import com.intellij.lang.ASTNode
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode
import top.criwits.scaleda.verilog.psi.nodes.signal.SignalIdentifierPsiNode

abstract class AbstractAssignmentPsiNode(node: ASTNode) extends ANTLRPsiNode(node){
  def getLValues: Iterable[SignalIdentifierPsiNode]
}
