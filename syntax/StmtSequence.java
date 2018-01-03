package smpl.syntax;

import smpl.semantics.Visitor;
import smpl.sys.SmplException;
import java.util.*;
import smpl.syntax.Exp;

public class StmtSequence extends Statement {

    ArrayList<Statement> seq;		

    public StmtSequence() {
	   seq = new ArrayList<>();
    }

    public StmtSequence(Statement s) {
	   this();
	   seq.add(s);
    }
    
    public StmtSequence(Exp s) {
	   this();
	   seq.add(s);
    }

    public ArrayList<Statement> getSeq() {
	   return seq;
    }

    public StmtSequence add(Statement s) {
	   seq.add(s);
	   return this;
    }
    
    public StmtSequence add(Exp s) {
	   seq.add(s);
	   return this;
    }

    @Override
    public <S,T> T visit(Visitor<S, T> v, S arg) throws SmplException {
    	return v.visitStmtSequence(this, arg);
    }

    @Override
    public String toString() {
	Iterator iter = seq.iterator();

    	String result = "";
    	while (iter.hasNext()) {
    	    result = result + iter.next().toString() + "\n";
    	}
    	return result;
    }

}

