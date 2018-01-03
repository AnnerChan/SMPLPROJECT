package smpl.syntax;

import smpl.semantics.Visitor;
import smpl.sys.SmplException;
import java.util.*;

public class StmtDefinition extends Exp{

	ArrayList<String> vars;
	ArrayList<Exp> exps;
	ExpVectorRef vr;
	Exp r,e;
	String vars;

	public StmtDefinition(ArrayList<String> v, ArrayList<Exp> e){
		vars = v;
		exps = e;
	}

	public StmtDefinition(String v, Exp e){
		vars = v;
		exps = e;
	}
	
	public StmtDefinition(ExpVectorRef vr, Exp r){
		this.vr = vr;
		this.r = r;
	}

  public Exp getExp(){
		return r;
	}
  
	public ArrayList<String> getVars(){
		return vars;
	}

	public ArrayList<Exp> getExps(){
		return exps;
	}

	public ExpVectorRef getVectorRef(){
		return vr;
	}

	@Override
	public <S, T> T visit(Visitor<S, T> v, S arg) throws SmplException {
		return v.visitStmtDefinition(this, arg);
	}

	@Override
	public String toString() {
		if(vr != null){
      		return r.toString();
  		}
		else{
      		return vars.toString();
      	}
	}
}
