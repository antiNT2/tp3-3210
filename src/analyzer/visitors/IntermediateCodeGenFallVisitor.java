package analyzer.visitors;

import analyzer.ast.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;


/**
 * Ce visiteur explore l'AST et génère du code intermédiaire.
 *
 * @author Félix Brunet
 * @author Doriane Olewicki
 * @author Quentin Guidée
 * @author Raphaël Tremblay
 * @version 2024.02.26
 */
public class IntermediateCodeGenFallVisitor implements ParserVisitor {
    public static final String FALL = "fall";

    private final PrintWriter m_writer;

    public HashMap<String, VarType> SymbolTable = new HashMap<>();
    public HashMap<String, Integer> EnumValueTable = new HashMap<>();

    private int id = 0;
    private int label = 0;

    public IntermediateCodeGenFallVisitor(PrintWriter writer) {
        m_writer = writer;
    }

    private String newID() {
        return "_t" + id++;
    }

    private String newLabel() {
        return "_L" + label++;
    }

    @Override
    public Object visit(SimpleNode node, Object data) {
        return data;
    }

    @Override
    public Object visit(ASTProgram node, Object data) {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTDeclaration node, Object data) {
        String varName = ((ASTIdentifier) node.jjtGetChild(0)).getValue();
        VarType varType;

        if (node.getValue() == null) {
            varName = ((ASTIdentifier) node.jjtGetChild(1)).getValue();
            varType = VarType.EnumVar;
        } else
            varType = node.getValue().equals("num") ? VarType.Number : VarType.Bool;


        SymbolTable.put(varName, varType);

        return null;
    }

    @Override
    public Object visit(ASTBlock node, Object data) {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTStmt node, Object data) {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTEnumStmt node, Object data) {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTSwitchStmt node, Object data) {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTCaseStmt node, Object data) {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTBreakStmt node, Object data) {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTIfStmt node, Object data) {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTWhileStmt node, Object data) {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTForStmt node, Object data) {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTAssignStmt node, Object data) {
        String identifier = ((ASTIdentifier) node.jjtGetChild(0)).getValue();
        node.jjtGetChild(1).jjtAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTExpr node, Object data) {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object codeExtAddMul(SimpleNode node, Object data, Vector<String> ops) {
        // À noter qu'il n'est pas nécessaire de boucler sur tous les enfants.
        // La grammaire n'accepte plus que 2 enfants maximum pour certaines opérations, au lieu de plusieurs
        // dans les TPs précédents. Vous pouvez vérifier au cas par cas dans le fichier Grammaire.jjt.
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTAddExpr node, Object data) {
        return codeExtAddMul(node, data, node.getOps());
    }

    @Override
    public Object visit(ASTMulExpr node, Object data) {
        return codeExtAddMul(node, data, node.getOps());
    }

    @Override
    public Object visit(ASTUnaExpr node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTBoolExpr node, Object data) {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTCompExpr node, Object data) {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTNotExpr node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTGenValue node, Object data) {
        node.jjtGetChild(0).jjtAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTBoolValue node, Object data) {
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTIdentifier node, Object data) {
        // TODO
        return node.getValue();
    }

    @Override
    public Object visit(ASTIntValue node, Object data) {
        return Integer.toString(node.getValue());
    }

    public enum VarType {
        Bool,
        Number,
        EnumType,
        EnumVar,
        EnumValue
    }

    private static class BoolLabel {
        public String lTrue;
        public String lFalse;

        public BoolLabel(String lTrue, String lFalse) {
            this.lTrue = lTrue;
            this.lFalse = lFalse;
        }
    }
}
