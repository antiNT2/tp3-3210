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
public class IntermediateCodeGenVisitor implements ParserVisitor
{
    private final PrintWriter m_writer;

    public HashMap<String, VarType> SymbolTable = new HashMap<>();
    public HashMap<String, Integer> EnumValueTable = new HashMap<>();

    private int id = 0;
    private int label = 0;

    public IntermediateCodeGenVisitor(PrintWriter writer)
    {
        m_writer = writer;
    }

    private String newID()
    {
        return "_t" + id++;
    }

    private String newLabel()
    {
        return "_L" + label++;
    }

    @Override
    public Object visit(SimpleNode node, Object data)
    {
        return data;
    }

    @Override
    public Object visit(ASTProgram node, Object data)
    {
        String label = newLabel();
        node.childrenAccept(this, data);
        m_writer.println(label);

        return null;
    }

    @Override
    public Object visit(ASTDeclaration node, Object data)
    {
        String varName = ((ASTIdentifier) node.jjtGetChild(0)).getValue();
        VarType varType;

        if (node.getValue() == null)
        {
            varName = ((ASTIdentifier) node.jjtGetChild(1)).getValue();
            varType = VarType.EnumVar;
        } else varType = node.getValue().equals("num") ? VarType.Number : VarType.Bool;

        SymbolTable.put(varName, varType);
        return null;
    }

    @Override
    public Object visit(ASTBlock node, Object data)
    {
        int numberOfChildren = node.jjtGetNumChildren();

        for (int i = 0; i < numberOfChildren; i++)
        {
            if (i == numberOfChildren - 1)
            {
                node.jjtGetChild(i).jjtAccept(this, data);
                return null;
            }
            String label = newLabel();
            node.jjtGetChild(i).jjtAccept(this, label);
            m_writer.println(label);
        }
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTEnumStmt node, Object data)
    {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTSwitchStmt node, Object data)
    {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTCaseStmt node, Object data)
    {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTBreakStmt node, Object data)
    {
        node.childrenAccept(this, data);
        // TODO
        return null;
    }

    @Override
    public Object visit(ASTStmt node, Object data)
    {
        node.childrenAccept(this, data);
        return null;
    }

    @Override
    public Object visit(ASTIfStmt node, Object data)
    {
        int numberOfChildren = node.jjtGetNumChildren();

        String nextLabel = (data == null) ? "_L0" : (String) data;

        if (numberOfChildren == 2)
        {
            BoolLabel newBoolLabel = new BoolLabel(newLabel(), nextLabel);

            node.jjtGetChild(0).jjtAccept(this, newBoolLabel);

            m_writer.println(newBoolLabel.lTrue);

            node.jjtGetChild(1).jjtAccept(this, nextLabel);

            return null;
        }

        BoolLabel newBoolLabel = new BoolLabel(newLabel(), newLabel());

        node.jjtGetChild(0).jjtAccept(this, newBoolLabel);
        m_writer.println(newBoolLabel.lTrue);

        node.jjtGetChild(1).jjtAccept(this, nextLabel);
        m_writer.println("goto " + nextLabel);
        m_writer.println(newBoolLabel.lFalse);

        node.jjtGetChild(2).jjtAccept(this, nextLabel);

        // TODO
        return null;
    }

    @Override
    public Object visit(ASTWhileStmt node, Object data)
    {
        String initLabel = newLabel();

        String nextLabel = (data == null) ? "_L0" : (String) data;

        BoolLabel loopBoolLabel = new BoolLabel(newLabel(), nextLabel);

        m_writer.println(initLabel);

        node.jjtGetChild(0).jjtAccept(this, loopBoolLabel);

        m_writer.println(loopBoolLabel.lTrue);

        node.jjtGetChild(1).jjtAccept(this, initLabel);

        m_writer.println("goto " + initLabel);

        // TODO
        return null;
    }

    @Override
    public Object visit(ASTForStmt node, Object data)
    {
        String initLabel = newLabel();

        String nextLabel = (data == null) ? "_L0" : ((BoolLabel) data).lTrue;

        BoolLabel loopBoolLabel = new BoolLabel(newLabel(), nextLabel);

        node.jjtGetChild(0).jjtAccept(this, loopBoolLabel);
        m_writer.println(initLabel);

        BoolLabel blockBoolLabel = new BoolLabel(newLabel(), nextLabel);

        node.jjtGetChild(1).jjtAccept(this, blockBoolLabel);
        m_writer.println(blockBoolLabel.lTrue);

        node.jjtGetChild(3).jjtAccept(this, loopBoolLabel);
        m_writer.println(loopBoolLabel.lTrue);

        node.jjtGetChild(2).jjtAccept(this, loopBoolLabel);
        m_writer.println("goto " + initLabel);

        return null;
    }

    @Override
    public Object visit(ASTAssignStmt node, Object data)
    {
        String identifier = ((ASTIdentifier) node.jjtGetChild(0)).getValue();
        VarType varType = SymbolTable.get(identifier);

        // TODO

        if (varType == VarType.Number)
        {
            m_writer.println(identifier + " = " + node.jjtGetChild(1).jjtAccept(this, data));
        }

        if (varType == VarType.Bool)
        {
            BoolLabel boolLabel = new BoolLabel(newLabel(), newLabel());
            node.jjtGetChild(1).jjtAccept(this, boolLabel);
            m_writer.println(boolLabel.lTrue + "\n" + identifier + " = 1");
            String id = data == null ? "_L0" : data.toString();
            m_writer.println("goto " + id + "\n" + boolLabel.lFalse + "\n" + identifier + " = 0");
        }

        return identifier;
    }

    @Override
    public Object visit(ASTExpr node, Object data)
    {
        return node.jjtGetChild(0).jjtAccept(this, data);
    }

    public Object codeExtAddMul(SimpleNode node, Object data, Vector<String> ops)
    {
        // À noter qu'il n'est pas nécessaire de boucler sur tous les enfants.
        // La grammaire n'accepte plus que 2 enfants maximum pour certaines opérations, au lieu de plusieurs
        // dans les TPs précédents. Vous pouvez vérifier au cas par cas dans le fichier Grammaire.jjt.

        if (ops.size() == 1 && node.jjtGetNumChildren() > 1)
        {
            String newId = newID();
            String firstValue = (String) node.jjtGetChild(0).jjtAccept(this, data);
            String secondValue = (String) node.jjtGetChild(1).jjtAccept(this, data);
            String operator = ops.get(0);

            m_writer.println(newId + " = " + firstValue + " " + operator + " " + secondValue);
            return newId;
        } else
        {
            return node.jjtGetChild(0).jjtAccept(this, data);
        }
    }

    @Override
    public Object visit(ASTAddExpr node, Object data)
    {
        return codeExtAddMul(node, data, node.getOps());
    }

    @Override
    public Object visit(ASTMulExpr node, Object data)
    {
        return codeExtAddMul(node, data, node.getOps());
    }

    @Override
    public Object visit(ASTUnaExpr node, Object data)
    {

        Object childValue = node.jjtGetChild(0).jjtAccept(this, data);
        // TODO

        if (node.getOps().size() == 0)
        {
            return childValue;
        }

        String oldestId = childValue.toString();

        for (int i = 0; i < node.getOps().size(); i++)
        {
            String latestId = newID();
            m_writer.println(latestId + " = " + node.getOps().get(i) + " " + oldestId);
            oldestId = latestId;
        }


        return oldestId;
    }

    @Override
    public Object visit(ASTBoolExpr node, Object data)
    {
        int numberOfChildren = node.jjtGetNumChildren();

        if (numberOfChildren <= 1)
        {
            Object childValue = node.jjtGetChild(0).jjtAccept(this, data);
            return childValue;
        }
        String newLabel = newLabel();
        String operator = (String) node.getOps().get(0);
        BoolLabel newBoolLabel;
        BoolLabel currentBoolLabel = ((BoolLabel) data);
        if (operator.equals("||"))
        {
            newBoolLabel = new BoolLabel(currentBoolLabel.lTrue, newLabel);
        } else
        {
            newBoolLabel = new BoolLabel(newLabel, currentBoolLabel.lFalse);
        }
        node.jjtGetChild(0).jjtAccept(this, newBoolLabel);
        m_writer.println(newLabel);
        node.jjtGetChild(1).jjtAccept(this, data);


        return null;
    }

    @Override
    public Object visit(ASTCompExpr node, Object data)
    {
        int numberOfChildren = node.jjtGetNumChildren();

        if (numberOfChildren <= 1)
        {
            Object childValue = node.jjtGetChild(0).jjtAccept(this, data);
            return childValue;
        }

        String operator = node.getValue();

        String firstValue = (String) node.jjtGetChild(0).jjtAccept(this, data);
        String secondValue = (String) node.jjtGetChild(1).jjtAccept(this, data);
        BoolLabel currentBoolLabel = ((BoolLabel) data);

        m_writer.println("if " + firstValue + " " + operator + " " + secondValue + " goto " + currentBoolLabel.lTrue);
        m_writer.println("goto " + currentBoolLabel.lFalse);

        // TODO
        return null;
    }

    @Override
    public Object visit(ASTNotExpr node, Object data)
    {

        int numberOfOps = node.getOps().size();

        if (numberOfOps % 2 != 0)
        {
            // We have an odd number of '!'

            BoolLabel currentBoolLabel = ((BoolLabel) data);
            BoolLabel newBoolLabel = new BoolLabel(currentBoolLabel.lFalse, currentBoolLabel.lTrue);

            Object childValue = node.jjtGetChild(0).jjtAccept(this, newBoolLabel);
            return childValue;

        }

        Object childValue = node.jjtGetChild(0).jjtAccept(this, data);


        return childValue;
    }

    @Override
    public Object visit(ASTGenValue node, Object data)
    {
        Object childValue = node.jjtGetChild(0).jjtAccept(this, data);
        // TODO
        return childValue;
    }

    @Override
    public Object visit(ASTBoolValue node, Object data)
    {
        Boolean boolValue = node.getValue();
        BoolLabel boolLabel = ((BoolLabel) data);

        String valueToGo = boolValue ? boolLabel.lTrue : boolLabel.lFalse;

        m_writer.println("goto " + valueToGo);


        return null;
    }

    @Override
    public Object visit(ASTIdentifier node, Object data)
    {
        // TODO
        String identifier = node.getValue();
        if (SymbolTable.get(identifier) == VarType.Bool)
        {
            BoolLabel boolLabel = ((BoolLabel) data);
            m_writer.println("if " + identifier + " == 1 goto " + boolLabel.lTrue);
            m_writer.println("goto " + boolLabel.lFalse);
        }
        return node.getValue();
    }

    @Override
    public Object visit(ASTIntValue node, Object data)
    {
        return Integer.toString(node.getValue());
    }

    public enum VarType
    {
        Bool, Number, EnumType, EnumVar, EnumValue
    }

    private static class BoolLabel
    {
        public String lTrue;
        public String lFalse;

        public BoolLabel(String lTrue, String lFalse)
        {
            this.lTrue = lTrue;
            this.lFalse = lFalse;
        }
    }
}
