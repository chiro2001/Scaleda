package top.criwits.scaleda
package verilog

import verilog.parser._

import com.intellij.lang.{ASTNode, ParserDefinition, PsiParser}
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.tree.{IElementType, IFileElementType, TokenSet}
import com.intellij.psi.{FileViewProvider, PsiElement, PsiFile}
import org.antlr.intellij.adaptor.lexer.{ANTLRLexerAdaptor, PSIElementTypeFactory, RuleIElementType, TokenIElementType}
import org.antlr.intellij.adaptor.parser.ANTLRParserAdaptor
import org.antlr.intellij.adaptor.psi.ANTLRPsiNode
import org.antlr.v4.runtime.Parser
import top.criwits.scaleda.verilog.psi.factory.VerilogPsiNodeFactory

class VerilogParserDefinition extends ParserDefinition {
  override def createLexer(project: Project): Lexer =
    new ANTLRLexerAdaptor(VerilogLanguage, new VerilogLexer(null))

  override def createParser(project: Project): PsiParser =
    new ANTLRParserAdaptor(VerilogLanguage, new VerilogParser(null)) {
      override def parse(parser: Parser, root: IElementType) = {
        parser.asInstanceOf[VerilogParser].source_text()
      }
    }

  override def getFileNodeType: IFileElementType = VerilogParserDefinition.FILE

  override def getCommentTokens: TokenSet = VerilogParserDefinition.COMMENTS

  override def getStringLiteralElements: TokenSet = VerilogParserDefinition.STRING

  override def createElement(node: ASTNode): PsiElement = {
    // val elType = node.getElementType
    // if (elType.isInstanceOf[TokenIElementType]) return new ANTLRPsiNode(node)
    // if (!elType.isInstanceOf[RuleIElementType]) return new ANTLRPsiNode(node)
    // val ruleElType = elType.asInstanceOf[RuleIElementType]
    // ruleElType.getRuleIndex match {
    //   case VerilogParser.RULE_
    // }
    VerilogPsiNodeFactory.create(node)
  }

  override def createFile(viewProvider: FileViewProvider): PsiFile = new VerilogPSIFileRoot(viewProvider)
}

object VerilogParserDefinition {
  val FILE = new IFileElementType(VerilogLanguage)
  val COMMENTS = PSIElementTypeFactory.createTokenSet(
    VerilogLanguage,
    VerilogLexer.COMMENT,
    VerilogLexer.COMMENTS,
    // TODO: what XXX_NUMBER means?
    VerilogLexer.COMMENT_5,
  )
  val WHITESPACE = PSIElementTypeFactory.createTokenSet(
    VerilogLanguage,
    VerilogLexer.WHITE_SPACE,
    VerilogLexer.WHITE_SPACE_7,
  )
  val STRING = PSIElementTypeFactory.createTokenSet(
    VerilogLanguage,
    VerilogLexer.STRING
  )
}