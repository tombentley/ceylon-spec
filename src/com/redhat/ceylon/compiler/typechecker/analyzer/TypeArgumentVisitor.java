package com.redhat.ceylon.compiler.typechecker.analyzer;

import java.util.List;

import com.redhat.ceylon.compiler.typechecker.model.Declaration;
import com.redhat.ceylon.compiler.typechecker.model.ProducedType;
import com.redhat.ceylon.compiler.typechecker.model.TypeParameter;
import com.redhat.ceylon.compiler.typechecker.tree.Node;
import com.redhat.ceylon.compiler.typechecker.tree.Tree;
import com.redhat.ceylon.compiler.typechecker.tree.Visitor;

/**
 * Validates the position in which covariant and contravariant
 * type parameters appear in the schemas of declarations.
 * 
 * @author Gavin King
 *
 */
public class TypeArgumentVisitor extends Visitor {
    
    private boolean contravariant = false;
    private Declaration parameterizedDeclaration;
    
    private void flip() {
        contravariant = !contravariant;
    }
    
    @Override public void visit(Tree.ParameterList that) {
        flip();
        super.visit(that);
        flip();
    }
    
    @Override public void visit(Tree.TypeConstraint that) {
        super.visit(that);
        TypeParameter dec = that.getDeclarationModel();
        if (dec!=null) {
            parameterizedDeclaration = dec.getDeclaration();
            flip();
            if (that.getSatisfiedTypes()!=null) {
                for (Tree.Type type: that.getSatisfiedTypes().getTypes()) {
                    check(type, false, null);
                }
            }
            flip();
            parameterizedDeclaration = null;
        }
    }
    
    @Override public void visit(Tree.ParameterDeclaration that) {
        boolean topLevel = parameterizedDeclaration==null;
        if (topLevel) {
            parameterizedDeclaration = that.getParameterModel().getDeclaration();
        }
        super.visit(that);
        check(that.getTypedDeclaration().getType(), false, parameterizedDeclaration);
        if (topLevel) {
            parameterizedDeclaration = null;
        }
    }
    
    @Override public void visit(Tree.InitializerParameter that) {
        boolean topLevel = parameterizedDeclaration==null;
        if (topLevel) {
            parameterizedDeclaration = that.getParameterModel().getDeclaration();
        }
        super.visit(that);
        check(that.getParameterModel().getType(), false, parameterizedDeclaration, that);
        if (topLevel) {
            parameterizedDeclaration = null;
        }
    }
    
    @Override public void visit(Tree.TypedDeclaration that) {
        super.visit(that);
        if (!(that instanceof Tree.Variable)) {
            check(that.getType(), 
                    that.getDeclarationModel().isVariable(), 
                    that.getDeclarationModel());
        }
    }
    
    @Override public void visit(Tree.ClassOrInterface that) {
        super.visit(that);
        if (that.getSatisfiedTypes()!=null) {
            for (Tree.Type type: that.getSatisfiedTypes().getTypes()) {
                check(type, false, null);
            }
        }
    }
    
    @Override public void visit(Tree.ClassDeclaration that) {
        super.visit(that);
        if (that.getClassSpecifier()!=null) {
            check(that.getClassSpecifier().getType(), false, null);
        }
    }
    
    @Override public void visit(Tree.InterfaceDeclaration that) {
        super.visit(that);
        if (that.getTypeSpecifier()!=null) {
            check(that.getTypeSpecifier().getType(), false, null);
        }
    }
    
    @Override public void visit(Tree.TypeAliasDeclaration that) {
        super.visit(that);
        if (that.getTypeSpecifier()!=null) {
            check(that.getTypeSpecifier().getType(), false, null);
        }
    }
    
    @Override public void visit(Tree.AnyClass that) {
        super.visit(that);
        if (that.getExtendedType()!=null) {
            check(that.getExtendedType().getType(), false, null);
        }
    }
    
    @Override public void visit(Tree.FunctionArgument that) {}

    private void check(Tree.Type that, boolean variable, Declaration d) {
        if (that!=null) {
            check(that.getTypeModel(), variable, d, that);
        }
    }

    private void check(ProducedType type, boolean variable, Declaration d, Node that) {
        if (d==null || d.isShared() || d.getOtherInstanceAccess()) {
            if (type!=null) {
                List<TypeParameter> errors = type.checkVariance(!contravariant && !variable, 
                        contravariant && !variable, parameterizedDeclaration);
                displayErrors(that, type, errors);
            }
        }
    }

    private void displayErrors(Node that, ProducedType type,
            List<TypeParameter> errors) {
        for (TypeParameter td: errors) {
            String var; String loc;
            if ( td.isContravariant() ) {
                var = "contravariant (in)";
                loc = "covariant";
            }
            else if ( td.isCovariant() ) {
                var = "covariant (out)";
                loc = "contravariant";
            }
            else {
                throw new RuntimeException();
            }
            that.addError(var + " type parameter " + td.getName() + 
                    " appears in " + loc + " location in type: " + 
                    type.getProducedTypeName(that.getUnit()));
        }
    }
    
}