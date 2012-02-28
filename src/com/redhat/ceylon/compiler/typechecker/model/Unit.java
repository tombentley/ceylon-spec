package com.redhat.ceylon.compiler.typechecker.model;

import static com.redhat.ceylon.compiler.typechecker.model.Util.intersectionType;
import static com.redhat.ceylon.compiler.typechecker.model.Util.producedType;
import static com.redhat.ceylon.compiler.typechecker.model.Util.unionType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.redhat.ceylon.compiler.typechecker.tree.Tree.Identifier;

public class Unit {

	private Package pkg;
	private List<Import> imports = new ArrayList<Import>();
	private List<Declaration> declarations = new ArrayList<Declaration>();
	private String filename;
	private List<ImportList> importLists = new ArrayList<ImportList>();
	private Set<Identifier> unresolvedReferences = new HashSet<Identifier>();
	private Set<Declaration> duplicateDeclarations = new HashSet<Declaration>();
    
    public List<Import> getImports() {
        return imports;
    }

    public List<ImportList> getImportLists() {
        return importLists;
    }

    public Set<Identifier> getUnresolvedReferences() {
        return unresolvedReferences;
    }

    public Set<Declaration> getDuplicateDeclarations() {
        return duplicateDeclarations;
    }

    public Package getPackage() {
        return pkg;
    }

    public void setPackage(Package p) {
        pkg = p;
    }

    public List<Declaration> getDeclarations() {
        return declarations;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        return "Unit[" + filename + "]";
    }

    public Import getImport(String name) {
        for (Import i: getImports()) {
            if (i.getTypeDeclaration()==null &&
                    i.getAlias().equals(name)) {
                return i;
            }
        }
        return null;
    }
    
    /**
     * Search the imports of a compilation unit
     * for the named toplevel declaration.
     */
    public Declaration getImportedDeclaration(String name, 
            List<ProducedType> signature) {
        for (Import i: getImports()) {
            if (i.getTypeDeclaration()==null && 
            		i.getAlias().equals(name)) {
                //in case of an overloaded member, this will
                //be the "abstraction", so search for the 
                //correct overloaded version
                Declaration d = i.getDeclaration();
                return d.getContainer().getMember(d.getName(), signature);
            }
        }
        return null;
    }
    
    /**
     * Search the imports of a compilation unit
     * for the named member declaration.
     */
    public Declaration getImportedDeclaration(TypeDeclaration td, String name, 
            List<ProducedType> signature) {
        for (Import i: getImports()) {
            TypeDeclaration itd = i.getTypeDeclaration();
			if (itd!=null && itd.equals(td) && 
            		i.getAlias().equals(name)) {
                //in case of an overloaded member, this will
                //be the "abstraction", so search for the 
                //correct overloaded version
                Declaration d = i.getDeclaration();
                return d.getContainer().getMember(d.getName(), signature);
            }
        }
        return null;
    }
    
    public Map<String, DeclarationWithProximity> getMatchingImportedDeclarations(String startingWith, int proximity) {
    	Map<String, DeclarationWithProximity> result = new TreeMap<String, DeclarationWithProximity>();
        for (Import i: getImports()) {
            if (i.getAlias()!=null &&
                    i.getAlias().toLowerCase().startsWith(startingWith.toLowerCase())) {
                result.put(i.getAlias(), new DeclarationWithProximity(i, proximity));
            }
        }
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Unit) {
            Unit that = (Unit) obj;
            return that.getPackage().equals(getPackage())
                    && that.getFilename().equals(getFilename());
        }
        else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return getFilename().hashCode();
    }

    /**
     * Search for a declaration in the language module. 
     */
    public Declaration getLanguageModuleDeclaration(String name) {
        //all elements in ceylon.language are auto-imported
        //traverse all default module packages provided they have not been traversed yet
        Module languageModule = getPackage().getModule().getLanguageModule();
        if ( languageModule != null && languageModule.isAvailable() ) {
            if ("Bottom".equals(name)) {
                return getBottomDeclaration();
            }
            for (Package languageScope : languageModule.getPackages() ) {
                Declaration d = languageScope.getMember(name, null);
                if (d != null) {
                    return d;
                }
            }
        }
        return null;
    }
    
    public Interface getCorrespondenceDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Correspondence");
    }

    public Class getVoidDeclaration() {
        return (Class) getLanguageModuleDeclaration("Void");
    }
    
    public Class getNothingDeclaration() {
        return (Class) getLanguageModuleDeclaration("Nothing");
    }

    public Value getNullDeclaration() {
        return (Value) getLanguageModuleDeclaration("null");
    }

    public Interface getEmptyDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Empty");
    }

    public Interface getSequenceDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Sequence");
    }

    public Class getObjectDeclaration() {
        return (Class) getLanguageModuleDeclaration("Object");
    }
    
    public Class getIdentifiableObjectDeclaration() {
        return (Class) getLanguageModuleDeclaration("IdentifiableObject");
    }
    
    public Class getExceptionDeclaration() {
        return (Class) getLanguageModuleDeclaration("Exception");
    }

    public Interface getCategoryDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Category");
    }
    
    public Interface getIterableDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Iterable");
    }
    
    /**
     * Gets the declaration of {@code Iterator}
     * @return The declaration
     */
    public Interface getIteratorDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Iterator");
    }

    private Interface getFixedSizedDeclaration() {
        return (Interface) getLanguageModuleDeclaration("FixedSized");
    }

    private Interface getSomeDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Some");
    }

    private Interface getNoneDeclaration() {
        return (Interface) getLanguageModuleDeclaration("None");
    }

    public Interface getCallableDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Callable");
    }
    
    public Class getAbstractCallableDeclaration() {
        return (Class) getLanguageModuleDeclaration("AbstractCallable");
    }
    
    public ProducedType getAbstractCallableType(ProducedType returnType) {
        return com.redhat.ceylon.compiler.typechecker.model.Util.producedType(getAbstractCallableDeclaration(), returnType);
    }
    
    public Interface getCastableDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Castable");
    }
    
    public Interface getSummableDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Summable");
    }
    
    //TODO: remove!
    public Interface getSubtractableDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Subtractable");
    }
        
    public Interface getNumericDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Numeric");
    }
        
    public Interface getIntegralDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Integral");
    }
        
    public Interface getInvertableDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Invertable");
    }
        
    public Interface getSetDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Set");
    }
        
    public Interface getFixedSlotsDeclaration() {
        return (Interface) getLanguageModuleDeclaration("FixedSlots");
    }
        
    public TypeDeclaration getComparisonDeclaration() {
        return (TypeDeclaration) getLanguageModuleDeclaration("Comparison");
    }
        
    public TypeDeclaration getBooleanDeclaration() {
        return (TypeDeclaration) getLanguageModuleDeclaration("Boolean");
    }
        
    public TypeDeclaration getStringDeclaration() {
        return (TypeDeclaration) getLanguageModuleDeclaration("String");
    }
        
    public TypeDeclaration getFloatDeclaration() {
        return (TypeDeclaration) getLanguageModuleDeclaration("Float");
    }
    
    public TypeDeclaration getIntegerDeclaration() {
        return (TypeDeclaration) getLanguageModuleDeclaration("Integer");
    }
        
    public TypeDeclaration getCharacterDeclaration() {
        return (TypeDeclaration) getLanguageModuleDeclaration("Character");
    }
        
    public TypeDeclaration getQuotedDeclaration() {
        return (TypeDeclaration) getLanguageModuleDeclaration("Quoted");
    }
        
    /*public Interface getEqualityDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Equality");
    }*/
        
    public Interface getComparableDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Comparable");
    }
        
    public Interface getCloseableDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Closeable");
    }
        
    public Interface getOrdinalDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Ordinal");
    }
        
    public Class getRangeDeclaration() {
        return (Class) getLanguageModuleDeclaration("Range");
    }
        
    public Interface getRangedDeclaration() {
        return (Interface) getLanguageModuleDeclaration("Ranged");
    }
        
    public Class getEntryDeclaration() {
        return (Class) getLanguageModuleDeclaration("Entry");
    }
    
    ProducedType getCallableType(ProducedReference ref, ProducedType rt) {
    	if (ref.getType()==null ||
    	        ref.getType().getDeclaration() instanceof UnknownType) {
    		//special case for forward reference to member
    		//with inferred type TODO: something better
    		return new UnknownType(this).getType();
    	}
    	List<ProducedType> args = new ArrayList<ProducedType>();
    	args.add(rt);
    	if ( ref.getDeclaration() instanceof Functional) {
    	    for (ParameterList pl: ((Functional) ref.getDeclaration()).getParameterLists()) {
    	    	//TODO: support multiple parameter lists!!
    	    	for (Parameter p: pl.getParameters()) {
    	    		ProducedTypedReference np = ref.getTypedParameter(p);
    	    		if (np.getDeclaration() instanceof Functional) {
    	    			args.add(getCallableType(np, np.getType()));
    	    		}
    	    		else {
					    args.add(np.getType());
    	    		}
    	    	}
    	    }
    	}
    	return getCallableDeclaration().getProducedType(null, args);
    }
    
    public ProducedType getEmptyType(ProducedType pt) {
        return pt==null ? null :
            unionType(pt, getEmptyDeclaration().getType(), this);
        /*else if (isEmptyType(pt)) {
            //Nothing|Nothing|T == Nothing|T
            return pt;
        }
        else if (pt.getDeclaration() instanceof BottomType) {
            //Nothing|0 == Nothing
            return getEmptyDeclaration().getType();
        }
        else {
            UnionType ut = new UnionType();
            List<ProducedType> types = new ArrayList<ProducedType>();
            addToUnion(types,getEmptyDeclaration().getType());
            addToUnion(types,pt);
            ut.setCaseTypes(types);
            return ut.getType();
        }*/
    }
    
    public ProducedType getPossiblyNoneType(ProducedType pt) {
        return pt==null ? null :
            unionType(pt, producedType(getFixedSizedDeclaration(), 
                    getVoidDeclaration().getType()), this);
    }
    
    public ProducedType getOptionalType(ProducedType pt) {
        return pt==null ? null :
            unionType(pt, getNothingDeclaration().getType(), this);
        /*else if (isOptionalType(pt)) {
            //Nothing|Nothing|T == Nothing|T
            return pt;
        }
        else if (pt.getDeclaration() instanceof BottomType) {
            //Nothing|0 == Nothing
            return getNothingDeclaration().getType();
        }
        else {
            UnionType ut = new UnionType();
            List<ProducedType> types = new ArrayList<ProducedType>();
            addToUnion(types,getNothingDeclaration().getType());
            addToUnion(types,pt);
            ut.setCaseTypes(types);
            return ut.getType();
        }*/
    }
    
    public ProducedType getSequenceType(ProducedType et) {
        return producedType(getSequenceDeclaration(), et);
    }
    
    public ProducedType getIterableType(ProducedType et) {
        return producedType(getIterableDeclaration(), et);
    }

    public ProducedType getSetType(ProducedType et) {
        return producedType(getSetDeclaration(), et);
    }

    /**
     * Returns a ProducedType corresponding to {@code Iterator<T>}
     * @param et The ProducedType corresponding to {@code T}
     * @return The ProducedType corresponding to {@code Iterator<T>}
     */
    public ProducedType getIteratorType(ProducedType et) {
        return Util.producedType(getIteratorDeclaration(), et);
    }

    /**
     * Returns a ProducedType corresponding to {@code Range<T>}
     * @param rt The ProducedType corresponding to {@code T}
     * @return The ProducedType corresponding to {@code Range<T>}
     */
    public ProducedType getRangeType(ProducedType rt) {
        return Util.producedType(getRangeDeclaration(), rt);
    }

    public ProducedType getCastableType(ProducedType et) {
        return producedType(getCastableDeclaration(), et);
    }

    public ProducedType getEntryType(ProducedType kt, ProducedType vt) {
        return producedType(getEntryDeclaration(), kt, vt);
    }

    public ProducedType getKeyType(ProducedType type) {
        ProducedType st = type.getSupertype(getEntryDeclaration());
        if (st!=null && st.getTypeArguments().size()==2) {
            return st.getTypeArgumentList().get(0);
        }
        else {
            return null;
        }
    }

    public ProducedType getValueType(ProducedType type) {
        ProducedType st = type.getSupertype(getEntryDeclaration());
        if (st!=null && st.getTypeArguments().size()==2) {
            return st.getTypeArgumentList().get(1);
        }
        else {
            return null;
        }
    }

    public ProducedType getIteratedType(ProducedType type) {
        ProducedType st = type.getSupertype(getIterableDeclaration());
        if (st!=null && st.getTypeArguments().size()==1) {
            return st.getTypeArgumentList().get(0);
        }
        else {
            return null;
        }
    }

    public ProducedType getSetElementType(ProducedType type) {
        ProducedType st = type.getSupertype(getSetDeclaration());
        if (st!=null && st.getTypeArguments().size()==1) {
            return st.getTypeArgumentList().get(0);
        }
        else {
            return null;
        }
    }

    public ProducedType getFixedSizedElementType(ProducedType type) {
        ProducedType st = type.getSupertype(getFixedSizedDeclaration());
        if (st!=null && st.getTypeArguments().size()==1) {
            return st.getTypeArgumentList().get(0);
        }
        else {
            return null;
        }
    }

    public ProducedType getDefiniteType(ProducedType pt) {
        return intersectionType(getObjectDeclaration().getType(), 
                pt, pt.getDeclaration().getUnit());
        /*if (pt.getDeclaration().equals(getVoidDeclaration())) {
            return getObjectDeclaration().getType();
        }
        else {
            return pt.minus(getNothingDeclaration());
        }*/
    }

    public ProducedType getNonemptyType(ProducedType pt) {
        return intersectionType(producedType(getSomeDeclaration(), 
                getFixedSizedElementType(pt)), pt, 
                pt.getDeclaration().getUnit());
        /*if (pt.getDeclaration().equals(getVoidDeclaration())) {
            return getObjectDeclaration().getType();
        }
        else {
            return pt.minus(getNothingDeclaration());
        }*/
    }

    public ProducedType getNonemptyDefiniteType(ProducedType pt) {
        return getNonemptyType(getDefiniteType(pt));
    }

    public ProducedType getNonemptySequenceType(ProducedType pt) {
        return pt.minus(getEmptyDeclaration()).getSupertype(getSequenceDeclaration());
    }
    
    public ProducedType getNonemptyIterableType(ProducedType pt) {
        return pt.minus(getEmptyDeclaration()).getSupertype(getIterableDeclaration());
    }
    
    public boolean isEntryType(ProducedType pt) {
        return pt.getSupertype(getEntryDeclaration())!=null;
    }
    
    public boolean isIterableType(ProducedType pt) {
        return pt.getSupertype(getIterableDeclaration())!=null;
    }
    
    public boolean isOptionalType(ProducedType pt) {
        //shortcut way of testing that the given type 
        //has a nonempty intersection with Nothing
        return getNullDeclaration().getType().isSubtypeOf(pt);
                //&& !pt.getDeclaration().equals(getVoidDeclaration());
    }
    
    public boolean isEmptyType(ProducedType pt) {
        //must be a subtype of FixedSized<Void>?
        return unionType(producedType(getFixedSizedDeclaration(), 
                    getVoidDeclaration().getType()), 
                    getNothingDeclaration().getType(), this)
                .isSupertypeOf(pt) &&
        //must have non-empty intersection with None<Bottom>
                !(intersectionType(producedType(getNoneDeclaration(),
                    getBottomDeclaration().getType()), pt, this)
                .getDeclaration() instanceof BottomType);
    }
    
    public ProducedType getElementType(ProducedType pt) {
        ProducedType st = getNonemptySequenceType(pt);
        if (st!=null && st.getTypeArguments().size()==1) {
            return st.getTypeArgumentList().get(0);
        }
        else {
            return null;
        }
    }
    
    public BottomType getBottomDeclaration() {
        return new BottomType(this);
    }
    
}
