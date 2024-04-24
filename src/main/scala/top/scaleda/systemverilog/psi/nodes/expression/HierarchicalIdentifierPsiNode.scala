package top.scaleda
package systemverilog.psi.nodes.expression

import systemverilog.psi.nodes.signal.SignalIdentifierPsiNode
import systemverilog.psi.nodes.{ReferenceHolder, SimpleIdentifierPsiLeafNode}
import systemverilog.references.HierarchicalIdentifierReference

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.psi.util.PsiTreeUtil
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode

/**
 * Represents references in expressions, for example, `clk` in `always @(posedge clk)` or `rst` in `if (rst)`
 * @param node The [[ASTNode]]
 */
class HierarchicalIdentifierPsiNode(node: ASTNode)
    extends ANTLRPsiNode(node)
    with ReferenceHolder[SimpleIdentifierPsiLeafNode] {

  override def getName: String = {
    this.getHoldPsiNode().getName
  }

  override def getHoldPsiNode: SimpleIdentifierPsiLeafNode = {
    PsiTreeUtil.findChildOfType(this, classOf[SimpleIdentifierPsiLeafNode])
  }

  override def getHoldPsiNodeRelativeTextRange: TextRange = {
    getHoldPsiNode().getTextRange
      .shiftLeft(this.getTextOffset)
  }

  override def getReference: PsiReference = {
    new HierarchicalIdentifierReference(this)
  }

  def resolveReferenceIdentifier: SignalIdentifierPsiNode = {
    val reference = getReference
    if (reference == null) return null

    reference.resolve().asInstanceOf[SignalIdentifierPsiNode]
  }

}
